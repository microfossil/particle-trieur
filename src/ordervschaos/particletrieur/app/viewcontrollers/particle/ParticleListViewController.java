/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.viewcontrollers.particle;

import javafx.util.Callback;
import ordervschaos.particletrieur.app.App;
import ordervschaos.particletrieur.app.controls.SymbolLabel;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.viewmodels.MainViewModel;
import ordervschaos.particletrieur.app.viewmodels.SelectionViewModel;
import com.google.inject.Inject;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;
//import org.kordamp.ikonli.javafx.FontIcon;

import javax.imageio.ImageIO;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ParticleListViewController implements Initializable {

    @FXML
    TableView<Particle> tableViewForams;

    @Inject
    private Supervisor supervisor;
    @Inject
    private SelectionViewModel selectionViewModel;
    @Inject
    MainViewModel mainViewModel;

    // Column with image
    private TableColumn<Particle, File> colImage = new TableColumn<>();
    private int imageWidth = 64;

    //Scrolling
    private VirtualFlow<?> virtualFlow;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupListView();
        setupBindings();
        Platform.runLater(() -> {
            virtualFlow = (VirtualFlow<?>) ((TableViewSkin<?>) tableViewForams.getSkin()).getChildren().get(1);
        });
        selectionViewModel.nextImageRequested.addListener(val -> {
            selectNext();
        });
        selectionViewModel.previousImageRequested.addListener(val -> {
            selectPrevious();
        });
    }


    public void setupListView() {

        //Enable multiple selection
        tableViewForams.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //Index
        TableColumn<Particle, Number> colIndex = new TableColumn<>();
        colIndex.setCellValueFactory(column -> new ReadOnlyObjectWrapper<Number>(supervisor.project.particles.indexOf(column.getValue()) + 1));
        colIndex.setText("#");
        tableViewForams.getColumns().add(colIndex);
        colIndex.setPrefWidth(40);

        //Image
        colImage = new TableColumn<>("");
        colImage.setText("Image");
        colImage.setPrefWidth(imageWidth);
        colImage.setCellFactory(param -> {
            //Set up the ImageView
            final ImageView imageview = new ImageView();
            imageview.fitWidthProperty().bind(Bindings.subtract(colImage.widthProperty(), 7));
            imageview.setPreserveRatio(true);

            //Loading task
            ObjectProperty<Task<Image>> loadingTask = new SimpleObjectProperty<>();

            //Set up the Table
            TableCell<Particle, File> cell = new TableCell<Particle, File>() {
                @Override
                public void updateItem(File item, boolean empty) {
                    //Stop already running image fetch task
                    if (loadingTask.get() != null &&
                            loadingTask.get().getState() != Worker.State.SUCCEEDED &&
                            loadingTask.get().getState() != Worker.State.FAILED) {

                        loadingTask.get().cancel();
                    }
                    loadingTask.set(null);
                    //Load image if not null
                    if (empty || item == null) {
                        imageview.setVisible(false);
                    } else {
                        imageview.setVisible(true);
                        imageview.setOnMouseClicked(event -> {
                            Particle particle = (Particle) this.getTableRow().getItem();
                            ImageDescriptionPopover popover = new ImageDescriptionPopover(particle, PopOver.ArrowLocation.LEFT_CENTER);
                            popover.show(imageview);
                        });
                        Task<Image> task = new Task<Image>() {
                            @Override
                            public Image call() throws Exception {
                                if (item.exists()) {
                                    return SwingFXUtils.toFXImage(ImageIO.read(item), null);
                                } else {
                                    return new Image(App.class.getResourceAsStream("resources/missing-image-128.png"), 64, 64, true, true);
                                }
                            }
                        };
                        loadingTask.set(task);
                        task.setOnSucceeded(event -> {

                            imageview.setImage(task.getValue());
                        });
                        App.getExecutorService().submit(task);
                    }
                }
            };
            // Attach the imageview to the cell
            cell.setGraphic(imageview);
            return cell;
        });
        colImage.setCellValueFactory(cellData -> cellData.getValue().fileProperty());
        tableViewForams.getColumns().add(colImage);

        //Sample / index
        TableColumn<Particle, String> coreIDCol = new TableColumn<>("Sample");
        coreIDCol.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().sampleIDProperty(),
                "#",
                Bindings.createStringBinding(() -> Double.toString(cellData.getValue().getIndex1()), cellData.getValue().index1Property()),
                "#",
                Bindings.createStringBinding(() -> Double.toString(cellData.getValue().getIndex2()), cellData.getValue().index2Property())
        ));
        coreIDCol.setCellFactory(param -> {
            //VBox
            final VBox vBox = new VBox();
            final Label labelCore = new Label();
            final Label labelNum1 = new Label();
            final Label labelNum2 = new Label();
            labelCore.setStyle("-fx-font-weight: bold");
            vBox.getChildren().addAll(labelCore, labelNum1, labelNum2);
            TableCell<Particle, String> cell = new TableCell<Particle, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    if (empty || item == null) {
                        vBox.setVisible(false);
                    } else {
                        vBox.setVisible(true);
                        String[] part = item.split("#", -1);
                        if (part.length == 3) {
                            labelCore.setText(part[0].equals("") ? "unknown" : part[0]);
                            labelNum1.setText("[1] " + part[1]);
                            labelNum2.setText("[2] " + part[2]);
                        }
                    }
                }
            };
            cell.setGraphic(vBox);
            return cell;
        });
        coreIDCol.setPrefWidth(90);
        tableViewForams.getColumns().add(coreIDCol);

        //Class / tags
        TableColumn<Particle, String> classCol = new TableColumn<>("Label/Tag");
        classCol.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().classification,
                "#",
                cellData.getValue().tagUIProperty,
                "#",
                cellData.getValue().validatorProperty(),
                "#"
        ));
        classCol.setCellFactory(param -> {
            //VBox
            final VBox vBox = new VBox();
            final Label labelClass = new Label();
            final Label labelTags = new Label();
            labelClass.setStyle("-fx-font-weight: bold");
            final SymbolLabel symbolLabel = new SymbolLabel("feathercheckcircle", 16);
            symbolLabel.setSymbolColor("green");
            vBox.getChildren().addAll(labelClass, labelTags, symbolLabel);

            TableCell<Particle, String> cell = new TableCell<Particle, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    if (empty || item == null) {
                        vBox.setVisible(false);
                    } else {
                        vBox.setVisible(true);
                        String[] part = item.split("#", -1);
                        if (part.length > 0) {
                            labelClass.setText(part[0]);
                        }
                        if (part.length > 1) {
                            labelTags.setText(part[1]);
                        }
                        if (part.length > 2) {
                            if (part[2].equals("")) {
                                symbolLabel.setVisible(false);
                            } else {
                                symbolLabel.setVisible(true);
                            }
                        }
                    }
                }
            };
            cell.setGraphic(vBox);
            return cell;
        });
        classCol.setPrefWidth(90);
        tableViewForams.getColumns().add(classCol);

        //Class / tags
        TableColumn<Particle, String> valCol = new TableColumn<>("Annotator");
        valCol.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().classifierIdProperty,
                "#",
                cellData.getValue().validatorProperty(),
                "#"
        ));
        valCol.setCellFactory(param -> {
            //VBox
            final VBox vBox = new VBox();
            final Label labelAnnotator = new Label();
            final Label labelValidator = new Label();
            labelAnnotator.setStyle("-fx-font-weight: bold");
            vBox.getChildren().addAll(labelAnnotator, labelValidator);

            TableCell<Particle, String> cell = new TableCell<Particle, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    if (empty || item == null) {
                        vBox.setVisible(false);
                    } else {
                        vBox.setVisible(true);
                        String[] part = item.split("#", -1);
                        if (part.length > 0) {
                            labelAnnotator.setText(part[0]);
                        }
                        if (part.length > 1) {
                            labelValidator.setText(part[1]);
                        }
                    }
                }
            };
            cell.setGraphic(vBox);
            return cell;
        });
        valCol.setPrefWidth(90);
        tableViewForams.getColumns().add(valCol);


        //Filename
        TableColumn<Particle, String> columnFilename = new TableColumn<>("Filename");
        columnFilename.setCellValueFactory(column -> column.getValue().shortFilenameProperty());
        columnFilename.setCellFactory(createTextWrappedCell());
        columnFilename.setPrefWidth(120);
        tableViewForams.getColumns().add(columnFilename);

        //Folder
        TableColumn<Particle, String> columnFolder = new TableColumn<>("Folder");
        columnFolder.setCellValueFactory(column -> column.getValue().folderProperty());
        columnFolder.setCellFactory(createTextWrappedCell());
        columnFolder.setPrefWidth(160);
        tableViewForams.getColumns().add(columnFolder);

        //Sample
        TableColumn<Particle, String> columnSample = new TableColumn<>("Sample");
        columnSample.setCellValueFactory(column -> column.getValue().sampleIDProperty());
        columnSample.setCellFactory(createTextWrappedCell());
        columnSample.setPrefWidth(100);
        tableViewForams.getColumns().add(columnSample);

        //Index 1
        TableColumn<Particle, Number> columnIndex1 = new TableColumn<>("Index 1");
        columnIndex1.setCellValueFactory(column -> column.getValue().index1Property());
        columnIndex1.setPrefWidth(60);
        tableViewForams.getColumns().add(columnIndex1);

        //Index 2
        TableColumn<Particle, Number> columnIndex2 = new TableColumn<>("Index 2");
        columnIndex2.setCellValueFactory(column -> column.getValue().index2Property());
        columnIndex2.setPrefWidth(60);
        tableViewForams.getColumns().add(columnIndex2);

        //Resolution
        TableColumn<Particle, Number> columnResolution = new TableColumn<>("Resolution");
        columnResolution.setCellValueFactory(column -> column.getValue().resolutionProperty());
        columnResolution.setPrefWidth(60);
        tableViewForams.getColumns().add(columnResolution);

        //GUID
        TableColumn<Particle, String> columnGUID = new TableColumn<>("GUID");
        columnGUID.setCellValueFactory(column -> column.getValue().GUIDProperty());
        columnGUID.setCellFactory(createTextWrappedCell());
        columnGUID.setPrefWidth(100);
        tableViewForams.getColumns().add(columnGUID);

        //TableViewHelpers.autoResizeColumns(tableViewForams);

        //Display the particle when the user clicks on the list
        tableViewForams.getSelectionModel().getSelectedItems().addListener((ListChangeListener) c -> {
            selectionViewModel.setCurrentParticles(tableViewForams.getSelectionModel().getSelectedItems());
        });

        //If the list changes order, scroll such that the current particle is displayed
        tableViewForams.getSortOrder().addListener((ListChangeListener) (event -> {
            scrollToSelectedForam();
        }));
    }

    private Callback<TableColumn<Particle, String>, TableCell<Particle, String>> createTextWrappedCell() {
        Callback<TableColumn<Particle, String>, TableCell<Particle, String>> callback = param -> {
            TableCell<Particle, String> cell = new TableCell<>();
            Label label = new Label();
            cell.setGraphic(label);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            label.setWrapText(true);
            label.textProperty().bind(cell.itemProperty());
            return cell;
        };
        return callback;
    }

    public void setupBindings() {

        // Jump to image if added
        supervisor.project.particles.addListener((ListChangeListener<Particle>) (event -> {
            event.next();
            if (event.getAddedSize() > 0) {
                select(event.getAddedSubList().get(0));
            }
        }));

        // Select all if requested
        selectionViewModel.selectAllRequested.addListener(event -> {
            selectAll();
        });

        // Image size
        selectionViewModel.decreaseSizeRequested.addListener(v -> {
            if (selectionViewModel.selectedTabIndex != 0) return;
            if (imageWidth >= 32 + 16) {
                imageWidth -= 16;
            }
            colImage.setPrefWidth(imageWidth);
        });

        selectionViewModel.increaseSizeRequested.addListener(v -> {
            if (selectionViewModel.selectedTabIndex != 0) return;
            if (imageWidth <= 512 - 16) {
                imageWidth += 16;
            }
            colImage.setPrefWidth(imageWidth);
        });

        selectionViewModel.sortedList.comparatorProperty().bind(tableViewForams.comparatorProperty());
        tableViewForams.setItems(selectionViewModel.sortedList);

        supervisor.project.particleAddedEvent.addListener(this::select);
        selectFirst();

        // Indices
        selectionViewModel.controlSelectIndex.addListener(val ->{
            if (tableViewForams.getSelectionModel().getSelectedIndices().contains(val)) {
                tableViewForams.getSelectionModel().clearSelection(val);
            }
            else {
                tableViewForams.getSelectionModel().select(val);
            }
        });
        selectionViewModel.selectIndex.addListener(val -> {
            tableViewForams.getSelectionModel().clearAndSelect(val);
        });
        selectionViewModel.shiftSelectIndices.addListener(val -> {
            tableViewForams.getSelectionModel().clearSelection();
            tableViewForams.getSelectionModel().selectRange(val[0], val[1]+1);
        });
    }

    public void select(Particle particle) {
        tableViewForams.getSelectionModel().clearSelection();
        tableViewForams.getSelectionModel().select(particle);
    }

    public void selectNext() {
        int idx = tableViewForams.getSelectionModel().getSelectedIndex();
        if (idx < tableViewForams.getItems().size() - 1) {
            tableViewForams.getSelectionModel().clearAndSelect(++idx);
            scrollToSelectedForam();
        }
    }

    public void selectPrevious() {
        if (tableViewForams.getItems().size() > 0) {
            int idx = tableViewForams.getSelectionModel().getSelectedIndex();
            if (idx > 0) {
                tableViewForams.getSelectionModel().clearAndSelect(--idx);
                scrollToSelectedForam();
            }
        }
    }

    public void selectFirst() {
        if (tableViewForams.getItems().size() > 0) {
            tableViewForams.getSelectionModel().clearAndSelect(0);
            scrollToSelectedForam();
        }
    }

    public void selectLast() {
        if (tableViewForams.getItems().size() > 0) {
            tableViewForams.getSelectionModel().clearAndSelect(tableViewForams.getItems().size() - 1);
            scrollToSelectedForam();
        }
    }

    public void selectAll() {
        tableViewForams.getSelectionModel().selectAll();
    }

    private void scrollToSelectedForam() {
        int index = tableViewForams.getSelectionModel().getSelectedIndex();
        int first = virtualFlow.getFirstVisibleCell().getIndex();
        int last = virtualFlow.getLastVisibleCell().getIndex();
        if (index <= first) {
            while (index <= first && virtualFlow.adjustPixels(-1) < 0) {
                first = virtualFlow.getFirstVisibleCell().getIndex();
            }
        } else {
            while (index >= last && virtualFlow.adjustPixels(1) > 0) {
                last = virtualFlow.getLastVisibleCell().getIndex();
            }
        }
    }
}
