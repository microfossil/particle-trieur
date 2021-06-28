/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.export;

import com.google.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import particletrieur.AbstractDialogController;
import particletrieur.App;
import particletrieur.AppController;
import particletrieur.FxmlLocation;
import particletrieur.controls.dialogs.AlertEx;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Tag;
import particletrieur.models.project.Taxon;
import particletrieur.services.export.ExportImagesService;
import particletrieur.viewmodels.SelectionViewModel;
import particletrieur.viewmodels.export.ExportViewModel;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("/views/export/ExportDataView.fxml")
public class ExportDataViewController extends AbstractDialogController implements Initializable {

    @FXML
    public CheckBox checkBoxParameters;
    @FXML
    public CheckBox checkBoxMorphology;

    Supervisor supervisor;
    SelectionViewModel selectionViewModel;
    ExportViewModel exportViewModel;

    @Inject
    public ExportDataViewController(Supervisor supervisor, SelectionViewModel selectionViewModel, ExportViewModel exportViewModel) {
        this.supervisor = supervisor;
        this.selectionViewModel = selectionViewModel;
        this.exportViewModel = exportViewModel;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void processDialogResult(ButtonType buttonType) {
        if (buttonType == ButtonType.OK) {
            exportViewModel.exportDataToCSV(checkBoxParameters.isSelected(), checkBoxMorphology.isSelected());
        }
    }

    @Override
    public String getHeader() {
        return "Export Project";
    }

    @Override
    public String getSymbol() {
        return "featherupload";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[] { ButtonType.OK, ButtonType.CANCEL };
    }
}
