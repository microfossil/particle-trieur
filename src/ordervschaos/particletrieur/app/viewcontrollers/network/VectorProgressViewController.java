package ordervschaos.particletrieur.app.viewcontrollers.network;

import javafx.scene.control.Button;
import ordervschaos.particletrieur.app.controls.SymbolLabel;
import ordervschaos.particletrieur.app.AbstractController;
import com.google.inject.Inject;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.viewmodels.network.NetworkViewModel;

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

        progressIndicator.setProgress(0.00001);

        networkViewModel.getVectorCalculationService().messageProperty().addListener((observable, oldValue, newValue) -> {
            labelProgress.setText(newValue);
        });

        networkViewModel.getVectorCalculationService().progressProperty().addListener((observable, oldValue, newValue) -> {
            progressIndicator.setProgress(newValue.doubleValue());
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
