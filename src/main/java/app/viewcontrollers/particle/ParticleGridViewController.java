/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.app.viewcontrollers.particle;

import com.google.inject.Inject;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import main.java.app.App;
import main.java.app.controls.ParticleGridCellControl;
import main.java.app.models.Supervisor;
import main.java.app.models.project.Particle;
import main.java.app.viewmodels.SelectionViewModel;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.PopOver;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ParticleGridViewController implements Initializable {

    @FXML
    GridView<Particle> gridViewParticles;

    @Inject
    private Supervisor supervisor;
    @Inject
    private SelectionViewModel selectionViewModel;

    private ArrayList<WeakReference<ParticleCell>> currentCells = new ArrayList<>();
    private ObservableList<Particle> selectedItems = FXCollections.observableArrayList();
    private int selectedIndex = 0;
    private int shiftSelectedIndex = 0;

    private double cellWidth = 160;
    private double cellRatio = 1.25;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupGridView();
    }

    private void setupGridView() {
        gridViewParticles.setCellWidth(cellWidth);
        gridViewParticles.setCellHeight(cellRatio * cellWidth);

        selectionViewModel.getCurrentParticles().addListener((ListChangeListener<Particle>) c -> {
            c.next();
            Iterator<WeakReference<ParticleCell>> itr = currentCells.iterator();
            while (itr.hasNext()) {
                WeakReference<ParticleCell> cell = itr.next();
                try {
                    cell.get().updateSelection();
                }
                catch (NullPointerException ex) {
                    itr.remove();
                }
            }
        });
        gridViewParticles.setCellFactory(param -> new ParticleCell());
        gridViewParticles.setItems(selectionViewModel.sortedList);

        selectionViewModel.decreaseSizeRequested.addListener(v -> {
            if (selectionViewModel.selectedTabIndex != 1) return;
            if (cellWidth >= 96 + 32) {
                cellWidth -= 32;
            }
            gridViewParticles.setCellWidth(cellWidth);
            gridViewParticles.setCellHeight(cellRatio * cellWidth);
        });

        selectionViewModel.increaseSizeRequested.addListener(v -> {
            if (selectionViewModel.selectedTabIndex != 1) return;
            if (cellWidth <= 512 + 32) {
                cellWidth += 32;
            }
            gridViewParticles.setCellWidth(cellWidth);
            gridViewParticles.setCellHeight(cellRatio * cellWidth);
        });
    }

    public class ParticleCell extends GridCell<Particle> {
        public ParticleGridCellControl im = new ParticleGridCellControl();
        ObjectProperty<Task<Image>> loadingTask = new SimpleObjectProperty<>();

        public ParticleCell() {
            super();
//            this.setStyle("-fx-border-color: #eeeeee; -fx-background-color: transparent; -fx-text-fill: -fx-text-base-color;");
//            im.setSize(currentSize);
            currentCells.add(new WeakReference<>(this));
            setOnMouseClicked(event ->
            {
                if (event.getButton() == MouseButton.SECONDARY) {
                    ImageDescriptionPopover popover = new ImageDescriptionPopover(getItem(), PopOver.ArrowLocation.LEFT_CENTER);
                    popover.show(this);
                    return;
                }
                if (event.isMetaDown() || event.isControlDown()) {
//                    if (selectedItems.contains(getItem())) {
//                        selectionViewModel.getCurrentParticles().remove(getItem());
////                        selectedItems.remove(getItem());
//                    } else {
//                        selectionViewModel.getCurrentParticles().add(getItem());
////                        selectedItems.add(getItem());
//                    }
//                    selectionViewModel.getCurrentParticles().add(getItem());
                    selectedIndex = super.getIndex();
                    shiftSelectedIndex = selectedIndex;
                    selectionViewModel.controlSelectIndex.broadcast(selectedIndex);
                }
                else if (event.isShiftDown()) {
                    int currentIdx = getIndex();
                    int startIdx = Math.min(currentIdx, selectedIndex);
                    int endIdx = Math.max(currentIdx, selectedIndex);
//                    int startIdxOld = Math.min(shiftSelectedIndex, currentIdx);
//                    int endIdxOld = Math.max(shiftSelectedIndex, currentIdx);
//
//                    ArrayList<Particle> toRemove = new ArrayList<>();
//                    ArrayList<Particle> toAdd= new ArrayList<>();
//
//                    for (int i = startIdxOld; i <= endIdxOld; i++) {
//                        toRemove.add(selectionViewModel.sortedList.get(i));
//                    }
//
//                    for (int i = startIdx; i <= endIdx; i++) {
//                        toAdd.add(selectionViewModel.sortedList.get(i));
//                    }
//                    selectionViewModel.getCurrentParticles().removeAll(toRemove);
//                    selectionViewModel.getCurrentParticles().addAll(toAdd);
//                    shiftSelectedIndex = currentIdx;
                    selectionViewModel.shiftSelectIndices.broadcast(new int[] {startIdx, endIdx});
                }
                else {
                    selectedIndex = super.getIndex();
//                    shiftSelectedIndex = selectedIndex;
                    selectionViewModel.selectIndex.broadcast(selectedIndex);
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
        protected void updateItem(Particle item, boolean empty) {
            //Stop already running image fetch task
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
                updateSelection();
                setGraphic(im);
                Task<Image> task = new Task<Image>() {
                    @Override
                    public Image call() throws Exception {
                        Image image = null;
                        try {
                            image = item.getImage();
                        }
                        catch (Exception ex) {
                            image = null;
                        }
                        return image;
                    }
                };
                loadingTask.set(task);
                task.setOnSucceeded(event -> {
                    im.setData(item, selectionViewModel.getParticleIndex(item));
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
            if (selectionViewModel.getCurrentParticles().contains(this.getItem())) {
                this.setStyle("-fx-border-color: #eeeeee; -fx-background-color: -fx-accent; -fx-text-fill: white;");
                im.getStyleClass().clear();
                im.getStyleClass().add("selected-content");
            }
            else {
                this.setStyle("-fx-border-color: #eeeeee; -fx-background-color: transparent; -fx-text-fill: -fx-text-base-color;");
                im.getStyleClass().clear();
            }
        }
    }
}
