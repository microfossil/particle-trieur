/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.controls;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.models.project.Particle;

import java.io.IOException;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ParticleGridCellSimilarityControl extends BorderPane {

    @FXML
    Circle circleRed;
    @FXML
    Circle circleGreen;
    @FXML
    ImageViewPane imageViewPane;
    @FXML
    ImageView imageView;
    @FXML
    Label labelId;
    @FXML
    Label labelClassification;
    @FXML
    Label labelInfo;

    private Particle particle;
    private Particle relativeParticle;

    public ParticleGridCellSimilarityControl() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ParticleGridCellSimilarityControl.fxml"));
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
        circleRed.setVisible(false);
        circleGreen.setVisible(false);
        BorderPane.setMargin(imageViewPane, new Insets(4));
        labelClassification.textProperty().addListener((observable, oldValue, newValue) -> setState());
    }

    private void setState() {
        if (particle == relativeParticle) {
            circleGreen.setVisible(false);
            circleRed.setVisible(false);
        }
        else if (particle.getClassification().equals(relativeParticle.getClassification())) {
            circleGreen.setVisible(true);
            circleRed.setVisible(false);
        }
        else {
            circleGreen.setVisible(false);
            circleRed.setVisible(true);
        }
    }

    public void setData(Particle particle, Particle relativeParticle, int index, String description, Image image) {
        this.particle = particle;
        this.relativeParticle = relativeParticle;

        labelId.setText(String.format("#%d",index));
        labelClassification.textProperty().bind(this.particle.classification);
        labelInfo.setText(description);

        if (particle.equals(relativeParticle)) {
            labelInfo.setText("selected");
        }
        setState();

        if (image != null) {
            imageView.setImage(image);
        }
        else {
            Image missingImage = new Image(App.class.getResourceAsStream("resources/missing-image-128.png"), 128, 128, true, true);
            imageView.setImage(missingImage);
        }
    }
}
