/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.models.project;

import particletrieur.models.network.classification.Classification;
import particletrieur.models.network.classification.ClassificationSet;
import particletrieur.models.processing.Morphology;
import particletrieur.models.processing.processors.MatUtilities;
import particletrieur.xml.ParametersMapAdapter;
import particletrieur.xml.RelativePathAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import io.seruco.encoding.base62.Base62;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.opencv.core.Mat;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@XmlRootElement(name = "image")
@XmlAccessorType(XmlAccessType.NONE)
public class Particle {

    //File
    private final ObjectProperty<File> file = new SimpleObjectProperty<>();
    public File getFile() {
        return file.get();
    }
    private void setFile(File value) {
        file.set(value);
    }
    public ObjectProperty fileProperty() {
        return file;
    }

    //Filename
    private final StringProperty filename = new SimpleStringProperty();
    @XmlElement
    @XmlPath("source/filename/text()")
    @XmlJavaTypeAdapter(RelativePathAdapter.class)
    public String getFilename() {
        return filename.get();
    }
    public void setFilename(String value) {
        filename.set(value);
        File file = new File(value);
        setFile(file);
        setShortFilename(file.getName());
        setFolder(file.getParent());
    }
    public StringProperty filenameProperty() {
        return filename;
    }
    public void refreshFile() {
        setFile(new File(getFilename()));
    }

    //Short filename
    private final StringProperty shortFilename = new SimpleStringProperty();
    public String getShortFilename() {
        return shortFilename.get();
    }
    private void setShortFilename(String value) {
        shortFilename.set(value);
    }
    public StringProperty shortFilenameProperty() {
        return shortFilename;
    }

    //Folder
    private final StringProperty folder = new SimpleStringProperty();
    public String getFolder() {
        return folder.get();
    }
    private void setFolder(String value) {
        folder.set(value);
    }
    public StringProperty folderProperty() {
        return folder;
    }

    //Validator
    private final StringProperty validator = new SimpleStringProperty("");
    @XmlElement
    @XmlPath("val_by/text()")
    public String getValidator() {
        return validator.get();
    }
    private void setValidator(String value) {
        validator.set(value);
    }
    public StringProperty validatorProperty() {
        return validator;
    }

    //Image Width
    private final IntegerProperty imageWidth = new SimpleIntegerProperty();
    @XmlElement
    @XmlPath("source/width/text()")
    public int getImageWidth() {
        return imageWidth.get();
    }
    protected void setImageWidth(int value) {
        imageWidth.set(value);
    }
    public IntegerProperty imageWidthProperty() {
        return imageWidth;
    }

    //Image Height
    private final IntegerProperty imageHeight = new SimpleIntegerProperty();
    @XmlElement
    @XmlPath("source/height/text()")
    public int getImageHeight() {
        return imageHeight.get();
    }
    protected void setImageHeight(int value) {
        imageHeight.set(value);
    }
    public IntegerProperty imageHeightProperty() {
        return imageHeight;
    }

    //Sample
    private final StringProperty sampleID = new SimpleStringProperty("unknown");
    @XmlElement
    @XmlPath("source/sampleID/text()")
    public String getSampleID() {
        return sampleID.get();
    }
    protected void setSampleID(String value) {
        sampleID.set(value);
    }
    public StringProperty sampleIDProperty() {
        return sampleID;
    }

    //Indices
    //TODO change to numerics
    private final DoubleProperty index1 = new SimpleDoubleProperty();
    @XmlElement
    @XmlPath("source/index1/text()")
    public double getIndex1() {
        return index1.get();
    }
    protected void setIndex1(double value) {
        index1.set(value);
    }
    public DoubleProperty index1Property() {
        return index1;
    }
    private final DoubleProperty index2 = new SimpleDoubleProperty();
    @XmlElement
    @XmlPath("source/index2/text()")
    public double getIndex2() {
        return index2.get();
    }
    protected void setIndex2(double value) {
        index2.set(value);
    }
    public DoubleProperty index2Property() {
        return index2;
    }

    public double getIndexN(int N) {
        if (N == 1) return getIndex1();
        else return getIndex2();
    }

    //GUID
    private final StringProperty GUID = new SimpleStringProperty();
    @XmlElement
    @XmlPath("source/GUID/text()")
    public String getGUID() {
        return GUID.get();
    }
    protected void setGUID(String value) {
        GUID.set(value);
    }
    public StringProperty GUIDProperty() {
        return GUID;
    }

