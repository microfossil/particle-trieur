package particletrieur;

import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import particletrieur.controls.dialogs.DialogEx;
import particletrieur.controls.SymbolLabel;
import particletrieur.viewcontrollers.MainController;

public abstract class AbstractJFXDialogController extends AbstractController{

    private DialogEx dialog = null;
    private HBox container = null;
    public DialogEx getDialog() {
        return dialog;
    }

    public abstract void processDialogResult(ButtonType buttonType);

    public abstract String getHeader();

    public abstract String getSymbol();

    public abstract ButtonType[] getButtonTypes();

    public void showEmbedded() {
        asDialog(getHeader(), getSymbol(), getButtonTypes()).showAndWait();
    }

    @Override
    public void showAndWait() {
        show();
    }

    @Override
    public void show() {
        container = new HBox();
        VBox vbox = new VBox();
        container.getChildren().add(vbox);
        dialog = asDialog(getHeader(), getSymbol(), getButtonTypes());
        dialog.getDialogPane().setStyle("-fx-border-color: -fx-accent; -fx-border-width: 1; -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.5) , 20, 0.0 , 0 , 6 );");
        vbox.getChildren().add(dialog.getDialogPane());
//        MainController.instance.rootMain.setEffect(new GaussianBlur(10));
        MainController.instance.rootVBox.getChildren().add(container);
        MainController.instance.rootDialog.setVisible(true);
    }

    public DialogEx asDialog(ButtonType... buttonTypes) {
        DialogEx dialog = new DialogEx();
        this.dialog = dialog;
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypes);
        dialog.getDialogPane().setContent(stage.getScene().getRoot());
        dialog.setResultConverter(button -> {
            processDialogResult((ButtonType) button);
//            MainController.instance.rootMain.setEffect(null);
            MainController.instance.rootVBox.getChildren().remove(container);
            MainController.instance.rootDialog.setVisible(false);
            return button;
        });
        postDialogSetup();
        return dialog;
    }

    public DialogEx asDialog(String header, String symbol, ButtonType... buttonTypes) {
        DialogEx dialog = asDialog(buttonTypes);
        dialog.setHeaderText(header);
        dialog.setTitle(header);
        dialog.setGraphic(new SymbolLabel(symbol, 24));
        return dialog;
    }

    public void postDialogSetup() {
        this.getDialog().getDialogPane().getScene().getWindow().sizeToScene();
    }
}
