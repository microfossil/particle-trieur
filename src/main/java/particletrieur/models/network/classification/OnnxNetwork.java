package particletrieur.models.network.classification;

import ai.onnxruntime.*;
import com.google.common.io.ByteStreams;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import particletrieur.App;
import particletrieur.controls.dialogs.BasicDialogs;

import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class OnnxNetwork extends NetworkBase {

    private OrtEnvironment ortEnvironment;

    private OrtSession ortSession;

    private String inputName;

    @Override
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

            // Load the model
            ortEnvironment = OrtEnvironment.getEnvironment();
            byte[] modelArray;
            if (networkInfo.isResource) {
                modelArray = ByteStreams.toByteArray(App.class.getResourceAsStream(networkInfo.protobuf));
                ortEnvironment.createSession(modelArray);
            }
            else {
                modelArray = Files.readAllBytes(Paths.get(networkInfo.protobuf));
            }
            ortSession = ortEnvironment.createSession(modelArray, new OrtSession.SessionOptions());

            Set<String> inputNames = ortSession.getInputNames();
            inputName = inputNames.stream().findFirst().get();

            setEnabled(true);
        } catch (Exception ex) {
            BasicDialogs.ShowException("Error opening ONNX model", ex);
            return false;
        }
        return true;
    }

    public float[] predictVectorFromFloatArray(float[] patch, long[] shape) throws OrtException {
        // Create input
        FloatBuffer buffer = FloatBuffer.wrap(patch);
        OnnxTensor inputTensor = OnnxTensor.createTensor(ortEnvironment, buffer, shape);
        Map<String, OnnxTensor> input = new HashMap<>();
        input.put(inputName, inputTensor);
        OrtSession.Result result = ortSession.run(input);

        float[][] output = (float[][])result.get(0).getValue();

        return output[0];
    }

    public float[][][] predictImageFromFloatArray(float[] patch, long[] shape) throws OrtException {
        // Create input
        FloatBuffer buffer = FloatBuffer.wrap(patch);
        OnnxTensor inputTensor = OnnxTensor.createTensor(ortEnvironment, buffer, shape);
        Map<String, OnnxTensor> input = new HashMap<>();
        input.put(inputName, inputTensor);
        OrtSession.Result result = ortSession.run(input);
        float[][][][] output = (float[][][][])result.get(0).getValue();
        return output[0];
    }

    public float[] predictVectorFromMat(Mat mat) throws OrtException {
        float[] patch = matToFloatArray(mat);
        return predictVectorFromFloatArray(patch, new long[]{1, mat.rows(), mat.cols(), mat.channels()});
    }

    public float[][][] predictImageFromMat(Mat mat) throws OrtException {
        float[] patch = matToFloatArray(mat);
        return predictImageFromFloatArray(patch, new long[]{1, mat.rows(), mat.cols(), mat.channels()});
    }

    public Mat predictFromMatSegmentation(Mat mat) throws OrtException {
        float[][][] result = predictImageFromMat(mat);
//        Tensor outputTensor = predict(mat, input, output);
//        long[] shape = outputTensor.shape();
//        float[][][][] result = new float[1][(int)shape[1]][(int)shape[2]][(int)shape[3]];
//        outputTensor.copyTo(result);
        Mat outputMat = new Mat(mat.size(), CvType.CV_32FC1);
        float[] flattened = new float[mat.rows() * mat.cols()];
        int idx = 0;
        for (int i = 0; i < mat.rows(); i++) {
            for (int j = 0; j < mat.cols(); j++) {
                for (int k = 0; k < mat.channels(); k++) {
                    flattened[idx] = result[i][j][k];
                    idx++;
                }
            }
        }
        outputMat.put(0,0, flattened);
//        if (outputMat.channels() == 3) Imgproc.cvtColor(outputMat, outputMat, Imgproc.COLOR_RGB2BGR);
//        outputTensor.close();
        return outputMat;
    }

    @Override
    public ClassificationSet classify(Mat mat) {
        float[] patch = matToFloatArray(mat);
        try {
            float[] probs = predictVectorFromFloatArray(patch, new long[]{1, mat.rows(), mat.cols(), mat.channels()});
            ClassificationSet classificationSet = new ClassificationSet();
            for (int i = 0; i < probs.length; i++) {
                classificationSet.add(networkInfo.labels.get(i).code, probs[i], networkInfo.name);
            }
            return classificationSet;
        }
        catch (OrtException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ClassificationSet> classify(List<Mat> mats) {
        float[] patch = listOfMatsToFloatArray(mats);
        try {
            float[] probs = predictVectorFromFloatArray(patch, new long[] {mats.size(), mats.get(0).rows(), mats.get(0).cols(), mats.get(0).channels()});
            ArrayList<ClassificationSet> classificationSets = new ArrayList<>();
            int numOutputs = probs.length / mats.size();
            for (int i = 0; i < probs.length; i+= numOutputs) {
                ClassificationSet classificationSet = new ClassificationSet();
                for (int j = 0; j < numOutputs; j++) {
                    classificationSet.add(networkInfo.labels.get(j).code, probs[i + j], networkInfo.name);
                }
                classificationSets.add(classificationSet);
            }
            return classificationSets;
        }
        catch (OrtException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> HashMap<T, ClassificationSet> classify(HashMap<T, Mat> matHashMap) {
        ArrayList<T> keys = new ArrayList<>();
        ArrayList<Mat> mats = new ArrayList<>();
        for(Map.Entry<T,Mat> entry : matHashMap.entrySet()) {
            keys.add(entry.getKey());
            mats.add(entry.getValue());
        }
        List<ClassificationSet> classificationSets = classify(mats);
        HashMap<T,ClassificationSet> classificationSetHashMap = new HashMap<>();
        for (int i = 0; i < mats.size(); i++) {
            classificationSetHashMap.put(keys.get(i), classificationSets.get(i));
        }
        return classificationSetHashMap;
    }
}
