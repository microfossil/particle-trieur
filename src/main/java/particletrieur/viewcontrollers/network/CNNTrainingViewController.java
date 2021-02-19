/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.network;

import com.google.inject.Inject;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import particletrieur.App;
import particletrieur.AppPreferences;
import particletrieur.FxmlLocation;
import particletrieur.controls.AlertEx;
import particletrieur.controls.BasicDialogs;
import particletrieur.models.Supervisor;
import particletrieur.models.network.training.GPUStatus;
import particletrieur.models.network.training.CNNTrainingScript;
import particletrieur.models.network.training.ModelDefaults;
import particletrieur.services.network.CNNTrainingService;
import particletrieur.services.network.TrainingNetworkDescriptionService;
import particletrieur.AbstractDialogController;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.CustomTextField;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("/views/CNNTrainingView.fxml")
public class CNNTrainingViewController extends AbstractDialogController implements Initializable {

    @FXML
    Label labelGPUMemory;
    @FXML
    Label labelGPUUsage;
    @FXML
    Label labelPythonLocation;
    @FXML
    CheckBox checkBoxUseOther;
    @FXML
    CheckBox checkBoxUseSplit;
    @FXML
    GridPane gridPaneSplit;
    @FXML
    CustomTextField textFieldName;
    @FXML
    HBox hboxInputFolder;
    @FXML
    CheckBox checkBoxApplyAugmentation;
    @FXML
    CheckBox checkBoxBalanceClassWeights;
    @FXML
    Spinner<Integer> spinnerBatchSize;
    @FXML
    RadioButton radioButtonInputThisProject;
    @FXML
    RadioButton radioButtonInputCloudZipFile;
    @FXML
    RadioButton radioButtonInputFolder;
    @FXML
    CustomTextField textFieldInputFolder;
    @FXML
    Spinner<Integer> spinnerMinImagesPerClass;
    @FXML
    Spinner<Double> spinnerTestSplitFraction;
    @FXML
    CustomTextField textFieldCloudZipFile;
    @FXML
    CustomTextField textFieldOutputFolder;
    @FXML
    CheckBox checkBoxOutputSaveModel;
    @FXML
    CheckBox checkBoxOutputSaveMislabeled;
    @FXML
    ComboBox<ModelDefaults> comboBoxCNNType;
    @FXML
    ComboBox<Integer> comboBoxInputSize;
    @FXML
    ComboBox<String> comboBoxColourMode;
    @FXML
    Spinner<Integer> spinnerAlrEpochs;
    @FXML
    Label labelNetworkDescription;
    @FXML
    CheckBox checkBoxUseMemoryMapping;

    @Inject
    Supervisor supervisor;

