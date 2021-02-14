package ordervschaos.particletrieur.app.viewmodels.network;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.network.features.ResNet50FeatureVectorService;
import ordervschaos.particletrieur.app.models.network.training.GPUStatus;
import ordervschaos.particletrieur.app.services.network.CNNTrainingService;

import java.util.Timer;
import java.util.TimerTask;

public class NetworkViewModel {

    private Service vectorCalculationService;
    public Service getVectorCalculationService() { return vectorCalculationService; }

    private BooleanProperty vectorCalculationEnabled = new SimpleBooleanProperty(true);
    public boolean getVectorCalculationEnabled() {
        return vectorCalculationEnabled.get();
    }
    public BooleanProperty vectorCalculationEnabledProperty() {
        return vectorCalculationEnabled;
    }
    public void setVectorCalculationEnabled(boolean vectorCalculationEnabled) {
        this.vectorCalculationEnabled.set(vectorCalculationEnabled);
    }
    public void toggleEnabled() {
        this.vectorCalculationEnabled.set(!vectorCalculationEnabled.get());
    }

    public ObjectProperty<GPUStatus> GPUStatus = new SimpleObjectProperty<>();

    private Timer timer;

    private Supervisor supervisor;
    public final ResNet50FeatureVectorService resNet50FeatureVectorService = new ResNet50FeatureVectorService();

    @Inject
    public NetworkViewModel(Supervisor supervisor) {
        this.supervisor = supervisor;

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    GPUStatus.set(CNNTrainingService.getNVIDIAStatus());
                });
            }
        }, 0, 10000);

        vectorCalculationService = resNet50FeatureVectorService.calculateCNNVector(supervisor);
        vectorCalculationService.setOnSucceeded(event -> {
            resNet50FeatureVectorService.isRecalculate = false;
        });
        supervisor.project.particles.addListener((ListChangeListener) observable -> {
            if (getVectorCalculationEnabled()) vectorCalculationService.restart();
        });

        vectorCalculationEnabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) vectorCalculationService.restart();
            else vectorCalculationService.cancel();
        });
    }

    public void recalculateAll() {
        supervisor.project.particles.stream().forEach(p -> supervisor.project.setParticleCNNVector(p, null));
        setVectorCalculationEnabled(true);
        resNet50FeatureVectorService.isRecalculate = true;
        vectorCalculationService.restart();
    }

    public void Stop() {
        timer.cancel();
    }
}
