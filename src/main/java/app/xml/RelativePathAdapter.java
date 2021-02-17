/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.xml;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class RelativePathAdapter extends XmlAdapter<String, String> {
    
    public RelativePathAdapter() {
        
    }
            
    private String rootPath;
    
    public RelativePathAdapter(String rootPath) {
        this.rootPath = rootPath;
    }
            
    @Override
    public String unmarshal(String v) throws Exception {
        File file = new File(v);
        if (file.isAbsolute()) return v;
        Path root = Paths.get(rootPath);
        Path path = Paths.get(v);
        return root.resolve(path).normalize().toString();
    }

    @Override
    public String marshal(String v) throws Exception {
        //Network share (windows) - return absolute path
        if (v.startsWith("\\")) return v;
        //Different volume (windows) - return absolute path
        if (!v.startsWith(rootPath.substring(0,1))) return v;
        //Else relative path
        Path root = Paths.get(rootPath);
        Path file = Paths.get(v);
        return root.relativize(file).toString().replace('\\', '/');
    }
}
