package particletrieur.controls.dialogs;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import particletrieur.App;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import particletrieur.AppController;
import particletrieur.viewcontrollers.MainController;

import java.util.Optional;

public class AlertEx extends Alert {

    public AlertEx(AlertType alertType) {
        super(alertType);
        Scene scene = this.getDialogPane().getScene();
        ((Stage)scene.getWindow()).getIcons().add(new Image(App.class.getResourceAsStream("/icons/icon.png" )));
        scene.getStylesheets().add(App.class.getResource("/styles/style.css").toExternalForm());
    }

    public AlertEx(AlertType alertType, String contentText, ButtonType... buttons) {
        super(alertType, contentText, buttons);
        Scene scene = this.getDialogPane().getScene();
        ((Stage)scene.getWindow()).getIcons().add(new Image(App.class.getResourceAsStream("/icons/icon.png" )));
        scene.getStylesheets().add(App.class.getResource("/styles/style.css").toExternalForm());
    }

    public void showEmbedded() {
        AppController.getInstance().showDialog(this);
    }
}
