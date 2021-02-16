/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.viewcontrollers.morphology;

import main.java.app.controls.SymbolLabel;
import main.java.app.models.Supervisor;
import main.java.app.models.processing.*;
import main.java.app.models.processing.processors.MatUtilities;
import main.java.app.AbstractController;
import main.java.app.viewmodels.SelectionViewModel;
import main.java.app.controls.ImageCell;
import com.google.inject.Inject;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;


/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ProcessingViewController extends AbstractController implements Initializable {

    @FXML
    Label labelNoOutline;
    @FXML
    SymbolLabel symbolLabelIsUpdating;
    //Image type
    @FXML
    ChoiceBox<ImageType> choiceBoxImageType;

    //Initial fixes
    @FXML
    CheckBox checkBoxRemoveBlackBorder;
    @FXML
    CheckBox checkBoxRemoveWhiteBorder;
    @FXML
    CheckBox checkBoxMakeSquare;

    //Colour adjustments
    @FXML
    CheckBox checkBoxConvertToGreyscale;
    @FXML
    CheckBox checkBoxNormalise;
    @FXML
    Spinner spinnerNormalisationParameter;

    //Segmentation
    @FXML
    ChoiceBox<SegmentationType> choiceBoxSegmentationMethod;
    @FXML
    Spinner spinnerSegmentationThreshold;
    @FXML
    CheckBox checkBoxEnhanceEdges;
    @FXML
    CheckBox checkBoxRescaleIntensity;

    //Segmentation-based adjustments
    @FXML
    CheckBox checkBoxCentreImage;
    @FXML
    CheckBox checkBoxRotateImage;
    @FXML
    CheckBox checkBoxRemoveBackground;
    @FXML
    Spinner spinnerBackgroundRemovalMargin;


    @FXML
    ImageCell imageCellProcessed;
    @FXML
    ImageCell imageCellEdge;
    @FXML
    ImageCell imageCellOriginal;
    @FXML
    GridPane gridPaneImages;
    @FXML
    GridPane gridPaneRoot;
    @FXML
    Label labelOriginalImage;

    @Inject
    SelectionViewModel selectionViewModel;
    @Inject
    Supervisor supervisor;

    RotateTransition rt;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        choiceBoxImageType.getItems().setAll(ImageType.values());
        choiceBoxSegmentationMethod.getItems().setAll(SegmentationType.values());
        setupBindings();

        gridPaneRoot.getColumnConstraints().get(0).prefWidthProperty().bind(
                Bindings.multiply(gridPaneRoot.heightProperty(),0.3)
        );

        rt = new RotateTransition(Duration.millis(3000), symbolLabelIsUpdating);
        rt.setByAngle(360);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.setCycleCount(Animation.INDEFINITE);
        symbolLabelIsUpdating.setCache(true);
        symbolLabelIsUpdating.setCacheHint(CacheHint.SPEED);

        //Spinner hack
        for (Field field : getClass().getDeclaredFields()) {
            try {
                Object obj = field.get(this);
                if (obj != null && obj instanceof Spinner)
                    ((Spinner) obj).focusedProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue) {
                            ((Spinner) obj).increment(0);
                        }
                    });
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Setup up the bindings between the Main form and instance values of the controller
     */
    public void setupBindings() {

        ProcessingInfo def = supervisor.project.processingInfo;

        //Image type
        choiceBoxImageType.valueProperty().bindBidirectional(def.imageTypeProperty());

        //Initial Fixes
        checkBoxRemoveWhiteBorder.selectedProperty().bindBidirectional(def.removeWhiteBorderProperty());
        checkBoxRemoveBlackBorder.selectedProperty().bindBidirectional(def.removeBlackBorderProperty());
        checkBoxMakeSquare.selectedProperty().bindBidirectional(def.makeSquareProperty());

        //Colour adjustments
        checkBoxConvertToGreyscale.selectedProperty().bindBidirectional(def.convertToGreyscaleProperty());
        checkBoxNormalise.selectedProperty().bindBidirectional(def.normaliseProperty());
        spinnerNormalisationParameter.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.05, 1, 0.5, 0.05));
        spinnerNormalisationParameter.getValueFactory().valueProperty().bindBidirectional(def.normalisationParameterProperty());

        //Segmentation
        choiceBoxSegmentationMethod.valueProperty().bindBidirectional(def.segmentationMethodProperty());
        spinnerSegmentationThreshold.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.5, 0.05));
        spinnerSegmentationThreshold.getValueFactory().valueProperty().bindBidirectional(def.segmentationThresholdProperty());
        checkBoxEnhanceEdges.selectedProperty().bindBidirectional(def.segmentationEnhanceEdgesProperty());
        checkBoxRescaleIntensity.selectedProperty().bindBidirectional(def.segmentationRescaleProperty());

        //Segmentation-based adjustments
        checkBoxCentreImage.selectedProperty().bindBidirectional(def.centreProperty());
        checkBoxRotateImage.selectedProperty().bindBidirectional(def.rotateToMajorAxisProperty());
        checkBoxRemoveBackground.selectedProperty().bindBidirectional(def.removeBackgroundProperty());
        spinnerBackgroundRemovalMargin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 64, 16, 4));
        spinnerBackgroundRemovalMargin.getValueFactory().valueProperty().bindBidirectional(def.backgroundRemovalMarginProperty());
        checkBoxRotateImage.disableProperty().bind(Bindings.not(checkBoxCentreImage.selectedProperty()));

        selectionViewModel.currentParticleImageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                imageCellOriginal.setImage(null);
                imageCellEdge.setImage(null);
                imageCellProcessed.setImage(null);
            }
            else {
                imageCellOriginal.setImage(MatUtilities.mat2Image(newValue.colourImage));
                if (newValue.mask.isOutline()) {
                    imageCellEdge.setImage(MatUtilities.mat2Image(newValue.mask.display));
                    labelNoOutline.setVisible(false);
                } else {
                    imageCellEdge.setImage(null);
                    labelNoOutline.setVisible(true);
                }
                imageCellProcessed.setImage(MatUtilities.mat2Image(newValue.workingImage, 255));
            }
        });

        selectionViewModel.getImageProcessingIsRunningProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                symbolLabelIsUpdating.setVisible(true);
                rt.play();
            }
            else {
                symbolLabelIsUpdating.setVisible(false);
                rt.stop();
            }
        });
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        //TODO This needs to be a separate function refreshProcessedImage etc.
        selectionViewModel.refreshParticleImage();
    }
}
