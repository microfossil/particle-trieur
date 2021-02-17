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
public enum ImageType {
    LIGHTONDARK("Light particle on dark background"),
    DARKONLIGHT("Dark particle on light background"),
    GREY("Light and dark on grey background");

    private final String value;

    ImageType(String s) {
        value = s;
    }

    @Override
    public String toString() {
        return value;
    }
}
