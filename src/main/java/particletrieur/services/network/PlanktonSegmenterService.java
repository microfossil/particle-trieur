package particletrieur.services.network;

import ai.onnxruntime.OrtException;
import org.opencv.core.Mat;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.models.network.classification.OnnxNetwork;
import particletrieur.models.network.classification.TensorflowNetwork;
import particletrieur.models.network.classification.NetworkInfo;
import particletrieur.models.network.classification.TensorInfo;
import particletrieur.models.processing.processors.Preprocessor;

public class PlanktonSegmenterService implements ISegmenterService {

    public TensorflowNetwork tensorflowNetwork;

    public OnnxNetwork onnxNetwork;
    private NetworkInfo info;

    public PlanktonSegmenterService() {
        info = new NetworkInfo();
        info.name = "debugging";
        info.description = "debugging";
//        info.protobuf =  "/trained_networks/plankton_segmenter/frozen_model.pb";
        info.protobuf =  "/trained_networks/plankton_segmenter/model.onnx";
        info.type = "segmenter";
        TensorInfo input = new TensorInfo();
        input.name = "image";
        input.operation = "image";
        TensorInfo output = new TensorInfo();
        output.name = "pred";
        output.operation = "sigmoid/Sigmoid";
        output.operation = "pred/Sigmoid";
        info.inputs.add(input);
        info.outputs.add(output);
        info.isResource = true;
    }

    public Mat predict(Mat mat) {
//        if (tensorflowNetwork == null) {
//            System.out.println("Loading plankton segmentation network");
//            tensorflowNetwork = new TensorflowNetwork();
//            tensorflowNetwork.setNetworkInfo(info);
//            if (!tensorflowNetwork.setup()) {
//                BasicDialogs.ShowError("Network error",
//                        "Cannot start plankton segmentation network.\nThe tensorflow graph file does not exist, or the network XML file was an old version.");
//            }
//        }
//        Mat input = Preprocessor.resize(mat, 224, 224, 1);
//        Mat output = tensorflowNetwork.predictSegmentation(input, "image", "pred");
//        output = Preprocessor.resize(output, mat.rows(), mat.cols(), 1);
//        input.release();
//        return output;
        if (onnxNetwork == null) {
            System.out.println("Loading plankton segmentation network");
            onnxNetwork = new OnnxNetwork();
            onnxNetwork.setNetworkInfo(info);
            if (!onnxNetwork.setup()) {
                BasicDialogs.ShowError("Network error",
                        "Cannot start plankton segmentation network.\nThe onnx model file does not exist, or the network XML file was an old version.");
            }
        }
        Mat input = Preprocessor.resize(mat, 224, 224, 1);
        try {
            Mat output = onnxNetwork.predictFromMatSegmentation(input);
            output = Preprocessor.resize(output, mat.rows(), mat.cols(), 1);
            input.release();
            return output;
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
    }
}
