/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.xml;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ParametersMapAdapter extends XmlAdapter<ParametersMapAdapter.ParametersAdaptedMap, LinkedHashMap<String, String>> {

    public ParametersMapAdapter() {

    }

    @Override
    public LinkedHashMap<String, String> unmarshal(ParametersAdaptedMap v) throws Exception {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (Data d : v.list) {
            map.put(d.key, d.value);
        }
        return map;
    }

    @Override
    public ParametersAdaptedMap marshal(LinkedHashMap<String, String> v) throws Exception {
        ParametersAdaptedMap map = new ParametersAdaptedMap();
        ArrayList<Data> data = new ArrayList<>();
        for (Map.Entry<String, String> entry : v.entrySet()) {
            data.add(new Data(entry.getKey(), entry.getValue()));
        }
        map.list = data;
        return map;
    }

    public static class ParametersAdaptedMap {
        @XmlElement(name = "item")
        List<Data> list;
    }

    public static class Data {
        @XmlAttribute
        public String key;
        @XmlValue
        public String value;

        public Data() {

        }

        public Data(String key, String value) {
            this.key = key;
            this.value = value;
        }

    }
}