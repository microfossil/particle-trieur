package particletrieur.viewcontrollers;

import javafx.animation.RotateTransition;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import particletrieur.AbstractController;
import particletrieur.App;
import particletrieur.controls.BasicDialogs;
import particletrieur.controls.SymbolLabel;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StartupViewController extends AbstractController implements Initializable {

    @FXML
    VBox vboxRecent;
    @FXML
    Label labelRecent;
    @FXML
    Label labelUpdate;
    @FXML
    SymbolLabel symbolLabelChecking;
    @FXML
    Hyperlink hyperlinkUpdate;
    @FXML
    Button buttonOpenRecent;
    @FXML
    VBox vboxWelcome;
    @FXML
    HBox hboxLoading;
    @FXML
    SymbolLabel symbolLoading;

    RotateTransition rt;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        rt = new RotateTransition(Duration.millis(3000), symbolLabelChecking);
//        rt.setByAngle(360);
//        rt.setInterpolator(Interpolator.LINEAR);
//        rt.setCycleCount(Animation.INDEFINITE);
//        symbolLabelChecking.setCache(true);
//        symbolLabelChecking.setCacheHint(CacheHint.SPEED);
//        rt.play();

        List<String> recents = App.getPrefs().getRecentProjects();
        for (String recent : recents) {
            File file = new File(recent);
            if (file.exists()) {
                Hyperlink link = new Hyperlink(recent);
                link.setWrapText(true);
                link.setOnAction( p -> {
                    start(3, recent);
                });
                vboxRecent.getChildren().add(link);
            }
        }
        if (vboxRecent.getChildren().size() == 0) {
            vboxRecent.getChildren().add(new Label("Recent project list is empty"));
        }


//        File file = new File(App.getPrefs().getRecentProject());
//        if (!file.exists()) {
//            buttonOpenRecent.setDisable(true);
//        }
//        else {
//            labelRecent.setText(file.getAbsolutePath());
//        }

//        Service<String> service = UpdateCheckService.getVersion();
//
//        service.setOnSucceeded(event -> {
//            String version = service.getValue();
//            String currentVersion = App.class.getPackage().getImplementationVersion() != null ?
//                    App.class.getPackage().getImplementationVersion().split("-")[0] : "dev";
//            System.out.println(version);
//            System.out.println(currentVersion);
//            if (version == null) {
//                rt.stop();
//                symbolLabelChecking.setCache(false);
//                symbolLabelChecking.setRotate(0);
//                symbolLabelChecking.setSymbol("featherxoctagon");
//                symbolLabelChecking.setSymbolColor("darkred");
//                labelUpdate.setText("Could not check for updates");
//                labelUpdate.setStyle("-fx-text-fill: darkred;");
//            }
//            else if (!version.equals(currentVersion)) {
//                rt.stop();
//                symbolLabelChecking.setCache(false);
//                symbolLabelChecking.setRotate(0);
//                labelUpdate.setVisible(false);
//                hyperlinkUpdate.setText(String.format("Update available (%s)", version));
//                hyperlinkUpdate.setVisible(true);
//            }
//            else {
//                rt.stop();
//                symbolLabelChecking.setCache(false);
//                symbolLabelChecking.setRotate(0);
//                symbolLabelChecking.setSymbol("feathercheckcircle");
//                symbolLabelChecking.setSymbolColor("darkgreen");
//                labelUpdate.setText("Up to date");
//                labelUpdate.setStyle("-fx-text-fill: darkgreen;");
//            }
//        });
//        service.setOnFailed(event -> {
//            rt.stop();
//            symbolLabelChecking.setCache(false);
//            symbolLabelChecking.setRotate(0);
//            symbolLabelChecking.setSymbol("featherxoctagon");
//            symbolLabelChecking.setSymbolColor("darkred");
//            labelUpdate.setText("Could not check for updates");
//        });
//
//        service.start();
    }

    @FXML
    private void handleNew(ActionEvent event) {
        start(0, null);
    }

    @FXML
    private void handleNewFromTemplate(ActionEvent event) {
        start(1, null);
    }

    @FXML
    private void handleOpen(ActionEvent event) {
        start(2, null);
    }

    @FXML
    private void handleOpenRecent(ActionEvent event) {
        start(3, App.getPrefs().getRecentProject());
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://particle-classification.readthedocs.io/en/latest/"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void start(int mode, String path) {
        vboxWelcome.setDisable(true);
        vboxWelcome.setEffect(new GaussianBlur(10));
        hboxLoading.setVisible(true);
//        MainController controller = null;
//        try {
//            controller = AbstractController.create(MainController.class, ResourceBundle.getBundle("bundles.Lang"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        controller.setupAccelerators();
//        controller.setupStage(controller.stage);
//        controller.show();
//        controller.setStartupMode(mode);
//        controller.startup();
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(100);
                return null;
            }
        };
        Task<Void> task2 = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(100);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
//            Platform.runLater(() -> {
            try {
                MainController controller = AbstractController.create(MainController.class, ResourceBundle.getBundle("bundles.Lang"));
                controller.setupAccelerators();
                controller.setupStage(controller.stage);
                controller.show();
                task2.setOnSucceeded(event1 -> {
                    controller.setStartupParams(path);
                    controller.setStartupMode(mode);
                    controller.startup();
                    stage.close();
                });
                task2.setOnFailed(event2 -> {
                    task2.getException().printStackTrace();
                });
                App.getExecutorService().submit(task2);
            } catch (Exception e) {
                BasicDialogs.ShowException("Error opening ParticleTrieur", e);
                e.printStackTrace();
            }
        });
        App.getExecutorService().submit(task);
    }
}
