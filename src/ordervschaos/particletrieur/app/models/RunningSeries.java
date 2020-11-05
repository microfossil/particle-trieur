package ordervschaos.particletrieur.app.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

public class RunningSeries {

    private double maximumRange = 100;
    private double currentValue = 0;
    private double buffer = 5;

    ObservableList<XYChart.Data<Number,Number>> data = FXCollections.observableArrayList();
    XYChart.Series<Number,Number> series = new XYChart.Series<>(data);

    public DoubleProperty xRangeLower = new SimpleDoubleProperty();
    public DoubleProperty xRangeUpper = new SimpleDoubleProperty();

    public XYChart.Series<Number,Number> getSeries() {
        return series;
    }

    public RunningSeries(double maximumRange, double buffer) {
        this.maximumRange = maximumRange;
        xRangeLower.set(0);
        xRangeUpper.set(maximumRange+buffer);
    }

    public void add(double x, Number y) {
        //TODO make sure the bottom range is working properly
        data.add(new XYChart.Data<>(x, y));
        if (x > xRangeUpper.get() - buffer) {
            data.remove(0);
            xRangeUpper.set(x + buffer);
            xRangeLower.set(x - maximumRange);
        }
        currentValue = x;
    }

    public void add(Number y) {
        add(currentValue + 1, y);
    }

    public void reset() {
        data.clear();
        currentValue = 0;
        xRangeLower.set(0);
        xRangeUpper.set(maximumRange+buffer);
    }
}
