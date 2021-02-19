/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.controls;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonBar.ButtonData;

// package scope
class DialogUtils {

    static void forcefullyHideDialog(javafx.scene.control.Dialog<?> dialog) {
        // for the dialog to be able to hide, we need a cancel button,
        // so lets put one in now and then immediately call hide, and then
        // remove the button again (if necessary).
        DialogPane dialogPane = dialog.getDialogPane();
        if (containsCancelButton(dialog)) {
            dialog.hide();
            return;
        }
        
        dialogPane.getButtonTypes().add(ButtonType.CANCEL); 
        dialog.hide();
        dialogPane.getButtonTypes().remove(ButtonType.CANCEL);
    }
    
    static boolean containsCancelButton(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        for (ButtonType type : dialogPane.getButtonTypes()) {
            if (type.getButtonData() == ButtonData.CANCEL_CLOSE) {
                return true;
            }
        }
        return false;
    }
}