/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.services;

import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.project.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 * 
 * Remember to add if (this.isCancelled()) return null; to all services!
 * Throw exceptions so they can be dealt with where the service runs.
 */
public class ProjectService {

    
    /*
    Calculate morphology for a list of particles
    */
//    public static Service calculateMorphology(List<Particle> particles, Supervisor supervisor, boolean all) {
//
//        //Create service
//        Service<Void> service = new Service<Void>() {
//            @Override
//            protected Task<Void> createTask() {
//                return new Task<Void>() {
//                    @Override
//                    protected Void call() throws InterruptedException {
//                        updateMessage("Calculating...");
//                        int idx = 0;
//                        int skippedBecauseOfErrors = 0;
//                        ProcessedImage processedImage = new ProcessedImage();
//                        for (Particle foram : particles) {
//                            try {
//                                if (all == true || foram.morphologyStateProperty.get() == false) {
//                                    foram.calculateMorphology(processedImage, supervisor.project.processingInfo);
//                                }
//                            }
//                            catch (ProcessingException ex) {
//                                skippedBecauseOfErrors++;
//                            }
//                            updateProgress(idx, particles.size());
//                            idx++;
//                            updateMessage(String.format("%d/%d morphologies calculated",idx, particles.size()));
//                            if (this.isCancelled()) return null;
//                        }
//                        processedImage.release();
//                        if (skippedBecauseOfErrors > 0) {
//                            updateMessage(String.format("%d skipped because of errors",skippedBecauseOfErrors));
//                        }
//                        else {
//                            //updateMessage("Complete");
//                        }
//                        return null;
//                    }
//                };
//            }
//        };
//        return service;
//    }
    
    /*
    Helper to add images from the add dialog
    */
//    public static Service parseImagesForAdding(List<Particle> particles, Project project, EditParticleMetadataViewController addDialog) {
//
//        Service<ArrayList<Particle>> service = new Service<ArrayList<Particle>>() {
//            @Override
//            protected Task<ArrayList<Particle>> createTask() {
//                return new Task<ArrayList<Particle>>() {
//                    @Override
//                    protected ArrayList<Particle> call() throws InterruptedException {
//                        updateMessage("Adding images...");
//                        ArrayList<Particle> toAdd = new ArrayList<>();
//                        //Parse particles
//                        int idx = 0;
//                        int total = particles.size();
//                        for (Particle particle : particles) {
//                            if (this.isCancelled()) return null;
//                            Particle particle = new Particle(file, project.UNLABELED_CODE, "");
//                            //TODO: This logic needs to be somewhere else!!!!!
//                            if (options.extractClassFromFilename) {
//                                HashMap<String,String> parts = addDialog.parseFile(file);
//                                //System.out.println(file.getName());
//                                //Class
//                                if (parts.containsKey("class")) {
//                                    String code = parts.get("class");
//                                    particle.clearAndAdd(code, 1, "from_filename");
//                                }
//                                //Depths
//                                Double depthMin = null;
//                                Double depthMax = null;
//                                if (parts.containsKey("depthMin")) {
//                                    try {
//                                        depthMin =  Double.parseDouble(parts.get("depthMin"));
//                                    }
//                                    catch (Exception ex) {
//                                    }
//                                }
//                                if (parts.containsKey("depthMax")) {
//                                    try {
//                                        depthMax =  Double.parseDouble(parts.get("depthMax"));
//                                    }
//                                    catch (Exception ex) {
//                                    }
//                                }
//                                if (depthMin != null && depthMax == null) depthMax = depthMin;
//                                if (depthMax != null && depthMin == null) depthMin = depthMax;
//                                if (depthMax == null && depthMin == null) {depthMin = 0.0; depthMax = 0.0;}
//                                particle.setIndex1(depthMin);
//                                particle.setIndex2(depthMax);
//                                //CoreID
//                                if (parts.containsKey("coreID")) {
//                                    String id = parts.get("coreID");
//                                    particle.setSampleID(id);
//                                }
//                                //GUID
//                                if (parts.containsKey("GUID")) {
//                                    String GUID = parts.get("GUID");
//                                    particle.setGUID(GUID);
//                                }
//                            }
//                            toAdd.add(particle);
//                            idx++;
//                            //if(idx == 200) throw new RuntimeException();
//                            if (idx % 10 == 0 || idx == total) {
//                                updateMessage(String.format("%d/%d images added",idx,total));
//                                updateProgress(idx, total);
//                            }
//                        }
//                        //updateMessage(String.format("Adding complete\n%d images processed",idx));
//                        return toAdd;
//                    }
//                };
//            }
//        };
//        return service;
//    }

