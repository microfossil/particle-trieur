/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.viewmodels;

import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import ordervschaos.particletrieur.app.helpers.AutoCancellingServiceRunner;
import ordervschaos.particletrieur.app.helpers.CSEvent;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.network.classification.ClassificationSet;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.services.ImageProcessingService;
import ordervschaos.particletrieur.app.models.processing.ParticleImage;

import java.util.List;

import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import ordervschaos.particletrieur.app.services.network.CNNPredictionService;
import ordervschaos.particletrieur.app.services.network.KNNVectorPredictionService;
import org.opencv.core.Mat;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class SelectionViewModel {

    public CSEvent currentParticleUpdatedEvent = new CSEvent<>();
    public CSEvent<Boolean> nextImageRequested = new CSEvent<>();
    public CSEvent<Boolean> previousImageRequested = new CSEvent<>();

    public CSEvent<Integer> controlSelectIndex = new CSEvent<Integer>();
    public CSEvent<Integer> selectIndex = new CSEvent<Integer>();
    public CSEvent<int[]> selectIndicesRequested = new CSEvent<>();
    public CSEvent<int[]> shiftSelectIndices = new CSEvent<>();
    public CSEvent selectAllRequested = new CSEvent<>();

    public CSEvent increaseSizeRequested = new CSEvent<>();
    public CSEvent decreaseSizeRequested = new CSEvent<>();

    public SortedList<Particle> sortedList;
    public FilteredList<Particle> filteredList;
    
    //Current particle
    private final ObjectProperty<Particle> currentParticle = new SimpleObjectProperty<>();
    public ObjectProperty<Particle> currentParticleProperty() { return currentParticle; }
    private void setCurrentParticle(Particle value) { currentParticle.set(value); }
    public Particle getCurrentParticle() { return currentParticle.get(); }
    
    //Current particles
    private final ObservableList<Particle> currentParticles = FXCollections.observableArrayList();
    public ObservableList<Particle> getCurrentParticles() { return currentParticles; }
    public void setCurrentParticles(List<Particle> values) { currentParticles.clear(); currentParticles.addAll(values); }

    public int getCurrentParticleIndex() {
        int index =  supervisor.project.getParticles().indexOf(getCurrentParticle());
        return index;
    }

    public int getParticleIndex(Particle particle) {
        return supervisor.project.getParticles().indexOf(particle);
    }

    //Current tab
    public int selectedTabIndex = 0;

    //Particle image
    private final ObjectProperty<ParticleImage> currentParticleImage = new SimpleObjectProperty<>();
    public ObjectProperty<ParticleImage> currentParticleImageProperty() { return currentParticleImage; }
    private void setCurrentParticleImage(ParticleImage value) { currentParticleImage.set(value); }
    public ParticleImage getCurrentParticleImage() { return currentParticleImage.get(); }

    // Parts
    private Supervisor supervisor;
    private ImageProcessingService imageProcessingService;
    public KNNPredictionViewModel knnPredictionViewModel;
    public CNNPredictionViewModel cnnPredictionViewModel;

    public BooleanProperty getImageProcessingIsRunningProperty() {
        return imageProcessingService.runningProperty();
    }

    @Inject
    public SelectionViewModel(Supervisor supervisor) {
        this.supervisor = supervisor;
        filteredList = new FilteredList<>(supervisor.project.particles, p -> true);
        sortedList = new SortedList<>(filteredList);

        knnPredictionViewModel = new KNNPredictionViewModel(supervisor);
        cnnPredictionViewModel = new CNNPredictionViewModel(supervisor);
        imageProcessingService = new ImageProcessingService(supervisor.FCNNSegmenter);

        currentParticles.addListener((ListChangeListener<? super Particle>) listener -> {
            if (getCurrentParticles().size() > 0) {
                setCurrentParticle(getCurrentParticles().get(0));
            } else {
                setCurrentParticle(null);
            }
        });

        currentParticleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getFile().exists()) {
                Mat mat = newValue.getMat();
                if (mat != null) {
                    imageProcessingService.processAsyncAndReleaseMat(mat, supervisor.project.processingInfo, currentParticleImage);
                    knnPredictionViewModel.onCurrentParticleUpdated(newValue);
                    cnnPredictionViewModel.onCurrentParticleUpdated(newValue);
                }
            }
            else {
                setCurrentParticleImage(null);
            }
        });
    }

    public void refreshParticleImage() {
        if (getCurrentParticle() != null) {
            Mat mat = getCurrentParticle().getMat();
            if (mat != null) {
                imageProcessingService.processAsyncAndReleaseMat(mat, supervisor.project.processingInfo, currentParticleImage);
            }
        }
    }

    public void refreshPredictions() {
        knnPredictionViewModel.onCurrentParticleUpdated(getCurrentParticle());
        cnnPredictionViewModel.onCurrentParticleUpdated(getCurrentParticle());
    }

    public void checkIfCurrentWasUpdated(Particle particle) {
        if (particle == getCurrentParticle()) currentParticleUpdatedEvent.broadcast();
    }

    public void checkIfCurrentWasUpdated(List<Particle> particles) {
        if (particles.contains(getCurrentParticle())) currentParticleUpdatedEvent.broadcast();
    }
    /**
     * Monitors the currently selected particle and predicts what label it should have
     */
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

        private Supervisor supervisor;
        private ordervschaos.particletrieur.app.services.network.KNNVectorPredictionService KNNVectorPredictionService;

        public KNNPredictionViewModel(Supervisor supervisor) {
            this.supervisor = supervisor;
            KNNVectorPredictionService = new KNNVectorPredictionService(supervisor);
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
                }
                else {
                    setkNNPredictedClassification(null);
                }
            });
            service.setOnFailed(event -> {
                Exception ex = new Exception(service.getException());
                ex.printStackTrace();
            });
            kNNPredictionServiceRunner.run(service);
        }
    }

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

        private Supervisor supervisor;
        private CNNPredictionService cnnPredictionService;

        public CNNPredictionViewModel(Supervisor supervisor) {
            this.supervisor = supervisor;
            this.cnnPredictionService = new CNNPredictionService(supervisor);
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
    }
}
