/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.models.network.classification;

import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.helpers.XMLClonable;
import ordervschaos.particletrieur.app.xml.RelativePathAdapter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@XmlRootElement(name = "network")
@XmlAccessorType(XmlAccessType.NONE)
public class NetworkInfo extends XMLClonable<NetworkInfo> {

    //Details
    @XmlElement
    public String name = "";
    @XmlElement
    public String description = "";
    @XmlElement
    public String type = "";
    @XmlElement
    public String date;
    @XmlElement
    public HashMap<String, String> params = new HashMap<>();
    @XmlElement
    @XmlJavaTypeAdapter(RelativePathAdapter.class)
    public String protobuf = "frozen_model.pb";
    @XmlElement
    public String source_data = "";

    //Tensors
    @XmlElementWrapper(name = "inputs")
    @XmlElements({ @XmlElement(name = "input", type = TensorInfo.class) })
    public ArrayList<TensorInfo> inputs = new ArrayList<>();
    @XmlElementWrapper(name = "outputs")
    @XmlElements({ @XmlElement(name = "output", type = TensorInfo.class) })
    public ArrayList<TensorInfo> outputs = new ArrayList<>();
    @XmlElementWrapper(name = "labels")
    @XmlElements({ @XmlElement(name = "label", type = NetworkLabel.class) })
    public ArrayList<NetworkLabel> labels = new ArrayList<>();

    @XmlElement
    public double accuracy = 0;
    @XmlElement
    public double precision = 0;
    @XmlElement
    public double recall = 0;
    @XmlElement
    public double f1score = 0;


    //Is Resource
    public boolean isResource = false;


    public NetworkInfo() {

    }
    
    private void save(File xmlFile) {
        try {
            JAXBContext context = JAXBContext.newInstance(NetworkInfo.class);
            Marshaller m = context.createMarshaller();
            m.setAdapter(new RelativePathAdapter(xmlFile.getParent()));
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);            
            m.marshal(this, new FileOutputStream(xmlFile));
        } 
        catch (Exception e) { // catches ANY exception
            BasicDialogs.ShowException("Could not save data to file:\n" + xmlFile.getPath(),e);
        }
    }
    
    public static NetworkInfo load(File xmlFile) {
        NetworkInfo definition = null;
        try {
            JAXBContext context = JAXBContext.newInstance(NetworkInfo.class);
            Unmarshaller um = context.createUnmarshaller();
            um.setAdapter(new RelativePathAdapter(xmlFile.getParent()));
            definition = (NetworkInfo) um.unmarshal(xmlFile);
            
            if (definition.name == null || definition.name.equalsIgnoreCase("")) {
                LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(xmlFile.lastModified()), ZoneId.systemDefault());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
                definition.name = String.format("network_%s", date.format(formatter));
            }
        } 
        catch (Exception e) { // catches ANY exception
            BasicDialogs.ShowException("Could not load data from file:\n" + xmlFile.getPath(),e);
        }
        return definition;
    }
//
//    public static NetworkDefinition load(InputStream stream) {
//        NetworkDefinition definition = null;
//        try {
//            JAXBContext context = JAXBContext.newInstance(NetworkDefinition.class);
//            Unmarshaller um = context.createUnmarshaller();
//            um.setAdapter(new RelativePathAdapter(""));
//            definition = (NetworkDefinition) um.unmarshal(stream);
//            Path path = Paths.get(definition.filename);
//        }
//        catch (Exception e) { // catches ANY exception
//            BasicDialogs.ShowException("Could not load data from stream",e);
//        }
//        return definition;
//    }
}
