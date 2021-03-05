package particletrieur;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Dialog;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import particletrieur.controls.SymbolLabel;
import particletrieur.viewcontrollers.StartupViewController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@FxmlLocation("/views/App.fxml")
public class AppController extends AbstractController implements Initializable {

    private static AppController instance;
    public static AppController getInstance() {
        return instance;
    }

    @FXML
    StackPane rootContainer;
    @FXML
    StackPane rootDialog;
    @FXML
    StackPane rootLoading;
    @FXML
    SymbolLabel symbolLabelWorking;

    public static Stage getStage() { return App.getStage(); }
    public static Window getWindow() { return getRootContainer().getScene().getWindow(); }
    public static Pane getRootContainer() { return AppController.getInstance().rootContainer; }

    RotateTransition rt;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        try {
            StartupViewController controller = AbstractController.create(StartupViewController.class, ResourceBundle.getBundle("bundles.Lang"));
            setRoot(controller);
        } catch (IOException e) {
            e.printStackTrace();
        }
        rt = new RotateTransition(Duration.millis(3000), symbolLabelWorking);
        rt.setByAngle(360);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.setCycleCount(Animation.INDEFINITE);
        symbolLabelWorking.setCache(true);
        symbolLabelWorking.setCacheHint(CacheHint.SPEED);
    }

    public void setRoot(AbstractController controller) {
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(controller.root);
        getStage().sizeToScene();
        getStage().centerOnScreen();
    }

    public void showDialog(Dialog dialog) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(vbox);
        dialog.getDialogPane().setStyle("-fx-border-color: -fx-accent; -fx-border-width: 1; -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.5) , 20, 0.0 , 0 , 6 );");
        vbox.getChildren().add(dialog.getDialogPane());
        rootDialog.getChildren().add(hbox);
        rootDialog.setVisible(true);
        dialog.setOnHiding(event -> {
            rootDialog.getChildren().remove(hbox);
            if (rootDialog.getChildren().size() == 0) {
                rootDialog.setVisible(false);
            }
        });
    }

    public void showLoading(boolean show) {
        rootLoading.setVisible(show);
        if (show) {
            rootContainer.setEffect(new GaussianBlur(10));
            rt.play();
        }
        else {
            rootContainer.setEffect(null);
            rt.stop();
        }
    }
}
