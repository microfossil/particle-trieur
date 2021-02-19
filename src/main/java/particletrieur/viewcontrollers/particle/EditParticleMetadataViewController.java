/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.particle;

import particletrieur.*;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Particle;
import particletrieur.controls.BasicDialogs;
import particletrieur.controls.ExpressionBuilderControl;
import particletrieur.services.ExtractMetadataFromFilenamesService;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import particletrieur.viewmodels.SelectionViewModel;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("/views/particle/EditParticleMetadataView.fxml")
public class EditParticleMetadataViewController extends AbstractDialogController implements Initializable {

    public RadioButton radioButtonClass;
    public RadioButton radioButtonSampleClass;
    public RadioButton radioButtonCustom;
    @FXML VBox vboxExtract;
    @FXML ComboBox<String> comboBoxSample;
    @FXML ComboBox<String> comboBoxNumeric1;
    @FXML ComboBox<String> comboBoxNumeric2;
    @FXML ComboBox<String> comboBoxResolution;
    @FXML Label labelExampleFilename;
    @FXML CheckBox checkBoxExtractFromFilename;
//    @FXML CheckBox checkboxIncludeParentDirectory;
    @FXML ExpressionBuilderControl expressionBuilderControl;

    List<Particle> particles;
    File displayFile = null;

    @Inject
    Supervisor supervisor;
    @Inject
    SelectionViewModel selectionViewModel;
    
    public EditParticleMetadataViewController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //Types of parameters in the filenames
        expressionBuilderControl.setParameters(new String[] {"label","sample","index1","index2","GUID"});

        //Use extraction and include parent directory options
        checkBoxExtractFromFilename.selectedProperty().addListener((obj,oldv,newv) -> {
            //expressionBuilderControl.setDisable(!newv);
            vboxExtract.setDisable(!newv);
            if (newv == true) expressionBuilderControl.refresh();
        });
//        checkBoxExtractFromFilename.setSelected(true);
        vboxExtract.setDisable(true);
//        checkboxIncludeParentDirectory.selectedProperty().addListener((ob,ov,nv) -> {
//            updateDisplayFile();
//        });
//        checkboxIncludeParentDirectory.setSelected(true);

