package main.java.app.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Pattern;

public class UpdateCheckService {

    public static Service<String> getVersion() {

        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                Task<String> task = new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        URL url;
                        InputStream is = null;
                        BufferedReader br;
                        String line;

                        Pattern p = Pattern.compile("Download \\w* .* for");   // the pattern to search for

                        try {
//                            url = new URL("https://particle-classification.readthedocs.io/en/latest/");
                            url = new URL("https://api.github.com/repos/microfossil/particle-trieur/releases");

                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.connect();

//Getting the response code
                            int responsecode = conn.getResponseCode();


                            String inline = "";
                            Scanner scanner = new Scanner(url.openStream());

                            //Write all the JSON data into a string using a scanner
                            while (scanner.hasNext()) {
                                inline += scanner.nextLine();
                            }

                            //Close the scanner
                            scanner.close();

                            //Using the JSON simple library parse the string into a json object
                            JSONParser parse = new JSONParser();
                            JSONArray arr = (JSONArray) parse.parse(inline);
                            JSONObject latest_release = (JSONObject) arr.get(0);
                            String latest_version = (String) latest_release.get("tag_name");
                            System.out.println("Latest version: " + latest_version);
                            return latest_version;
                        } catch (MalformedURLException mue) {
                            mue.printStackTrace();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        } finally {
                            try {
                                if (is != null) is.close();
                            } catch (IOException ioe) {
                                // nothing to see here
                            }
                        }
                        return null;
                    }
                };
                return task;
            }
        };
        return service;
    }
}
