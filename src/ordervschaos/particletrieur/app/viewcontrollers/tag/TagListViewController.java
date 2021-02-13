/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.viewcontrollers.tag;

import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.AbstractController;
import ordervschaos.particletrieur.app.AbstractDialogController;
import ordervschaos.particletrieur.app.FxmlLocation;
import ordervschaos.particletrieur.app.models.project.Project;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.viewmodels.particles.LabelsViewModel;
import ordervschaos.particletrieur.app.viewmodels.SelectionViewModel;
import ordervschaos.particletrieur.app.models.project.Tag;
import ordervschaos.particletrieur.app.viewmodels.particles.TagsViewModel;
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
@FxmlLocation("views/tag/TagListView.fxml")
public class TagListViewController extends AbstractController implements Initializable {

    @FXML
    private ListView<Tag> listViewTags;
    @FXML
    private Label labelCode;
    @FXML
    private Label labelName;
    @FXML
    private Label labelDescription;
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
    SelectionViewModel selectionViewModel;
    @Inject
    Supervisor supervisor;
    @Inject
    TagsViewModel tagsViewModel;
    @Inject
    LabelsViewModel labelsViewModel;

    public ObservableList<Tag> tagList = FXCollections.observableArrayList();
    private static DataFormat TAG_MIME_TYPE = new DataFormat("application/x-java-serialized-object-tag");

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
        listViewTags.setCellFactory(param -> {
            ListCell<Tag> cell = new ListCell<Tag>() {
                @Override
                public void updateItem(Tag item, boolean empty) {
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
                    cc.put(TAG_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });

            cell.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(TAG_MIME_TYPE)) {
                    if (cell.getIndex() != ((Integer) db.getContent(TAG_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });

            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(TAG_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(TAG_MIME_TYPE);
                    Tag draggedPerson = (Tag) listViewTags.getItems().remove(draggedIndex);
                    int dropIndex;
                    if (cell.isEmpty()) {
                        dropIndex = listViewTags.getItems().size();
                    } else {
                        dropIndex = cell.getIndex();
                    }
                    listViewTags.getItems().add(dropIndex, draggedPerson);
                    event.setDropCompleted(true);
                    listViewTags.getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });
            return cell;
        });

        listViewTags.getSelectionModel().selectedItemProperty().addListener((obs, oldv, newv) -> {
            if (newv != null) {
                populateFields(newv);
            }
        });
        listViewTags.setItems(tagList);

        checkBoxExampleImagesEnabled.selectedProperty().addListener((obs, oldv, newv) -> {
            populateFields(listViewTags.getSelectionModel().getSelectedItem());
        });

        setupScrolling();
        setupList();
    }


    private void setupList() {
        Project project = supervisor.project;
        tagList.clear();
        for (Tag tag : project.tags.values()) {
            tagList.add(tag);
        }
        listViewTags.getSelectionModel().selectFirst();
    }


    private void populateFields(Tag tag) {
        labelCode.setText(tag.getCode());
        labelName.setText(tag.getName());
        labelDescription.setText(tag.getDescription());
        if (Arrays.asList(supervisor.project.requiredTags).contains(tag.getCode())) {
            labelRequired.setText("Yes");
            buttonEdit.setDisable(true);
            buttonRemove.setDisable(true);
        } else {
            labelRequired.setText("No");
            buttonEdit.setDisable(false);
            buttonRemove.setDisable(false);
        }
        //Add example images
        flowPaneImages.getChildren().clear();

        //TODO this should be a service
        if (checkBoxExampleImagesEnabled.isSelected()) {
            int added = 0;
            int index = 0;
            for (Particle particle : supervisor.project.getParticles()) {
                index++;
                if (particle.classification.get().equals(tag.getCode())) {
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
            EditTagViewController controller = AbstractDialogController.create(EditTagViewController.class);
            controller.setData(listViewTags.getSelectionModel().getSelectedItem());
            controller.showAndWait();
            populateFields(listViewTags.getSelectionModel().getSelectedItem());
            listViewTags.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void handleRemove(ActionEvent event) {
        //Get current taxon
        Tag tag = listViewTags.getSelectionModel().getSelectedItem();
        tagsViewModel.deleteTag(tag);
        //Remove adapter
        tagList.remove(tag);
        //Select next 
        listViewTags.getSelectionModel().selectPrevious();
    }


    @FXML
    void handleDone(ActionEvent event) {
        //Do the rearrangement
        ArrayList<Tag> newTags = new ArrayList<>();
        for (Tag tag : tagList) {
            newTags.add(tag);
        }
        tagsViewModel.initialiseTags(newTags);
        stage.close();
    }


    @FXML
    void handleAdd(ActionEvent event) {
        try {
            EditTagViewController controller = AbstractDialogController.create(EditTagViewController.class);
            controller.showAndWait();
            Tag result = controller.getData();
            if (result != null) tagList.add(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        listViewTags.refresh();
        listViewTags.getSelectionModel().selectLast();
    }


    @FXML
    void handleAlphabeticalOrdering(ActionEvent event) {
        FXCollections.sort(tagList, Comparator.comparing(Tag::getCode));
    }


    @FXML
    void handleResetOrdering(ActionEvent event) {
        setupList();
    }

    private void setupScrolling() {
        scrolltimeline.setCycleCount(Timeline.INDEFINITE);
        scrolltimeline.getKeyFrames().add(new KeyFrame(Duration.millis(20), (ActionEvent) -> {
            dragScroll();
        }));

        listViewTags.setOnDragExited((DragEvent event) -> {
            if (event.getY() > 0) {
                scrollVelocity = 1.0 / scrollSpeed;
            } else {
                scrollVelocity = -1.0 / scrollSpeed;
            }
            if (!dropped) {
                scrolltimeline.play();
            }
        });

        listViewTags.setOnDragEntered(event -> {
            scrolltimeline.stop();
            dropped = false;
        });
        listViewTags.setOnDragDone(event -> {
            scrolltimeline.stop();
        });
        listViewTags.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            //((VBox) listViewTaxons.getContent()).getChildren().add(new Label(db.getString())); 
            scrolltimeline.stop();
            event.setDropCompleted(true);
            dropped = true;
        });
//        listViewTaxons.setOnDragOver((DragEvent event) ->{
//            event.acceptTransferModes(TransferMode.MOVE);                            
//        });
        listViewTags.setOnScroll(event -> {
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
        for (Node n : listViewTags.lookupAll(".scroll-bar")) {
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
