/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.project.Particle;
import org.controlsfx.control.PopOver;
import org.eclipse.persistence.platform.database.SybasePlatform;

import java.io.IOException;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ParticleGridCellControl extends AnchorPane {

    @FXML ImageView imageView;
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
        symbolLabelValidated.setVisible(false);
    }

    public void setData(Particle particle, int index) {
        this.particle = particle;
        labelId.setText(String.format("#%d", index + 1));
        labelClassification.textProperty().bind(this.particle.classification);
        symbolLabelValidated.visibleProperty().bind(Bindings.not(Bindings.equal(particle.validatorProperty(), "")));
        try {
            imageView.setImage(particle.getImage());
        } catch (IOException ex) {
            Image missingImage = new Image(App.class.getResourceAsStream("resources/missing-image-128.png"), 128, 128, true, true);
            imageView.setImage(missingImage);
        }
    }
}
