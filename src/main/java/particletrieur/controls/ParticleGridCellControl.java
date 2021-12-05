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
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
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
    @FXML
    HBox hboxResolution;
    @FXML
    Rectangle rectangleResolution;
    @FXML
    Label labelResolution;

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
    }

    public void setData(Particle particle, int index) {
        this.particle = particle;
        labelId.setText(String.format("#%d", index + 1));
        labelClassification.textProperty().bind(this.particle.classification);
        validated.visibleProperty().bind(Bindings.not(Bindings.equal(particle.validatorProperty(), "")));

        try {
            Image image = particle.getImage();
            imageView.setImage(image);

            if (particle.getResolution() > 0) {
                Pair<Double[], String> measurement = getMeasurementForDisplay(image.getWidth(), image.getHeight(), particle.getResolution());
                labelResolution.setText(String.format("%.1f x %.1f %s", measurement.getKey()[0], measurement.getKey()[1], measurement.getValue()));
            }
            else {
                labelResolution.setText(String.format("%.0f x %.0f px", image.getWidth(), image.getHeight()));
            }
            hboxResolution.setVisible(true);
        } catch (IOException ex) {
            Image missingImage = new Image(App.class.getResourceAsStream("/icons/missing-image-128.png"), 128, 128, true, true);
            imageView.setImage(missingImage);
            hboxResolution.setVisible(false);
        }
        imageViewPane.layoutChildren();
    }

    public Pair<Double[], String> getMeasurementForDisplay(double width, double height, double resoluton) {

        double sizeInMillimeters = width / resoluton;

        if (sizeInMillimeters >= 1000) {
            return new Pair<>(new Double [] {sizeInMillimeters / 1000, height / resoluton / 1000}, "m");
        }
        else if (sizeInMillimeters >= 10) {
            return new Pair<>(new Double [] {sizeInMillimeters / 10, height / resoluton / 10}, "cm");
        }
        else if (sizeInMillimeters >= 1) {
            return new Pair<>(new Double [] {sizeInMillimeters, height / resoluton}, "mm");
        }
        else if (sizeInMillimeters >= 0.001) {
            return new Pair<>(new Double [] {sizeInMillimeters / 0.001, height / resoluton / 0.001}, "um");
        }
        else {
            return new Pair<>(new Double [] {sizeInMillimeters / 0.000001, height / resoluton / 0.000001}, "nm");
        }
    }

    public double quantised(double value) {
        double log10 = Math.log10(value);
        double floorlog10 = Math.floor(log10);
        double remainder = log10 - floorlog10;
        double base = Math.pow(10, floorlog10);
        if (remainder > Math.log10(5)) {
            base *= 5;
        }
        else if (remainder > Math.log10(2)) {
            base *= 2;
        }
        return base;
    }
}
