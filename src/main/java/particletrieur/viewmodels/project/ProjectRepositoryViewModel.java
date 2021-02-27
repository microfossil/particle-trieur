package particletrieur.viewmodels.project;

import particletrieur.App;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.models.ProjectRepository;
import particletrieur.models.Supervisor;
import com.google.inject.Inject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import particletrieur.controls.dialogs.AlertEx;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import particletrieur.viewmanagers.UndoManager;
import particletrieur.viewmodels.MainViewModel;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class ProjectRepositoryViewModel {

    @Inject
    Supervisor supervisor;
    @Inject
    MainViewModel mainViewModel;
    @Inject
    UndoManager undoManager;

    //TODO put operation in progress in App
    public void openProject() {
        mainViewModel.setOperationInProgress(true);
        if (!showIfProjectNeedsSave("Choose whether to save before opening a new app:")) {
            mainViewModel.setOperationInProgress(false);
            return;
        }
        //Select app
        FileChooser fc = new FileChooser();
        String path = App.getPrefs().getProjectPath();
        if (path != null && Files.exists(Paths.get(path))) {
            fc.setInitialDirectory(new File(path));
        }
        fc.setTitle("Choose a project file to open");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project file", "*.xml"));
        File file = fc.showOpenDialog(App.getWindow());
        //Open
        if (file != null) {
            try {
                supervisor.projectRepository.open(file);
                undoManager.clear();
            } catch (Exception ex) {
                BasicDialogs.ShowException("Error opening project", ex);
            }
        }
        mainViewModel.setOperationInProgress(false);
    }

    public void openProject(String filename) {
        mainViewModel.setOperationInProgress(true);
        File file = new File(filename);
        //Open
        if (file.exists()) {
            try {
                supervisor.projectRepository.open(file);
                undoManager.clear();
            } catch (Exception ex) {
                BasicDialogs.ShowException("Error opening project", ex);
            }
        }
        mainViewModel.setOperationInProgress(false);
    }

    public void showProject() {
        if (supervisor.project.getFile() == null) {
            BasicDialogs.ShowError("Error", "The project has not been saved yet.");
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(supervisor.project.getFile().getParentFile());
        } catch (IOException e) {
            BasicDialogs.ShowException("Error opening folder", e);
        }
    }

    public void newProjectFromExisting() {
        mainViewModel.setOperationInProgress(true);
        if (!showIfProjectNeedsSave("Choose whether to save before opening a new project")) {
            mainViewModel.setOperationInProgress(false);
            return;
        }
        //Select app
        FileChooser fc = new FileChooser();
        String path = App.getPrefs().getProjectPath();
        if (path != null && Files.exists(Paths.get(path))) {
            fc.setInitialDirectory(new File(path));
        }
        fc.setTitle("Choose a project file as a template");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project file", "*.xml"));
        File file = fc.showOpenDialog(App.getWindow());
        //Open
        if (file != null) {
            try {
                supervisor.projectRepository.loadFromTemplate(file);
                undoManager.clear();
            } catch (Exception ex) {
                BasicDialogs.ShowException("Error opening project template", ex);
            }
        }
        mainViewModel.setOperationInProgress(false);
    }

    public void newProject() {
        if (!showIfProjectNeedsSave("Choose whether to save before starting a new project")) {
            return;
        }
        undoManager.clear();
        supervisor.project.resetToDefaults();

    }

    public void saveProject() {
        if (supervisor.project.getFile() == null) {
            saveAsProject();
        } else {
            mainViewModel.setOperationInProgress(true);
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws JAXBException, IOException, XMLStreamException, ProjectRepository.RepositoryException {
                            supervisor.projectRepository.save();
                            return null;
                        }
                    };
                }
            };
            service.setOnSucceeded(e -> {
                mainViewModel.setOperationInProgress(false);
                BasicDialogs.ShowInfo("Operation complete", "Project file was saved");
            });
            service.setOnFailed(e -> {
                mainViewModel.setOperationInProgress(false);
                BasicDialogs.ShowException("There was a problem saving the project file", new Exception(service.getException()));
            });
            service.start();
        }
    }

    public void saveAsProject() {
        //Choose the file
        mainViewModel.setOperationInProgress(true);
        FileChooser fc = new FileChooser();
        String path = App.getPrefs().getProjectPath();
        if (path != null && Files.exists(Paths.get(path))) {
            fc.setInitialDirectory(new File(path));
        }
        fc.setTitle("Enter the name of the project file");
        File projectFile = supervisor.project.getFile();
        if (projectFile != null) {
            fc.setInitialFileName(projectFile.getName());
        } else {
            fc.setInitialFileName("project.xml");
        }

        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project (*.xml)", "*.xml"));
        File file = fc.showSaveDialog(App.getWindow());
        //Save
        if (file != null) {
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".xml");
            }
            final File fileToSave = file;
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws JAXBException, IOException, XMLStreamException, ProjectRepository.RepositoryException {
                            supervisor.projectRepository.saveAs(fileToSave);
                            return null;
                        }
                    };
                }
            };
            service.setOnSucceeded(e -> {
                mainViewModel.setOperationInProgress(false);
                if (service.getException() != null) {
                    BasicDialogs.ShowException("There was a problem saving the project file", new Exception(service.getException()));
                } else {
                    BasicDialogs.ShowInfo("Operation complete", "Project file was saved");
                }
            });
            service.setOnFailed(e -> {
                mainViewModel.setOperationInProgress(false);
                BasicDialogs.ShowException("There was a problem saving the project file", new Exception(service.getException()));
            });
            service.start();
        } else {
            mainViewModel.setOperationInProgress(false);
        }
    }

    public void importFromProject() {
        mainViewModel.setOperationInProgress(true);
        //Select app
        FileChooser fc = new FileChooser();
        String path = App.getPrefs().getProjectPath();
        if (path != null && Files.exists(Paths.get(path))) {
            fc.setInitialDirectory(new File(path));
        }
        fc.setTitle("Choose a project to import");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project files (*.xml)", "*.xml"));
        File file = fc.showOpenDialog(App.getWindow());
        //Open
        if (file != null) {
            try {
                supervisor.projectRepository.importFromOther(file);
            } catch (JAXBException ex) {
                BasicDialogs.ShowException("Error opening project", ex);
            }
        }
        mainViewModel.setOperationInProgress(false);
    }

    public boolean showIfProjectNeedsSave(String message) {
        if (supervisor.project.getIsDirty()) {
            AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save");
            alert.setHeaderText("Save Before Exit?");
            alert.setContentText(message);
            Image image = new Image(App.class.getResourceAsStream("/icons/icon.png" ), 44, 44, true, true);
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(image);
            ButtonType buttonTypeOne = new ButtonType("Save");
            ButtonType buttonTypeTwo = new ButtonType("Don't save");
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == buttonTypeOne) {
                    saveProject();
                    return false;
                } else if (result.get() == buttonTypeTwo) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}
