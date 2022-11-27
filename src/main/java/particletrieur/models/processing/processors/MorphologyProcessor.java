package particletrieur.models.processing.processors;

import particletrieur.models.processing.Mask;
import particletrieur.models.processing.Morphology;
import particletrieur.models.processing.ParticleImage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;

public class MorphologyProcessor {

    public static Morphology calculateMorphology(ParticleImage image) {

        Morphology m = new Morphology();

        Mask mask = image.mask;
        Mat greyscale;
//        if (image.workingImage.channels() == 3) {
//            greyscale = new Mat();
//            Imgproc.cvtColor(image.workingImage, greyscale, Imgproc.COLOR_BGR2GRAY);
//        }
//        else {
            greyscale = image.greyscaleImage.clone();
//        }
        Mat binary = mask.binary;
        Mat binaryF = mask.binaryF;
        MatOfPoint contour = mask.contour;
        MatOfPoint2f contour2f = mask.contour2f;

        //Exit if mask is non-existant or too small
        if (contour == null) {
            image.morphology = m;
            return null;
        }
        m.area = Imgproc.contourArea(contour);
        if (m.area < 25) {
            image.morphology = m;
            return null;
        }

        m.width = image.workingImage.width();
        m.height = image.workingImage.height();

        m.meanDiameter = Math.sqrt(m.area / Math.PI) * 2;

        //
        // IMAGE INTENSITY MOMENTS
        //
        //Mean(0) and standard deviation(1)
        MatOfDouble meanMat = new MatOfDouble();
        MatOfDouble stddevMat = new MatOfDouble();
        Core.meanStdDev(greyscale, meanMat, stddevMat, binary);
        m.mean = meanMat.get(0, 0)[0];
        m.stddev = stddevMat.get(0, 0)[0];
        m.stddevInvariant = m.stddev / m.mean;
        meanMat.release();
        stddevMat.release();

        double variance = Math.pow(m.stddev,2);
        double numPixelsInMask = Core.sumElems(binaryF).val[0];

        Mat greyscaleMasked = new Mat();

        //Mean
        Core.multiply(binaryF, greyscale, greyscaleMasked, 1.0, CvType.CV_32FC1);
        m.mean = Core.sumElems(greyscaleMasked).val[0] / numPixelsInMask;

        //Subtract mean
        Core.subtract(greyscaleMasked, new Scalar(m.mean), greyscaleMasked);
        Core.multiply(greyscaleMasked, binaryF, greyscaleMasked, 1.0, CvType.CV_32FC1);

        //Standard deviation
        Mat temp = new Mat();
        Core.pow(greyscaleMasked, 2, temp);
        double tempSum = Core.sumElems(temp).val[0];
        m.stddev = Math.sqrt(tempSum / numPixelsInMask);

        //Skew
        Core.pow(greyscaleMasked, 3, temp);
        tempSum = Core.sumElems(temp).val[0];
        m.skew = tempSum / Math.pow(m.stddev, 3) / numPixelsInMask;

        //Kurtosis
        Core.pow(greyscaleMasked, 4, temp);
        tempSum = Core.sumElems(temp).val[0];
        m.kurtosis = tempSum / Math.pow(m.stddev, 4) / numPixelsInMask;

        //5th moment
        Core.pow(greyscaleMasked, 5, temp);
        tempSum = Core.sumElems(temp).val[0];
        m.moment5 = tempSum / Math.pow(m.stddev, 5) / numPixelsInMask;

        //6th moment
        Core.pow(greyscaleMasked, 6, temp);
        tempSum = Core.sumElems(temp).val[0];
        m.moment6 = tempSum / Math.pow(m.stddev, 6) / numPixelsInMask;

        temp.release();
        greyscaleMasked.release();


        //
        // MASK DIMENSIONS
        //
        //Area and perimeter
        m.perimeter = Imgproc.arcLength(contour2f, true);

        //Convex area and perimeter
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(contour, hull);
        Point[] temp1 = contour.toArray();
        int[] temp2 = hull.toArray();
        ArrayList<Point> tempPoints = new ArrayList<>();
        for (int tempIdx : temp2) {
            tempPoints.add(temp1[tempIdx]);
        }
        MatOfPoint convexContour = new MatOfPoint(tempPoints.toArray(new Point[tempPoints.size()]));

        m.convexArea = Imgproc.contourArea(convexContour);
        m.convexPerimeter = Imgproc.arcLength(new MatOfPoint2f(convexContour.toArray()), true);
        m.convexPerimeterToPerimeterRatio = m.convexPerimeter / m.perimeter;
        m.convexAreaToAreaRatio = m.convexArea / m.area;

        hull.release();
        convexContour.release();

        //
        // ELLIPSE AND CIRCLE
        //
        //Enclosing circle
        Point centre = new Point();
        float[] radius = new float[1];
        Imgproc.minEnclosingCircle(contour2f, centre, radius);
        m.circleRadius = radius[0];
        m.circleArea = radius[0] * radius[0] * Math.PI;

        //Approximating ellipse
        if(contour2f.rows() >= 5) {
            RotatedRect ellipseRect = Imgproc.fitEllipse(contour2f);
            m.majorAxisLength = ellipseRect.size.height;
            m.minorAxisLength = ellipseRect.size.width;
            m.eccentricity = Math.sqrt(1 - Math.pow(m.minorAxisLength, 2) / Math.pow(m.majorAxisLength, 2));
            m.angle = ellipseRect.angle;
            m.roundness = 4.0 * m.area / Math.PI / Math.pow(m.majorAxisLength,2);
            m.elongation = m.majorAxisLength / m.minorAxisLength;
        }

        //
        // COMMON MEASUREMENTS
        //
        m.solidity = m.area / m.convexArea;
        m.circularity = 4.0 * Math.PI * m.area / Math.pow(m.perimeter, 2);
        m.perimeterToAreaRatio = m.perimeter / m.area;
        m.equivalentDiameter = Math.sqrt(4.0 * m.area / Math.PI);
        m.equivalentSphericalDiameter = 2.0 * Math.sqrt(m.area / Math.PI);

        Rect boundingRectangle = Imgproc.boundingRect(contour);
        m.aspectRatio = (double)boundingRectangle.width / boundingRectangle.height;
        m.areaToBoundingRectangleArea = m.area / (boundingRectangle.width * boundingRectangle.height);

        double[] dHuInvariants = computeHuInvariants(contour);
        m.Husmoment1 = dHuInvariants[0];
        m.Husmoment2 = dHuInvariants[1];
        m.Husmoment3 = dHuInvariants[2];
        m.Husmoment4 = dHuInvariants[3];
        m.Husmoment5 = dHuInvariants[4];
        m.Husmoment6 = dHuInvariants[5];
        m.Husmoment7 = dHuInvariants[6];

        return m;
    }

