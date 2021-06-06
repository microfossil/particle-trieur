package particletrieur.models.processing.processors;

import particletrieur.models.processing.ParticleImage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class BorderRemovalProcessor {

    public static void removeBorder(ParticleImage image, boolean borderColourIsWhite) {
        //Get source image
        Mat input = image.workingImage.clone();
        if (input.channels() == 3) Imgproc.cvtColor(input, input, Imgproc.COLOR_BGR2GRAY);
        input.convertTo(input, CvType.CV_8UC1);

        //Create a mask
        Mat mask = new Mat();
        if (borderColourIsWhite) { //White border
            Imgproc.threshold(input, mask, 230, 255, Imgproc.THRESH_BINARY_INV);
        }
        else { //Black border
            Imgproc.threshold(input, mask, 2, 255, Imgproc.THRESH_BINARY);
        }
        final Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);

        //Find maximum region
        MatOfPoint contour = MatUtilities.findLargestContourInMask(mask);
        if (contour.rows() > 0) {
            Rect rect = Imgproc.boundingRect(contour);
            rect.x += 1;
            rect.y += 1;
            rect.width -= 2;
            rect.height -= 2;

            contour.release();
            input.release();
            mask.release();

            //Crop images
            image.colourImage = (new Mat(image.colourImage, rect)).clone();
            image.greyscaleImage = (new Mat(image.greyscaleImage, rect)).clone();
            image.workingImage = (new Mat(image.workingImage, rect)).clone();
        }
    }

    public static void removeWhiteBorder(ParticleImage image) {
        removeBorder(image, true);
    }

    public static void removeBlackBorder(ParticleImage image) {
        removeBorder(image, false);
    }

    public static void resizeByMaximumDimension(Mat mat, int squareLength) {
        double width = mat.width();
        double height = mat.height();

        if (width > height) {
            height = height * width / squareLength;
            width = squareLength;
        }
        else if (height > width) {
            width = width * height / squareLength;
            height = squareLength;
        }
        else {
            width = squareLength;
            height = squareLength;
        }
        Imgproc.resize(mat, mat, new Size(Math.round(width), Math.round(height)));
    }

    public static void resizeByMaximumDimension(ParticleImage image, int squareLength) {
        resizeByMaximumDimension(image.workingImage, squareLength);
    }

    public static void makeSquareUsingReplicate(ParticleImage image) {
        int width = image.workingImage.width();
        int height = image.workingImage.height();
        int top = 0;
        int bottom = 0;
        int left = 0;
        int right = 0;
        if (width > height) {
            top = (width - height) / 2;
            bottom = (width - height + 1) / 2;
        } else if (height > width) {
            left = (height - width) / 2;
            right = (height - width + 1) / 2;
        }
        if (width != height) {
//            Core.copyMakeBorder(image.colourImage, image.colourImage, top, bottom, left, right, Core.BORDER_REPLICATE);
//            Core.copyMakeBorder(image.greyscaleImage, image.greyscaleImage, top, bottom, left, right, Core.BORDER_REPLICATE);
            Core.copyMakeBorder(image.workingImage, image.workingImage, top, bottom, left, right, Core.BORDER_REPLICATE);
        }
    }

    public static void makeSquareUsingMedianOfBorder(ParticleImage image) {

        Scalar colourBorder = getMedianOfBorder8U(image.colourImage);
        Scalar greyscaleBorder = getMedianOfBorder8U(image.greyscaleImage);
        Scalar workingBorder = getMedianOfBorder32F(image.workingImage);

//        System.out.println(String.format("Colour border is: %.0f %.0f %.0f", colourBorder.val[0], colourBorder.val[1], colourBorder.val[2]));
//        System.out.println(String.format("Greyscale border is: %.0f %.0f %.0f", greyscaleBorder.val[0], greyscaleBorder.val[1], greyscaleBorder.val[2]));
//        System.out.println(String.format("Working border is: %.0f %.0f %.0f", workingBorder.val[0], workingBorder.val[1], workingBorder.val[2]));

        int width = image.workingImage.width();
        int height = image.workingImage.height();
        int top = 0;
        int bottom = 0;
        int left = 0;
        int right = 0;
        if (width > height) {
            top = (width - height) / 2;
            bottom = (width - height + 1) / 2;
        } else if (height > width) {
            left = (height - width) / 2;
            right = (height - width + 1) / 2;
        }
        if (width != height) {
            Core.copyMakeBorder(image.colourImage, image.colourImage, top, bottom, left, right, Core.BORDER_CONSTANT, colourBorder);
            Core.copyMakeBorder(image.greyscaleImage, image.greyscaleImage, top, bottom, left, right, Core.BORDER_CONSTANT, greyscaleBorder);
            Core.copyMakeBorder(image.workingImage, image.workingImage, top, bottom, left, right, Core.BORDER_CONSTANT, workingBorder);
        }
    }

    public static Scalar getMedianOfBorder32F(Mat mat) {
        float[] pixels = new float[mat.rows() * mat.cols() * mat.channels()];
        mat.get(0,0, pixels);

        float[] borderValue = new float[mat.channels()];
        int channels = mat.channels();

//        float[] borderPixels = new float[(2 * mat.rows() + 2 * mat.cols()) * channels];
//        int ptr = 0;

        for (int ch = 0; ch < channels; ch++) {

            float[] borderPixels = new float[2 * mat.rows() + 2 * mat.cols()];
            int ptr = 0;

            //Top and bottom row
            int lastRowOffset = (mat.rows() - 1) * mat.cols() * channels;
            for (int i = 0; i < mat.cols(); i++) {
                borderPixels[ptr] = pixels[i * channels + ch];
                ptr++;
                borderPixels[ptr] = pixels[i * channels + lastRowOffset];
                ptr++;
            }

            //Sides
            int lastColOffset = (mat.cols() - 1) * channels;
            for (int i = 0; i < mat.rows(); i++) {
                borderPixels[ptr] = pixels[i * channels * mat.cols() + ch];
                ptr++;
                borderPixels[ptr] = pixels[i * channels * mat.cols() + lastColOffset + ch];
                ptr++;
            }

            Arrays.sort(borderPixels);
            borderValue[ch] = borderPixels[borderPixels.length/2];
        }

        if (channels == 3) {
            return new Scalar(borderValue[0], borderValue[1], borderValue[2]);
        }
        else {
            return new Scalar(borderValue[0]);
        }
    }

    public static Scalar getMedianOfBorder8U(Mat mat) {
        Mat temp = new Mat();
        mat.convertTo(temp, CvType.CV_32F);
        Scalar borderValue = getMedianOfBorder32F(temp);
        temp.release();
        return borderValue;
    }
}
