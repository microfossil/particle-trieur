/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.models.project;

import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.helpers.CSEvent;
import ordervschaos.particletrieur.app.models.network.classification.Classification;
import ordervschaos.particletrieur.app.models.network.classification.ClassificationSet;
import ordervschaos.particletrieur.app.models.network.classification.NetworkInfo;
import ordervschaos.particletrieur.app.models.processing.ProcessingInfo;

import java.io.File;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ordervschaos.particletrieur.app.services.ExtractMetadataFromFilenamesService;
import ordervschaos.particletrieur.app.xml.TagMapAdapter;
import ordervschaos.particletrieur.app.xml.TaxonMapAdapter;

import java.io.Serializable;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang3.ObjectUtils;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@XmlRootElement(name = "project")
@XmlAccessorType(XmlAccessType.NONE)
public class Project implements Serializable {
    @XmlAttribute(name = "version")
    public String version = ObjectUtils.firstNonNull(App.class.getPackage().getImplementationVersion(), "dev");

    //Taxons
    @XmlElement(name = "taxons")
    @XmlJavaTypeAdapter(TaxonMapAdapter.class)
    public LinkedHashMap<String, Taxon> taxons;

    public LinkedHashMap<String, Taxon> getTaxons() {
        return taxons;
    }

    //Tags
    @XmlElement(name = "tags")
    @XmlJavaTypeAdapter(TagMapAdapter.class)
    public LinkedHashMap<String, Tag> tags;

    public LinkedHashMap<String, Tag> getTags() {
        return tags;
    }

    //Particle samples
    public ObservableList<Particle> particles;

    @XmlElementWrapper(name = "images")
    @XmlElements({@XmlElement(name = "image", type = Particle.class)})
    public List<Particle> getParticles() {
        return particles;
    }

    //Network    
    private ObjectProperty<NetworkInfo> networkDefinition = new SimpleObjectProperty<>();
    public ObjectProperty<NetworkInfo> networkDefinitionProperty() {
        return networkDefinition;
    }

    @XmlElement(name = "network")
    public NetworkInfo getNetworkDefinition() {
        return networkDefinition.get();
    }

    public void setNetworkDefinition(NetworkInfo value) {
        networkDefinition.set(value);
        if (value != null) {
            List<String> codes = value.labels.stream().map(networkLabel -> networkLabel.code).collect(Collectors.toList());
            addTaxonsIfMissing(codes);
            setIsDirty(true);
        }
    }

    //Processing (observable values contained within)
    @XmlElement
    public ProcessingInfo processingInfo;

    //The project file
    private final ObjectProperty<File> file = new SimpleObjectProperty<>();
    public File getFile() {
        return file.get();
    }
    public void setFile(File value) {
        file.set(value);
    }
    public ObjectProperty<File> fileProperty() {
        return file;
    }

    //Constants
    public static final String UNLABELED_CODE = "unlabeled";
    public static final String UNSURE_CODE = "unsure";
    public static final String DUPLICATE_CODE = "duplicate";
    public static final String MISSING_CODE = "missing";
    public static final String AUTO_CODE = "auto";

    public final String[] requiredTaxons = {UNLABELED_CODE, UNSURE_CODE};
    public final String[] requiredTags = {DUPLICATE_CODE, AUTO_CODE};

    //Status
    private final BooleanProperty isDirty = new SimpleBooleanProperty(false);

    public BooleanProperty isDirtyProperty() {
        return isDirty;
    }

    public void setIsDirty(Boolean value) {
        isDirty.set(value);
    }

    public Boolean getIsDirty() {
        return isDirty.get();
    }

    //Events
    public CSEvent taxonsUpdatedEvent = new CSEvent();
    public CSEvent tagsUpdatedEvent = new CSEvent();
    public CSEvent<Particle> particleAddedEvent = new CSEvent<>();
    public CSEvent newProjectEvent = new CSEvent();


    /**
     * Create a app
     */
    public Project() {
        particles = FXCollections.observableArrayList();
        taxons = new LinkedHashMap<>();
        tags = new LinkedHashMap<>();
        setNetworkDefinition(null);
        processingInfo = new ProcessingInfo();
        addDefaultTags();
        addRequiredTaxons();
        addRequiredTags();
        setIsDirty(false);
    }

    public void resetToDefaults() {
        particles.clear();
        taxons.clear();
        tags.clear();
        processingInfo.resetToDefaults();
        setFile(null);
        addRequiredTaxons();
        addRequiredTags();
        setNetworkDefinition(null);
        taxonsUpdatedEvent.broadcast();
        tagsUpdatedEvent.broadcast();
        setIsDirty(false);
        newProjectEvent.broadcast();
    }

