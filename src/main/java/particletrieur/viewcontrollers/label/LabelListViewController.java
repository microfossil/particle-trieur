/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.label;

import javafx.scene.control.*;
import particletrieur.models.project.Particle;
import particletrieur.AbstractDialogController;
import particletrieur.FxmlLocation;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Taxon;
import particletrieur.viewmodels.particles.LabelsViewModel;
import com.google.inject.Inject;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.FlowPane;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("/views/label/LabelListView.fxml")
public class LabelListViewController extends AbstractDialogController implements Initializable {

    @FXML
    private ListView<Taxon> listViewTaxons;
    @FXML
    private Label labelCode;
    @FXML
    Label labelGroup;
    @FXML
    private Label labelName;
    @FXML
    private Label labelDescription;
    @FXML
    private Label labelMorphotype;
    @FXML
    private Label labelRequired;
    @FXML
    private FlowPane flowPaneImages;
    @FXML
    private CheckBox checkBoxExampleImagesEnabled;
    @FXML
    private Button buttonEdit;
    @FXML
    private Button buttonRemove;

    @Inject
    private Supervisor supervisor;
    @Inject
    private LabelsViewModel labelsViewModel;

    public ObservableList<Taxon> taxonList = FXCollections.observableArrayList();
    private static DataFormat TAXON_MIME_TYPE = new DataFormat("application/x-java-serialized-object-taxon");

    private Timeline scrolltimeline = new Timeline();
    private double scrollVelocity = 0;
    private boolean dropped = false;
    int scrollSpeed = 200;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cell factory
        listViewTaxons.setCellFactory(param -> {
            ListCell<Taxon> cell = new ListCell<Taxon>() {
                @Override
                public void updateItem(Taxon item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) setText(item.getCode());
                    else setText("");
                }
            };
//            cell.setOnDragDetected(event -> {
//                if (!cell.isEmpty()) {
//                    Integer index = cell.getIndex();
//                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
//                    db.setDragView(cell.snapshot(null, null));
//                    ClipboardContent cc = new ClipboardContent();
//                    cc.put(TAXON_MIME_TYPE, index);
//                    db.setContent(cc);
//                    event.consume();
//                }
//            });
//
//            cell.setOnDragOver(event -> {
//                Dragboard db = event.getDragboard();
//                if (db.hasContent(TAXON_MIME_TYPE)) {
//                    if (cell.getIndex() != ((Integer)db.getContent(TAXON_MIME_TYPE)).intValue()) {
//                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
//                        event.consume();
//                    }
//                }
//            });
//
//            cell.setOnDragDropped(event -> {
//                Dragboard db = event.getDragboard();
//                if (db.hasContent(TAXON_MIME_TYPE)) {
//                    int draggedIndex = (Integer) db.getContent(TAXON_MIME_TYPE);
//                    Taxon draggedPerson = (Taxon) listViewTaxons.getItems().remove(draggedIndex);
//                    int dropIndex ;
//                    if (cell.isEmpty()) {
//                        dropIndex = listViewTaxons.getItems().size();
//                    } else {
//                        dropIndex = cell.getIndex();
//                    }
//                    listViewTaxons.getItems().add(dropIndex, draggedPerson);
//                    event.setDropCompleted(true);
//                    listViewTaxons.getSelectionModel().select(dropIndex);
//                    event.consume();
//                }
//            });

            return cell;
        });

        listViewTaxons.getSelectionModel().selectedItemProperty().addListener((obs, oldv, newv) -> {
            if (newv != null) {
                populateFields(newv);
            }
        });
        listViewTaxons.setItems(taxonList);

        checkBoxExampleImagesEnabled.selectedProperty().addListener((obs, oldv, newv) -> {
            populateFields(listViewTaxons.getSelectionModel().getSelectedItem());
        });

