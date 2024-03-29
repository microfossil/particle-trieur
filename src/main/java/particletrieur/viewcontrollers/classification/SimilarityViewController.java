package particletrieur.viewcontrollers.classification;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import org.controlsfx.control.PopOver;
import particletrieur.App;
import particletrieur.controls.ParticleGridCellSimilarityControl;
import particletrieur.helpers.AutoCancellingServiceRunner;
import particletrieur.models.network.classification.Similarity;
import particletrieur.models.Supervisor;
import particletrieur.models.project.Particle;
import particletrieur.models.project.Taxon;
import particletrieur.viewcontrollers.particle.ImageDescriptionPopover;
import particletrieur.viewmodels.network.KNNPredictionViewModel;
import particletrieur.viewmodels.particles.LabelsViewModel;
import particletrieur.viewmodels.particles.ParticlesViewModel;
import particletrieur.services.ParticleSimilarityService;
import particletrieur.AbstractController;
import particletrieur.viewmodels.SelectionViewModel;
import com.google.inject.Inject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.*;

public class SimilarityViewController extends AbstractController implements Initializable {

//    @FXML
//    ToggleButton toggleButtonMultiple;
    @FXML
    ComboBox<ParticleSimilarityService.SortBy> comboBoxSimilar;
    @FXML
    GridView<Similarity> gridView;
    @FXML
    SplitMenuButton menuButtonLabels;
    @FXML
    SplitMenuButton menuButtonCurrentLabels;

    private AutoCancellingServiceRunner<List<Similarity>> similarityServiceRunner;

    @Inject
    SelectionViewModel selectionViewModel;
    @Inject
    Supervisor supervisor;
    @Inject
    ParticlesViewModel particlesViewModel;
    @Inject
    LabelsViewModel labelsViewModel;
    @Inject
    KNNPredictionViewModel knnPredictionViewModel;

    ObservableList<Similarity> images = FXCollections.observableArrayList();
    private ObservableList<Similarity> selectedItems = FXCollections.observableArrayList();
    private int selectedIndex = 0;
    private int shiftSelectedIndex = 0;

    public BooleanProperty isEnabledProperty = new SimpleBooleanProperty(false);

    private ArrayList<WeakReference<SimilarParticleCell>> currentCells = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        similarityServiceRunner = new AutoCancellingServiceRunner<>("similarity");

        comboBoxSimilar.getItems().setAll(ParticleSimilarityService.SortBy.values());
        comboBoxSimilar.getSelectionModel().selectLast();

