package ordervschaos.particletrieur.app.viewmodels.network;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.controls.AlertEx;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
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
import ordervschaos.particletrieur.app.viewmodels.LabelsViewModel;
import ordervschaos.particletrieur.app.viewmodels.SelectionViewModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkViewModel {

    @Inject
    Supervisor supervisor;
    @Inject
    SelectionViewModel selectionViewModel;
    @Inject
    LabelsViewModel labelsViewModel;


    private Timer timer;

    public ObjectProperty<GPUStatus> GPUStatus = new SimpleObjectProperty<>();

    DoubleProperty knnThreshold = new SimpleDoubleProperty(0.8);
    public double getKnnThreshold() {
        return knnThreshold.get();
    }
    public DoubleProperty knnThresholdProperty() {
        return knnThreshold;
    }
    public void setKnnThreshold(double knnThreshold) {
        this.knnThreshold.set(knnThreshold);
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

    public void predictUsingkNN(List<Particle> particles) {
        //Check if any images are selected
        if (particles.size() == 0) {
            BasicDialogs.ShowError("Error", "No images selected.");
            return;
        }
        KNNVectorPredictionService KNNVectorPredictionService = new KNNVectorPredictionService(supervisor);
        Service<HashMap<Particle, ClassificationSet>> service = KNNVectorPredictionService.predictUsingKNNService(particles, supervisor.project.particles, 10);
        service.setOnSucceeded(event -> {
            HashMap<Particle, ClassificationSet> classifications = service.getValue();
            labelsViewModel.setLabelSet(classifications, getKnnThreshold(), true);
            selectionViewModel.currentParticleUpdatedEvent.broadcast();
        });
        if (particles.size() > 1) {
            BasicDialogs.ProgressDialogWithCancel2(
                    "Operation",
                    "kNN Prediction",
                    App.getRootPane(),
                    service).start();
        } else {
            service.start();
        }
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
}
