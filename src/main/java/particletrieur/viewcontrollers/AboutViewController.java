package particletrieur.viewcontrollers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import particletrieur.AbstractDialogController;
import particletrieur.App;
import particletrieur.FxmlLocation;

import java.net.URL;
import java.util.ResourceBundle;

@FxmlLocation("/views/AboutView.fxml")
public class AboutViewController extends AbstractDialogController implements Initializable {

    @FXML
    Label labelTitle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labelTitle.setText("ParticleTrieur " + App.VERSION + ", User: " + System.getProperty("user.name"));
    }

    @Override
    public void processDialogResult(ButtonType buttonType) {

    }

    @Override
    public String getHeader() {
        return "About";
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
