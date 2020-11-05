package ordervschaos.particletrieur.app.viewmanagers.commands;

import ordervschaos.particletrieur.app.models.network.classification.ClassificationSet;
import ordervschaos.particletrieur.app.models.project.Project;
import ordervschaos.particletrieur.app.models.project.Particle;

import java.util.ArrayList;
import java.util.List;

public class SetLabelCommand extends UndoableCommand {

    private Project project;
    private List<Particle> particles;
    private List<ClassificationSet> classificationSets;
    private String code;
    double score;
    String classifier;
    boolean clearOthers;

    public SetLabelCommand(Project project, List<Particle> particles, String code, double score, String classifier, boolean clearOthers) {
        super(String.format("label %d images as %s", particles.size(), code));
        this.project = project;
        this.particles = new ArrayList<>(particles);
        this.code = code;
        this.score = score;
        this.classifier = classifier;
        this.clearOthers = clearOthers;
        classificationSets = new ArrayList<>();
        for (Particle p : particles) {
            classificationSets.add(p.getClassifications().clone());
        }
    }

    @Override
    public boolean apply() throws Project.TaxonDoesntExistException {
        project.setParticleLabel(particles, code, score, classifier, clearOthers);
        return true;
    }

    @Override
    public boolean revert() {
        int idx = 0;
        for (Particle p : particles) {
            project.setParticleLabelSet(p, classificationSets.get(idx));
            idx++;
        }
        return true;
    }
}
