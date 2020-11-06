/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.viewcontrollers.particle;

import ordervschaos.particletrieur.app.*;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.controls.ProgressDialog2;
import ordervschaos.particletrieur.app.viewmanagers.UndoManager;
import ordervschaos.particletrieur.app.viewmanagers.commands.AddParticlesCommand;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.services.ProjectService;
import ordervschaos.particletrieur.app.viewmodels.SelectionViewModel;
import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.util.stream.Collectors;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("views/particle/AddParticleView.fxml")
public class AddParticleViewController extends AbstractDialogController implements Initializable {

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
    BooleanProperty useFolder = new SimpleBooleanProperty(true);

    @Inject
    Supervisor supervisor;
    @Inject
    SelectionViewModel selectionViewModel;
    @Inject
    UndoManager undoManager;

    public AddParticleViewController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //Folder or file import options
        addType.selectedToggleProperty().addListener(event -> {
            if (addType.getSelectedToggle() == radioButtonFolder) {
                useFolder.set(true);
            } else {
                useFolder.set(false);
            }
        });

        radioButtonFolder.setSelected(true);

        gridViewImages.setCellFactory(param -> new ParticleCell());
        gridViewImages.setItems(files);
    }

    @FXML
    private void handleChooseFiles(ActionEvent event) {

        if (!useFolder.get()) {
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
            List<File> dcFiles = dc.showOpenMultipleDialog(buttonChooseFolder.getScene().getWindow());
            files.addAll(dcFiles);
            if (files == null || files.isEmpty()) return;
            App.getPrefs().setImagePath(files.get(0).getParent());
            App.getPrefs().save();
        } else {
            DirectoryChooser dc = new DirectoryChooser();
            String path = App.getPrefs().getImagePath();
            if (path != null && Files.exists(Paths.get(path))) {
                dc.setInitialDirectory(new File(path));
            }
            dc.setTitle("Select folder of images to add");
//            buttonChooseFolder.setDisable(true);
            File dir = dc.showDialog(buttonChooseFolder.getScene().getWindow());
//            buttonChooseFolder.setDisable(false);
            if (dir == null) return;
            App.getPrefs().setImagePath(dir.getAbsolutePath());
            App.getPrefs().save();
//            try {
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
                files.clear();
                files.addAll(service.getValue());
            });
            service.setOnFailed(event1 -> {
                BasicDialogs.ShowException("Error loading files", new Exception(service.getException()));
            });
            service.messageProperty().addListener((observable, oldValue, newValue) -> {
                labelFileCount.setText(newValue);
            });
            service.start();
//                List<File> filesToAdd = Files.find(Paths.get(dir.getAbsolutePath()), Integer.MAX_VALUE, (filePath, fileAttr) -> {
//                    if (fileAttr.isRegularFile()) {
//                        String extension = FilenameUtils.getExtension(filePath.toString());
//                        if (extension.endsWith("bmp") ||
//                                extension.endsWith("png") ||
//                                extension.endsWith("tiff") ||
//                                extension.endsWith("tif") ||
//                                extension.endsWith("jpg") ||
//                                extension.endsWith("jpeg")) {
//                            return true;
//                        }
//                    }
//                    return false;
//                }).sorted().map(Path::toFile).collect(Collectors.toList());
//                files.clear();
//                files.addAll(filesToAdd);
//            } catch (IOException ex) {
//                BasicDialogs.ShowException("Error loading files", ex);
//            }
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
            Service<ArrayList<Particle>> service = ProjectService.addImagesToProject(files, supervisor.project, selectionSize);
            service.setOnSucceeded(succeeded -> {
//                supervisor.project.addParticles((ArrayList<Particle>) service.getValue());
//                selectionViewModel.setCurrentParticle(service.getValue().get(0));
                AddParticlesCommand command = new AddParticlesCommand(supervisor.project, service.getValue());
                command.apply();
                undoManager.add(command);
            });
            ProgressDialog2 dialog = BasicDialogs.ProgressDialogWithCancel2(
                    "Operation",
                    "Adding files",
                    App.getRootPane(),
                    service);
            dialog.setResultConverter(value -> {
                if (value == ButtonType.CLOSE) {
                    if (!twice) {
                        try {
                            EditParticleMetadataViewController controller = AbstractController.create(EditParticleMetadataViewController.class);
                            controller.setup(service.getValue());
                            controller.showAndWait();
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
