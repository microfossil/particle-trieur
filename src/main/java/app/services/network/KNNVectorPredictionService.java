package main.java.app.services.network;

import main.java.app.helpers.Metrics;
import main.java.app.models.Supervisor;
import main.java.app.models.network.classification.ClassificationSet;
import main.java.app.models.project.Particle;
import main.java.app.models.project.Taxon;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class KNNVectorPredictionService {

    Supervisor supervisor;

    public KNNVectorPredictionService(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    public Service<ClassificationSet> predictUsingKNNService(Particle currentParticle, List<Particle> particles, int k) {
        return new Service<ClassificationSet>() {
            @Override
            protected Task<ClassificationSet> createTask() {
                return new Task<ClassificationSet>() {
                    @Override
                    protected ClassificationSet call() throws Exception {
                        return predictUsingKNN(currentParticle,
                                particles,
                                k,
                                () -> (this.isCancelled() || Thread.currentThread().isInterrupted()));
                    }
                };
            }
        };
    }

    public Service<HashMap<Particle,ClassificationSet>> predictUsingKNNService(List<Particle> currentParticles, List<Particle> particles, int k) {
        return new Service<HashMap<Particle,ClassificationSet>>() {
            @Override
            protected Task<HashMap<Particle,ClassificationSet>> createTask() {
                return new Task<HashMap<Particle,ClassificationSet>>() {
                    @Override
                    protected HashMap<Particle,ClassificationSet> call() throws Exception {
                        HashMap<Particle,ClassificationSet> classifications = new HashMap<>();
                        updateMessage("Calculating...");
                        int progress = 0;
                        for (Particle particle : currentParticles) {
                            if (this.isCancelled() || Thread.currentThread().isInterrupted()) {
                                return null;
                            }
                            ClassificationSet cls = predictUsingKNN(particle,
                                    particles,
                                    k,
                                    () -> (this.isCancelled() || Thread.currentThread().isInterrupted()));
                            classifications.put(particle,cls);
                            progress++;
                            updateMessage(String.format("%d/%d predictions complete", progress, currentParticles.size()));
                            updateProgress(progress, currentParticles.size());
                        }
                        return classifications;
                    }
                };
            }
        };
    }

    public ClassificationSet predictUsingKNN(Particle currentParticle, List<Particle> particles, int k, Supplier<Boolean> wasCancelled) {
        ArrayList<Double> scores = new ArrayList<>(particles.size());

//        double minScore = 1.0;

        for (Particle particle : particles) {
            //Check if cancelled
            if (wasCancelled.get()) {
                return null;
            }
            //Give score of zero if current value
            if (particle == currentParticle) {
                scores.add(0.0);
                continue;
            }
            if (particle.getCNNVector() != null && currentParticle.getCNNVector() != null) {
                double score = Metrics.vectorCosineSimilarity(particle.getCNNVector(), currentParticle.getCNNVector());
                scores.add(score);
//                if (score < minScore) minScore = score;
            } else {
                scores.add(0.0);
            }
        }
        int[] sortedIndices = IntStream.range(0, particles.size()).boxed().sorted((i, j) -> {
            double score1 = scores.get(i);
            double score2 = scores.get(j);
            int answer;
            if (score1 == score2) {
                answer = 0;
            } else if (score1 > score2) {
                answer = 1;
            } else {
                answer = -1;
            }
            return -1 * answer;
        }).mapToInt(ele -> ele).toArray();
        //Check if cancelled
        if (wasCancelled.get()) {
            return null;
        }
        //Find the top k values
        LinkedHashMap<String, Double> freq = new LinkedHashMap<>();
        int size = 0;
        for (int i = 0; i < particles.size(); i++) {
            if (wasCancelled.get()) {
                return null;
            }
            int idx = sortedIndices[i];
            Particle particle = particles.get(idx);
            String code = particle.classification.get();
            Taxon taxon = supervisor.project.getTaxons().get(code);
            double score = scores.get(idx);

//            try {
                if (taxon.getIsClass()) {
                    //Add the score
                    double totalScore = freq.containsKey(code) ? freq.get(code) : 0;
                    freq.put(code, totalScore + score);
                    size++;
                    //Exit if we have enough
                    if (size >= k) break;
                }
//            }
//            catch (NullPointerException ex) {
//                System.out.print(code);
//                ex.printStackTrace();
//            }
        }
        //Add to classification set
        ClassificationSet classificationSet = new ClassificationSet();
        for(Map.Entry<String, Double> entry : freq.entrySet()) {
            classificationSet.add(entry.getKey(), entry.getValue() / k, "kNN");
        }
        return classificationSet;
    }
}
