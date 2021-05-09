package particletrieur.viewcontrollers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.apache.commons.lang3.SystemUtils;
import particletrieur.AbstractDialogController;
import particletrieur.FxmlLocation;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

@FxmlLocation("/views/WhatsNewView.fxml")
public class WhatsNewViewController extends AbstractDialogController implements Initializable {

    @FXML
    Label labelTitle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void browseURL(String urlString) {

        try {
            if (SystemUtils.IS_OS_LINUX) {
                // Workaround for Linux because "Desktop.getDesktop().browse()" doesn't work on some Linux implementations
                if (Runtime.getRuntime().exec(new String[] { "which", "xdg-open" }).getInputStream().read() != -1) {
                    Runtime.getRuntime().exec(new String[] { "xdg-open", urlString });
                } else {
                    System.out.println("xdg-open not supported!");
                }
            } else {
                if (Desktop.isDesktopSupported())
                {
                    Desktop.getDesktop().browse(new URI(urlString));
                } else {
                    System.out.println("Desktop command not supported!");
                }
            }

        } catch (IOException | URISyntaxException e) {
            System.out.println("Failed to open URL");
        }
    }

    @FXML
    public void handleClickLink(ActionEvent event) {
        browseURL("https://github.com/microfossil/particle-trieur/issues");
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