    private void addDefaultTags() {
        tags.put("trunc", new Tag("trunc", "Truncated", "The particle is truncated."));
        tags.put("dirty", new Tag("dirty", "Dirty", "The particle is dirty."));
        tags.put("fuzzy", new Tag("fuzzy", "Fuzzy", "The image is blurry."));
        tags.put("best", new Tag("best", "Best Example", "The image is a good example of the particular class."));
    }

    public void addRequiredTaxons() {
        taxons.put(UNLABELED_CODE, new Taxon(UNLABELED_CODE, "Not classified", "The default state for all unclassified images.", "other", false));
        taxons.put(UNSURE_CODE, new Taxon(UNSURE_CODE, "Class is unsure", "None of the classes identified by the CNN are above the threshold", "other", false));
        //taxons.put("other", new Taxon("other", "Another morphotype", "The image is not in the current taxonomy.", false));
    }

    public void addRequiredTags() {
        tags.put(DUPLICATE_CODE, new Tag(DUPLICATE_CODE, "Duplicate", "The image is a duplicate of another in the set."));
        tags.put(AUTO_CODE, new Tag(AUTO_CODE, "Automatically classified", "The image was automatically classified."));
    }
        
    
    /*
    LOADING AND SAVING TO XML
    */


    /*
    FORAMS
    */
    public void addParticle(Particle particle) {
        particles.add(particle);
        boolean updateTaxons = false;
        boolean updateTags = false;
        for (String code : particle.getClassifications().classifications.keySet()) {
            if (!taxons.containsKey(code)) {
                try {
                    addTaxon(new Taxon(code, "", "", "auto", true));
                } catch (TaxonAlreadyExistsException ex) {
                    //Should never get here
                }
                updateTaxons = true;
            }
        }
        for (String code : particle.tags) {
            if (!tags.containsKey(code)) {
                try {
                    addTag(new Tag(code, "", ""));
                } catch (TagAlreadyExistsException ex) {
                    //Should never get here
                }
                updateTags = true;
            }
        }
        particleAddedEvent.broadcast(particle);
        if (updateTaxons) taxonsUpdatedEvent.broadcast(null);
        if (updateTags) tagsUpdatedEvent.broadcast(null);
        setIsDirty(true);
    }

    public void addParticles(List<Particle> particles) {
        boolean updateTaxons = false;
        boolean updateTags = false;
        this.particles.addAll(particles);
        for (Particle particle : particles) {
            for (String code : particle.getClassifications().classifications.keySet()) {
                if (!taxons.containsKey(code)) {
                    try {
                        addTaxon(new Taxon(code, "", "", "", true));
                    } catch (TaxonAlreadyExistsException ex) {
                        //Should never get here
                    }
                    updateTaxons = true;
                }
            }
            for (String code : particle.tags) {
                if (!tags.containsKey(code)) {
                    try {
                        addTag(new Tag(code, "", ""));
                    } catch (TagAlreadyExistsException ex) {
                        //Should never get here
                    }
                    updateTags = true;
                }
            }
        }
        particleAddedEvent.broadcast(particles.get(0));
        if (updateTaxons) taxonsUpdatedEvent.broadcast(null);
        if (updateTags) tagsUpdatedEvent.broadcast(null);
        setIsDirty(true);
    }

    public void removeParticle(Particle particle) {
        particles.remove(particle);
        setIsDirty(true);
    }

