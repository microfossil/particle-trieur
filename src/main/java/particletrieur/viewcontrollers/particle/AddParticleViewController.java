/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.particle;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.scene.control.cell.CheckBoxTreeCell;
import org.controlsfx.control.CheckTreeView;
import particletrieur.*;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.controls.dialogs.ProgressDialog2;
import particletrieur.services.ParametersFromCSVService;
import particletrieur.viewmanagers.UndoManager;
import particletrieur.viewmanagers.commands.AddParticlesCommand;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Particle;
import particletrieur.services.ProjectService;
import particletrieur.viewmodels.SelectionViewModel;
import com.google.inject.Inject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("/views/particle/AddParticleView.fxml")
public class AddParticleViewController extends AbstractDialogController implements Initializable {

    @FXML
    CheckTreeView<ProjectService.DisplayPath> treeView;
    @FXML
    RadioButton radioButtonFiles;
    @FXML
    RadioButton radioButtonCSV;
    @FXML
    CheckBox checkBoxRandom;
    @FXML
    TextField textFieldRandom;
    @FXML
    Button buttonChooseFolder;
    @FXML
    RadioButton radioButtonFolder;
    @FXML
    ToggleGroup addType;
    @FXML
    Label labelFileCount;
    @FXML
    GridView gridViewImages;

    ObservableList<File> files = FXCollections.observableArrayList();
    LinkedHashMap<String, LinkedHashMap<String, String>> csvData = new LinkedHashMap<>();

    private boolean withFiles = false;

    @Inject
    Supervisor supervisor;
    @Inject
    UndoManager undoManager;

    public AddParticleViewController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        radioButtonFolder.setSelected(true);

