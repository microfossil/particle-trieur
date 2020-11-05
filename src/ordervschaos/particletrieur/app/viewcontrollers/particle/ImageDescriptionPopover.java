package ordervschaos.particletrieur.app.viewcontrollers.particle;

import ordervschaos.particletrieur.app.models.project.Particle;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.control.PopOver;

import java.io.File;
import java.io.IOException;

public class ImageDescriptionPopover extends PopOver {

    public ImageDescriptionPopover(Particle particle, PopOver.ArrowLocation arrowLocation) {
        VBox infoVBox = new VBox();
        //infoVBox.setPrefWidth(200);
        infoVBox.setPadding(new Insets(10, 10, 10, 10));
        infoVBox.setSpacing(5);
        try {
            ImageView thumbnail = new ImageView(particle.getImage());
            thumbnail.maxWidth(200);
            thumbnail.maxHeight(200);
            infoVBox.getChildren().add(thumbnail);
        } catch (IOException ex) {
            Label missingLabel = new Label("File is missing...");
            infoVBox.getChildren().add(missingLabel);
        }
        GridPane infoGridPane = new GridPane();
        infoGridPane.setHgap(5);
        infoGridPane.setVgap(5);
        infoGridPane.add(new Text("Class:"),0,0);
        infoGridPane.add(new Text("Core:"),0,1);
        infoGridPane.add(new Text("Depth:"),0,2);
        infoGridPane.add(new Text("GUID:"),0,3);
        infoGridPane.add(new Text("Filename:"),0,4);
        infoGridPane.add(new Text("Path:"),0,5);
        infoGridPane.add(new Text("Size:"),0,6);
        infoGridPane.add(new Text(particle.classification.get()),1,0);
        infoGridPane.add(new Text(particle.getSampleID()),1,1);
        infoGridPane.add(new Text(String.format("%f - %f", particle.getIndex1(), particle.getIndex2())),1,2);
        infoGridPane.add(new Text(particle.getGUID()),1,3);
        infoGridPane.add(new Text(particle.getShortFilename()),1,4);
        infoGridPane.add(new Text(particle.getFolder()),1,5);
        infoGridPane.add(new Text(printFileSize(particle.getFile())),1,6);
        infoVBox.getChildren().add(infoGridPane);
        setDetachable(false);
        setContentNode(infoVBox);
        setArrowLocation(arrowLocation);
    }

    public String printFileSize(File file) {
        long sizeInBytes = file.length();
        String result;
        if (sizeInBytes < 1024) {
            result = String.format("%d bytes", sizeInBytes);
        }
        else if (sizeInBytes < (1024*1024)) {
            result = String.format("%.1f kB", (double) sizeInBytes / 1024);
        }
        else {
            result = String.format("%.1f MB", (double) sizeInBytes / (1024*1024));
        }
        return result;
    }
}
