package main.java.app.services.network;

import main.java.app.models.Supervisor;
import main.java.app.models.network.classification.ClassificationSet;
import main.java.app.models.project.Particle;
import main.java.app.models.processing.ParticleImage;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import main.java.app.services.ImageProcessingService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.opencv.core.Mat;

/**
 * Service to perform classification and vector calculating on sets of images
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 * <p>
 * Reference on batched streaming in Java 8:
 * https://stackoverflow.com/questions/30641383/java-8-stream-with-batch-processing
 */
public class CNNPredictionService {

    Supervisor supervisor;

    public CNNPredictionService(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    public Service<ClassificationSet> predictService(Particle currentParticle, boolean processBeforeClassification) {
        return new Service<ClassificationSet>() {
            @Override
            protected Task<ClassificationSet> createTask() {
                return new Task<ClassificationSet>() {
                    @Override
                    protected ClassificationSet call() throws Exception {
                        return predict(currentParticle, processBeforeClassification);
                    }
                };
            }
        };
    }

    public Service<HashMap<Particle, ClassificationSet>> predictService(List<Particle> currentParticles, boolean processBeforeClassification) {
        return new Service<HashMap<Particle, ClassificationSet>>() {
            @Override
            protected Task<HashMap<Particle, ClassificationSet>> createTask() {
                return new Task<HashMap<Particle, ClassificationSet>>() {
                    @Override
                    protected HashMap<Particle, ClassificationSet> call() throws Exception {
                        HashMap<Particle, ClassificationSet> classifications = new HashMap<>();
                        updateMessage("Calculating...");
                        final int total = currentParticles.size();
                        AtomicInteger idx = new AtomicInteger(0);
                        AtomicInteger skippedBecauseOfErrors = new AtomicInteger(0);

                        ImageProcessingService imageProcessingService = new ImageProcessingService(supervisor.FCNNSegmenterService);

                        //Break into batches
                        int BATCH = 16;
                        Instant start = Instant.now();
                        IntStream.range(0, (currentParticles.size() + BATCH - 1) / BATCH)
                                .mapToObj(i -> currentParticles.subList(i * BATCH, Math.min(currentParticles.size(), (i + 1) * BATCH)))
                                .forEach(batch -> {
                                    if (!this.isCancelled()) {
                                        //Calculate particle images
                                        ConcurrentHashMap<Particle, Mat> mats = new ConcurrentHashMap<>();
                                        batch.stream().forEach(particle -> {
//                                            System.out.println(particle.getFilename());
                                            if (!this.isCancelled()) {
                                                if (particle != null) {
                                                    Mat mat = particle.getMat();
                                                    if (mat != null) {
                                                        ParticleImage image;
                                                        if (processBeforeClassification) {
                                                            image = imageProcessingService.processForNetwork(mat,
                                                                    supervisor.project.processingInfo, supervisor.network.getNetworkInfo());
                                                        } else {
                                                            image = imageProcessingService.processForNetwork(mat, supervisor.network.getNetworkInfo());
                                                        }
                                                        mats.put(particle, image.workingImage.clone());
                                                        image.release();
                                                        mat.release();
                                                    }
                                                }
                                                idx.getAndIncrement();
                                            }
                                        });
                                        Instant end = Instant.now();
                                        long timeElapsed = Duration.between(start, end).toMillis() / 1000;
                                        if (skippedBecauseOfErrors.get() > 0) {
                                            updateMessage(String.format("%d/%d predictions complete (%d skipped)\n%d seconds elapsed", idx.get(), total, skippedBecauseOfErrors.get(), timeElapsed));
                                        } else {
                                            updateMessage(String.format("%d/%d predictions complete\n%d seconds elapsed", idx.get(), total, timeElapsed));
                                        }
                                        updateProgress(idx.get(), total);
                                        //Cleanup
                                        if (mats.size() > 0) {
                                            HashMap<Particle, ClassificationSet> results = supervisor.network.predictLabel(new HashMap<>(mats));
                                            for (Mat mat : mats.values()) {
                                                mat.release();
                                            }
                                            for (Map.Entry<Particle, ClassificationSet> entry : results.entrySet()) {
                                                classifications.put(entry.getKey(), entry.getValue());
                                            }
                                        }
                                    }
                                });
                        return classifications;
                    }
                };
            }
        };
    }

    public ClassificationSet predict(Particle particle, boolean processBeforeClassification) {
        if (particle != null && supervisor.network.isEnabled()) {
            ImageProcessingService imageProcessingService = new ImageProcessingService(supervisor.FCNNSegmenterService);
            Mat mat = particle.getMat();
            if (mat != null) {
                ParticleImage image;
                if (processBeforeClassification) {
                    image = imageProcessingService.processForNetwork(mat, supervisor.project.processingInfo, supervisor.network.getNetworkInfo());
                    mat.release();
                } else {
                    image = imageProcessingService.processForNetwork(mat, supervisor.network.getNetworkInfo());
                    mat.release();
                }
                ClassificationSet classificationSet = supervisor.network.predictLabel(image.workingImage);
                image.release();
                return classificationSet;
            }
        }
        return null;
    }
}
//
//    public Service estimateClass(
//            List<Particle> particles,
//            ProcessingInfo proDef,
//            Network network)
//    {
//        //Create service
//        Service<Void> service = new Service<Void>() {
//            @Override
//            protected Task<Void> createTask() {
//                return new Task<Void>() {
//                    @Override
//                    protected Void call() throws InterruptedException {
//                        updateMessage("Classifying...");
//                        final int total = particles.size();
//
//                        AtomicInteger idx = new AtomicInteger(0);
//                        AtomicInteger skippedBecauseOfErrors = new AtomicInteger(0);
//
//                        //Batch
//                        int BATCH = 64;
//                        IntStream.range(0, (particles.size()+BATCH-1)/BATCH)
//                            .mapToObj(i -> particles.subList(i*BATCH, Math.min(particles.size(), (i+1)*BATCH)))
//                            .forEach(batch -> {
//                                if (!this.isCancelled()) {
//                                    //Calculate mats
//                                    ConcurrentHashMap<Particle,Mat> mats = new ConcurrentHashMap<>();
//                                    batch.stream().forEach(foram -> {
//                                        System.out.println(foram.getFilename());
//                                        if (!this.isCancelled()) {
//                                            if (foram != null) {
//                                                ProcessedImage im = new ProcessedImage();
//                                                im.initialise(foram.getMat());
//                                                try {
//                                                    im.preprocessMatForNetwork(proDef, network.getNetworkDefinition());
////                                                    if (particle.morphologyStateProperty.get() == false) {
////                                                        particle.calculateMorphology(im, proDef);
////                                                    }
//                                                    Mat temp = im.getWorkingMat().clone();
//                                                    mats.put(foram, temp);
//                                                }
//                                                catch (Exception ex) {
//                                                    foram.setCNNVector(network.nullVector());
//                                                    skippedBecauseOfErrors.getAndIncrement();
//                                                }
//                                                im.release();
//                                            }
//                                            idx.getAndIncrement();
//                                            if (skippedBecauseOfErrors.get() > 0) {
//                                                updateMessage(String.format("%d/%d images classified (%d skipped)",idx.get(),total, skippedBecauseOfErrors.get()));
//                                            }
//                                            else {
//                                                updateMessage(String.format("%d/%d images classified",idx.get(),total));
//                                            }
//                                            updateProgress(idx.get(), total);
//                                        }
//                                    });
//                                    //Classify
//                                    HashMap<Particle,ClassificationSet> results = network.<Particle>classify(new HashMap<>(mats));
//                                    HashMap<Particle,float[]> vectors = network.<Particle>calculateVector(new HashMap<>(mats));
//                                    for(Mat mat : mats.values()) {
//                                        mat.release();
//                                    }
//                                    Platform.runLater(() -> {
//                                        for (Map.Entry<Particle,ClassificationSet> entry : results.entrySet()) {
//                                            entry.getKey().setClassificationsFromNetwork(entry.getValue(), network.getNetworkDefinition().name, proDef.getClassificationThreshold());
//                                        }
//                                        for (Map.Entry<Particle,float[]> entry : vectors.entrySet()) {
//                                            entry.getKey().setCNNVector(entry.getValue());
//                                        }
//                                    });
//                                }
//                        });
//                        return null;
//                    }
//                };
//            }
//        };
//        return service;
//    }

//    public Service calculateCNNVector(List<Particle> particles, Supervisor supervisor, boolean all) {
//
//        Network network = supervisor.network;
//        ProcessingInfo proDef = supervisor.app.processingInfo;
//        NetworkDefinition netDef = supervisor.app.getNetworkDefinition();
//
//        //Create service
//        Service<Void> service = new Service<Void>() {
//            @Override
//            protected Task<Void> createTask() {
//                return new Task<Void>() {
//                    @Override
//                    protected Void call() throws InterruptedException {
//                        updateMessage("Calculating...");
//
//                        final int total = particles.size();
//                        AtomicInteger idx = new AtomicInteger(0);
//                        AtomicInteger skippedBecauseOfErrors = new AtomicInteger(0);
//
//                        //Batch
//                        int BATCH = 64;
//                        IntStream.range(0, (particles.size()+BATCH-1)/BATCH)
//                            .mapToObj(i -> particles.subList(i*BATCH, Math.min(particles.size(), (i+1)*BATCH)))
//                            .forEach(batch -> {
//                                if (!this.isCancelled()) {
//                                    //Calculate mats
//                                    ConcurrentHashMap<Particle,Mat> mats = new ConcurrentHashMap<>();
//                                    batch.stream().forEach(particle -> {
//                                        System.out.println(particle.getFilename());
//                                        if (!this.isCancelled()) {
//                                            if (particle != null) {
//                                                if (all == true || particle.cnnVectorStateProperty.get() == false) {
//                                                    ProcessedImage im = new ProcessedImage();
//                                                    im.initialise(particle.getMat());
//                                                    try {
//                                                        im.preprocessMatForNetwork(proDef, network.getNetworkDefinition());
//                                                        Mat temp = im.getWorkingMat().clone();
//                                                        mats.put(particle, temp);
//                                                    }
//                                                    catch (Exception ex) {
//                                                        particle.setCNNVector(network.nullVector());
//                                                        skippedBecauseOfErrors.getAndIncrement();
//                                                    }
//                                                    im.release();
//                                                }
//                                            }
//                                            idx.getAndIncrement();
//                                            if (skippedBecauseOfErrors.get() > 0) {
//                                                updateMessage(String.format("%d/%d images processed (%d skipped)",idx.get(),total, skippedBecauseOfErrors.get()));
//                                            }
//                                            else {
//                                                updateMessage(String.format("%d/%d images processed",idx.get(),total));
//                                            }
//                                            updateProgress(idx.get(), total);
//                                        }
//                                    });
//                                    //Classify
//                                    if (mats.size() > 0) {
//                                        HashMap<Particle,float[]> results = network.<Particle>calculateVector(new HashMap<>(mats));
//                                        for(Mat mat : mats.values()) {
//                                            mat.release();
//                                        }
//                                        Platform.runLater(() -> {
//                                            for (Map.Entry<Particle,float[]> entry : results.entrySet()) {
//                                                entry.getKey().setCNNVector(entry.getValue());
//                                            }
//                                        });
//                                    }
//                                }
//
//                        });
//                        return null;
//                    }
//                };
//            }
//        };
//        return service;
//    }
//
//}


/*
cd "C:\Program Files\Oracle\VirtualBox\"
VBoxManage.exe modifyvm "macOS 10.14 Mojave" --cpuidset 00000001 000106e5 00100800 0098e3fd bfebfbff
VBoxManage setextradata "macOS 10.14 Mojave" "VBoxInternal/Devices/efi/0/Config/DmiSystemProduct" "iMac11,3"
VBoxManage setextradata "macOS 10.14 Mojave" "VBoxInternal/Devices/efi/0/Config/DmiSystemVersion" "1.0"
VBoxManage setextradata "macOS 10.14 Mojave" "VBoxInternal/Devices/efi/0/Config/DmiBoardProduct" "Iloveapple"
VBoxManage setextradata "macOS 10.14 Mojave" "VBoxInternal/Devices/smc/0/Config/DeviceKey" "ourhardworkbythesewordsguardedpleasedontsteal(c)AppleComputerInc"
VBoxManage setextradata "macOS 10.14 Mojave" "VBoxInternal/Devices/smc/0/Config/GetKeyFromRealSMC" 1
*/