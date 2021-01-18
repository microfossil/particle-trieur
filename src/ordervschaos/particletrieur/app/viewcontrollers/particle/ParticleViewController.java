/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.viewcontrollers.particle;

import com.google.inject.Inject;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.controls.SymbolLabel;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.viewcontrollers.particle.ImageDescriptionPopover;
import ordervschaos.particletrieur.app.viewmodels.MainViewModel;
import ordervschaos.particletrieur.app.viewmodels.SelectionViewModel;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import javax.imageio.ImageIO;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ParticleViewController implements Initializable {

    @FXML
    SymbolLabel fontIconExpander;
    @FXML
    CustomTextField customTextFieldFilter;

    @Inject
    private Supervisor supervisor;
    @Inject
    private SelectionViewModel selectionViewModel;
    @Inject
    MainViewModel mainViewModel;

    private final BooleanProperty expanded = new SimpleBooleanProperty();
    private TableColumn<Particle, File> colImage = new TableColumn<>();


    public BooleanProperty expandedProperty() { return expanded; }
    public boolean isExpanded() { return expanded.get(); }
    public void setExpanded(boolean value) {
        expanded.set(value);
        if (value) {
            fontIconExpander.setSymbol("featherrewind");
        } else {
            fontIconExpander.setSymbol("featherfastforward");
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUI();
    }

    public void setupUI() {
        //Initialise the filter text field to have a clear button
        try {
            Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
            m.setAccessible(true);
            m.invoke(null, customTextFieldFilter, customTextFieldFilter.rightProperty());
        } catch (Exception ex) {
            BasicDialogs.ShowException("There was a problem setting up the filter text field", ex);
        }

        //If filter changes update predicate
        customTextFieldFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            selectionViewModel.filteredList.setPredicate(particle -> filterParticle(particle, newValue));
        });
    }

    private boolean filterParticle(Particle particle, String filter) {
        if (filter == null || filter.length() < 1) {
            return true;
        }
        String lowerCaseFilter = filter.toLowerCase();

        String[] parts = lowerCaseFilter.split("\\s+");

        for (String part : parts) {
            String[] params = part.split("((?<=:)|(?=:)|(?<===)|(?===)|(?=!=)|(?=!=))");
            if (params.length != 3) {
                return false;
            }
            // TODO add the match all
            boolean matched = false;
            boolean result = false;
            switch (params[0]) {
                case "#":
                    matched = true;
                    int index;
                    try {
                        index = Integer.parseInt(params[2]);
                    }
                    catch (NumberFormatException ex) {
                        index = -1;
                    }
                    result = supervisor.project.particles.indexOf(particle) == index;
                    break;
                case "file":
                    matched = true;
                    result = particle.getShortFilename().toLowerCase().contains(params[2]);
                    break;
                case "folder":
                    matched = true;
                    result = particle.getFile().getParent().toLowerCase().contains(params[2]);
                    break;
                case "tag":
                    matched = true;
                    result = particle.tagsToString().toLowerCase().contains(params[2]);
                    break;
                case "label":
                    matched = true;
                    result = particle.getClassification().toLowerCase().contains(params[2]);
                    break;
                case "guid":
                    matched = true;
                    result = particle.getGUID().toLowerCase().contains(params[2]);
                    break;
                case "sample":
                    matched = true;
                    result = particle.getSampleID().toLowerCase().contains(params[2]);
                    break;
                case "index1":
                    matched = true;
                    result = Double.toString(particle.getIndex1()).equals(params[2]);
                    break;
                case "index2":
                    matched = true;
                    result = Double.toString(particle.getIndex2()).equals(params[2]);
                    break;
                case "valid":
                    matched = true;
                    if (params[2].equals("true")) result = particle.getValidator() != null && !particle.getValidator().equals("");
                    else result = particle.getValidator() == null || particle.getValidator().equals("");
                    break;
            }
            if (params[1].equals("!=")) {
                result = !result;
            }
            if (!matched) result = false;
            if (!result) return false;
        }
        return true;
    }

    @FXML
    private void handleExpandList(ActionEvent event) {
        mainViewModel.expandListRequested.broadcast();
    }

    @FXML
    private void handleSelectAll(ActionEvent event) {
        selectionViewModel.selectAllRequested.broadcast();
    }

    @FXML
    private void handleSmallImage(ActionEvent event) {
        selectionViewModel.decreaseListViewImageSize();
    }

    @FXML
    private void handleLargeImage(ActionEvent event) {
        selectionViewModel.increaseListViewImageSize();

    }

    private String getCurrentFilterArgument() {
        String current = customTextFieldFilter.getText();
        if (current.startsWith("#")) current = current.substring(1);
        if (current.length() == 0) return "";
        String[] parts = current.split(":");
        if (parts.length >= 2) return parts[1];
        else if (current.contains(":")) return "";
        else return parts[0];
    }

    @FXML
    public void handleFilterNumber(ActionEvent actionEvent) {
        customTextFieldFilter.setText("#==");
    }

    @FXML
    public void handleFilterFile(ActionEvent actionEvent) {
        customTextFieldFilter.setText(customTextFieldFilter.getText() + " file==");
    }

    @FXML
    public void handleFilterFolder(ActionEvent actionEvent) {
        customTextFieldFilter.setText(customTextFieldFilter.getText() + " folder==");
    }

    @FXML
    public void handleFilterLabel(ActionEvent actionEvent) {
        customTextFieldFilter.setText(customTextFieldFilter.getText() + " label==");
    }

    @FXML
    public void handleFilterTag(ActionEvent actionEvent) {
        customTextFieldFilter.setText(customTextFieldFilter.getText() + " tag==");
    }

    @FXML
    public void handleFilterIndex1(ActionEvent actionEvent) {
        customTextFieldFilter.setText(customTextFieldFilter.getText() + " index1==");
    }

    @FXML
    public void handleFilterIndex2(ActionEvent actionEvent) {
        customTextFieldFilter.setText(customTextFieldFilter.getText() + " index2==");
    }

    @FXML
    public void handleFilterGuid(ActionEvent actionEvent) {
        customTextFieldFilter.setText(customTextFieldFilter.getText() + " guid==");
    }

    @FXML
    public void handleFilterValid(ActionEvent actionEvent) {
        customTextFieldFilter.setText(customTextFieldFilter.getText() + " valid==");
    }

    @FXML
    public void handleFilterAll(ActionEvent actionEvent) {
        customTextFieldFilter.setText(getCurrentFilterArgument());
    }
}
