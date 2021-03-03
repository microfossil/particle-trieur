package particletrieur.viewmodels.particles;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import particletrieur.models.network.classification.ClassificationSet;
import particletrieur.models.project.Project;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Particle;
import particletrieur.models.project.Taxon;
import particletrieur.controls.dialogs.BasicDialogs;
import com.google.inject.Inject;
import particletrieur.viewmanagers.UndoManager;
import particletrieur.viewmanagers.commands.SetLabelCommand;
import particletrieur.viewmanagers.commands.SetLabelSetCommand;
import particletrieur.viewmodels.SelectionViewModel;

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

    public enum ValidationState {
        INVALID,
        INDETERMINATE,
        VALIDATED
    }

    @Inject
    public LabelsViewModel(SelectionViewModel selectionViewModel, Supervisor supervisor) {
        this.selectionViewModel = selectionViewModel;
        this.supervisor = supervisor;
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
            selectionViewModel.nextImageRequested.broadcast(true);
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
                selectionViewModel.nextImageRequested.broadcast(true);
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

}
