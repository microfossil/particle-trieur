package main.java.app.models.processing.processors;

import main.java.app.models.processing.Mask;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class DisplayProcessor {

    public static void forDisplay(Mask mask) {
        mask.display = mask.image.clone();
        Core.multiply(mask.display, Scalar.all(255), mask.display);
        mask.display.convertTo(mask.display, CvType.CV_8U);
        if (mask.display.channels() == 1) {
            Imgproc.cvtColor(mask.display, mask.display, Imgproc.COLOR_GRAY2BGR);
        }
        int size = mask.display.width() / 200 + 1;
        ArrayList<MatOfPoint> list = new ArrayList<>();
        if (mask.contour != null) {
            Mat R = mask.binary.clone();
            Mat G = Mat.zeros(mask.binary.size(), CvType.CV_8U);
            Mat B = Mat.zeros(mask.binary.size(), CvType.CV_8U);
            Core.divide(R, Scalar.all(8), R);
            Mat tint = new Mat();
            ArrayList<Mat> mats = new ArrayList<>();
            mats.add(R);
            mats.add(G);
            mats.add(B);
            Core.merge(mats, tint);
            Core.add(mask.display, tint, mask.display);
            R.release();
            G.release();
            B.release();
            tint.release();
            list.add(mask.contour);
            Imgproc.drawContours(mask.display, list, -1, new Scalar(255, 128, 0), size);
        }
    }
}
