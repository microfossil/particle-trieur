/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.xml;

import particletrieur.models.project.Tag;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class TagMapAdapter extends XmlAdapter<TagMapAdapter.TagAdaptedMap, LinkedHashMap<String,Tag>> {
    
    public TagMapAdapter() {
        
    }

    @Override
    public LinkedHashMap<String, Tag> unmarshal(TagAdaptedMap v) throws Exception {
        LinkedHashMap<String,Tag> map = new LinkedHashMap<>();
        for (Tag taxon : v.list) {
            map.put(taxon.getCode(), taxon);
        }
        return map;
    }

    @Override
    public TagAdaptedMap marshal(LinkedHashMap<String, Tag> v) throws Exception {
        TagAdaptedMap map = new TagAdaptedMap();
        map.list = new ArrayList<>(v.values());
        return map;
    }
    
    public static class TagAdaptedMap {
        @XmlElement(name = "tag")
        List<Tag> list;
    }
}
