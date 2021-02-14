/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app;

import ordervschaos.particletrieur.app.viewcontrollers.MainController;
import ordervschaos.particletrieur.app.viewcontrollers.StartupViewController;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import ordervschaos.particletrieur.app.viewmodels.network.NetworkViewModel;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class App extends Application {

    public static String VERSION = "2.4.0";
    
    public static Image iconImage = new Image(App.class.getResourceAsStream("resources/icon.png" ),44, 44,true,true);
    
    private static App instance;
    public static App getInstance() {
        return instance;
    }

    private static AppPreferences prefs = new AppPreferences();
    public static AppPreferences getPrefs() { return prefs; }

    private Stage stage;
    public final Injector injector = Guice.createInjector(new MainModule());

    ExecutorService executorService = Executors.newFixedThreadPool(10);
    public static ExecutorService getExecutorService() { return instance.executorService; }

    private Parent root;

    public static Window getWindow() { return MainController.instance.getWindow(); }
    public static Pane getRootPane() { return MainController.instance.getRootPane(); }
    
    @Override
    public void start(Stage stage) throws Exception {
        instance = this;
        this.stage = stage;

        //ResourceBundle bundle = ResourceBundle.getBundle("ordervschaos.particle.bundles.Lang", new Locale("fr"));
        ResourceBundle bundle = ResourceBundle.getBundle("ordervschaos.particletrieur.bundles.Lang");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("views/StartupView.fxml"),bundle);
        loader.setControllerFactory(instantiatedClass -> injector.getInstance(instantiatedClass));
        root = loader.load();
        stage.setTitle("Particle Trieur " + App.VERSION);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("resources/icon.png" )));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("resources/styles/style.css").toExternalForm());

        StartupViewController controller = loader.getController();
        controller.stage = stage;
        //controller.setupAccelerators();
        
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        System.out.println("STOP AND SHUTDOWN");
        executorService.shutdown();
        injector.getInstance(NetworkViewModel.class).Stop();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        launch(args);        
    }
    
    public void updateTitle(String title) {
        stage.setTitle(title);
    }
}