        treeView.setCellFactory(tv -> {
            CheckBoxTreeCell<ProjectService.DisplayPath> cell = new CheckBoxTreeCell<ProjectService.DisplayPath>() {
                @Override
                public void updateItem(ProjectService.DisplayPath item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.displayPath);
                    }
                }
            };
            return cell ;
        });

        files.addListener((ListChangeListener<? super File>) listener -> {
            LinkedHashMap<String, Object> list = new LinkedHashMap<>();
            int i = 0;
            for (File file : files) {
                String [] parts = file.getAbsolutePath().split(Matcher.quoteReplacement(System.getProperty("file.separator")));
                System.out.println(i);
                ProjectService.createDirectoryTree(file.getAbsolutePath(), parts, 0, list);
                i++;
            }
            CheckBoxTreeItem<ProjectService.DisplayPath> root = new CheckBoxTreeItem<>(new ProjectService.DisplayPath("" ,""));
            ProjectService.createTreeView(list, root, withFiles);
            root.setSelected(true);
            treeView.setRoot(root);
            treeView.getRoot().setExpanded(true);
        });
    }

    private void updateFromFileSelection(List<File> selection) {
        files.clear();
        files.addAll(selection);
        this.getDialog().getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
    }

    private void findCheckedItems(CheckBoxTreeItem<ProjectService.DisplayPath> item, List<CheckBoxTreeItem<ProjectService.DisplayPath>> checkedItems) {
        if (item.getChildren().size() == 0 && item.isSelected()) {
            checkedItems.add(item);
        }
        for (TreeItem<?> child : item.getChildren()) {
            findCheckedItems((CheckBoxTreeItem<ProjectService.DisplayPath>) child, checkedItems);
        }
    }

    private List<File> getSelectedFiles() {
        if (withFiles) {
            ArrayList<CheckBoxTreeItem<ProjectService.DisplayPath>> checkedItems = new ArrayList<>();
            findCheckedItems((CheckBoxTreeItem<ProjectService.DisplayPath>) treeView.getRoot(), checkedItems);
            checkedItems.stream().filter(c -> !c.getValue().path.equals("")).map(c -> new File(c.getValue().path)).forEach(c -> System.out.println(c));
            return checkedItems.stream().filter(c -> !c.getValue().path.equals("")).map(c -> new File(c.getValue().path)).collect(Collectors.toList());
        }
        else {
            ArrayList<CheckBoxTreeItem<ProjectService.DisplayPath>> checkedItems = new ArrayList<>();
            findCheckedItems((CheckBoxTreeItem<ProjectService.DisplayPath>) treeView.getRoot(), checkedItems);
            Set<String> parents = checkedItems.stream().map(c -> c.getValue().path).collect(Collectors.toSet());
            return files.stream().filter(f -> parents.contains(f.getParent())).collect(Collectors.toList());
        }
    }

    @FXML
    private void handleChooseFiles(ActionEvent event) {
        if (addType.getSelectedToggle() == radioButtonFiles) {
            FileChooser dc = new FileChooser();
            dc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images (bmp|png|jpg|tif)",
                    "*.bmp", "*.jpg", "*.jpeg", "*.png", "*.tif", "*.tiff",
                    "*.BMP", "*.JPG", "*.JPEG", "*.PNG", "*.TIF", "*.TIFF"));
            String path = App.getPrefs().getImagePath();
            if (path != null && Files.exists(Paths.get(path))) {
                dc.setInitialDirectory(new File(path));
            }
            dc.setTitle("Select images to add");
            files.clear();
            csvData.clear();
            withFiles = true;
            List<File> dcFiles = dc.showOpenMultipleDialog(buttonChooseFolder.getScene().getWindow());
            updateFromFileSelection(dcFiles);
            if (files == null || files.isEmpty()) return;
            App.getPrefs().setImagePath(files.get(0).getParent());
            App.getPrefs().save();
        }
        else if (addType.getSelectedToggle() == radioButtonFolder) {
            DirectoryChooser dc = new DirectoryChooser();
            String path = App.getPrefs().getImagePath();
            if (path != null && Files.exists(Paths.get(path))) {
                dc.setInitialDirectory(new File(path));
            }
            dc.setTitle("Select folder of images to add");
            File dir = dc.showDialog(buttonChooseFolder.getScene().getWindow());
            if (dir == null) return;
            App.getPrefs().setImagePath(dir.getAbsolutePath());
            App.getPrefs().save();
            Service<List<File>> service = new Service<List<File>>() {
                @Override
                protected Task<List<File>> createTask() {
                    return new Task<List<File>>() {
                        @Override
                        protected List<File> call() throws Exception {
                            final AtomicInteger count = new AtomicInteger(0);
                            List<File> filesToAdd = Files.find(Paths.get(dir.getAbsolutePath()), Integer.MAX_VALUE, (filePath, fileAttr) -> {
                                if (fileAttr.isRegularFile()) {
                                    String extension = FilenameUtils.getExtension(filePath.toString());
                                    if (extension.toLowerCase().endsWith("bmp") ||
                                            extension.toLowerCase().endsWith("png") ||
                                            extension.toLowerCase().endsWith("tiff") ||
                                            extension.toLowerCase().endsWith("tif") ||
                                            extension.toLowerCase().endsWith("jpg") ||
                                            extension.toLowerCase().endsWith("jpeg")) {
                                        updateMessage(String.format("%d images found", count.incrementAndGet()));
                                        return true;
                                    }
                                }
                                return false;
                            }).sorted().map(Path::toFile).collect(Collectors.toList());
                            return filesToAdd;
                        }
                    };
                }
            };
            service.setOnSucceeded(event1 -> {
                withFiles = false;
                updateFromFileSelection(service.getValue());
            });
            service.setOnFailed(event1 -> {
                BasicDialogs.ShowException("Error loading files", new Exception(service.getException()));
                this.getDialog().getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
            });
            service.messageProperty().addListener((observable, oldValue, newValue) -> {
                labelFileCount.setText(newValue);
            });
            this.getDialog().getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
            service.start();
        }
        if (addType.getSelectedToggle() == radioButtonCSV) {
            FileChooser dc = new FileChooser();
            dc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (csv)","*.csv", "*.CSV"));
            String path = App.getPrefs().getImagePath();
            if (path != null && Files.exists(Paths.get(path))) {
                dc.setInitialDirectory(new File(path));
            }
            dc.setTitle("Select CSV file with image data");
            File file = dc.showOpenDialog(buttonChooseFolder.getScene().getWindow());
            if (file == null) return;
            App.getPrefs().setImagePath(file.getParent());
            App.getPrefs().save();
            Service<LinkedHashMap<String, LinkedHashMap<String, String>>> service = ParametersFromCSVService.getParametersFromCSV(file);
            service.setOnSucceeded(event1 -> {
                withFiles = false;
                csvData = service.getValue();
                List<File> toAdd = csvData.keySet().stream().map(File::new).collect(Collectors.toList());
                updateFromFileSelection(toAdd);
            });
            service.setOnFailed(event1 -> {
                BasicDialogs.ShowException("Error loading CSV", new Exception(service.getException()));
                this.getDialog().getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
            });
            service.messageProperty().addListener((observable, oldValue, newValue) -> {
                labelFileCount.setText(newValue);
            });
            this.getDialog().getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
            service.start();
        }

        if (files.size() == 0) {
            labelFileCount.setText("0 files found");
        } else {
            labelFileCount.setText(String.format("%d files found, e.g: %s", files.size(), files.get(0).getName()));
        }
    }

    public class ParticleCell extends GridCell<File> {

        //Loading task
        ObjectProperty<Task<Image>> loadingTask = new SimpleObjectProperty<>();

        ImageView imageView;

        public ParticleCell() {
            imageView = new ImageView();
            imageView.fitHeightProperty().bind(heightProperty());
            imageView.fitWidthProperty().bind(widthProperty());
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
        }

        @Override
        protected void updateItem(File item, boolean empty) {
            //Stop already running image fetch tast
            if (loadingTask.get() != null &&
                    loadingTask.get().getState() != Worker.State.SUCCEEDED &&
                    loadingTask.get().getState() != Worker.State.FAILED) {

                loadingTask.get().cancel();
            }
            loadingTask.set(null);

            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                Task<Image> task = new Task<Image>() {
                    @Override
                    public Image call() throws Exception {
                        return SwingFXUtils.toFXImage(ImageIO.read(item), null);
                    }
                };
                loadingTask.set(task);
                task.setOnSucceeded(event -> {
                    imageView.setImage(task.getValue());
                    setGraphic(imageView);
                });
                task.setOnFailed(event -> {
                    setText("Error");
                    System.out.println((new Exception(task.getException())).getMessage());
                });
                App.getExecutorService().submit(task);
            }
        }
    }

    boolean twice = false;

    @Override
    public void processDialogResult(ButtonType buttonType) {
        if (buttonType == ButtonType.OK) {
            twice = false;
            if (files == null || files.size() == 0) return;
            int selectionSize = -1;
            if (checkBoxRandom.isSelected()) {
                try {
                    selectionSize = Integer.parseInt(textFieldRandom.getText());
                } catch (NumberFormatException ex) {

                }
            }
            Service<ArrayList<Particle>> service;
            if (radioButtonCSV.isSelected()) {
                service = ProjectService.addImagesToProject(getSelectedFiles(), csvData, supervisor.project, selectionSize);
            }
            else {
                service = ProjectService.addImagesToProject(getSelectedFiles(), supervisor.project, selectionSize);
            }
            service.setOnSucceeded(succeeded -> {
                AddParticlesCommand command = new AddParticlesCommand(supervisor.project, service.getValue());
                command.apply();
                undoManager.add(command);
            });
            ProgressDialog2 dialog = BasicDialogs.ProgressDialogWithCancel2(
                    "Operation",
                    "Adding files",
                    service);
            dialog.setResultConverter(value -> {
                if (value == ButtonType.CLOSE) {
                    if (!twice) {
                        try {
                            EditParticleMetadataViewController controller = AbstractDialogController.create(EditParticleMetadataViewController.class);
                            controller.setup(service.getValue());
                            controller.showEmbedded();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    twice = true;
                }
                return null;
            });
            dialog.start();
        }
    }

    @Override
    public String getHeader() {
        return "Add Images";
    }

    @Override
    public String getSymbol() {
        return "featherplus";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[]{ButtonType.OK, ButtonType.CANCEL};
    }
}
