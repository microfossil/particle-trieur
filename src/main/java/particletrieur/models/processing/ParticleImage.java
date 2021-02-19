package particletrieur.models.processing;

import particletrieur.models.processing.processors.BorderRemovalProcessor;
import particletrieur.models.processing.processors.ColourProcessor;
import particletrieur.models.processing.processors.PositionAdjustmentProcessor;
import particletrieur.models.processing.processors.Preprocessor;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class ParticleImage {

    public Mat colourImage;
    public Mat greyscaleImage;
    public Mat workingImage;
    public Mat debugImage;
    public Mask mask;
    public ImageType type;

    public Morphology morphology;

    //TODO check mat that is put in is released...
    public static ParticleImage create(Mat mat, ImageType type, boolean toGreyscale) {
        ParticleImage image = new ParticleImage();
        image.type = type;
        if (mat.channels() == 1) {
            image.greyscaleImage = mat.clone();
            image.colourImage = new Mat();
            image.workingImage = new Mat();
            Imgproc.cvtColor(image.greyscaleImage, image.colourImage, Imgproc.COLOR_GRAY2BGR);
            if (!toGreyscale) image.colourImage.convertTo(image.workingImage, CvType.CV_32F);
            else image.greyscaleImage.convertTo(image.workingImage, CvType.CV_32F);
        }
        else {
            image.colourImage = mat.clone();
            image.greyscaleImage = new Mat();
            image.workingImage = new Mat();
            Imgproc.cvtColor(image.colourImage, image.greyscaleImage, Imgproc.COLOR_BGR2GRAY);
            if (!toGreyscale) image.colourImage.convertTo(image.workingImage, CvType.CV_32F);
            else image.greyscaleImage.convertTo(image.workingImage, CvType.CV_32F);
        }
        return image;
    }

    public ParticleImage convertChannels(int channels) {
        if (channels == workingImage.channels()) return this;
        if (channels == 3) Imgproc.cvtColor(workingImage, workingImage, Imgproc.COLOR_BGR2GRAY);
        else Imgproc.cvtColor(workingImage, workingImage, Imgproc.COLOR_BGR2GRAY);
        return this;
    }

    public void release() {
        if (colourImage != null) colourImage.release();
        if (greyscaleImage != null) greyscaleImage.release();
        if (workingImage != null) workingImage.release();
        if (debugImage != null) debugImage.release();
        if (mask != null) mask.release();
    }

    //Mask
    public ParticleImage setMask(Mask mask) {
        if (this.mask != null) this.mask.release();
        this.mask = mask;
        return this;
    }

    //Border removal
    public ParticleImage removeWhiteBorder() {
        BorderRemovalProcessor.removeWhiteBorder(this);
        return this;
    }
    public ParticleImage removeBlackBorder() {
        BorderRemovalProcessor.removeBlackBorder(this);
        return this;
    }

    public ParticleImage resizeByMaximumDimension(int maxSide) {
        BorderRemovalProcessor.resizeByMaximumDimension(this, maxSide);
        return this;
    }


    public ParticleImage makeSquare() {
        BorderRemovalProcessor.makeSquareUsingMedianOfBorder(this);
        return this;
    }

    //Preprocessor
    public ParticleImage rescale(double divisor) {
        Preprocessor.rescale(this, divisor);
        return this;
    }

    //Colour adjustments
    public ParticleImage normalise(double contrast, boolean useMask) {
        ColourProcessor.normalise(this, contrast, useMask);
        return this;
    }

    //Colour adjustments
    public ParticleImage normaliseMinMax(double min, double max) {
        ColourProcessor.normaliseMinMax(this, min, max);
        return this;
    }

    //Segmentation adjustments
    public ParticleImage adjustFromMask(boolean rotate) {
        PositionAdjustmentProcessor.adjust(this, rotate);
        return this;
    }
    public ParticleImage removeBackground(int margin) {
        PositionAdjustmentProcessor.removeBackground(this, margin);
        return this;
    }

    //Resize
    public ParticleImage resize(int width, int height) {
        Imgproc.resize(workingImage, workingImage, new Size(width, height));
        return this;
    }

    //Output
    public Mat forSaving(double multiplier) {
        Mat mat = new Mat();
        Core.multiply(workingImage, Scalar.all(multiplier), mat);
        mat.convertTo(mat, CvType.CV_8U);
        return mat;
    }
}
