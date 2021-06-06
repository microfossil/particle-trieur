package particletrieur.services.network;

import org.opencv.core.Mat;

public interface ISegmenterService {
    public Mat predict(Mat mat);
}
