package particletrieur.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import particletrieur.models.Supervisor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.List;

public class ParametersFromCSVService {

    public static Service<LinkedHashMap<String,LinkedHashMap<String, String>>> getParametersFromCSV(File csvFile) {
        return new Service<LinkedHashMap<String,LinkedHashMap<String, String>>>() {
            @Override
            protected Task<LinkedHashMap<String,LinkedHashMap<String, String>>> createTask() {
                return new Task<LinkedHashMap<String,LinkedHashMap<String, String>>>() {
                    @Override
                    protected LinkedHashMap<String,LinkedHashMap<String, String>> call() throws Exception {
                        CSVParser parser = CSVParser.parse(csvFile, StandardCharsets.UTF_8, CSVFormat.RFC4180.withAllowMissingColumnNames().withFirstRecordAsHeader());

                        LinkedHashMap<String,LinkedHashMap<String, String>> data = new LinkedHashMap<>();
                        List<String> headerNames = parser.getHeaderNames();
                        for (String s : parser.getHeaderNames()) {
                            System.out.println(s);
                        }

                        String filenameHeader;
                        if (headerNames.contains("filename")) filenameHeader = "filename";
                        else if (headerNames.contains("file")) filenameHeader = "file";
                        else if (headerNames.contains("fichier")) filenameHeader = "fichier";
                        else throw new InputMismatchException("The CSV file must contain a column named filename, file or dossier.");

                        int count = 0;
                        for (CSVRecord record : parser) {
                            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
                            String filename = record.get(filenameHeader);
                            for (String headerName : headerNames) {
                                if (headerName.equals("")) continue;
                                payload.put(headerName.toLowerCase(), record.get(headerName));
                            }
                            data.put(filename, payload);
                            count++;
                            updateMessage(String.format("%d images found", count));
                        }
                        return data;
                    }
                };
            }
        };
    }
}
