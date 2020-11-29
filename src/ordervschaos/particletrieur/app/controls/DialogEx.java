package ordervschaos.particletrieur.app.controls;

import ordervschaos.particletrieur.app.App;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class DialogEx<T> extends Dialog<T> {

    public DialogEx() {
        super();
        Scene scene = this.getDialogPane().getScene();
        ((Stage)scene.getWindow()).getIcons().add(new Image(App.class.getResourceAsStream("resources/icon.png")));
        scene.getStylesheets().add(App.class.getResource("styles/style.css").toExternalForm());
    }
}