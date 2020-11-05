package ordervschaos.particletrieur.app.models.processing.processors;

import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.util.ArrayList;

public class MatUtilities {

    public static Mat imread(String filename) throws IOException {
        FileInputStream stream = new FileInputStream(filename);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] temporaryImageInMemory = buffer.toByteArray();
        buffer.close();
        stream.close();
        return Imgcodecs.imdecode(new MatOfByte(temporaryImageInMemory), Imgcodecs.CV_LOAD_IMAGE_COLOR);
    }

    public static MatOfPoint findLargestContourInMask(Mat mask) {
        //Find maximum region
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = 0;
        int maxIdx = 0;
        for (int i = 0; i < contours.size(); i++) {
            double area = Imgproc.contourArea(contours.get(i));
            if (area > maxArea) {
                maxArea = area;
                maxIdx = i;
            }
        }
        return contours.get(maxIdx);
    }

    public static Mat createTranslationRotationScaleMatrix(Point imageCentre, Point rotationCentre, double rotation, double scale) {
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

    public static Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public static Image mat2Image(Mat mat, double rescale) {
        Mat frame = new Mat();
        Core.multiply(mat, Scalar.all(rescale), frame);
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        frame.release();
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public static void printMatDetails(Mat mat) {
//        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(mat);
//        System.out.println(String.format(
//                "Min: %.2f, Max: %.2f",
//                minMaxLocResult.minVal,
//                minMaxLocResult.maxVal
//        ));
    }
}
