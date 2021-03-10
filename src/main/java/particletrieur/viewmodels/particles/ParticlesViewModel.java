package particletrieur.viewmodels.particles;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Pair;
import particletrieur.controls.dialogs.AlertEx;
import particletrieur.helpers.AutoCancellingServiceRunner;
import particletrieur.helpers.AutoCancellingTaskRunner;
import particletrieur.viewmanagers.UndoManager;
import particletrieur.viewmanagers.commands.RemoveParticlesCommand;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Particle;
import particletrieur.AbstractDialogController;
import particletrieur.viewcontrollers.particle.AddParticleViewController;
import particletrieur.viewcontrollers.particle.EditParticleMetadataViewController;
import com.google.inject.Inject;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import particletrieur.viewmodels.SelectionViewModel;

import java.io.IOException;
import java.util.List;

public class ParticlesViewModel {

    @Inject
    private SelectionViewModel selectionViewModel;
    @Inject
    UndoManager undoManager;
    private Supervisor supervisor;

    private DoubleProperty validatedPercentage = new SimpleDoubleProperty(0);
    public double getValidatedPercentage() {
        return validatedPercentage.get();
    }
    public DoubleProperty validatedPercentageProperty() {
        return validatedPercentage;
    }
    public void setValidatedPercentage(double validatedPercentage) {
        this.validatedPercentage.set(validatedPercentage);
    }

    private DoubleProperty labeledPercentage = new SimpleDoubleProperty(0);
    public double getLabeledPercentage() {
        return labeledPercentage.get();
    }
    public DoubleProperty labeledPercentageProperty() {
        return labeledPercentage;
    }
    public void setLabeledPercentage(double labeledPercentage) {
        this.labeledPercentage.set(labeledPercentage);
    }

    private AutoCancellingTaskRunner<Pair<Double, Double>> validatedCountRunner;

    @Inject
    public ParticlesViewModel(Supervisor supervisor) {
        this.supervisor = supervisor;
        validatedCountRunner = new AutoCancellingTaskRunner<>();

        supervisor.project.particleValidatedEvent.addListener(object -> {
            countValidated();
        });
        supervisor.project.particleLabeledEvent.addListener(object -> {
            countValidated();
        });
        supervisor.project.particles.addListener((ListChangeListener<Particle>) c -> countValidated());
    }

    private void countValidated() {
        Task<Pair<Double, Double>> t = new Task<Pair<Double, Double>>() {
            @Override
            protected Pair<Double, Double> call() throws Exception {
                double validated = 0;
                double labeled = 0;
                for (Particle particle : supervisor.project.getParticles()) {
                    if (particle.getValidator() != "") {
                        validated ++;
                    }
                    if (supervisor.project.getTaxons().get(particle.getClassification()).getIsClass()) {
                        labeled ++;
                    }
                }
                return new Pair<>(validated / supervisor.project.getParticles().size(), labeled / supervisor.project.getParticles().size());
            }
        };
        t.setOnSucceeded(event -> {
            Pair<Double, Double> result = t.getValue();
            setValidatedPercentage(result.getKey());
            setLabeledPercentage(result.getValue());
            System.out.println(getValidatedPercentage());
            System.out.println(getLabeledPercentage());
        });
        validatedCountRunner.runTask(t);
    }

    public void addParticles() {
        try {
            AbstractDialogController.create(AddParticleViewController.class).showEmbedded();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeParticles(List<Particle> particles) {
        if (particles.isEmpty()) return;
        AlertEx alert = new AlertEx(Alert.AlertType.CONFIRMATION, "Are you sure you wish to remove these images?", ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.YES) {
                RemoveParticlesCommand command = new RemoveParticlesCommand(supervisor.project, particles);
                command.apply();
                undoManager.add(command);
            }
        });
    }

    public void editParticlesMetadata() {
        try {
            EditParticleMetadataViewController controller = AbstractDialogController.create(EditParticleMetadataViewController.class);
            controller.setup(selectionViewModel.getCurrentParticles());
            controller.showEmbedded();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void randomiseParticles() {
        supervisor.project.randomiseParticles();
    }
}
