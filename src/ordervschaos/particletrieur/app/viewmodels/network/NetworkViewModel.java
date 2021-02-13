package ordervschaos.particletrieur.app.viewmodels.network;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.controls.AlertEx;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.models.Supervisor;
import com.google.inject.Inject;
import ordervschaos.particletrieur.app.models.network.classification.NetworkInfo;
import ordervschaos.particletrieur.app.models.network.training.GPUStatus;
import ordervschaos.particletrieur.app.services.network.CNNTrainingService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkViewModel {

    @Inject
    Supervisor supervisor;

    private Timer timer;

    public ObjectProperty<GPUStatus> GPUStatus = new SimpleObjectProperty<>();

    public NetworkViewModel() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    GPUStatus.set(CNNTrainingService.getNVIDIAStatus());
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
    }

    public NetworkInfo loadNetworkDefinition() {
        FileChooser dc = new FileChooser();
        dc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Network Information (xml)", "*.xml"));
        String path = App.getPrefs().getTrainingPath();
        if(path != null && Files.exists(Paths.get(path))) {
            dc.setInitialDirectory(new File(path));
        }
        dc.setTitle("Select network definition");
        File file = dc.showOpenDialog(App.getWindow());
        if (file == null) return null;
        NetworkInfo def = null;
        try {
            def = NetworkInfo.load(file);
        }
        catch (Exception ex) {
            BasicDialogs.ShowException("The definition file is not valid", ex);
        }
        return def;
    }
}
