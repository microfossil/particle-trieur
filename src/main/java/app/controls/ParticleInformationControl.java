/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.controls;

import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import main.java.app.models.project.Particle;

import java.io.File;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ParticleInformationControl extends GridPane {

    private Particle particle;
    private Image missingImage;

    public ParticleInformationControl() {

    }
    
    @FXML 
    public void initialize() {

    }

    public void setData(Particle particle, int index) {
        this.setHgap(5);
        this.setVgap(5);
        this.getColumnConstraints().add(new ColumnConstraints(Control.USE_COMPUTED_SIZE));
        this.getChildren().clear();
        int i = 0;
        this.add(new Label("Label:"),0,i++);
        this.add(new Label("Tags:"),0,i++);
        this.add(new Label("Classifier:"),0,i++);
        this.add(new Label("Validated:"),0,i++);
        this.add(new Label("Sample:"),0,i++);
        this.add(new Label("Filename:"),0,i++);
        this.add(new Label("Size:"),0,i++);
        this.add(new Label("GUID:"),0,i++);
        i = 0;
        this.add(new Label(particle.classification.get()),1,i++);
        this.add(new Label(particle.tagUIProperty.get()),1,i++);
        this.add(new Label(particle.classifierIdProperty.get()),1,i++);
        this.add(new Label(particle.validatorProperty().get()),1,i++);
        this.add(new Label(particle.getSampleID()),1,i++);

//        this.add(new Text(String.format("%f - %f", particle.getIndex1(), particle.getIndex2())),1,2);
        Label label = new Label();
        label.setWrapText(true);
        label.setText(particle.getFilename());
        this.add(label,1,i++);
        this.add(new Label(printFileSize(particle.getFile())),1,i++);
        this.add(new Label(particle.getGUID()),1,i++);
    }

    public String printFileSize(File file) {
        long sizeInBytes = file.length();
        String result;
        if (sizeInBytes < 1024) {
            result = String.format("%d bytes", sizeInBytes);
        }
        else if (sizeInBytes < (1024*1024)) {
            result = String.format("%.1f kB", (double) sizeInBytes / 1024);
        }
        else {
            result = String.format("%.1f MB", (double) sizeInBytes / (1024*1024));
        }
        return result;
    }
}
