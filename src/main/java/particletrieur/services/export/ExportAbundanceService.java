package particletrieur.services.export;

import particletrieur.models.project.Project;
import particletrieur.services.StatisticsService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExportAbundanceService {

    public static Service exportAbundance(
            Project project,
            File file) {
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException, IOException {

                        updateMessage("Exporting...");

                        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

                        StatisticsService stats = new StatisticsService(project);
                        LinkedHashMap<Double, LinkedHashMap<String, Double>> counts = stats.indexByLabel(1, false);

                        int idx = 0;

                        //Iterate through each class
                        boolean firstEntry = true;
                        for (Map.Entry depthEntry : counts.entrySet()) {
                            double depthCode = (double) depthEntry.getKey();
                            LinkedHashMap<String, Double> classMap = (LinkedHashMap<String, Double>) depthEntry.getValue();

                            //Header
                            if (firstEntry) {
                                writer.write("index1");
                                for (Map.Entry classEntry : classMap.entrySet()) {
                                    String taxonCode = (String) classEntry.getKey();
                                    writer.write(String.format(",%s", taxonCode));
                                }
                                writer.write("\n");
                                firstEntry = false;
                            }
                            //Class values
                            writer.write(String.format("%.0f", depthCode));
                            for (Map.Entry classEntry : classMap.entrySet()) {
                                double count = (double) classEntry.getValue();
                                writer.write(String.format(",%.0f", count));
                            }
                            writer.write("\n");
                            idx++;
                            updateMessage(String.format("%d/%d rows exported", idx, counts.keySet().size()));
                            updateProgress(idx, counts.keySet().size());
                        }
                        writer.close();
                        return null;
                    }
                };
            }
        };
        return service;
    }

    public static Service exportCounts(
            Project project,
            File file) {
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException, IOException {

                        updateMessage("Exporting...");

                        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

                        StatisticsService stats = new StatisticsService(project);
                        LinkedHashMap<String, LinkedHashMap<String, Double>> counts = stats.sampleByLabel(false);

                        int idx = 0;

                        //Iterate through each class
                        boolean firstEntry = true;
                        for (Map.Entry depthEntry : counts.entrySet()) {
                            String depthCode = (String) depthEntry.getKey();
                            LinkedHashMap<String, Double> classMap = (LinkedHashMap<String, Double>) depthEntry.getValue();

                            //Header
                            if (firstEntry) {
                                writer.write("sample");
                                for (Map.Entry classEntry : classMap.entrySet()) {
                                    String taxonCode = (String) classEntry.getKey();
                                    writer.write(String.format(",%s", taxonCode));
                                }
                                writer.write("\n");
                                firstEntry = false;
                            }
                            //Class values
                            writer.write(String.format("%s", depthCode));
                            for (Map.Entry classEntry : classMap.entrySet()) {
                                double count = (double) classEntry.getValue();
                                writer.write(String.format(",%.0f", count));
                            }
                            writer.write("\n");
                            idx++;
                            updateMessage(String.format("%d/%d samples exported", idx, counts.keySet().size()));
                            updateProgress(idx, counts.keySet().size());
                        }
                        writer.close();
                        return null;
                    }
                };
            }
        };
        return service;
    }
}
