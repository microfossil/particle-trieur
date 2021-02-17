/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author rossm
 */
public class MorphologyDoubleMapAdapter extends XmlAdapter<String, Double> {

    @Override
    public Double unmarshal(String v) throws Exception {
        return Double.parseDouble(v);
    }

    @Override
    public String marshal(Double v) throws Exception {
        return String.format("%.3f", v);
    }
}
