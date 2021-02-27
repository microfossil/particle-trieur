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
import particletrieur.viewcontrollers.MainController;

import javax.swing.text.html.Option;
import java.util.Optional;

public class DialogEx<T> extends Dialog<T> {

    HBox container;

    public DialogEx() {
        super();
        Scene scene = this.getDialogPane().getScene();
        ((Stage)scene.getWindow()).getIcons().add(new Image(App.class.getResourceAsStream("/icons/icon.png" )));
        scene.getStylesheets().add(App.class.getResource("/styles/style.css").toExternalForm());
    }

    public void showEmbedded() {
        container = new HBox();
        VBox vbox = new VBox();
        container.getChildren().add(vbox);
        this.getDialogPane().setStyle("-fx-border-color: -fx-accent; -fx-border-width: 1; -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.5) , 20, 0.0 , 0 , 6 );");
        vbox.getChildren().add(this.getDialogPane());
        MainController.instance.rootVBox.getChildren().add(container);
        MainController.instance.rootDialog.setVisible(true);
        this.setOnHiding(event -> {
            MainController.instance.rootVBox.getChildren().remove(container);
            if (MainController.instance.rootVBox.getChildren().size() == 0) {
                MainController.instance.rootDialog.setVisible(false);
            }
        });
    }
}
