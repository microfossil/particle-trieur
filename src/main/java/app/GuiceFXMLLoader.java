/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.InputStream;
import java.net.URL;
import javafx.fxml.FXMLLoader;

/**
 *
 * @author rossm
 */
public class GuiceFXMLLoader {
    private final Injector injector;
    
    private FXMLLoader loader;
     
    @Inject public GuiceFXMLLoader(Injector injector) {
        this.injector = injector;
    }
     
    // Load some FXML file, using the supplied Controller, and return the
    // instance of the initialized controller...?
    public Object load(String url, Class<?> controller) {
        Object instance = injector.getInstance(controller);
        loader = new FXMLLoader();
        loader.getNamespace().put("controller", instance);
        InputStream in = null;
        try {
            try {
                URL u = new URL(url);
                in = u.openStream();
            } catch (Exception e) {
                in = controller.getResourceAsStream(url);
            }
            return loader.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) try { in.close(); } catch (Exception ee) { }
        }
        return null;
    }
}
