/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.viewcontrollers;

import ordervschaos.particletrieur.app.AppPreferences;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.controls.ExpressionBuilderControl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;


/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ForamExtractParametersDialog extends Dialog<ForamExtractParametersDialog.Options> {

    @FXML ComboBox<String> comboBoxClassificationPosition;
    @FXML ComboBox<String> comboBoxDepthMinPosition;
    @FXML ComboBox<String> comboBoxDepthMaxPosition;
    @FXML ComboBox<String> comboBoxCoreIDPosition;
    @FXML ComboBox<String> comboBoxDelimiter;
    @FXML Label labelFileCount;
    @FXML ExpressionBuilderControl expressionBuilderControl;
    @FXML CheckBox checkboxIncludeParentDirectory;
    
    private static AppPreferences appPrefs = new AppPreferences();
    
    BooleanProperty useFolder = new SimpleBooleanProperty(true);
    List<Particle> particles;
    
    public final Options options = new Options();
    File root = null;
    
    public ForamExtractParametersDialog() {
        
        Image iconImage = new Image(App.class.getResourceAsStream("resources/icon.png" ),44, 44,true,true);
        ((Stage)this.getDialogPane().getScene().getWindow()).getIcons().add(iconImage);
        this.setTitle("Extract Metadata");
        this.setHeaderText("Edit the options for extracting information");
        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                        
        // Set the controls        
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("views/ForamExtractParametersView.fxml"));
        fxmlLoader.setController(this);
        try {
            Parent content = fxmlLoader.load();
            this.getDialogPane().setContent(content);
        } catch (IOException exception) {
             throw new RuntimeException(exception);
        }                
                                            
        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                appPrefs.setParseString(expressionBuilderControl.getSimpleRegex());
                appPrefs.save();
                return getOptions();
            }
            else {
                return null;
            }
        });
        
        checkboxIncludeParentDirectory.selectedProperty().addListener((ob,ov,nv) -> {
            expressionBuilderControl.setDisplayText(getFileString(particles.get(0).getFile()));
        });
        
        //Clear some parts on showing
        this.setOnShowing(event -> {
            expressionBuilderControl.setSimpleRegex(appPrefs.getParseString());
        });                    
        expressionBuilderControl.setParameters(new String[] {"class","depthMin","depthMax","coreID","GUID"});
    }    
    
    public void setData(List<Particle> particles) {
        this.particles = particles;
        if(particles.size() > 0) {
            labelFileCount.setText(String.format("%d particles selected", particles.size()));
            expressionBuilderControl.setDisplayText(getFileString(particles.get(0).getFile()));
        }
    }
        
    public String getFileString(File file) {
        if (checkboxIncludeParentDirectory.isSelected()) {
            Path rootPath = Paths.get(file.getParentFile().getParent());
            return FilenameUtils.removeExtension(rootPath.relativize(file.toPath()).toString().replace('\\', '/'));
        }
        else {
            return FilenameUtils.removeExtension(file.getName());
        }
    }
    
    @FXML 
    private void handlePickRandom(ActionEvent event) {
        int index = new Random().nextInt(particles.size());
        expressionBuilderControl.setDisplayText(getFileString(particles.get(index).getFile()));
    }
    
    @FXML 
    private void handleSetMachineRegex(ActionEvent event) {
        expressionBuilderControl.setSimpleRegex("$skip$/$skip$/$skip$_$skip$_$coreID$_$depthMin$-$depthMax$_($skip$)_$GUID$");
    }
    
    @FXML
    private void handleTestExtraction(ActionEvent event) {   
        if (particles == null || particles.size() == 0) {
            BasicDialogs.ShowInfo("Test extraction", "No files have been loaded yet.");
        }
        int goodCount = 0;
        int badCount = 0;
        String badString = "Example filenames that were not matched:";
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
    
    public Options getOptions() {
        options.useFolder = useFolder.get();
        //options.extractClassFromFilename = checkBoxExtractFromFilename.isSelected();
        //options.files = new ArrayList<>(files);
        return options;
    }
    
    public class Options {
        public boolean useFolder;
        public boolean extractClassFromFilename;
        public File baseFolder;
        public ArrayList<File> files;
    }
}
