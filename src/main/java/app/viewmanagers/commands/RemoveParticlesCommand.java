package main.java.app.viewmanagers.commands;

import main.java.app.models.project.Project;
import main.java.app.models.project.Particle;

import java.util.ArrayList;
import java.util.List;

public class RemoveParticlesCommand extends UndoableCommand {

    private Project project;
    private List<Particle> particles;

    public RemoveParticlesCommand(Project project, List<Particle> particles) {
        super(String.format("remove %d images", particles.size()));
        this.project = project;
        this.particles = new ArrayList<>(particles);
    }

    @Override
    public boolean apply() {
        project.removeParticles(particles);
        return true;
    }

    @Override
    public boolean revert() {
        project.addParticles(particles);
        return true;
    }
}
