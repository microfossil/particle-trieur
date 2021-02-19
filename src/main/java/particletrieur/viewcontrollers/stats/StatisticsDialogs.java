/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package particletrieur.viewcontrollers.stats;

import particletrieur.App;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javafx.collections.ListChangeListener;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

/**
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class StatisticsDialogs {

    public static Dialog CategoryCountDialog(
            String title,
            String description,
            String xlabel,
            String ylabel,
            LinkedHashMap<String, Integer> values) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> bc = new BarChart<String, Number>(xAxis, yAxis);
        bc.setTitle(title);
        xAxis.setLabel(xlabel);
        yAxis.setLabel(ylabel);
        XYChart.Series series1 = new XYChart.Series();
        for (Entry entry : values.entrySet()) {
            series1.getData().add(new XYChart.Data(entry.getKey(), entry.getValue()));
        }
        bc.getData().add(series1);
        bc.setLegendVisible(false);
        bc.setCategoryGap(2);
        bc.setBarGap(0);

        // Create the custom dialog.        
        Dialog dialog = new Dialog();
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(App.iconImage);
        dialog.setTitle(title);
        dialog.setHeaderText(description);
        bc.setPrefWidth(1000);
        bc.setPrefHeight(600);
        dialog.setResizable(true);

        // Set the buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        // Set the controls        
        dialog.getDialogPane().setContent(bc);
        return dialog;
    }

    public static Dialog IndexCountDialog(
            String title,
            String description,
            String xlabel,
            String ylabel,
            LinkedHashMap<Double, Integer> values) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> bc = new BarChart<String, Number>(xAxis, yAxis);
        bc.setTitle(title);
        xAxis.setLabel(xlabel);
        yAxis.setLabel(ylabel);
        XYChart.Series series1 = new XYChart.Series();
        for (Entry entry : values.entrySet()) {
            series1.getData().add(new XYChart.Data(entry.getKey().toString(), entry.getValue()));
        }
        bc.getData().add(series1);
        bc.setLegendVisible(false);
        bc.setCategoryGap(2);
        bc.setBarGap(0);

        //Create the custom dialog.
        Dialog dialog = new Dialog();
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(App.iconImage);
        dialog.setTitle(title);
        dialog.setHeaderText(description);
        bc.setPrefWidth(1000);
        bc.setPrefHeight(600);
        dialog.setResizable(true);

        //Set the buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        //Set the controls
        dialog.getDialogPane().setContent(bc);
        return dialog;
    }

    public static Dialog RelativeAbundanceDialog(
            String title,
            String description,
            String xlabel,
            String ylabel,
            LinkedHashMap<String, HashMap<Double, Double>> values) {

        CheckComboBox<String> comboBox = new CheckComboBox<>();

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        final LineChart<Number, Number> bc = new LineChart<>(xAxis, yAxis);
        bc.setTitle(title);
        xAxis.setLabel(xlabel);
        yAxis.setLabel(ylabel);
        bc.setCreateSymbols(false);

        for (Entry entry : values.entrySet()) {
            String type = (String) entry.getKey();
            comboBox.getItems().add(type);
        }

        comboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                bc.getData().clear();
                for (Entry entry : values.entrySet()) {
                    if (comboBox.getCheckModel().isChecked((String) entry.getKey())) {
                        XYChart.Series series = new XYChart.Series();
                        series.setName((String) entry.getKey());
                        HashMap<Double, Integer> points = (HashMap<Double, Integer>) entry.getValue();
                        for (Entry entry2 : points.entrySet()) {
                            series.getData().add(new XYChart.Data(entry2.getKey(), entry2.getValue()));
                        }
                        bc.getData().add(series);
                    }
                }
            }
        });

        if (comboBox.getItems().size() > 0) {
            comboBox.getCheckModel().check(0);
        }

        // Create the custom dialog.
        Dialog dialog = new Dialog();
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(App.iconImage);
        dialog.setTitle(title);
        dialog.setHeaderText(description);
        dialog.setResizable(true);

        // Set the buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        // Set the controls
        VBox vbox = new VBox();
        vbox.getChildren().add(comboBox);
        bc.setPrefWidth(1000);
        bc.setPrefHeight(600);
        vbox.getChildren().add(bc);
        dialog.getDialogPane().setContent(vbox);

        return dialog;
    }

    public static Dialog RelativeAbundanceWithCoreIDDialog(
            String title,
            String description,
            String xlabel,
            String ylabel,
            LinkedHashMap<String, LinkedHashMap<String, Double>> values) {

        CheckComboBox<String> comboBox = new CheckComboBox<>();

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final LineChart<String, Number> bc = new LineChart<>(xAxis, yAxis);
        bc.setTitle(title);
        xAxis.setLabel(xlabel);
        yAxis.setLabel(ylabel);
        bc.setCreateSymbols(false);

        for (Entry entry : values.entrySet()) {
            String type = (String) entry.getKey();
            comboBox.getItems().add(type);
        }

        comboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                bc.getData().clear();
                values.entrySet().stream().forEach(entry -> {
                    //for (Entry entry : values.entrySet()) {
                    if (comboBox.getCheckModel().isChecked((String) entry.getKey())) {
                        XYChart.Series series = new XYChart.Series();
                        series.setName((String) entry.getKey());
                        LinkedHashMap<String, Double> points = (LinkedHashMap<String, Double>) entry.getValue();
                        points.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry2 -> {
                            //for (Entry entry2 : points.entrySet()) {
                            series.getData().add(new XYChart.Data(entry2.getKey(), entry2.getValue()));
                        });
                        bc.getData().add(series);
                    }
                });
            }
        });

        if (comboBox.getItems().size() > 0) {
            comboBox.getCheckModel().check(0);
        }

        // Create the custom dialog.
        Dialog dialog = new Dialog();
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(App.iconImage);
        dialog.setTitle(title);
        dialog.setHeaderText(description);
        dialog.setResizable(true);

        // Set the buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        // Set the controls
        VBox vbox = new VBox();
        vbox.getChildren().add(comboBox);
        bc.setPrefWidth(1000);
        bc.setPrefHeight(600);
        vbox.getChildren().add(bc);
        dialog.getDialogPane().setContent(vbox);

        return dialog;
    }

    public static Dialog RelativeAbundanceStackedDialog(
            String title,
            String description,
            String xlabel,
            String ylabel,
            LinkedHashMap<String, HashMap<Double, Double>> values) {

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        final StackedAreaChart<Number, Number> bc = new StackedAreaChart<>(xAxis, yAxis);
        bc.setTitle(title);
        xAxis.setLabel(xlabel);
        yAxis.setLabel(ylabel);
        bc.setCreateSymbols(false);

        for (Entry entry : values.entrySet()) {
            XYChart.Series series = new XYChart.Series();
            series.setName((String) entry.getKey());
            HashMap<Double, Integer> points = (HashMap<Double, Integer>) entry.getValue();
            for (Entry entry2 : points.entrySet()) {
                series.getData().add(new XYChart.Data(entry2.getKey(), entry2.getValue()));
            }
            bc.getData().add(series);
        }

        // Create the custom dialog.
        Dialog dialog = new Dialog();
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(App.iconImage);
        dialog.setTitle(title);
        dialog.setHeaderText(description);
        dialog.setResizable(true);

        // Set the buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        // Set the controls
        VBox vbox = new VBox();
        bc.setPrefWidth(1000);
        bc.setPrefHeight(600);
        vbox.getChildren().add(bc);
        dialog.getDialogPane().setContent(vbox);

        return dialog;
    }
}
