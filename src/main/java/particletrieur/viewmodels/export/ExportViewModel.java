package particletrieur.viewmodels.export;

import com.google.inject.Inject;
import javafx.concurrent.Service;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import particletrieur.AbstractDialogController;
import particletrieur.App;
import particletrieur.controls.AlertEx;
import particletrieur.controls.BasicDialogs;
import particletrieur.models.Supervisor;
import particletrieur.services.export.ExportAbundanceService;
import particletrieur.services.export.ExportMorphologyService;
import particletrieur.viewcontrollers.export.ExportViewController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;


public class ExportViewModel {

    @Inject
    Supervisor supervisor;

    public void exportProjectImages() {
        //Show export option
        try {
            ExportViewController controller = AbstractDialogController.create(ExportViewController.class);
            controller.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportMorphologyToCSV() {
        //Export file
        FileChooser fc = new FileChooser();
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

    public void exportProjectToCSV() {
        //Export file
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose where to save exported file");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv"));
        fc.setInitialFileName("project.csv");
        String path = App.getPrefs().getExportPath();
        if(path != null && Files.exists(Paths.get(path))) {
            fc.setInitialDirectory(new File(path));
        }
        File file = fc.showSaveDialog(App.getWindow());

        if (file == null) return;

        //Confimation dialog
        AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Export project to CSV");
        alert.setContentText("Exporting information to "  + file.getAbsolutePath());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if(!result.isPresent()) {
            return;
        }
        App.getPrefs().setExportPath(file.getParent());
        if (result.get() == ButtonType.OK) {
            Service service = ExportMorphologyService.exportInformationToCSV(supervisor.project.particles, supervisor, file);
            BasicDialogs.ProgressDialogWithCancel2(
                    "Operation",
                    "Exporting project to CSV",
                    App.getRootPane(),
                    service).start();
        }
    }

    public void exportAbundance() {
        //Export file
        FileChooser fc = new FileChooser();
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
