package particletrieur.models.processing;

import particletrieur.services.network.ForaminiferaSegmenterService;
import particletrieur.models.processing.processors.DisplayProcessor;
import particletrieur.models.processing.processors.SegmentationProcessor;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import particletrieur.services.network.ISegmenterService;

public class Mask {

    public Mat image;
    public Mat energy;
    public Mat display;
    public Mat binary;
    public Mat binaryF;
    public MatOfPoint contour;
    public MatOfPoint2f contour2f;
    public Point centreOfMass;
    public double maxRadius;
    public double maxRadiusAngle;
    public ImageType type;

    public Mask(Mat input, ImageType type) {
        if (input.channels() == 3) {
            image = new Mat();
            Imgproc.cvtColor(input, image, Imgproc.COLOR_BGR2GRAY);
        }
        else {
            image = input.clone();
        }
        this.type = type;
    }

    public void release() {
        if (image != null) image.release();
        if (energy != null) energy.release();
        if (display != null) display.release();
        if (binary != null) binary.release();
        if (binaryF != null) binaryF.release();
        if (contour != null) contour.release();
        if (contour2f != null) contour2f.release();
    }

    public boolean isOutline() {
        return contour != null;
    }

    public static Mask create(Mat input, ImageType type) {
        Mask mask = new Mask(input, type);
        return mask;
    }

    public Mask segmentFixedIntensity(double threshold) {
        SegmentationProcessor.segmentFixedIntensity(this, this.type, threshold);
        return this;
    }

    public Mask segmentOtsuIntensity(double threshold) {
        SegmentationProcessor.segmentOtsuIntensity(this, this.type, threshold);
        return this;
    }

    public Mask segmentCNN(double threshold, ISegmenterService predictionService) {
        SegmentationProcessor.segmentCNN(this, this.type, threshold, predictionService);
        return this;
    }

    public Mask segmentExperimental(double threshold) {
        SegmentationProcessor.segmentExperimental(this, this.type, threshold);
        return this;
    }

    public Mask segmentAdaptiveThreshold(double threshold) {
        SegmentationProcessor.segmentAdaptiveThreshold(this, this.type, threshold);
        return this;
    }

    public Mask enhanceEdges() {
        SegmentationProcessor.enhanceEdges(this, this.type, 1);
        return this;
    }

    public Mask rescale() {
        SegmentationProcessor.rescaleMinMax(this);
        return this;
    }

    public Mask largestRegion() {
        SegmentationProcessor.largestRegion(this);
        return this;
    }

    public Mask calculateParameters() {
        if (contour == null) return this;

        //Find centre of mass
        Moments m = Imgproc.moments(binary, true);
        centreOfMass = new Point(m.m10 / m.m00, m.m01 / m.m00);

        //Find maximum radius
        Point[] contourPoints = contour.toArray();
        double maxRadius = 0;
        double maxAngle = 0;
        for (int i = 0; i < contourPoints.length; i++) {
            double radius = Math.sqrt(Math.pow(contourPoints[i].x - centreOfMass.x, 2) + Math.pow(contourPoints[i].y - centreOfMass.y, 2));
            if (radius > maxRadius) {
                maxRadius = radius;
                maxAngle = Math.atan2(contourPoints[i].y - centreOfMass.y, contourPoints[i].x - centreOfMass.x);
            }
        }

        this.maxRadius = maxRadius;
        maxRadiusAngle = maxAngle;
        return this;
    }

    public Mask forDisplay() {
        DisplayProcessor.forDisplay(this);
        return this;
    }
}
