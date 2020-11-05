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
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import org.controlsfx.control.PopOver;
import org.controlsfx.control.ToggleSwitch;

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
public class ClassificationPaneController implements Initializable {

    @FXML GridPane gridPaneCNN;
    @FXML GridPane gridPaneKNN;
    @FXML HBox hboxCNN;
    @FXML
    SymbolLabel symbolLabelDeepCNNRefresh;
    @FXML
    ToggleSwitch toggleSwitchAutoAdvance;
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
    PredictionViewModel predictionViewModel;
    @Inject
    LabelsViewModel labelsViewModel;

    //Buttons for classification and tagging
    Map<String, StackPane> labelStackPanes = new HashMap<>();

    Map<String, ClassificationButton> labelButtons = new HashMap<>();

    Map<String, Pane> labelKNNRegions = new HashMap<>();
    Map<String, Pane> labelCNNRegions = new HashMap<>();

    Map<String, Button> tagButtons = new HashMap<>();

    RotateTransition rotateTransitionSymbolLabelDeepCNN;

    public ArrayList<String> taxonCodes = new ArrayList<>();

    //Properties for rest of UI
    public BooleanProperty autoAdvance = new SimpleBooleanProperty(true);

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        rotateTransitionSymbolLabelDeepCNN = new RotateTransition(Duration.millis(3000), symbolLabelDeepCNNRefresh);
        rotateTransitionSymbolLabelDeepCNN.setByAngle(360);
        rotateTransitionSymbolLabelDeepCNN.setInterpolator(Interpolator.LINEAR);
        rotateTransitionSymbolLabelDeepCNN.setCycleCount(Animation.INDEFINITE);
        symbolLabelDeepCNNRefresh.setCache(true);
        symbolLabelDeepCNNRefresh.setCacheHint(CacheHint.SPEED);
        //rotateTransitionSymbolLabelDeepCNN.play();

        //Controls
        checkBoxPreprocessBeforeClassification.selectedProperty().bindBidirectional(supervisor.project.processingInfo.processBeforeClassificationProperty());

//        hboxCNN.disableProperty().bind(Bindings.not(supervisor.network.enabledProperty()));
//        gridPaneCnn.disableProperty().bind(Bindings.not(supervisor.network.enabledProperty()));

        spinnerCNNThreshold.getValueFactory().valueProperty().bindBidirectional(predictionViewModel.cnnThresholdProperty());
        spinnerKNNThreshold.getValueFactory().valueProperty().bindBidirectional(predictionViewModel.knnThresholdProperty());

        //Current particle
        selectionViewModel.currentParticleProperty().addListener((observable, oldValue, newValue) -> {
            //if (newValue != null) {
                updateClassificationUI(newValue);
                updateTagUI(newValue);
            //}
        });

        //Events
        supervisor.project.taxonsUpdatedEvent.addListener(listener -> {
            setupClassificationUI(supervisor.project);
            updateClassificationUI(selectionViewModel.getCurrentParticle());
            selectionViewModel.refreshPredictions();
        });
        supervisor.project.tagsUpdatedEvent.addListener(listener -> {
            setupTagUI(supervisor.project);
            updateTagUI(selectionViewModel.getCurrentParticle());
        });
        supervisor.project.networkDefinitionProperty().addListener(listener -> {
            setupNetworkUI(supervisor.project.getNetworkDefinition());
        });
        selectionViewModel.currentParticleUpdatedEvent.addListener(listener -> {
            updateClassificationUI(selectionViewModel.getCurrentParticle());
            updateTagUI(selectionViewModel.getCurrentParticle());
        });

        setupClassificationUI(supervisor.project);
        setupTagUI(supervisor.project);

