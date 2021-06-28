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

    public static class DisplayPath {
        public String path;
        public String displayPath;

        public DisplayPath(String path, String displayPath) {
            this.path = path;
            this.displayPath = displayPath;
        }
    }

    public static class Directory {
        public String name;
        public String path;
        public ArrayList<File> files = new ArrayList<>();
        public LinkedHashMap<String, Directory> directories = new LinkedHashMap<>();

        public Directory(String name) {
            this.name = name;
        }
    }

    public static Directory filesToTree(List<File> files) {
        Directory root = new Directory("root");
        for (File file : files) {
            Directory currentDir = root;
            String[] parts = file.getAbsolutePath().split(Matcher.quoteReplacement(System.getProperty("file.separator")));
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].equals("")) continue;
                if (currentDir.directories.containsKey(parts[i])) {
                    currentDir = currentDir.directories.get(parts[i]);
                }
                else {
                    Directory newDirectory = new Directory(parts[i]);
                    newDirectory.path = String.join(System.getProperty("file.separator"), Arrays.stream(parts).limit(i+1).collect(Collectors.toList()));
                    currentDir.directories.put(parts[i], newDirectory);
                    currentDir = newDirectory;
                }
            }
            System.out.print(file.getAbsolutePath());
            currentDir.files.add(file);
        }
        return root;
    }

    public static CheckBoxTreeItem<DisplayPath> createTreeView(Directory root, CheckBoxTreeItem<DisplayPath> treeItem, boolean withFiles) {
        if (withFiles) {
            for (File file : root.files) {
                CheckBoxTreeItem<DisplayPath> fileTreeItem = new CheckBoxTreeItem<>(new DisplayPath(file.getAbsolutePath(), file.getName()));
                treeItem.getChildren().add(fileTreeItem);
            }
        }
        for (Map.Entry<String, Directory> entry : root.directories.entrySet()) {
            String name = entry.getKey();
            Directory dir = entry.getValue();
            if (root.directories.size() == 1) {
                treeItem.getValue().displayPath = dir.path;
                return createTreeView(dir, treeItem, withFiles);
            }
            else {
                CheckBoxTreeItem<DisplayPath> dirTreeItem = new CheckBoxTreeItem<>(new DisplayPath(dir.path, name));
                treeItem.getChildren().add(createTreeView(dir, dirTreeItem, withFiles));
            }
        }
        return treeItem;
    }

//
//
//    public static void createDirectoryTree(String filename, String[] parts, int idx, LinkedHashMap<String, Object> list) {
//        // Still at directory level
//        if (parts.length > idx + 1) {
//            if (list.containsKey(parts[idx])) {
//                createDirectoryTree(filename, parts, idx + 1, (LinkedHashMap<String, Object> ) list.get(parts[idx]));
//            } else {
//                LinkedHashMap<String, Object> newlist = new LinkedHashMap<>();
//                list.put(parts[idx], newlist);
//                createDirectoryTree(filename, parts, idx + 1, newlist);
//            }
//        }
//        else {
//            list.put(parts[idx], filename);
//            System.out.println(filename);
//        }
//    }
//
//    public static void createTreeView(LinkedHashMap<String, Object> list, CheckBoxTreeItem<DisplayPath> treeItem, boolean withFiles) {
//        for(Map.Entry<String, Object> entry : list.entrySet()) {
//            if (entry.getKey() == null) {
//                createTreeView((LinkedHashMap<String, Object>) entry.getValue(), treeItem, withFiles);
//            }
//            else if (entry.getValue() instanceof LinkedHashMap && list.size() == 1) {
//                treeItem.setValue(new DisplayPath("",treeItem.getValue().displayPath + "/" + entry.getKey()));
//                createTreeView((LinkedHashMap<String, Object>) entry.getValue(), treeItem, withFiles);
//            }
//            else if (entry.getValue() instanceof LinkedHashMap) {
//                CheckBoxTreeItem<DisplayPath> newTreeItem = new CheckBoxTreeItem<>(new DisplayPath("", entry.getKey()));
//                createTreeView((LinkedHashMap<String, Object>) entry.getValue(), newTreeItem, withFiles);
//                treeItem.getChildren().add(newTreeItem);
//            }
//            else {
//                if (withFiles) {
//                    CheckBoxTreeItem<DisplayPath> newTreeItem = new CheckBoxTreeItem<>(new DisplayPath((String) entry.getValue(), (String) entry.getKey()));
//                    treeItem.getChildren().add(newTreeItem);
//                }
//                treeItem.getValue().path = (new File((String) entry.getValue())).getParent();
////                System.out.println(treeItem.getValue().path);
////                System.out.println((String) entry.getValue());
//            }
//        }
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

    public static Service<ArrayList<Particle>> addImagesToProject(List<File> validFiles,
                                                                  LinkedHashMap<String, LinkedHashMap<String, String>> files,
                                                                  Project project,
                                                                  int selectionSize,
                                                                  boolean overwriteExisting) {
        Service<ArrayList<Particle>> service = new Service<ArrayList<Particle>>() {
            @Override
            protected Task<ArrayList<Particle>> createTask() {
                return new Task<ArrayList<Particle>>() {
                    @Override
                    protected ArrayList<Particle> call() throws InterruptedException {
                        int size = selectionSize;
                        Set<String> validFileKeys = validFiles.stream().map(File::getAbsolutePath).collect(Collectors.toSet());
                        List<String> selection = files.keySet().stream().filter(validFileKeys::contains).collect(Collectors.toList());
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
                            particle.addParameters(data, overwriteExisting);
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
