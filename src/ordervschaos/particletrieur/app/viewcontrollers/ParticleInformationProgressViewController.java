package ordervschaos.particletrieur.app.viewcontrollers;

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

import java.net.URL;
import java.util.ResourceBundle;

public class ParticleInformationProgressViewController extends AbstractController implements Initializable {

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
    Supervisor supervisor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        progressIndicator.setVisible(false);
        progressIndicator.setProgress(0.00001);

        Service service = supervisor.particleInformationManager.getService();

        service.runningProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
//                labelProgress.setVisible(false);
//                progressIndicator.setVisible(false);
            }
            else {
//                labelProgress.setVisible(true);
//                progressIndicator.setVisible(true);
            }
        });

        service.messageProperty().addListener((observable, oldValue, newValue) -> {
            labelProgress.setText(newValue);
        });

        service.progressProperty().addListener((observable, oldValue, newValue) -> {
            progressIndicator.setProgress(newValue.doubleValue());
            symbolLabelTick.setVisible(newValue.doubleValue() == 1.0);
            buttonPlayPause.setVisible(newValue.doubleValue() != 1.0);
        });

        supervisor.particleInformationManager.enabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                symbolLabelPlayPause.setSymbol("featherpause");
            }
            else {
                symbolLabelPlayPause.setSymbol("featherplay");
            }
        });

        buttonPlayPause.setOnAction(event -> {
            supervisor.particleInformationManager.toggleEnabled();
        });

    }
}
