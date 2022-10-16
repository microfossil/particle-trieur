package particletrieur.services.network;

import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.models.network.classification.TensorflowNetwork;
import particletrieur.models.network.classification.NetworkInfo;
import particletrieur.models.network.classification.TensorInfo;
import particletrieur.models.processing.processors.Preprocessor;
import org.opencv.core.Mat;

public class ForaminiferaSegmenterService implements ISegmenterService {

    public TensorflowNetwork tensorflowNetwork;

    public ForaminiferaSegmenterService() {

        NetworkInfo info = new NetworkInfo();
        info.name = "debugging";
        info.description = "debugging";
        info.protobuf = "/trained_networks/foraminifera_segmenter/frozen_model.pb";
        info.type = "segmenter";
        TensorInfo input = new TensorInfo();
        input.name = "image";
        input.operation = "data";
        TensorInfo output = new TensorInfo();
        output.name = "mask";
        output.operation = "sigmoid/Sigmoid";
        info.inputs.add(input);
        info.outputs.add(output);
        info.isResource = true;

        tensorflowNetwork = new TensorflowNetwork();
        tensorflowNetwork.setNetworkInfo(info);
        if (!tensorflowNetwork.setup()) {
            System.out.println("Loading foraminifera segmentation network");
            BasicDialogs.ShowError("Network error",
                    "Cannot start segmentation network.\nThe tensorflow graph file does not exist, or the network XML file was an old version.");
        }
    }

    public Mat predict(Mat mat) {
        TensorInfo info = tensorflowNetwork.getNetworkInfo().inputs.get(0);
        TensorInfo outputInfo = tensorflowNetwork.getNetworkInfo().outputs.get(0);
        Mat input = Preprocessor.resize(mat, info.height, info.width, info.channels);
        Mat output = tensorflowNetwork.predictSegmentation(input, "image", "mask");
//        Core.multiply(output,new Scalar(255),output);
        output = Preprocessor.resize(output, mat.rows(), mat.cols(), 1);
        input.release();
        return output;
    }
}
