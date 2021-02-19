package main.java.app.viewmodels.network;

import com.google.inject.Inject;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import main.java.app.App;
import main.java.app.controls.AlertEx;
import main.java.app.controls.BasicDialogs;
import main.java.app.helpers.AutoCancellingServiceRunner;
import main.java.app.models.Supervisor;
import main.java.app.models.network.classification.ClassificationSet;
import main.java.app.models.project.Particle;
import main.java.app.services.network.KNNVectorPredictionService;
import main.java.app.viewmodels.SelectionViewModel;
import main.java.app.viewmodels.particles.LabelsViewModel;

import java.util.HashMap;
import java.util.List;

public class KNNPredictionViewModel {

    public final int K = 10;

    private AutoCancellingServiceRunner<ClassificationSet> kNNPredictionServiceRunner = new AutoCancellingServiceRunner<>("knn");

    private ObjectProperty<ClassificationSet> kNNPredictedClassification = new SimpleObjectProperty<>();
    public ClassificationSet getkNNPredictedClassification() {
        return kNNPredictedClassification.get();
    }
    public ObjectProperty<ClassificationSet> kNNPredictedClassificationProperty() {
        return kNNPredictedClassification;
    }
    public void setkNNPredictedClassification(ClassificationSet kNNPredictedClassification) {
        this.kNNPredictedClassification.set(kNNPredictedClassification);
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

    Supervisor supervisor;
    NetworkViewModel networkViewModel;
    SelectionViewModel selectionViewModel;
    LabelsViewModel labelsViewModel;
    private KNNVectorPredictionService KNNVectorPredictionService;

    @Inject
    public KNNPredictionViewModel(Supervisor supervisor,
                                  NetworkViewModel networkViewModel,
                                  SelectionViewModel selectionViewModel,
                                  LabelsViewModel labelsViewModel)  {
        this.supervisor = supervisor;
        this.networkViewModel = networkViewModel;
        this.selectionViewModel = selectionViewModel;
        this.labelsViewModel = labelsViewModel;

        KNNVectorPredictionService = new KNNVectorPredictionService(supervisor);
        selectionViewModel.currentParticleProperty().addListener((observable, oldValue, newValue) -> {
            onCurrentParticleUpdated(newValue);
        });
    }

    public void onCurrentParticleUpdated(Particle particle) {
        if (particle == null) {
            setkNNPredictedClassification(null);
            return;
        }
        Service<ClassificationSet> service = KNNVectorPredictionService.predictUsingKNNService(particle, supervisor.project.getParticles(), K);
        service.setOnCancelled(event -> {

        });
        service.setOnSucceeded(event -> {
            ClassificationSet classificationSet = service.getValue();
            if (classificationSet != null) {
                setkNNPredictedClassification(classificationSet);
            } else {
                setkNNPredictedClassification(null);
            }
        });
        service.setOnFailed(event -> {
            Exception ex = new Exception(service.getException());
            ex.printStackTrace();
        });
        kNNPredictionServiceRunner.run(service);
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


    public void refreshPredictions() {
        onCurrentParticleUpdated(selectionViewModel.getCurrentParticle());
    }

    public void calculateVectors() {
        AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION, "This will reset all feature vectors. Are you sure?", ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.YES) {
                networkViewModel.recalculateAll();
            }
        });
    }
}
