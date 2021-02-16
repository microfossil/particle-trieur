package main.java.app.models.processing.processors;

import main.java.app.services.network.FCNNSegmenterService;
import main.java.app.models.processing.ImageType;
import main.java.app.models.processing.Mask;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class SegmentationProcessor {

    public SegmentationProcessor() {

    }

    public static void rescaleMinMax(Mask mask) {
        Core.normalize(mask.image, mask.image, 0, 1, Core.NORM_MINMAX, CvType.CV_32F);
    }

    public static void enhanceEdges(Mask mask, ImageType imageType, int scale) {
        Mat image = mask.image.clone();
        int maxDimension = image.width() > image.height() ? image.width() : image.height();
        int kernelSize = maxDimension / 10;
        kernelSize = kernelSize % 2 == 1 ? kernelSize : kernelSize + 1;
        Imgproc.blur(image, image, new Size(kernelSize, kernelSize));
        Core.subtract(mask.image, image, image);
        Core.multiply(image, Scalar.all(5), image);
        Core.add(mask.image, image, image);
//        Core.MinMaxLocResult result = Core.minMaxLoc(image);
//        Core.multiply(image, Scalar.all(1.0/result.maxVal), image);
        mask.image.release();
        mask.image = image;
    }

    public static void segmentExperimental(Mask mask, ImageType imageType, double threshold) {
//        Mat image = mask.image.clone();
//        Imgproc.blur(image, image, new Size(31,31));
//        Core.subtract(mask.image, image, image);
//        Core.multiply(image, Scalar.all(5), image);
//        Core.add(mask.image, image, image);
//        Core.MinMaxLocResult result = Core.minMaxLoc(image);
//        Core.multiply(image, Scalar.all(255.0/result.maxVal), image);
//        image.convertTo(image, CvType.CV_8U);
//        mask.binary = image;
    }

    public static void segmentFixedIntensity(Mask mask, ImageType imageType, double threshold) {
        //Get source image
        Mat image = mask.image.clone();
        MatUtilities.printMatDetails(image);
        Core.multiply(image, Scalar.all(255), image);
        MatUtilities.printMatDetails(image);
        //Threshold
        image.convertTo(image, CvType.CV_8UC1);
        MatUtilities.printMatDetails(image);
        if (mask.binary != null) mask.binary.release();
        mask.binary = new Mat();
        if (imageType == ImageType.LIGHTONDARK) {
            threshold *= 255;
            Imgproc.threshold(image, mask.binary, threshold, 255, Imgproc.THRESH_BINARY);
        }
        else if (imageType == ImageType.DARKONLIGHT) {
            threshold *= 255;
            Imgproc.threshold(image, mask.binary, 255 - threshold, 255, Imgproc.THRESH_BINARY_INV);
        }
        else if (imageType == ImageType.GREY) {
//            mask.energy = calculateStructureTensor(mask.image, 2);
//            Core.multiply(mask.energy, Scalar.all(10), mask.energy);
//            mask.energy = getEdgeFilter31();
//            Core.rotate(mask.energy, mask.energy, Core.ROTATE_90_CLOCKWISE);
//            MatUtilities.printMatDetails(mask.energy);
            Mat border = imageBorderMask(image.size(), 4);
            MatOfDouble mean = new MatOfDouble();
            MatOfDouble stddev = new MatOfDouble();
            Core.meanStdDev(image, mean, stddev, border);
            border.release();
            int thzero = (int) mean.toArray()[0];
            Mat upper = new Mat();
//            System.out.println(thzero);
//            System.out.println(255 - (255 - thzero) * threshold);
//            System.out.println(thzero * threshold);
            Imgproc.threshold(image, upper, thzero + (255 - thzero) * threshold, 255, Imgproc.THRESH_BINARY);
            Imgproc.threshold(image, mask.binary, thzero - thzero * threshold, 255, Imgproc.THRESH_BINARY_INV);
            Core.bitwise_or(mask.binary, upper, mask.binary);
            upper.release();
        }
        morpholigcallyClose(mask.binary, 5);
        if (mask.binaryF != null) mask.binaryF.release();
        mask.binaryF = new Mat();
        mask.binary.convertTo(mask.binaryF, CvType.CV_32F);
        Core.divide(mask.binaryF, Scalar.all(255), mask.binaryF);
        MatUtilities.printMatDetails(mask.binary);
        MatUtilities.printMatDetails(mask.binaryF);
        image.release();
    }

    public static void segmentOtsuIntensity(Mask mask, ImageType imageType, double threshold) {
        //Get source image
        Mat image = mask.image.clone();
        Core.multiply(image, Scalar.all(255), image);
        //Threshold
        image.convertTo(image, CvType.CV_8UC1);
        if (mask.binary != null) mask.binary.release();
        mask.binary = new Mat();
        double th = Imgproc.threshold(image, mask.binary, 0, 255, Imgproc.THRESH_OTSU);
        threshold *= th * 2;
        if (imageType == ImageType.LIGHTONDARK)
            Imgproc.threshold(image, mask.binary, threshold, 255, Imgproc.THRESH_BINARY);
        if (imageType == ImageType.DARKONLIGHT)
            Imgproc.threshold(image, mask.binary, 255 - threshold, 255, Imgproc.THRESH_BINARY_INV);
        if (mask.binaryF != null) mask.binaryF.release();
        mask.binaryF = new Mat();
        mask.binary.convertTo(mask.binaryF, CvType.CV_32F);
        Core.divide(mask.binaryF, Scalar.all(255), mask.binaryF);
        image.release();
    }

    public static void segmentCNN(Mask mask, ImageType imageType, double threshold, FCNNSegmenterService predictionService) {
        //Get image
        Mat input = new Mat();
        mask.image.convertTo(input, CvType.CV_32F);
        //Convert to greyscale if necessary
        if (input.channels() == 3) Imgproc.cvtColor(input, input, Imgproc.COLOR_BGR2GRAY);
        //Adjust for background
        if (imageType == ImageType.DARKONLIGHT) {
            Core.multiply(input, Scalar.all(-1), input);
            Core.add(input, Scalar.all(1), input);
        }
        //Predict
        if (mask.binaryF != null) mask.binaryF.release();
        mask.binaryF = predictionService.predict(input);
        //Threshold
        //TODO make private and do release in setter
        if (mask.binary != null) mask.release();
        mask.binary = mask.binaryF.clone();
        Core.multiply(mask.binary, Scalar.all(255), mask.binary);
        mask.binary.convertTo(mask.binary, CvType.CV_8U);
        Imgproc.threshold(mask.binary, mask.binary, threshold * 255, 255, Imgproc.THRESH_BINARY);
        if (mask.binaryF != null) mask.binaryF.release();
        mask.binaryF = new Mat();
        mask.binary.convertTo(mask.binaryF, CvType.CV_8UC1);
        Core.divide(mask.binaryF, Scalar.all(255), mask.binaryF);
        input.release();
    }

    public static void largestRegion(Mask mask) {
        //Get all contours
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask.binary, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        if (contours.size() == 0) return;

        //Find the largest contour
        double maxArea = 0;
        int maxIdx = 0;
        for (int i = 0; i < contours.size(); i++) {
            double area = Imgproc.contourArea(contours.get(i));
            if (area > maxArea) {
                maxArea = area;
                maxIdx = i;
            }
        }
        if (maxArea < 30) return;

        //Convert to float valued mask. Mask will have holes filled.
        Mat binaryF = Mat.zeros(mask.binary.size(), CvType.CV_8UC1);
        Imgproc.fillPoly(binaryF, contours.subList(maxIdx, maxIdx + 1), new Scalar(1.0));

        //TODO should this happen before making the contour?
        //TODO should the contour be redone?
        //Close by one pixel
        final Mat kernel = Mat.ones(3, 3, CvType.CV_8UC1);
        Imgproc.morphologyEx(binaryF, binaryF, Imgproc.MORPH_CLOSE, kernel);

        //Contours
        MatOfPoint contour = contours.get(maxIdx);
        MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());

        if (mask.binaryF != null) mask.binaryF.release();
        mask.binaryF = new Mat();
        binaryF.convertTo(mask.binaryF, CvType.CV_32F);
        binaryF.release();
        mask.contour = contour;
        mask.contour2f = contour2f;
    }

    public static void morpholigcallyClose(Mat mat, int width) {
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(width, width));
        Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_CLOSE, kernel);
        kernel.release();
    }

    public static Mat imageBorderMask(Size size, int thickness) {
        Mat mat = Mat.ones(size, CvType.CV_8U);
        Imgproc.rectangle(mat, new Point(thickness, thickness), new Point(size.width - thickness, size.height - thickness), Scalar.all(0), -1);
        return mat;
    }

    public static Mat calculateStructureTensor(Mat mat, int blurSigma) {
        int wid = blurSigma * 6 + 1;

        //Derivatives
        Mat x = mat.clone();
        Mat y = mat.clone();

        //Filter with log-gabor kernels
        Mat hKernel = getEdgeFilter31();
        Mat vKernel = new Mat();
        Core.rotate(hKernel, vKernel, Core.ROTATE_90_CLOCKWISE);
        Imgproc.filter2D(x, x, CvType.CV_32F, vKernel);
//        x = x.mul(x);
        Core.multiply(x, x, x);
        Imgproc.filter2D(y, y, CvType.CV_32F, hKernel);
        Core.multiply(y, y, y);
//        y = y.mul(y);

        Mat energy = new Mat();
        Core.add(x, y, energy);
        Core.sqrt(energy, energy);

//        Mat gaussianKernel = Imgproc.getGaussianKernel(wid, blurSigma);
//        Imgproc.filter2D(energy, energy, CvType.CV_32F, gaussianKernel);
        Imgproc.blur(energy, energy, new Size(wid, wid));
//        gaussianKernel.release();
        x.release();
        y.release();
        return energy;
    }

    private static Mat getEdgeFilter31() {
        float[] filter = new float[]
                {
                        -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0001F,
                        -0.0001F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0001F,
                        0.0001F, 0.0001F, 0.0001F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, -0.0000F,
                        -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F,
                        -0.0001F, -0.0000F, -0.0000F, -0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0001F, 0.0001F, 0.0001F,
                        0.0001F, 0.0001F, 0.0001F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, -0.0000F, -0.0000F,
                        -0.0000F, -0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F,
                        -0.0001F, -0.0000F, -0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F,
                        0.0001F, 0.0001F, 0.0001F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0000F,
                        -0.0000F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F,
                        -0.0001F, -0.0001F, 0.0000F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F,
                        0.0001F, 0.0001F, 0.0001F, 0.0000F, 0.0000F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0001F,
                        -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0002F, -0.0002F, -0.0002F, -0.0001F, -0.0001F,
                        -0.0000F, 0.0000F, 0.0000F, 0.0001F, 0.0001F, 0.0002F, 0.0002F, 0.0002F, 0.0001F, 0.0001F, 0.0001F,
                        0.0001F, 0.0001F, 0.0001F, 0.0000F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0001F,
                        -0.0001F, -0.0002F, -0.0002F, -0.0002F, -0.0002F, -0.0002F, -0.0002F, -0.0002F, -0.0001F, -0.0001F,
                        0.0000F, 0.0001F, 0.0001F, 0.0002F, 0.0002F, 0.0002F, 0.0002F, 0.0002F, 0.0002F, 0.0002F, 0.0001F,
                        0.0001F, 0.0001F, 0.0001F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0001F, -0.0002F,
                        -0.0002F, -0.0003F, -0.0003F, -0.0003F, -0.0003F, -0.0003F, -0.0003F, -0.0002F, -0.0000F, 0.0000F,
                        0.0000F, 0.0002F, 0.0003F, 0.0003F, 0.0003F, 0.0003F, 0.0003F, 0.0003F, 0.0002F, 0.0002F, 0.0001F,
                        0.0001F, 0.0001F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0002F, -0.0002F,
                        -0.0003F, -0.0003F, -0.0004F, -0.0004F, -0.0005F, -0.0005F, -0.0004F, -0.0003F, -0.0003F, 0.0000F,
                        0.0003F, 0.0003F, 0.0004F, 0.0005F, 0.0005F, 0.0004F, 0.0004F, 0.0003F, 0.0003F, 0.0002F, 0.0002F,
                        0.0001F, 0.0001F, 0.0000F, 0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0002F, -0.0002F, -0.0003F,
                        -0.0004F, -0.0005F, -0.0005F, -0.0006F, -0.0007F, -0.0007F, -0.0005F, -0.0004F, -0.0001F, 0.0000F,
                        0.0001F, 0.0004F, 0.0005F, 0.0007F, 0.0007F, 0.0006F, 0.0005F, 0.0005F, 0.0004F, 0.0003F, 0.0002F,
                        0.0002F, 0.0001F, 0.0001F, 0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0002F, -0.0003F, -0.0004F,
                        -0.0005F, -0.0006F, -0.0007F, -0.0008F, -0.0009F, -0.0008F, -0.0007F, -0.0005F, -0.0004F, 0.0000F,
                        0.0004F, 0.0005F, 0.0007F, 0.0008F, 0.0009F, 0.0008F, 0.0007F, 0.0006F, 0.0005F, 0.0004F, 0.0003F,
                        0.0002F, 0.0001F, 0.0001F, 0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0002F, -0.0003F, -0.0005F,
                        -0.0006F, -0.0008F, -0.0009F, -0.0011F, -0.0011F, -0.0010F, -0.0007F, -0.0003F, 0.0002F, 0.0000F,
                        -0.0002F, 0.0003F, 0.0007F, 0.0010F, 0.0011F, 0.0011F, 0.0009F, 0.0008F, 0.0006F, 0.0005F, 0.0003F,
                        0.0002F, 0.0001F, 0.0001F, 0.0000F, -0.0000F, -0.0001F, -0.0002F, -0.0003F, -0.0004F, -0.0005F,
                        -0.0007F, -0.0009F, -0.0011F, -0.0012F, -0.0012F, -0.0008F, -0.0001F, 0.0008F, 0.0005F, 0.0000F,
                        -0.0005F, -0.0008F, 0.0001F, 0.0008F, 0.0012F, 0.0012F, 0.0011F, 0.0009F, 0.0007F, 0.0005F, 0.0004F,
                        0.0003F, 0.0002F, 0.0001F, 0.0000F, -0.0000F, -0.0001F, -0.0002F, -0.0003F, -0.0004F, -0.0006F,
                        -0.0008F, -0.0011F, -0.0013F, -0.0014F, -0.0011F, -0.0001F, 0.0022F, 0.0048F, 0.0057F, 0.0000F,
                        -0.0057F, -0.0048F, -0.0022F, 0.0001F, 0.0011F, 0.0014F, 0.0013F, 0.0011F, 0.0008F, 0.0006F, 0.0004F,
                        0.0003F, 0.0002F, 0.0001F, 0.0000F, -0.0000F, -0.0001F, -0.0002F, -0.0003F, -0.0005F, -0.0007F,
                        -0.0009F, -0.0012F, -0.0015F, -0.0014F, -0.0008F, 0.0016F, 0.0072F, 0.0169F, 0.0194F, 0.0000F, -0.0194F,
                        -0.0169F, -0.0072F, -0.0016F, 0.0008F, 0.0014F, 0.0015F, 0.0012F, 0.0009F, 0.0007F, 0.0005F, 0.0003F,
                        0.0002F, 0.0001F, 0.0000F, 0.0007F, -0.0008F, 0.0005F, -0.0011F, 0.0003F, -0.0016F, -0.0001F, -0.0023F,
                        -0.0004F, -0.0027F, 0.0011F, 0.0020F, 0.0171F, 0.0388F, 0.0791F, 0.0000F, -0.0791F, -0.0388F, -0.0171F,
                        -0.0020F, -0.0011F, 0.0027F, 0.0004F, 0.0023F, 0.0001F, 0.0016F, -0.0003F, 0.0011F, -0.0005F, 0.0008F,
                        -0.0007F, 0.0025F, -0.0027F, 0.0024F, -0.0030F, 0.0023F, -0.0037F, 0.0022F, -0.0047F, 0.0023F, -0.0058F,
                        0.0049F, -0.0014F, 0.0272F, 0.0484F, 0.1544F, 0.0000F, -0.1544F, -0.0484F, -0.0272F, 0.0014F, -0.0049F,
                        0.0058F, -0.0023F, 0.0047F, -0.0022F, 0.0037F, -0.0023F, 0.0030F, -0.0024F, 0.0027F, -0.0025F, 0.0007F,
                        -0.0008F, 0.0005F, -0.0011F, 0.0003F, -0.0016F, -0.0001F, -0.0023F, -0.0004F, -0.0027F, 0.0011F,
                        0.0020F, 0.0171F, 0.0388F, 0.0791F, 0.0000F, -0.0791F, -0.0388F, -0.0171F, -0.0020F, -0.0011F, 0.0027F,
                        0.0004F, 0.0023F, 0.0001F, 0.0016F, -0.0003F, 0.0011F, -0.0005F, 0.0008F, -0.0007F, -0.0000F, -0.0001F,
                        -0.0002F, -0.0003F, -0.0005F, -0.0007F, -0.0009F, -0.0012F, -0.0015F, -0.0014F, -0.0008F, 0.0016F,
                        0.0072F, 0.0169F, 0.0194F, 0.0000F, -0.0194F, -0.0169F, -0.0072F, -0.0016F, 0.0008F, 0.0014F, 0.0015F,
                        0.0012F, 0.0009F, 0.0007F, 0.0005F, 0.0003F, 0.0002F, 0.0001F, 0.0000F, -0.0000F, -0.0001F, -0.0002F,
                        -0.0003F, -0.0004F, -0.0006F, -0.0008F, -0.0011F, -0.0013F, -0.0014F, -0.0011F, -0.0001F, 0.0022F,
                        0.0048F, 0.0057F, 0.0000F, -0.0057F, -0.0048F, -0.0022F, 0.0001F, 0.0011F, 0.0014F, 0.0013F, 0.0011F,
                        0.0008F, 0.0006F, 0.0004F, 0.0003F, 0.0002F, 0.0001F, 0.0000F, -0.0000F, -0.0001F, -0.0002F, -0.0003F,
                        -0.0004F, -0.0005F, -0.0007F, -0.0009F, -0.0011F, -0.0012F, -0.0012F, -0.0008F, -0.0001F, 0.0008F,
                        0.0005F, 0.0000F, -0.0005F, -0.0008F, 0.0001F, 0.0008F, 0.0012F, 0.0012F, 0.0011F, 0.0009F, 0.0007F,
                        0.0005F, 0.0004F, 0.0003F, 0.0002F, 0.0001F, 0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0002F, -0.0003F,
                        -0.0005F, -0.0006F, -0.0008F, -0.0009F, -0.0011F, -0.0011F, -0.0010F, -0.0007F, -0.0003F, 0.0002F,
                        0.0000F, -0.0002F, 0.0003F, 0.0007F, 0.0010F, 0.0011F, 0.0011F, 0.0009F, 0.0008F, 0.0006F, 0.0005F,
                        0.0003F, 0.0002F, 0.0001F, 0.0001F, 0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0002F, -0.0003F, -0.0004F,
                        -0.0005F, -0.0006F, -0.0007F, -0.0008F, -0.0009F, -0.0008F, -0.0007F, -0.0005F, -0.0004F, 0.0000F,
                        0.0004F, 0.0005F, 0.0007F, 0.0008F, 0.0009F, 0.0008F, 0.0007F, 0.0006F, 0.0005F, 0.0004F, 0.0003F,
                        0.0002F, 0.0001F, 0.0001F, 0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0002F, -0.0002F, -0.0003F,
                        -0.0004F, -0.0005F, -0.0005F, -0.0006F, -0.0007F, -0.0007F, -0.0005F, -0.0004F, -0.0001F, 0.0000F,
                        0.0001F, 0.0004F, 0.0005F, 0.0007F, 0.0007F, 0.0006F, 0.0005F, 0.0005F, 0.0004F, 0.0003F, 0.0002F,
                        0.0002F, 0.0001F, 0.0001F, 0.0000F, -0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0002F, -0.0002F,
                        -0.0003F, -0.0003F, -0.0004F, -0.0004F, -0.0005F, -0.0005F, -0.0004F, -0.0003F, -0.0003F, 0.0000F,
                        0.0003F, 0.0003F, 0.0004F, 0.0005F, 0.0005F, 0.0004F, 0.0004F, 0.0003F, 0.0003F, 0.0002F, 0.0002F,
                        0.0001F, 0.0001F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0001F, -0.0002F,
                        -0.0002F, -0.0003F, -0.0003F, -0.0003F, -0.0003F, -0.0003F, -0.0003F, -0.0002F, -0.0000F, 0.0000F,
                        0.0000F, 0.0002F, 0.0003F, 0.0003F, 0.0003F, 0.0003F, 0.0003F, 0.0003F, 0.0002F, 0.0002F, 0.0001F,
                        0.0001F, 0.0001F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0001F, -0.0001F,
                        -0.0002F, -0.0002F, -0.0002F, -0.0002F, -0.0002F, -0.0002F, -0.0002F, -0.0001F, -0.0001F, 0.0000F,
                        0.0001F, 0.0001F, 0.0002F, 0.0002F, 0.0002F, 0.0002F, 0.0002F, 0.0002F, 0.0002F, 0.0001F, 0.0001F,
                        0.0001F, 0.0001F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0001F, -0.0001F, -0.0001F,
                        -0.0001F, -0.0001F, -0.0001F, -0.0002F, -0.0002F, -0.0002F, -0.0001F, -0.0001F, -0.0000F, 0.0000F,
                        0.0000F, 0.0001F, 0.0001F, 0.0002F, 0.0002F, 0.0002F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F,
                        0.0001F, 0.0000F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0001F, -0.0001F,
                        -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, 0.0000F,
                        0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F,
                        0.0000F, 0.0000F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0001F,
                        -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0000F, -0.0000F, 0.0000F,
                        0.0000F, 0.0000F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0000F,
                        0.0000F, 0.0000F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F,
                        -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0000F, -0.0000F, -0.0000F, 0.0000F,
                        0.0000F, 0.0000F, 0.0000F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0000F, 0.0000F,
                        0.0000F, 0.0000F, 0.0000F, 0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, -0.0000F,
                        -0.0000F, -0.0001F, -0.0001F, -0.0001F, -0.0001F, -0.0000F, -0.0000F, -0.0000F, -0.0000F, 0.0000F,
                        0.0000F, 0.0000F, 0.0000F, 0.0000F, 0.0001F, 0.0001F, 0.0001F, 0.0001F, 0.0000F, 0.0000F, 0.0000F,
                        0.0000F, 0.0000F, 0.0000F, 0.0000F
                };
        Mat mat = Mat.zeros(31, 31, CvType.CV_32FC1);
        mat.put(0, 0, filter);
        return mat;
    }
}
