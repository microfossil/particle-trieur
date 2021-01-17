/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.viewcontrollers;

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
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.controls.ParticleGridCellControl;
import ordervschaos.particletrieur.app.controls.ParticleImageControl;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.network.features.Similarity;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.viewmodels.MainViewModel;
import ordervschaos.particletrieur.app.viewmodels.SelectionViewModel;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

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

    //TODO make a preference
    private int imageWidth = 64;

    private ArrayList<WeakReference<ParticleCell>> currentCells = new ArrayList<>();
    private ObservableList<Particle> selectedItems = FXCollections.observableArrayList();
    private int selectedIndex = 0;
    private int shiftSelectedIndex = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
//        setupUI();
//        setupBindings();
        setupGridView();

        selectionViewModel.nextImageRequested.addListener(val -> {
//            selectNext();
        });
        selectionViewModel.previousImageRequested.addListener(val -> {
//            selectPrevious();
        });
    }

    private void setupUI() {

    }

    private void setupGridView() {
        selectionViewModel.getCurrentParticles().addListener((ListChangeListener<Particle>) c -> {
            c.next();
//            if (c.getAddedSize() > 0) {
//                Particle  = c.getAddedSubList().get(0);
//                Particle particle = supervisor.project.particles.get(similarity.index);
//                menuButtonCurrentLabels.setText(particle.classification.get());
//            }
            System.out.printf("Current cells: %d%n", currentCells.size());
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
    }

    public class ParticleCell extends GridCell<Particle> {
        public ParticleGridCellControl im = new ParticleGridCellControl(supervisor);
        ObjectProperty<Task<Image>> loadingTask = new SimpleObjectProperty<>();

        public ParticleCell() {
            super();
            currentCells.add(new WeakReference<>(this));
            setOnMouseClicked(event ->
            {
                if (event.isMetaDown() || event.isControlDown()) {
//                    if (selectedItems.contains(getItem())) {
//                        selectionViewModel.getCurrentParticles().remove(getItem());
////                        selectedItems.remove(getItem());
//                    } else {
//                        selectionViewModel.getCurrentParticles().add(getItem());
////                        selectedItems.add(getItem());
//                    }
                    selectionViewModel.getCurrentParticles().add(getItem());
                    selectedIndex = super.getIndex();
                    shiftSelectedIndex = selectedIndex;
                }
                else if (event.isShiftDown()) {
                    int currentIdx = getIndex();
                    int startIdx = Math.min(currentIdx, selectedIndex);
                    int endIdx = Math.max(currentIdx, selectedIndex);
                    int startIdxOld = Math.min(shiftSelectedIndex, currentIdx);
                    int endIdxOld = Math.max(shiftSelectedIndex, currentIdx);

                    ArrayList<Particle> toRemove = new ArrayList<>();
                    ArrayList<Particle> toAdd= new ArrayList<>();

                    for (int i = startIdxOld; i <= endIdxOld; i++) {
                        toRemove.add(selectionViewModel.sortedList.get(i));
                    }

                    for (int i = startIdx; i <= endIdx; i++) {
                        toAdd.add(selectionViewModel.sortedList.get(i));
                    }
                    selectionViewModel.getCurrentParticles().removeAll(toRemove);
                    selectionViewModel.getCurrentParticles().addAll(toAdd);
                    shiftSelectedIndex = currentIdx;
                }
                else {
                    selectedIndex = super.getIndex();
                    shiftSelectedIndex = selectedIndex;
//                    selectionViewModel.setCurrentParticle(getItem());
                    selectionViewModel.getCurrentParticles().clear();
                    selectionViewModel.getCurrentParticles().add(getItem());
                    selectionViewModel.setCurrentParticle(getItem());
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
            // Update using task to get image asynchonously
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            }
            else {
                updateSelection();
//                setText(item.getFilename());
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
                this.setStyle("-fx-background-color: -fx-accent;");
            }
            else {
                this.setStyle("-fx-background-color: transparent;");
            }
//            im.selected.set(selectionViewModel.getCurrentParticles().contains(this.getItem()));
//            System.out.print(getIndex());
//            System.out.print(" ");
        }
    }

//
//    public void setupUI() {
//
//        //Init the filter text field to have a clear button
//        try {
//            Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
//            m.setAccessible(true);
//            m.invoke(null, customTextFieldFilter, customTextFieldFilter.rightProperty());
//        } catch (Exception ex) {
//            BasicDialogs.ShowException("There was a problem setting up the filter text field", ex);
//        }
//
//        //Enable multiple selection
//        tableViewForams.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//
//        //Index
//        TableColumn<Particle, Number> colIndex = new TableColumn<>();
//        colIndex.setCellValueFactory(column -> new ReadOnlyObjectWrapper<Number>(supervisor.project.particles.indexOf(column.getValue()) + 1));
//        colIndex.setText("#");
//        tableViewForams.getColumns().add(colIndex);
//        colIndex.setPrefWidth(40);
//
//        //Image
//        colImage = new TableColumn<>("");
//        colImage.setText("Image");
//        colImage.setPrefWidth(imageWidth);
//        colImage.setCellFactory(param -> {
//            //Set up the ImageView
//            final ImageView imageview = new ImageView();
//            imageview.fitWidthProperty().bind(Bindings.subtract(colImage.widthProperty(), 7));
//            imageview.setPreserveRatio(true);
//
//            //Loading task
//            ObjectProperty<Task<Image>> loadingTask = new SimpleObjectProperty<>();
//
//            //Set up the Table
//            TableCell<Particle, File> cell = new TableCell<Particle, File>() {
//                @Override
//                public void updateItem(File item, boolean empty) {
//                    //Stop already running image fetch task
//                    if (loadingTask.get() != null &&
//                            loadingTask.get().getState() != Worker.State.SUCCEEDED &&
//                            loadingTask.get().getState() != Worker.State.FAILED) {
//
//                        loadingTask.get().cancel();
//                    }
//                    loadingTask.set(null);
//                    //Load image if not null
//                    if (empty || item == null) {
//                        imageview.setVisible(false);
//                    } else {
//                        imageview.setVisible(true);
//                        imageview.setOnMouseClicked(event -> {
//                            Particle particle = (Particle) this.getTableRow().getItem();
//                            ImageDescriptionPopover popover = new ImageDescriptionPopover(particle, PopOver.ArrowLocation.LEFT_CENTER);
//                            popover.show(imageview);
//                        });
//                        Task<Image> task = new Task<Image>() {
//                            @Override
//                            public Image call() throws Exception {
//                                if (item.exists()) {
//                                    return SwingFXUtils.toFXImage(ImageIO.read(item), null);
//                                }
//                                else {
//                                    return new Image(App.class.getResourceAsStream("resources/missing-image-128.png"), 64, 64, true, true);
//                                }
//                            }
//                        };
//                        loadingTask.set(task);
//                        task.setOnSucceeded(event -> {
//
//                            imageview.setImage(task.getValue());
//                        });
//                        App.getExecutorService().submit(task);
//                    }
//                }
//            };
//            // Attach the imageview to the cell
//            cell.setGraphic(imageview);
//            return cell;
//        });
//        colImage.setCellValueFactory(cellData -> cellData.getValue().fileProperty());
//        tableViewForams.getColumns().add(colImage);
//
//        //Sample / index
//        TableColumn<Particle, String> coreIDCol = new TableColumn<>("Sample");
//        coreIDCol.setCellValueFactory(cellData -> Bindings.concat(
//                cellData.getValue().sampleIDProperty(),
//                "#",
//                Bindings.createStringBinding(() -> Double.toString(cellData.getValue().getIndex1()), cellData.getValue().index1Property()),
//                "#",
//                Bindings.createStringBinding(() -> Double.toString(cellData.getValue().getIndex2()), cellData.getValue().index2Property())
//        ));
//        coreIDCol.setCellFactory(param -> {
//            //VBox
//            final VBox vBox = new VBox();
//            final Label labelCore = new Label();
//            final Label labelNum1 = new Label();
//            final Label labelNum2 = new Label();
//            labelCore.setStyle("-fx-font-weight: bold");
//            vBox.getChildren().addAll(labelCore, labelNum1, labelNum2);
//            TableCell<Particle, String> cell = new TableCell<Particle, String>() {
//                @Override
//                public void updateItem(String item, boolean empty) {
//                    if (empty || item == null) {
//                        vBox.setVisible(false);
//                    }
//                    else {
//                        vBox.setVisible(true);
//                        String[] part = item.split("#",-1);
//                        if (part.length == 3) {
//                            labelCore.setText(part[0].equals("") ? "unknown" : part[0]);
//                            labelNum1.setText("[1] " + part[1]);
//                            labelNum2.setText("[2] " + part[2]);
//                        }
//                    }
//                }
//            };
//            cell.setGraphic(vBox);
//            return cell;
//        });
//        coreIDCol.setPrefWidth(90);
//        tableViewForams.getColumns().add(coreIDCol);
//
//        //Class / tags
//        TableColumn<Particle, String> classCol = new TableColumn<>("Label/Tag");
//        classCol.setCellValueFactory(cellData -> Bindings.concat(
//                cellData.getValue().classification,
//                "#",
//                cellData.getValue().tagUIProperty,
//                "#",
//                cellData.getValue().validatorProperty(),
//                "#"
//        ));
//        classCol.setCellFactory(param -> {
//            //VBox
//            final VBox vBox = new VBox();
//            final Label labelClass = new Label();
//            final Label labelTags = new Label();
//            labelClass.setStyle("-fx-font-weight: bold");
//            final SymbolLabel symbolLabel = new SymbolLabel("feathercheckcircle", 16);
//            symbolLabel.setSymbolColor("green");
//            vBox.getChildren().addAll(labelClass, labelTags, symbolLabel);
//
//            TableCell<Particle, String> cell = new TableCell<Particle, String>() {
//                @Override
//                public void updateItem(String item, boolean empty) {
//                    if (empty || item == null) {
//                        vBox.setVisible(false);
//                    }
//                    else {
//                        vBox.setVisible(true);
//                        String[] part = item.split("#",-1);
//                        if (part.length > 0) {
//                            labelClass.setText(part[0]);
//                        }
//                        if (part.length > 1) {
//                            labelTags.setText(part[1]);
//                        }
//                        if (part.length > 2) {
//                            if (part[2].equals("")) {
//                                symbolLabel.setVisible(false);
//                            }
//                            else {
//                                symbolLabel.setVisible(true);
//                            }
//                        }
//                    }
//                }
//            };
//            cell.setGraphic(vBox);
//            return cell;
//        });
//        classCol.setPrefWidth(90);
//        tableViewForams.getColumns().add(classCol);
//
//        //Class / tags
//        TableColumn<Particle, String> valCol = new TableColumn<>("Annotator");
//        valCol.setCellValueFactory(cellData -> Bindings.concat(
//                cellData.getValue().classifierIdProperty,
//                "#",
//                cellData.getValue().validatorProperty(),
//                "#"
//        ));
//        valCol.setCellFactory(param -> {
//            //VBox
//            final VBox vBox = new VBox();
//            final Label labelAnnotator = new Label();
//            final Label labelValidator = new Label();
//            labelAnnotator.setStyle("-fx-font-weight: bold");
//            vBox.getChildren().addAll(labelAnnotator, labelValidator);
//
//            TableCell<Particle, String> cell = new TableCell<Particle, String>() {
//                @Override
//                public void updateItem(String item, boolean empty) {
//                    if (empty || item == null) {
//                        vBox.setVisible(false);
//                    }
//                    else {
//                        vBox.setVisible(true);
//                        String[] part = item.split("#",-1);
//                        if (part.length > 0) {
//                            labelAnnotator.setText(part[0]);
//                        }
//                        if (part.length > 1) {
//                            labelValidator.setText(part[1]);
//                        }
//                    }
//                }
//            };
//            cell.setGraphic(vBox);
//            return cell;
//        });
//        valCol.setPrefWidth(90);
//        tableViewForams.getColumns().add(valCol);
//
//
//        //Filename
//        TableColumn<Particle, String> columnFilename = new TableColumn<>("Filename");
//        columnFilename.setCellValueFactory(column -> column.getValue().shortFilenameProperty());
//        columnFilename.setCellFactory(createTextWrappedCell());
//        columnFilename.setPrefWidth(120);
//        tableViewForams.getColumns().add(columnFilename);
//
//        //Folder
//        TableColumn<Particle, String> columnFolder = new TableColumn<>("Folder");
//        columnFolder.setCellValueFactory(column -> column.getValue().folderProperty());
//        columnFolder.setCellFactory(createTextWrappedCell());
//        columnFolder.setPrefWidth(160);
//        tableViewForams.getColumns().add(columnFolder);
//
//        //Sample
//        TableColumn<Particle, String> columnSample = new TableColumn<>("Sample");
//        columnSample.setCellValueFactory(column -> column.getValue().sampleIDProperty());
//        columnSample.setCellFactory(createTextWrappedCell());
//        columnSample.setPrefWidth(100);
//        tableViewForams.getColumns().add(columnSample);
//
//        //Index 1
//        TableColumn<Particle, Number> columnIndex1 = new TableColumn<>("Index 1");
//        columnIndex1.setCellValueFactory(column -> column.getValue().index1Property());
//        columnIndex1.setPrefWidth(60);
//        tableViewForams.getColumns().add(columnIndex1);
//
//        //Index 2
//        TableColumn<Particle, Number> columnIndex2 = new TableColumn<>("Index 2");
//        columnIndex2.setCellValueFactory(column -> column.getValue().index2Property());
//        columnIndex2.setPrefWidth(60);
//        tableViewForams.getColumns().add(columnIndex2);
//
//        //Resolution
//        TableColumn<Particle, Number> columnResolution = new TableColumn<>("Resolution");
//        columnResolution.setCellValueFactory(column -> column.getValue().resolutionProperty());
//        columnResolution.setPrefWidth(60);
//        tableViewForams.getColumns().add(columnResolution);
//
//        //GUID
//        TableColumn<Particle, String> columnGUID = new TableColumn<>("GUID");
//        columnGUID.setCellValueFactory(column -> column.getValue().GUIDProperty());
//        columnGUID.setCellFactory(createTextWrappedCell());
//        columnGUID.setPrefWidth(100);
//        tableViewForams.getColumns().add(columnGUID);
//
//
//
//
//
//
//        //TableViewHelpers.autoResizeColumns(tableViewForams);
//
//        //Display the particle when the user clicks on the list
//        tableViewForams.getSelectionModel().getSelectedItems().addListener((ListChangeListener) c -> {
//            selectionViewModel.setCurrentParticles(tableViewForams.getSelectionModel().getSelectedItems());
//            if (selectionViewModel.getCurrentParticles().size() > 0) {
//                selectionViewModel.setCurrentParticle(selectionViewModel.getCurrentParticles().get(0));
//            } else {
//                selectionViewModel.setCurrentParticle(null);
//            }
//        });
//
//        //If the list changes order, scroll such that the current particle is displayed
//        tableViewForams.getSortOrder().addListener((ListChangeListener) (event -> {
//            scrollToSelectedForam();
//        }));
//    }
//
//    private Callback<TableColumn<Particle, String>, TableCell<Particle, String>> createTextWrappedCell() {
//        Callback<TableColumn<Particle, String>, TableCell<Particle, String>> callback = param -> {
//            TableCell<Particle, String> cell = new TableCell<>();
//            Label label = new Label();
//            cell.setGraphic(label);
//            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
//            label.setWrapText(true);
//            label.textProperty().bind(cell.itemProperty());
//            return cell;
//        };
//        return callback;
//    }
//
//
//    public void setupBindings() {
//
//        //TODO when any new images are added it should jump to them
//        supervisor.project.particles.addListener((ListChangeListener<Particle>) (event -> {
//            event.next();
//            if (event.getAddedSize() > 0) {
//                select(event.getAddedSubList().get(0));
//            }
//        }));
////        .imageAddedFromServerEvent.addListener(listener -> {
////            selectLast();
////        });
//
//        //Table of particles
//        selectionViewModel.filteredList = new FilteredList<>(supervisor.project.particles, p -> true);
//        customTextFieldFilter.textProperty().addListener((observable, oldValue, newValue) -> {
//            selectionViewModel.filteredList.setPredicate(foram -> {
//                // If filter text is empty, display all persons.
//                if (newValue == null || newValue.length() < 1) {
//                    return true;
//                }
//                // Compare first name and last name of every person with filter text.
//                String lowerCaseFilter = newValue.toLowerCase();
//
////                if (lowerCaseFilter.startsWith("rating:")) {
////                    try {
////                        int rating = Integer.parseInt(lowerCaseFilter.substring(7));
////                        return foram.getImageQuality() == rating;
////                    } catch (Exception ex) {
////
////                    }
////                } else
//                if (lowerCaseFilter.startsWith("#")) {
//                    try {
//                        int index = Integer.parseInt(lowerCaseFilter.substring(1))-1;
//                        if (index >= 0 && index < supervisor.project.particles.size()) {
//                            return supervisor.project.particles.get(index) == foram;
//                        }
//                        else return false;
//                    } catch (NumberFormatException ex) {
//                        return false;
//                    }
//                }
//                else if (lowerCaseFilter.startsWith("file:")) {
//                    return foram.getShortFilename().toLowerCase().contains(lowerCaseFilter.substring(5));
//                } else if (lowerCaseFilter.startsWith("folder:")) {
//                    return foram.getFile().getParent().toLowerCase().contains(lowerCaseFilter.substring(7));
//                } else if (lowerCaseFilter.startsWith("tag:")) {
//                    return foram.tagsToString().toLowerCase().contains(lowerCaseFilter.substring(4));
//                } else if (lowerCaseFilter.startsWith("label:")) {
//                    return foram.getClassification().toLowerCase().contains(lowerCaseFilter.substring(6));
//                } else if (lowerCaseFilter.startsWith("guid:")) {
//                    return foram.getGUID().toLowerCase().contains(lowerCaseFilter.substring(5));
//                } else if (lowerCaseFilter.startsWith("sample:")) {
//                    return foram.getSampleID().toLowerCase().contains(lowerCaseFilter.substring(7));
//                } else if (lowerCaseFilter.startsWith("index1:")) {
//                    String d = Double.toString(foram.getIndex1());
//                    return d.contains(lowerCaseFilter.substring(7));
//                } else if (lowerCaseFilter.startsWith("index2:")) {
//                    String d = Double.toString(foram.getIndex2());
//                    return d.contains(lowerCaseFilter.substring(7));
//                } else if (foram.toString().toLowerCase().contains(lowerCaseFilter)) {
//                    return true;
//                }
//                return false;
//            });
//        });
//        selectionViewModel.sortedList = new SortedList<>(selectionViewModel.filteredList);
//        selectionViewModel.sortedList.comparatorProperty().bind(tableViewForams.comparatorProperty());
//        tableViewForams.setItems(selectionViewModel.sortedList);
//
//        supervisor.project.foramAddedEvent.addListener((foram) -> {
//            select(foram);
//        });
//        selectFirst();
//    }
//
//
//    public void select(Particle particle) {
//        tableViewForams.getSelectionModel().clearSelection();
//        tableViewForams.getSelectionModel().select(particle);
//    }
//
//
//    public void selectNext() {
//        int idx = tableViewForams.getSelectionModel().getSelectedIndex();
//        if (idx < tableViewForams.getItems().size() - 1) {
//            tableViewForams.getSelectionModel().clearAndSelect(++idx);
//            scrollToSelectedForam();
//        }
//    }
//
//
//    public void selectPrevious() {
//        if (tableViewForams.getItems().size() > 0) {
//            int idx = tableViewForams.getSelectionModel().getSelectedIndex();
//            if (idx > 0) {
//                tableViewForams.getSelectionModel().clearAndSelect(--idx);
//                scrollToSelectedForam();
//            }
//        }
//    }
//
//
//    public void selectFirst() {
//        if (tableViewForams.getItems().size() > 0) {
//            tableViewForams.getSelectionModel().clearAndSelect(0);
//            scrollToSelectedForam();
//        }
//    }
//
//    public void selectLast() {
//        if (tableViewForams.getItems().size() > 0) {
//            tableViewForams.getSelectionModel().clearAndSelect(tableViewForams.getItems().size() - 1);
//            scrollToSelectedForam();
//        }
//    }
//
//    public void selectAll() {
//        tableViewForams.getSelectionModel().selectAll();
//    }
//
//    private void scrollToSelectedForam() {
//        int index = tableViewForams.getSelectionModel().getSelectedIndex();
//        int first = virtualFlow.getFirstVisibleCell().getIndex();
//        int last = virtualFlow.getLastVisibleCell().getIndex();
//        if (index <= first) {
//            while (index <= first && virtualFlow.adjustPixels(-1) < 0) {
//                first = virtualFlow.getFirstVisibleCell().getIndex();
//            }
//        } else {
//            while (index >= last && virtualFlow.adjustPixels(1) > 0) {
//                last = virtualFlow.getLastVisibleCell().getIndex();
//            }
//        }
//    }
//
////    public void sortByArea() {
////        for (Particle particle : supervisor.project.particles) {
////            particle.scoreProperty.set(particle.morphology.getArea());
////        }
////        colScore.setText("Area");
////        colScore.setVisible(true);
////        tableViewForams.getSortOrder().clear();
////        tableViewForams.getSortOrder().addAll(colScore);
////    }
////
////
////    public void sortByEccentricity() {
////        for (Particle particle : supervisor.project.particles) {
////            particle.scoreProperty.set(particle.morphology.getEccentricity());
////        }
////        colScore.setText("Eccentricity");
////        colScore.setVisible(true);
////        tableViewForams.getSortOrder().clear();
////        tableViewForams.getSortOrder().addAll(colScore);
////    }
//
//
////    public void sortBySolidity() {
////        for (Particle particle : supervisor.project.particles) {
////            particle.scoreProperty.set(particle.morphology.solidity);
////        }
////        colScore.setText("Solidity");
////        colScore.setVisible(true);
////        tableViewForams.getSortOrder().clear();
////        tableViewForams.getSortOrder().addAll(colScore);
////    }
////
////
////    public void sortByRoundness() {
////        for (Particle particle : supervisor.project.particles) {
////            particle.scoreProperty.set(particle.morphology.roundness);
////        }
////        colScore.setText("Roundness");
////        colScore.setVisible(true);
////        tableViewForams.getSortOrder().clear();
////        tableViewForams.getSortOrder().addAll(colScore);
////    }
////
////
////    public void sortByCircularity() {
////        for (Particle particle : supervisor.project.particles) {
////            particle.scoreProperty.set(particle.morphology.circularity);
////        }
////        colScore.setText("Circularity");
////        colScore.setVisible(true);
////        tableViewForams.getSortOrder().clear();
////        tableViewForams.getSortOrder().addAll(colScore);
////    }
////
////
////    public void sortBySimilarityMorphology() {
////        Particle currentParticle = selectionViewModel.getCurrentParticle();
////        for (Particle particle : supervisor.project.getParticles()) {
////            particle.scoreProperty.set(particle.morphology.similarityTo(currentParticle.morphology));
////        }
////        colScore.setText("Morphology_old");
////        colScore.setVisible(true);
////        colScore.setSortType(TableColumn.SortType.DESCENDING);
////        tableViewForams.getSortOrder().clear();
////        tableViewForams.getSortOrder().addAll(colScore);
////    }
////
////
////    public void sortBySimilarityNeuralNetwork() {
////        Particle currentParticle = selectionViewModel.getCurrentParticle();
////        List<Particle> particles = supervisor.project.particles;
////        Service service = particleSimilarityService.similarityByCNNVector(currentParticle, particles, supervisor);
////        service.setOnSucceeded(value -> {
////            colScore.setText("Similarity");
////            colScore.setVisible(true);
////            colScore.setSortType(TableColumn.SortType.DESCENDING);
////            tableViewForams.getSortOrder().clear();
////            tableViewForams.getSortOrder().addAll(colScore);
////        });
////        BasicDialogs.ProgressDialogWithCancel2(
////                "Operation",
////                "Calculating classification vectors",
////                App.getRootPane(),
////                service).start();
////    }
//
//
//    @FXML
//    private void handleExpandList(ActionEvent event) {
//        mainViewModel.expandListRequested.broadcast();
//    }
//
//    @FXML
//    private void handleSelectAll(ActionEvent event) {
//        tableViewForams.getSelectionModel().selectAll();
//    }
//
//    @FXML
//    private void handleSmallImage(ActionEvent event) {
//        if (imageWidth > 16) {
//            imageWidth -= 16;
//            colImage.setPrefWidth(imageWidth);
//        }
//    }
//
//    @FXML
//    private void handleLargeImage(ActionEvent event) {
//        if (imageWidth < 512) {
//            imageWidth += 16;
//            colImage.setPrefWidth(imageWidth);
//        }
//    }
//
//    private String getCurrentFilterArgument() {
//        String current = customTextFieldFilter.getText();
//        if (current.startsWith("#")) current = current.substring(1);
//        if (current.length() == 0) return "";
//        String[] parts = current.split(":");
//        if (parts.length >= 2) return parts[1];
//        else if (current.contains(":")) return "";
//        else return parts[0];
//    }
//
//    @FXML
//    public void handleFilterNumber(ActionEvent actionEvent) {
//        customTextFieldFilter.setText("#");
//    }
//
//    @FXML
//    public void handleFilterFile(ActionEvent actionEvent) {
//        customTextFieldFilter.setText("file:"+getCurrentFilterArgument());
//    }
//
//    @FXML
//    public void handleFilterFolder(ActionEvent actionEvent) {
//        customTextFieldFilter.setText("folder:"+getCurrentFilterArgument());
//    }
//
//    @FXML
//    public void handleFilterLabel(ActionEvent actionEvent) {
//        customTextFieldFilter.setText("label:"+getCurrentFilterArgument());
//    }
//
//    @FXML
//    public void handleFilterTag(ActionEvent actionEvent) {
//        customTextFieldFilter.setText("tag:"+getCurrentFilterArgument());
//    }
//
//    @FXML
//    public void handleFilterIndex1(ActionEvent actionEvent) {
//        customTextFieldFilter.setText("index1:"+getCurrentFilterArgument());
//    }
//
//    @FXML
//    public void handleFilterIndex2(ActionEvent actionEvent) {
//        customTextFieldFilter.setText("index2:"+getCurrentFilterArgument());
//    }
//
//    @FXML
//    public void handleFilterGuid(ActionEvent actionEvent) {
//        customTextFieldFilter.setText("guid:"+getCurrentFilterArgument());
//    }
//
//    @FXML
//    public void handleFilterAll(ActionEvent actionEvent) {
//        customTextFieldFilter.setText(getCurrentFilterArgument());
//    }
}
