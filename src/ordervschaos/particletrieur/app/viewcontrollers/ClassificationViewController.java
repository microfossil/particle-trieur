/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.viewcontrollers;

import ordervschaos.particletrieur.app.AbstractDialogController;
import ordervschaos.particletrieur.app.controls.BasicDialogs;
import ordervschaos.particletrieur.app.controls.ClassificationButton;
import ordervschaos.particletrieur.app.controls.SymbolLabel;
import ordervschaos.particletrieur.app.models.project.Project;
import ordervschaos.particletrieur.app.models.Supervisor;
import ordervschaos.particletrieur.app.models.network.classification.Classification;
import ordervschaos.particletrieur.app.models.network.classification.ClassificationSet;
import ordervschaos.particletrieur.app.models.network.classification.NetworkInfo;
import ordervschaos.particletrieur.app.models.project.Particle;
import ordervschaos.particletrieur.app.models.project.Tag;
import ordervschaos.particletrieur.app.models.project.Taxon;
import ordervschaos.particletrieur.app.viewcontrollers.label.EditLabelViewController;
import ordervschaos.particletrieur.app.viewcontrollers.network.SelectNetworkViewController;
import ordervschaos.particletrieur.app.viewcontrollers.tag.EditTagViewController;
import ordervschaos.particletrieur.app.viewmodels.*;
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
import ordervschaos.particletrieur.app.viewmodels.network.CNNPredictionViewModel;
import ordervschaos.particletrieur.app.viewmodels.network.KNNPredictionViewModel;
import ordervschaos.particletrieur.app.viewmodels.particles.LabelsViewModel;
import ordervschaos.particletrieur.app.viewmodels.particles.TagsViewModel;
import ordervschaos.particletrieur.app.viewmodels.network.NetworkViewModel;
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
    CheckBox checkBoxAutoValidate;
    @FXML
    CheckBox checkBoxAutoAdvance;
    @FXML
    GridPane gridPaneCNN;
    @FXML
    GridPane gridPaneKNN;
    @FXML
    HBox hboxCNN;
    @FXML
    SymbolLabel symbolLabelDeepCNNRefresh;
    //    @FXML
//    ToggleSwitch toggleSwitchAutoAdvance;
    @FXML
    CheckBox checkBoxPreprocessBeforeClassification;
    @FXML
    Spinner spinnerCNNThreshold;
    @FXML
    Spinner spinnerKNNThreshold;
    //    @FXML
