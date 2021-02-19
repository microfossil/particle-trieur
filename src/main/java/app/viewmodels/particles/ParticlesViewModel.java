package main.java.app.viewmodels.particles;

import main.java.app.controls.AlertEx;
import main.java.app.viewmanagers.UndoManager;
import main.java.app.viewmanagers.commands.RemoveParticlesCommand;
import main.java.app.models.Supervisor;
import main.java.app.models.project.Particle;
import main.java.app.AbstractDialogController;
import main.java.app.viewcontrollers.particle.AddParticleViewController;
import main.java.app.viewcontrollers.particle.EditParticleMetadataViewController;
import com.google.inject.Inject;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import main.java.app.viewmodels.SelectionViewModel;

import java.io.IOException;
import java.util.List;

public class ParticlesViewModel {

    @Inject
    private SelectionViewModel selectionViewModel;
    @Inject
    private Supervisor supervisor;
    @Inject
    UndoManager undoManager;

    public void addParticles() {
        try {
            AbstractDialogController.create(AddParticleViewController.class).showAndWait();
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
            controller.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
