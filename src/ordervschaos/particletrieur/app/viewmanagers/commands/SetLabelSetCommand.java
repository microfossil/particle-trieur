package ordervschaos.particletrieur.app.viewmanagers.commands;

import ordervschaos.particletrieur.app.models.network.classification.ClassificationSet;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.project.Project;

import java.util.*;

public class SetLabelSetCommand extends UndoableCommand {

    private Project project;
    private HashMap<Particle,ClassificationSet> classifications;
    private HashMap<Particle,ClassificationSet> oldClassifications;
    double threshold;
    boolean wasAuto;

    public SetLabelSetCommand(Project project, HashMap<Particle,ClassificationSet> classifications, double threshold, boolean wasAuto) {
        super(String.format("label %d images", classifications.size()));
        this.project = project;
        this.threshold = threshold;
        this.classifications = classifications;
        this.wasAuto = wasAuto;
        oldClassifications = new LinkedHashMap<>();
        for (Map.Entry<Particle, ClassificationSet> entry : classifications.entrySet()) {
            oldClassifications.put(entry.getKey(), entry.getKey().getClassifications());
        }
    }

    @Override
    public boolean apply() {
        for (Map.Entry<Particle, ClassificationSet> entry : classifications.entrySet()) {
            project.setParticleLabelSet(entry.getKey(), entry.getValue(), threshold, wasAuto);
        }
        return true;
    }

    @Override
    public boolean revert() {
        for (Map.Entry<Particle, ClassificationSet> entry : oldClassifications.entrySet()) {
            project.setParticleLabelSet(entry.getKey(), entry.getValue());
        }
        return true;
    }
}
