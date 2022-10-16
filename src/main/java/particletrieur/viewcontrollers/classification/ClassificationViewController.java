/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.classification;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import particletrieur.AbstractDialogController;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.controls.ClassificationButton;
import particletrieur.controls.SymbolLabel;
import particletrieur.helpers.FilteredTreeViewSelectionModel;
import particletrieur.helpers.TreeItemSelectionFilter;
import particletrieur.models.project.Project;
import particletrieur.models.Supervisor;
import particletrieur.models.network.classification.Classification;
import particletrieur.models.network.classification.ClassificationSet;
import particletrieur.models.network.classification.NetworkInfo;
import particletrieur.models.project.Particle;
import particletrieur.models.project.Tag;
import particletrieur.models.project.Taxon;
import particletrieur.viewcontrollers.label.EditLabelViewController;
import particletrieur.viewcontrollers.network.SelectNetworkViewController;
import particletrieur.viewcontrollers.tag.EditTagViewController;
import particletrieur.viewmodels.*;
import com.google.inject.Inject;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import particletrieur.viewmodels.network.CNNPredictionViewModel;
import particletrieur.viewmodels.network.KNNPredictionViewModel;
import particletrieur.viewmodels.particles.LabelsViewModel;
import particletrieur.viewmodels.particles.TagsViewModel;
import org.controlsfx.control.PopOver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ClassificationViewController implements Initializable {

    @FXML
    GridPane gridPaneContainer;
    @FXML
    StackPane stackPanePredictions;
    @FXML
    BorderPane borderPanePredictions;
    @FXML
    VBox vboxPredictions;
    @FXML
    Button buttonCollapsePredictions;
    @FXML
    ScrollPane scrollPaneClassButtons;
    @FXML
    ToggleButton toggleButtonLabelsCategory;
    @FXML
    ToggleGroup toggleGroupLabelMode;
    @FXML
    ToggleButton toggleButtonLabelsFlat;
    @FXML
    SymbolLabel symbolValidate;
    @FXML
    Button buttonValidate;
    @FXML
    VBox gridPaneCNN;
    @FXML
    VBox gridPaneKNN;
    @FXML
    HBox hboxCNN;
    @FXML
    SymbolLabel symbolLabelDeepCNNRefresh;
    @FXML
    CheckBox checkBoxPreprocessBeforeClassification;
    @FXML
    Spinner spinnerCNNThreshold;
    @FXML
    Spinner spinnerKNNThreshold;
    @FXML
    FlowPane flowPaneTags;
    @FXML
    VBox vboxClasses;
    @FXML
    public Label labelNetwork;

    @Inject
    MainViewModel mainViewModel;
    @Inject
    SelectionViewModel selectionViewModel;
    @Inject
    Supervisor supervisor;
    @Inject
    TagsViewModel tagsViewModel;
    @Inject
    CNNPredictionViewModel cnnPredictionViewModel;
    @Inject
    KNNPredictionViewModel knnPredictionViewModel;
    @Inject
    LabelsViewModel labelsViewModel;

    //Buttons for classification and tagging
    Map<String, ClassificationButton> labelButtons = new HashMap<>();
    Map<String, TreeItem<String>> labelTreeItems = new HashMap<>();
    TreeView<String> treeView = new TreeView<>();
    Map<String, Button> tagButtons = new HashMap<>();
    RotateTransition rotateTransitionSymbolLabelDeepCNN;
    public ArrayList<String> taxonCodes = new ArrayList<>();

    private IntegerProperty displayMode = new SimpleIntegerProperty(1);
    private boolean sidePaneToggled = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //CNN classifying animation
        rotateTransitionSymbolLabelDeepCNN = new RotateTransition(Duration.millis(3000), symbolLabelDeepCNNRefresh);
        rotateTransitionSymbolLabelDeepCNN.setByAngle(360);
        rotateTransitionSymbolLabelDeepCNN.setInterpolator(Interpolator.LINEAR);
        rotateTransitionSymbolLabelDeepCNN.setCycleCount(Animation.INDEFINITE);
        symbolLabelDeepCNNRefresh.setCache(true);
        symbolLabelDeepCNNRefresh.setCacheHint(CacheHint.SPEED);

        //Controls
        checkBoxPreprocessBeforeClassification.selectedProperty().bindBidirectional(supervisor.project.processingInfo.processBeforeClassificationProperty());
        spinnerCNNThreshold.getValueFactory().valueProperty().bindBidirectional(supervisor.project.processingInfo.cnnThresholdProperty());
        spinnerKNNThreshold.getValueFactory().valueProperty().bindBidirectional(supervisor.project.processingInfo.knnThresholdProperty());

        //Current particle updated
        selectionViewModel.currentParticleProperty().addListener((observable, oldValue, newValue) -> {
            updateClassificationUI(newValue);
            updateTagUI(newValue);
        });
        updateValidatedButton(LabelsViewModel.ValidationState.INVALID);

        //Network updated
        supervisor.project.networkDefinitionProperty().addListener(listener -> {
            setupNetworkUI(supervisor.project.getNetworkDefinition());
        });

        //Updated events
        supervisor.project.taxonsUpdatedEvent.addListener(listener -> {
            setupClassificationUI(supervisor.project);
            updateClassificationUI(selectionViewModel.getCurrentParticle());
            cnnPredictionViewModel.refreshPredictions();
            knnPredictionViewModel.refreshPredictions();
        });
        supervisor.project.tagsUpdatedEvent.addListener(listener -> {
            setupTagUI(supervisor.project);
            updateTagUI(selectionViewModel.getCurrentParticle());
        });
        selectionViewModel.currentParticleUpdatedEvent.addListener(listener -> {
            updateClassificationUI(selectionViewModel.getCurrentParticle());
            updateTagUI(selectionViewModel.getCurrentParticle());
        });
        displayMode.addListener((observable, oldValue, newValue) -> {
            setupClassificationUI(supervisor.project);
        });

        setupClassificationUI(supervisor.project);
        setupTagUI(supervisor.project);

        //kNN classification updated
        knnPredictionViewModel.kNNPredictedClassificationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.classifications.size() == 0) {
//                buttonkNNLabel.setText("N/A");
                updateKNNUI(null);
//                progressBarkNNScore.setProgress(0);
            } else {
                updateKNNUI(newValue);
//                buttonkNNLabel.setText(newValue.getBestCode());
//                progressBarkNNScore.setProgress(newValue.getBest().getValue());
            }
        });

        //CNN classification updated
        cnnPredictionViewModel.cnnPredictedClassificationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.classifications.size() == 0) {
//                buttonCNNLabel.setText("N/A");
                updateCNNUI(null);
//                progressBarCNNScore.setProgress(0);
            } else {
                updateCNNUI(newValue);
//                buttonCNNLabel.setText(newValue.getBest().getCode());
//                progressBarCNNScore.setProgress(newValue.getBest().getValue());
            }
        });

        //CNN running
        cnnPredictionViewModel.runningProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                symbolLabelDeepCNNRefresh.setVisible(true);
                rotateTransitionSymbolLabelDeepCNN.play();
            } else {
                symbolLabelDeepCNNRefresh.setVisible(false);
                rotateTransitionSymbolLabelDeepCNN.stop();
            }
        });
        gridPaneCNN.getChildren().clear();
        gridPaneCNN.getChildren().add(new Label("N/A"));

        //TODO change to a property on the supervisor
