/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.models.tools;

import javafx.application.Platform;
import main.java.app.models.Supervisor;
import main.java.app.models.network.classification.Classification;
import main.java.app.models.network.classification.ClassificationSet;
import main.java.app.models.network.classification.NetworkLabel;
import main.java.app.models.processing.Morphology;
import main.java.app.models.processing.ParticleImage;
import main.java.app.models.processing.processors.MorphologyProcessor;
import main.java.app.models.project.Particle;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import main.java.app.services.ImageProcessingService;
import main.java.app.services.network.CNNPredictionService;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.opencv.core.Mat;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ClassificationServer {

    HttpServer server;

    private ReadOnlyBooleanWrapper isRunning = new ReadOnlyBooleanWrapper(false);

    public ReadOnlyBooleanProperty isRunningProperty() {
        return isRunning.getReadOnlyProperty();
    }

    public boolean getIsRunning() {
        return isRunning.get();
    }

    private void setIsRunning(boolean value) {
        isRunning.set(value);
    }

    Supervisor supervisor;

    public ClassificationServer(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/image", imageHandler());
        server.createContext("/file", filenameHandler());
        server.createContext("/info", informationHandler());

        server.setExecutor(null);
        server.start();
        setIsRunning(true);
        System.out.println("Server has started");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server has stopped");
        }
        isRunning.set(false);
    }

    private HttpHandler imageHandler() {
        HttpHandler handler = (he) -> {
            String type = he.getRequestMethod();
            System.out.println(type + " received");
            String response;
            if (type.equals("GET")) {
                response = "<!doctype html>\n" +
                        "    <title>Import file</title>\n" +
                        "    <h1>Import and classify a particle image</h1>\n" +
                        "    <p>Upload a file to have it saved in the project directory and and classified:</p>\n" +
                        "    <form method=post enctype=multipart/form-data>\n" +
                        "        Sample:</br>\n<input type=text name=sample>\n</br>\n" +
                        "        Index 1:</br>\n<input type=text name=index1>\n</br>\n" +
                        "        Index 2:</br>\n<input type=text name=index2>\n</br>\n" +
                        "        Resolution (pixels/mm):</br>\n<input type=text name=resolution>\n</br>\n" +
                        "        <input type=\"checkbox\" name=\"morphology\" value=\"true\"> Calculate morphology\n</br>\n" +
                        "        Image:</br>\n<input type=file name=file>\n</br>\n" +
                        "        </br>\n<input type=submit value=Upload>\n" +
                        "    </form>";
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if (type.equals("POST")) {

                for (Entry<String, List<String>> header : he.getRequestHeaders().entrySet()) {
                    System.out.println(header.getKey() + ": " + header.getValue().get(0));
                }

                try {
                    ServletFileUpload upload = new ServletFileUpload();
                    RequestContext context = new RequestContext() {
                        @Override
                        public String getCharacterEncoding() {
                            return "UTF-8";
                        }

                        @Override
                        public int getContentLength() {
                            return 0; //tested to work with 0 as return
                        }

                        @Override
                        public String getContentType() {
                            return he.getRequestHeaders().getFirst("Content-type");
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            return he.getRequestBody();
                        }
                    };
                    if (!ServletFileUpload.isMultipartContent(context)) {
                        response = "{\"error\":\"does not contain multipart content\"}";
                    } else {

                        // Parse the request
                        FileItemIterator iter = upload.getItemIterator(context);
                        Particle image = null;
                        String sample = null;
                        double index1 = 0;
                        double index2 = 0;
                        double resolution = 0;
                        boolean morphology = false;
                        while (iter.hasNext()) {
                            FileItemStream item = iter.next();
                            String name = item.getFieldName();
                            InputStream stream = item.openStream();
                            if (item.isFormField()) {
                                System.out.println("Form field " + name + " detected.");
                                if (name.equals("sample")) sample = Streams.asString(stream);
                                else if (name.equals("index1")) index1 = parseDouble(Streams.asString(stream), 0);
                                else if (name.equals("index2")) index2 = parseDouble(Streams.asString(stream), 0);
                                else if (name.equals("resolution"))
                                    resolution = parseDouble(Streams.asString(stream), 0);
                                else if (name.equals("morphology")) {
                                    morphology = Streams.asString(stream).equals("true");
                                }
                            } else {
                                System.out.println("File field " + name + " with file name "
                                        + item.getName() + " detected.");
                                InputStream fileStream = item.openStream();
                                image = createParticleFromStream(item.getName(), fileStream);
                            }
                        }
                        if (image == null) {
                            response = "{\"error\":\"a new project must be saved before import\"}";
                        }
                        else {
                            Optional<Classification> result = importAndClassify(image, sample, index1, index2, resolution);
                            image.resolutionProperty().set(resolution); //Hack as we normally have to set this on another thread
                            if (morphology) {
                                if (result.isPresent()) response = String.format("{\"code\":\"%s\", \"score\":%.2f, ", result.get().getCode(), result.get().getValue());
                                else response = "{";

                                Morphology morphologyPixels = calculateMorphology(image);
                                Morphology morphologyMM = morphologyPixels.convertToMM(image.getResolution());

                                response += String.format("\"areaPX\": %.1f, ", morphologyPixels.area);
                                response += String.format("\"areaMM\": %.5f, ", morphologyMM.area);
                                response += String.format("\"convexAreaPX\": %.1f, ", morphologyPixels.convexArea);
                                response += String.format("\"convexAreaMM\": %.5f, ", morphologyMM.convexArea);
                                response += String.format("\"diameterPX\": %.1f, ", morphologyPixels.meanDiameter);
                                response += String.format("\"diameterMM\": %.3f, ", morphologyMM.meanDiameter);
                                response += String.format("\"majorAxisPX\": %.1f, ", morphologyPixels.majorAxisLength);
                                response += String.format("\"majorAxisMM\": %.3f, ", morphologyMM.majorAxisLength);
                                response += String.format("\"minorAxisPX\": %.1f, ", morphologyPixels.minorAxisLength);
                                response += String.format("\"minorAxisMM\": %.3f, ", morphologyMM.minorAxisLength);
                                response += String.format("\"circularity\": %.3f, ", morphologyPixels.circularity);
                                response += String.format("\"roundness\": %.3f, ", morphologyPixels.roundness);
                                response += String.format("\"solidity\": %.3f, ", morphologyPixels.solidity);
                                response += String.format("\"eccentricity\": %.3f,", morphologyPixels.eccentricity);
                                response += String.format("\"mean\": %.3f,", morphologyPixels.mean);
                                response += String.format("\"stddev\": %.3f,", morphologyPixels.stddev);
                                response += String.format("\"stddevInvariant\": %.3f,", morphologyPixels.stddevInvariant);
                                response += String.format("\"skew\": %.3f,", morphologyPixels.skew);
                                response += String.format("\"kurtosis\": %.3f,", morphologyPixels.kurtosis);
                                response += String.format("\"moment5\": %.3f,", morphologyPixels.moment5);
                                response += String.format("\"moment6\": %.3f", morphologyPixels.moment6);
                                response += "}";
                            }
                            else {
                                if (result.isPresent()) {
                                    response = String.format("{\"code\":\"%s\", \"score\":%.2f}", result.get().getCode(), result.get().getValue());
                                } else {
                                    response = "{\"error\":\"classification failed\"}";
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    response = "{\"error\":\"" + e.getMessage() + "\"}";
                }
                System.out.println("the response will be " + response);
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if (!type.equals("HEAD")) {
                he.sendResponseHeaders(405, 0);
            }
        };
        return handler;
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private HttpHandler informationHandler() {
        HttpHandler handler = (he) -> {
            String type = he.getRequestMethod();
            System.out.println(type + " received");
            if (type.equals("GET")) {
                JSONObject jsonObject = new JSONObject();
                if (!supervisor.network.isEnabled()) {
                    jsonObject.put("error", "CNN network is not present");
                } else {
                    jsonObject.put("name", supervisor.network.getNetworkInfo().name);
                    jsonObject.put("description", supervisor.network.getNetworkInfo().description);
                    jsonObject.put("source", supervisor.network.getNetworkInfo().source_data);
                    JSONArray jsonLabels = new JSONArray();
                    for (NetworkLabel label : supervisor.network.getNetworkInfo().labels) {
                        jsonLabels.add(label.code);
                    }
                    jsonObject.put("labels", jsonLabels);
                }
                String response = jsonObject.toJSONString();
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if (!type.equals("HEAD")) {
                he.sendResponseHeaders(405, 0);
            }
        };
        return handler;
    }

    private HttpHandler filenameHandler() {
        HttpHandler handler = (he) -> {
            String type = he.getRequestMethod();
            System.out.println(type + " received");
            //TODO handle case where nothing received - send response
            if (type.equals("GET")) {

                String response;

                List<NameValuePair> params = URLEncodedUtils.parse(he.getRequestURI(), Charset.forName("UTF-8"));
                if (params.size() == 0) {
                    response = "<!doctype html>\n" +
                            "    <title>Import file</title>\n" +
                            "    <h1>Import and classify a particle image</h1>\n" +
                            "    <p>Enter the full file path to import the image and classify it</p>\n" +
                            "    <form method=\"GET\">\n" +
                            "        File path:</br>\n<input type=text name=filename>\n</br>\n" +
                            "        Sample:</br>\n<input type=text name=sample>\n</br>\n" +
                            "        Index 1:</br>\n<input type=text name=index1>\n</br>\n" +
                            "        Index 2:</br>\n<input type=text name=index2>\n</br>\n" +
                            "        Resolution (pixels/mm):</br>\n<input type=text name=resolution>\n" +
                            "        <input type=\"checkbox\" name=\"morphology\" value=\"true\"> Calculate morphology\n</br>\n" +
                            "        </br>\n<input type=submit value=Upload>\n" +
                            "    </form>";
                } else {
                    String filename = null;
                    String sample = "unknown";
                    double index1 = 0;
                    double index2 = 0;
                    double resolution = 0;
                    boolean morphology = false;
                    for (NameValuePair param : params) {
                        if (param.getName().equals("filename")) filename = param.getValue();
                        else if (param.getName().equals("sample")) sample = param.getValue();
                        else if (param.getName().equals("index1")) index1 = parseDouble(param.getValue(), 0);
                        else if (param.getName().equals("index2")) index2 = parseDouble(param.getValue(), 0);
                        else if (param.getName().equals("resolution")) resolution = parseDouble(param.getValue(), 0);
                        else if (param.getName().equals("morphology")) morphology = param.getValue().equals("true");
                    }

                    if (filename == null) {
                        response = "{\"error\":\"filename not present\"}";
                    }
                    else {
                        Optional<Particle> particleResult = particleFromFile(filename);
                        if (!particleResult.isPresent()) response = "{\"error\":\"file not found\"}";
                        else {
                            Particle image = particleResult.get();
                            image.resolutionProperty().set(resolution); //Hack as we normally have to set this on another thread
                            Optional<Classification> result = importAndClassify(image, sample, index1, index2, resolution);
                            if (morphology) {
                                if (result.isPresent())
                                    response = String.format("{\"code\":\"%s\", \"score\":%.2f, ", result.get().getCode(), result.get().getValue());
                                else response = "{";

                                Morphology morphologyPixels = calculateMorphology(image);
                                Morphology morphologyMM = morphologyPixels.convertToMM(image.getResolution());

                                response += String.format("\"areaPX\": %.1f, ", morphologyPixels.area);
                                response += String.format("\"areaMM\": %.5f, ", morphologyMM.area);
                                response += String.format("\"convexAreaPX\": %.1f, ", morphologyPixels.convexArea);
                                response += String.format("\"convexAreaMM\": %.5f, ", morphologyMM.convexArea);
                                response += String.format("\"diameterPX\": %.1f, ", morphologyPixels.meanDiameter);
                                response += String.format("\"diameterMM\": %.3f, ", morphologyMM.meanDiameter);
                                response += String.format("\"majorAxisPX\": %.1f, ", morphologyPixels.majorAxisLength);
                                response += String.format("\"majorAxisMM\": %.3f, ", morphologyMM.majorAxisLength);
                                response += String.format("\"minorAxisPX\": %.1f, ", morphologyPixels.minorAxisLength);
                                response += String.format("\"minorAxisMM\": %.3f, ", morphologyMM.minorAxisLength);
                                response += String.format("\"circularity\": %.3f, ", morphologyPixels.circularity);
                                response += String.format("\"roundness\": %.3f, ", morphologyPixels.roundness);
                                response += String.format("\"solidity\": %.3f, ", morphologyPixels.solidity);
                                response += String.format("\"eccentricity\": %.3f,", morphologyPixels.eccentricity);
                                response += String.format("\"mean\": %.3f,", morphologyPixels.mean);
                                response += String.format("\"stddev\": %.3f,", morphologyPixels.stddev);
                                response += String.format("\"stddevInvariant\": %.3f,", morphologyPixels.stddevInvariant);
                                response += String.format("\"skew\": %.3f,", morphologyPixels.skew);
                                response += String.format("\"kurtosis\": %.3f,", morphologyPixels.kurtosis);
                                response += String.format("\"moment5\": %.3f,", morphologyPixels.moment5);
                                response += String.format("\"moment6\": %.3f", morphologyPixels.moment6);
                                response += "}";
                            } else {
                                if (result.isPresent()) {
                                    response = String.format("{\"code\":\"%s\", \"score\":%.2f}", result.get().getCode(), result.get().getValue());
                                } else {
                                    response = "{\"error\":\"classification failed\"}";
                                }
                            }
                        }
                    }
                }
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if (!type.equals("HEAD")) {
                he.sendResponseHeaders(405, 0);
            }
        };
        return handler;
    }

    public Optional<Particle> particleFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) return Optional.empty();
        Particle particle = new Particle(new File(filename));
        return Optional.of(particle);
    }

    public Optional<Classification> importAndClassify(Particle particle, String sample, double index1, double index2, double resolution) {
        if (particle == null) return Optional.empty();

        Platform.runLater(() -> {
            supervisor.project.addParticle(particle);
            supervisor.project.setParticleMetadata(particle, sample, index1, index2, resolution);
        });

        if (supervisor.network.isEnabled()) {
            CNNPredictionService cnnPredictionService = new CNNPredictionService(supervisor);
            ClassificationSet result = cnnPredictionService.predict(particle, supervisor.project.processingInfo.getProcessBeforeClassification());

            Platform.runLater(() -> {
                supervisor.project.setParticleLabelSet(particle, result);
            });

            return Optional.of(result.getBest());
        }
        return Optional.empty();
    }

    public Morphology calculateMorphology(Particle particle) {
        ImageProcessingService imageProcessingService = new ImageProcessingService(supervisor.FCNNSegmenterService);
        Mat mat = particle.getMat();
        if (mat != null) {
            ParticleImage image = imageProcessingService.process(mat, supervisor.project.processingInfo);
            Morphology m = MorphologyProcessor.calculateMorphology(image);
            mat.release();
            return m;
        }
        else {
            return new Morphology();
        }
    }

    public Particle createParticleFromStream(String filename, InputStream stream) throws IOException {
        if (supervisor.project.getFile() != null) {
            File parentDirectory = supervisor.project.getFile().getParentFile();
            File imagesDirectory = Paths.get(parentDirectory.getAbsolutePath(), "images").toFile();
            FileUtils.forceMkdir(imagesDirectory);
            File outputFile = Paths.get(imagesDirectory.getAbsolutePath(), filename).toFile();
            Files.copy(stream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Particle particle = new Particle(outputFile, supervisor.project.UNLABELED_CODE, "");
            return particle;
        }
        return null;
    }


//    public Optional<Boolean> presenceServerFile(FileItemStream item) {
////        try {
////            InputStream stream = item.openStream();
////            byte[] temporaryImageInMemory = readStream(stream);
////            Mat outputImage = Imgcodecs.imdecode(new MatOfByte(temporaryImageInMemory), Imgcodecs.IMREAD_UNCHANGED);
////            Morphology_old morph = new Morphology_old();
////            try {
////                ProcessedImage processedImage = new ProcessedImage();
////                return Optional.of(morph.isParticlePresent(outputImage, 0.1, processedImage, supervisor.project.processingInfo));
////            }
////            catch (ProcessingException ex ){
////                return Optional.of(false);
////            }
////        }
////        catch (IOException ex) {
//////            BasicDialogs.ShowException("An image was received by the server, but could not be imported into the app.", ex);
////        }
//        return Optional.empty();
//    }

    private static byte[] readStream(InputStream stream) throws IOException {
        // Copy content of the image to byte-array
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] temporaryImageInMemory = buffer.toByteArray();
        buffer.close();
        stream.close();
        return temporaryImageInMemory;
    }
}
