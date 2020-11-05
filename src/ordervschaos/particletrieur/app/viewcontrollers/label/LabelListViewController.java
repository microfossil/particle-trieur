/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.viewcontrollers.label;

import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.AbstractController;
import ordervschaos.particletrieur.app.AbstractDialogController;
import ordervschaos.particletrieur.app.FxmlLocation;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.project.Taxon;
import ordervschaos.particletrieur.app.viewmodels.LabelsViewModel;
import com.google.inject.Inject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
@FxmlLocation("views/label/LabelListView.fxml")
public class LabelListViewController extends AbstractController implements Initializable {
    
    @FXML private ListView<Taxon> listViewTaxons;
    @FXML private Label labelCode;
    @FXML Label labelGroup;
    @FXML private Label labelName;
    @FXML private Label labelDescription;
    @FXML private Label labelMorphotype;
    @FXML private Label labelRequired;
    @FXML private FlowPane flowPaneImages;
    @FXML private CheckBox checkBoxExampleImagesEnabled;
    @FXML private Button buttonEdit;
    @FXML private Button buttonRemove;

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
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Integer index = cell.getIndex();
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(cell.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(TAXON_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });

            cell.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(TAXON_MIME_TYPE)) {
                    if (cell.getIndex() != ((Integer)db.getContent(TAXON_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });

            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(TAXON_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(TAXON_MIME_TYPE);
                    Taxon draggedPerson = (Taxon) listViewTaxons.getItems().remove(draggedIndex);
                    int dropIndex ; 
                    if (cell.isEmpty()) {
                        dropIndex = listViewTaxons.getItems().size();
                    } else {
                        dropIndex = cell.getIndex();
                    }
                    listViewTaxons.getItems().add(dropIndex, draggedPerson);
                    event.setDropCompleted(true);
                    listViewTaxons.getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });
            
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
        
        setupScrolling();
        
        setupList();
    }    

    private void setupList() {
        for (Taxon taxon : supervisor.project.taxons.values()) {
            taxonList.add(taxon);
        }
        listViewTaxons.getSelectionModel().selectFirst();
    }
    
    private void populateFields(Taxon taxon) {
        labelCode.setText(taxon.getCode());
        labelName.setText(taxon.getName());
        labelDescription.setText(taxon.getDescription());
        if (taxon.getIsClass()) {
            labelMorphotype.setText("Yes");
        }
        else {
            labelMorphotype.setText("No");
        }
        if (Arrays.asList(supervisor.project.requiredTaxons).contains(taxon.getCode())) {
            labelRequired.setText("Yes");
            buttonEdit.setDisable(true);
            buttonRemove.setDisable(true);
        }
        else {
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
                if (added >= 15) break;
            }
        }
    }
        
    @FXML
    void handleEdit(ActionEvent event) {

        try {
            EditLabelViewController controller = AbstractDialogController.create(EditLabelViewController.class);
            controller.setData(listViewTaxons.getSelectionModel().getSelectedItem());
            controller.showAndWait();
            populateFields(listViewTaxons.getSelectionModel().getSelectedItem());
            listViewTaxons.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    void handleRemove(ActionEvent event) {        
        //Get current taxon
        Taxon adapter = listViewTaxons.getSelectionModel().getSelectedItem();
        labelsViewModel.deleteLabel(adapter);
        //Remove adapter
        taxonList.remove(adapter);
        //Select next 
        listViewTaxons.getSelectionModel().selectPrevious();
    }
    
    @FXML
    void handleDone(ActionEvent event) {        
        //Initialise the taxons (is this necessary?)
        ArrayList<Taxon> newTaxons = new ArrayList<>();
        for (Taxon taxon : taxonList) {
            newTaxons.add(taxon);
        }
        labelsViewModel.initialiseLabels(newTaxons);
        stage.close();
    }

    @FXML
    void handleAdd(ActionEvent event) {
        try {
            EditLabelViewController controller = AbstractDialogController.create(EditLabelViewController.class);
            controller.showAndWait();
            Taxon result = controller.getData();
            if (result != null) taxonList.add(result);
//            populateFields(listViewTaxons.getSelectionModel().getSelectedItem());
//            listViewTaxons.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
        listViewTaxons.refresh();
        listViewTaxons.getSelectionModel().selectLast();
    }
    
    @FXML
    void handleAlphabeticalOrdering(ActionEvent event) {
        FXCollections.sort(taxonList, Comparator.comparing(Taxon::getCode));
    } 

    @FXML
    void handleResetOrdering(ActionEvent event) {
        setupList();
    }
    
//    public class TaxonAdapter {
//
//        public Taxon originalTaxon;
//
//        public StringProperty code = new SimpleStringProperty();
//        public StringProperty name = new SimpleStringProperty();
//        public StringProperty group = new SimpleStringProperty();
//        public StringProperty description = new SimpleStringProperty();
//        public BooleanProperty isMorphotype= new SimpleBooleanProperty();
//
//        private int index = 0;
//
//        public TaxonAdapter(Taxon taxon, int i) {
//            set(taxon);
//            index = i;
//        }
//
//        public void set(Taxon taxon) {
//            originalTaxon = taxon;
//            code.set(taxon.getCode());
//            name.set(taxon.getName());
//            group.set(taxon.getGroup());
//            description.set(taxon.getDescription());
//            isMorphotype.set(taxon.getIsClass());
//        }
//
//        public void reset() {
//            set(originalTaxon);
//        }
//
//        public Taxon getResult() {
//            Taxon taxon = new Taxon(code.get(),
//                    name.get(),
//                    description.get(),
//                    group.get(),
//                    isMorphotype.get());
//            return taxon;
//        }
//
//        public int getIndex() {
//            return index;
//        }
//
//        public String getCode() {
//            return code.get();
//        }
//    }
    
    private void setupScrolling() {
        scrolltimeline.setCycleCount(Timeline.INDEFINITE);
        scrolltimeline.getKeyFrames().add(new KeyFrame(Duration.millis(20), (ActionEvent) -> { dragScroll();}));

        listViewTaxons.setOnDragExited((DragEvent event) -> {
            if (event.getY() > 0) {
                scrollVelocity = 1.0 / scrollSpeed;
            }
            else {
                scrollVelocity = -1.0 / scrollSpeed;
            }
            if (!dropped){
                scrolltimeline.play();
            }
        });

        listViewTaxons.setOnDragEntered(event -> {
            scrolltimeline.stop();
            dropped = false;
        });
        listViewTaxons.setOnDragDone(event -> {
            scrolltimeline.stop();
        });           
        listViewTaxons.setOnDragDropped((DragEvent event) ->{      
            Dragboard db = event.getDragboard();
            //((VBox) listViewTaxons.getContent()).getChildren().add(new Label(db.getString())); 
            scrolltimeline.stop();
            event.setDropCompleted(true);
            dropped = true;
        });
//        listViewTaxons.setOnDragOver((DragEvent event) ->{
//            event.acceptTransferModes(TransferMode.MOVE);                            
//        });
        listViewTaxons.setOnScroll(event -> {
            scrolltimeline.stop();   
        });
    }
    private void dragScroll() {
        ScrollBar sb = getVerticalScrollbar();
        if (sb != null) {
            double newValue = sb.getValue() + scrollVelocity;
            newValue = Math.min(newValue, 1.0);
            newValue = Math.max(newValue, 0.0);
            sb.setValue(newValue);
        }
    }

    private ScrollBar getVerticalScrollbar() {
        ScrollBar result = null;
        for (Node n : listViewTaxons.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    result = bar;
                }
            }
        }        
        return result;
    }
}
