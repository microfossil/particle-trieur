package ordervschaos.particletrieur.app.models.processing;

import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;

public class ImageFormatConverter {

    public static Image mat2Image(Mat frame) {
        //Create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        //Encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        //Build and return an Image created from the image encoded in the buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}