//        supervisor.network.enabledProperty().addListener((observable, oldValue, newValue) -> {
//            gridPaneCNN.getChildren().clear();
//            if (newValue) gridPaneCNN.getChildren().add(new Label("Ready"));
//            else gridPaneCNN.getChildren().add(new Label("N/A"));
//        });

        //Spinner hack
        for (Field field : getClass().getDeclaredFields()) {
            try {
                Object obj = field.get(this);
                if (obj != null && obj instanceof Spinner)
                    ((Spinner) obj).focusedProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue) {
                            ((Spinner) obj).increment(0);
                        }
                    });
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        //Validation button
        buttonValidate.setOnAction(event -> {
            labelsViewModel.toggleValidated();
        });

        //Disable selection of leaf in tree view
        MultipleSelectionModel<TreeItem<String>> selectionModel = treeView.getSelectionModel();
        TreeItemSelectionFilter<String> filter = TreeItem::isLeaf;
        FilteredTreeViewSelectionModel<String> filteredSelectionModel = new FilteredTreeViewSelectionModel<>(treeView, selectionModel, filter);
        treeView.setSelectionModel(filteredSelectionModel);
        //Custom tree view cell with click events
        treeView.setCellFactory(p -> new ClassificationTreeCell());
        //Disable right click from selecting cell
        treeView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.isSecondaryButtonDown()) {
                Node text = (Node) event.getTarget();
                if (text.getParent() instanceof TreeCell) {
                    event.consume();
//                    TreeCell treeCell = (TreeCell) text.getParent();
//                    treeCell.getContextMenu().show(text, 0, 0);
                }
            }
        });

        //Fixed cell size
        treeView.setFixedCellSize(24);

        //Fix scroll pane slow scrolling
        scrollPaneClassButtons.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY();
            double contentHeight = scrollPaneClassButtons.getContent().getBoundsInLocal().getHeight();
            double scrollPaneHeight = scrollPaneClassButtons.getHeight();
            double diff = contentHeight - scrollPaneHeight;
            if (diff < 1) diff = 1;
            double vvalue = scrollPaneClassButtons.getVvalue();
            scrollPaneClassButtons.setVvalue(vvalue + -deltaY/diff);
        });

        //Collapse predictions
        buttonCollapsePredictions.setOnAction(event -> {
            if (sidePaneToggled) {
                gridPaneContainer.getColumnConstraints().get(1).setPrefWidth(40);
                gridPaneContainer.getColumnConstraints().get(1).setMinWidth(40);
                buttonCollapsePredictions.setGraphic(new SymbolLabel("featherchevronsleft", 12));
                stackPanePredictions.setVisible(false);
            }
            else {
                gridPaneContainer.getColumnConstraints().get(1).setPrefWidth(240);
                gridPaneContainer.getColumnConstraints().get(1).setMinWidth(240);
                buttonCollapsePredictions.setGraphic(new SymbolLabel("featherchevronsright", 12));
                stackPanePredictions.setVisible(true);
            }
            sidePaneToggled = !sidePaneToggled;
        });
        buttonCollapsePredictions.setGraphic(new SymbolLabel("featherchevronsright", 12));
    }

    private List<Taxon> sortedListOfTaxons(Project project, boolean isClass) {
        ArrayList<Taxon> taxons = new ArrayList<>();
        for (Map.Entry<String, Taxon> entry : project.taxons.entrySet()) {
            Taxon taxon = entry.getValue();
            if (taxon.getIsClass() == isClass) taxons.add(taxon);
        }
        List<Taxon> sortedTaxons = taxons.stream().sorted(Comparator.comparing(Taxon::getCode)).collect(Collectors.toList());
        return sortedTaxons;
    }

    private List<Tag> sortedListOfTags(Project project) {
        List<Tag> sortedTags = project.tags.values().stream().sorted(Comparator.comparing(Tag::getCode)).collect(Collectors.toList());
        return sortedTags;
    }

    private void setupClassificationUI(Project project) {
        int mode = displayMode.get();
        if (mode == 0) {
            labelButtons.clear();
            labelTreeItems.clear();
            vboxClasses.getChildren().clear();
            List<Taxon> taxonList = sortedListOfTaxons(project, true);
            List<Taxon> nonTaxonList = sortedListOfTaxons(project, false);
            addButtonGroup("Classes", taxonList, false);
            addButtonGroup("Non-classes", nonTaxonList, false);
        }
        if (mode == 1) {
            //Group the taxons by the start of their code
            LinkedHashMap<String, ArrayList<Taxon>> groupedTaxonMap = new LinkedHashMap<>();
            ArrayList<Taxon> otherList = new ArrayList<>();
            List<Taxon> taxonList = sortedListOfTaxons(project, true);
            List<Taxon> nonTaxonList = sortedListOfTaxons(project, false);
            for (Taxon taxon : taxonList) {
                final String code = taxon.getCode();
                String[] parts = code.split("[-_ ]+");
                if (parts.length > 1) {
                    ArrayList<Taxon> list = groupedTaxonMap.getOrDefault(parts[0], new ArrayList<>());
                    list.add(taxon);
                    groupedTaxonMap.putIfAbsent(parts[0], list);
                }
                else {
                    otherList.add(taxon);
                }
            }
            //Add
//            taxonCodes.clear();
            labelButtons.clear();
            labelTreeItems.clear();
            vboxClasses.getChildren().clear();
//            ArrayList<Taxon> otherList = new ArrayList<>();
            for (Map.Entry<String, ArrayList<Taxon>> group : groupedTaxonMap.entrySet()) {
//                if (group.getValue().size() == 1) {
//                    otherList.add(group.getValue().get(0));
//                    continue;
//                }
                addButtonGroup(group.getKey(), group.getValue(), true);
            }
            addButtonGroup("Other", otherList, false);
            addButtonGroup("Non-classes", nonTaxonList, false);
        }
        if (mode == 2) {
            labelButtons.clear();
            labelTreeItems.clear();
            vboxClasses.getChildren().clear();
            vboxClasses.getChildren().add(new Label("Classes"));
            //Group the taxons by the start of their code
            LinkedHashMap<String, ArrayList<Taxon>> taxonMap = new LinkedHashMap<>();
            List<Taxon> taxonList = sortedListOfTaxons(project, true);
            List<Taxon> nonTaxonList = sortedListOfTaxons(project, false);
            for (Taxon taxon : taxonList) {
                final String code = taxon.getCode();
                String[] parts = code.split("[-_ ]+");
                ArrayList<Taxon> list = taxonMap.getOrDefault(parts[0], new ArrayList<>());
                list.add(taxon);
                taxonMap.putIfAbsent(parts[0], list);
            }
            // Add to tree view
            if (treeView.getRoot() != null) {
                treeView.getRoot().getChildren().clear();
            }
            else {
                treeView.setRoot(new TreeItem<>());
            }
            treeView.setShowRoot(false);
            for (Map.Entry<String, ArrayList<Taxon>> entry : taxonMap.entrySet()) {
                String code = entry.getKey();
                ArrayList<Taxon> taxons = entry.getValue();
                if (taxons.size() == 1) {
                    TreeItem<String> treeItem = new TreeItem<>(taxons.get(0).getCode());
                    labelTreeItems.put(taxons.get(0).getCode(), treeItem);
                    treeView.getRoot().getChildren().add(treeItem);
                }
                else {
                    TreeItem<String> treeItem = new TreeItem<>(code);
                    treeItem.setExpanded(true);
                    treeView.getRoot().getChildren().add(treeItem);
                    for (Taxon taxon : taxons) {
                        TreeItem<String> subTreeItem = new TreeItem<>(taxon.getCode());
                        labelTreeItems.put(taxon.getCode(), subTreeItem);
                        treeItem.getChildren().add(subTreeItem);
                    }
                }
            }
            treeView.setPrefHeight(treeView.getFixedCellSize() * treeView.getExpandedItemCount() + 8);
            vboxClasses.getChildren().add(treeView);
            addButtonGroup("Non-classes", nonTaxonList, false);
        }
        updateClassificationUI(selectionViewModel.getCurrentParticle());
    }

    private void addButtonGroup(String name, List<Taxon> list, boolean isGrouped) {
        Label label = new Label(name);
        label.setStyle("-fx-font-size: 14px;");
//        label.setPadding(new Insets(7,0,0,0));
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(7);
        flowPane.setVgap(7);
        flowPane.setPadding(new Insets(0,0,7,0));
        for (Taxon taxon : list) {
            final String code = taxon.getCode();
            String buttonCode = code;
            //Create a button
            if (isGrouped) {
                String[] parts = code.split("[-_ ]+");
                if (parts.length > 1) {
                    buttonCode = code.substring(parts[0].length()+1);
                }
            }
            ClassificationButton button = new ClassificationButton(buttonCode);
            button.getButton().setMinWidth(80);
            //Change the particle label when the button is clicked
            button.getButton().addEventHandler(MouseEvent.MOUSE_CLICKED, (event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    labelsViewModel.setLabel(code, 1.0, true);
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    PopOver popOver = createLabelPopover(taxon);
                    popOver.show(button);
                }
            }));
            flowPane.getChildren().add(button);
            //Put button in dictionary so we can modify it later.
            labelButtons.put(code, button);
        }
        Button buttonAddNewLabel = new Button();
        buttonAddNewLabel.setMinWidth(20);
        buttonAddNewLabel.getStyleClass().add("flat-button");
        buttonAddNewLabel.setGraphic(new SymbolLabel("featherplus", 12));
        buttonAddNewLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (name.equals("Other") || name.equals("Classes")) {
                showEditLabelDialog("", true);
            } else if (name.equals("Non-classes")) {
                showEditLabelDialog("", false);
            } else {
                showEditLabelDialog(name, true);
            }
        });
        flowPane.getChildren().add(buttonAddNewLabel);
        vboxClasses.getChildren().addAll(label, flowPane);
