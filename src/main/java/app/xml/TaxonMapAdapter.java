/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.xml;

import main.java.app.models.project.Taxon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class TaxonMapAdapter extends XmlAdapter<TaxonMapAdapter.TaxonAdaptedMap, LinkedHashMap<String, Taxon>> {
    
    public TaxonMapAdapter() {
        
    }

    @Override
    public LinkedHashMap<String, Taxon> unmarshal(TaxonAdaptedMap v) throws Exception {
        LinkedHashMap<String, Taxon> map = new LinkedHashMap<>();
        for (Taxon taxon : v.list) {
            map.put(taxon.getCode(), taxon);
        }
        return map;
    }

    @Override
    public TaxonAdaptedMap marshal(LinkedHashMap<String, Taxon> v) throws Exception {
        TaxonAdaptedMap map = new TaxonAdaptedMap();
        map.list = new ArrayList<>(v.values());
        return map;
    }
    
    public static class TaxonAdaptedMap {
        @XmlElement(name = "taxon")
        List<Taxon> list;
    }
}