    public static Service<ArrayList<Particle>> addImagesToProject(List<File> files, Project project, int selectionSize) {
        Service<ArrayList<Particle>> service = new Service<ArrayList<Particle>>() {
            @Override
            protected Task<ArrayList<Particle>> createTask() {
                return new Task<ArrayList<Particle>>() {
                    @Override
                    protected ArrayList<Particle> call() throws InterruptedException {
                        int size = selectionSize;
                        List<File> selection = new ArrayList<>();
                        selection.addAll(files);
                        updateMessage("Adding images...");
                        if (size > 0) {
                            updateMessage("Randomly selecting images...");
                            if (size > selection.size()) size = selection.size();
                            Collections.shuffle(selection);
                            selection = selection.subList(0, size);
                        }
                        ArrayList<Particle> toAdd = new ArrayList<>();
                        //Parse files
                        int idx = 0;
                        int total = selection.size();
                        for (File file : selection) {
                            if (this.isCancelled()) return null;
                            Particle particle = new Particle(file, project.UNLABELED_CODE, "");
                            toAdd.add(particle);
                            idx++;
                            //if(idx == 200) throw new RuntimeException();
                            if (idx % 10 == 0 || idx == total) {
                                updateMessage(String.format("%d/%d images added",idx,total));
                                updateProgress(idx, total);
                            }
                        }
                        return toAdd;
                    }
                };
            }
        };
        return service;
    }

    public static Service<ArrayList<Particle>> addImagesToProject(LinkedHashMap<String,LinkedHashMap<String, String>> files, Project project, int selectionSize) {
        Service<ArrayList<Particle>> service = new Service<ArrayList<Particle>>() {
            @Override
            protected Task<ArrayList<Particle>> createTask() {
                return new Task<ArrayList<Particle>>() {
                    @Override
                    protected ArrayList<Particle> call() throws InterruptedException {
                        int size = selectionSize;
                        List<String> selection = files.keySet().stream().collect(Collectors.toList());
//                        selection.addAll(files);
                        updateMessage("Adding images...");
                        if (size > 0) {
                            updateMessage("Randomly selecting images...");
                            if (size > selection.size()) size = selection.size();
                            Collections.shuffle(selection);
                            selection = selection.subList(0, size);
                        }
                        ArrayList<Particle> toAdd = new ArrayList<>();
                        //Parse files
                        int idx = 0;
                        int total = selection.size();
                        for (String key : selection) {
                            if (this.isCancelled()) return null;
                            LinkedHashMap<String, String> data = files.get(key);
                            String sample = Project.UNKNOWN_SAMPLE;
                            String code = Project.UNLABELED_CODE;
                            String classifierID = "from_csv";
                            double score = 1.0;
                            double resolution = 0.0;
                            //Sample
                            if (data.containsKey("sample")) {
                                sample = data.get("sample");
                            }
                            //Class
                            if (data.containsKey("class")) {
                                code = data.get("class");
                            }
                            else if (data.containsKey("label")) {
                                code = data.get("label");
                            }
                            //Classifier
                            if (data.containsKey("classifier")) {
                                classifierID = data.get("classifier");
                            }
                            //Score
                            if (data.containsKey("score")) {
                                try {
                                    score = Double.parseDouble(data.get("score"));
                                }
                                catch (NumberFormatException ex) {

                                }
                            }
                            //Resolution
                            if (data.containsKey("resolution")) {
                                try {
                                    resolution = Double.parseDouble(data.get("resolution"));
                                }
                                catch (NumberFormatException ex) {

                                }
                            }
                            Particle particle = new Particle(new File(key),
                                    code,
                                    classifierID,
                                    score,
                                    sample,
                                    resolution);
                            toAdd.add(particle);
                            idx++;
                            //if(idx == 200) throw new RuntimeException();
                            if (idx % 10 == 0 || idx == total) {
                                updateMessage(String.format("%d/%d images added",idx,total));
                                updateProgress(idx, total);
                            }
                        }
                        return toAdd;
                    }
                };
            }
        };
        return service;
    }
    
    /*
    Remove truncated image
    */
//    public static Service removeTruncated(List<Particle> particles) {
//        Service<ArrayList<Particle>> service = new Service<ArrayList<Particle>>() {
//            @Override
//            protected Task<ArrayList<Particle>> createTask() {
//                return new Task<ArrayList<Particle>>() {
//                    @Override
//                    protected ArrayList<Particle> call() throws InterruptedException {
//                        int idx = 0;
//                        updateMessage("Tagging truncated...");
//                        ArrayList<Particle> toRemove = new ArrayList<>();
//                        for (Particle foram : particles) {
//                            Mat imageMat = foram.getMat();
//                            double ratio = (double) imageMat.cols() / imageMat.rows();
//                            if (foram.tags.contains("trunc")
//                                    || ratio > 1.1 || ratio < 0.9) {
//                                toRemove.add(foram);
//                            }
//                            updateProgress(idx, particles.size());
//                            idx++;
//                            updateMessage(String.format("%d/%d images processed\n%d tagged",idx, particles.size(), toRemove.size()));
//                            if (this.isCancelled()) return null;
//                        }
//                        return toRemove;
//                    }
//                };
//            }
//        };
//        return service;
//    }
}
