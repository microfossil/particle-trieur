package ordervschaos.particletrieur.app.viewmodels.network;

import com.google.inject.Inject;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.stage.FileChooser;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.helpers.AutoCancellingServiceRunner;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.network.classification.ClassificationSet;
import ordervschaos.particletrieur.app.models.network.classification.NetworkInfo;
import ordervschaos.particletrieur.app.models.network.classification.NetworkLabel;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.project.Project;
import ordervschaos.particletrieur.app.models.project.Taxon;
import ordervschaos.particletrieur.app.services.network.CNNPredictionService;
import ordervschaos.particletrieur.app.viewmodels.SelectionViewModel;
import ordervschaos.particletrieur.app.viewmodels.particles.LabelsViewModel;
import org.opencv.core.Mat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

/**
 * Monitors the currently selected particle and predicts what label it should have
 */
public class CNNPredictionViewModel {

    public final int K = 10;
    private AutoCancellingServiceRunner<ClassificationSet> cnnPredictionServiceRunner = new AutoCancellingServiceRunner<>("cnn");

    private ObjectProperty<ClassificationSet> cnnPredictedClassification = new SimpleObjectProperty<>();
    public ClassificationSet getCnnPredictedClassification() {
        return cnnPredictedClassification.get();
    }
    public ObjectProperty<ClassificationSet> cnnPredictedClassificationProperty() {
        return cnnPredictedClassification;
    }
    public void setCnnPredictedClassification(ClassificationSet cnnPredictedClassification) {
        this.cnnPredictedClassification.set(cnnPredictedClassification);
    }

    private BooleanProperty running = new SimpleBooleanProperty(false);
    public boolean isRunning() {
        return running.get();
    }
    public BooleanProperty runningProperty() {
        return running;
    }
    public void setRunning(boolean running) {
        this.running.set(running);
    }

    DoubleProperty cnnThreshold = new SimpleDoubleProperty(0.8);
    public double getCnnThreshold() {
        return cnnThreshold.get();
    }
    public DoubleProperty cnnThresholdProperty() {
        return cnnThreshold;
    }
    public void setCnnThreshold(double cnnThreshold) {
        this.cnnThreshold.set(cnnThreshold);
    }


    Supervisor supervisor;
    SelectionViewModel selectionViewModel;
    LabelsViewModel labelsViewModel;
    private CNNPredictionService cnnPredictionService;

    @Inject
    public CNNPredictionViewModel(Supervisor supervisor, SelectionViewModel selectionViewModel, LabelsViewModel labelsViewModel) {
        this.supervisor = supervisor;
        this.selectionViewModel = selectionViewModel;
        this.labelsViewModel = labelsViewModel;

        this.cnnPredictionService = new CNNPredictionService(supervisor);
        selectionViewModel.currentParticleProperty().addListener((observable, oldValue, newValue) -> {
            onCurrentParticleUpdated(newValue);
        });
    }

    public void onCurrentParticleUpdated(Particle particle) {
        if (particle == null || !supervisor.network.isEnabled()) {
            setCnnPredictedClassification(null);
            return;
        }

        Service<ClassificationSet> service = cnnPredictionService.predictService(particle, supervisor.project.processingInfo.getProcessBeforeClassification());
        service.setOnCancelled(event -> {

        });

        //TODO remove the thread is interupted from the services
        service.setOnSucceeded(event -> {
            if (service.getValue() != null) {
                setCnnPredictedClassification(service.getValue());
            }
            setRunning(false);
        });

        service.setOnFailed(event -> {
            Exception ex = new Exception(service.getException());
            ex.printStackTrace();
            setRunning(false);
        });
        setRunning(true);
        cnnPredictionServiceRunner.run(service);
    }

    //TODO preprocess before classification should be in here as a setting or there should be some kind of global settings
    //Maybe project prefs?
    public void predictUsingCNN(List<Particle> particles, boolean processBeforeClassification) {
        //Check if network is enabled
        if (!supervisor.network.isEnabled()) {
            BasicDialogs.ShowError("Error", "Please configure a CNN to classify images.");
            return;
        }
        //Check if any images are selected
        if (particles.size() == 0) {
            BasicDialogs.ShowError("Error", "No images selected.");
            return;
        }
        //Make sure the relevant taxons are in the app
        for (NetworkLabel networkLabel : supervisor.project.getNetworkDefinition().labels) {
            if (!supervisor.project.taxons.containsKey(networkLabel.code)) {
                try {
                    supervisor.project.addTaxon(new Taxon(networkLabel.code, "", "", "", true));
                } catch (Project.TaxonAlreadyExistsException ex) {
                    //Should never arrive here
                }
            }
        }
        //Try to classify
        CNNPredictionService cnnPredictionService = new CNNPredictionService(supervisor);
        Service<HashMap<Particle,ClassificationSet>> service = cnnPredictionService.predictService(particles, processBeforeClassification);
        service.setOnSucceeded(event -> {
            HashMap<Particle,ClassificationSet> classifications = service.getValue();
            labelsViewModel.setLabelSet(classifications, getCnnThreshold(), true);
            selectionViewModel.currentParticleUpdatedEvent.broadcast();
        });
        service.setOnFailed(event -> {
            BasicDialogs.ShowException("Error", new Exception(service.getException()));
        });
        if (particles.size() > 1) {
            BasicDialogs.ProgressDialogWithCancel2(
                    "Operation",
                    "CNN Prediction",
                    App.getRootPane(),
                    service).start();
        } else {
            service.start();
        }
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
