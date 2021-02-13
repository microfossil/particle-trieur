package ordervschaos.particletrieur.app.viewmodels.network;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.controls.AlertEx;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.helpers.AutoCancellingServiceRunner;
import ordervschaos.particletrieur.app.models.Supervisor;
import com.google.inject.Inject;
import ordervschaos.particletrieur.app.models.network.classification.ClassificationSet;
import ordervschaos.particletrieur.app.models.network.classification.NetworkInfo;
import ordervschaos.particletrieur.app.models.network.classification.NetworkLabel;
import ordervschaos.particletrieur.app.models.network.training.GPUStatus;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.project.Project;
import ordervschaos.particletrieur.app.models.project.Taxon;
import ordervschaos.particletrieur.app.services.network.CNNPredictionService;
import ordervschaos.particletrieur.app.services.network.CNNTrainingService;
import ordervschaos.particletrieur.app.services.network.KNNVectorPredictionService;
import ordervschaos.particletrieur.app.viewmodels.particles.LabelsViewModel;
import ordervschaos.particletrieur.app.viewmodels.SelectionViewModel;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkViewModel {


    public ObjectProperty<GPUStatus> GPUStatus = new SimpleObjectProperty<>();
    private Timer timer;

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
}