        selectionViewModel.knnPredictionViewModel.kNNPredictedClassificationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.classifications.size() == 0) {
                //gridPaneKnn.setDisable(true);
                buttonkNNLabel.setText("N/A");
                updateKNNUI(null);
                progressBarkNNScore.setProgress(0);
            } else {
                //gridPaneKnn.setDisable(false);
                updateKNNUI(newValue);
                buttonkNNLabel.setText(newValue.getBestCode());
                progressBarkNNScore.setProgress(newValue.getBest().getValue());
            }
        });
        selectionViewModel.cnnPredictionViewModel.cnnPredictedClassificationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.classifications.size() == 0) {
                //gridPaneCnn.setDisable(true);
                buttonCNNLabel.setText("N/A");
                updateCNNUI(null);
                progressBarCNNScore.setProgress(0);
            } else {
                //gridPaneCnn.setDisable(false);
                updateCNNUI(newValue);
                buttonCNNLabel.setText(newValue.getBest().getCode());
                progressBarCNNScore.setProgress(newValue.getBest().getValue());
            }
        });
        selectionViewModel.cnnPredictionViewModel.runningProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                symbolLabelDeepCNNRefresh.setVisible(true);
                rotateTransitionSymbolLabelDeepCNN.play();
            }
            else {
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
    }

    private void setupClassificationUI(Project project) {

        taxonCodes.clear();
        labelButtons.clear();
//        labelKNNRegions.clear();
        vboxClasses.getChildren().clear();

        Label labelClasses = new Label("Classes:");
        FlowPane labelFlowPane = new FlowPane();
        labelFlowPane.setHgap(7);
        labelFlowPane.setVgap(7);

        Label labelRequiredClasses = new Label("Non-classes:");
        FlowPane requiredFlowPane = new FlowPane();
        requiredFlowPane.setHgap(7);
        requiredFlowPane.setVgap(7);
//        requiredFlowPane.setPadding(new Insets(7, 0, 0, 0));

        vboxClasses.getChildren().addAll(
                labelClasses,
                labelFlowPane,
                labelRequiredClasses,
                requiredFlowPane
        );

        for (Map.Entry<String, Taxon> entry : project.taxons.entrySet()) {
            Taxon taxon = entry.getValue();
            final String code = taxon.getCode();

            //Create a button
//            StackPane stackPane = new StackPane();
//            Button button = new Button(code);
            ClassificationButton button = new ClassificationButton(code);
            button.getButton().setMinWidth(80);

//            Pane knnRegion = new Pane();
//            knnRegion.setMaxWidth(5);
//            knnRegion.setStyle("-fx-opacity: 0.0;");
//            stackPane.getChildren().add(button);
//            stackPane.getChildren().add(knnRegion);
//            StackPane.setAlignment(knnRegion, Pos.CENTER_RIGHT);

            //Change the particle label when the button is clicked
            //Event when the button is clicked
            button.getButton().addEventHandler(MouseEvent.MOUSE_CLICKED, (event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    labelsViewModel.setLabel(code, 1.0, true);
                    if (toggleSwitchAutoAdvance.isSelected()) {
                        selectionViewModel.nextImageRequested.broadcast(true);
                    }
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    PopOver popOver = createLabelPopover(taxon);
                    popOver.show(button);
                }
            }));

            //Add it to the appropriate vBox
            if (!taxon.getIsClass() || taxon.getGroup().equalsIgnoreCase("other")) {
                requiredFlowPane.getChildren().add(button);
            } else {
                labelFlowPane.getChildren().add(button);
            }

            //Put button in dictionary so we can modify it later.
            labelButtons.put(code, button);
//            labelStackPanes.put(code, stackPane);
//            labelKNNRegions.put(code, knnRegion);
        }

        //Button to add a new label
        Button buttonAddNewLabel = new Button();
        buttonAddNewLabel.setMinWidth(80);
        buttonAddNewLabel.getStyleClass().add("flat-button");
        buttonAddNewLabel.setText("Add");
        buttonAddNewLabel.setGraphic(new SymbolLabel("featherplus", 12));
        buttonAddNewLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            //App.blur(true);
            showEditLabelDialog(null);
            //App.blur(false);
        });
        labelFlowPane.getChildren().add(buttonAddNewLabel);
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
            //App.blur(true);
            showEditTagDialog(null);
            //App.blur(false);
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
        }

        else {
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
        }
        else {
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
        }
        else {
            for (Map.Entry<String, ClassificationButton> b : labelButtons.entrySet()) {
                b.getValue().setIsHighlighted(false);
            }
        }
