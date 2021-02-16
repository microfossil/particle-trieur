/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.other.segmenter;

import java.io.File;
import java.io.IOException;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import main.java.app.models.processing.processors.MatUtilities;
import org.opencv.core.Mat;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class RawSlide {
    
    //File
    private final ObjectProperty<File> file = new SimpleObjectProperty<>();
    public File getFile() {
        return file.get();
    }
    public void setFile(File value) {
        file.set(value);
        setShortFilename(value.getName());
        setFolder(value.getParent());
        setFilename(value.getAbsolutePath());
    }
    public ObjectProperty fileProperty() {
        return file;
    }

    //Filename (set by getFile)
    private final StringProperty filename = new SimpleStringProperty();
    public String getFilename() {
        return filename.get();
    }
    public void setFilename(String value) {
        filename.set(value);
    }
    public StringProperty filenameProperty() {
        return filename;
    }
    
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
    
    //Core ID
    private final StringProperty coreID = new SimpleStringProperty();
    public String getCoreID() {
        return coreID.get();
    }
    public void setCoreID(String value) {
        coreID.set(value);
    }
    public StringProperty coreIDProperty() {
        return coreID;
    }
    
    //Depths
    private final DoubleProperty depthMin = new SimpleDoubleProperty();
    public double getDepthMin() {
        return depthMin.get();
    }
    public void setDepthMin(double value) {
        depthMin.set(value);
    }
    public DoubleProperty depthMinProperty() {
        return depthMin;
    }
    
    private final DoubleProperty depthMax = new SimpleDoubleProperty();
    public double getDepthMax() {
        return depthMax.get();
    }
    public void setDepthMax(double value) {
        depthMax.set(value);
    }
    public DoubleProperty depthMaxProperty() {
        return depthMax;
    }
    
    public RawSlide(File file) {
        setFile(file);
    }
    
    public Mat LoadImage() {
        Mat mat = null;
        try {
            mat = MatUtilities.imread(getFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        if (mat.channels() == 3) Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        return mat;
    }
}
