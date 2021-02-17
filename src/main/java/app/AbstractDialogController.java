package main.java.app;

import main.java.app.controls.SymbolLabel;
import main.java.app.controls.DialogEx;
import javafx.scene.control.ButtonType;

public abstract class AbstractDialogController extends AbstractController{

    private DialogEx dialog = null;
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
        asDialog(getHeader(), getSymbol(), getButtonTypes()).showAndWait();
//        MainController.instance.rootVBox.getChildren().add(asDialog().getDialogPane());
//        MainController.instance.rootDialog.setVisible(true);
    }

    @Override
    public void show() {
        asDialog(getHeader(), getSymbol(), getButtonTypes()).show();
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
