package particletrieur.viewcontrollers.particle;

import com.google.inject.Inject;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import particletrieur.AbstractDialogController;
import particletrieur.FxmlLocation;
import particletrieur.models.Supervisor;
import particletrieur.viewmanagers.UndoManager;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("/views/particle/MissingParticlesView.fxml")
public class MissingParticlesViewController extends AbstractDialogController implements Initializable {

    @Inject
    Supervisor supervisor;
    @Inject
    UndoManager undoManager;

    public MissingParticlesViewController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @Override
    public void processDialogResult(ButtonType buttonType) {

    }

    @Override
    public String getHeader() {
        return "Add Images";
    }

    @Override
    public String getSymbol() {
        return "featherplus";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[]{ButtonType.OK, ButtonType.CANCEL};
    }
}
