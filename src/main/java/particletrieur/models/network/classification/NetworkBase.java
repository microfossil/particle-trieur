package particletrieur.models.network.classification;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.List;

public abstract class NetworkBase {

    //Is the network running?
    protected BooleanProperty enabled = new SimpleBooleanProperty(false);
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
    protected NetworkInfo networkInfo;
    public void setNetworkInfo(NetworkInfo networkInfo) {
        this.networkInfo = networkInfo;
    }
    public NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    public abstract boolean setup();

    public abstract ClassificationSet classify(Mat mat);

    public abstract List<ClassificationSet> classify(List<Mat> mats);

    public abstract <T extends Object> HashMap<T, ClassificationSet> classify(HashMap<T, Mat> matHashMap);

    protected float[] matToFloatArray(Mat mat) {
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

    protected float[] listOfMatsToFloatArray(List<Mat> mats) {
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
}