//        vboxClasses.getChildren().add(new Separator());
    }

    private void setupTagUI(Project project) {
        flowPaneTags.getChildren().clear();
        tagButtons.clear();
        List<Tag> tags = sortedListOfTags(project);
        //Generate tag buttons
        for (Tag tag : tags) {
            final String code = tag.getCode();
            Button button = new Button(code);
            button.setMnemonicParsing(false);
            button.setMinWidth(80);

            //Event when the button is clicked
            button.addEventHandler(MouseEvent.MOUSE_CLICKED, (event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    tagsViewModel.setTag(code);
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    //To do: enable multiclass
                    PopOver popOver = createTagPopover(tag);
                    popOver.show(button);
                }
            }));

            //Add to pane
            flowPaneTags.getChildren().add(button);

            //Put button in dictionary so we can modify it later.
            tagButtons.put(code, button);
        }

        //Button to add a new tag
        Button buttonAddNewTag = new Button();
        buttonAddNewTag.setMinWidth(20);
        buttonAddNewTag.getStyleClass().add("flat-button");
        buttonAddNewTag.setGraphic(new SymbolLabel("featherplus", 12));
        buttonAddNewTag.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            showEditTagDialog(null);
        });
        flowPaneTags.getChildren().add(buttonAddNewTag);
    }

    private void setupNetworkUI(NetworkInfo def) {
        labelNetwork.setText("No network");
        if (def != null) {
            labelNetwork.setText(def.protobuf);
        }
    }

    private void showEditLabelDialog(Taxon taxon) {
        try {
            EditLabelViewController controller = AbstractDialogController.create(EditLabelViewController.class);
            if (taxon != null) controller.setup(taxon);
            controller.showEmbedded();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showEditLabelDialog(String prefix, boolean isClass) {
        try {
            EditLabelViewController controller = AbstractDialogController.create(EditLabelViewController.class);
            if (prefix != null) {
                controller.textFieldCode.setText(prefix);
            }
            controller.checkBoxIsClass.setSelected(isClass);
            controller.showEmbedded();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showEditTagDialog(Tag tag) {
        try {
            EditTagViewController controller = AbstractDialogController.create(EditTagViewController.class);
            if (tag != null) controller.setData(tag);
            controller.showEmbedded();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PopOver createLabelPopover(Taxon taxon) {
        final String code = taxon.getCode();
        taxonCodes.add(code);
        Button button = new Button(code);
        button.setMnemonicParsing(false);
        button.setFocusTraversable(false);
        PopOver popOver = new PopOver();
        VBox infoVBox = new VBox();
        infoVBox.setPadding(new Insets(10, 10, 10, 10));
        infoVBox.setSpacing(5);
        GridPane infoGridPane = new GridPane();
        infoGridPane.setHgap(5);
        infoGridPane.setVgap(5);
        infoGridPane.add(new Text("Code:"), 0, 0);
        infoGridPane.add(new Text("Name:"), 0, 1);
        infoGridPane.add(new Text("Description:"), 0, 2);
        infoGridPane.add(new Text("Score:"), 0, 3);
        infoGridPane.add(new Text(taxon.getCode()), 1, 0);
        infoGridPane.add(new Text(taxon.getName()), 1, 1);
        infoGridPane.add(new Text(taxon.getDescription()), 1, 2);
//        Slider infoSlider = new Slider();
//        infoSlider.setMin(0.0);
//        infoSlider.setMax(1.0);
//        infoSlider.setMajorTickUnit(0.1);
//        infoSlider.setMinorTickCount(0);
//        infoSlider.setShowTickMarks(true);
//        infoSlider.setShowTickLabels(true);
//        infoSlider.setMinWidth(200);
        infoVBox.getChildren().add(infoGridPane);
//        infoVBox.getChildren().add(infoSlider);

        FlowPane flowPaneImages = new FlowPane();
        flowPaneImages.setHgap(4);
        flowPaneImages.setVgap(4);

        List<Particle> particles = supervisor.project.getParticles().stream().filter(p -> p.classification.get().equals(taxon.getCode())).collect(Collectors.toList());
        Collections.shuffle(particles);

        for (int i = 0; i < Math.min(particles.size(), 16); i++) {
            try {
                ImageView imageView = new ImageView(particles.get(i).getImage());
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                flowPaneImages.getChildren().add(imageView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        infoVBox.getChildren().add(flowPaneImages);

        if (!Arrays.asList(supervisor.project.requiredTaxons).contains(taxon.getCode())) {
            HBox buttons = new HBox();
            buttons.setSpacing(8);
            Button editButton = new Button("Edit");
            editButton.setGraphic(new SymbolLabel("featheredit2", 13));
            editButton.addEventHandler(ActionEvent.ACTION, (event -> {
                popOver.hide();
                showEditLabelDialog(taxon);
            }));
            Button deleteButton = new Button("Delete");
            deleteButton.setGraphic(new SymbolLabel("feathertrash2", 13));
            deleteButton.addEventHandler(ActionEvent.ACTION, (event -> {
                popOver.hide();
                labelsViewModel.deleteLabel(taxon);
            }));
            Pane expander = new Pane();
            HBox.setHgrow(expander, Priority.ALWAYS);
            buttons.getChildren().addAll(editButton, expander, deleteButton);
            infoVBox.getChildren().add(buttons);
        } else {
            infoVBox.getChildren().add(new Label("Cannot be edited / deleted"));
        }
        popOver.setDetachable(false);
        popOver.setContentNode(infoVBox);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
//        infoSlider.setStyle("-fx-base: #ececec;");

        //Event for when popup is opened
//        popOver.setOnShowing(event -> {
//            if (selectionViewModel.getCurrentParticle() != null) {
//                infoSlider.setValue(selectionViewModel.getCurrentParticle().getScore(code));
//            }
//        });

        //Event for when slider is moved
//        infoSlider.valueProperty().addListener(event -> {
//            labelsViewModel.setLabel(code, infoSlider.getValue(), false);
//        });

        return popOver;
    }

    private PopOver createTagPopover(Tag tag) {
        final String code = tag.getCode();
        taxonCodes.add(code);
        Button button = new Button(code);
        button.setMnemonicParsing(false);
        button.setFocusTraversable(false);
        PopOver popOver = new PopOver();
        VBox infoVBox = new VBox();
        infoVBox.setPadding(new Insets(10, 10, 10, 10));
        infoVBox.setSpacing(5);
        GridPane infoGridPane = new GridPane();
        infoGridPane.setHgap(5);
        infoGridPane.setVgap(5);
        infoGridPane.add(new Text("Code:"), 0, 0);
        infoGridPane.add(new Text("Name:"), 0, 1);
        infoGridPane.add(new Text("Description:"), 0, 2);
        infoGridPane.add(new Text(tag.getCode()), 1, 0);
        infoGridPane.add(new Text(tag.getName()), 1, 1);
        infoGridPane.add(new Text(tag.getDescription()), 1, 2);
        infoVBox.getChildren().add(infoGridPane);
        if (!Arrays.asList(supervisor.project.requiredTags).contains(tag.getCode())) {
            HBox buttons = new HBox();
            buttons.setSpacing(7);
            Button editButton = new Button("Edit");
            editButton.setGraphic(new SymbolLabel("featheredit2", 13));
            editButton.addEventHandler(ActionEvent.ACTION, (event -> {
                showEditTagDialog(tag);
            }));
            Button deleteButton = new Button("Delete");
            deleteButton.setGraphic(new SymbolLabel("feathertrash2", 13));
            deleteButton.addEventHandler(ActionEvent.ACTION, (event -> {
                tagsViewModel.deleteTag(tag);
            }));
            Pane expander = new Pane();
            HBox.setHgrow(expander, Priority.ALWAYS);
            buttons.getChildren().addAll(editButton, expander, deleteButton);
            infoVBox.getChildren().add(buttons);
        } else {
            infoVBox.getChildren().add(new Label("Cannot be edited / deleted"));
        }
        popOver.setDetachable(false);
        popOver.setContentNode(infoVBox);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);

        return popOver;
    }

    private void updateClassificationUI(Particle particle) {
        if (particle != null) {
            String code = particle.getClassification();
            for (Map.Entry<String, ClassificationButton> b : labelButtons.entrySet()) {
                b.getValue().setIsHighlighted(b.getKey().equals(code));
            }
            updateValidatedButton(labelsViewModel.getValidationState());
            treeView.getSelectionModel().select(labelTreeItems.get(code));
        } else {
            for (Map.Entry<String, ClassificationButton> b : labelButtons.entrySet()) {
                b.getValue().setIsHighlighted(false);
            }
            treeView.getSelectionModel().clearSelection();
        }
    }

    private void updateValidatedButton(LabelsViewModel.ValidationState state) {
        if (state == LabelsViewModel.ValidationState.VALIDATED) {
            buttonValidate.setText("Validated");
            buttonValidate.setStyle("-fx-text-fill: green");
            symbolValidate.setSymbol("feathercheckcircle");
            symbolValidate.setSymbolColor("green");
        } else if (state == LabelsViewModel.ValidationState.INDETERMINATE) {
            buttonValidate.setText("Some validated");
            buttonValidate.setStyle("-fx-text-fill: darkorange");
            symbolValidate.setSymbol("featheralerttriangle");
            symbolValidate.setSymbolColor("darkorange");
        } else {
            buttonValidate.setText("Not validated");
            buttonValidate.setStyle("-fx-text-fill: red");
            symbolValidate.setSymbol("featherxcircle");
            symbolValidate.setSymbolColor("red");
        }
    }

    private void addClassificationRow(VBox pane, ClassificationSet cls) {
        Comparator<Map.Entry<String, Classification>> compare = (e1, e2) -> Double.compare(e2.getValue().getValue(), e1.getValue().getValue());
        LinkedHashMap<String, Classification> sorted = cls.classifications.entrySet().stream().sorted(compare).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
        pane.getChildren().clear();
        int current_row = 0;
        for (Map.Entry<String, Classification> c : sorted.entrySet()) {
            String code = c.getValue().getCode();
            double score = c.getValue().getValue();
            Label label = new Label(code);
            BorderPane.setMargin(label, new Insets(0, 3, 0, 3));
            BorderPane.setAlignment(label, Pos.CENTER_LEFT);
//            ProgressBar progressBar = new ProgressBar(score);
//            progressBar.setPrefWidth(300);
            Button button = new Button();
            button.setGraphic(new SymbolLabel("featherarrowleft", 8));
            button.setStyle("-fx-font-size: 10;");
            button.getStyleClass().add("flat-button");
            button.setOnAction(event -> {
                handleSetClassFromPrediction(c.getValue());
            });
            Label labelScore = new Label(String.format("%.0f%%", score * 100));
            labelScore.setPrefWidth(30);
            BorderPane.setAlignment(labelScore, Pos.CENTER_LEFT);
            BorderPane borderPane = new BorderPane();
            borderPane.setRight(button);
            borderPane.setCenter(label);
            borderPane.setLeft(labelScore);
            pane.getChildren().add(borderPane);
            current_row++;
        }
//        pane.getColumnConstraints().get(0).setHgrow(Priority.ALWAYS);
    }

    private void updateKNNUI(ClassificationSet cls) {
        if (cls != null) {
            addClassificationRow(gridPaneKNN, cls);
            for (Map.Entry<String, ClassificationButton> b : labelButtons.entrySet()) {
                String key = b.getKey();
                ClassificationButton button = b.getValue();

                if (cls.classifications.containsKey(key)) {
                    double score = cls.classifications.get(key).getValue();
                    button.setKNNScore(score);
                    if (cls.getBestCode().equals(key)) {
                        button.setIsKNN(true);
                    } else {
                        button.setIsKNN(false);
                    }
                } else {
                    button.setKNNScore(0);
                    button.setIsKNN(false);
                }
            }
            for (Map.Entry<String, TreeItem<String>> b : labelTreeItems.entrySet()) {
                String key = b.getKey();
                SymbolLabel symbolLabel = new SymbolLabel("feathergrid", 12);
                if (cls.getBestCode().equals(key)) {
                    b.getValue().setGraphic(symbolLabel);
                }
                else {
                    b.getValue().setGraphic(null);
                }
            }
            treeView.refresh();
        } else {
            gridPaneKNN.getChildren().clear();
            gridPaneKNN.getChildren().add(new Label("N/A"));
            for (Map.Entry<String, ClassificationButton> b : labelButtons.entrySet()) {
                ClassificationButton button = b.getValue();
                button.setIsKNN(false);
            }
        }
    }

    private void updateCNNUI(ClassificationSet cls) {
        if (cls != null) {
            addClassificationRow(gridPaneCNN, cls);
            for (Map.Entry<String, ClassificationButton> b : labelButtons.entrySet()) {
                String key = b.getKey();
                ClassificationButton button = b.getValue();

                if (cls.classifications.containsKey(key)) {
                    double score = cls.classifications.get(key).getValue();
                    button.setCNNScore(score);
                    if (cls.getBestCode().equals(key)) {
                        button.setIsCNN(true);
                    } else {
                        button.setIsCNN(false);
                    }
                } else {
                    button.setCNNScore(0);
                    button.setIsCNN(false);
                }
            }
            for (Map.Entry<String, TreeItem<String>> b : labelTreeItems.entrySet()) {
                String key = b.getKey();
                SymbolLabel symbolLabel = new SymbolLabel("feathercpu", 12);
                if (cls.getBestCode().equals(key)) {
                    b.getValue().setGraphic(symbolLabel);
                }
                else {
                    b.getValue().setGraphic(null);
                }

            }
            treeView.refresh();
        } else {
            gridPaneCNN.getChildren().clear();
            gridPaneCNN.getChildren().add(new Label("N/A"));
            for (Map.Entry<String, ClassificationButton> b : labelButtons.entrySet()) {
                ClassificationButton button = b.getValue();
                button.setIsCNN(false);
            }
        }
    }

    private void updateTagUI(Particle particle) {
        for (Map.Entry<String, Button> b : tagButtons.entrySet()) {
            b.getValue().setStyle("");
        }
        if (particle != null) {
            for (String tag : particle.tags) {
                if (tagButtons.containsKey(tag)) {
                    tagButtons.get(tag).setStyle(String.format("-fx-base: derive(#0096C9,%d%%)", 0));
                } else {
                    BasicDialogs.ShowError("Tag Error",
                            String.format("The tag %s is not in the project.", tag));
                }
            }
        }
    }

    private final class ClassificationTreeCell extends TreeCell<String> {

        public ClassificationTreeCell() {
//            setContentDisplay(ContentDisplay.);

            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (labelTreeItems.containsKey(getItem())) {
                        labelsViewModel.setLabel(getItem(), 1.0, true);
                    }
                }
                else if (event.getButton() == MouseButton.SECONDARY) {
                    if (labelTreeItems.containsKey(getItem())) {
                        PopOver popOver = createLabelPopover(supervisor.project.getTaxons().get(getItem()));
                        popOver.show(this);
                    }
                }
            });
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                setGraphic(getTreeItem().getGraphic());
            }
        }
    }

    @FXML
    private void handleChangeCNNPredictionNetwork(ActionEvent event) {
        try {
            SelectNetworkViewController controller = AbstractDialogController.create(SelectNetworkViewController.class);
            controller.showEmbedded();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePredictUsingCNN(ActionEvent event) {
        cnnPredictionViewModel.predictUsingCNN(
                selectionViewModel.getCurrentParticles(),
                supervisor.project.processingInfo.getProcessBeforeClassification(),
                supervisor.project.processingInfo.getCnnThreshold());
    }

    @FXML
    private void handleSetClassFromkNNPrediction(ActionEvent event) {
        Classification cls = knnPredictionViewModel.getkNNPredictedClassification().getBest();
        labelsViewModel.setLabel(cls.getCode(), cls.getValue(), true);

    }

    private void handleSetClassFromPrediction(Classification cls) {
        labelsViewModel.setLabel(cls.getCode(), cls.getValue(), true);
    }

    @FXML
    private void handleSetClassFromCNNPrediction(ActionEvent event) {
        Classification cls = cnnPredictionViewModel.getCnnPredictedClassification().getBest();
        labelsViewModel.setLabel(cls.getCode(), cls.getValue(), true);
    }

    @FXML
    private void handlePredictUsingkNN(ActionEvent event) {
        knnPredictionViewModel.predictUsingkNN(
                selectionViewModel.getCurrentParticles(),
                supervisor.project.processingInfo.getKnnThreshold());
    }

    @FXML
    private void handleToggleLabelModeFlat(ActionEvent event) {
        displayMode.set(0);
    }

    @FXML
    private void handleToggleLabelModeCategory(ActionEvent event) {
        displayMode.set(1);
    }

    @FXML
    private void handleToggleLabelModeTree(ActionEvent event) {
        displayMode.set(2);
    }
}
