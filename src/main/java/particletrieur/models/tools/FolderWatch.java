/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.models.tools;

import particletrieur.controls.BasicDialogs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import particletrieur.models.Supervisor;
import particletrieur.models.network.classification.ClassificationSet;
import particletrieur.models.project.Particle;
import particletrieur.models.project.Project;
import particletrieur.services.network.CNNPredictionService;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class FolderWatch implements Runnable {
    
    private Supervisor supervisor;
    private WatchService watchService;
    private WatchKey key;
    private File folder;
    ExecutorService executor;
    Future<?> future;
    
    private final BooleanProperty enabled = new SimpleBooleanProperty();
    public boolean isEnabled() {
        return enabled.get();
    }
    public void setEnabled(boolean value) {
        enabled.set(value);
    }
    public BooleanProperty enabledProperty() {
        return enabled;
    }

    String[] extensions = new String[] { "bmp", "jpg", "jpeg", "png", "tif", "tiff" };
    ArrayList<String> extensionsList = new ArrayList<>();
    
    public FolderWatch(Supervisor supervisor) {
        this.supervisor = supervisor;
        extensionsList.addAll(Arrays.stream(extensions).collect(Collectors.toList()));
        try {
            watchService = FileSystems.getDefault().newWatchService();
        }
        catch (IOException ex) {
            BasicDialogs.ShowException("There was an error starting the folder watch service", ex);
        }
    }   
    
    public void start(File folder) throws IOException {
        stop();
        this.folder = folder;
        key = folder.toPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        //TODO use another executor?
        executor = Executors.newSingleThreadExecutor();
        future = executor.submit(this);
        setEnabled(true);
    }

    public void stop() {
        if (future != null) future.cancel(true);
        if (executor != null) executor.shutdownNow();
        setEnabled(false);
    }
    
    @Override
    public void run() {
        try {
            for (;;) {
                // wait for key to be signalled
                WatchKey key = watchService.take();

                if (this.key != key) {
                    System.err.println("WatchKey not recognized!");
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent<Path> ev = (WatchEvent<Path>)(event);
                    System.out.format("%s: %s\n", ev.kind(), folder.toPath().resolve(ev.context()).toFile().getAbsolutePath());
                    File file = folder.toPath().resolve(ev.context()).toFile();

                    if (file.isFile() && extensionsList.contains(FilenameUtils.getExtension(file.getAbsolutePath()).toLowerCase())) {
                        Platform.runLater(() -> {
                            Particle particle = new Particle(
                                    file,
                                    Project.UNLABELED_CODE, "");
                            supervisor.project.addParticle(particle);
                            if (supervisor.network.isEnabled()) {
                                CNNPredictionService cnnPredictionService = new CNNPredictionService(supervisor);
                                ClassificationSet result = cnnPredictionService.predict(particle, supervisor.project.processingInfo.getProcessBeforeClassification());
                                supervisor.project.setParticleLabelSet(particle, result);
                            }
                        });
                    }
                }

                // reset key
                if (!key.reset()) {
                    break;
                }
            }
        } catch (InterruptedException x) {
            return;
        }
    }
}
