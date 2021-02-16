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
@XmlRootElement(name = "tag")
public class Tag {
    
    private String code = "";
    private String name = "";
    private String description = "";
    
    public Tag() {
        
    }
    
    public Tag(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() { return code; }
    protected void setCode(String code) { this.code = code;  }

    public String getName() { return name; }
    protected void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    protected void setDescription(String description) { this.description = description; }
}
