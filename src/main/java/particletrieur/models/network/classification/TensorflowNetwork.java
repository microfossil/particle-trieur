/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.models.network.classification;

import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.ndarray.buffer.DataBuffer;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;
import org.tensorflow.proto.framework.GraphDef;
import org.tensorflow.types.TFloat32;
import particletrieur.App;
import particletrieur.controls.dialogs.BasicDialogs;

import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.ByteStreams;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class TensorflowNetwork extends NetworkBase {

    //Tensorflow
    Graph graph;
    Session sess;

    public TensorflowNetwork() {

    }

    public boolean setup() {
        setEnabled(false);

        //Check the network info is valid
        if (networkInfo == null) return false;
        if (networkInfo.isResource) {

        } else {
            if (!Files.exists(Paths.get(networkInfo.protobuf))) {
                return false;
            }
        }

        try {
            //Load the graph
            byte[] graphDef;
            if (networkInfo.isResource) {
                graphDef = ByteStreams.toByteArray(App.class.getResourceAsStream(networkInfo.protobuf));
            } else {
                graphDef = Files.readAllBytes(Paths.get(networkInfo.protobuf));
            }

            graph = new Graph();
            graph.importGraphDef(GraphDef.parseFrom(graphDef));

            //Start a session
            sess = new Session(graph);

            //Get the tensor dimensions directly from the graph
            graph.operations().forEachRemaining(operation -> {
                String opName = operation.output(0).op().name();
                for (TensorInfo tensorInfo : networkInfo.inputs) {
                    if (opName.equals(tensorInfo.operation)) {
                        Shape shape = operation.output(0).shape();
                        tensorInfo.height = shape.numDimensions() > 1 ? (int) shape.size(1) : 0;
                        tensorInfo.width = shape.numDimensions() > 2 ? (int) shape.size(2) : 0;
                        tensorInfo.channels = shape.numDimensions() > 3 ? (int) shape.size(3) : 0;
                    }
                }
            });
            setEnabled(true);
        } catch (Exception ex) {
            BasicDialogs.ShowException("Error opening tensorflow graph", ex);
            return false;
        }
        return true;
    }

    private TFloat32 predictFromFloatArray(float[] patch, int[] shape, String input, String output) {
        // Create input
        FloatBuffer buffer = FloatBuffer.wrap(patch);
        TensorInfo inputInfo = getTensorInfoByName(networkInfo.inputs, input);
        TFloat32 inputTensor = createTensorFromInfo(inputInfo, shape, buffer);
        TensorInfo outputInfo = getTensorInfoByName(networkInfo.outputs, output);

        // Calculate output
        Tensor outputTensor = sess.runner()
                .feed(inputInfo.operation, inputTensor)
                .fetch(outputInfo.operation)
                .run()
                .get(0);
        inputTensor.close();
        buffer.clear();
        patch = null;

        return (TFloat32) outputTensor;
    }

    private TFloat32 predict(Mat mat, String input, String output) {
        //Create input
        float[] patch = matToFloatArray(mat);
        return predictFromFloatArray(patch, new int[]{1, mat.rows(), mat.cols(), mat.channels()}, input, output);
    }

    private TFloat32 predict(List<Mat> mats, String input, String output) {
        //Create input
        float[] patch = listOfMatsToFloatArray(mats);
        return predictFromFloatArray(patch, new int[]{mats.size(), mats.get(0).rows(), mats.get(0).cols(), mats.get(0).channels()}, input, output);
    }

    public float[][] predictLabel(Mat mat, String input, String output) {
        TFloat32 outputTensor = predict(mat, input, output);
        return tensorToFloat(outputTensor);
    }

    public float[][] predictLabel(List<Mat> mats, String input, String output) {
        TFloat32 outputTensor = predict(mats, input, output);
        return tensorToFloat(outputTensor);
    }

    public ClassificationSet classify(Mat mat) {
        float[] probs = predictLabel(mat, networkInfo.inputs.get(0).name, networkInfo.outputs.get(0).name)[0];
        ClassificationSet classificationSet = new ClassificationSet();
        for (int i = 0; i < probs.length; i++) {
            classificationSet.add(networkInfo.labels.get(i).code, probs[i], networkInfo.name);
        }
        return classificationSet;
    }

    public List<ClassificationSet> classify(List<Mat> mats) {
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

    public <T extends Object> HashMap<T, ClassificationSet> classify(HashMap<T, Mat> matHashMap) {
        ArrayList<T> keys = new ArrayList<>();
        ArrayList<Mat> mats = new ArrayList<>();
        for (Map.Entry<T, Mat> entry : matHashMap.entrySet()) {
            keys.add(entry.getKey());
            mats.add(entry.getValue());
        }
        List<ClassificationSet> classificationSets = classify(mats);
        HashMap<T, ClassificationSet> classificationSetHashMap = new HashMap<>();
        for (int i = 0; i < mats.size(); i++) {
            classificationSetHashMap.put(keys.get(i), classificationSets.get(i));
        }
        return classificationSetHashMap;
    }

    public Mat predictSegmentation(Mat mat, String input, String output) {
        TFloat32 outputTensor = predict(mat, input, output);
        float[] floatArray = new float[(int) outputTensor.shape().size()];
        FloatDataBuffer floatBuffer = DataBuffers.of(floatArray);
        outputTensor.read(floatBuffer);
        long[] shape = outputTensor.shape().asArray();
//        float[][][][] result = new float[1][(int) shape[1]][(int) shape[2]][(int) shape[3]];
//        outputTensor.copyTo(result);
        Mat outputMat = new Mat(new Size(shape[2], shape[1]), CvType.CV_32FC((int) shape[3]));
//        float[] flattened = new float[outputTensor.numElements()];
//        int idx = 0;
//        for (int i = 0; i < shape[1]; i++) {
//            for (int j = 0; j < shape[2]; j++) {
//                for (int k = 0; k < shape[3]; k++) {
//                    flattened[idx] = result[0][i][j][k];
//                    idx++;
//                }
//            }
//        }
        outputMat.put(0, 0, floatArray);
        if (outputMat.channels() == 3) Imgproc.cvtColor(outputMat, outputMat, Imgproc.COLOR_RGB2BGR);
        outputTensor.close();
        return outputMat;
    }

    public Mat predictSegmentation(Mat mat) {
        return predictSegmentation(mat, networkInfo.inputs.get(0).name, networkInfo.outputs.get(0).name);
    }

    public float[][] tensorToFloat(TFloat32 outputTensor) {
        // Convert the FloatNdArray to a 2D float array
        float[][] result = StdArrays.array2dCopyOf(outputTensor);
        // Close the tensor
        outputTensor.close();
        return result;
    }


    private TensorInfo getTensorInfoByName(ArrayList<TensorInfo> tensorInfos, String name) {
        for (TensorInfo tensorInfo : tensorInfos) {
            if (tensorInfo.name.equals(name)) return tensorInfo;
        }
        return null;
    }

    private TFloat32 createTensorFromInfo(TensorInfo tensorInfo, int[] shape, FloatBuffer buffer) {

        int height = tensorInfo.height == -1 ? shape[1] : tensorInfo.height;

        // 1D
        if (tensorInfo.width == 0) {
            return TFloat32.tensorOf(Shape.of(shape[0], height), DataBuffers.of(buffer));
        }

        // 2D
        int width = tensorInfo.width == -1 ? shape[2] : tensorInfo.width;
        if (tensorInfo.channels == 0) {
            return TFloat32.tensorOf(Shape.of(shape[0], height, width), DataBuffers.of(buffer));
        }

        // 3D
        int channels = tensorInfo.channels == -1 ? shape[3] : tensorInfo.channels;
        return TFloat32.tensorOf(Shape.of(shape[0], height, width, channels), DataBuffers.of(buffer));
    }

}
