package particletrieur.viewcontrollers.network;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import particletrieur.AbstractDialogController;
import particletrieur.FxmlLocation;
import particletrieur.controls.dialogs.BasicDialogs;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@FxmlLocation("/views/network/NetworkTrainingProgressView.fxml")
public class NetworkTrainingProgressViewController extends AbstractDialogController implements Initializable {

//    @FXML
//    Label labelSubStatus;
    @FXML
    TextArea textAreaMessage;
    @FXML
    LineChart<Number, Number> lineChartTraining;
    @FXML
    Label labelStatus;

//    RunningSeries runningSeriesTrain = new RunningSeries(50, 5);
//    RunningSeries runningSeriesTest = new RunningSeries(50, 5);

    ObservableList<XYChart.Data<Number,Number>> dataTrain = FXCollections.observableArrayList();
    XYChart.Series<Number,Number> seriesTrain = new XYChart.Series<>(dataTrain);
    ObservableList<XYChart.Data<Number,Number>> dataTest = FXCollections.observableArrayList();
    XYChart.Series<Number,Number> seriesTest = new XYChart.Series<>(dataTest);

    private Service<Void> service;

    public NetworkTrainingProgressViewController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NumberAxis xAxis = (NumberAxis) lineChartTraining.getXAxis();
        xAxis.setForceZeroInRange(true);
        xAxis.setAutoRanging(true);
//        xAxis.lowerBoundProperty().bind(runningSeriesTest.xRangeLower);
//        xAxis.upperBoundProperty().bind(runningSeriesTest.xRangeUpper);
        NumberAxis yAxis = (NumberAxis) lineChartTraining.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setTickUnit(10);
        yAxis.setMinorTickVisible(false);
        yAxis.setUpperBound(100);
        yAxis.setForceZeroInRange(true);
        seriesTrain.setName("Train");
        seriesTest.setName("Test");
        lineChartTraining.getData().clear();
        lineChartTraining.getData().add(seriesTrain);
        lineChartTraining.getData().add(seriesTest);
        lineChartTraining.getXAxis().setLabel("Epochs");
        lineChartTraining.getYAxis().setLabel("Accuracy (%)");
        lineChartTraining.setCreateSymbols(false);

//        this.getDialog().setOnCloseRequest(event -> {
//            service.cancel();
//        });
    }

    public void setup(Service service) {
        this.service = service;
        service.messageProperty().addListener((observable, oldValue, newValue) -> {
            textAreaMessage.appendText(newValue);
            List<String> parts = Arrays.asList(newValue.split("\\s+"));
            if (parts.contains("Epoch:") && parts.contains("#Train:") && parts.contains("*Val:")) {
                double epoch = Double.parseDouble(parts.get(parts.indexOf("Epoch:")+1));
                String train = parts.get(parts.indexOf("#Train:")+1);
                String val = parts.get(parts.indexOf("*Val:")+1);
                train = train.substring(0, train.length()-1);
                val = val.substring(0, val.length()-1);
                double train_val = Double.parseDouble(train);
                double test_val = Double.parseDouble(val);
                dataTrain.add(new XYChart.Data<>(epoch, train_val));
                dataTest.add(new XYChart.Data<>(epoch, test_val));
                seriesTrain.setName(String.format("Train: %.1f%%", train_val));
                seriesTest.setName(String.format("Test: %.1f%%", test_val));
            }
            else if (newValue.startsWith("@")) {
                labelStatus.setText(newValue.substring(1));
//                labelSubStatus.setText("");
            }
            else {
//                labelSubStatus.setText(newValue);
            }
                //# Epoch: 381.0 Batch: 11078 #Train:  99.8% (0.0073), *Val:  75.7% (1.7194) !Prob  0.9 Time:  0.95s
        });
        service.setOnSucceeded(event -> {
            getDialog().getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
            getDialog().getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        });
        service.setOnFailed(event -> {
            labelStatus.setText("Error during training");
            getDialog().getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
            getDialog().getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            BasicDialogs.ShowException("Exception", new Exception(service.getException()));
        });
    }



    @Override
    public void processDialogResult(ButtonType buttonType) {
        service.cancel();
    }

    @Override
    public String getHeader() {
        return "Training Monitor";
    }

    @Override
    public String getSymbol() {
        return "featheractivity";
    }

    @Override
    public ButtonType[] getButtonTypes() {
        return new ButtonType[] { ButtonType.CANCEL };
    }

    @Override
    public void postDialogSetup() {
        ((Pane)getDialog().getDialogPane().getContent()).setPadding(new Insets(0));
        this.getDialog().setOnCloseRequest(event -> {
            service.cancel();
        });
    }
}

