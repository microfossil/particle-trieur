package ordervschaos.particletrieur.app.controls;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class CheckedRangeControl extends HBox {

    @FXML
    CheckBox checkBox;
    @FXML
    Spinner spinnerLowerBound;
    @FXML
    Spinner spinnerUpperBound;

    //Lower bound
    private final DoubleProperty lowerBound = new SimpleDoubleProperty(0);
    public double getLowerBound() {
        return lowerBound.get();
    }
    public DoubleProperty lowerBoundProperty() {
        return lowerBound;
    }
    public void setLowerBound(double lowerBound) {
        this.lowerBound.set(lowerBound);
    }

    //Upper bound
    private final DoubleProperty upperBound = new SimpleDoubleProperty(1);
    public double getUpperBound() {
        return upperBound.get();
    }
    public DoubleProperty upperBoundProperty() {
        return upperBound;
    }
    public void setUpperBound(double upperBound) {
        this.upperBound.set(upperBound);
    }

    //Lower bound
    private final DoubleProperty maxLowerBound = new SimpleDoubleProperty(0);
    public double getMaxLowerBound() {
        return maxLowerBound.get();
    }
    public DoubleProperty maxLowerBoundProperty() {
        return maxLowerBound;
    }
    public void setMaxLowerBound(double maxLowerBound) {
        this.maxLowerBound.set(maxLowerBound);
    }

    //Upper bound
    private final DoubleProperty maxUpperBound = new SimpleDoubleProperty(2);
    public double getMaxUpperBound() {
        return maxUpperBound.get();
    }
    public DoubleProperty maxUpperBoundProperty() {
        return maxUpperBound;
    }
    public void setMaxUpperBound(double maxUpperBound) {
        this.maxUpperBound.set(maxUpperBound);
    }

    //Step
    private final DoubleProperty step = new SimpleDoubleProperty(0.1);
    public double getStep() {
        return step.get();
    }
    public DoubleProperty stepProperty() {
        return step;
    }
    public void setStep(double step) {
        this.step.set(step);
    }

    //Check box
    private final StringProperty label = new SimpleStringProperty("CheckBox");
    public StringProperty labelProperty() {
        return label;
    }
    public String getLabel() {
        return label.getValue();
    }
    public void setLabel(String value) {
        label.set(value);
    }

    public CheckedRangeControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CheckedRangeControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        maxLowerBound.addListener(observable -> updateSpinners());
        maxUpperBound.addListener(observable -> updateSpinners());
        step.addListener(observable -> updateSpinners());

        updateSpinners();

        lowerBound.addListener((observable, oldValue, newValue) -> {
            spinnerLowerBound.getValueFactory().setValue(newValue);
        });
        upperBound.addListener((observable, oldValue, newValue) -> {
            spinnerUpperBound.getValueFactory().setValue(newValue);
        });
        label.addListener((observable, oldValue, newValue) -> {
            checkBox.setText(newValue);
        });

        checkBox.setText(getLabel());
    }

    private void updateSpinners() {
        spinnerLowerBound.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(getMaxLowerBound(), getMaxUpperBound(), getLowerBound(), getStep()));
        spinnerUpperBound.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(getMaxLowerBound(), getMaxUpperBound(), getUpperBound(), getStep()));
    }

    public BooleanProperty getSelectedProperty() {
        return checkBox.selectedProperty();
    }
    public boolean isSelected() {
        return checkBox.isSelected();
    }
    public void setSelected(boolean value) {
        checkBox.setSelected(value);
    }
}
