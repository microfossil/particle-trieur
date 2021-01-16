package ordervschaos.particletrieur.app.viewcontrollers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.helpers.AutoCancellingServiceRunner;
import ordervschaos.particletrieur.app.models.network.features.Similarity;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.project.Taxon;
import ordervschaos.particletrieur.app.viewmodels.LabelsViewModel;
import ordervschaos.particletrieur.app.viewmodels.ParticlesViewModel;
import ordervschaos.particletrieur.app.services.ParticleSimilarityService;
import ordervschaos.particletrieur.app.AbstractController;
import ordervschaos.particletrieur.app.viewmodels.SelectionViewModel;
import ordervschaos.particletrieur.app.controls.ForamImageControl;
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

    @FXML
    ToggleButton toggleButtonMultiple;
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

        selectionViewModel.currentParticleImageProperty().addListener((observable, oldValue, newValue) -> updateSimilarUI());

        selectedItems.addListener((ListChangeListener<Similarity>) c -> {
            c.next();
            if (c.getAddedSize() > 0) {
                Similarity similarity = c.getAddedSubList().get(0);
                Particle particle = supervisor.project.particles.get(similarity.index);
                menuButtonCurrentLabels.setText(particle.classification.get());
            }
            System.out.println(String.format("Current cells: %d", currentCells.size()));
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

        selectionViewModel.knnPredictionViewModel.kNNPredictedClassificationProperty().addListener((observable, oldValue, newValue) -> {
            menuButtonCurrentLabels.setText(newValue.getBestCode());
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

        public ForamImageControl im = new ForamImageControl(supervisor, labelsViewModel);

        public int index = 0;

        //Loading task
        ObjectProperty<Task<Image>> loadingTask = new SimpleObjectProperty<>();

        public SimilarParticleCell() {
            super();
            im.setSize(100);
            currentCells.add(new WeakReference<>(this));
            setOnMouseClicked(event ->
            {
                if (getItem().isOriginal) return;
                if (event.isMetaDown() || event.isControlDown() || toggleButtonMultiple.isSelected()) {
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

                    System.out.println("-----");
                    System.out.println(selectedIndex);
                    System.out.println(currentIdx);
                    System.out.println(shiftSelectedIndex);

                    ArrayList<Similarity> toRemove = new ArrayList<>();
                    ArrayList<Similarity> toAdd= new ArrayList<>();

                    for (int i = startIdxOld; i <= endIdxOld; i++) {
                        toRemove.add(images.get(i));
//                        if (!currentItems.contains(images.get(i))) {
//                            selectedItems.remove(images.get(i));
//                            System.out.print(i);
//                            System.out.print(" ");
//                        }
                    }

                    for (int i = startIdx; i <= endIdx; i++) {
                        toAdd.add(images.get(i));
//                        currentItems.add(images.get(i));
//                        selectedItems.add(images.get(i));
//                        System.out.print(i);
//                        System.out.print(" ");
                    }
                    selectedItems.removeAll(toRemove);
                    selectedItems.addAll(toAdd);

//                    for (int i = startIdx; i <= endIdx; i++) {
//                        currentItems.add(images.get(i));
//                        selectedItems.add(images.get(i));
//                        System.out.print(i);
//                        System.out.print(" ");
//                    }
//                    System.out.println();
//                    for (int i = startIdxOld; i <= endIdxOld; i++) {
//                        if (!currentItems.contains(images.get(i))) {
//                            selectedItems.remove(images.get(i));
//                            System.out.print(i);
//                            System.out.print(" ");
//                        }
//                    }
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
                setGraphic(im);
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
                    im.setData(thisParticle, thatParticle, item.index + 1, item.value, true ,task.getValue());
                    im.selected.set(selectedItems.contains(item));
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
            im.selected.set(selectedItems.contains(this.getItem()));
            System.out.print(getIndex());
            System.out.print(" ");
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


    private Service<List<ForamImageControl>> populateImagesService(List<Similarity> similarities) {
        return new Service<List<ForamImageControl>>() {
            @Override
            protected Task<List<ForamImageControl>> createTask() {
                return new Task<List<ForamImageControl>>() {
                    @Override
                    protected List<ForamImageControl> call() throws Exception {
                        ArrayList<ForamImageControl> foramImageControls = new ArrayList<>();

                        for (Similarity sim : similarities) {
                            if (this.isCancelled()) {
                                return null;
                            }
                            ForamImageControl im;
                            if (supervisor.project.particles.get(sim.index) == selectionViewModel.getCurrentParticle()) {
                                continue;
                            }
                            else {
                                im = new ForamImageControl(supervisor, labelsViewModel);
                                im.setSize(100);
                                im.setData(supervisor.project.particles.get(sim.index), selectionViewModel.getCurrentParticle(), sim.index + 1, sim.value, true);
                            }
                            foramImageControls.add(im);
                        }
                        ForamImageControl im = new ForamImageControl(supervisor, labelsViewModel);
                        im.setSize(100);
                        im.setData(selectionViewModel.getCurrentParticle(), null, selectionViewModel.getCurrentParticleIndex() + 1, "", true);
                        foramImageControls.add(0, im);

                        return foramImageControls;
                    }
                };
            }
        };
    }
}