        comboBoxSimilar.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateSimilarUI();
        });

        isEnabledProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) updateSimilarUI();
        });

        selectionViewModel.currentParticleProperty().addListener((observable, oldValue, newValue) -> {
            updateSimilarUI();
        });

        selectedItems.addListener((ListChangeListener<Similarity>) c -> {
            c.next();
            if (c.getAddedSize() > 0) {
                Similarity similarity = c.getAddedSubList().get(0);
                Particle particle = supervisor.project.particles.get(similarity.index);
                menuButtonCurrentLabels.setText(particle.classification.get());
            }
//            System.out.println(String.format("Current cells: %d", currentCells.size()));
            Iterator<WeakReference<SimilarParticleCell>> itr = currentCells.iterator();
            while (itr.hasNext()) {
                WeakReference<SimilarParticleCell> cell = itr.next();
                try {
                    cell.get().updateSelection();
                }
                catch (NullPointerException ex) {
//                    System.out.println("disposed");
                    itr.remove();
                }
            }
        });

        gridView.setCellFactory(param -> new SimilarParticleCell());
        gridView.setItems(images);
        supervisor.project.taxonsUpdatedEvent.addListener(listener -> {
            onTaxonsUpdated();
        });
        onTaxonsUpdated();

        knnPredictionViewModel.kNNPredictedClassificationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) menuButtonCurrentLabels.setText(newValue.getBestCode());
        });
    }

    private void onTaxonsUpdated() {
        menuButtonLabels.getItems().clear();
        menuButtonCurrentLabels.getItems().clear();
        for (Taxon taxon : supervisor.project.getTaxons().values()) {
            if (taxon.getIsClass()) {
                MenuItem item = new MenuItem(taxon.getCode());
                final String code = taxon.getCode();
                item.setOnAction(event -> {
                    menuButtonLabels.setText(code);
                });
                menuButtonLabels.getItems().add(item);
                MenuItem item2 = new MenuItem(taxon.getCode());
                final String code2 = taxon.getCode();
                item2.setOnAction(event -> {
                    menuButtonCurrentLabels.setText(code2);
                });
                menuButtonCurrentLabels.getItems().add(item2);
            }
        }
    }

    @FXML
    private void handleSetLabel(ActionEvent event) {
        String code = menuButtonLabels.getText();
        ArrayList<Particle> particles = new ArrayList<>();
        for (Similarity similarity : selectedItems) {
            particles.add(supervisor.project.particles.get(similarity.index));
        }
        if (particles.size() > 0) {
            labelsViewModel.setLabel(particles, code, 1.0, true);
        }
    }

    @FXML
    private void handleSetCurrentLabel(ActionEvent event) {
        String code = menuButtonCurrentLabels.getText();
        labelsViewModel.setLabel(selectionViewModel.getCurrentParticle(), code, 1.0, true);
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        ArrayList<Particle> particles = new ArrayList<>();
        for (Similarity similarity : selectedItems) {
            particles.add(supervisor.project.particles.get(similarity.index));
        }
        particlesViewModel.removeParticles(particles);
        updateSimilarUI();
    }

    public class SimilarParticleCell extends GridCell<Similarity> {

        public ParticleGridCellSimilarityControl im = new ParticleGridCellSimilarityControl();
        public int index = 0;

        //Loading task
        ObjectProperty<Task<Image>> loadingTask = new SimpleObjectProperty<>();

        public SimilarParticleCell() {
            super();
            updateSelection();
            currentCells.add(new WeakReference<>(this));
            setOnMouseClicked(event ->
            {
                if (event.getButton() == MouseButton.SECONDARY) {
                    ImageDescriptionPopover popover = new ImageDescriptionPopover(supervisor.project.particles.get(getItem().index), PopOver.ArrowLocation.LEFT_CENTER);
                    popover.show(this);
                    return;
                }
                if (getItem().isOriginal) return;
                if (event.isMetaDown() || event.isControlDown()) {
                    if (selectedItems.contains(getItem())) {
                        selectedItems.remove(getItem());
                    } else {
                        selectedItems.add(getItem());
                    }
                    selectedItems.add(getItem());
                    selectedIndex = super.getIndex();
                    shiftSelectedIndex = selectedIndex;
                }
                else if (event.isShiftDown()) {
                    int currentIdx = getIndex();
                    int startIdx = Math.min(currentIdx, selectedIndex);
                    int endIdx = Math.max(currentIdx, selectedIndex);
                    int startIdxOld = Math.min(shiftSelectedIndex, currentIdx);
                    int endIdxOld = Math.max(shiftSelectedIndex, currentIdx);

                    ArrayList<Similarity> toRemove = new ArrayList<>();
                    ArrayList<Similarity> toAdd= new ArrayList<>();

                    for (int i = startIdxOld; i <= endIdxOld; i++) {
                        toRemove.add(images.get(i));
                    }

                    for (int i = startIdx; i <= endIdx; i++) {
                        toAdd.add(images.get(i));
                    }
                    selectedItems.removeAll(toRemove);
                    selectedItems.addAll(toAdd);
                    shiftSelectedIndex = currentIdx;
                }
                else {
                    selectedIndex = super.getIndex();
                    shiftSelectedIndex = selectedIndex;
                    selectedItems.clear();
                    selectedItems.add(getItem());
                }
            });
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
        }

        @Override
        public void updateIndex(int i) {
            super.updateIndex(i);
        }

        @Override
        protected void updateItem(Similarity item, boolean empty) {
            //Stop already running image fetch tast
            if (loadingTask.get() != null &&
                    loadingTask.get().getState() != Worker.State.SUCCEEDED &&
                    loadingTask.get().getState() != Worker.State.FAILED) {

                loadingTask.get().cancel();
            }
            loadingTask.set(null);

            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            }
            else {
                setGraphic(null);
                Particle thisParticle = supervisor.project.particles.get(item.index);
                Particle thatParticle = selectionViewModel.getCurrentParticle();
                Task<Image> task = new Task<Image>() {
                    @Override
                    public Image call() throws Exception {
                        Image image = null;
                        try {
                            image = thisParticle.getImage();
                        }
                        catch (Exception ex) {
                            image = null;
                        }
                        return image;
                    }
                };
                loadingTask.set(task);
                task.setOnSucceeded(event -> {
                    im.setData(thisParticle, thatParticle, item.index + 1, item.value, task.getValue());
                    setGraphic(im);
//                    im.selected.set(selectedItems.contains(item));
                });
                App.getExecutorService().submit(task);
            }
        }

        @Override
        protected void finalize() throws Throwable {
            currentCells.remove(this);
            super.finalize();
        }

        public void updateSelection() {
//            if (this.getItem() == null) {
//                this.setStyle("-fx-border-color: #eeeeee; -fx-background-color: transparent; -fx-text-fill: -fx-text-base-color;");
//            }
//            else if (this.getItem().index == selectionViewModel.getCurrentParticleIndex()) {
//                this.setStyle("-fx-border-color: #eeeeee; -fx-background-color: #333333; -fx-text-fill: white;");
//            }
            if (selectedItems.contains(this.getItem())) {
                this.setStyle("-fx-border-color: derive(-fx-accent, -20%); -fx-background-color: -fx-accent; -fx-text-fill: white;");
                im.getStyleClass().clear();
                im.getStyleClass().add("selected-content");
            }
            else {
                this.setStyle("-fx-border-color: #dddddd; -fx-background-color: transparent; -fx-text-fill: -fx-text-base-color;");
                im.getStyleClass().clear();
            }
        }
    }

    private void updateSimilarUI() {
        if (!isEnabledProperty.get()) return;
        if (selectionViewModel.getCurrentParticle() != null) {
            menuButtonLabels.setText(selectionViewModel.getCurrentParticle().classification.get());
        }
        else {
            menuButtonCurrentLabels.setText("N/A");
            menuButtonLabels.setText("N/A");
            images.clear();
            return;
        }

        Service<List<Similarity>> service = ParticleSimilarityService.findMostSimilarService(
                selectionViewModel.getCurrentParticle(),
                supervisor.project.getParticles(),
                comboBoxSimilar.getSelectionModel().getSelectedItem());
        service.setOnCancelled(event -> {

        });
        service.setOnSucceeded(event -> {
            List<Similarity> similar = service.getValue();
            images.clear();
            images.addAll(similar);
        });
        service.setOnFailed(event -> {

        });
        similarityServiceRunner.run(service);
    }


