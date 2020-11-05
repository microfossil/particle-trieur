package ordervschaos.particletrieur.app.controls;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.css.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StatusLabel2 extends Label {

    @FXML
    SymbolLabel icon;

    RotateTransition rt;

    private Status status = Status.OK;
    private String message = "";

    //CSS Styling
    private static final StyleablePropertyFactory<StatusLabel2> FACTORY =
            new StyleablePropertyFactory<>(Label.getClassCssMetaData());
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return FACTORY.getCssMetaData();
    }
    @Override public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    //Text size
//    private static final CssMetaData<StatusLabel2, Number> TEXT_SIZE = FACTORY.createSizeCssMetaData("-text-size", s -> s.textSize, 24);
//    private final StyleableDoubleProperty textSize = new SimpleStyleableDoubleProperty(TEXT_SIZE);
//    public DoubleProperty textSizeProperty() { return textSize; }
//    public double getTextSize() { return textSize.getValue(); }
//    public void setTextSize(double value) {
//        textSize.set(value);
//    }

    //Icon size
    private static final CssMetaData<StatusLabel2, Number> ICON_SIZE = FACTORY.createSizeCssMetaData("-icon-size", s -> s.iconSize, 24);
    private final StyleableDoubleProperty iconSize = new SimpleStyleableDoubleProperty(ICON_SIZE);
    public DoubleProperty iconSizeProperty() { return iconSize; }
    public double getIconSize() { return iconSize.getValue(); }
    public void setIconSize(double value) {
        iconSize.set(value);
    }

    //Invert colours
    private static final CssMetaData<StatusLabel2, Boolean> INVERT = FACTORY.createBooleanCssMetaData("-invert", s -> s.invert, false);
    private final StyleableBooleanProperty invert = new SimpleStyleableBooleanProperty(INVERT);
    public BooleanProperty invertProperty() { return invert; }
    public boolean getInvert() { return invert.getValue(); }
    public void setInvert(boolean value) {
        invert.set(value);
    }


    public StatusLabel2() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("StatusLabel2.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        rt = new RotateTransition(Duration.millis(3000), icon);
        rt.setByAngle(360);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.setCycleCount(Animation.INDEFINITE);
        setStatus(Status.OK, "Default message");

        iconSize.addListener((observable, oldValue, newValue) -> {
            icon.setSymbolSize((double)newValue);
//            icon.setStyle("-fx-font-size: " + newValue);
//            Font font = icon.getFont();
//            icon.setFont(Font.font(font.getFamily(),newValue.doubleValue()));
        });
        invert.addListener((observable, oldValue, newValue) -> {
            this.setStatus(this.status, this.message);
        });
//        textSize.addListener((observable, oldValue, newValue) -> {
//            this.setStyle("-fx-font-size: " + newValue);
//            Font font = this.getFont();
//            this.setFont(Font.font("Feather",newValue.doubleValue()));
//        });
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setStatus(Status status, String message) {
        this.status = status;
        this.message = message;
        this.setText(message);
        switch (status) {
            case OK:
                icon.setCache(false);
                icon.setSymbol("feathercheckcircle");
                if (getInvert()) {
                    this.setBackground(new Background(new BackgroundFill(Color.DARKGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                    icon.setTextFill(Color.WHITE);
                    setTextFill(Color.WHITE);
                }
                else {
                    this.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                    icon.setTextFill(Color.DARKGREEN);
                    setTextFill(Color.DARKGREEN);
                }
                rt.stop();
                icon.setRotate(0);
                break;
            case Error:
                icon.setCache(false);
                rt.setByAngle(0);
                icon.setSymbol("featherxcircle");
                if (getInvert()) {
                    this.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                    icon.setTextFill(Color.WHITE);
                    setTextFill(Color.WHITE);
                }
                else {
                    this.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                    icon.setTextFill(Color.RED);
                    setTextFill(Color.RED);
                }
                rt.stop();
                icon.setRotate(0);
                break;
            case InProgress:
                rt.setByAngle(360);
                icon.setSymbol("featherrefreshcw");
                if (getInvert()) {
                    this.setBackground(new Background(new BackgroundFill(Color.DARKORANGE, CornerRadii.EMPTY, Insets.EMPTY)));
                    icon.setTextFill(Color.WHITE);
                    setTextFill(Color.WHITE);
                }
                else {
                    this.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                    icon.setTextFill(Color.DARKORANGE);
                    setTextFill(Color.DARKORANGE);
                }
                icon.setCache(true);
                icon.setCacheHint(CacheHint.SPEED);
                rt.play();
                break;
            case Warning:
                icon.setCache(false);
                rt.setByAngle(0);
                icon.setSymbol("featheralerttriangle");
                if (getInvert()) {
                    this.setBackground(new Background(new BackgroundFill(Color.DARKORANGE, CornerRadii.EMPTY, Insets.EMPTY)));
                    icon.setTextFill(Color.WHITE);
                    setTextFill(Color.WHITE);
                }
                else {
                    this.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                    icon.setTextFill(Color.DARKORANGE);
                    setTextFill(Color.DARKORANGE);
                }
                icon.setRotate(0);
                break;
        }
    }
}

