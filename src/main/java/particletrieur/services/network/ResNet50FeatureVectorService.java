package particletrieur.services.network;

import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.models.network.classification.TensorflowNetwork;
import particletrieur.models.network.classification.NetworkInfo;
import particletrieur.models.network.classification.TensorInfo;
import particletrieur.models.processing.ImageType;
import particletrieur.models.processing.ParticleImage;
import particletrieur.models.processing.processors.Preprocessor;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Particle;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import particletrieur.xml.FloatBufferMapAdapter;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;

import java.io.*;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;

public class ResNet50FeatureVectorService {

    public TensorflowNetwork tensorflowNetwork;
    public boolean isRecalculate = false;

    public ResNet50FeatureVectorService() {

        NetworkInfo info = new NetworkInfo();
        info.name = "feature_vector";
        info.description = "Creates a feature vector from the image";
        info.protobuf =  "/trained_networks/feature_vector/frozen_model.pb";
        info.type = "vector";
        TensorInfo input = new TensorInfo();
        input.name = "image";
        input.operation = "input_1";
        input.height = 224;
        input.width = 224;
        input.channels = 3;
        TensorInfo output = new TensorInfo();
        output.name = "vector";
        output.operation = "global_average_pooling2d/Mean";
        info.inputs.add(input);
        info.outputs.add(output);
        info.isResource = true;

        tensorflowNetwork = new TensorflowNetwork();
        tensorflowNetwork.setNetworkInfo(info);
        if (!tensorflowNetwork.setup()) {
            BasicDialogs.ShowError("Network error",
                    "Cannot start ResNet50 feature vector network.\nThe tensorflow graph file does not exist, or the network XML file was an old version.");
        }
    }

    public float[] predict(Mat mat) {
        TensorInfo info = tensorflowNetwork.getNetworkInfo().inputs.get(0);
        ParticleImage image = ParticleImage.create(mat, ImageType.LIGHTONDARK, false);
        image.normaliseMinMax(0, 255);
        image.makeSquare();
        Mat input = Preprocessor.resize(image.workingImage, info.height, info.width, info.channels);
//        input.convertTo(input, CvType.CV_32F);
//        Core.divide(input, Scalar.all(255), input);
        float[][] vector = tensorflowNetwork.predictLabel(input, "image", "vector");
        input.release();
        image.release();
        return vector[0];
    }

    //TODO move to service
    public Service calculateCNNVector(Supervisor supervisor) {
        //Create service
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException {
//                        updateMessage("Calculating vectors...");
                        int i = 0;
                        for (Particle particle : supervisor.project.particles) {
                            if(this.isCancelled()) {
                                System.out.print("Cancelled");
                                return null;
                                //throw new InterruptedException();
                            }
                            boolean isLoaded = false;
                            try {
                                String filename = particle.getFile().getAbsolutePath();
                                String sidecar_filename = FilenameUtils.concat(FilenameUtils.getFullPath(filename), "vectors");
                                sidecar_filename = FilenameUtils.concat(sidecar_filename,
                                        "." + FilenameUtils.getBaseName(filename) + ".resnet50");
                                if (particle.getCNNVector() == null && particle.getFile().exists()) {
                                    File sidecar = new File(sidecar_filename);
                                    boolean needsCalculating = true;
                                    if (sidecar.exists() && !isRecalculate) {
                                        byte[] vectorAsBytes = Files.readAllBytes(sidecar.toPath());
                                        if (vectorAsBytes.length == 2048 * Float.BYTES) {
                                            float[] vector = FloatBufferMapAdapter.byteToFloatArray(vectorAsBytes);
                                            supervisor.project.setParticleCNNVector(particle, vector);
                                            isLoaded = true;
                                            needsCalculating = false;
                                        }
                                    }
                                    if (needsCalculating) {
                                        sidecar.getParentFile().mkdirs();
                                        Mat mat = particle.getMat();
                                        if (mat != null) {
                                            try {
                                                float[] vector = predict(mat);
                                                byte[] vectorAsBytes = FloatBufferMapAdapter.floatToByteArray(vector);
                                                Files.write(sidecar.toPath(), vectorAsBytes);
                                                supervisor.project.setParticleCNNVector(particle, vector);
                                            } catch (CvException ex) {
                                                System.out.println(particle.getFilename());
                                                ex.printStackTrace();
                                            } catch (ClosedByInterruptException ex) {
                                                System.out.println("Closed by interrupt " + particle.getFilename());
                                            }
                                            mat.release();
                                        }
                                    }
                                }
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            i++;
//                            if (!isLoaded) updateMessage(String.format("%d/%d vectors calculated", i, supervisor.project.particles.size()));
//                            else updateMessage(String.format("%d/%d vectors loaded", i, supervisor.project.particles.size()));
                            updateMessage(String.format("%.0f%%", (double) i / supervisor.project.particles.size() * 100));
//                            else updateMessage(String.format("%d/%d vectors loaded", i, supervisor.project.particles.size()));
                            updateProgress(i, supervisor.project.particles.size());
                        }
//                        updateMessage(String.format("All %d vectors calculated", supervisor.project.particles.size()));
                        updateMessage("");
                        updateProgress(1,1);
                        return null;
                    }
                };
            }
        };
        return service;
    }
}
