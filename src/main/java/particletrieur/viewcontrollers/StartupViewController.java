package particletrieur.viewcontrollers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import particletrieur.AbstractController;
import particletrieur.App;
import particletrieur.AppController;
import particletrieur.FxmlLocation;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.controls.SymbolLabel;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@FxmlLocation("/views/StartupView.fxml")
public class StartupViewController extends AbstractController implements Initializable {

    @FXML
    VBox vboxRecent;
    @FXML
    VBox vboxWelcome;
    @FXML
    HBox hboxLoading;
    @FXML
    SymbolLabel symbolLoading;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

    private void start(int mode, String path) {
        //Show "loading" screen
        vboxWelcome.setDisable(true);
        vboxWelcome.setEffect(new GaussianBlur(10));
        hboxLoading.setVisible(true);

        //Task to give a short delay to allow the JavaFX loop to show the "loading" screen
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(100);
                return null;
            }
        };

        //After the delay, add the main view to this stage and centre
        task.setOnSucceeded(event -> {
            try {
                MainController controller = AbstractController.create(MainController.class, ResourceBundle.getBundle("bundles.Lang"));
                controller.setupAccelerators();
                controller.setupStage(AppController.getStage());
                AnchorPane.setLeftAnchor(controller.root, 0.0);
                AnchorPane.setBottomAnchor(controller.root, 0.0);
                AnchorPane.setRightAnchor(controller.root, 0.0);
                AnchorPane.setTopAnchor(controller.root, 0.0);
                AppController.getInstance().setRoot(controller);
                controller.setStartupParams(path);
                controller.setStartupMode(mode);
                controller.startup();
            } catch (Exception e) {
                e.printStackTrace();
                BasicDialogs.ShowException("Error opening ParticleTrieur", e);
            }
        });
        App.getExecutorService().submit(task);
    }
}
