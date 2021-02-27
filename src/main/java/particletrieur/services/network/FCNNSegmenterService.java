package particletrieur.services.network;

import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.models.network.classification.NetworkEx;
import particletrieur.models.network.classification.NetworkInfo;
import particletrieur.models.network.classification.TensorInfo;
import particletrieur.models.processing.processors.Preprocessor;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class FCNNSegmenterService {

    public NetworkEx networkEx;

    public FCNNSegmenterService() {

        NetworkInfo info = new NetworkInfo();
        info.name = "debugging";
        info.description = "debugging";
        info.protobuf =  "/trained_networks/UNet-ResNet34-segmenter/frozen_model.pb";
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

        networkEx = new NetworkEx();
        networkEx.setNetworkInfo(info);
        if (!networkEx.setup()) {
            BasicDialogs.ShowError("Network error",
                    "Cannot start segmentation network.\nThe tensorflow graph file does not exist, or the network XML file was an old version.");
        }
    }

    public Mat predict(Mat mat) {
        TensorInfo info = networkEx.getNetworkInfo().inputs.get(0);
        TensorInfo outputInfo = networkEx.getNetworkInfo().outputs.get(0);
        Mat input = Preprocessor.resize(mat, info.height, info.width, info.channels);
        Mat output = networkEx.predictMat(input, "image", "mask");
        Core.multiply(output,new Scalar(255),output);
        output = Preprocessor.resize(output, mat.rows(), mat.cols(), 1);
        input.release();
        return output;
    }
}
