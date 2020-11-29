/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.models.network.classification;

import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.controls.BasicDialogs;

import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.ByteStreams;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.*;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class NetworkEx {

    //Tensorflow
    Graph graph;
    Session sess;

    //Is the network running?
    private BooleanProperty enabled = new SimpleBooleanProperty(false);
    public BooleanProperty enabledProperty() {
        return enabled;
    }
    public void setEnabled(boolean value) {
        enabled.set(value);
    }
    public boolean isEnabled() {
        return enabled.get();
    }

    //The network definition / information
    private NetworkInfo networkInfo;
    public void setNetworkInfo(NetworkInfo networkInfo) {
        this.networkInfo = networkInfo;
    }
    public NetworkInfo getNetworkInfo() {
        return networkInfo;
    }


    public NetworkEx() {

    }

    public boolean setup() {
        setEnabled(false);

        //Check the network info is valid
        if (networkInfo == null) return false;
        if (networkInfo.isResource) {

        }
        else {
            if (!Files.exists(Paths.get(networkInfo.protobuf))) {
                return false;
            }
        }

        try {
            //Load the graph
            byte[] graphDef;
            if (networkInfo.isResource) {
                graphDef = ByteStreams.toByteArray(App.class.getResourceAsStream(networkInfo.protobuf));
            }
            else {
                graphDef = Files.readAllBytes(Paths.get(networkInfo.protobuf));
            }

            graph = new Graph();
            graph.importGraphDef(graphDef);

            //Start a session
            sess = new Session(graph);

            //Get the tensor dimensions directory from the graph
            graph.operations().forEachRemaining(operation -> {
                String opName = operation.output(0).op().name();
                for (TensorInfo tensorInfo : networkInfo.inputs) {
                    if (opName.equals(tensorInfo.operation)) {
                        Shape shape = operation.output(0).shape();
                        tensorInfo.height = shape.numDimensions() > 1 ? (int)shape.size(1) : 0;
                        tensorInfo.width = shape.numDimensions() > 2 ? (int)shape.size(2) : 0;
                        tensorInfo.channels = shape.numDimensions() > 3 ? (int)shape.size(3) : 0;
//                        System.out.println(operation.output(0).op().name());
//                        System.out.println(operation.output(0).shape());
                    }
                }
            });

            //See if there is metadata
//            Tensor metadataTensor = sess.runner()
//                    .fetch("metadata:0")
//                    .run()
//                    .get(0);
//            byte[] metadataString = new byte[metadataTensor.numBytes()];
//            metadataTensor.copyTo(metadataString);
//            metadataTensor.close();
            setEnabled(true);
        } catch (Exception ex) {
            BasicDialogs.ShowException("Error opening tensorflow graph", ex);
            return false;
        }
        return true;
    }

    private Tensor predict(float[] patch, String input, String output, int batchSize) {
        //Create input
        FloatBuffer buffer = FloatBuffer.wrap(patch);
        TensorInfo inputInfo = getTensorInfoByName(networkInfo.inputs, input);
        Tensor inputTensor = createTensorFromInfo(inputInfo, batchSize, buffer);
        TensorInfo outputInfo = getTensorInfoByName(networkInfo.outputs, output);
        //Calculate output
        Tensor outputTensor = sess.runner()
                .feed(inputInfo.operation, inputTensor)
                .fetch(outputInfo.operation)
                .run()
                .get(0);
        inputTensor.close();
        buffer.clear();
        patch = null;
        return outputTensor;
    }

    private Tensor predict(Mat mat, String input, String output) {
        //Create input
        float[] patch = matToFloatArray(mat);
        return predict(patch, input, output, 1);
    }

    private Tensor predict(List<Mat> mats, String input, String output) {
        //Create input
        float[] patch = matToFloatArray(mats);
        return predict(patch, input, output, mats.size());
    }

    public float[][] tensorToFloat(Tensor outputTensor) {
        long[] shape = outputTensor.shape();
        float[][] result = new float[(int)shape[0]][(int)(outputTensor.numElements() / shape[0])];
        outputTensor.copyTo(result);
        outputTensor.close();
        return result;
    }

    public float[][] predictLabel(Mat mat, String input, String output) {
        Tensor outputTensor = predict(mat, input, output);
        return tensorToFloat(outputTensor);
    }

    public float[][] predictLabel(List<Mat> mats, String input, String output) {
        Tensor outputTensor = predict(mats, input, output);
        return tensorToFloat(outputTensor);
    }

    public Mat predictMat(Mat mat, String input, String output) {
        Tensor outputTensor = predict(mat, input, output);
        long[] shape = outputTensor.shape();
        float[][][][] result = new float[1][(int)shape[1]][(int)shape[2]][(int)shape[3]];
        outputTensor.copyTo(result);
        Mat outputMat = new Mat(new Size(shape[2], shape[1]), CvType.CV_32FC((int)shape[3]));
        float[] flattened = new float[outputTensor.numElements()];
        int idx = 0;
        for (int i = 0; i < shape[1]; i++) {
            for (int j = 0; j < shape[2]; j++) {
                for (int k = 0; k < shape[3]; k++) {
                    flattened[idx] = result[0][i][j][k];
                    idx++;
                }
            }
        }
        outputMat.put(0,0, flattened);
        if (outputMat.channels() == 3) Imgproc.cvtColor(outputMat, outputMat, Imgproc.COLOR_RGB2BGR);
        outputTensor.close();
        return outputMat;
    }

    public Mat predictMat(Mat mat) {
        return predictMat(mat, networkInfo.inputs.get(0).name, networkInfo.outputs.get(0).name);
    }

    public ClassificationSet predictLabel(Mat mat) {
        float[] probs = predictLabel(mat, networkInfo.inputs.get(0).name, networkInfo.outputs.get(0).name)[0];
        ClassificationSet classificationSet = new ClassificationSet();
        for (int i = 0; i < probs.length; i++) {
            classificationSet.add(networkInfo.labels.get(i).code, probs[i], networkInfo.name);
        }
        return classificationSet;
    }

    public List<ClassificationSet> predictLabel(List<Mat> mats) {
        float[][] probs = predictLabel(mats, networkInfo.inputs.get(0).name, networkInfo.outputs.get(0).name);
        ArrayList<ClassificationSet> classificationSets = new ArrayList<>();
        for (int i = 0; i < probs.length; i++) {
            ClassificationSet classificationSet = new ClassificationSet();
            float[] prob = probs[i];
            for (int j = 0; j < prob.length; j++) {
                classificationSet.add(networkInfo.labels.get(j).code, prob[j], networkInfo.name);
            }
            classificationSets.add(classificationSet);
        }
        return classificationSets;
    }

    public <T extends Object> HashMap<T,ClassificationSet> predictLabel(HashMap<T, Mat> matHashMap) {
        ArrayList<T> keys = new ArrayList<>();
        ArrayList<Mat> mats = new ArrayList<>();
        for(Map.Entry<T,Mat> entry : matHashMap.entrySet()) {
            keys.add(entry.getKey());
            mats.add(entry.getValue());
        }
        List<ClassificationSet> classificationSets = predictLabel(mats);
        HashMap<T,ClassificationSet> classificationSetHashMap = new HashMap<>();
        for (int i = 0; i < mats.size(); i++) {
            classificationSetHashMap.put(keys.get(i), classificationSets.get(i));
        }
        return classificationSetHashMap;
    }

    private float[] matToFloatArray(Mat mat) {
        //Convert to RGB (OpenCV is BGR)
        Mat rgb = new Mat();
        if (mat.channels() == 3) Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_BGR2RGB);
        else rgb = mat.clone();
        //Copy to mat data to float array for feeding into the network
        int size = rgb.rows() * rgb.cols() * rgb.channels();
        float[] patch = new float[size];
        rgb.get(0, 0, patch);
        rgb.release();
        return patch;
    }

    private float[] matToFloatArray(List<Mat> mats) {
        //Check we actually have mats
        if (mats.size() == 0) return null;
        //Initialise the float array
        Mat firstMat = mats.get(0);
        int size = firstMat.rows() * firstMat.cols() * firstMat.channels();
        float[] patch = new float[size*mats.size()];
        float[] temp = new float[size];

        int idx = 0;
        for (Mat mat : mats) {
            if (mat.rows() != firstMat.rows() ||
                    mat.cols() != firstMat.cols() ||
                    mat.channels() != firstMat.channels()) {
                throw new IllegalArgumentException("All Mats must have the same dimensions.");
            }
            Mat rgb = new Mat();
            if (mat.channels() == 3) Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_BGR2RGB);
            else rgb = mat.clone();
            rgb.get(0, 0, temp);
            rgb.release();
            System.arraycopy(temp, 0, patch, idx, size);
            idx += size;
        }
        return patch;
    }

    private TensorInfo getTensorInfoByName(ArrayList<TensorInfo> tensorInfos, String name) {
        for (TensorInfo tensorInfo : tensorInfos) {
            if (tensorInfo.name.equals(name)) return tensorInfo;
        }
        return  null;
    }

    private Tensor createTensorFromInfo(TensorInfo tensorInfo, int batchSize, FloatBuffer buffer) {
        if (tensorInfo.width == 0) {
            return Tensor.create(new long[] {batchSize, tensorInfo.height}, buffer);
        }
        else if(tensorInfo.channels == 0) {
            return Tensor.create(new long[] {batchSize, tensorInfo.height, tensorInfo.width}, buffer);
        }
        else {
            return Tensor.create(new long[] {batchSize, tensorInfo.height, tensorInfo.width, tensorInfo.channels}, buffer);
        }
    }

    /*
    CLASSIFICATION
    */
