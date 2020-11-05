package ordervschaos.particletrieur.app.viewmodels;

import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.models.Supervisor;
import com.google.inject.Inject;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FolderWatchViewModel {

    @Inject
    Supervisor supervisor;

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
