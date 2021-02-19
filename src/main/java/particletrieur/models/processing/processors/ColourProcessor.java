package particletrieur.models.processing.processors;

import particletrieur.models.processing.ImageType;
import particletrieur.models.processing.ParticleImage;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class ColourProcessor {

    /**
     * Normalise the image, using its mask if needed
     *
     * Note that it is assumed the image has ALREADY been rescaled to [0,1]
     * @param input
     * @param contrast
     */
    public static void normalise(ParticleImage input, double contrast, boolean useMask) {

        //Get the working image
        Mat workingImage = input.workingImage;
        ImageType type = input.type;

        //Greyscale if necessary
        Mat norm;
        if (workingImage.channels() == 1) {
            norm = workingImage.clone();
        }
        else {
            norm = new Mat();
            Imgproc.cvtColor(workingImage, norm, Imgproc.COLOR_BGR2GRAY);
        }

        //Invert if dark on light
//        if (type == ImageType.DARKONLIGHT) {
//            Mat ones = Mat.ones(norm.rows(),norm.cols(), norm.type());
//            Core.subtract(ones, norm, norm);
//            ones.release();
//        }

        if (useMask) {
            // - apply mask
            Core.multiply(norm, input.mask.binaryF, norm, 1.0, CvType.CV_32F);
        }



        int size = norm.rows() * norm.cols();
        // - get values
        float[] patch = new float[size];
        norm.get(0, 0, patch);
        // - sort
        Arrays.sort(patch);
        int start = 0;
        if (useMask) {
            // - find first non-zero element (i.e. first pixel inside mask)
            start = patch.length;
            for (int i = 0; i < patch.length; i++) {
                if (patch[i] > 0.01) {
                    start = i;
                    break;
                }
            }
        }
        norm.release();


        int diff = size - start;
        int topIdx = (int) Math.round(diff * 0.98) + start;
        int midIdx = (int) Math.round(diff * 0.5) + start;
        int bottomIdx = (int) Math.round(diff * 0.02) + start;
        double topVal = patch[topIdx];
        double midVal = patch[midIdx];
        double bottomVal = patch[bottomIdx];

        //Apply normalisation
        if (type == ImageType.LIGHTONDARK) {
            double mult =  0.95 / topVal;
            Core.multiply(workingImage, Scalar.all(mult), workingImage);
            double pwr = (Math.log(contrast) / Math.log(midVal * mult));
            Core.pow(workingImage, pwr, workingImage);
        }
        else if (type == ImageType.DARKONLIGHT) {
            double mult = 0.95 / (1 - bottomVal);
            Mat ones = Mat.ones(workingImage.rows(), workingImage.cols(), workingImage.type());
            Mat inverted = new Mat();
            //Core.subtract(ones, workingImage, inverted);
            Core.multiply(workingImage, Scalar.all(-1), inverted);
            Core.add(inverted, Scalar.all(1), inverted);
            Core.multiply(inverted, Scalar.all(mult), inverted);
            double pwr = (Math.log(contrast) / Math.log((1-midVal) * mult));
            Core.pow(inverted, pwr, inverted);
//            Core.subtract(ones, inverted, workingImage);
            Core.multiply(inverted, Scalar.all(-1), inverted);
            Core.add(inverted, Scalar.all(1), workingImage);
//            input.workingImage = inverted.clone();
            ones.release();
            inverted.release();
        }
        else {
            Core.subtract(workingImage, Scalar.all(midVal - 0.5), workingImage);
            double mult = 0.90 / (topVal - bottomVal) * contrast;
            Core.multiply(workingImage, Scalar.all(mult), workingImage);
            //MatUtilities.printMatDetails(workingImage);

//            float pwr = (float) (Math.log(contrast) / Math.log(midVal * mult));
//            Core.pow(workingImage, pwr, workingImage);
        }
    }

    /**
     * Expands the pixel range to [0, 1]
     *
     * @param input
     */
    public static void normaliseMinMax(ParticleImage input, double min, double max) {
        Mat workingImage = input.workingImage;
        Core.normalize(workingImage, workingImage, min, max, Core.NORM_MINMAX, CvType.CV_32F);
    }
}