//        setupScrolling();

        supervisor.project.taxonsUpdatedEvent.addListener(event -> {
            updateList();
            populateFields(listViewTaxons.getSelectionModel().getSelectedItem());
        });

        updateList();
    }

    private void updateList() {
        List<Taxon> list = supervisor.project.getTaxons().values().stream().sorted(Comparator.comparing(Taxon::getCode)).collect(Collectors.toList());
        Taxon selected = listViewTaxons.getSelectionModel().getSelectedItem();
        taxonList.clear();
        taxonList.addAll(list);
        if (list.contains(selected)) {
            listViewTaxons.getSelectionModel().select(selected);
        } else {
            listViewTaxons.getSelectionModel().selectFirst();
        }
    }

    private void populateFields(Taxon taxon) {
        labelCode.setText(taxon.getCode());
        labelName.setText(taxon.getName());
        labelDescription.setText(taxon.getDescription());
        if (taxon.getIsClass()) {
            labelMorphotype.setText("Yes");
        } else {
            labelMorphotype.setText("No");
        }
        if (Arrays.asList(supervisor.project.requiredTaxons).contains(taxon.getCode())) {
            labelRequired.setText("Yes");
            buttonEdit.setDisable(true);
            buttonRemove.setDisable(true);
        } else {
            labelRequired.setText("No");
            buttonEdit.setDisable(false);
            buttonRemove.setDisable(false);
        }
        // Add example images
        flowPaneImages.getChildren().clear();

        if (checkBoxExampleImagesEnabled.isSelected()) {
            int added = 0;
            int index = 0;
            for (Particle particle : supervisor.project.getParticles()) {
                index++;
                if (particle.classification.get().equals(taxon.getCode())) {
                    try {
                        ImageView imageView = new ImageView(particle.getImage());
                        imageView.setFitWidth(80);
                        imageView.setFitHeight(80);
                        flowPaneImages.getChildren().add(imageView);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    added++;
                }
                if (added >= 30) break;
            }
        }
    }

    @FXML
    void handleEdit(ActionEvent event) {
        try {
            EditLabelViewController controller = AbstractDialogController.create(EditLabelViewController.class);
            controller.setup(listViewTaxons.getSelectionModel().getSelectedItem());
            controller.showEmbedded();
//            populateFields(listViewTaxons.getSelectionModel().getSelectedItem());
//            listViewTaxons.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleRemove(ActionEvent event) {
        //Get current taxon
        Taxon adapter = listViewTaxons.getSelectionModel().getSelectedItem();
        labelsViewModel.deleteLabel(adapter);
    }


    @FXML
    void handleAdd(ActionEvent event) {
        try {
            EditLabelViewController controller = AbstractDialogController.create(EditLabelViewController.class);
            controller.showEmbedded();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @FXML
//    void handleAlphabeticalOrdering(ActionEvent event) {
//        LinkedHashMap<String, Taxon> result = supervisor.project.getTaxons().entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
//                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
//        supervisor.project.taxons = result;
//    }
//
//    @FXML
//    void handleResetOrdering(ActionEvent event) {
//        updateList();
//    }

//    private void setupScrolling() {
//        scrolltimeline.setCycleCount(Timeline.INDEFINITE);
//        scrolltimeline.getKeyFrames().add(new KeyFrame(Duration.millis(20), (ActionEvent) -> { dragScroll();}));
//
//        listViewTaxons.setOnDragExited((DragEvent event) -> {
//            if (event.getY() > 0) {
//                scrollVelocity = 1.0 / scrollSpeed;
//            }
//            else {
//                scrollVelocity = -1.0 / scrollSpeed;
//            }
//            if (!dropped){
//                scrolltimeline.play();
//            }
//        });
//
//        listViewTaxons.setOnDragEntered(event -> {
//            scrolltimeline.stop();
//            dropped = false;
//        });
//        listViewTaxons.setOnDragDone(event -> {
//            scrolltimeline.stop();
//        });
//        listViewTaxons.setOnDragDropped((DragEvent event) ->{
//            Dragboard db = event.getDragboard();
//            scrolltimeline.stop();
//            event.setDropCompleted(true);
//            dropped = true;
//        });
//        listViewTaxons.setOnScroll(event -> {
//            scrolltimeline.stop();
//        });
//    }
//
//    private void dragScroll() {
//        ScrollBar sb = getVerticalScrollbar();
//        if (sb != null) {
//            double newValue = sb.getValue() + scrollVelocity;
//            newValue = Math.min(newValue, 1.0);
//            newValue = Math.max(newValue, 0.0);
//            sb.setValue(newValue);
//        }
//    }
//
//    private ScrollBar getVerticalScrollbar() {
//        ScrollBar result = null;
//        for (Node n : listViewTaxons.lookupAll(".scroll-bar")) {
//            if (n instanceof ScrollBar) {
//                ScrollBar bar = (ScrollBar) n;
//                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
//                    result = bar;
//                }
//            }
//        }
//        return result;
//    }

    @Override
    public void processDialogResult(ButtonType buttonType) {
//        if (buttonType == ButtonType.APPLY) {
//            ArrayList<Taxon> newTaxons = new ArrayList<>();
//            for (Taxon taxon : taxonList) {
//                newTaxons.add(taxon);
//            }
//            labelsViewModel.initialiseLabels(newTaxons);
//        }
    }

    @Override
    public String getHeader() {
        return "Labels";
    }

    @Override
    public String getSymbol() {
        return "feathertag";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[]{ButtonType.CLOSE};
    }
}
