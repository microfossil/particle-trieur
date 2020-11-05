package ordervschaos.particletrieur.app.models.processing.processors;

import ordervschaos.particletrieur.app.models.processing.ImageType;
import ordervschaos.particletrieur.app.models.processing.ParticleImage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.text.MessageFormat;

public class PositionAdjustmentProcessor {

    public static final double CROP_BUFFER = 1.1;

    public static void adjust(ParticleImage image, boolean rotate) {

        //Final dimensions
        int finalWidth = (int) Math.round(image.mask.maxRadius * 2 * CROP_BUFFER);
        Size finalSize = new Size(finalWidth, finalWidth);
        float halfWidth = (float) finalWidth / 2;
        Mat transform;
        if (rotate) {
            transform = getTranslationRotationScaleMatrix(new Point(halfWidth - 1, halfWidth - 1), image.mask.centreOfMass, -image.mask.maxRadiusAngle, 1);
        } else {
            transform = getTranslationRotationScaleMatrix(new Point(halfWidth - 1, halfWidth - 1), image.mask.centreOfMass, 0, 1);
        }
        Scalar borderValue = BorderRemovalProcessor.getMedianOfBorder32F(image.workingImage);
        Imgproc.warpAffine(image.workingImage, image.workingImage, transform, finalSize, Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, borderValue);

//        System.out.println(MessageFormat.format("- size changed from {0},{1} to {2},{3}",
//                image.colourImage.cols(),
//                image.colourImage.rows(),
//                image.workingImage.cols(),
//                image.workingImage.cols()));
    }

    public static void removeBackground(ParticleImage image, int margin) {

        //Smoothed mask
        Mat maskSmoothed = image.mask.binaryF.clone();

        //Background
        int dilationRadius = margin;
        double gaussianSigma = margin / 2 + 0.01;
        Mat se = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(dilationRadius * 2 + 1, dilationRadius * 2 + 1));
        Imgproc.dilate(maskSmoothed, maskSmoothed, se);
        Imgproc.GaussianBlur(maskSmoothed, maskSmoothed, new Size(0, 0), gaussianSigma);

        //Apply mask to image
        if (image.workingImage.channels() == 3) {
            Mat temp = new Mat();
            Imgproc.cvtColor(maskSmoothed, temp, Imgproc.COLOR_GRAY2BGR);
            Core.multiply(image.workingImage, temp, image.workingImage);
            if (image.type == ImageType.DARKONLIGHT) {
                Mat white = temp.clone();
                white.setTo(new Scalar(1.0, 1.0, 1.0));
                Core.subtract(white, temp, white);
                Core.multiply(white, new Scalar(255.0, 255.0, 255.0), white);
                Core.add(image.workingImage, white, image.workingImage);
                white.release();
            }
            temp.release();
        } else {
            Core.multiply(image.workingImage, maskSmoothed, image.workingImage);
            if (image.type == ImageType.DARKONLIGHT) {
                Mat maskClone = maskSmoothed.clone();
                Mat white = Mat.ones(maskClone.size(), CvType.CV_32FC(1));
                Core.subtract(white, maskClone, white);
                Core.multiply(white, new Scalar(255), white);
                Core.add(image.workingImage, white, image.workingImage);
                white.release();
                maskClone.release();
            }
        }
    }

    public static Mat getTranslationRotationScaleMatrix(Point imageCentre, Point rotationCentre, double rotation, double scale) {
        float cosTheta = (float) Math.cos(rotation);
        float sinTheta = (float) Math.sin(rotation);

        float[] C = {1, 0, (float) imageCentre.x, 0, 1, (float) imageCentre.y, 0, 0, 1};
        float[] T = {1, 0, (float) -rotationCentre.x, 0, 1, (float) -rotationCentre.y, 0, 0, 1};
        float[] R = {cosTheta, -sinTheta, 0, sinTheta, cosTheta, 0, 0, 0, 1};
        float[] S = {(float) scale, 0, 0, 0, (float) scale, 0, 0, 0, 1};

        Mat Tmat = Mat.zeros(3, 3, CvType.CV_32F);
        Mat Rmat = Mat.zeros(3, 3, CvType.CV_32F);
        Mat Smat = Mat.zeros(3, 3, CvType.CV_32F);
        Mat Cmat = Mat.zeros(3, 3, CvType.CV_32F);

        Mat blankMat = Mat.zeros(3, 3, CvType.CV_32F);

        Tmat.put(0, 0, T);
        Rmat.put(0, 0, R);
        Smat.put(0, 0, S);
        Cmat.put(0, 0, C);

        Mat result = Mat.zeros(3, 3, CvType.CV_32F);
        Core.gemm(Cmat, Smat, 1, blankMat, 0, result);
        Core.gemm(result, Rmat, 1, blankMat, 0, result);
        Core.gemm(result, Tmat, 1, blankMat, 0, result);
        return new Mat(result, new Rect(0, 0, 3, 2));

    }
}
