package particletrieur.models.processing.processors;

import particletrieur.models.processing.Mask;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Random;

public class DisplayProcessor {

    public static void forDisplay(Mask mask, boolean isShowEllipseFitting) {
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

            if(isShowEllipseFitting){
                MatOfPoint2f contour2f = mask.contour2f;
                Random rng = new Random(12345);
                if(contour2f.rows() >= 5) {
                    RotatedRect ellipseRect = Imgproc.fitEllipse(contour2f);

                    Imgproc.ellipse(mask.display, ellipseRect, new Scalar(0,  0, 255), 2);

                    // draw circle at cente5
                    Point center_ellipse;
                    center_ellipse = ellipseRect.center;

                    Imgproc.circle(mask.display, center_ellipse, 3, new Scalar(0,  255, 0), -1);

                    // draw major minor axes
                    //Rect rect_ellipse = ellipseRect.boundingRect();
                    Point[] rectPoints = new Point[4];
                    ellipseRect.points(rectPoints);

                    Point pts1 = new Point();
                    pts1.x = (rectPoints[0].x + rectPoints[1].x)/2;
                    pts1.y = (rectPoints[0].y + rectPoints[1].y)/2;
                    Point pts2 = new Point();
                    pts2.x = (rectPoints[2].x + rectPoints[3].x)/2;
                    pts2.y = (rectPoints[2].y + rectPoints[3].y)/2;

                    Point pts3 = new Point();
                    pts3.x = (rectPoints[1].x + rectPoints[2].x)/2;
                    pts3.y = (rectPoints[1].y + rectPoints[2].y)/2;
                    Point pts4 = new Point();
                    pts4.x = (rectPoints[3].x + rectPoints[0].x)/2;
                    pts4.y = (rectPoints[3].y + rectPoints[0].y)/2;

                    Imgproc.line(mask.display, pts1, pts2, new Scalar(255,  0, 0));
                    Imgproc.line(mask.display, pts3, pts4, new Scalar(255,  0, 0));

                }
            }

        }
    }
}
