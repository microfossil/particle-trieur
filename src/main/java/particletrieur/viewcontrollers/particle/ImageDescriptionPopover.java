package particletrieur.viewcontrollers.particle;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import particletrieur.controls.ParticleInformationControl;
import particletrieur.controls.SymbolLabel;
import particletrieur.models.project.Particle;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import java.io.IOException;

public class ImageDescriptionPopover extends PopOver {

    public ImageDescriptionPopover(Particle particle, PopOver.ArrowLocation arrowLocation) {
        VBox infoVBox = new VBox();
        infoVBox.setPrefWidth(512);
        infoVBox.setPadding(new Insets(10, 10, 10, 10));
        infoVBox.setSpacing(5);
        try {
            ImageView thumbnail = new ImageView(particle.getImage());
            thumbnail.setFitHeight(512);
            thumbnail.setFitWidth(512);
            thumbnail.setPreserveRatio(true);
            StackPane pane = new StackPane();
            pane.setAlignment(Pos.CENTER);
            pane.getChildren().add(thumbnail);
            Button buttonClose = new Button();
            buttonClose.setGraphic(new SymbolLabel("featherx", 14));
                buttonClose.getStyleClass().add("flat-button");
                buttonClose.setOnAction(event -> this.hide());
                pane.getChildren().add(buttonClose);
                StackPane.setAlignment(buttonClose, Pos.TOP_RIGHT);
                infoVBox.getChildren().add(pane);
            } catch (IOException ex) {
                Label missingLabel = new Label("File is missing...");
            infoVBox.getChildren().add(missingLabel);
        }
        ParticleInformationControl pic = new ParticleInformationControl();
        pic.setData(particle, 0);
        infoVBox.getChildren().add(pic);
        setDetachable(false);
        setContentNode(infoVBox);
        setArrowLocation(arrowLocation);
    }
}