    //Resolution
    private final DoubleProperty resolution = new SimpleDoubleProperty();
    @XmlElement
    @XmlPath("source/resolution/text()")
    public double getResolution() {
        return resolution.get();
    }
    protected void setResolution(double value) {
        resolution.set(value);
    }
    public DoubleProperty resolutionProperty() {
        return resolution;
    }

    //Score
    private final IntegerProperty imageQuality = new SimpleIntegerProperty();
    @XmlElement
    @XmlPath("source/imageQuality/text()")
    public int getImageQuality() {
        return imageQuality.get();
    }
    protected void setImageQuality(int value) {
        imageQuality.set(value);
    }
    public IntegerProperty imageQualityProperty() {
        return imageQuality;
    }

    //Classification information
    @XmlElement
    private ClassificationSet classifications = new ClassificationSet();

    //Tags   
    @XmlElementWrapper(name = "tags")
    @XmlElement(name = "tag")
    public HashSet<String> tags = new HashSet<>();

    //CNN vector
    private float[] cnnVector;
    protected void setCNNVector(float[] vector) {
        cnnVector = vector;
    }
    public float[] getCNNVector() {
        return cnnVector;
    }

    //Morphology
    private final ObjectProperty<Morphology> morphology = new SimpleObjectProperty<>();
    public Morphology getMorphology() {
        return morphology.get();
    }
    public ObjectProperty<Morphology> morphologyProperty() {
        return morphology;
    }
    protected void setMorphology(Morphology morphology) {
        this.morphology.set(morphology);
    }

    //Parameters
    public LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
    @XmlElement(name = "parameters")
    @XmlJavaTypeAdapter(ParametersMapAdapter.class)
    public LinkedHashMap<String, String> getParameters() {
        return parameters;
    }
    public void setParameters(LinkedHashMap<String, String> value) {
        parameters = value;
    }

    //Other observable properties for UI (TableView)
    //Most are derived from others
    public StringProperty classification = new SimpleStringProperty(this, "classification", Project.UNLABELED_CODE);
    public String getClassification() {
        return classification.get();
    }
    public StringProperty classificationProperty() {
        return classification;
    }
    protected void setClassification(String classification) {
        this.classification.set(classification);
    }

    public StringProperty classifierIdProperty = new SimpleStringProperty(this, "classifierId", "none");
    public StringProperty tagUIProperty = new SimpleStringProperty(this, "tagUI", "");
    public IntegerProperty qualityProperty = new SimpleIntegerProperty(this, "quality", 0);
    public DoubleProperty scoreProperty = new SimpleDoubleProperty(this, "score", 0.0);

