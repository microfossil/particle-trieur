package main.java.app.services;

import main.java.app.models.project.Project;
import main.java.app.models.project.Particle;
import main.java.app.viewcontrollers.particle.EditParticleMetadataViewController;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExtractMetadataFromFilenamesService {

    public static Service<List<MetadataUpdatePayload>> parseFilenamesAndUpdate(List<Particle> particles, Project project, EditParticleMetadataViewController extractDialog) {

        ArrayList<MetadataUpdatePayload> metadata = new ArrayList<>();

        Service<List<MetadataUpdatePayload>> service = new Service<List<MetadataUpdatePayload>>() {
            @Override
            protected Task<List<MetadataUpdatePayload>> createTask() {
                return new Task<List<MetadataUpdatePayload>>() {
                    @Override
                    protected List<MetadataUpdatePayload> call() throws InterruptedException, Project.TaxonAlreadyExistsException {
                        updateMessage("Parsing filenames...");
                        //Parse files
                        int idx = 0;
                        int total = particles.size();
                        for (Particle particle : particles) {
                            if (this.isCancelled()) return null;

                            MetadataUpdatePayload payload = new MetadataUpdatePayload(particle);

                            //TODO: This logic needs to be somewhere else!!!!! nah all good
                            if (true) {
                                HashMap<String, String> parts = extractDialog.parseFile(particle.getFile());
                                //Class
                                if (parts.containsKey("label")) {
                                    String code = parts.get("label");
                                    payload.label = code;
                                }
                                //Depths
//                                Double index1 = null;
//                                Double index2 = null;
                                if (parts.containsKey("index1")) {
                                    try {
                                        payload.index1 = Double.parseDouble(parts.get("index1"));
                                    } catch (NumberFormatException ex) {

                                    }
                                }
                                if (parts.containsKey("index2")) {
                                    try {
                                        payload.index2 = Double.parseDouble(parts.get("index2"));
                                    } catch (NumberFormatException ex) {
                                    }
                                }

                                //CoreID
                                if (parts.containsKey("sample")) {
                                    payload.sample = parts.get("sample");
                                }
                                //GUID
                                if (parts.containsKey("GUID")) {
                                    payload.guid = parts.get("GUID");
                                }
                                metadata.add(payload);
                            }
                            idx++;
                            updateMessage(String.format("%d/%d particles updated", idx, total));
                            updateProgress(idx, total);
                        }
                        return metadata;
                    }
                };
            }
        };
        return service;
    }

    public static class MetadataUpdatePayload {

        public Particle particle;

        public String sample = null;
        public String label = null;
        public String guid = null;
        public Double index1 = null;
        public Double index2 = null;

        public MetadataUpdatePayload(Particle particle) {
            this.particle = particle;
        }
    }
}
