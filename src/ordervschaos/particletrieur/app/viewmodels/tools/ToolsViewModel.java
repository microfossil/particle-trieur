package ordervschaos.particletrieur.app.viewmodels.tools;

import javafx.stage.DirectoryChooser;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.models.project.Project;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.project.Tag;
import ordervschaos.particletrieur.app.models.project.Taxon;
import ordervschaos.particletrieur.app.services.FindDuplicatesService;
import com.google.inject.Inject;
import javafx.concurrent.Service;
import javafx.scene.control.TextInputDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ToolsViewModel {

    @Inject
    Supervisor supervisor;

    public ToolsViewModel() {

    }

    public void tagMissing(List<Particle> particles) {
        Service service = FindDuplicatesService.findMissing(particles);
        service.setOnSucceeded(succeeded -> {
            List<Particle> missing = (List<Particle>) service.getValue();
            if (missing != null && missing.size() > 0) {
                //Double check missing code is in there
                if (!supervisor.project.tags.containsKey(Project.MISSING_CODE)) {
                    try {
                        supervisor.project.addTag(new Tag(Project.MISSING_CODE, "Missing", "The image file is missing"));
                    } catch (Project.TagAlreadyExistsException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    supervisor.project.setParticleTag(missing, Project.MISSING_CODE);
                } catch (Project.TagDoesntExistException ex) {
                    BasicDialogs.ShowException("The missing tag was not found", ex);
                }
            }
        });
        BasicDialogs.ProgressDialogWithCancel2(
                "Operation",
                "Tagging duplicates by file match",
                App.getRootPane(),
                service).start();
    }

    public void tagDuplicatesUsingFilename(List<Particle> particles, boolean all) {
        Service service;
        if (!all) {
            service = FindDuplicatesService.findDuplicatesUsingHashCode(particles, supervisor.project.getParticles());
        }
        else {
            service = FindDuplicatesService.findDuplicatesUsingHashCode(supervisor.project.getParticles());
        }
        service.setOnSucceeded(succeeded -> {
            List<Particle> toRemove = (List<Particle>) service.getValue();
            if (toRemove != null && toRemove.size() > 0) {
                //Double check duplicate code is in there
                if (!supervisor.project.tags.containsKey(supervisor.project.DUPLICATE_CODE)) {
                    supervisor.project.addRequiredTags();
                }
                try {
                    supervisor.project.setParticleTag(toRemove, supervisor.project.DUPLICATE_CODE);
                } catch (Project.TagDoesntExistException ex) {
                    BasicDialogs.ShowException("The duplicate tag was not found", ex);
                }
            }
        });
        BasicDialogs.ProgressDialogWithCancel2(
                "Operation",
                "Tagging duplicates by file matching (hash code)",
                App.getRootPane(),
                service).start();
    }

    public void tagDuplicatesUsingCNNVector(List<Particle> particles, boolean all) {
        TextInputDialog ted = new TextInputDialog("0.995");
        ted.setHeaderText("Tag Duplicates");
        ted.setContentText("Enter the similarity threshold:");
        double threshold = 0.995;
        if(ted.showAndWait().isPresent()) {
            try {
                threshold = Double.parseDouble(ted.getResult());
            }
            catch (Exception ex) {
                return;
            }
        }
        else {
            return;
        }
        Service service;
        if (!all) {
            service = FindDuplicatesService.findDuplicatesUsingVector(particles, supervisor.project.getParticles(), threshold);
        }
        else {
            service = FindDuplicatesService.findDuplicatesUsingVector(supervisor.project.getParticles(), threshold);
        }
        service.setOnSucceeded(succeeded -> {
            List<Particle> toRemove = (List<Particle>) service.getValue();
            if (toRemove != null && toRemove.size() > 0) {
                //Double check duplicate code is in there
                if (!supervisor.project.tags.containsKey(supervisor.project.DUPLICATE_CODE)) {
                    supervisor.project.addRequiredTags();
                }
                try {
                    supervisor.project.setParticleTag(toRemove, supervisor.project.DUPLICATE_CODE);
                } catch (Project.TagDoesntExistException ex) {
                    BasicDialogs.ShowException("The duplicate tag was not found", ex);
                }
            }
        });
        BasicDialogs.ProgressDialogWithCancel2(
                "Operation",
                "Tagging duplicates by feature vector",
                App.getRootPane(),
                service).start();
    }

    public void toggleFolderWatch() {
        if (!supervisor.folderWatch.isEnabled()) {
            BasicDialogs.ShowInfo("Folder Watch", "Select a folder to watch. Images added to this directory will be added to the current project.");
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select folder to watch...");
            String path = App.getPrefs().getProjectPath();
            if (path != null && Files.exists(Paths.get(path))) {
                chooser.setInitialDirectory(new File(path));
            }
            File res = chooser.showDialog(App.getWindow());
            if (res == null) return;
            try {
                supervisor.folderWatch.start(res);
                if (supervisor.folderWatch.isEnabled()) {
                    BasicDialogs.ShowInfo("Folder Watch",
                            String.format("The folder\n%s\nis now being watched.",
                                    res.getAbsolutePath()));
                }
                else {
                    BasicDialogs.ShowError("Folder Watch", "Unknown error starting folder watch.");
                }
            } catch (IOException e) {
                BasicDialogs.ShowException("Error starting folder watch", e);
            }

        }
        else {
            supervisor.folderWatch.stop();
        }
    }
}
