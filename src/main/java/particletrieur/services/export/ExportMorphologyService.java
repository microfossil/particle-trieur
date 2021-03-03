package particletrieur.services.export;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import particletrieur.models.Supervisor;
import particletrieur.models.processing.Morphology;
import particletrieur.models.processing.ParticleImage;
import particletrieur.models.processing.ProcessingInfo;
import particletrieur.models.processing.processors.MorphologyProcessor;
import particletrieur.models.project.Particle;
import particletrieur.models.project.Project;
import particletrieur.services.ImageProcessingService;
import org.opencv.core.Mat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ExportMorphologyService {

    public static Service exportMorphologyToCSV(
            List<Particle> particles,
            Supervisor supervisor,
            File file) {

        ImageProcessingService imageProcessingService = new ImageProcessingService(supervisor.FCNNSegmenterService);
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
                                } catch (Exception ex) {
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
                        for (Map.Entry<Particle, Morphology> entry : morphologies.entrySet()) {
                            if (this.isCancelled()) return null;
                            Particle particle = entry.getKey();
                            Morphology morphology = entry.getValue();
                            String morphologyCSV;
                            if (morphology == null) {
                                morphologyCSV = "error";
                            }
                            else {
                                morphologyCSV = morphology.toStringCSV(particle.getResolution());
                            }
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
                                    + morphologyCSV + '\n');
                            idx2++;
                            updateMessage(String.format("%d/%d records exported", idx2, particles.size()));
                            updateProgress(idx2, particles.size());
                        }
                        writer.flush();
                        writer.close();
                        return null;
                    }
                };
            }
        };
        return service;
    }

    public static Service exportInformationToCSV(
            List<Particle> particles,
            Supervisor supervisor,
            File file) {


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

                        //Get headers
                        HashSet<String> headerSet = new HashSet<>();
                        particles.forEach(particle -> {
                            for (Map.Entry<String, String> entry : particle.parameters.entrySet()) {
                                headerSet.add(entry.getKey());
                            }
                        });
                        List<String> headers = headerSet.stream().sorted().collect(Collectors.toList());

                        //Output to file
                        String headerString = "id,filename,label,sample,index1,index2,resolution,GUID,labeled_by,validated_by";
                        for (String header : headers) {
                            headerString += "," + header;
                        }
                        headerString += "\n";
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
                        writer.write(headerString);
                        int idx2 = 0;
                        for (Particle particle : particles) {
                            if (this.isCancelled()) return null;
                            writer.write(String.format("%d,%s,%s,%s,%f,%f,%f,%s,%s,%s",
                                    idx2 + 1,
                                    particle.getFile().getAbsolutePath(),
                                    particle.getClassification(),
                                    particle.getSampleID(),
                                    particle.getIndex1(),
                                    particle.getIndex2(),
                                    particle.getResolution(),
                                    particle.getGUID(),
                                    particle.classifierIdProperty.get(),
                                    particle.getValidator()));
                            for (String header : headers) {
                                String value = particle.parameters.getOrDefault(header,"");
                                writer.write("," + value);
                            }
                            writer.write("\n");
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