    private static double[] computeHuInvariants(MatOfPoint contour){
        Moments p = Imgproc.moments(contour);
        double
                n20 = p.get_nu20(),
                n02 = p.get_nu02(),
                n30 = p.get_nu30(),
                n12 = p.get_nu12(),
                n21 = p.get_nu21(),
                n03 = p.get_nu03(),
                n11 = p.get_nu11();

        double[] dHuInvariants = new double[8];

        //First moment
        dHuInvariants[0] = n20 + n02;

        //Second moment
        dHuInvariants[1] = Math.pow((n20 - n02), 2) + Math.pow(2 * n11, 2);

        //Third moment
        dHuInvariants[2] = Math.pow(n30 - (3 * (n12)), 2)
                + Math.pow((3 * n21 - n03), 2);

        //Fourth moment
        dHuInvariants[3] = Math.pow((n30 + n12), 2) + Math.pow((n12 + n03), 2);

        //Fifth moment
        dHuInvariants[4] = (n30 - 3 * n12) * (n30 + n12)
                * (Math.pow((n30 + n12), 2) - 3 * Math.pow((n21 + n03), 2))
                + (3 * n21 - n03) * (n21 + n03)
                * (3 * Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2));

        //Sixth moment
        dHuInvariants[5] = (n20 - n02)
                * (Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2))
                + 4 * n11 * (n30 + n12) * (n21 + n03);

        //Seventh moment
        dHuInvariants[6] = (3 * n21 - n03) * (n30 + n12)
                * (Math.pow((n30 + n12), 2) - 3 * Math.pow((n21 + n03), 2))
                + (n30 - 3 * n12) * (n21 + n03)
                * (3 * Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2));

        //Eighth moment
        dHuInvariants[7] = n11 * (Math.pow((n30 + n12), 2) - Math.pow((n03 + n21), 2))
                - (n20 - n02) * (n30 + n12) * (n03 + n21);

        return dHuInvariants;

    }

}
