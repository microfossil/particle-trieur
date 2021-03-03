package particletrieur;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import particletrieur.controls.SymbolLabel;
import particletrieur.controls.dialogs.DialogEx;
import javafx.scene.control.ButtonType;
import particletrieur.viewcontrollers.MainController;

import java.util.function.Consumer;


public abstract class AbstractDialogController extends AbstractController {

    protected DialogEx dialog = null;
    public DialogEx getDialog() {
        return dialog;
    }

    public abstract void processDialogResult(ButtonType buttonType);

    public abstract String getHeader();

    public abstract String getSymbol();

    public abstract ButtonType[] getButtonTypes();

    public void showEmbedded() {
        dialog = asDialog(getHeader(), getSymbol(), getButtonTypes());
        MainController.instance.showDialog(dialog);
    }

    @Override
    public void showAndWait() {
        show();
    }

    @Override
    public void show() {
        showEmbedded();
    }

    public DialogEx asDialog(ButtonType... buttonTypes) {
        DialogEx dialog = new DialogEx();
        this.dialog = dialog;
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypes);
        dialog.getDialogPane().setContent(stage.getScene().getRoot());
        dialog.setResultConverter(button -> {
            processDialogResult((ButtonType) button);
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
