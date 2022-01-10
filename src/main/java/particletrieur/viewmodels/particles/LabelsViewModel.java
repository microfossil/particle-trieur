package particletrieur.viewmodels.particles;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import particletrieur.App;
import particletrieur.AppController;
import particletrieur.models.network.classification.ClassificationSet;
import particletrieur.models.project.Project;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Particle;
import particletrieur.models.project.Taxon;
import particletrieur.controls.dialogs.BasicDialogs;
import com.google.inject.Inject;
import particletrieur.models.project.TreeTaxon;
import particletrieur.models.taxonomy.RappTaxon;
import particletrieur.services.taxonomy.RappTaxonService;
import particletrieur.viewmanagers.UndoManager;
import particletrieur.viewmanagers.commands.SetLabelCommand;
import particletrieur.viewmanagers.commands.SetLabelSetCommand;
import particletrieur.viewmodels.SelectionViewModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LabelsViewModel {

    @Inject
    SelectionViewModel selectionViewModel;
    @Inject
    Supervisor supervisor;
    @Inject
    UndoManager undoManager;

    private BooleanProperty autoValidate = new SimpleBooleanProperty(false);
    public boolean isAutoValidate() {
        return autoValidate.get();
    }
    public BooleanProperty autoValidateProperty() {
        return autoValidate;
    }
    public void setAutoValidate(boolean autoValidate) {
        this.autoValidate.set(autoValidate);
    }

    private BooleanProperty autoAdvance = new SimpleBooleanProperty(false);
    public boolean isAutoAdvance() {
        return autoAdvance.get();
    }
    public BooleanProperty autoAdvanceProperty() {
        return autoAdvance;
    }
    public void setAutoAdvance(boolean autoAdvance) {
        this.autoAdvance.set(autoAdvance);
    }

    private StringProperty rappXLXSPath = new SimpleStringProperty();
    public String getRappXLXSPath() {
        return rappXLXSPath.get();
    }
    public StringProperty rappXLXSPathProperty() {
        return rappXLXSPath;
    }
    public void setRappXLXSPath(String rappXLXSPath) {
        this.rappXLXSPath.set(rappXLXSPath);
    }

    public ObservableList<RappTaxon> rappTaxons = FXCollections.observableArrayList();

    public enum ValidationState {
        INVALID,
        INDETERMINATE,
        VALIDATED
    }

    @Inject
    public LabelsViewModel(SelectionViewModel selectionViewModel, Supervisor supervisor) {
        this.selectionViewModel = selectionViewModel;
        this.supervisor = supervisor;

        rappXLXSPath.addListener(((observable, oldValue, newValue) -> {
            try {
                if ((new File(newValue)).exists()) {
                    updateRappTaxonomy(newValue);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }));

        setRappXLXSPath(App.getPrefs().getRappPath());
    }

    private String getValidator() {
        if (!isAutoValidate()) return null;
        else return supervisor.getUsername();
    }

    public ValidationState getValidationState() {
        int count = 0;
        int validCount = 0;
        for (Particle p : selectionViewModel.getCurrentParticles()) {
            if (!p.getValidator().equals("")) {
                validCount++;
            }
            count++;
        }
        if (validCount == count) return ValidationState.VALIDATED;
        else if (validCount > 0) return ValidationState.INDETERMINATE;
        else return ValidationState.INVALID;
    }

    public void toggleValidated() {
        ValidationState state = getValidationState();
        if (state != ValidationState.VALIDATED) {
            supervisor.project.setParticleValidator(selectionViewModel.getCurrentParticles(), supervisor.getUsername());
        }
        else {
            supervisor.project.setParticleValidator(selectionViewModel.getCurrentParticles(), "");
        }
        selectionViewModel.currentParticleUpdatedEvent.broadcast();
        if (isAutoAdvance()) {
            selectionViewModel.requestNextImage();
        }
    }

    public void setLabel(String code, double score, boolean clearOthers) {
        setLabel(selectionViewModel.getCurrentParticles(), code, score, clearOthers);
        selectionViewModel.currentParticleUpdatedEvent.broadcast();
    }

    public void setLabel(Particle particle, String code, double score, boolean clearOthers) {
        List<Particle> particles = new ArrayList<>();
        particles.add(particle);
        setLabel(particles, code, score, clearOthers);
        selectionViewModel.checkIfCurrentWasUpdated(particle);
    }

    public void setLabel(List<Particle> particles, String code, double score, boolean clearOthers) {
        try {
            SetLabelCommand command = new SetLabelCommand(supervisor.project, particles, code, score, supervisor.getUsername(), clearOthers, getValidator());
            command.apply();
            undoManager.add(command);
            selectionViewModel.checkIfCurrentWasUpdated(particles);
            if (isAutoAdvance()) {
                selectionViewModel.requestNextImage();
            }
        } catch (Project.TaxonDoesntExistException ex) {
            BasicDialogs.ShowError("Label doesn't exist",
                    "The label referred to by the button doesn't exist in the project's label list.\n"
                            + "Please contact me regarding this error at ross.g.marchant@gmail.com");
        }
    }

    public void setLabelSet(HashMap<Particle, ClassificationSet> classifications, double threshold, boolean wasAuto) {
        SetLabelSetCommand command = new SetLabelSetCommand(supervisor.project, classifications, threshold, wasAuto, getValidator());
        command.apply();
        undoManager.add(command);
        selectionViewModel.checkIfCurrentWasUpdated(new ArrayList<>(classifications.keySet()));
    }

    public boolean updateLabel(String code, Taxon updated) {
        try {
            if (updated.getCode().equalsIgnoreCase("")) {
                BasicDialogs.ShowError("Error", "You must enter a code");
                return false;
            }
            supervisor.project.updateTaxon(code, updated);
            if (!code.equals(updated.getCode())) {
                BasicDialogs.ShowInfo("Code changed",
                        String.format("The label code \"%s\" has been changed to \"%s\".\nAll particles with this label have also been updated.", code, updated.getCode()));
            }
            return true;
        } catch (Project.TaxonAlreadyExistsException ex) {
            BasicDialogs.ShowError("Conflict",
                    String.format("Tried to modify label code \"%s\" to \"%s\", but the code \"%s\" already exists.\n"
                                    + "Use the classification buttons to change label, then delete the other label.",
                            code, updated.getCode(), updated.getCode()));
            return false;
        }
    }

    public boolean addLabel(Taxon updated) {
        try {
            if (updated.getCode().equalsIgnoreCase("")) {
                BasicDialogs.ShowError("Error", "You must enter a code");
                return false;
            }
            supervisor.project.addTaxon(updated);
            return true;
        } catch (Project.TaxonAlreadyExistsException ex) {
            BasicDialogs.ShowError("Conflict",
                    String.format("Tried to add a new label with code \"%s\", but the code already exists.",
                            updated.getCode()));
            return false;
        }
    }

    public void deleteLabel(Taxon taxon) {
        BasicDialogs.ShowConfirmation("Delete label",
                String.format("Are you sure you wish to delete the label \"%s\"?", taxon.getCode()),
                () -> {
                    try {
                        supervisor.project.deleteTaxon(taxon);
                        BasicDialogs.ShowInfo("Code changed",
                                String.format("The label code \"%s\" has been deleted. All particles with this label have been changed to unlabeled.", taxon.getCode()));
                    } catch (Project.TaxonDoesntExistException ex) {
                        BasicDialogs.ShowError("Error",
                                String.format("The label code \"%s\" does not exist.", taxon.getCode()));
                    }
                },null);
    }

    public void initialiseLabels(List<Taxon> taxons) {
        supervisor.project.initialiseTaxons(taxons);
    }

    public void loadRappTaxonomy() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose XLSX file with taxonomy");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLSX file (*.xlsx)", "*.xlsx"));
        String path = App.getPrefs().getRappPath();
        if (path != null && Files.exists(Paths.get(path))) {
            File f = new File(path);
            fc.setInitialDirectory(f.getParentFile());
        }
        File file = fc.showOpenDialog(AppController.getWindow());
        if (file == null) return;
        setRappXLXSPath(file.getAbsolutePath());
    }

    private void updateRappTaxonomy(String filename) throws IOException {
        List<RappTaxon> taxons = RappTaxonService.parseCodes(filename);
        if (taxons.size() > 0) {
            rappTaxons.clear();
            rappTaxons.addAll(taxons);
            App.getPrefs().setRappPath(filename);
        }
        else {
            BasicDialogs.ShowError("Error", "XLSX file did not contain a list of taxons in sheet 3\n\nFormat must be: id, type, group, name");
        }
    }

    public TreeTaxon addTaxonTree() {
        //Export file
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose XLSX file with taxonomy");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLSX file (*.xlsx)", "*.xlsx"));
        fc.setInitialDirectory(new File("C:\\Users\\rossm\\OneDrive\\RAPP"));
        File file = fc.showOpenDialog(AppController.getWindow());
        if (file == null) return null;

        try {
            return RappTaxonService.ParseTaxonomicTreeXLSX(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

//        //Confimation dialog
//        AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Export project to CSV");
//        alert.setContentText("Exporting to "  + file.getAbsolutePath() + ".\n\n This may take a long time.");
//        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
//        alert.showEmbedded();
//        alert.setResultConverter(button -> {
//            if (button == ButtonType.OK) {
//                //TODO morphology should export everything as well and option for the same
//                App.getPrefs().setExportPath(file.getParent());
//                Service service = ExportMorphologyService.exportToCSV(supervisor.project.particles, supervisor, file, exportParameters, exportMorphology);
//                BasicDialogs.ProgressDialogWithCancel2(
//                        "Operation",
//                        "Exporting project to CSV",
//                        service).start();
//            }
//            return null;
//        });
    }

}