//    public ClassificationSet classify(Mat mat) {
//        float[] probs = calculateProbability(mat);
//        ClassificationSet classificationSet = convertToClassificationSet(probs);
//        return classificationSet;
//    }
//
//    public List<ClassificationSet> classify(List<Mat> mats) {
//        float[][] probs = calculateProbability(mats);
//        ArrayList<ClassificationSet> results = new ArrayList<>();
//        for (int i = 0; i < mats.size(); i++) {
//            ClassificationSet classificationSet = convertToClassificationSet(probs[i]);
//            results.add(classificationSet);
//        }
//        return results;
//    }
//
//    public <T extends Object> HashMap<T,ClassificationSet> classify(HashMap<T, Mat> mats) {
//        ArrayList<T> keys = new ArrayList<>();
//        ArrayList<Mat> values = new ArrayList<>();
//        for(Map.Entry<T,Mat> entry : mats.entrySet()) {
//            keys.add(entry.getKey());
//            values.add(entry.getValue());
//        }
//        HashMap<T,ClassificationSet> results = new HashMap<>();
//        float[][] probs = calculateProbability(values);
//        for (int i = 0; i < mats.size(); i++) {
//            ClassificationSet classificationSet = convertToClassificationSet(probs[i]);
//            results.put(keys.get(i), classificationSet);
//        }
//        return results;
//    }
//
//    public float[] calculateProbability(Mat mat) {
//        return calculateProbability(getPatch(mat), 1)[0];
//    }
//
//    public float[][] calculateProbability(List<Mat> mats) {
//        return calculateProbability(getPatch(mats), mats.size());
//    }
//
//    public float[][] calculateProbability(float[] patch, int count) {
//        inputBuffer = FloatBuffer.wrap(patch);
//        inputTensor = Tensor.create(new long[] {
//                        count,
//                        networkDefinition.width,
//                        networkDefinition.height,
//                        networkDefinition.channels},
//                inputBuffer);
//        //Flag allows for a control value to be fed into the network, e.g. to control dropout
//        if (networkDefinition.flag != null && !networkDefinition.flag.equals("")) {
//            outputTensor = sess.runner()
//                    .feed(networkDefinition.input, inputTensor)
//                    .feed(networkDefinition.flag, flagTensor)
//                    .fetch(networkDefinition.output)
//                    .run().get(0);
//        }
//        else {
//            outputTensor = sess.runner()
//                    .feed(networkDefinition.input, inputTensor)
//                    .fetch(networkDefinition.output).run().get(0);
//        }
//        // Get results
//        System.out.println("Tensorflow classification results:");
//        System.out.println(String.format("- bytes: %d",outputTensor.numBytes()));
//        //Number of labels
//        final long[] rshape = outputTensor.shape();
//        int nlabels = (int) rshape[1];
//        //Probability for each image
//        float[][] probs = new float[count][nlabels];
//        outputTensor.copyTo(probs);
//
////        for (int im = 0; im < count; im++) {
////            //Convert to labels (debug)
////            for (int i = 0; i < probs[im].length; i++) {
////                if (i < networkDefinition.labels.size()) {
////                    System.out.println(String.format("- %s: %f", networkDefinition.labels.get(i).getCode(), probs[im][i]));
////                }
////                else {
////                    System.out.println(String.format("- %d: %f", i, probs[im][i]));
////                }
////            }
////        }
//        return probs;
//    }
//
//    public float[] calculateVector(Mat mat) {
//        return calculateVector(getPatch(mat), 1)[0];
//    }
//
//    public float[][] calculateVector(List<Mat> mats) {
//        return calculateVector(getPatch(mats), mats.size());
//    }
//
//    public <T extends Object> HashMap<T,float[]> calculateVector(HashMap<T, Mat> mats) {
//        ArrayList<T> keys = new ArrayList<>();
//        ArrayList<Mat> values = new ArrayList<>();
//        for(Map.Entry<T,Mat> entry : mats.entrySet()) {
//            keys.add(entry.getKey());
//            values.add(entry.getValue());
//        }
//        HashMap<T,float[]> results = new HashMap<>();
//        float[][] vectors = calculateVector(values);
//        for (int i = 0; i < mats.size(); i++) {
//            float[] vector = vectors[i];
//            results.put(keys.get(i), vector);
//        }
//        return results;
//    }
//
//    public float[][] calculateVector(float[] patch, int count) {
//        inputBuffer = FloatBuffer.wrap(patch);
//        inputTensor = Tensor.create(new long[] {
//                        count,
//                        networkDefinition.width,
//                        networkDefinition.height,
//                        networkDefinition.channels},
//                inputBuffer);
//        if (networkDefinition.flag != null && !networkDefinition.flag.equals("")) {
//            vectorTensor = sess.runner().feed(networkDefinition.input, inputTensor).feed(networkDefinition.flag, flagTensor).fetch(networkDefinition.vector).run().get(0);
//        }
//        else {
//            vectorTensor = sess.runner().feed(networkDefinition.input, inputTensor).fetch(networkDefinition.vector).run().get(0);
//        }
//        System.out.println("Tensorflow vector results:");
//        System.out.println(String.format("- bytes: %d", vectorTensor.numBytes()));
//        final long[] rshape = vectorTensor.shape();
//        int nlabels = (int) rshape[1];
//        float[][] probs = new float[count][nlabels];
//        vectorTensor.copyTo(probs);
//        return probs;
//    }
//
//    public float[] nullVector() {
//        final long[] rshape = vectorTensor.shape();
//        int nlabels = (int) rshape[1];
//        float[] probs = new float[nlabels];
//        Arrays.fill(probs, (float)0.0);
//        return probs;
//    }
//
//    public ClassificationSet convertToClassificationSet(float[] probs) {
//        ClassificationSet classificationSet = new ClassificationSet();
//        for (int i = 0; i < probs.length; i++) {
//            classificationSet.add(networkDefinition.labels.get(i).getCode(), probs[i], networkDefinition.name);
//        }
//        return classificationSet;
//    }
//
//    private float[] getPatch(Mat mat) {
//        //Copy to mat data to float array for feeding into the network
//        int size = mat.rows() * mat.cols();
//        float[] patch = new float[size];
//        mat.get(0, 0, patch);
//        //Check the maximum value (debug)
//        float max = 0;
//        for (int i = 0; i < size; i++) {
//            if (patch[i] > max) max = patch[i];
//        }
//        System.out.println(String.format("MAX OF PATCH IS %f", max));
//        return patch;
//    }
//
//    private float[] getPatch(List<Mat> mats) {
//
//        //Check we actually have mats
//        if (mats.size() == 0) return null;
//
//        //Initialise the float array
//        Mat firstMat = mats.get(0);
//        int size = firstMat.rows() * firstMat.cols() * firstMat.channels();
//        float[] patch = new float[size*mats.size()];
//        float[] temp = new float[size];
//
//        int idx = 0;
//        for (Mat mat : mats) {
//            if (mat.rows() != firstMat.rows() ||
//                    mat.cols() != firstMat.cols() ||
//                    mat.channels() != firstMat.channels()) {
//                throw new IllegalArgumentException("All Mats must have the same dimensions.");
//            }
//            mat.get(0, 0, temp);
//            System.arraycopy(temp, 0, patch, idx, size);
//            idx += size;
//        }
//        return patch;
//    }
}