//    Rating ratingImage;
    @FXML
    FlowPane flowPaneTags;
    @FXML
    VBox vboxClasses;
    @FXML
    public Label labelNetwork;

    @FXML
    Button buttonkNNLabel;
    @FXML
    ProgressBar progressBarkNNScore;
    @FXML
    Button buttonCNNLabel;
    @FXML
    ProgressBar progressBarCNNScore;

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
    Map<String, Button> tagButtons = new HashMap<>();
    RotateTransition rotateTransitionSymbolLabelDeepCNN;
    public ArrayList<String> taxonCodes = new ArrayList<>();

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
        spinnerCNNThreshold.getValueFactory().valueProperty().bindBidirectional(cnnPredictionViewModel.cnnThresholdProperty());
        spinnerKNNThreshold.getValueFactory().valueProperty().bindBidirectional(knnPredictionViewModel.knnThresholdProperty());

        //Current particle updated
        selectionViewModel.currentParticleProperty().addListener((observable, oldValue, newValue) -> {
            updateClassificationUI(newValue);
            updateTagUI(newValue);
        });

        //Network updated
        supervisor.project.networkDefinitionProperty().addListener(listener -> {
            setupNetworkUI(supervisor.project.getNetworkDefinition());
        });

        //Updated events
        supervisor.project.taxonsUpdatedEvent.addListener(listener -> {
            setupClassificationUI(supervisor.project, 1);
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

        setupClassificationUI(supervisor.project, 1);
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
        gridPaneCNN.addRow(0, new Label("N/A"));
        supervisor.network.enabledProperty().addListener((observable, oldValue, newValue) -> {
            gridPaneCNN.getChildren().clear();
            if (newValue) gridPaneCNN.addRow(0, new Label("Ready"));
            else gridPaneCNN.addRow(0, new Label("N/A"));
        });

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
    }

    private void setupClassificationUI(Project project, int mode) {
        if (mode == 0) {
            taxonCodes.clear();
            labelButtons.clear();
            vboxClasses.getChildren().clear();

            ArrayList<Taxon> classList = new ArrayList<>();
            ArrayList<Taxon> nonclassList = new ArrayList<>();
            for (Map.Entry<String, Taxon> entry : project.taxons.entrySet()) {
                Taxon taxon = entry.getValue();
                final String code = taxon.getCode();
                if (taxon.getIsClass()) classList.add(taxon);
                else nonclassList.add(taxon);
            }

            addButtonGroup("Classes", classList);
            addButtonGroup("Non-classes", nonclassList);
        }
        if (mode == 1) {
            //Group the taxons by the start of their code
            LinkedHashMap<String, ArrayList<Taxon>> rawTaxonMap = new LinkedHashMap<>();
            ArrayList<Taxon> nonTaxonList = new ArrayList<>();
            for (Map.Entry<String, Taxon> entry : project.taxons.entrySet()) {
                Taxon taxon = entry.getValue();
                final String code = taxon.getCode();
                if (!taxon.getIsClass()) {
                    nonTaxonList.add(taxon);
                    continue;
                }
                String[] parts = code.split("[-_ ]+");
                ArrayList<Taxon> list = rawTaxonMap.getOrDefault(parts[0], new ArrayList<>());
                list.add(taxon);
                rawTaxonMap.putIfAbsent(parts[0], list);
            }
            // Sort by key
            LinkedHashMap<String, ArrayList<Taxon>> taxonMap =
                    rawTaxonMap.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByKey())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));

            //Add
            taxonCodes.clear();
            labelButtons.clear();
            vboxClasses.getChildren().clear();
            ArrayList<Taxon> otherList = new ArrayList<>();

            for (Map.Entry<String, ArrayList<Taxon>> group : taxonMap.entrySet()) {

                if (group.getValue().size() == 1) {
                    otherList.add(group.getValue().get(0));
                    continue;
                }

                addButtonGroup(group.getKey(), group.getValue());

            }
            addButtonGroup("Other", otherList);
            addButtonGroup("Non-classes", nonTaxonList);
        }
    }

    private void addButtonGroup(String name, List<Taxon> list) {
        Label label = new Label(name);
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(7);
        flowPane.setVgap(7);
        for (Taxon taxon : list) {
            final String code = taxon.getCode();
            //Create a button
            ClassificationButton button = new ClassificationButton(code);
            button.getButton().setMinWidth(80);
            //Change the particle label when the button is clicked
            button.getButton().addEventHandler(MouseEvent.MOUSE_CLICKED, (event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    labelsViewModel.setLabel(code, 1.0, true);
                    if (checkBoxAutoAdvance.isSelected()) {
                        selectionViewModel.nextImageRequested.broadcast(true);
                    }
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
        buttonAddNewLabel.setMinWidth(80);
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
    }

    private void setupTagUI(Project project) {
        flowPaneTags.getChildren().clear();
        tagButtons.clear();

        //Generate tag buttons
        for (Map.Entry<String, Tag> entry : project.getTags().entrySet()) {
            Tag tag = entry.getValue();
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
        buttonAddNewTag.setMinWidth(80);
        buttonAddNewTag.getStyleClass().add("flat-button");
        buttonAddNewTag.setText("Add");
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
            if (taxon != null) controller.setData(taxon);
            controller.showAndWait();
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
            controller.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showEditTagDialog(Tag tag) {
        try {
            EditTagViewController controller = AbstractDialogController.create(EditTagViewController.class);
            if (tag != null) controller.setData(tag);
            controller.showAndWait();
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
        Slider infoSlider = new Slider();
        infoSlider.setMin(0.0);
        infoSlider.setMax(1.0);
        infoSlider.setMajorTickUnit(0.1);
        infoSlider.setMinorTickCount(0);
        infoSlider.setShowTickMarks(true);
        infoSlider.setShowTickLabels(true);
        infoSlider.setMinWidth(200);
        infoVBox.getChildren().add(infoGridPane);
        infoVBox.getChildren().add(infoSlider);
        if (!Arrays.asList(supervisor.project.requiredTaxons).contains(taxon.getCode())) {
            HBox buttons = new HBox();
            Button editButton = new Button("Edit");
            editButton.setGraphic(new SymbolLabel("featheredit2", 13));
            editButton.addEventHandler(ActionEvent.ACTION, (event -> {
                showEditLabelDialog(taxon);
            }));
            Button deleteButton = new Button("Delete");
            deleteButton.setGraphic(new SymbolLabel("feathertrash2", 13));
            deleteButton.addEventHandler(ActionEvent.ACTION, (event -> {
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
        infoSlider.setStyle("-fx-base: #ececec;");

        //Event for when popup is opened
        popOver.setOnShowing(event -> {
            if (selectionViewModel.getCurrentParticle() != null) {
                infoSlider.setValue(selectionViewModel.getCurrentParticle().getScore(code));
            }
        });

        //Event for when slider is moved
        infoSlider.valueProperty().addListener(event -> {
            labelsViewModel.setLabel(code, infoSlider.getValue(), false);
        });

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
            LabelsViewModel.ValidationState state = labelsViewModel.getValidationState();
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
        } else {
            for (Map.Entry<String, ClassificationButton> b : labelButtons.entrySet()) {
                b.getValue().setIsHighlighted(false);
            }
        }
    }

    private void addClassificationRow(GridPane pane, ClassificationSet cls) {
        Comparator<Map.Entry<String, Classification>> compare = (e1, e2) -> Double.compare(e2.getValue().getValue(), e1.getValue().getValue());
        LinkedHashMap<String, Classification> sorted = cls.classifications.entrySet().stream().sorted(compare).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
        pane.getChildren().clear();
        int current_row = 0;
        for (Map.Entry<String, Classification> c : sorted.entrySet()) {
            String code = c.getValue().getCode();
            double score = c.getValue().getValue();
            Label label = new Label(code);
            ProgressBar progressBar = new ProgressBar(score);
            Button button = new Button();
            button.setGraphic(new SymbolLabel("featherarrowleft", 8));
            button.setStyle("-fx-font-size: 10;");
            button.getStyleClass().add("flat-button");
            button.setOnAction(event -> {
                handleSetClassFromPrediction(c.getValue());
            });
            pane.addRow(current_row, label, progressBar, button);
            current_row++;
        }
        pane.getColumnConstraints().get(0).setHgrow(Priority.ALWAYS);
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
        } else {
            gridPaneKNN.getChildren().clear();
            gridPaneKNN.addRow(0, new Label("N/A"));
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
        } else {
            gridPaneCNN.getChildren().clear();
            gridPaneCNN.addRow(0, new Label("N/A"));
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

    @FXML
    private void handleChangeCNNPredictionNetwork(ActionEvent event) {
        try {
            SelectNetworkViewController controller = AbstractDialogController.create(SelectNetworkViewController.class);
            controller.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePredictUsingCNN(ActionEvent event) {
        cnnPredictionViewModel.predictUsingCNN(
                selectionViewModel.getCurrentParticles(),
                supervisor.project.processingInfo.getProcessBeforeClassification());
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
        knnPredictionViewModel.predictUsingkNN(selectionViewModel.getCurrentParticles());
    }

    @FXML
    private void handleToggleLabelModeFlat(ActionEvent event) {
        setupClassificationUI(supervisor.project, 0);
    }

    @FXML
    private void handleToggleLabelModeCategory(ActionEvent event) {
        setupClassificationUI(supervisor.project, 1);
    }
}
