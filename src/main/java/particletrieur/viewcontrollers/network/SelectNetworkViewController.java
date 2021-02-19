/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.network;

import com.google.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import particletrieur.AbstractDialogController;
import particletrieur.FxmlLocation;
import particletrieur.models.Supervisor;
import particletrieur.models.network.classification.NetworkInfo;
import particletrieur.models.network.classification.NetworkLabel;
import particletrieur.viewmodels.network.CNNPredictionViewModel;

import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("/views/network/SelectNetworkView.fxml")
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

    @Inject
    Supervisor supervisor;

    @Inject
    CNNPredictionViewModel cnnPredictionViewModel;

    NetworkInfo def = null;
    
    public SelectNetworkViewController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (supervisor.project.getNetworkDefinition() != null) {
            def = supervisor.project.getNetworkDefinition().cloneByXML();
        }
        updateUI(true);
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
        NetworkInfo info = cnnPredictionViewModel.loadNetworkDefinition();
        if (info != null) {
            def = info;
            updateUI(false);
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
