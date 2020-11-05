package ordervschaos.particletrieur.app.services.export;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.processing.Morphology;
import ordervschaos.particletrieur.app.models.processing.ParticleImage;
import ordervschaos.particletrieur.app.models.processing.ProcessingInfo;
import ordervschaos.particletrieur.app.models.processing.processors.MorphologyProcessor;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.project.Project;
import ordervschaos.particletrieur.app.services.ImageProcessingService;
import org.opencv.core.Mat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ExportMorphologyService {

    public static Service exportMorphologyToCSV(
            List<Particle> particles,
            Supervisor supervisor,
            File file) {

        ImageProcessingService imageProcessingService = new ImageProcessingService(supervisor.FCNNSegmenter);
        Project project = supervisor.project;
        ProcessingInfo proDef = project.processingInfo;

        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException, IOException {
                        updateMessage("Exporting...");

                        final AtomicInteger skippedBecauseOfErrors = new AtomicInteger(0);
                        final AtomicInteger idx = new AtomicInteger(0);

                        LinkedHashMap<Particle, Morphology> morphologies = new LinkedHashMap<>();
                        LinkedHashMap<Particle, Integer> indices = new LinkedHashMap<>();

                        //Calculate missing
                        particles.forEach(foram -> {
                            if (!this.isCancelled()) {
                                ParticleImage image = null;
                                try {
                                    Mat mat = foram.getMat();
                                    if (mat != null) {
                                        image = imageProcessingService.process(mat, proDef);
                                        mat.release();
                                        image.morphology = MorphologyProcessor.calculateMorphology(image);
                                        morphologies.put(foram, image.morphology);
                                        indices.put(foram, idx.intValue() + 1);
                                        image.release();
                                    }
                                }
                                catch (Exception ex) {
                                    skippedBecauseOfErrors.getAndIncrement();
                                }
                                idx.getAndIncrement();
                                updateMessage(String.format("%d/%d morphology calculated\n%d skipped because of errors",
                                        idx.get(), particles.size(), skippedBecauseOfErrors.get()));
                                updateProgress(idx.get(), particles.size());
                            }
                        });

                        //Output to file
                        String headerString = "id,image,label,sample,index1,index2,GUID,heightPX,widthPX," +
                                Morphology.getHeaderStringForCSV() + "\n";
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
                        writer.write(headerString);
                        int idx2 = 0;
                        for (Map.Entry<Particle,Morphology> entry : morphologies.entrySet()) {
                            if (this.isCancelled()) return null;
                            Particle particle = entry.getKey();
                            Morphology morphology = entry.getValue();
                            writer.write(String.format("%d,%s,%s,%s,%f,%f,%s,%d,%d,",
                                    indices.get(particle),
                                    particle.getFile().getAbsolutePath(),
                                    particle.classification.get(),
                                    particle.getSampleID(),
                                    particle.getIndex1(),
                                    particle.getIndex2(),
                                    particle.getGUID(),
                                    particle.getImageHeight(),
                                    particle.getImageWidth())
                                    + morphology.toStringCSV(particle.getResolution()) + '\n');
                            idx2++;
                            updateMessage(String.format("%d/%d records exported", idx2, particles.size()));
                            updateProgress(idx2, particles.size());
                        }
                        writer.close();
                        return null;
                    }
                };
            }
        };
        return service;
    }
}
