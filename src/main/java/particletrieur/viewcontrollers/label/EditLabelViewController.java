/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.label;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.models.project.Taxon;
import particletrieur.AbstractDialogController;
import particletrieur.FxmlLocation;
import particletrieur.models.taxonomy.RappTaxon;
import particletrieur.models.taxonomy.WormsTaxon;
import particletrieur.services.taxonomy.WormsService;
import particletrieur.viewmodels.particles.LabelsViewModel;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("/views/label/EditLabelView.fxml")
public class EditLabelViewController extends AbstractDialogController implements Initializable {

    @FXML
    public TextField textFieldCode;
    @FXML
    public TextField textFieldName;
    @FXML
    public TextArea textAreaDescription;
    @FXML
    public CheckBox checkBoxIsClass;
    @FXML
    public ComboBox<String> comboBoxGroup;
    public TextField textFieldRappXlsxPath;
    public Button buttonRAPPXLSXLoad;
    public ListView<RappTaxon> listViewRappTaxons;
    public TextField textFieldWormsSearch;
    public Button buttonWormsSearch;
    public ListView<WormsTaxon> listViewWormsTaxons;
    public Label labelWormsStatus;
    public Label labelWormsTaxonInformation;

    private Taxon taxon;
    private LabelsViewModel labelsViewModel;

    public ObservableList<WormsTaxon> wormsTaxons = FXCollections.observableArrayList();

    @Inject
    public EditLabelViewController(LabelsViewModel labelsViewModel) {
        this.labelsViewModel = labelsViewModel;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // RAPP taxon list
        listViewRappTaxons.setCellFactory(param -> {
            ListCell<RappTaxon> cell = new ListCell<RappTaxon>() {
                @Override
                public void updateItem(RappTaxon item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) setText(item.group + " - " + item.name);
                    else setText("");
                }
            };
            return cell;
        });
        listViewRappTaxons.setItems(labelsViewModel.rappTaxons);
        listViewRappTaxons.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            textFieldCode.setText(newValue.group + "_" + newValue.name);
            textFieldName.setText(newValue.name);
            textAreaDescription.setText(String.format("id: %d\ntype: %s\ngroup: %s\nname: %s", newValue.id, newValue.type, newValue.group, newValue.name));
        }));
        textFieldRappXlsxPath.textProperty().bind(labelsViewModel.rappXLXSPathProperty());

        // Worms search results
        listViewWormsTaxons.setCellFactory(param -> {
            ListCell<WormsTaxon> cell = new ListCell<WormsTaxon>() {
                @Override
                public void updateItem(WormsTaxon item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) setText(item.scientificname);
                    else setText("");
                }
            };
            return cell;
        });
        listViewWormsTaxons.setItems(wormsTaxons);
        listViewWormsTaxons.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            textFieldCode.setText(newValue.scientificname.replace(' ', '_'));
            textFieldName.setText(newValue.scientificname);
            textAreaDescription.setText(newValue.toString());

            labelWormsTaxonInformation.setText(newValue.toString());
        }));
    }

    public void setup(Taxon taxon) {
        this.taxon = taxon;
        textFieldCode.setText(taxon.getCode());
        textFieldName.setText(taxon.getName());
        textAreaDescription.setText(taxon.getDescription());
        checkBoxIsClass.setSelected(taxon.getIsClass());
    }

    private Taxon create() {
        if (textFieldCode.getText().equals("")) return null;
        Taxon newTaxon = new Taxon(
                textFieldCode.getText(),
                textFieldName.getText(),
                textAreaDescription.getText(),
                "",
                checkBoxIsClass.isSelected());
        return newTaxon;
    }

    public void handleLoadRAPPXLSX(ActionEvent actionEvent) {
        labelsViewModel.loadRappTaxonomy();
    }

    @Override
    public void processDialogResult(ButtonType buttonType) {
        if (buttonType == ButtonType.OK) {
            Taxon newTaxon = create();
            if (newTaxon != null) {
                if (this.taxon == null) {
                    labelsViewModel.addLabel(newTaxon);
                } else {
                    labelsViewModel.updateLabel(taxon.getCode(), newTaxon);
                }
            }
        }
    }

    @Override
    public void postDialogSetup() {
        ((BorderPane) this.root).setPadding(new Insets(0));
    }

    @Override
    public String getHeader() {
        return "Edit Label";
    }

    @Override
    public String getSymbol() {
        return "featheredit3";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[]{ButtonType.OK, ButtonType.CANCEL};
    }

    public void handleWormsSearch(ActionEvent actionEvent) {
        Service<List<WormsTaxon>> service = WormsService.searchTaxonsService(textFieldWormsSearch.getText());
        wormsTaxons.clear();
        labelWormsStatus.setText("Searching...");
        labelWormsTaxonInformation.setText("");
        service.setOnSucceeded(event -> {
            if (service.getValue().size() == 0) {
                BasicDialogs.ShowInfo("Search", "No records found");
            }
            else {
                labelWormsStatus.setText(String.format("Found %d results", service.getValue().size()));
                wormsTaxons.addAll(service.getValue());
            }
        });
        service.setOnFailed(event -> {
            labelWormsStatus.setText("Error searching for " + textFieldWormsSearch.getText());
            service.getException().printStackTrace();
            BasicDialogs.ShowError("Error", service.getException().getMessage());
        });
        service.start();
    }
}
