/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package other.segmenter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.core.Size;

/**
 *
 * @author chaos
 */
public class Segmenter {
    
    private double lowerSizeThreshold = 150;
    private double upperSizeThreshold = 1000;
    private double lowerBinaryThreshold = 50;
    private double upperBinaryThreshold = 255;
    private double margin = 0.3;
    private int erosion = 1;
    
    public BooleanProperty discardEdgeProperty = new SimpleBooleanProperty(true);
    public BooleanProperty showThresholdedImage = new SimpleBooleanProperty(true);
    
    Mat originalMat;
    Mat thresholdMat;
    Mat displayMat;
    
    public ArrayList<MatOfPoint> contours;
    public ArrayList<Rect> rectangles;

    public double getLowerBinaryThreshold() {
        return lowerBinaryThreshold;
    }

    public void setLowerBinaryThreshold(double lowerBinaryThreshold) {
        this.lowerBinaryThreshold = lowerBinaryThreshold;
    }

    public double getUpperBinaryThreshold() {
        return upperBinaryThreshold;
    }

    public void setUpperBinaryThreshold(double upperBinaryThreshold) {
        this.upperBinaryThreshold = upperBinaryThreshold;
    }

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    } 

    public int getErosion() {
        return erosion;
    }

    public void setErosion(int erosion) {
        this.erosion = erosion;
    }

    public double getLowerSizeThreshold() {
        return lowerSizeThreshold;
    }

    public void setLowerSizeThreshold(double lowerSizeThreshold) {
        this.lowerSizeThreshold = lowerSizeThreshold;
    }

    public double getUpperSizeThreshold() {
        return upperSizeThreshold;
    }

    public void setUpperSizeThreshold(double upperSizeThreshold) {
        this.upperSizeThreshold = upperSizeThreshold;
    }
    
    
    
    
    
    public void Export(RawSlide slide, File file, int index) {
        
        Segment(slide.LoadImage());
        
        int subIndex = 0;
        
        String name = FilenameUtils.removeExtension(slide.getFilename());
        String[] pathParts = name.split(Matcher.quoteReplacement(System.getProperty("file.separator")));
        if (pathParts.length >= 2) {
            name = pathParts[pathParts.length-2];
        }
        else {
            name = "";
        }        
        
//        for (Rect nr : rectangles) {
//            Imgcodecs.imwrite(String.format("%s/seg-%05d-%02d-(%s).tif",
//                        file.getAbsolutePath(),
//                        index,
//                        subIndex,
//                        name), 
//                    originalMat.submat(nr));
//            subIndex++;
//        }
        for (Rect nr : rectangles) {
            File imageFile = new File(String.format("%s/%s/seg-%05d-%02d.tif",
                        file.getAbsolutePath(),
                        name,
                        index,
                        subIndex,
                        name));
            imageFile.getParentFile().mkdirs();
            Imgcodecs.imwrite(imageFile.getAbsolutePath(), originalMat.submat(nr));
            subIndex++;
        }
    }
    
    public void Segment(Mat image) {
        //Read image
        originalMat = image;
        thresholdMat = new Mat();
        
        //Convert to grey        
        Imgproc.cvtColor(originalMat, thresholdMat, Imgproc.COLOR_BGR2GRAY);
        
        //Threshold
        Core.inRange(thresholdMat, new Scalar(lowerBinaryThreshold), new Scalar(upperBinaryThreshold), thresholdMat);
        
        //Erode
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(erosion, erosion));
        Imgproc.erode(thresholdMat, thresholdMat, element);
        
        //Find areas
        contours = new ArrayList<>();
        ArrayList<MatOfPoint> tempContours = new ArrayList<>();
        Imgproc.findContours(thresholdMat.clone(), tempContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        //Discard areas that are too small or near the edge        
        Rect validRect = new Rect(2,2,thresholdMat.width()-4,thresholdMat.height()-4);
        for (MatOfPoint m : tempContours) {
            Rect boundingRect = Imgproc.boundingRect(m); 
            double area = Imgproc.contourArea(m);
            //double size = boundingRect.width > boundingRect.height ? boundingRect.width : boundingRect.height;
            if (area > Math.pow(lowerSizeThreshold,2) && area <= Math.pow(upperSizeThreshold,2)) {
                if (discardEdgeProperty.get() == true) {                    
                    if (!boundingRect.tl().inside(validRect) || !boundingRect.br().inside(validRect)) {
                        continue;
                    }
                }
                if (area < 0.1 * boundingRect.area()) {
                    continue;
                }
                contours.add(m);
            }
        }
        
        //Find rectangles
        rectangles = new ArrayList<>();
        for (MatOfPoint m : contours) {
            Rect boundingRect = Imgproc.boundingRect(m);
            double maxD = Math.max(boundingRect.width, boundingRect.height);
            maxD *= (margin + 1);
            double incY = (maxD - boundingRect.height)/2;
            double incX = (maxD - boundingRect.width)/2;
            double ntlx = Math.max(boundingRect.tl().x - incX, 1);
            double ntly = Math.max(boundingRect.tl().y - incY, 1);
            double nrbx = Math.min(boundingRect.br().x + incX, thresholdMat.width());
            double nrby = Math.min(boundingRect.br().y + incY, thresholdMat.height());
            rectangles.add(new Rect((int)ntlx, (int)ntly, (int)(nrbx-ntlx), (int)(nrby-ntly)));
        }    
    }
    
    public Image Process(RawSlide slide) {  
        
        Segment(slide.LoadImage());
        
        if (showThresholdedImage.get()) {
              return mat2Image(thresholdMat);
//            Imgproc.distanceTransform(thresholdMat, thresholdMat, Imgproc.CV_DIST_L2, Imgproc.CV_DIST_MASK_PRECISE);
//            Mat labels = new Mat();
//            Imgproc.threshold(thresholdMat,labels,20,255,0);
//            labels.convertTo(labels, CvType.CV_8UC1);
//            Imgproc.connectedComponents(labels, labels);
//            Core.multiply(labels, new Scalar(10), labels, 1);
//            labels.convertTo(labels, CvType.CV_32SC1);
//            Imgproc.cvtColor(thresholdMat, thresholdMat, Imgproc.COLOR_GRAY2RGB);
//            thresholdMat.convertTo(thresholdMat, CvType.CV_8UC3);
//            Imgproc.watershed(thresholdMat, labels);
//            Core.multiply(labels, new Scalar(10), labels, 1);
//            return mat2Image(labels);
        }
        else {
            displayMat = originalMat.clone();            
            
            //Draw contours
            int size = Math.min(displayMat.width(), displayMat.height()) / 400 + 1;
            Imgproc.drawContours(displayMat, contours, -1, new Scalar(255,128,0), size);

            for (Rect nr : rectangles) {
                Imgproc.rectangle(displayMat, nr.tl(), nr.br(), new Scalar(0,128,0), size);
            }
            
            return mat2Image(displayMat);
        }
    }
    
    public static Image mat2Image(Mat frame)
    {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}
