/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.viewcontrollers.network;

import ordervschaos.particletrieur.app.AbstractDialogController;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.FxmlLocation;
import ordervschaos.particletrieur.app.controls.BasicDialogs;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.network.classification.NetworkInfo;
import ordervschaos.particletrieur.app.models.network.classification.NetworkLabel;
import com.google.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("views/network/SelectNetworkView.fxml")
public class SelectNetworkViewController extends AbstractDialogController implements Initializable {

    @FXML FlowPane flowPaneLabels;
    @FXML Label labelName;
    @FXML Label labelDescription;
    @FXML Label labelType;
    @FXML Label labelProtobuf;
    @FXML Label labelCreated;
    @FXML Label labelSourceData;
    @FXML Label labelAccuracy;
    @FXML Label labelInputName;
    @FXML Label labelInputOperation;
    @FXML Label labelInputShape;
    @FXML Label labelOutputName;
    @FXML Label labelOutputOperation;
    @FXML Label labelOutputShape;
    @FXML Button buttonChoose;
    
    

    @Inject
    Supervisor supervisor;

    NetworkInfo def = null;
    
    public SelectNetworkViewController() {
        
//        Image iconImage = new Image(App.class.getResourceAsStream("resources/icon.png" ),64,,64,,true,true);
//        ((Stage)this.getDialogPane().getScene().getWindow()).getIcons().add(iconImage);
//        this.setTitle("Choose network");
//        this.setHeaderText("Choose network to use for classification");
//        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
//
//        // Set the controls
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../views/network/SelectNetworkView.fxml"));
//        fxmlLoader.setController(this);
//        try {
//            VBox content = fxmlLoader.load();
//            this.getDialogPane().setContent(content);
//        } catch (IOException exception) {
//             throw new RuntimeException(exception);
//        }
//
//        this.setResultConverter(dialogButton -> {
//            if (dialogButton == ButtonType.OK) {
//                return getData();
//            }
//            else {
//                return null;
//            }
//        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (supervisor.project.getNetworkDefinition() != null) {
            def = supervisor.project.getNetworkDefinition().cloneByXML();
        }
        updateUI(true);
        //this.getDialog().getDialogPane().setPadding(new Insets(0));
    }
    
    private void updateUI(boolean isInit) {
        if (def != null) {
            labelName.setText(def.name);
            labelDescription.setText(def.description);
            labelType.setText(def.type);
            labelProtobuf.setText(def.protobuf);
            labelCreated.setText(def.date);
            labelSourceData.setText(def.source_data);
            labelAccuracy.setText(
                    String.format("%.1f%% (prec: %.1f%%, rec: %.1f%%, f1: %.1f%%)",
                            def.accuracy * 100,
                            def.precision * 100,
                            def.recall * 100,
                            def.f1score * 100)
            );

            labelInputName.setText(def.inputs.get(0).name);
            labelInputOperation.setText(def.inputs.get(0).operation);
            labelInputShape.setText(String.format("%d x %d x %d",
                    def.inputs.get(0).height,
                    def.inputs.get(0).width,
                    def.inputs.get(0).channels));

            labelOutputName.setText(def.outputs.get(0).name);
            labelOutputOperation.setText(def.outputs.get(0).operation);
            labelOutputShape.setText(String.format("%d x %d x %d",
                    def.outputs.get(0).height,
                    def.outputs.get(0).width,
                    def.outputs.get(0).channels));

//            labelNetworkDefinitionFilename.setText(def.protobuf);
//            labelGraphFilename.setText(def.protobuf);
//            TensorInfo input = def.inputs.get(0);
//            TensorInfo output = def.outputs.get(0);
//            labelInputDimensions.setText(String.format("%d x %d x %d", input.width, input.height, input.channels));
//            labelInput.setText(input.operation);
//            labelOutput.setText(output.operation);
//            labelVector.setText("");
//            labelRange.setText(String.format("0 - %d", 0));
            flowPaneLabels.getChildren().clear();
            if (def.labels != null) {
                int i = 0;
                for (NetworkLabel networkLabel : def.labels) {
                    Label label = new Label(String.format("#%d:   %s", i, networkLabel.code));
                    label.setPadding(new Insets(2, 7, 2, 7));
                    label.setStyle("-fx-background-color: derive(-fx-accent, 100%); -fx-background-radius: 2;");
                    flowPaneLabels.getChildren().add(label);
                    i++;
                }
            }
            if (!isInit) this.getDialog().getDialogPane().getScene().getWindow().sizeToScene();
        }
    }

    @Override
    public void postDialogSetup() {
        this.getDialog().getDialogPane().getScene().getWindow().sizeToScene();
        ((BorderPane)this.root).setPadding(new Insets(0));
    }

    @FXML
    private void handleChooseNetwork(ActionEvent event) {
        FileChooser dc = new FileChooser();
        dc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Network Information (xml)", "*.xml"));
        String path = App.getPrefs().getTrainingPath();
        if(path != null && Files.exists(Paths.get(path))) {
            dc.setInitialDirectory(new File(path));
        }
        dc.setTitle("Select network definition");
        File file = dc.showOpenDialog(this.stage.getOwner());
        if (file == null) return;            
        try {
            def = NetworkInfo.load(file);
            updateUI(false);
            //TODO should this be here?
            App.getPrefs().setNetworkPath(file.getParent());
            App.getPrefs().save();
        }
        catch (Exception ex) {  
            BasicDialogs.ShowException("The definition file is not valid", ex);
        }
    }

    @FXML
    private void handleReset(ActionEvent event) {
        def = supervisor.project.getNetworkDefinition().cloneByXML();
        updateUI(false);
    }

    @Override
    public void processDialogResult(ButtonType buttonType) {
        if (buttonType == ButtonType.OK) {
            supervisor.project.setNetworkDefinition(def);
        }
    }

    @Override
    public String getHeader() {
        return "Classification Network";
    }

    @Override
    public String getSymbol() {
        return "feathercpu";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[] {ButtonType.OK, ButtonType.CANCEL};
    }


}