//        if (Map.Entry<String, Button> b : labelButtons.entrySet()) {
//            b.getValue().setStyle("");
//            if (removeDecorations) {
//                StackPane stackPane = labelStackPanes.get(b.getKey());
//                ArrayList<Node> toRemove = new ArrayList<>();
//                for (Node node : stackPane.getChildren()) {
//                    if (node.getClass() == SymbolLabel.class) {
//                        toRemove.add(node);
//                    }
//                }
//                stackPane.getChildren().removeAll(toRemove);
////                if (stackPane.getChildren().size() > 1)
////                    stackPane.getChildren().remove(1, stackPane.getChildren().size());
//            }
//        }
//        if (particle != null) {
//            for (Classification cl : particle.getClassificationsAsList()) {
//                String code = cl.getCode();
//                if (labelButtons.containsKey(code)) {
//                    if (cl.getValue() > 0.01) {
//                        Button b = labelButtons.get(code);
//                        if (code.equalsIgnoreCase(particle.classification.get())) {
//                            b.setStyle(String.format("-fx-base: derive(#0096C9,%d%%)", (int) (100 - cl.getValue() * 100)));
//                        } else {
//                            b.setStyle(String.format("-fx-base: derive(#FF3355,%d%%)", (int) (100 - cl.getValue() * 100)));
//                        }
//                        //styleButton(b, cl.getValue());
//
////                        b.setStyle(String.format("-fx-base: derive(#0096C9,%d%%);"
////                                        + "-fx-inner-border: derive(#0096C9,100%%); "
////                                        + "-fx-background-insets: 0 0 -1 0, 0, 1, 1 %d 1 1;",
////                                (int) (50), 1 + (int) ((b.getWidth() - 2) * (1.0 - cl.getValue()))
////                        ));
//                    }
//                }
//            }
//        }
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
        }
        else {
            gridPaneKNN.getChildren().clear();
            gridPaneKNN.addRow(0, new Label("N/A"));
            for (Map.Entry<String, ClassificationButton> b : labelButtons.entrySet()) {
                ClassificationButton button = b.getValue();
                button.setIsKNN(false);
            }
        }
//        for (Map.Entry<String, Pane> region : labelKNNRegions.entrySet()) {
//
//        }
//        if (labelStackPanes.containsKey(cls.getCode())) {
//            StackPane stackPane = labelStackPanes.get(cls.getCode());
//            SymbolLabel symbolLabel = new SymbolLabel("feathergrid", 12);
//            symbolLabel.getStyleClass().add("button-decoration");
//            stackPane.getChildren().add(symbolLabel);
//            stackPane.setAlignment(symbolLabel, Pos.TOP_LEFT);
//            if (stackPane.getChildren().size() < 3) {
//                stackPane.setMargin(symbolLabel, new Insets(-4, 0, 0, -4));
//            } else {
//                stackPane.setMargin(symbolLabel, new Insets(-4, 0, 0, 12));
//            }
//        }
//        for (Map.Entry<String, Pane> region : labelKNNRegions.entrySet()) {
//            if (region.getKey().equals(cls.getCode())) {
//                region.getValue().setPrefWidth(labelButtons.get(cls.getCode()).getWidth());
//                region.getValue().setStyle(String.format(
//                        "-fx-background-insets: 0 %d 0 0;" +
//                                "-fx-background-color: blue;" +
//                                "-fx-background-radius: 2, 0, 0, 2;" +
//                                "-fx-opacity: 0.3;",
//                        (int) (labelButtons.get(cls.getCode()).getWidth() * (1.0 - cls.getValue()))));
//            }
//            else {
//                region.getValue().setStyle("-fx-opacity: 0.0");
//            }
//        }
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
        }
        else {
            gridPaneCNN.getChildren().clear();
            gridPaneCNN.addRow(0, new Label("N/A"));
            for (Map.Entry<String, ClassificationButton> b : labelButtons.entrySet()) {
                ClassificationButton button = b.getValue();
                button.setIsCNN(false);
            }
        }
