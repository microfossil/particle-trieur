package ordervschaos.particletrieur.app.viewmanagers.commands;

import ordervschaos.particletrieur.app.models.network.classification.ClassificationSet;
import ordervschaos.particletrieur.app.models.project.Project;
import ordervschaos.particletrieur.app.models.project.Particle;

import java.util.*;

public class SetLabelCommand extends UndoableCommand {

    private Project project;
    private List<Particle> particles;
    private List<ClassificationSet> classificationSets;
    private String code;
    double score;
    String classifier;
    boolean clearOthers;
    String validator;
    private HashMap<Particle,String> oldValidators;

    public SetLabelCommand(Project project, List<Particle> particles, String code, double score, String classifier, boolean clearOthers, String validator) {
        super(String.format("label %d images as %s", particles.size(), code));
        this.project = project;
        this.particles = new ArrayList<>(particles);
        this.code = code;
        this.score = score;
        this.classifier = classifier;
        this.clearOthers = clearOthers;
        this.validator = validator;
        classificationSets = new ArrayList<>();
        oldValidators = new LinkedHashMap<>();
        for (Particle p : particles) {
            classificationSets.add(p.getClassifications().clone());
            oldValidators.put(p, p.getValidator());
        }
    }

    @Override
    public boolean apply() throws Project.TaxonDoesntExistException {
        project.setParticleLabel(particles, code, score, classifier, clearOthers);
        project.setParticleValidator(particles, validator);
        return true;
    }

    @Override
    public boolean revert() {
        int idx = 0;
        for (Particle p : particles) {
            project.setParticleLabelSet(p, classificationSets.get(idx));
            idx++;
        }
        for (Map.Entry<Particle, String> entry : oldValidators.entrySet()) {
            project.setParticleValidator(entry.getKey(), entry.getValue());
        }
        return true;
    }
}