    private static AppPreferences appPrefs = new AppPreferences();


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboBoxCNNType.getItems().addAll(TrainingNetworkDescriptionService.getDescriptions());
        comboBoxCNNType.setCellFactory(param -> {
            HBox hBox = new HBox();
            Pane pane = new Pane();
            pane.setPrefWidth(5);
            VBox vBox = new VBox();
            Label name = new Label();
            name.setStyle("-fx-font-weight: bold;");
            Label description = new Label();
            vBox.getChildren().addAll(name, description);
            hBox.getChildren().addAll(pane, vBox);
            HBox.setMargin(pane, new Insets(0, 7, 0, 0));
            ListCell<ModelDefaults> cell = new ListCell<ModelDefaults>() {
                @Override
                protected void updateItem(ModelDefaults item, boolean empty) {
                    int pos = comboBoxCNNType.getItems().indexOf(item) % 2;
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setGraphic(null);
                    } else {
                        if (item.networkType.startsWith("base_cyclic") || item.networkType.startsWith("resnet_cyclic")) {
                            pane.setStyle("-fx-background-color: darkorange;");
                        } else if (item.networkType.endsWith("tl")) {
                            pane.setStyle("-fx-background-color: darkgreen;");
                        } else {
                            pane.setStyle("-fx-background-color: darkred;");
                        }
                        name.setText(item.name);
                        description.setText(item.description);
                        setGraphic(hBox);
                    }
                }
            };
            return cell;
        });
        comboBoxCNNType.setConverter(new StringConverter<ModelDefaults>() {
            @Override
            public String toString(ModelDefaults object) {
                return object.name;
            }

            @Override
            public ModelDefaults fromString(String string) {
                return null;
            }
        });

        comboBoxInputSize.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return Integer.toString(object);
            }

            @Override
            public Integer fromString(String string) {
                Integer result;
                try {
                    result = Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    result = 0;
                }
                return result;
            }
        });
        comboBoxInputSize.getItems().addAll(64, 96, 128, 160, 192, 224, 256);
        comboBoxInputSize.getSelectionModel().select(2);

        comboBoxColourMode.getItems().addAll("Greyscale", "Colour (RGB)");
        comboBoxColourMode.getSelectionModel().select(0);

        CNNTrainingScript info = new CNNTrainingScript();

        spinnerAlrEpochs.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, info.trainingAlrEpochs, 5));
        spinnerBatchSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(16, 256, info.trainingBatchSize, 16));
        spinnerMinImagesPerClass.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 100000, info.datasetMinCount, 5));
        spinnerTestSplitFraction.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 0.9, info.datasetValSplit, 0.05));

        hboxInputFolder.disableProperty().bind(Bindings.not(radioButtonInputFolder.selectedProperty()));
        textFieldCloudZipFile.disableProperty().bind(Bindings.not(radioButtonInputCloudZipFile.selectedProperty()));

        comboBoxCNNType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            labelNetworkDescription.setText(newValue.description);
            comboBoxInputSize.setValue(newValue.imageWidth);
            if (newValue.imageChannels == 1) comboBoxColourMode.getSelectionModel().selectFirst();
            else comboBoxColourMode.getSelectionModel().selectLast();
            if (newValue.networkType.endsWith("_tl")) {
                //comboBoxColourMode.setDisable(true);
                checkBoxApplyAugmentation.setDisable(true);
            } else {
                //comboBoxColourMode.setDisable(false);
                checkBoxApplyAugmentation.setDisable(false);
            }
            if (textFieldName.getText().equals("") || textFieldName.getText().equals(oldValue.name)) {
                textFieldName.setText(newValue.name);
            }
        });
        comboBoxCNNType.getSelectionModel().select(0);

        gridPaneSplit.disableProperty().bind(Bindings.not(checkBoxUseSplit.selectedProperty()));

        defaultOutputFolder();

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
        labelPythonLocation.setText("Python location: " + CNNTrainingService.getAnacondaInstallationLocation());
    }

    private void defaultOutputFolder() {
        File savedOutput = new File(App.getPrefs().getTrainingPath());
        if (savedOutput.exists()) {
            textFieldOutputFolder.setText(savedOutput.getAbsolutePath());
        }
        else {
            if (supervisor.project.getFile().exists()) {
                textFieldOutputFolder.setText(supervisor.project.getFile().getParent() + File.separator + "training");
            } else {
                textFieldOutputFolder.setText(System.getProperty("user.home"));
            }
        }
    }

    public CNNTrainingScript createTrainingScript() {

        CNNTrainingScript info = new CNNTrainingScript();
        ModelDefaults networkInfo = comboBoxCNNType.getSelectionModel().getSelectedItem();

        //Input
        if (radioButtonInputFolder.isSelected()) {
            info.datasetSource = textFieldInputFolder.getText();
        } else if (radioButtonInputCloudZipFile.isSelected()) {
            info.datasetSource = textFieldCloudZipFile.getText();
        } else {
            info.datasetSource = supervisor.project.getFile().getAbsolutePath();
        }

        info.datasetMinCount = spinnerMinImagesPerClass.getValue();
        info.datasetValSplit = checkBoxUseSplit.isSelected() ? spinnerTestSplitFraction.getValue() : 0.0;
        info.datasetMapOthers = checkBoxUseOther.isSelected();
        info.datasetUseMemmap = checkBoxUseMemoryMapping.isSelected();

        //Output
        info.outputDirectory = textFieldOutputFolder.getText();
        info.outputSaveModel = checkBoxOutputSaveModel.isSelected();
        info.outputSaveMislabeled = checkBoxOutputSaveMislabeled.isSelected();

        //Network
        info.cnnId = networkInfo.networkType;
        info.name = textFieldName.getText();
        info.description = "";
        info.cnnFilters = networkInfo.numFilters;

        //Image Input
        info.cnnImgShape[0] = comboBoxInputSize.getValue();
        info.cnnImgShape[1] = comboBoxInputSize.getValue();
        if (comboBoxColourMode.getSelectionModel().getSelectedIndex() == 0) {
            info.cnnImgType = "greyscale";
            info.cnnImgShape[2] = 1;
        } else {
            info.cnnImgType = "rgb";
            info.cnnImgShape[2] = 3;
        }

        //Augmentation
        info.trainingUseAugmentation = checkBoxApplyAugmentation.isSelected();

        //Training
        info.trainingUseClassWeights = checkBoxBalanceClassWeights.isSelected();
        info.trainingBatchSize = spinnerBatchSize.getValue();
        info.trainingAlrEpochs = spinnerAlrEpochs.getValue();

        return info;
    }

    @FXML
    private void handleSelectInputFolder(ActionEvent event) {
        File folder = new File(textFieldInputFolder.getText());
        if (!folder.exists()) folder = new File(App.getPrefs().getExportPath());
        if (!folder.exists()) folder = new File(System.getProperty("user.home"));
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(folder);
        chooser.setTitle("Choose the folder containing the images for training");
        folder = chooser.showDialog(this.stage);
        if (folder != null) {
            textFieldInputFolder.setText(folder.getAbsolutePath());
        }
    }

    @FXML
    private void handleSelectOutputFolder(ActionEvent event) {
        File folder = new File(textFieldOutputFolder.getText());
        if (!folder.exists()) {
            if (radioButtonInputThisProject.isSelected()) {
                try {
                    folder = supervisor.project.getFile().getParentFile();
                } catch (Exception ex) {

                }
            } else {
                folder = new File(App.getPrefs().getExportPath());
                if (!folder.exists()) {
                    folder = new File(System.getProperty("user.home"));
                }
            }
        }
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(folder);
        chooser.setTitle("Choose the folder to save the trained network to");
        folder = chooser.showDialog(this.stage);
        if (folder != null) {
            textFieldOutputFolder.setText(folder.getAbsolutePath());
        }
    }

    @FXML
    private void handleShowFolder(ActionEvent event) {
        File file = new File(textFieldOutputFolder.getText());
        if (file.exists()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(file);
            } catch (IOException e) {
                BasicDialogs.ShowException("Error opening folder", e);
            }
        }
        else {
            BasicDialogs.ShowError("Error", "Output folder has not been created yet");
        }
    }

    @FXML
    private void handleUpdateMISO(ActionEvent event) {
        CNNTrainingService trainingService = new CNNTrainingService();
        trainingService.updateMISO();
    }

    @FXML
    private void handleUpdatePythonLocation(ActionEvent event) {
        String python = appPrefs.getPythonPath();
        if (python.equals("") || !(new File(python).exists())) python = System.getProperty("user.home");
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(python).getParentFile());
        File file = fc.showOpenDialog(this.stage);
        //Open
        if (file != null) {
            appPrefs.setPythonPath(file.getAbsolutePath());
            labelPythonLocation.setText("Python location: " + file.getAbsolutePath());
        }
    }

    @FXML
    private void handleLaunch(ActionEvent event) {
        GPUStatus status = CNNTrainingService.getNVIDIAStatus();
        if (status != null && (status.memoryPercentage > 50 || status.usagePercentage > 50)) {
            BasicDialogs.ShowError("GPU In Use", String.format("The GPU is currently in use (%d%% cores, %.0f%% memory)\n\nTraining may be delayed until any scripts have finished.", status.usagePercentage, status.memoryPercentage));
//            return;
        }
        CNNTrainingService trainingService = new CNNTrainingService();
        if (trainingService.getAnacondaInstallationLocation() == null) {
            BasicDialogs.ShowError("Python Missing", "Python could not be found at any of the default locations.\nPlease update its location");
        } else {
            App.getPrefs().setTrainingPath(textFieldOutputFolder.getText());

            if (radioButtonInputThisProject.isSelected() && supervisor.project.getFile() == null) {
                BasicDialogs.ShowError("Error", "You must save the project first");
                return;
            }
            trainingService.launchTraining(createTrainingScript());
        }
    }

    @FXML
    private void handleCreateScript(ActionEvent event) {
        if (radioButtonInputThisProject.isSelected() && supervisor.project.getFile() == null) {
            BasicDialogs.ShowError("Error", "You must save the project first");
            return;
        }
        CNNTrainingScript info = createTrainingScript();

        String script = null;
        try {
            script = info.getScript();
        } catch (IOException e) {
            BasicDialogs.ShowError("Error", "Could not create temporary directory for memmap file");
            return;
        }

        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(script);
        clipboard.setContent(content);

        AlertEx alert = new AlertEx(Alert.AlertType.INFORMATION, "Script has been copied to clipboard", ButtonType.OK);
        alert.setHeaderText("Script Generated");
        alert.showAndWait();
    }

    @Override
    public void postDialogSetup() {
        ((BorderPane) this.root).setPadding(new Insets(0));
    }

    @Override
    public void processDialogResult(ButtonType buttonType) {

    }

    @Override
    public String getHeader() {
        return "CNN Training";
    }

    @Override
    public String getSymbol() {
        return "featheractivity";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[]{ButtonType.CLOSE};
    }
}
