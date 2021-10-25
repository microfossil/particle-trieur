/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.particle;

import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckTreeView;
import particletrieur.AbstractDialogController;
import particletrieur.FxmlLocation;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.models.Supervisor;
import particletrieur.models.ecotaxa.EcoTaxaSearchResult;
import particletrieur.models.ecotaxa.EcoTaxaTaxon;
import particletrieur.models.project.Taxon;
import particletrieur.models.project.TreeTaxon;
import particletrieur.services.ecotaxa.EcoTaxaService;
import particletrieur.viewmanagers.UndoManager;
import particletrieur.viewmodels.particles.LabelsViewModel;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("/views/particle/TaxonTreeView.fxml")
public class TaxonTreeViewController extends AbstractDialogController implements Initializable {

    public Label statusLabel;
    public ListView<EcoTaxaSearchResult> searchResultsListView;
    public VBox taxonDetails;
    public Label taxonName;
    public VBox taxonLineage;
    public Label taxonId;
    public Label taxonNumberChildren;
    @FXML
    TextField taxonCodeTextField;
    @FXML
    TextField taxonSearchTextField;

    @FXML
    CheckTreeView<TreeTaxon> treeView;

    @Inject
    Supervisor supervisor;
    @Inject
    UndoManager undoManager;
    @Inject
    LabelsViewModel labelsViewModel;

    ObservableList<EcoTaxaSearchResult> searchResults = FXCollections.observableArrayList();

    public TaxonTreeViewController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        treeView.setCellFactory(tv -> {
//            CheckBoxTreeCell<TreeTaxon> cell = new CheckBoxTreeCell<TreeTaxon>() {
//                @Override
//                public void updateItem(TreeTaxon item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (item == null || empty) {
//                        setText(null);
//                    } else {
//                        setText(item.name);
//                    }
//                }
//            };
//            return cell ;
//        });

        searchResultsListView.setCellFactory(params -> {
            return new ListCell<EcoTaxaSearchResult>() {
                @Override
                public void updateItem(EcoTaxaSearchResult item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) setText(item.text);
                    else setText("");
                }
            };
        });

        searchResultsListView.setItems(searchResults);
        searchResultsListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            taxonLineage.getChildren().clear();
            taxonId.setText("");
            taxonName.setText("");
            taxonNumberChildren.setText("");
            if (newValue == null) {
                return;
            }

            taxonName.setText("Loading...");

            Service<EcoTaxaTaxon> service = EcoTaxaService.getTaxonByIdService(newValue.id);
            service.setOnFailed(ev -> {
                taxonName.setText("");
                BasicDialogs.ShowError(
                        "Error",
                        String.format("Error retreiving EcoTaxa taxon id %d\n\n%s",
                                newValue.id,
                                service.getException().getMessage())
                );
            });
            service.setOnSucceeded(ev -> {
                taxonId.setText(String.format("Taxon #%d", service.getValue().id));
                taxonName.setText(service.getValue().display_name);
                taxonNumberChildren.setText(String.format("%d children", service.getValue().nb_children_objects));
                taxonLineage.getChildren().clear();
//                StringBuilder sb = new StringBuilder();
//                int i = 0;
                String[] lineage = service.getValue().lineage;
                int j = 0;
                for (int i = lineage.length-1; i >=0; i--) {
                    if (j == 0) taxonLineage.getChildren().add(new Label(lineage[i]));
                    else taxonLineage.getChildren().add(new Label(String.format("%" + j + "s", "") + lineage[i]));
                    j += 2;
                }
//                for (String s : ) {

//                    if (i != 0) sb.insert(0, s + " -> ");
//                    else sb.insert(0, s);
//                    i++;
//                }
//                statusLabel.setText(sb.toString());
            });
            service.start();
        }));
    }

    @FXML
    public void handleAddTree(ActionEvent event) {
        TreeTaxon t = labelsViewModel.addTaxonTree();
        if (t != null) {
            treeView.setRoot(convertToTreeItem(t));
        }
    }

    @FXML
    public void handleSearchCode(ActionEvent event) {
        try {
            EcoTaxaTaxon taxon = EcoTaxaService.getTaxonById(Integer.parseInt(taxonCodeTextField.getText()));
            System.out.println(taxon.display_name);
            Arrays.stream(taxon.lineage).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSearchText(ActionEvent event) {
        Service<List<EcoTaxaSearchResult>> service = EcoTaxaService.searchTaxonsService(taxonSearchTextField.getText());
        statusLabel.setText(String.format("Searching for %s...", taxonSearchTextField.getText()));
        service.setOnFailed(ev -> {
            Exception ex = new Exception(service.getException());
            ex.printStackTrace();
            statusLabel.setText("Error:  + ex.getMessage()");
        });
        service.setOnSucceeded(ev -> {
            List<EcoTaxaSearchResult> results = service.getValue();
            statusLabel.setText(String.format("Found %d results", results.size()));
            searchResults.clear();
            searchResults.addAll(results);
        });
        service.start();
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
