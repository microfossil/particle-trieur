/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.xml;

import main.java.app.models.network.classification.Classification;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ClassificationMapAdapter extends XmlAdapter<ClassificationMapAdapter.ClassificationAdaptedMap, LinkedHashMap<String,Classification>> {
    
    public ClassificationMapAdapter() {
        
    }

    @Override
    public LinkedHashMap<String, Classification> unmarshal(ClassificationAdaptedMap v) throws Exception {
        LinkedHashMap<String, Classification> map = new LinkedHashMap<>();
        for (Classification cl : v.list) {
            map.put(cl.getCode(), cl);
        }
        return map;
    }

    @Override
    public ClassificationAdaptedMap marshal(LinkedHashMap<String, Classification> v) throws Exception {
        ClassificationAdaptedMap map = new ClassificationAdaptedMap();
        map.list = new ArrayList(v.values());
        return map;
    }

    public static class ClassificationAdaptedMap {
        @XmlElement(name = "classification")
        List<Classification> list;
    }     
}
