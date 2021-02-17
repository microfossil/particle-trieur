package main.java.app.viewmanagers.commands;

import main.java.app.models.project.Project;
import main.java.app.models.project.Particle;

import java.util.ArrayList;
import java.util.List;

public class AddParticlesCommand extends UndoableCommand {

    private Project project;
    private List<Particle> particles;

    public AddParticlesCommand(Project project, List<Particle> particles) {
        super(String.format("add %d images", particles.size()));
        this.project = project;
        this.particles = new ArrayList<>(particles);
    }

    @Override
    public boolean apply() {
        project.addParticles(particles);
        return true;
    }

    @Override
    public boolean revert() {
        project.removeParticles(particles);
        return true;
    }
}
