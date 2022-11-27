/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.models.processing;

import particletrieur.xml.MorphologyDoubleMapAdapter;

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

    //More morphologies
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double aspectRatio = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double equivalentDiameter = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double perimeterToAreaRatio = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double areaToBoundingRectangleArea = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double equivalentSphericalDiameter = 0;

    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double elongation = 0;

    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double Husmoment1 = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double Husmoment2 = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double Husmoment3 = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double Husmoment4 = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double Husmoment5 = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double Husmoment6 = 0;
    @XmlJavaTypeAdapter(MorphologyDoubleMapAdapter.class)
    public double Husmoment7 = 0;





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

        sb.append(String.format("%f", (float) aspectRatio));
        sb.append(String.format("%f", (float) equivalentDiameter));
        sb.append(String.format("%f", (float) perimeterToAreaRatio));
        sb.append(String.format("%f", (float) areaToBoundingRectangleArea));
        sb.append(String.format("%f", (float) equivalentSphericalDiameter));
        sb.append(String.format("%f", (float) elongation));
        sb.append(String.format("%f", (float) Husmoment1));
        sb.append(String.format("%f", (float) Husmoment2));
        sb.append(String.format("%f", (float) Husmoment3));
        sb.append(String.format("%f", (float) Husmoment4));
        sb.append(String.format("%f", (float) Husmoment5));
        sb.append(String.format("%f", (float) Husmoment6));
        sb.append(String.format("%f", (float) Husmoment7));

        return sb.toString();
    }

    public static String getHeaderStringForCSV() {
        return "m_area,m_areaMM2," +
                "m_perimeter,m_perimeterMM," +
                "m_convexArea,m_convexAreaMM2," +
                "m_convexPerimeter,m_convexPerimeterMM," +
                "m_meanDiameter,m_meanDiameterMM," +
                "m_majorAxis,m_majorAxisMM," +
                "m_minorAxis,m_minorAxisMM," +
                "m_minEnclosingCircleArea,m_minEnclosingCircleAreaMM2," +
                "m_minEnclosingCircleRadius,m_minEnclosingCircleRadiusMM," +
                "m_eccentricity,m_solidity,m_roundness,m_circularity," +
                "m_mean,m_stddev,m_stddevInvariant,"
                + "m_skewness,m_kurtosis,m_5thmoment,m_6thmoment";
    }
}
