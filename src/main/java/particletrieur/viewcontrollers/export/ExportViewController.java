/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.export;

import javafx.beans.binding.Bindings;
import javafx.util.StringConverter;
import particletrieur.App;
import particletrieur.AppController;
import particletrieur.controls.dialogs.AlertEx;
import particletrieur.models.project.Project;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Tag;
import particletrieur.models.project.Taxon;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import particletrieur.services.export.ExportImagesService;
import particletrieur.AbstractDialogController;
import particletrieur.FxmlLocation;
import particletrieur.services.export.ExportMorphologyService;
import particletrieur.viewmodels.SelectionViewModel;
import particletrieur.controls.dialogs.BasicDialogs;
import com.google.inject.Inject;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("/views/ExportView.fxml")
public class ExportViewController extends AbstractDialogController implements Initializable {

    @FXML
    RadioButton radioButtonOriginalFilenames;
    @FXML
    CheckBox checkBoxResize;
    @FXML
    ComboBox<Integer> comboBoxResize;
    @FXML
    RadioButton radioButtonProcess;

    @FXML CheckBox checkBoxPrefix;
    @FXML CheckBox checkBoxLabel;
    @FXML CheckBox checkBoxSample;
    @FXML CheckBox checkBoxIndex1;
    @FXML CheckBox checkBoxIndex2;
    @FXML CheckBox checkBoxGUID;
    @FXML TextField prefixTextField;

    @FXML
    ComboBox<ExportImagesService.ImageFormat> comboBoxConvertMode;
    @FXML
    FlowPane flowPaneLabels;
    @FXML
    FlowPane flowPaneLabelsNonTaxonomic;
    @FXML
    FlowPane flowPaneTags;

    @FXML ComboBox<ExportImagesService.FolderMode> folderOrganisationComboBox;

    ArrayList<String> taxonsToExport = new ArrayList<>();
    ArrayList<String> tagsToNotExport = new ArrayList<>();

    public ExportImagesService exportImagesService;

    Supervisor supervisor;
    SelectionViewModel selectionViewModel;

    @Inject
    public ExportViewController(Supervisor supervisor, SelectionViewModel selectionViewModel) {
        this.supervisor = supervisor;
        this.selectionViewModel = selectionViewModel;
        exportImagesService = new ExportImagesService(supervisor);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        folderOrganisationComboBox.getItems().setAll(ExportImagesService.FolderMode.values());
        folderOrganisationComboBox.getSelectionModel().select(1);

        comboBoxConvertMode.getItems().setAll(ExportImagesService.ImageFormat.values());
        comboBoxConvertMode.getSelectionModel().select(0);

        comboBoxResize.setConverter(new StringConverter<Integer>() {
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
                    result = -1;
                }
                return result;
            }
        });
        comboBoxResize.getItems().addAll(64, 96, 128, 160, 192, 224, 256, 512, 1024);
        comboBoxResize.getSelectionModel().select(5);
        comboBoxResize.disableProperty().bind(Bindings.not(checkBoxResize.selectedProperty()));

        setupLabelsUI();
        setupTagsUI();
    }

    private void setupLabelsUI() {
        boolean wasMorphotypes = false;
        boolean wasOthers = false;
        for (Taxon taxon : supervisor.project.getTaxons().values()) {
            CheckBox checkBox = new CheckBox(taxon.getCode());
            checkBox.setMnemonicParsing(false);
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) taxonsToExport.add(taxon.getCode());
                else taxonsToExport.remove(taxon.getCode());
            });
            if (taxon.getIsClass()) {
                checkBox.setSelected(true);
                flowPaneLabels.getChildren().add(checkBox);
                wasMorphotypes = true;
            }
            else {
                checkBox.setSelected(false);
                flowPaneLabelsNonTaxonomic.getChildren().add(checkBox);
                wasOthers = true;
            }
        }
        if (!wasMorphotypes) flowPaneLabels.getChildren().add(new Label("none"));
        if (!wasOthers) flowPaneLabelsNonTaxonomic.getChildren().add(new Label("none"));
    }

    private void setupTagsUI() {
        for (Tag tag : supervisor.project.getTags().values()) {
            CheckBox checkBox = new CheckBox(tag.getCode());
            checkBox.setMnemonicParsing(false);
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(!newValue) tagsToNotExport.add(tag.getCode());
                else tagsToNotExport.remove(tag.getCode());
            });
            checkBox.setSelected(true);
            flowPaneTags.getChildren().add(checkBox);
        }
    }

    private void launchExport() {
        //Processing
        exportImagesService.performPreprocessing = radioButtonProcess.isSelected();
        exportImagesService.conversionFormat = comboBoxConvertMode.getValue();
        exportImagesService.performResize = checkBoxResize.isSelected();
        exportImagesService.resizeLength = comboBoxResize.getValue();

        //Labels
        exportImagesService.taxonCodes = taxonsToExport;

        //Tags
        exportImagesService.tagCodes = tagsToNotExport;

        //Organisation
        // - file parts
        exportImagesService.useOriginalFilenames = radioButtonOriginalFilenames.isSelected();
        exportImagesService.includeClassification = checkBoxLabel.isSelected();
        exportImagesService.includeSample = checkBoxSample.isSelected();
        exportImagesService.includeIndex1 = checkBoxIndex1.isSelected();
        exportImagesService.includeIndex2 = checkBoxIndex2.isSelected();
        exportImagesService.includeGUID = checkBoxGUID.isSelected();
        // - prefix
        exportImagesService.includePrefix = checkBoxPrefix.isSelected();
        exportImagesService.prefix = prefixTextField.getText();
        // - folder
        exportImagesService.folderMode = folderOrganisationComboBox.getValue();

        //Choose directory for export
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select export directory");
        String path = App.getPrefs().getExportPath();
        if(path != null && Files.exists(Paths.get(path))) {
            chooser.setInitialDirectory(new File(path));
        }
        File res = chooser.showDialog(AppController.getStage());
        if (res == null) return;
        File outputDirectory = new File(res.getAbsolutePath());
        exportImagesService.outputDirectory = outputDirectory;

        //Show dialog to confirm we want to proceed
        AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Export labeled images.");
        alert.setContentText("Exporting files to "  + outputDirectory.getAbsolutePath() + ".\n\n This may take a long time.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showEmbedded();
        alert.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                App.getPrefs().setExportPath(outputDirectory.getAbsolutePath());
                Service service = exportImagesService.exportImages();
                BasicDialogs.ProgressDialogWithCancel2(
                        "Operation",
                        "Exporting images",
                        service).start();
            }
            return null;
        });
    }

    @Override
    public void processDialogResult(ButtonType buttonType) {
        if (buttonType == ButtonType.OK) {
            launchExport();
        }
    }

    @Override
    public String getHeader() {
        return "Export Images";
    }

    @Override
    public String getSymbol() {
        return "featherupload";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[] { ButtonType.OK, ButtonType.CANCEL };
    }
}