//    private Service<List<ForamImageControl>> populateImagesService(List<Similarity> similarities) {
//        return new Service<List<ForamImageControl>>() {
//            @Override
//            protected Task<List<ForamImageControl>> createTask() {
//                return new Task<List<ForamImageControl>>() {
//                    @Override
//                    protected List<ForamImageControl> call() throws Exception {
//                        ArrayList<ForamImageControl> foramImageControls = new ArrayList<>();
//
//                        for (Similarity sim : similarities) {
//                            if (this.isCancelled()) {
//                                return null;
//                            }
//                            ForamImageControl im;
//                            if (supervisor.project.particles.get(sim.index) == selectionViewModel.getCurrentParticle()) {
//                                continue;
//                            }
//                            else {
//                                im = new ForamImageControl(supervisor, labelsViewModel);
//                                im.setSize(100);
//                                im.setData(supervisor.project.particles.get(sim.index), selectionViewModel.getCurrentParticle(), sim.index + 1, sim.value, true);
//                            }
//                            foramImageControls.add(im);
//                        }
//                        ForamImageControl im = new ForamImageControl(supervisor, labelsViewModel);
//                        im.setSize(100);
//                        im.setData(selectionViewModel.getCurrentParticle(), null, selectionViewModel.getCurrentParticleIndex() + 1, "", true);
//                        foramImageControls.add(0, im);
//
//                        return foramImageControls;
//                    }
//                };
//            }
//        };
//    }
}
