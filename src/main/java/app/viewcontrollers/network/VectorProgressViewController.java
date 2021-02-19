package main.java.app.viewcontrollers.network;

import javafx.scene.control.Button;
import main.java.app.controls.SymbolLabel;
import main.java.app.AbstractController;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import main.java.app.viewmodels.network.NetworkViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public class VectorProgressViewController extends AbstractController implements Initializable {

    @FXML
    SymbolLabel symbolLabelTick;
    @FXML
    Label labelProgress;
    @FXML
    ProgressBar progressIndicator;
    @FXML
    Button buttonPlayPause;
    @FXML
    SymbolLabel symbolLabelPlayPause;

    @Inject
    NetworkViewModel networkViewModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        progressIndicator.setProgress(0.00001);

        networkViewModel.getVectorCalculationService().messageProperty().addListener((observable, oldValue, newValue) -> {
            labelProgress.setText(newValue);
        });

        networkViewModel.getVectorCalculationService().progressProperty().addListener((observable, oldValue, newValue) -> {
            progressIndicator.setProgress(newValue.doubleValue());
            progressIndicator.setVisible(newValue.doubleValue() != 1.0);
            symbolLabelTick.setVisible(newValue.doubleValue() == 1.0);
            buttonPlayPause.setVisible(newValue.doubleValue() != 1.0);
        });

        networkViewModel.vectorCalculationEnabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                symbolLabelPlayPause.setSymbol("featherpause");
            }
            else {
                symbolLabelPlayPause.setSymbol("featherplay");
            }
        });

        buttonPlayPause.setOnAction(event -> {
            networkViewModel.toggleEnabled();
        });
    }
}
