package ordervschaos.particletrieur.app.models.processing;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class ImageAnnotator {

    public static Mat createContourImage(Mat image, MatOfPoint contour) {
        Mat output = image.clone();
        if (contour != null) {
            int size = output.width() / 200 + 1;
            ArrayList<MatOfPoint> list = new ArrayList<>();
            list.add(contour);
            Imgproc.drawContours(output, list, -1, new Scalar(255, 128, 0), size);
        }
        else {
//            Imgproc.putText(output, "No contour", new Point(10,10), Core.FONT_HERSHEY_SIMPLEX, 10, Scalar.all(255), 2);
        }
        return output;
    }
}
