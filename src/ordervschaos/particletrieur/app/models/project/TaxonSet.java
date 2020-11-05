/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.models.project;

import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.xml.TaxonMapAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "taxons")
public class TaxonSet {
   
    @XmlPath(".")
    @XmlJavaTypeAdapter(TaxonMapAdapter.class)
    public LinkedHashMap<String, Taxon> taxons =  new LinkedHashMap<>();
    
    public void save(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(TaxonSet.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);            
            m.marshal(this, new FileOutputStream(file));
        } 
        catch (Exception e) {
            BasicDialogs.ShowException("Could not save data to file:\n" + file.getPath(),e);
        }
    }
    
    public void load(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(TaxonSet.class);
            Unmarshaller um = context.createUnmarshaller();
            
            TaxonSet temp = (TaxonSet) um.unmarshal(file);        
            taxons.clear();
            taxons.putAll(temp.taxons); 
        } 
        catch (Exception e) { // catches ANY exception
            BasicDialogs.ShowException("Could not load data from file:\n" + file.getPath(),e);
        }
    }
    
    public void load(InputStream stream) {
        try {
            JAXBContext context = JAXBContext.newInstance(TaxonSet.class);
            Unmarshaller um = context.createUnmarshaller();
            TaxonSet temp = (TaxonSet) um.unmarshal(stream);
            taxons.clear();
            taxons.putAll(temp.taxons); 
        } 
        catch (Exception e) { // catches ANY exception
            BasicDialogs.ShowException("Could not load data from InputStream.",e);
        }
    }
}
