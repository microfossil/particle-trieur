package particletrieur.controls.dialogs;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.DialogEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import particletrieur.App;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import particletrieur.AppController;
import particletrieur.viewcontrollers.MainController;

import javax.swing.text.html.Option;
import java.util.Optional;

public class DialogEx<T> extends Dialog<T> {

    public DialogEx() {
        super();
        Scene scene = this.getDialogPane().getScene();
        ((Stage)scene.getWindow()).getIcons().add(new Image(App.class.getResourceAsStream("/icons/icon.png" )));
        scene.getStylesheets().add(App.class.getResource("/styles/style.css").toExternalForm());
    }

    public void showEmbedded() {
        AppController.getInstance().showDialog(this);
    }
}
