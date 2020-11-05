/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.services;

import ordervschaos.particletrieur.app.helpers.Metrics;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.network.features.Similarity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ParticleSimilarityService {

    public enum SortBy {
        Class("Class"),
        ImageSize("Image Size"),
        Vector("Feature Vector");
        private String label;
        SortBy(String label) {
            this.label = label;
        }
        public String toString() {
            return label;
        }
    }

    public static Service<List<Similarity>> findMostSimilarService(Particle currentParticle, List<Particle> particles, SortBy sortBy) {
        Service<List<Similarity>> service =  new Service<List<Similarity>>() {
            @Override
            protected Task<List<Similarity>> createTask() {
                Task<List<Similarity>> task = new Task<List<Similarity>>() {
                    @Override
                    protected List<Similarity> call() throws Exception {

                        //Blank list if file is missing
                        if (!currentParticle.getFile().exists()) return new ArrayList<>();

                        ArrayList<Double> scores = new ArrayList<>(particles.size());
                        ArrayList<String> values = new ArrayList<>(particles.size());

                        for (Particle foram : particles) {
                            if (this.isCancelled() || Thread.currentThread().isInterrupted()) {
                                return null;
                            }

                            if (foram == currentParticle) {
                                scores.add(2.0);
                                values.add("");
                                continue;
                            }

                            switch (sortBy) {
                                case Class:
                                    if (foram.classification.get().equals(currentParticle.classification.get())) {
                                        scores.add(1.0);
                                        values.add("");
                                    } else {
                                        scores.add(0.0);
                                        values.add("");
                                    }
                                    break;
                                case ImageSize:
                                    double ha = foram.getImageHeight();
                                    double wa = foram.getImageWidth();
                                    double hb = currentParticle.getImageHeight();
                                    double wb = currentParticle.getImageWidth();
                                    double la = Math.sqrt(ha * ha + wa * wa);
                                    double lb = Math.sqrt(hb * hb + wb * wb);
                                    double d = Math.sqrt((ha - hb) * (ha - hb) + (wa - wb) * (wa - wb));
                                    d = la > lb ? 1 - (d / la) : 1 - (d / lb);
                                    scores.add(d);
                                    values.add(String.format("%d x %d", foram.getImageHeight(), foram.getImageWidth()));
                                    break;
                                case Vector:
                                    if (foram.getCNNVector() != null && currentParticle.getCNNVector() != null) {
                                        double score = Metrics.vectorCosineSimilarity(foram.getCNNVector(), currentParticle.getCNNVector());
                                        scores.add(score);
                                        values.add(String.format("%.3f", score));
                                    } else {
                                        scores.add(-1.0);
                                        values.add("no vector");
                                    }
                                    break;
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

                        if (this.isCancelled() || Thread.currentThread().isInterrupted()) {
                            return null;
                        }

                        ArrayList<Similarity> similarities = new ArrayList<>();
                        for (int i = 0; i < particles.size(); i++) {
                            similarities.add(new Similarity(particles.get(sortedIndices[i]) == currentParticle, sortedIndices[i], scores.get(sortedIndices[i]), values.get(sortedIndices[i])));
                        }
                        if (this.isCancelled() || Thread.currentThread().isInterrupted()) {
                            return null;
                        }
                        return similarities;
                    }
                };
                return task;
            }
        };
        return service;
    }
}
