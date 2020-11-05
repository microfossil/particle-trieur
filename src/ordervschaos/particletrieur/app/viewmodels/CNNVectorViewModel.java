package ordervschaos.particletrieur.app.viewmodels;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ordervschaos.particletrieur.app.controls.AlertEx;
import ordervschaos.particletrieur.app.models.Supervisor;
import com.google.inject.Inject;

public class CNNVectorViewModel {

    @Inject
    Supervisor supervisor;

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
