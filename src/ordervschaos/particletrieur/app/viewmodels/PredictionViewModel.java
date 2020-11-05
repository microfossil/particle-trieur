package ordervschaos.particletrieur.app.viewmodels;

import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.models.network.classification.ClassificationSet;
import ordervschaos.particletrieur.app.models.network.classification.NetworkLabel;
import ordervschaos.particletrieur.app.services.network.CNNPredictionService;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.project.Project;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.project.Taxon;
import ordervschaos.particletrieur.app.services.network.KNNVectorPredictionService;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import com.google.inject.Inject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Service;
import ordervschaos.particletrieur.app.viewmanagers.UndoManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredictionViewModel {

    @Inject
    Supervisor supervisor;
    @Inject
    SelectionViewModel selectionViewModel;
    @Inject
    LabelsViewModel labelsViewModel;
    @Inject
    UndoManager undoManager;

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
