package ordervschaos.particletrieur.app.viewmodels;

import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.AbstractDialogController;
import ordervschaos.particletrieur.app.services.export.ExportAbundanceService;
import ordervschaos.particletrieur.app.services.export.ExportMorphologyService;
import ordervschaos.particletrieur.app.viewcontrollers.ExportViewController;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import com.google.inject.Inject;
import javafx.concurrent.Service;
import ordervschaos.particletrieur.app.controls.AlertEx;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class ExportViewModel {

    @Inject
    Supervisor supervisor;
    @Inject
    SelectionViewModel selectionViewModel;

    public void exportProjectImages() {
        //Show export option
        try {
            ExportViewController controller = AbstractDialogController.create(ExportViewController.class);
            controller.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportProjectData() {
        //Export file
        FileChooser fc = new FileChooser();
//        String dir = App.getPrefs().getProjectPath();
//        if (dir != null) {
//            File dirFile = new File(dir);
//            if (dirFile.exists()) {
//                fc.setInitialDirectory(dirFile);
//            }
//        }
        fc.setTitle("Choose where to save exported file");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv"));
        fc.setInitialFileName("morphology.csv");
        String path = App.getPrefs().getExportPath();
        if(path != null && Files.exists(Paths.get(path))) {
            fc.setInitialDirectory(new File(path));
        }
        File file = fc.showSaveDialog(App.getWindow());

        if (file == null) return;

        //Confimation dialog
        AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Export morphology to CSV");
        alert.setContentText("Exporting files to "  + file.getAbsolutePath() + ".\n\n This may take a long time.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if(!result.isPresent()) {
            return;
        }
        App.getPrefs().setExportPath(file.getParent());

        if (result.get() == ButtonType.OK) {
            //TODO morphology should export everything as well and option for the same
            Service service = ExportMorphologyService.exportMorphologyToCSV(supervisor.project.particles, supervisor, file);
            BasicDialogs.ProgressDialogWithCancel2(
                    "Operation",
                    "Exporting morphology to CSV",
                    App.getRootPane(),
                    service).start();
        }
    }

    public void exportAbundance() {
        //Export file
        FileChooser fc = new FileChooser();
//        String dir = App.getPrefs().getProjectPath();
//        if (dir != null) {
//            File dirFile = new File(dir);
//            if (dirFile.exists()) {
//                fc.setInitialDirectory(dirFile);
//            }
//        }
        fc.setTitle("Choose where to save exported file");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv"));
        fc.setInitialFileName("abundance.csv");
        String path = App.getPrefs().getExportPath();
        if(path != null && Files.exists(Paths.get(path))) {
            fc.setInitialDirectory(new File(path));
        }
        File file = fc.showSaveDialog(App.getWindow());

        if (file == null) return;

        //Confimation dialog
        AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Export abundance");
        alert.setContentText("Exporting to "  + file.getAbsolutePath());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if(!result.isPresent()) {
            return;
        }
        App.getPrefs().setExportPath(file.getParent());

        if (result.get() == ButtonType.OK) {
            Service service = ExportAbundanceService.exportAbundance(supervisor.project, file);
            BasicDialogs.ProgressDialogWithCancel2(
                    "Operation",
                    "Exporting abundance CSV",
                    App.getRootPane(),
                    service).start();
        }
    }

    public void exportSampleCounts() {
        //Export file
        FileChooser fc = new FileChooser();
//        String dir = App.getPrefs().getProjectPath();
//        if (dir != null) {
//            File dirFile = new File(dir);
//            if (dirFile.exists()) {
//                fc.setInitialDirectory(dirFile);
//            }
//        }
        fc.setTitle("Choose where to save exported file");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv"));
        fc.setInitialFileName("sample_counts.csv");
        String path = App.getPrefs().getExportPath();
        if(path != null && Files.exists(Paths.get(path))) {
            fc.setInitialDirectory(new File(path));
        }
        File file = fc.showSaveDialog(App.getWindow());

        if (file == null) return;

        //Confimation dialog
        AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Export sample counts");
        alert.setContentText("Exporting to "  + file.getAbsolutePath());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if(!result.isPresent()) {
            return;
        }
        App.getPrefs().setExportPath(file.getParent());

        if (result.get() == ButtonType.OK) {
            Service service = ExportAbundanceService.exportCounts(supervisor.project, file);
            BasicDialogs.ProgressDialogWithCancel2(
                    "Operation",
                    "Exporting sample counts CSV",
                    App.getRootPane(),
                    service).start();
        }
    }
}
