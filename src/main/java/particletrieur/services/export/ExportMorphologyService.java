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

    public static Service exportToCSV(
            List<Particle> particles,
            Supervisor supervisor,
            File file,
            boolean exportParameters,
            boolean exportMorpohology) {

        ImageProcessingService imageProcessingService = new ImageProcessingService();
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
                        //Create headers
                        String headerString = "id,filename,label,tags,sample,index1,index2,resolution,GUID,labeled_by,validated_by";
                        LinkedHashSet<String> parameterHeaders = new LinkedHashSet<>();
                        if (exportParameters) {
                            particles.forEach(particle -> {
                                for (Map.Entry<String, String> entry : particle.parameters.entrySet()) {
                                    parameterHeaders.add(entry.getKey());
                                }
                            });
                            for (String header : parameterHeaders) {
                                headerString += ",p_" + header;
                            }
                        }
                        if (exportMorpohology) {
                            headerString += "," + Morphology.getHeaderStringForCSV();
                        }
                        headerString += "\n";

                        // Calculate morphology if needed
                        LinkedHashMap<Particle, Morphology> morphologies = new LinkedHashMap<>();
                        if (exportMorpohology) {
//                            LinkedHashMap<Particle, Integer> indices = new LinkedHashMap<>();

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
//                                            indices.put(foram, idx.intValue() + 1);
                                            image.release();
                                        }
                                    } catch (Exception ex) {
                                        System.out.println("Error calculating morphology for image " + foram.getFilename());
                                        ex.printStackTrace();
                                        skippedBecauseOfErrors.getAndIncrement();
                                    }
                                    idx.getAndIncrement();
                                    updateMessage(String.format("%d/%d morphology calculated\n%d skipped because of errors",
                                            idx.get(), particles.size(), skippedBecauseOfErrors.get()));
                                    updateProgress(idx.get(), particles.size());
                                }
                            });
                        }

                        // Write to CSV
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
                        writer.write(headerString);

                        int idx2 = 0;
                        for (Particle particle : particles) {
                            if (this.isCancelled()) return null;
                            // Default
                            writer.write(String.format("%d,%s,%s,%s,%s,%f,%f,%f,%s,%s,%s",
                                    idx2 + 1,
                                    particle.getFile().getAbsolutePath(),
                                    particle.getClassification(),
                                    particle.tagsToString(),
                                    particle.getSampleID(),
                                    particle.getIndex1(),
                                    particle.getIndex2(),
                                    particle.getResolution(),
                                    particle.getGUID(),
                                    particle.classifierIdProperty.get(),
                                    particle.getValidator()));
                            // Parameters
                            if (exportParameters) {
                                for (String header : parameterHeaders) {
                                    String value = particle.parameters.getOrDefault(header, "");
                                    writer.write("," + value);
                                }
                            }
                            if (exportMorpohology) {
                                Morphology morphology = null;
                                String morphologyCSV;
                                if (morphologies.containsKey(particle)) morphology = morphologies.get(particle);
                                if (morphology == null) {
                                    morphologyCSV = "error";
                                } else {
                                    morphologyCSV = morphology.toStringCSV(particle.getResolution());
                                }
                                writer.write("," + morphologyCSV);
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





    public static Service exportMorphologyToCSV(
            List<Particle> particles,
            Supervisor supervisor,
            File file) {

        ImageProcessingService imageProcessingService = new ImageProcessingService();
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
                                    System.out.println("Error calculating morphology for image " + foram.getFilename());
                                    ex.printStackTrace();
                                    skippedBecauseOfErrors.getAndIncrement();
                                }
                                idx.getAndIncrement();
                                updateMessage(String.format("%d/%d morphology calculated\n%d skipped because of errors",
                                        idx.get(), particles.size(), skippedBecauseOfErrors.get()));
                                updateProgress(idx.get(), particles.size());
                            }
                        });

                        //Output to file
                        String headerString = "id,image,label,tags,sample,index1,index2,GUID,heightPX,widthPX," +
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
                            writer.write(String.format("%d,%s,%s,%s,%s,%f,%f,%s,%d,%d,",
                                    indices.get(particle),
                                    particle.getFile().getAbsolutePath(),
                                    particle.classification.get(),
                                    particle.tagsToString(),
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

//                        final AtomicInteger skippedBecauseOfErrors = new AtomicInteger(0);
//                        final AtomicInteger idx = new AtomicInteger(0);
//
//                        LinkedHashMap<Particle, Morphology> morphologies = new LinkedHashMap<>();
//                        LinkedHashMap<Particle, Integer> indices = new LinkedHashMap<>();

                        //Get headers
                        LinkedHashSet<String> headers = new LinkedHashSet<>();
                        particles.forEach(particle -> {
                            for (Map.Entry<String, String> entry : particle.parameters.entrySet()) {
                                headers.add(entry.getKey());
                            }
                        });

                        //Output to file
                        String headerString = "id,filename,label,tags,sample,index1,index2,resolution,GUID,labeled_by,validated_by";
                        for (String header : headers) {
                            headerString += ",p_" + header;
                        }
                        headerString += "\n";
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
                        writer.write(headerString);
                        int idx2 = 0;
                        for (Particle particle : particles) {
                            if (this.isCancelled()) return null;
                            writer.write(String.format("%d,%s,%s,%s,%s,%f,%f,%f,%s,%s,%s",
                                    idx2 + 1,
                                    particle.getFile().getAbsolutePath(),
                                    particle.getClassification(),
                                    particle.tagsToString(),
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
