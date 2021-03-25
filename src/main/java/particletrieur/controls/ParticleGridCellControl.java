/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.controls;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import particletrieur.App;
import particletrieur.models.project.Particle;

import java.io.IOException;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ParticleGridCellControl extends BorderPane {

    @FXML
    StackPane validated;
    @FXML
    ImageViewPane imageViewPane;
    @FXML
    ImageView imageView;
    @FXML
    Label labelId;
    @FXML
    Label labelClassification;
    @FXML
    SymbolLabel symbolLabelValidated;

    private Particle particle;

    public ParticleGridCellControl() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ParticleGridCellControl.fxml"));
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
        validated.setVisible(false);
        BorderPane.setMargin(imageViewPane, new Insets(4));
//        symbolLabelValidated.setVisible(false);
    }

    public void setData(Particle particle, int index) {
        this.particle = particle;
        labelId.setText(String.format("#%d", index + 1));
        labelClassification.textProperty().bind(this.particle.classification);
        validated.visibleProperty().bind(Bindings.not(Bindings.equal(particle.validatorProperty(), "")));
        try {
            imageView.setImage(particle.getImage());
        } catch (IOException ex) {
            Image missingImage = new Image(App.class.getResourceAsStream("/icons/missing-image-128.png"), 128, 128, true, true);
            imageView.setImage(missingImage);
        }
        imageViewPane.layoutChildren();
    }
}
