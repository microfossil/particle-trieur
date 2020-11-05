/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.models.processing;

import ordervschaos.particletrieur.app.xml.MorphologyDoubleMapAdapter;
import org.apache.commons.lang3.SerializationUtils;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;

/**
 *
 * @author chaos
 */
@XmlRootElement(name = "morphology")
public class Morphology implements Serializable {

    public int height;
    public int width;

    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double area = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double perimeter = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double convexArea = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double convexPerimeter = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double circleArea = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double circleRadius = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double majorAxisLength = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double minorAxisLength = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double meanDiameter = 0;

    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double eccentricity = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double angle = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double solidity = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double circularity = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double roundness = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double convexPerimeterToPerimeterRatio = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double convexAreaToAreaRatio = 0;

    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double mean = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double stddev = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double stddevInvariant = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double skew = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double kurtosis = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double moment5 = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double moment6 = 0;


    public Morphology convertToMM(double pixelsPerMM) {

        double pixels2PerMM2 = pixelsPerMM * pixelsPerMM;

        Morphology converted = new Morphology();

        if (pixelsPerMM == 0) return new Morphology();

        converted.meanDiameter = this.meanDiameter / pixelsPerMM;
        converted.area = this.area / pixels2PerMM2;
        converted.perimeter = this.perimeter / pixelsPerMM;
        converted.convexArea = this.convexArea / pixels2PerMM2;
        converted.convexPerimeter = this.convexPerimeter / pixelsPerMM;
        converted.circleArea = this.circleArea / pixels2PerMM2;
        converted.circleRadius = this.circleRadius / pixelsPerMM;
        converted.majorAxisLength = this.majorAxisLength / pixelsPerMM;
        converted.minorAxisLength = this.minorAxisLength / pixelsPerMM;

        return converted;
    }

    public String toStringCSV(double pixelsPerMM) {
        Morphology converted = convertToMM(pixelsPerMM);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%f,", (float) area));
        sb.append(String.format("%f,", (float) converted.area));
        sb.append(String.format("%f,", (float) perimeter));
        sb.append(String.format("%f,", (float) converted.perimeter));
        sb.append(String.format("%f,", (float) convexArea));
        sb.append(String.format("%f,", (float) converted.convexArea));
        sb.append(String.format("%f,", (float) convexPerimeter));
        sb.append(String.format("%f,", (float) converted.convexPerimeter));

        sb.append(String.format("%f,", (float) meanDiameter));
        sb.append(String.format("%f,", (float) converted.meanDiameter));
        sb.append(String.format("%f,", (float) majorAxisLength));
        sb.append(String.format("%f,", (float) converted.majorAxisLength));
        sb.append(String.format("%f,", (float) minorAxisLength));
        sb.append(String.format("%f,", (float) converted.minorAxisLength));

        sb.append(String.format("%f,", (float) circleArea));
        sb.append(String.format("%f,", (float) converted.circleArea));
        sb.append(String.format("%f,", (float) circleRadius));
        sb.append(String.format("%f,", (float) converted.circleRadius));

        sb.append(String.format("%f,", (float) eccentricity));
        sb.append(String.format("%f,", (float) solidity));
        sb.append(String.format("%f,", (float) roundness));
        sb.append(String.format("%f,", (float) circularity));

        sb.append(String.format("%f,", (float) mean));
        sb.append(String.format("%f,", (float) stddev));
        sb.append(String.format("%f,", (float) stddevInvariant));
        sb.append(String.format("%f,", (float) skew));
        sb.append(String.format("%f,", (float) kurtosis));
        sb.append(String.format("%f,", (float) moment5));
        sb.append(String.format("%f", (float) moment6));

        return sb.toString();
    }

    public static String getHeaderStringForCSV() {
        return "area,areaMM2," +
                "perimeter,perimeterMM," +
                "convexArea,convexAreaMM2," +
                "convexPerimeter,convexPerimeterMM," +
                "meanDiameter,meanDiameterMM," +
                "majorAxis,majorAxisMM," +
                "minorAxis,minorAxisMM," +
                "minEnclosingCircleArea,minEnclosingCircleAreaMM2," +
                "minEnclosingCircleRadius,minEnclosingCircleRadiusMM," +
                "eccentricity,solidity,roundness,circularity," +
                "mean,stddev,stddevInvariant,"
                + "skewness,kurtosis,5thmoment,6thmoment";
    }
}
