/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.models;

import javafx.beans.property.ObjectProperty;
import particletrieur.App;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.helpers.ExceptionMonitor;
import particletrieur.models.network.classification.NetworkBase;
import particletrieur.models.network.classification.OnnxNetwork;
//import particletrieur.models.network.classification.TensorflowNetwork;
import particletrieur.models.project.Project;
import particletrieur.models.tools.ClassificationServer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import particletrieur.models.tools.FolderWatch;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class Supervisor {

    //Model objects
    public final Project project = new Project();
    public NetworkBase network;
    public final ClassificationServer classificationServer;
    public final FolderWatch folderWatch;
    public final ProjectRepository projectRepository = new ProjectRepository(project);

    //Current user
    private final StringProperty username = new SimpleStringProperty("");
    public String getUsername() {
        return username.get();
    }
    public void setUsername(String value) {
        username.set(value);
    }
    public StringProperty usernameProperty() { return username; }

    public ExceptionMonitor exceptionMonitor = new ExceptionMonitor();

    //TODO all of this in a module ?? view model??
    // Project as a module
    // This code in the main view model
    public Supervisor() {
        folderWatch = new FolderWatch(this);
        classificationServer = new ClassificationServer(this);

        //When project changes, update preferences
        project.fileProperty().addListener((objv,oldv,newv) -> {
            if (newv != null) {
                App.getPrefs().setProjectPath(newv.getParent());
                App.getPrefs().setExportPath(newv.getParent());
                App.getPrefs().save();
            }
        });

        //Connect project and network
        project.networkDefinitionProperty().addListener((objv,oldv,newv) -> {
//            if (newv.protobuf.endsWith("pb")) network = new TensorflowNetwork();
            if (newv.protobuf.endsWith("onnx")) network = new OnnxNetwork();
            network.setNetworkInfo(newv);
            if (!network.setup()) {
                BasicDialogs.ShowError("Network error",
                        "Cannot start project network at " + newv.protobuf + "\nThe model does not exist, or the network XML file was an old version.");
            }
            if (!network.isEnabled() && newv != null) project.setNetworkDefinition(null);
        });

        //Load preferences
        String username = App.getPrefs().getUsername();
        if (username.equals("")) username = System.getProperty("user.name");
        setUsername(username);

        //Update username
        usernameProperty().addListener(((observable, oldValue, newValue) -> {
            App.getPrefs().setUsername(newValue);
        }));

        //Exception handling
        Thread.setDefaultUncaughtExceptionHandler(exceptionMonitor);
    }
}
