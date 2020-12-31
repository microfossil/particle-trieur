package ordervschaos.particletrieur.app.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ordervschaos.particletrieur.app.controls.AlertEx;
import ordervschaos.particletrieur.app.models.Supervisor;
import com.google.inject.Inject;
import ordervschaos.particletrieur.app.models.network.training.GPUStatus;
import ordervschaos.particletrieur.app.services.network.CNNTrainingService;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class CNNVectorViewModel {

    @Inject
    Supervisor supervisor;

    private Timer timer;

    public ObjectProperty<GPUStatus> GPUStatus = new SimpleObjectProperty<>();

    public CNNVectorViewModel() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {
                        GPUStatus.set(CNNTrainingService.getNVIDIAStatus());
                    } catch (IOException e) {

                    }
                });
            }
        }, 0, 10000);
    }

    public void Stop() {
        timer.cancel();
    }

    public void calculateVectors() {

        AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION, "This will reset all feature vectors. Are you sure?", ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.YES) {
                supervisor.particleInformationManager.recalculateAll();
            }
        });
//
//        Service service = supervisor.ResNet50FeatureVectorService.calculateCNNVector(supervisor);
//        BasicDialogs.ProgressDialogWithCancel2(
//                "Operation",
//                "Calculating Vectors",
//                App.getRootPane(),
//                service).start();
    }
}
