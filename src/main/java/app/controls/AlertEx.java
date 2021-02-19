package main.java.app.controls;

import main.java.app.App;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AlertEx extends Alert {

    public AlertEx(AlertType alertType) {
        super(alertType);
        Scene scene = this.getDialogPane().getScene();
        ((Stage)scene.getWindow()).getIcons().add(new Image(App.class.getResourceAsStream("resources/icon.png")));
        scene.getStylesheets().add(App.class.getResource("resources/styles/style.css").toExternalForm());
    }

    public AlertEx(AlertType alertType, String contentText, ButtonType... buttons) {
        super(alertType, contentText, buttons);
        Scene scene = this.getDialogPane().getScene();
        ((Stage)scene.getWindow()).getIcons().add(new Image(App.class.getResourceAsStream("resources/icon.png")));
        scene.getStylesheets().add(App.class.getResource("resources/styles/style.css").toExternalForm());
    }
}
