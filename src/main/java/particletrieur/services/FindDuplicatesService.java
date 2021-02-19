package particletrieur.services;

import particletrieur.controls.BasicDialogs;
import particletrieur.helpers.Metrics;
import particletrieur.models.project.Particle;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.opencv.core.Mat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindDuplicatesService {

    public static Service findMissing(List<Particle> particles) {
        Service<List<Particle>> service = new Service<List<Particle>>() {
            @Override
            protected Task<List<Particle>> createTask() {
                return new Task<List<Particle>>() {
                    @Override
                    protected List<Particle> call() throws InterruptedException {
                        updateMessage("Tagging missing images...");
                        ArrayList<Particle> missing = new ArrayList<>();
                        int idx = 0;
                        for (Particle foram : particles) {
                            if (this.isCancelled()) return null;
                            if (!foram.getFile().exists()) missing.add(foram);
                            idx++;
                            updateMessage(String.format("%d/%d images checked\n%d missing", idx, particles.size(), missing.size()));
                        }
                        return missing;
                    }

                    ;
                };
            }
        };
        return service;
    }

    public static Service findDuplicatesUsingHashCode(List<Particle> particles) {
        Service<List<Particle>> service = new Service<List<Particle>>() {
            @Override
            protected Task<List<Particle>> createTask() {
                return new Task<List<Particle>>() {
                    @Override
                    protected List<Particle> call() throws InterruptedException {
                        updateMessage("Tagging duplicates...");
                        MessageDigest md;
                        try {
                            md = MessageDigest.getInstance("SHA-256");
                        } catch (NoSuchAlgorithmException ex) {
                            BasicDialogs.ShowException("The hashing algorithm could not be found.", ex);
                            return null;
                        }

                        int numForams = particles.size();
                        ArrayList<byte[]> hashes = new ArrayList<>(numForams);
                        ArrayList<Particle> toRemove = new ArrayList<>();

                        int idx = 0;
                        int idx1 = 0;
                        for (Particle foram : particles) {
                            if (this.isCancelled()) return null;
                            Mat mat = foram.getMat();
                            if (mat != null) {
                                int size = (int) (mat.total() * mat.channels());
                                byte[] buffer = new byte[size];
                                mat.get(0, 0, buffer);
                                hashes.add(md.digest(buffer));
                                mat.release();
                            }
                            else {
                                hashes.add(md.digest(foram.getFilename().getBytes()));
                            }
                            idx++;
                            idx1++;
                            updateProgress(idx, 2 * numForams - 1);
                            updateMessage(String.format("%d/%d images processed", idx1, numForams));
                        }

                        int idx2 = 0;
                        for (int i = numForams - 1; i > 0; i--) {
                            if (this.isCancelled()) return null;
                            byte[] hashOne = hashes.get(i);
                            for (int j = i - 1; j >= 0; j--) {
                                byte[] hashTwo = hashes.get(j);
                                if (Arrays.equals(hashOne, hashTwo)) {
                                    toRemove.add(particles.get(i));
                                    break;
                                }
                            }
                            idx++;
                            idx2++;
                            updateMessage(String.format("%d/%d images checked\n%d duplicates", idx2 + 1, numForams, toRemove.size()));
                            updateProgress(idx, 2 * numForams - 1);
                        }
                        //updateMessage("Complete");
                        return toRemove;
                    }

                    ;
                };
            }
        };
        return service;
    }

    public static Service findDuplicatesUsingHashCode(List<Particle> particles, List<Particle> foramsAll) {
        Service<List<Particle>> service = new Service<List<Particle>>() {
            @Override
            protected Task<List<Particle>> createTask() {
                return new Task<List<Particle>>() {
                    @Override
                    protected List<Particle> call() throws InterruptedException {
                        updateMessage("Processing...");
                        //Init
                        MessageDigest md;
                        try {
                            md = MessageDigest.getInstance("SHA-256");
                        } catch (NoSuchAlgorithmException ex) {
                            BasicDialogs.ShowException("The hashing algorithm could not be found.", ex);
                            return null;
                        }
                        int numForams = particles.size();
                        ArrayList<byte[]> hashes = new ArrayList<>(numForams);

                        int numForamsAll = foramsAll.size();
                        ArrayList<byte[]> hashesAll = new ArrayList<>(numForamsAll);

                        ArrayList<Particle> toRemove = new ArrayList<>();

                        //Create hashes - subset for removal
                        int idx = 0;
                        int idx1 = 0;
                        for (Particle foram : particles) {
                            if (this.isCancelled()) return null;
                            Mat mat = foram.getMat();
                            if (mat != null) {
                                int size = (int) (mat.total() * mat.channels());
                                byte[] buffer = new byte[size];
                                mat.get(0, 0, buffer);
                                byte[] hash = md.digest(buffer);
                                hashes.add(hash);
                                mat.release();
                            }
                            else {
                                hashes.add(md.digest(foram.getFilename().getBytes()));
                            }
                            idx++;
                            idx1++;
                            updateMessage(String.format("%d/%d images processed", idx1, numForams));
                            updateProgress(idx, 2 * numForams + numForamsAll);
                        }

                        //Create hashes - entire set
                        int idx2 = 0;
                        for (Particle foram : foramsAll) {
                            if (this.isCancelled()) return null;
                            Mat mat = foram.getMat();
                            if (mat != null) {
                                int size = (int) (mat.total() * mat.channels());
                                byte[] buffer = new byte[size];
                                mat.get(0, 0, buffer);
                                byte[] hash = md.digest(buffer);
                                hashesAll.add(hash);
                                mat.release();
                            }
                            else {
                                hashesAll.add(md.digest(foram.getFilename().getBytes()));
                            }
                            idx++;
                            idx1++;
                            updateMessage(String.format("%d/%d images processed", idx2, numForamsAll));
                            updateProgress(idx, 2 * numForams + numForamsAll);
                        }

                        //Compare each hash
                        //Loop through subset
                        int idx3 = 0;
                        for (int i = 0; i < numForams; i++) {
                            if (this.isCancelled()) return null;
                            byte[] hashOne = hashes.get(i);
                            Particle forami = particles.get(i);
                            //Loop through entire set
                            for (int j = 0; j < numForamsAll; j++) {
                                Particle foramj = foramsAll.get(j);
                                //If same object, go to next. Also, skip if compared object is also a duplicate, otherwise both will be labeled.
                                if (forami == foramj || toRemove.contains(foramj)) continue;
                                byte[] hashTwo = hashesAll.get(j);
                                if (Arrays.equals(hashOne, hashTwo)) {
                                    toRemove.add(particles.get(i));
                                    break;
                                }
                            }
                            idx++;
                            idx3++;
                            updateMessage(String.format("%d/%d images checked\n%d duplicates", idx3, numForams, toRemove.size()));
                            updateProgress(idx, 2 * numForams + numForamsAll);
                        }
                        //updateMessage("Complete");
                        return toRemove;
                    }
                };
            }
        };
        return service;
    }

    public static Service findDuplicatesUsingVector(List<Particle> particles, double threshold) {
        Service<List<Particle>> service = new Service<List<Particle>>() {
            @Override
            protected Task<List<Particle>> createTask() {
                return new Task<List<Particle>>() {
                    @Override
                    protected List<Particle> call() throws InterruptedException {
                        updateMessage("Processing...");
                        int numForams = particles.size();
                        ArrayList<Particle> toRemove = new ArrayList<>();
                        for (int i = numForams - 1; i > 0; i--) {
                            if (this.isCancelled()) return null;
                            Particle forami = particles.get(i);
                            if (forami.getCNNVector() == null) continue;
                            for (int j = i - 1; j >= 0; j--) {
                                Particle foramj = particles.get(j);
                                if (foramj.getCNNVector() == null) continue;
                                double score = Metrics.vectorCosineSimilarity(forami.getCNNVector(), foramj.getCNNVector());
                                if (score > threshold) {
                                    toRemove.add(forami);
                                    break;
                                }
                            }
                            updateProgress(numForams - i + 1, numForams);
                            updateMessage(String.format("%d/%d images checked\n%d tagged", numForams - i + 1, numForams, toRemove.size()));
                        }
                        //updateMessage("Complete");
                        return toRemove;
                    }

                    ;
                };
            }
        };
        return service;
    }

    public static Service findDuplicatesUsingVector(List<Particle> particles, List<Particle> foramsAll, double threshold) {
        Service<List<Particle>> service = new Service<List<Particle>>() {
            @Override
            protected Task<List<Particle>> createTask() {
                return new Task<List<Particle>>() {
                    @Override
                    protected List<Particle> call() throws InterruptedException {
                        updateMessage("Processing...");
                        int numForams = particles.size();
                        int numForamsAll = foramsAll.size();
                        ArrayList<Particle> toRemove = new ArrayList<>();
                        for (int i = 0; i < numForams; i++) {
                            if (this.isCancelled()) return null;
                            Particle forami = particles.get(i);
                            if (forami.getCNNVector() == null) continue;
                            for (int j = 0; j < numForamsAll; j++) {
                                Particle foramj = foramsAll.get(j);
                                if (forami == foramj || toRemove.contains(foramj) || foramj.getCNNVector() == null)
                                    continue;
                                double score = Metrics.vectorCosineSimilarity(forami.getCNNVector(), foramj.getCNNVector());
                                if (score > threshold) {
                                    toRemove.add(forami);
                                    break;
                                }
                            }
                            updateProgress(i + 1, numForams);
                            updateMessage(String.format("%d/%d images checked\n%d tagged", i + 1, numForams, toRemove.size()));
                        }
                        //updateMessage("Complete");
                        return toRemove;
                    }
                };
            }
        };
        return service;
    }
}