    public void removeParticles(List<Particle> particles) {
        if (particles.size() > 0) {
            this.particles.removeAll(particles);
            setIsDirty(true);
        }
    }

//    public void addImages(File dir) {
//        Collection<File> fileCollection = FileUtils.listFiles(dir, new String[]{"bmp", "png", "tiff", "tif", "jpg", "jpeg"}, true);
//        if (fileCollection.size() > 0) {
//            ArrayList<Particle> toAdd = new ArrayList<>();
//            for (File file : fileCollection) {
//                toAdd.add(new Particle(file, UNLABELED_CODE, ""));
//            }
//            addParticles(toAdd);
//        }
//    }
//
//    public void addImages(List<File> files, Particle template) {
//        if (files.size() > 0) {
//            ArrayList<Particle> toAdd = new ArrayList<>();
//            for (File file : files) {
//                Particle particle = new Particle(file);
//                particle.clearAndAddClassification(UNLABELED_CODE, 1.0, "");
//                particle.setSampleID(template.getSampleID());
//                particle.setIndex1(template.getIndex1());
//                particle.setIndex2(template.getIndex2());
//                toAdd.add(particle);
//            }
//            addParticles(toAdd);
//        }
//    }
//
//    public void addImages(List<File> files) {
//        if (files.size() > 0) {
//            ArrayList<Particle> toAdd = new ArrayList<>();
//            for (File file : files) {
//                toAdd.add(new Particle(file, UNLABELED_CODE, ""));
//            }
//            addParticles(toAdd);
//        }
//    }
//
//    public void importImages(List<File> files) {
//        if (files.size() > 0) {
//            if (file != null) {
//                File parentDirectory = getFile().getParentFile();
//                File imagesDirectory = Paths.get(parentDirectory.getAbsolutePath(), "images").toFile();
//                try {
//                    FileUtils.forceMkdir(imagesDirectory);
//                    for (File fileitem : files) {
//                        File outputFile = Paths.get(imagesDirectory.getAbsolutePath(), fileitem.getName()).toFile();
//                        Files.copy(getFile().toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//                        particles.add(new Particle(getFile(), UNLABELED_CODE, ""));
//                    }
//                } catch (IOException ex) {
//                    Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            ArrayList<Particle> toAdd = new ArrayList<>();
//            for (File file : files) {
//                toAdd.add(new Particle(file, UNLABELED_CODE, ""));
//            }
//            addParticles(toAdd);
//        }
//    }
//



    /*
    FORAMS
    */
    public void setParticleLabel(List<Particle> particles, String code, double score, String classifier, boolean clearOthers)
            throws TaxonDoesntExistException {
        if (taxons.containsKey(code)) {
            for (Particle particle : particles) {
                if (clearOthers) {
                    particle.clearAndAddClassification(code, 1.0, classifier);
                } else {
                    particle.addClassification(code, score, classifier);
                    if (particle.getClassificationsAsList().isEmpty()) {
                        particle.addClassification(UNLABELED_CODE, 1.0, "");
                    }
                }
            }
            setIsDirty(true);
        } else {
            throw new TaxonDoesntExistException("Taxon doesn't exist");
        }
    }

    public void setParticleLabel(Particle particle, String code, double score, String classifier, boolean clearOthers)
            throws TaxonDoesntExistException {
        if (taxons.containsKey(code)) {
            if (clearOthers) {
                particle.clearAndAddClassification(code, 1.0, classifier);
            } else {
                particle.addClassification(code, score, classifier);
                if (particle.getClassificationsAsList().isEmpty()) {
                    particle.addClassification(UNLABELED_CODE, 1.0, "");
                }
            }
            setIsDirty(true);
        } else {
            throw new TaxonDoesntExistException("Taxon doesn't exist");
        }
    }

    public void setParticleLabelSet(Particle particle, ClassificationSet classificationSet) {
        ArrayList<String> taxonsToAdd = new ArrayList<>();
        for (Classification cls : classificationSet.classifications.values()) {
            taxonsToAdd.add(cls.getCode());
        }
        addTaxonsIfMissing(taxonsToAdd);
        particle.setClassifications(classificationSet, 0, false);
        setIsDirty(true);
    }

    public void setParticleLabelSet(Particle particle, ClassificationSet classificationSet, double threshold, boolean wasAuto) {
        ArrayList<String> taxonsToAdd = new ArrayList<>();
        for (Classification cls : classificationSet.classifications.values()) {
            taxonsToAdd.add(cls.getCode());
        }
        addTaxonsIfMissing(taxonsToAdd);
        particle.setClassifications(classificationSet, threshold, wasAuto);
        setIsDirty(true);
    }

    public void setParticleTag(List<Particle> particles, String code) throws TagDoesntExistException {
        if (tags.containsKey(code)) {
            for (Particle particle : particles) {
                particle.toggleTag(code);
            }
            setIsDirty(true);
        } else {
            throw new TagDoesntExistException("Tag doesn't exist");
        }
    }

    public void setParticleTag(Particle particle, String code) throws TagDoesntExistException {
        if (tags.containsKey(code)) {
            particle.toggleTag(code);
            setIsDirty(true);
        } else {
            throw new TagDoesntExistException("Tag doesn't exist");
        }
    }

    public void setParticleQuality(Particle particle, int rating) {
        particle.setImageQuality(rating);
        setIsDirty(true);
    }

