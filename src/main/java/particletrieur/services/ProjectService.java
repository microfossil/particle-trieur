/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.services;

import javafx.scene.control.CheckBoxTreeItem;
import org.apache.commons.io.FilenameUtils;
import particletrieur.models.project.Particle;
import particletrieur.models.project.Project;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 * <p>
 * Remember to add if (this.isCancelled()) return null; to all services!
 * Throw exceptions so they can be dealt with where the service runs.
 */
public class ProjectService {

    public static void createDirectoryTree(String filename, String[ ] parts, int idx, LinkedHashMap<String, Object> list) {
        // Still at directory level
        if (parts.length > idx + 1) {
            if (list.containsKey(parts[idx])) {
                createDirectoryTree(filename, parts, idx + 1, (LinkedHashMap<String, Object> ) list.get(parts[idx]));
            } else {
                LinkedHashMap<String, Object> newlist = new LinkedHashMap<>();
                list.put(parts[idx], newlist);
                createDirectoryTree(filename, parts, idx + 1, newlist);
                System.out.println(parts[idx]);
            }
        }
        else {
            list.put(parts[idx], filename);
        }
    }

    public static void createTreeView(LinkedHashMap<String, Object> list, CheckBoxTreeItem<String> treeItem) {
        for(Map.Entry<String, Object> entry : list.entrySet()) {
            if (entry.getKey() == null) {
                createTreeView((LinkedHashMap<String, Object>) entry.getValue(), treeItem);
            }
            else if (list.size() == 1) {
                treeItem.setValue(treeItem.getValue() + "/" + entry.getKey());
                createTreeView((LinkedHashMap<String, Object>) entry.getValue(), treeItem);
            }
            else if (entry.getValue() instanceof LinkedHashMap) {
                CheckBoxTreeItem<String> newTreeItem = new CheckBoxTreeItem<>(entry.getKey());
                createTreeView((LinkedHashMap<String, Object>) entry.getValue(), newTreeItem);
                treeItem.getChildren().add(newTreeItem);
            }
            else {
                CheckBoxTreeItem<String> newTreeItem = new CheckBoxTreeItem<>((String) entry.getValue());
                treeItem.getChildren().add(newTreeItem);
            }
        }
    }

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
                                updateMessage(String.format("%d/%d images added", idx, total));
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

    public static Service<ArrayList<Particle>> addImagesToProject(LinkedHashMap<String, LinkedHashMap<String, String>> files, Project project, int selectionSize) {
        Service<ArrayList<Particle>> service = new Service<ArrayList<Particle>>() {
            @Override
            protected Task<ArrayList<Particle>> createTask() {
                return new Task<ArrayList<Particle>>() {
                    @Override
                    protected ArrayList<Particle> call() throws InterruptedException {
                        int size = selectionSize;
                        List<String> selection = files.keySet().stream().collect(Collectors.toList());
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
                            Particle particle = new Particle(new File(key),
                                    code,
                                    classifierID,
                                    score,
                                    sample,
                                    resolution);
                            particle.addParameters(data);
                            toAdd.add(particle);
                            idx++;
                            //if(idx == 200) throw new RuntimeException();
                            if (idx % 10 == 0 || idx == total) {
                                updateMessage(String.format("%d/%d images added", idx, total));
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

    public static Service<ArrayList<Particle>> updateParameters(LinkedHashMap<String, LinkedHashMap<String, String>> files, Project project, int selectionSize) {
        Service<ArrayList<Particle>> service = new Service<ArrayList<Particle>>() {
            @Override
            protected Task<ArrayList<Particle>> createTask() {
                return new Task<ArrayList<Particle>>() {
                    @Override
                    protected ArrayList<Particle> call() throws InterruptedException {
                        int size = selectionSize;
                        List<String> selection = files.keySet().stream().collect(Collectors.toList());
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
                            Particle particle = new Particle(new File(key),
                                    code,
                                    classifierID,
                                    score,
                                    sample,
                                    resolution);
                            particle.addParameters(data);
                            toAdd.add(particle);
                            idx++;
                            //if(idx == 200) throw new RuntimeException();
                            if (idx % 10 == 0 || idx == total) {
                                updateMessage(String.format("%d/%d images added", idx, total));
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
}
