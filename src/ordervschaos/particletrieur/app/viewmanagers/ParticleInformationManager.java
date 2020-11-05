package ordervschaos.particletrieur.app.viewmanagers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import ordervschaos.particletrieur.app.models.network.features.ResNet50FeatureVectorService;
import ordervschaos.particletrieur.app.models.Supervisor;
import com.google.inject.Inject;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;

/**
 * Monitors the list of particles and calculates new properties when they change
 */
public class ParticleInformationManager {

    private Service service;
    public Service getService() { return service; }

    Supervisor supervisor;
    ResNet50FeatureVectorService resNet50FeatureVectorService;

    private BooleanProperty enabled = new SimpleBooleanProperty(true);
    public boolean isEnabled() {
        return enabled.get();
    }
    public BooleanProperty enabledProperty() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }
    public void toggleEnabled() {
        this.enabled.set(!enabled.get());
    }

    @Inject
    public ParticleInformationManager(Supervisor supervisor) {

        this.supervisor = supervisor;
        this.resNet50FeatureVectorService = supervisor.ResNet50FeatureVectorService;

        service = createService();
        service.setOnSucceeded(event -> {
            resNet50FeatureVectorService.isRecalculate = false;
        });

        supervisor.project.particles.addListener((ListChangeListener) observable -> {
            if (isEnabled()) service.restart();
        });

        enabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) service.restart();
            else service.cancel();
        });
    }

    public void recalculateAll() {
        supervisor.project.particles.stream().forEach(p -> supervisor.project.setParticleCNNVector(p, null));
        setEnabled(true);
        resNet50FeatureVectorService.isRecalculate = true;
        service.restart();
    }

    private Service createService() {
        return resNet50FeatureVectorService.calculateCNNVector(supervisor);
    }
}
