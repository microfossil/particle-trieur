package particletrieur.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;

public class ParametersFromTXTService {
    public static Service<LinkedHashMap<String, LinkedHashMap<String, String>>> getParametersFromTextFile(File txtFile) {
        return new Service<LinkedHashMap<String,LinkedHashMap<String, String>>>() {
            @Override
            protected Task<LinkedHashMap<String,LinkedHashMap<String, String>>> createTask() {
                return new Task<LinkedHashMap<String,LinkedHashMap<String, String>>>() {
                    @Override
                    protected LinkedHashMap<String,LinkedHashMap<String, String>> call() throws Exception {
                        Scanner sc = new Scanner(txtFile);

                        LinkedHashMap<String,LinkedHashMap<String, String>> data = new LinkedHashMap<>();

                        int count = 0;
                        List<String> headerNames = new ArrayList<>();

                        while(sc.hasNextLine())
                        {
                            String line = sc.nextLine();
                            line = line.replaceAll(",",".");
                            if (line.trim().isEmpty()) continue; // skip blank lines.
                            String[] parts = line.split("\t");

                            if(count == 0) // headline or 1st line of txt file
                            {
                                headerNames.addAll(Arrays.asList(parts));
                                if(headerNames.contains("Label"))
                                {
                                    headerNames.set(headerNames.indexOf("Label"), "Label(in File)");
                                }
                            }
                            else
                            {
                                List<String> record = new ArrayList<>(parts.length);
                                record.addAll(Arrays.asList(parts));

                                String filename = record.get(headerNames.indexOf("Label(in File)")) + "_" + record.get(0) + ".jpg";

                                LinkedHashMap<String, String> payload = new LinkedHashMap<>();

                                for (String headerName : headerNames) {
                                    if (headerName.equals(" ")) continue;
                                    if (headerName.equals("Label(in File)")) continue;
                                    payload.put(headerName.toLowerCase(), record.get(headerNames.indexOf(headerName)));
                                }
                                data.put(filename, payload);
                                updateMessage(String.format("%d images found", count));
                            }
                            count++;
                        }
                        return data;
                    }
                };
            }
        };
    }
}