        String prefs = App.getPrefs().getParseString();
        prefs = prefs.replace("$class$", "$label$");
        prefs = prefs.replace("$depthMin$", "$index1$");
        prefs = prefs.replace("$depthMax$", "$index2$");
        expressionBuilderControl.setSimpleRegex(prefs);
    }

    public void setup(List<Particle> particles) {

        List<String> samples = supervisor.project.particles.stream().map(Particle::getSampleID).distinct().collect(Collectors.toList());
        comboBoxSample.getItems().add("");
        comboBoxSample.getItems().addAll(samples);

        List<String> num1 = supervisor.project.particles.stream().map(Particle::getIndex1).distinct().map(v -> v.toString()).collect(Collectors.toList());
        comboBoxNumeric1.getItems().add("");
        comboBoxNumeric1.getItems().addAll(num1);

        List<String> num2 = supervisor.project.particles.stream().map(Particle::getIndex2).distinct().map(v -> v.toString()).collect(Collectors.toList());
        comboBoxNumeric2.getItems().add("");
        comboBoxNumeric2.getItems().addAll(num2);

        this.particles = particles;
        handlePickRandom(null);
    }

    public String getFileString(File file) {
//        if (checkboxIncludeParentDirectory.isSelected()) {
            Path rootPath = Paths.get(file.getParentFile().getParentFile().getParent());
            return FilenameUtils.removeExtension(rootPath.relativize(file.toPath()).toString().replace('\\', '/'));
//        }
//        else {
//            return FilenameUtils.removeExtension(file.getName());
//        }
    }
    
    private void updateDisplayFile(File file) {
        displayFile = file;
        labelExampleFilename.setText(getFileString(file));
        expressionBuilderControl.setDisplayText(getFileString(file));    
    }
    
    private void updateDisplayFile() {
        if (displayFile != null) {
            labelExampleFilename.setText(getFileString(displayFile));
            expressionBuilderControl.setDisplayText(getFileString(displayFile));   
        }
    }
    
    @FXML 
    private void handlePickRandom(ActionEvent event) {
        if (particles != null && particles.size() > 0) {
            int index = new Random().nextInt(particles.size());
            updateDisplayFile(particles.get(index).getFile());
        }
    }
    
    @FXML 
    private void handleSetMachineRegex(ActionEvent event) {
        expressionBuilderControl.setSimpleRegex("$skip$/$skip$/$skip$_$skip$_$sample$_$index1$-$index2$_$skip$_$GUID$");
    }

    @FXML
    private void handleSetFolderAsLabelRegex(ActionEvent event) {
        expressionBuilderControl.setSimpleRegex("$skip$/$label$/$end$");
    }

    @FXML
    private void handleSetFolderAsSampleLabelRegex(ActionEvent event) {
        expressionBuilderControl.setSimpleRegex("$sample$/$label$/$end$");
    }
    
    @FXML
    private void handleTestExtraction(ActionEvent event) {   
        if (particles == null || particles.size() == 0) {
            BasicDialogs.ShowInfo("Test extraction", "No files have been loaded yet.");
            return;
        }
        int goodCount = 0;
        int badCount = 0;
        String badString = "Example filenames that were not matched:\n";
        for (Particle particle : particles) {
            String filename = getFileString(particle.getFile());
            if (expressionBuilderControl.simpleExpression.canMatch(filename)) {
                goodCount++;
            }
            else {
                badCount++;
                if (badCount < 5) {
                    badString += filename + "\n";
                }
            }
        }
        if (goodCount == particles.size()) {
            BasicDialogs.ShowInfo("Test extraction", "All filenames were matched exactly.");
        }
        else {
            BasicDialogs.ShowInfo("Test extraction", 
                    String.format("%d of %d filenames were matched.\n\n%s", 
                            goodCount, particles.size(),badString));
        }
    }
    
    public HashMap<String,String> parseFile(File file) {
        return expressionBuilderControl.simpleExpression.parse(getFileString(file), expressionBuilderControl.getParameters());
    }

    @Override
    public void processDialogResult(ButtonType buttonType) {
        if (buttonType == ButtonType.OK) {
            if (particles == null || particles.size() == 0) return;

            String sample = comboBoxSample.getValue();
            String num1 = comboBoxNumeric1.getValue();
            String num2 = comboBoxNumeric2.getValue();
            String res = comboBoxResolution.getValue();

            Double index1 = null;
            Double index2 = null;
            Double resolution = null;

            StringBuilder sb = new StringBuilder();

            if (sample != null && sample.equals("")) {
                sample = null;
            }
            else if (sample != null) {
                sb.append("Sample updated to " + sample + "\n");
            }
            if (num1 != null) {
                try {
                    index1 = Double.parseDouble(num1);
                    sb.append("Index 1 updated to " + num1 + "\n");
                }
                catch (NumberFormatException ex) {
                    sb.append("Index 1 (" + num1 + ") was not updated as the value is not a number\n");
                }
            }
            if (num2 != null) {
                try {
                    index2 = Double.parseDouble(num2);
                    sb.append("Index 2 updated to " + num2 + "\n");
                }
                catch (NumberFormatException ex) {
                    sb.append("Index 2 (" + num2 + ") was not updated as the value is not a number\n");
                }
            }
            if (res != null) {
                try {
                    resolution = Double.parseDouble(res);
                    sb.append("Resolution updated to " + res + " pixels/mm\n");
                }
                catch (NumberFormatException ex) {
                    sb.append("Resolution (" + res + ") was not updated as the value is not a number\n");
                }
            }

            supervisor.project.setParticleMetadata(particles, sample, index1, index2, resolution);

            if (checkBoxExtractFromFilename.isSelected()) {
                App.getPrefs().setParseString(expressionBuilderControl.getSimpleRegex());
                Service<List<ExtractMetadataFromFilenamesService.MetadataUpdatePayload>> service =
                        ExtractMetadataFromFilenamesService.parseFilenamesAndUpdate(particles, supervisor.project, this);
                service.setOnSucceeded(event -> {
                    supervisor.project.setParticleMetadata(service.getValue());
                    selectionViewModel.currentParticleUpdatedEvent.broadcast();

                });
                BasicDialogs.ProgressDialogWithCancel2(
                        "Operation",
                        "Updating Metadata",
                        App.getRootPane(),
                        service).start();
            }
            else {
                if (sample != null || index1 != null || index2 != null || resolution != null) {
                    BasicDialogs.ShowInfo("Updated", sb.toString());
                }
            }
        }
    }

    @Override
    public String getHeader() {
        return "Update Metadata";
    }

    @Override
    public String getSymbol() {
        return "featheredit3";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[] { ButtonType.OK, ButtonType.CANCEL };
    }
}
