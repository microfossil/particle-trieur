package main.java.app.models.processing.processors;

import main.java.app.models.processing.ParticleImage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class Preprocessor {

    public static void rescale(ParticleImage input, double divisor) {
        Core.divide(input.workingImage, Scalar.all(divisor), input.workingImage);
    }

    public static Mat resizeAndRescale(Mat input, double divisor, int height, int width, int channels) {
        Mat output = new Mat();
        input.convertTo(output, CvType.CV_32F);
        Core.divide(output, Scalar.all(divisor), output);
        if (input.channels() == 3 && channels == 1) {
            Imgproc.cvtColor(output, output, Imgproc.COLOR_BGR2GRAY);
        }
        if (input.rows() != height || input.cols() != width) {
            Imgproc.resize(output, output, new Size(width, height));
        }
        return output;
    }

    public static Mat resize(Mat input, int height, int width, int channels) {
        Mat output = input.clone();
        if (input.channels() == 3 && channels == 1) {
            Imgproc.cvtColor(output, output, Imgproc.COLOR_BGR2GRAY);
        }
        if (input.rows() != height || input.cols() != width) {
            Imgproc.resize(output, output, new Size(width, height));
        }
        return output;
    }
}
