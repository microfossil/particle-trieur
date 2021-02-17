/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.models.network.classification;

import main.java.app.models.project.Project;
import main.java.app.xml.ClassificationMapAdapter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@XmlRootElement(name = "classifications")
@XmlAccessorType(XmlAccessType.NONE)
public class ClassificationSet {
    
    @XmlElement
    @XmlPath(".")
    @XmlJavaTypeAdapter(ClassificationMapAdapter.class)
    public LinkedHashMap<String,Classification> classifications =  new LinkedHashMap<>(); 
    
    @XmlTransient
    public final double MINIMUM_VALUE = 0.01;
    public double maximumScore = 0;
    
    private final StringProperty classifierId = new SimpleStringProperty();
    @XmlAttribute(name="id")
    public String getClassifierId() {
        return classifierId.get();
    }
    public void setClassifierId(String value) {
        classifierId.set(value);
    }
    public StringProperty classifierIdProperty() {
        return classifierId;
    }       
    
    public ClassificationSet() {
        
    }
    
    public void add(String code, double score, String id) {
        if (score < MINIMUM_VALUE){
            if (classifications.containsKey(code)) {
                classifications.remove(code);
            }
        }        
        else {
            Classification cl = new Classification();
            cl.setCode(code);
            cl.setValue(score);
            classifications.put(code, cl);
        }
        if (score > maximumScore) {
            maximumScore = score;
        }
        setClassifierId(id);
    }
    
    public void clearAndAdd(String code, double score, String id) {
        classifications.clear();
        maximumScore = 0;
        add(code, score, id);
    }
    
    public void modifyKey(String key, String newKey) {
        Classification cls = classifications.remove(key);
        cls.setCode(newKey);
        classifications.put(newKey, cls);
    }

    public Classification getBest() {
        if (classifications.isEmpty()) return null;

        Classification classification = Collections.max(classifications.values(), new Comparator<Classification>() {
            @Override
            public int compare(Classification o1, Classification o2) {
                if (o1.getValue() > o2.getValue()) return 1;
                else if (o1.getValue() < o2.getValue()) return -1;
                else return 0;
            }
        });
        return classification;
    }
    
    public String getBestCode() {
        if (classifications.isEmpty()) return Project.UNLABELED_CODE;
        return getBest().getCode();
    }
    public ClassificationSet clone() {
        ClassificationSet clone = new ClassificationSet();
        clone.classifications = new LinkedHashMap<>(classifications);
        clone.setClassifierId(getClassifierId());
        clone.maximumScore = maximumScore;
        return clone;
    }
}
