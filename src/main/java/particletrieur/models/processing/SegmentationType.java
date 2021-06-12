/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.models.processing;

/**
 *
 * @author rossm
 */
public enum SegmentationType {
    INTENSITY("Intensity"),
    OTSU("Adaptive (Otsu)"),
    CNN("Foraminifera CNN"),
    PLANKTON("Plankton CNN");

    private final String value;

    SegmentationType(String s) {
        value = s;
    }

    @Override
    public String toString() {
        return value;
    }
}