    //Constructors
    public Particle() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        Base62 base62 = Base62.createInstance();
        setGUID(new String(base62.encode(bb.array())));
    }

    public Particle(File file) {
        this();
        setFilename(file.getAbsolutePath());
    }

    public Particle(File file, String code, String classifierId) {
        this(file);
        clearAndAddClassification(code, 1.0, classifierId);
    }

    public Particle(File file, String code, String classifierId, double score) {
        this(file);
        clearAndAddClassification(code, score, classifierId);
    }

    public Particle(File file, String code, String classifierId, double score, String sample, double resolution) {
        this(file, code, classifierId, score);
        setSampleID(sample);
        setResolution(resolution);
    }

    public void addParameters(LinkedHashMap<String, String> data, boolean overwriteExisting) {

        parameters = data;

        if (!overwriteExisting) return;

//        ArrayList<String> toRemove = new ArrayList<>();
        //Sample
        if (data.containsKey("sample")) {
            setSampleID(data.get("sample"));
//            toRemove.add("sample");
        }
        //Class
        String code = classifications.getBestCode();
        String classifierID = classifications.getClassifierId();
        double score = classifications.getBest().getValue();
        boolean wasUpdate = false;
        if (data.containsKey("class")) {
            code = data.get("class");
//            toRemove.add("class");
            wasUpdate = true;
        }
        else if (data.containsKey("label")) {
            code = data.get("label");
//            toRemove.add("label");
            wasUpdate = true;
        }
        //Classifier
        if (data.containsKey("classifier")) {
            classifierID = data.get("classifier");
//            toRemove.add("classifier");
            wasUpdate = true;
        }
        //Score
        if (data.containsKey("score")) {
            try {
                score = Double.parseDouble(data.get("score"));
//                toRemove.add("score");
                wasUpdate = true;
            }
            catch (NumberFormatException ex) {

            }
        }
        if (wasUpdate) clearAndAddClassification(code, score, classifierID);
        //Index 1
        if (data.containsKey("index1")) {
            try {
                setIndex1(Double.parseDouble(data.get("index1")));
//                toRemove.add("index1");
            }
            catch (NumberFormatException ex) {

            }
        }
        //Index 2
        if (data.containsKey("index2")) {
            try {
                setIndex2(Double.parseDouble(data.get("index2")));
//                toRemove.add("index2");
            }
            catch (NumberFormatException ex) {

            }
        }
        //GUID
        if (data.containsKey("guid")) {
            setGUID(data.get("guid"));
//            toRemove.add("guid");
        }
        //Resolution
        if (data.containsKey("resolution")) {
            try {
                setResolution(Double.parseDouble(data.get("resolution")));
//                toRemove.add("resolution");
            }
            catch (NumberFormatException ex) {

            }
        }
        //TODO remove extra fields?
//        for (String s : toRemove) {
//            data.remove(s);
//        }

    }

    /*
    Classification
    */
    protected void clearAndAddClassification(String code, double score, String classifierId) {
        classifications.clearAndAdd(code, score, classifierId);
        classification.set(classifications.getBestCode());
        classifierIdProperty.set(classifications.getClassifierId());
        setValidator("");
        removeTag("auto");
    }

    protected void addClassification(String code, double score, String classifierId) {
        classifications.add(code, score, classifierId);
        classification.set(classifications.getBestCode());
        classifierIdProperty.set(classifications.getClassifierId());
        setValidator("");
        removeTag("auto");
    }

    protected void setClassifications(ClassificationSet set, double threshold, boolean wasAuto) {
        classifications = set;
        if (classifications.maximumScore < threshold) {
            classifications.add("unsure", 1.0, classifications.getClassifierId());
        }
        classification.set(classifications.getBestCode());
        classifierIdProperty.set(classifications.getClassifierId());
        setValidator("");
        if (wasAuto) addTag("auto");
        else removeTag("auto");
    }

    public ClassificationSet getClassifications() {
        return classifications;
    }

    public List<Classification> getClassificationsAsList() {
        return new ArrayList(classifications.classifications.values());
    }

    public double getScore(String code) {
        if (classifications.classifications.containsKey(code)) {
            return classifications.classifications.get(code).getValue();
        } else {
            return 0.0;
        }
    }

    public void validate(String validator) {
        setValidator(validator);
    }

    /*
    Tag
    */
    protected void toggleTag(String code) {
        if (tags.contains(code)) {
            tags.remove(code);
        } else {
            tags.add(code);
        }
        tagUIProperty.set(tagsToString());
    }

    protected void addTag(String code) {
        if (!tags.contains(code)) {
            tags.add(code);
        }
        tagUIProperty.set(tagsToString());
    }

    protected void removeTag(String code) {
        if (tags.contains(code)) {
            tags.remove(code);
        }
        tagUIProperty.set(tagsToString());
    }

    public String tagsToString() {
        StringBuilder sb = new StringBuilder();
        tags.forEach(tag -> {
            sb.append(tag);
            sb.append(";");
        });
        if (sb.length() > 1) {
            return sb.substring(0, sb.length() - 1);
        } else {
            return " ";
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        tags.forEach(tag -> {
            sb.append(tag);
            sb.append(";");
        });
        sb.append(getClassification() + ";");
        sb.append(getFilename() + ";");
        sb.append(getGUID() + ";");
        return sb.toString();
    }

    /*
    Images
    */
    public Mat getMat() {
        if (!getFile().exists()) return null;
        try {
            Mat mat = MatUtilities.imread(getFilename());
            if (mat.rows() == 0 || mat.cols() == 0) {
                mat.release();
                return null;
            }
            return mat;
        } catch (IOException ex) {
            return null;
        }
    }

    public Image getImage() throws IOException {
        if (getFile() != null) {
            return SwingFXUtils.toFXImage(ImageIO.read(getFile()), null);
        } else {
            return null;
        }
    }

    public Image getImageThumbnail() throws IOException {
        if (getFile() != null) {
            return getImage();
        } else {
            return null;
        }
    }

    //Properties needed for tableView
    public void initUIProperties() {
        if (classifications.classifications == null) setClassifications(new ClassificationSet(), 0, false);
        if (classifications.classifications.isEmpty()) {
            addClassification(Project.UNLABELED_CODE, 1.0, "default");
        }
        classification.set(classifications.getBestCode());
        classifierIdProperty.set(classifications.getClassifierId());
        scoreProperty.set(0.0);
        tagUIProperty.set(tagsToString());
    }
}
