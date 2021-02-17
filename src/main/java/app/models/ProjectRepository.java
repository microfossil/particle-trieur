package main.java.app.models;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;
import main.java.app.App;
import main.java.app.helpers.BeanCopyService;
import main.java.app.models.processing.ProcessingInfo;
import main.java.app.models.project.Particle;
import main.java.app.models.project.Project;
import main.java.app.models.project.Tag;
import main.java.app.models.project.Taxon;
import main.java.app.xml.RelativePathAdapter;
import javafx.application.Platform;
import org.apache.commons.io.FilenameUtils;

import javax.xml.bind.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class ProjectRepository {

    private Project project;
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    public ProjectRepository(Project project) {
        this.project = project;
        try {
            createMarshallers();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void createMarshallers() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Project.class);
        marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        unmarshaller = context.createUnmarshaller();
    }

//    private void saveAsFastInfoSet(File file) throws JAXBException, FileNotFoundException, XMLStreamException {
//        marshaller.setAdapter(new RelativePathAdapter(file.getParent()));
//
//        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
//
//
//        Platform.runLater(() -> {
//            project.setFile(file);
//            App.getPrefs().setRecentProject(project.getFile().getAbsolutePath());
//            project.setIsDirty(false);
//        });
//    }

    public void saveAs(File file) throws JAXBException, IOException, XMLStreamException, RepositoryException {
//        if (file.exists()) {
//            file.delete();
//        }
//        OutputStream stream;
//        if (FilenameUtils.getExtension(file.getAbsolutePath()).equals("miso")) {
//            stream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
//            ZipEntry zipFile = new ZipEntry("project.xml");
//            ((ZipOutputStream)stream).putNextEntry(zipFile);
//        }
//        else {
//            stream = new FileOutputStream(file);
//        }
        String tempFilename = FilenameUtils.removeExtension(file.getAbsolutePath()) + ".temp";
        File temporaryNewFile = new File(tempFilename);
        if (temporaryNewFile.exists()) temporaryNewFile.delete();
        marshaller.setAdapter(new RelativePathAdapter(file.getParent()));

        //BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(temporaryNewFile));
        boolean isZip = FilenameUtils.getExtension(file.getAbsolutePath()).equals("miso");

        //Save to memory
        ByteArrayOutputStream memoryOutputStream = new ByteArrayOutputStream();
        if (isZip) {
            StAXDocumentSerializer sw = new StAXDocumentSerializer();
            sw.setOutputStream(memoryOutputStream);
            marshaller.marshal(project, (XMLStreamWriter) sw);
            sw.flush();
            sw.close();
        }
        else {
            marshaller.marshal(project, memoryOutputStream);
            memoryOutputStream.flush();
            memoryOutputStream.close();
        }

        long memorySize = memoryOutputStream.size();

        //Copy memory to file
        FileOutputStream fos = new FileOutputStream(temporaryNewFile);
        memoryOutputStream.writeTo(fos);
        fos.close();

        long fileSize = temporaryNewFile.length();

        if (memorySize != fileSize) {
            temporaryNewFile.delete();
            throw new RepositoryException(String.format("The saved project file size (%d) does not match the expected size (%d)", fileSize, memorySize));
        }

        //Rename old file to xxxx.backup
        String oldFilename = FilenameUtils.removeExtension(file.getAbsolutePath()) + ".old";
        File oldFile = new File(oldFilename);
        if (oldFile.exists()) oldFile.delete();
        file.renameTo(oldFile);

        //Rename temporary to proper name
        if (file.exists()) file.delete();
        temporaryNewFile.renameTo(file);

        Platform.runLater(() -> {
            project.setFile(file);
            App.getPrefs().setRecentProject(project.getFile().getAbsolutePath());
            project.setIsDirty(false);
        });
    }

    public void save() throws JAXBException, IOException, XMLStreamException, RepositoryException {
        saveAs(project.getFile());
    }

    public void load(File file) throws JAXBException, IOException, XMLStreamException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        unmarshaller.setAdapter(new RelativePathAdapter(file.getParent()));

        boolean isZip = FilenameUtils.getExtension(file.getAbsolutePath()).equals("miso");

        FileInputStream fis = new FileInputStream(file);

        Project loaded;
        if (isZip) {
            XMLStreamReader stream = new StAXDocumentParser(fis);
            loaded = (Project) unmarshaller.unmarshal(stream);
            stream.close();
        }
        else {
            loaded = (Project) unmarshaller.unmarshal(fis);
            fis.close();
        }

        //Remove all
        project.resetToDefaults();

        project.taxons.putAll(loaded.taxons);
        project.tags.putAll(loaded.tags);

        project.addRequiredTaxons();
        project.addRequiredTags();
        project.setFile(file);

        project.setNetworkDefinition(loaded.getNetworkDefinition());

        BeanCopyService.copy(loaded.processingInfo, project.processingInfo, ProcessingInfo.class);

        project.taxonsUpdatedEvent.broadcast(null);
        project.tagsUpdatedEvent.broadcast(null);

        project.particles.addAll(loaded.particles);
        //Update ui properties
        for (Particle particle : project.particles) {
            particle.initUIProperties();
        }

        if (project.particles.size() > 0) project.particleAddedEvent.broadcast(project.particles.get(0));

        project.setIsDirty(false);
    }

    public void open(File file) throws JAXBException, IOException, XMLStreamException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        Optional<Project> project = Optional.empty();
        if (file != null && file.exists()) {
            load(file);
            App.getPrefs().setRecentProject(file.getAbsolutePath());
        }
    }

    public void loadFromTemplate(File file) throws JAXBException, IOException, XMLStreamException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        if (file != null && file.exists()) {
            load(file);
            project.particles.clear();
            project.setFile(null);
        }
    }

    public void importFromOther(File file) throws JAXBException {
        if (file != null && file.exists()) {
            unmarshaller.setAdapter(new RelativePathAdapter(file.getParent()));
            Project loaded = (Project) unmarshaller.unmarshal(file);

            boolean update = false;
            for (Taxon taxon : loaded.taxons.values()) {
                if (!project.taxons.containsKey(taxon.getCode())) {
                    project.taxons.put(taxon.getCode(), taxon);
                    update = true;
                }
            }
            if (update) project.taxonsUpdatedEvent.broadcast();

            update = false;
            for (Tag tag : loaded.tags.values()) {
                if (!project.tags.containsKey(tag.getCode())) {
                    project.tags.put(tag.getCode(), tag);
                    update = true;
                }
            }
            if (update) project.taxonsUpdatedEvent.broadcast();

            project.addParticles(loaded.particles);
        }
    }

    public Project cloneByXML() throws JAXBException {
        if (project.getFile() == null) {
            marshaller.setAdapter(new RelativePathAdapter(System.getProperty("user.home")));
        }
        else {
            marshaller.setAdapter(new RelativePathAdapter(project.getFile().getParent()));
        }
        //Write to memory
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        marshaller.marshal(project, outputStream);
        //Read back from memory
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        if (project.getFile() == null) {
            unmarshaller.setAdapter(new RelativePathAdapter(System.getProperty("user.home")));
        }
        else {
            unmarshaller.setAdapter(new RelativePathAdapter(project.getFile().getParent()));
        }
        Project cloned = (Project) unmarshaller.unmarshal(inputStream);
        //Initialise
        for (Particle particle : cloned.particles) {
            particle.initUIProperties();
        }
        return cloned;
    }

    public class RepositoryException extends Exception {
        public RepositoryException(String message) {
            super(message);
        }
    }
}
