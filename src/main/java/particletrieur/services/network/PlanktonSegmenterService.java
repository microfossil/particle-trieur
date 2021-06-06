package particletrieur.services.network;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.models.network.classification.NetworkEx;
import particletrieur.models.network.classification.NetworkInfo;
import particletrieur.models.network.classification.TensorInfo;
import particletrieur.models.processing.processors.Preprocessor;

public class PlanktonSegmenterService implements ISegmenterService {

    public NetworkEx networkEx;
    private NetworkInfo info;

    public PlanktonSegmenterService() {
        info = new NetworkInfo();
        info.name = "debugging";
        info.description = "debugging";
        info.protobuf =  "/trained_networks/plankton_segmenter/frozen_model.pb";
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
        if (networkEx == null) {
            System.out.println("Loading plankton segmentation network");
            networkEx = new NetworkEx();
            networkEx.setNetworkInfo(info);
            if (!networkEx.setup()) {
                BasicDialogs.ShowError("Network error",
                        "Cannot start plankton segmentation network.\nThe tensorflow graph file does not exist, or the network XML file was an old version.");
            }
        }
//        TensorInfo inputInfo = networkEx.getNetworkInfo().inputs.get(0);
//        TensorInfo outputInfo = networkEx.getNetworkInfo().outputs.get(0);
        Mat input = Preprocessor.resize(mat, 224, 224, 1);
        Mat output = networkEx.predictMat(input, "image", "pred");
//        Core.multiply(input,new Scalar(255),input);
//        Imgcodecs.imwrite("D:\\test.png", input);

//        Core.multiply(output,new Scalar(255),output);
        output = Preprocessor.resize(output, mat.rows(), mat.cols(), 1);

//        Imgcodecs.imwrite("D:\\reseulr.png", output);
        input.release();
        return output;
    }
}