    public void setParticleQuality(List<Particle> particles, int rating) {
        for (Particle particle : particles) {
            particle.setImageQuality(rating);
        }
        setIsDirty(true);
    }

    public void setParticleMetadata(List<Particle> particles, String sampleID, Double index1, Double index2, Double resolution) {
        if (particles.size() > 0) {
            for (Particle particle : particles) {
                if (index1 != null) {
                    particle.setIndex1(index1);
                }
                if (index2 != null) {
                    particle.setIndex2(index2);
                }
                if (sampleID != null) particle.setSampleID(sampleID);
                if (resolution != null) particle.setResolution(resolution);
            }
            setIsDirty(true);
        }
    }

    public void setParticleMetadata(Particle particle, String sampleID, Double index1, Double index2, Double resolution) {
        List<Particle> list = new ArrayList<>();
        list.add(particle);
        setParticleMetadata(list, sampleID, index1, index2, resolution);
    }

    public void setParticleMetadata(List<ExtractMetadataFromFilenamesService.MetadataUpdatePayload> payloads) {
        ArrayList<String> codes = new ArrayList<>();
        for (ExtractMetadataFromFilenamesService.MetadataUpdatePayload payload : payloads) {
            if (payload.label != null && !taxons.containsKey(payload.label) && !codes.contains(payload.label)) {
                codes.add(payload.label);
            }
        }
        addTaxonsIfMissing(codes);
        for (ExtractMetadataFromFilenamesService.MetadataUpdatePayload payload : payloads) {
            if (payload.label != null) payload.particle.clearAndAddClassification(payload.label, 1, "from_filename");
            if (payload.index1 != null) payload.particle.setIndex1(payload.index1);
            if (payload.index2 != null) payload.particle.setIndex2(payload.index2);
            if (payload.sample != null) payload.particle.setSampleID(payload.sample);
            if (payload.guid != null) payload.particle.setGUID(payload.guid);
        }
        setIsDirty(true);
    }

    public void setParticleCNNVector(Particle particle, float[] vector) {
        particle.setCNNVector(vector);
        //TODO this is a complete hack
        //Update - not needed any more
//        if (getIsDirty() == false) {
//            if (Platform.isFxApplicationThread()) setIsDirty(true);
//            else Platform.runLater(() -> { setIsDirty(true); });
//        }
    }

    public void setParticleShape(Particle particle, int height, int width) {
        particle.setImageHeight(height);
        particle.setImageWidth(width);
        //TODO this is a complete hack
//        if (getIsDirty() == false) {
//            if (Platform.isFxApplicationThread()) setIsDirty(true);
//            else Platform.runLater(() -> { setIsDirty(true); });
//        }
    }

    public void setParticleFilename(Particle particle, String filename) {
        particle.setFilename(filename);
        setIsDirty(true);
    }

    public void setParticleValidator(Particle particle, String validator) {
        if (validator != null) {
            particle.validate(validator);
            setIsDirty(true);
        }
    }

    public void setParticleValidator(List<Particle> particles, String validator) {
        if (validator != null) {
            for (Particle particle : particles) {
                particle.validate(validator);
            }
            setIsDirty(true);
        }
    }

    /*
    TAXONS
    */
    public class TaxonAlreadyExistsException extends Exception {
        public TaxonAlreadyExistsException(String message) {
            super(message);
        }
    }

    public class TaxonDoesntExistException extends Exception {
        public TaxonDoesntExistException(String message) {
            super(message);
        }
    }

    public void updateTaxon(String code, Taxon updated) throws TaxonAlreadyExistsException {
        boolean isSame = code.equals(updated.getCode());
        boolean exists = taxons.containsKey(updated.getCode());
        if (!isSame && exists) {
            throw new TaxonAlreadyExistsException("Taxon already exists");
        } else {
            //Update
            Taxon target = taxons.get(code);
            target.setCode(updated.getCode());
            target.setDescription(updated.getDescription());
            target.setName(updated.getName());
            target.setIsClass(updated.getIsClass());

            //Gymnastics to change the key
            LinkedHashMap<String,Taxon> newTaxons = new LinkedHashMap<>();
            Collection<Taxon> taxonSet = taxons.values();
            for (Taxon taxon : taxonSet) {
                if (taxon == target) newTaxons.put(target.getCode(), target);
                else newTaxons.put(taxon.getCode(), taxon);
            }
            taxons = newTaxons;

            // Classifications are indexed by CODE
            for (Particle particle : particles) {
                if (particle.classification.get().equals(code)) {
                    particle.getClassifications().modifyKey(code, target.getCode());
                    particle.initUIProperties();
                }
            }
            taxonsUpdatedEvent.broadcast(null);
            setIsDirty(true);
        }
    }

