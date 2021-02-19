/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.models.processing;

/**
 *
 * @author rossm
 */
public enum SegmentationType {
    INTENSITY("Intensity"),
    OTSU("Adaptive (Otsu)"),
    CNN("Segmentation CNN");

    private final String value;

    SegmentationType(String s) {
        value = s;
    }

    @Override
    public String toString() {
        return value;
    }
}
