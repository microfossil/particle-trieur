/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.services.export;

import particletrieur.models.processing.processors.BorderRemovalProcessor;
import particletrieur.models.project.Particle;
import particletrieur.services.ImageProcessingService;
import particletrieur.models.Supervisor;
import particletrieur.models.processing.*;
import particletrieur.models.ProjectRepository;
import particletrieur.models.project.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ExportImagesService {

    public boolean performPreprocessing = false;
    public boolean useOriginalFilenames = true;
    public boolean includePrefix = false;
    public String prefix = "";
    public boolean includeClassification = false;
    public boolean includeIndex1 = false;
    public boolean includeIndex2 = false;
    public boolean includeSample = false;
    public boolean includeGUID = false;
    public FolderMode folderMode = FolderMode.NONE;
    public List<String> taxonCodes;
    public List<String> tagCodes;
    public File outputDirectory;
    public ImageFormat conversionFormat = ImageFormat.SAME;
    public boolean performResize = true;
    public int resizeLength = -1;

    public enum FolderMode {
        NONE("None"),
        CLASS("Label"),
        SAMPLE("Sample"),
        INDEX1("Index 1"),
        INDEX2("Index 2");

        private String description;

        private FolderMode(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public enum ImageFormat {
        SAME("No conversion", ""),
        JPEG("JPEG (compressed, small file size)", "jpg"),
        PNG("PNG (lossless, medium file size)", "png"),
        TIFF("TIFF (lossless, large file size)", "tif");

        private String description;
        private String extension;

        private ImageFormat(String description, String extension) {
            this.description = description;
            this.extension = extension;
        }

        @Override
        public String toString() {
            return description;
        }

        public String getExtension() {
            return extension;
        }
    }

    Supervisor supervisor;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");


    public ExportImagesService(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    public Service exportImages() {
        Project project = supervisor.project;
        ProcessingInfo proDef = project.processingInfo;

        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        //Lets begin!
                        updateMessage("Saving export project file...");

                        //Deep copy of app using XML
                        ProjectRepository projectRepository = new ProjectRepository(project);
                        Project exportProject = projectRepository.cloneByXML();
                        exportProject.setFile(Paths.get(outputDirectory.getAbsolutePath(), "project_exported.xml").toFile());

                        //If processing, remove vectors (they will not be correct otherwise)
                        if (performPreprocessing) {
                            exportProject.particles.stream().forEach(p -> exportProject.setParticleCNNVector(p, null));
                            exportProject.processingInfo.resetToDefaults();
                        }

                        //Create a sub-directory with the current date time
                        String directoryName = "images_" + LocalDateTime.now().format(dateTimeFormatter);
                        File imagesDirectory = Paths.get(outputDirectory.getAbsolutePath(), directoryName).toFile();
                        if (!imagesDirectory.exists()) {
                            imagesDirectory.mkdir();
                        }

                        //Save all the images
                        ConcurrentLinkedQueue<Particle> toRemove = new ConcurrentLinkedQueue<>();

                        //Create a format string for the image number based on the number of digits needed
                        String numberFormat = String.format("%%0%.0fd", Math.ceil(Math.log10(project.particles.size())));

                        final AtomicInteger processingErrorCount = new AtomicInteger(0);
                        final AtomicInteger fileNotFoundCount = new AtomicInteger(0);
                        final AtomicInteger notInTaxonTagListCount = new AtomicInteger(0);
                        final AtomicInteger idx = new AtomicInteger(0);

                        //
                        // Main loop to save images
                        //
                        HashMap<String, Integer> filenames = new LinkedHashMap<>();
                        exportProject.particles.stream().forEach(particle -> {
                            //Exit the loop if the service has been cancelled
                            if (!this.isCancelled()) {
                                //The index of the image
                                idx.getAndIncrement();

                                //If particle image file doesn't exist, it will be removed from the exported app
                                if (particle.getFile() == null) particle.refreshFile();
                                if (!particle.getFile().exists()) {
                                    toRemove.add(particle);
                                    fileNotFoundCount.incrementAndGet();
                                }
                                //Skip if not in list of codes to export
                                else if (!taxonCodes.contains(particle.classification.get()) ||
                                        !Collections.disjoint(tagCodes, particle.tags)) {
                                    toRemove.add(particle);
                                    notInTaxonTagListCount.incrementAndGet();
                                }
                                //Process each file
                                else {
                                    //Create the output directory
                                    File currentOutputDirectory = new File(imagesDirectory.getAbsolutePath());
                                    if (folderMode != FolderMode.NONE) {
                                        String folder = null;
                                        if (folderMode == FolderMode.CLASS) {
                                            folder = particle.classification.get();
                                        }
                                        else if (folderMode == FolderMode.INDEX1) {
                                            folder = String.format("%f", particle.getIndex1()).replace('.', 'p');
                                        }
                                        else if (folderMode == FolderMode.INDEX2) {
                                            folder = String.format("%f", particle.getIndex2()).replace('.', 'p');
                                        }
                                        else if (folderMode == FolderMode.SAMPLE) {
                                            folder = particle.getSampleID();
                                        }
                                        currentOutputDirectory = Paths.get(imagesDirectory.getAbsolutePath(), folder).toFile();
                                        if (!currentOutputDirectory.exists()) {
                                            currentOutputDirectory.mkdir();
                                        }
                                    }

                                    //Build filename
                                    StringBuilder sb = new StringBuilder(currentOutputDirectory.getAbsolutePath());
                                    sb.append(File.separator);
                                    if (useOriginalFilenames) {
                                        String filename = FilenameUtils.removeExtension(particle.getShortFilename());
                                        if (filenames.containsKey(filename)) {
                                            int index = filenames.get(filename) + 1;
                                            filenames.put(filename, index);
                                            sb.append(String.format("%s_%03d", filename, index));
                                        }
                                        else {
                                            filenames.put(filename, 0);
                                            sb.append(filename);
                                        }
                                    }
                                    else {
                                        //Prefix
                                        if (includePrefix) {
                                            sb.append(prefix);
                                            sb.append("-");
                                        }
                                        sb.append(String.format(numberFormat, idx.get()));
                                        if (includeClassification) {
                                            sb.append("-");
                                            sb.append(particle.classification.get());
                                        }
                                        if (includeSample && particle.getSampleID() != null) {
                                            sb.append("-");
                                            sb.append(particle.getSampleID());
                                        }
                                        if (includeIndex1) {
                                            sb.append("-");
                                            sb.append(String.format("%f", particle.getIndex1()).replace('.', 'p'));
                                        }
                                        if (includeIndex2) {
                                            sb.append("-");
                                            sb.append(String.format("%f", particle.getIndex2()).replace('.', 'p'));
                                        }
                                        if (includeGUID && particle.getGUID() != null && !particle.getGUID().equalsIgnoreCase("")) {
                                            sb.append("-");
                                            sb.append(particle.getGUID());
                                        }
                                    }

                                    //Conversion?
                                    String extension;
                                    if (conversionFormat == ImageFormat.SAME) {
                                        extension = FilenameUtils.getExtension(particle.getFilename());
                                    } else {
                                        extension = conversionFormat.getExtension();
                                    }
                                    sb.append(".");
                                    sb.append(extension);

                                    //Processing
                                    if (performPreprocessing) {
                                        ImageProcessingService imageProcessingService = new ImageProcessingService(supervisor.FCNNSegmenterService);
                                        ParticleImage image = null;
                                        Mat mat = new Mat();
                                        if (mat != null) {
                                            try {
                                                Mat temp = particle.getMat();
                                                image = imageProcessingService.process(temp, proDef);
                                                temp.release();
                                                if (performResize && resizeLength > 0)
                                                    image.resizeByMaximumDimension(resizeLength);
                                                mat = image.forSaving(255);
                                                writeMat(mat, sb.toString(), conversionFormat);
                                            } catch (Exception ex) {

                                            } finally {
                                                if (image != null) image.release();
                                                mat.release();
                                            }
                                        }
                                    }
                                    //No processing
                                    else {
                                        if (conversionFormat == ImageFormat.SAME && !(performResize && resizeLength > 0)) {
                                            try {
                                                FileUtils.copyFile(particle.getFile(), new File(sb.toString()));
                                            } catch (IOException e) {
                                                toRemove.add(particle);
                                                processingErrorCount.incrementAndGet();
                                                e.printStackTrace();
                                            }
                                        }
                                        else if (performResize && resizeLength > 0) {
                                            Mat mat = new Mat();
                                            if (mat != null) {
                                                try {
                                                    mat = particle.getMat();
                                                    BorderRemovalProcessor.resizeByMaximumDimension(mat, resizeLength);
                                                    writeMat(mat, sb.toString(), conversionFormat);
                                                } catch (Exception e) {
                                                    toRemove.add(particle);
                                                    processingErrorCount.incrementAndGet();
                                                    e.printStackTrace();
                                                } finally {
                                                    mat.release();
                                                }
                                            }
                                            else {
                                                toRemove.add(particle);
                                                processingErrorCount.incrementAndGet();
                                            }
                                        }
                                        else {
                                            Mat mat = particle.getMat();
                                            if (mat != null) {
                                                writeMat(mat, sb.toString(), conversionFormat);
                                                mat.release();
                                            }
                                        }
                                    }
//                                    System.out.println(sb.toString());
                                    exportProject.setParticleFilename(particle, sb.toString());
                                }
                                updateMessage(String.format(
                                        "%d/%d images exported\n%d skipped because of label\n%d file not found\n%d errors",
                                        idx.get() - notInTaxonTagListCount.get() - fileNotFoundCount.get() - processingErrorCount.get(),
                                        project.particles.size(),
                                        notInTaxonTagListCount.get(),
                                        fileNotFoundCount.get(),
                                        processingErrorCount.get()));
                                updateProgress(idx.get(), project.particles.size());
                            }
                        });
                        //Remove any skipped images
                        exportProject.particles.removeAll(toRemove);

                        //Save the results!
                        ProjectRepository exportRepositoryService = new ProjectRepository(exportProject);
                        exportRepositoryService.save();
                        return null;
                    }
                };
            }
        };
        return service;
    }

    private void writeMat(Mat mat, String filename, ImageFormat conversionFormat) {
        String extension = FilenameUtils.getExtension(filename);
        if (conversionFormat == ImageFormat.JPEG || extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
            Imgcodecs.imwrite(filename, mat, new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 90));
        } else {
            Imgcodecs.imwrite(filename, mat);
        }
    }
}