    public void addTaxon(Taxon updated) throws TaxonAlreadyExistsException {
        if (taxons.containsKey(updated.getCode())) {
            throw new TaxonAlreadyExistsException("Taxon already exists");
        } else {
            //Some gymnastics to ensure the nex taxon is in the same position in the list
            taxons.put(updated.getCode(), updated);
            taxonsUpdatedEvent.broadcast();
            setIsDirty(true);
        }
    }

    public void addTaxonsIfMissing(List<String> codes) {
        boolean wasNew = false;
        for (String code : codes) {
            if (code == null || code.equals("")) continue;
            if (taxons.putIfAbsent(code, new Taxon(code, "", "", "", true)) == null) wasNew = true;
        }
        if (wasNew) taxonsUpdatedEvent.broadcast();
    }

    public void deleteTaxon(Taxon taxon) throws TaxonDoesntExistException {
        if (!taxons.containsKey(taxon.getCode())) {
            throw new TaxonDoesntExistException("Taxon doesn't exist");
        } else {
            for (Particle particle : particles) {
                if (particle.classification.get().equals(taxon.getCode())) {
                    particle.getClassifications().modifyKey(taxon.getCode(), UNLABELED_CODE);
                    particle.initUIProperties();
                }
            }
            taxons.remove(taxon.getCode());
        }
        taxonsUpdatedEvent.broadcast(null);
        setIsDirty(true);
    }

    public void initialiseTaxons(List<Taxon> taxons) {
        this.taxons.clear();
        for (Taxon taxon : taxons) {
            this.taxons.put(taxon.getCode(), taxon);
        }
        addRequiredTaxons();
        taxonsUpdatedEvent.broadcast(null);
        setIsDirty(true);
    }


    /*
    TAGS
    */
    public class TagAlreadyExistsException extends Exception {
        public TagAlreadyExistsException(String message) {
            super(message);
        }
    }

    public class TagDoesntExistException extends Exception {
        public TagDoesntExistException(String message) {
            super(message);
        }

    }

    public void updateTag(String code, Tag updated) throws TagAlreadyExistsException {
        boolean isSame = code.equals(updated.getCode());
        boolean exists = tags.containsKey(updated.getCode());
        if (!isSame && exists) {
            throw new TagAlreadyExistsException("Tag already exists");
        } else {
            //Some gymnastics to ensure the nex taxon is in the same position in the list
            Tag target = tags.get(code);
            target.setCode(updated.getCode());
            target.setName(updated.getName());
            target.setDescription(updated.getDescription());

            //Gymnastics to change key
            LinkedHashMap<String,Tag> newTags = new LinkedHashMap<>();
            for (Tag tag : tags.values()) {
                if (tag == target) newTags.put(target.getCode(), target);
                else newTags.put(tag.getCode(), tag);
            }
            tags = newTags;

            for (Particle particle : particles) {
                if (particle.tags.contains(code)) {
                    particle.tags.remove(code);
                    particle.tags.add(target.getCode());
                    particle.initUIProperties();
                }
            }
            tagsUpdatedEvent.broadcast(null);
            setIsDirty(true);
        }
    }

    public void addTag(Tag updated) throws TagAlreadyExistsException {
        if (tags.containsKey(updated.getCode())) {
            throw new TagAlreadyExistsException("Tag already exists");
        } else {
            //Some gymnastics to ensure the nex taxon is in the same position in the list
            tags.put(updated.getCode(), updated);
            tagsUpdatedEvent.broadcast(null);
            setIsDirty(true);
        }
    }

    public void deleteTag(Tag tag) throws TaxonDoesntExistException {
        if (!tags.containsKey(tag.getCode())) {
            throw new TaxonDoesntExistException("Tag doesn't exist");
        } else {
            for (Particle particle : particles) {
                if (particle.classification.get().equals(tag.getCode())) {
                    particle.removeTag(tag.getCode());
                    particle.initUIProperties();
                }
            }
            tags.remove(tag.getCode());
            tagsUpdatedEvent.broadcast(null);
            setIsDirty(true);
        }
    }

    public void initialiseTags(List<Tag> tags) {
        this.tags.clear();
        for (Tag tag : tags) {
            this.tags.put(tag.getCode(), tag);
        }
        addRequiredTags();
        tagsUpdatedEvent.broadcast(null);
        setIsDirty(true);
    }
}
