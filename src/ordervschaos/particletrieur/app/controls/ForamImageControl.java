/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.controls;

import javafx.scene.image.Image;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.viewcontrollers.particle.ImageDescriptionPopover;

import java.io.IOException;

import ordervschaos.particletrieur.app.viewmodels.particles.LabelsViewModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.PopOver;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ForamImageControl extends AnchorPane {
    
    @FXML ImageView imageView;
    @FXML Label labelClassification;
    @FXML Label labelScore;
    @FXML Label labelId;
    PopOver popOver;
    
    private boolean showControls;

    private Supervisor supervisor;
    private LabelsViewModel labelsViewModel;
    
    private Particle particle;
    private Particle relativeParticle;
    private String description;

    public SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

    Image missingImage;

    public ForamImageControl(Supervisor supervisor, LabelsViewModel labelsViewModel) {

        missingImage = new Image(App.class.getResourceAsStream("resources/missing-image-128.png"), 128, 128, true, true);

        this.supervisor =  supervisor;
        this.labelsViewModel = labelsViewModel;
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ForamImageControl.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    @FXML 
    public void initialize() {
        popOver = new PopOver();

        imageView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                popOver.show(imageView);
            }
        });

        selected.addListener((observable, oldValue, newValue) -> {
            setState();
        });

        this.setOnMouseClicked(event -> {
            //selected.set(!selected.get());
        });

        labelClassification.textProperty().addListener((observable, oldValue, newValue) -> setState());
    }

    public void setState() {
        StringBuilder style = new StringBuilder();
        style.append("-fx-background-radius: 0; -fx-border-radius: 0; ");

        if (particle == relativeParticle) {
            style.append("-fx-border-color: -fx-accent; ");
            if (selected.get()) style.append("-fx-background-color: derive(-fx-accent, 50%); ");
            else style.append("-fx-background-color: derive(-fx-accent, 120%);");
        }
        else if (!supervisor.project.getTaxons().get(particle.classification.get()).getIsClass()) {
            style.append("-fx-border-color: grey; ");
            if (selected.get()) style.append("-fx-background-color: derive(-fx-accent, 50%); ");
            else style.append("-fx-background-color: derive(-fx-background, -10%);");
        }
        else if (particle.classification.get().equals(relativeParticle.classification.get())) {
            style.append("-fx-border-color: green; ");
            if (selected.get()) style.append("-fx-background-color: derive(-fx-accent, 50%);; ");
            else style.append("-fx-background-color: derive(green, 120%);");
        }
        else {
            style.append("-fx-border-color: red; ");
            if (selected.get()) style.append("-fx-background-color: derive(-fx-accent, 50%); ");
            else style.append("-fx-background-color: derive(red, 120%);");
        }
        style.append("-fx-border-width: 1;");
        setStyle(style.toString());
    }
    
    public void setSize(int size) {
//        setPrefHeight(size + 23);
//        setPrefWidth(size);
    }
    
    public void setData(Particle particle, Particle relativeParticle, int index, String description, boolean isComparing) {
        this.particle = particle;
        this.relativeParticle = relativeParticle;
        this.description = description;
        
        labelId.setText(String.format("#%d",index));
        labelClassification.textProperty().bind(this.particle.classification);
        labelScore.setText(description);

        if (particle.equals(relativeParticle)) {
            labelScore.setText("");
            labelId.setText(String.format("#%d (current)",index));
        }
        setState();
        
        try {
            imageView.setImage(particle.getImage());
        } catch (IOException ex) {
            imageView.setImage(missingImage);
        }
    }

    public void setData(Particle particle, Particle relativeParticle, int index, String description, boolean isComparing, Image image) {
        this.particle = particle;
        this.relativeParticle = relativeParticle;
        this.description = description;

        labelId.setText(String.format("#%d",index));
        labelClassification.textProperty().bind(this.particle.classification);
        labelScore.setText(description);

        if (particle.equals(relativeParticle)) {
            labelScore.setText("");
            labelId.setText(String.format("#%d (current)",index));
        }
        setState();

        if (image != null) {
            imageView.setImage(image);
        }
        else {
            imageView.setImage(missingImage);
        }
    }
    
    private String printSigFig(double number, int significantDigits) {
        if (number < Math.pow(10, -significantDigits)) number = 0;
        short offset = (short) Math.ceil(Math.log10(number) + 1);
        String fmt = "%." + Math.max(significantDigits - offset,0) + "f";
        String res = String.format(fmt, number);
        return res;
    }
    
    @FXML
    private void handleImportClass(ActionEvent event) {
        labelsViewModel.setLabel(particle, relativeParticle.classification.get(), 1.0, true);
    }
    
    @FXML
    private void handleExportClass(ActionEvent event) {
        labelsViewModel.setLabel(relativeParticle, particle.classification.get(), 1.0, true);
    }
    
    @FXML
    private void handleGoto(ActionEvent event) {
        ImageDescriptionPopover popover = new ImageDescriptionPopover(particle, PopOver.ArrowLocation.RIGHT_TOP);
        popover.show(this);
    }
    
    @FXML
    private void handleRemove(ActionEvent event) {
        supervisor.project.removeParticle(particle);  //TODO should be in view manager
        this.setVisible(false);
    }
}
