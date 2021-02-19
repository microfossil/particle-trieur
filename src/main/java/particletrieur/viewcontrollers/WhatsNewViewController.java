package particletrieur.viewcontrollers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import particletrieur.AbstractDialogController;
import particletrieur.FxmlLocation;

import java.net.URL;
import java.util.ResourceBundle;

@FxmlLocation("/views/WhatsNewView.fxml")
public class WhatsNewViewController extends AbstractDialogController implements Initializable {

    @FXML
    Label labelTitle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @Override
    public void processDialogResult(ButtonType buttonType) {

    }

    @Override
    public String getHeader() {
        return "What's New";
    }

    @Override
    public String getSymbol() {
        return "feathercoffee";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[] {ButtonType.CLOSE};
    }
}