//        if (labelStackPanes.containsKey(code)) {
//            StackPane stackPane = labelStackPanes.get(code);
//            SymbolLabel symbolLabel = new SymbolLabel("feathercpu", 12);
//            symbolLabel.getStyleClass().add("button-decoration");
//            stackPane.getChildren().add(symbolLabel);
//            stackPane.setAlignment(symbolLabel, Pos.TOP_LEFT);
//            if (stackPane.getChildren().size() < 3) {
//                stackPane.setMargin(symbolLabel, new Insets(-4, 0, 0, -4));
//            } else {
//                stackPane.setMargin(symbolLabel, new Insets(-4, 0, 0, 12));
//            }
//        }
    }

//    private void styleButton(Button button, double fraction) {
//        //String style = String.format("-fx-background-insets: 0 0 -1 0, 0, 1, 2 %d 2 2;", 1 + (int) ((button.getWidth() - 2) * (1.0 - fraction)));
//        //button.setStyle(style);
//        if (fraction > 0.01) {
//            SymbolLabel symbolLabel = new SymbolLabel("feathercircle",8);
//            symbolLabel.setStyle("-fx-text-fill: green");
//            button.setGraphic(symbolLabel);
//        }
//        else {
//            button.setGraphic(null);
//        }
//    }

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
                            String.format("The tag %s is not in the app.", tag));
                }
            }
        }
    }


//    private void updateRatingUI(Particle particle) {
//        //ratingImage.setRating(particle.getImageQuality());
//    }


//    @FXML
//    private void handleRating(MouseEvent event) {
//        supervisor.project.setParticleQuality(selectionViewModel.getCurrentParticles(), (int) ratingImage.getRating());
//    }


    @FXML
    private void handleChangeCNNPredictionNetwork(ActionEvent event) {
        try {
            SelectNetworkViewController controller = AbstractDialogController.create(SelectNetworkViewController.class);
            controller.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
//
//        SelectNetworkViewController dialog = new SelectNetworkViewController();
//        dialog.setData(supervisor.project.getNetworkDefinition());
//        Optional<NetworkInfo> result = dialog.showAndWait();
//        if (!result.isPresent()) {
//            return;
//        }
//        supervisor.project.setNetworkDefinition(result.get());
    }

    @FXML
    private void handlePredictUsingCNN(ActionEvent event) {
        predictionViewModel.predictUsingCNN(
                selectionViewModel.getCurrentParticles(),
                supervisor.project.processingInfo.getProcessBeforeClassification());
        selectionViewModel.currentParticleUpdatedEvent.broadcast(null);
    }

    @FXML
    private void handleSetClassFromkNNPrediction(ActionEvent event) {
        Classification cls = selectionViewModel.knnPredictionViewModel.getkNNPredictedClassification().getBest();
        labelsViewModel.setLabel(cls.getCode(), cls.getValue(), true);
        if (toggleSwitchAutoAdvance.isSelected()) {
            selectionViewModel.nextImageRequested.broadcast(true);
        }
    }

    private void handleSetClassFromPrediction(Classification cls) {
        labelsViewModel.setLabel(cls.getCode(), cls.getValue(), true);
        if (toggleSwitchAutoAdvance.isSelected()) {
            selectionViewModel.nextImageRequested.broadcast(true);
        }
    }

    @FXML
    private void handleSetClassFromCNNPrediction(ActionEvent event) {
        Classification cls = selectionViewModel.cnnPredictionViewModel.getCnnPredictedClassification().getBest();
        labelsViewModel.setLabel(cls.getCode(), cls.getValue(), true);
        if (toggleSwitchAutoAdvance.isSelected()) {
            selectionViewModel.nextImageRequested.broadcast(true);
        }
    }

    @FXML
    private void handlePredictUsingkNN(ActionEvent event) {
        predictionViewModel.predictUsingkNN(selectionViewModel.getCurrentParticles());
    }
}
