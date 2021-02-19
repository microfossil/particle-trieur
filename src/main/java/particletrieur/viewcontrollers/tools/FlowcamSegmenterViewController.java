package particletrieur.viewcontrollers.tools;

import javafx.stage.FileChooser;
import particletrieur.App;
import particletrieur.services.FlowcamSegmenterService;
import particletrieur.AbstractDialogController;
import particletrieur.FxmlLocation;
import particletrieur.services.network.CNNTrainingService;
import particletrieur.viewmodels.SelectionViewModel;
import com.google.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

@FxmlLocation("/views/FlowcamSegmenterView.fxml")
public class FlowcamSegmenterViewController extends AbstractDialogController implements Initializable {

    @FXML
    TextField textFieldCampaign;
    @FXML
    TextField textFieldInputDirectory;
    @FXML
    TextField textFieldSpeciesXLSX;
    @FXML
    TextField textFieldOutputDirectory;

    @Inject
    SelectionViewModel selectionViewModel;

    String lastPath;

    StringProperty inputDataCSV = new SimpleStringProperty();
    StringProperty inputSpeciesXLSX = new SimpleStringProperty();
    StringProperty outputDirectory = new SimpleStringProperty();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        textFieldInputDirectory.textProperty().bindBidirectional(inputDataCSV);
        textFieldSpeciesXLSX.textProperty().bindBidirectional(inputSpeciesXLSX);
        textFieldOutputDirectory.textProperty().bindBidirectional(outputDirectory);

        if(App.getPrefs().getFlowcamPath() != null && Files.exists(Paths.get(App.getPrefs().getFlowcamPath()))) {
            lastPath = App.getPrefs().getFlowcamPath();
//            inputDirectory.set(App.getPrefs().getFlowcamPath());
        }
        else {
            lastPath = System.getProperty("user.home");
        }
    }

    @FXML
    public void handleSelectInputDirectory(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        String filename = textFieldInputDirectory.getText();
        if(filename != null && (new File(filename)).exists()) {
            chooser.setInitialDirectory(new File(filename));
        }
        else if ((new File(lastPath)).exists()) {
            chooser.setInitialDirectory(new File(lastPath));
        }
        File directory = chooser.showDialog(this.stage);
        if (directory != null) {
            textFieldInputDirectory.setText(directory.getAbsolutePath());
            App.getPrefs().setFlowcamPath(directory.getAbsolutePath());
            lastPath = directory.getParent();
        }
//        FileChooser chooser = new FileChooser();
//        String filename = textFieldDataCSV.getText();
//        if(filename != null && (new File(filename)).exists()) {
//            chooser.setInitialDirectory((new File(filename)).getParentFile());
//        }
//        else if ((new File(lastPath)).exists()) {
//            chooser.setInitialDirectory(new File(lastPath));
//        }
//        File file = chooser.showOpenDialog(this.stage);
//        if (file != null) {
//            textFieldDataCSV.setText(file.getAbsolutePath());
//            App.getPrefs().setFlowcamPath(file.getParent());
//            lastPath = file.getParent();
//            if (textFieldOutputDirectory.getText() == null) {
//                String baseName = file.getParentFile().getName();
//                textFieldOutputDirectory.setText(Paths.get(file.getParent(), baseName + "_images_individuelles").toString());
//            }
//        }
    }

    @FXML
    public void handleSelectSpeciesXLSX(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        String filename = textFieldSpeciesXLSX.getText();
        if(filename != null && (new File(filename)).exists()) {
            chooser.setInitialDirectory((new File(filename)).getParentFile());
        }
        else if ((new File(lastPath)).exists()) {
            chooser.setInitialDirectory(new File(lastPath));
        }
        File file = chooser.showOpenDialog(this.stage);
        if (file != null) {
            textFieldSpeciesXLSX.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void handleSelectOutputDirectory(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        String filename = textFieldOutputDirectory.getText();
        if(filename != null && (new File(filename)).exists()) {
            chooser.setInitialDirectory(new File(filename));
        }
        else if ((new File(lastPath)).exists()) {
            chooser.setInitialDirectory(new File(lastPath));
        }
        File directory = chooser.showDialog(this.stage);
        if (directory != null) {
            textFieldOutputDirectory.setText(directory.getAbsolutePath());
            lastPath = directory.getParent();
        }
    }

    @Override
    public void processDialogResult(ButtonType buttonType) {
        if (buttonType == ButtonType.OK) {
            FlowcamSegmenterService segmenterService = new FlowcamSegmenterService();
            CNNTrainingService service = new CNNTrainingService();
            service.launchFlowcam(inputDataCSV.get(), outputDirectory.get(), textFieldCampaign.getText(), inputSpeciesXLSX.get());
//            Service service = segmenterService.createService(new File(inputDirectory.get()));

//            BasicDialogs.ProgressDialogWithCancel2(
//                    "Operation",
//                    "Adding files",
//                    App.getRootPane(),
//                    service).start();
        }
    }

    @Override
    public String getHeader() {
        return "Flowcam Segmenter";
    }

    @Override
    public String getSymbol() {
        return "featherimage";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[] { ButtonType.OK, ButtonType.CANCEL };
    }


}
