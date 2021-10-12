/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.particle;

import com.google.inject.Inject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.CheckTreeView;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import particletrieur.AbstractDialogController;
import particletrieur.App;
import particletrieur.FxmlLocation;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.controls.dialogs.ProgressDialog2;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Particle;
import particletrieur.models.project.TreeTaxon;
import particletrieur.services.ParametersFromCSVService;
import particletrieur.services.ProjectService;
import particletrieur.viewmanagers.UndoManager;
import particletrieur.viewmanagers.commands.AddParticlesCommand;
import particletrieur.viewmodels.particles.LabelsViewModel;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("/views/particle/TaxonTreeView.fxml")
public class TaxonTreeViewController extends AbstractDialogController implements Initializable {

    @FXML
    CheckTreeView<TreeTaxon> treeView;

    @Inject
    Supervisor supervisor;
    @Inject
    UndoManager undoManager;
    @Inject
    LabelsViewModel labelsViewModel;

    public TaxonTreeViewController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        treeView.setCellFactory(tv -> {
            CheckBoxTreeCell<TreeTaxon> cell = new CheckBoxTreeCell<TreeTaxon>() {
                @Override
                public void updateItem(TreeTaxon item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.name);
                    }
                }
            };
            return cell ;
        });
    }

    @FXML
    public void handleAddTree(ActionEvent event) {
        TreeTaxon t = labelsViewModel.addTaxonTree();
        if (t != null) {
            treeView.setRoot(convertToTreeItem(t));
        }
    }

    public CheckBoxTreeItem<TreeTaxon> convertToTreeItem(TreeTaxon t) {
        CheckBoxTreeItem<TreeTaxon> item = new CheckBoxTreeItem<>();
        item.setExpanded(true);
        item.setValue(t);
        for (Map.Entry<String, TreeTaxon> entry : t.children.entrySet()) {
            item.getChildren().add(convertToTreeItem(entry.getValue()));
        }
        return item;
    }
//
//    private CheckBoxTreeItem<ProjectService.DisplayPath> createTreeView(ProjectService.Directory root, CheckBoxTreeItem<ProjectService.DisplayPath> treeItem, boolean withFiles) {
//        if (withFiles) {
//            for (File file : root.files) {
//                CheckBoxTreeItem<ProjectService.DisplayPath> fileTreeItem = new CheckBoxTreeItem<>(new ProjectService.DisplayPath(file.getAbsolutePath(), file.getName()));
//                treeItem.getChildren().add(fileTreeItem);
//            }
//        }
//        for (Map.Entry<String, ProjectService.Directory> entry : root.directories.entrySet()) {
//            String name = entry.getKey();
//            ProjectService.Directory dir = entry.getValue();
//            if (root.directories.size() == 1) {
//                treeItem.getValue().path = dir.path;
//                treeItem.getValue().displayPath = dir.path;
//                return createTreeView(dir, treeItem, withFiles);
//            }
//            else {
//                CheckBoxTreeItem<ProjectService.DisplayPath> dirTreeItem = new CheckBoxTreeItem<>(new ProjectService.DisplayPath(dir.path, name));
//                treeItem.getChildren().add(createTreeView(dir, dirTreeItem, withFiles));
//            }
//        }
//        return treeItem;
//    }


//    private void findCheckedItems(CheckBoxTreeItem<ProjectService.DisplayPath> item, List<CheckBoxTreeItem<ProjectService.DisplayPath>> checkedItems) {
//        if (item.getChildren().size() == 0 && item.isSelected()) {
//            checkedItems.add(item);
//        }
//        for (TreeItem<?> child : item.getChildren()) {
//            findCheckedItems((CheckBoxTreeItem<ProjectService.DisplayPath>) child, checkedItems);
//        }
//    }



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
