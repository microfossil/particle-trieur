package ordervschaos.particletrieur.app.viewmodels;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import ordervschaos.particletrieur.app.models.network.classification.ClassificationSet;
import ordervschaos.particletrieur.app.models.project.Project;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.project.Taxon;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import com.google.inject.Inject;
import ordervschaos.particletrieur.app.viewmanagers.UndoManager;
import ordervschaos.particletrieur.app.viewmanagers.commands.SetLabelCommand;
import ordervschaos.particletrieur.app.viewmanagers.commands.SetLabelSetCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Inject
    public LabelsViewModel(SelectionViewModel selectionViewModel, Supervisor supervisor) {
        this.selectionViewModel = selectionViewModel;
        this.supervisor = supervisor;
    }

    private String getValidator() {
        if (!isAutoValidate()) return null;
        else return supervisor.getUsername();
    }

    public void toggleValidated() {
        Particle particle = selectionViewModel.getCurrentParticle();
        if (particle.getValidator().equals("")) {
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
        try {
            supervisor.project.deleteTaxon(taxon);
            BasicDialogs.ShowInfo("Code changed",
                    String.format("The label code \"%s\" has been deleted. All particles with this label have been changed to unlabeled.", taxon.getCode()));
        } catch (Project.TaxonDoesntExistException ex) {
            BasicDialogs.ShowError("Error",
                    String.format("The label code \"%s\" does not exist.", taxon.getCode()));
        }
    }

    public void initialiseLabels(List<Taxon> taxons) {
        supervisor.project.initialiseTaxons(taxons);
    }

}
