/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.models.project;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@XmlRootElement(name = "taxon")
public class Taxon {
        
    private String code = "";
    private String name = "";
    private String description = "";
    private String group = "";
    private Boolean isClass = true;
    
    public Taxon() {

    }
    
    public Taxon(String code, String name, String description, String group, boolean isClass) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.group = group;
        this.isClass = isClass;
    }
    
    public String getName() { return name; }
    protected void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    protected void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    protected void setDescription(String description) { this.description = description; }
    
    public String getGroup() { return group; }
    protected void setGroup(String group) { this.group = group; }

    public boolean getIsClass() { return isClass; }
    protected void setIsClass(boolean isClass) { this.isClass = isClass; }
}
