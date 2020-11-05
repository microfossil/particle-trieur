/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.models;

import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.helpers.ExceptionMonitor;
import ordervschaos.particletrieur.app.viewmanagers.ParticleInformationManager;
import ordervschaos.particletrieur.app.models.network.classification.NetworkEx;
import ordervschaos.particletrieur.app.models.network.features.ResNet50FeatureVectorService;
import ordervschaos.particletrieur.app.models.network.segmentation.FCNNSegmenter;
import ordervschaos.particletrieur.app.models.project.Project;
import ordervschaos.particletrieur.app.models.server.ClassificationServer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class Supervisor {

    //Model objects
    public final Project project = new Project();
    public final NetworkEx network = new NetworkEx();
    public final ClassificationServer classificationServer;
    public final FolderWatch folderWatch;

    public final FCNNSegmenter FCNNSegmenter = new FCNNSegmenter();
    public final ProjectRepository projectRepository = new ProjectRepository(project);
    public final ResNet50FeatureVectorService ResNet50FeatureVectorService = new ResNet50FeatureVectorService();
    public final ParticleInformationManager particleInformationManager;

    //Current user
    private final StringProperty username = new SimpleStringProperty();
    public String getUsername() { return username.get(); }
    public void setUsername(String value) { username.set(value); }
    public StringProperty usernameProperty() { return username; }

    public ExceptionMonitor exceptionMonitor = new ExceptionMonitor();
        
    public Supervisor() {
        folderWatch = new FolderWatch(this);
        classificationServer = new ClassificationServer(this);
        particleInformationManager = new ParticleInformationManager(this);

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
            network.setNetworkInfo(newv);
            if (!network.setup()) {
                BasicDialogs.ShowError("Network error",
                        "Cannot start project network at " + newv.protobuf + "\nThe tensorflow graph file does not exist, or the network XML file was an old version.");
            }
            if (!network.isEnabled() && newv != null) project.setNetworkDefinition(null);
        });
        //Load preferences
        setUsername(App.getPrefs().getUsername());
        //Exception handling
        Thread.setDefaultUncaughtExceptionHandler(exceptionMonitor);
    }
}
