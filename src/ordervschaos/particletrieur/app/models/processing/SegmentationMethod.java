/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.models.processing;

/**
 *
 * @author rossm
 */
public enum SegmentationMethod {
    INTENSITY("Intensity"),
    OTSU("Adaptive (Otsu)"),
    CNN("Segmentation CNN");
//    EXPERIMENTAL("Experimental");

    private final String value;

    SegmentationMethod(String s) {
        value = s;
    }

    @Override
    public String toString() {
        return value;
    }
}
