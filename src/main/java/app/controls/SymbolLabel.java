package main.java.app.controls;

import main.java.app.resources.styles.FontSymbols;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.css.*;
import javafx.scene.control.Label;

import java.util.List;

public class SymbolLabel extends Label {

    //CSS Styling
    private static final StyleablePropertyFactory<SymbolLabel> FACTORY =
            new StyleablePropertyFactory<>(Label.getClassCssMetaData());
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return FACTORY.getCssMetaData();
    }
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    //Font family
//    private static final CssMetaData<SymbolLabel, String> FONTFAMILY = FACTORY.createStringCssMetaData("font-family", s -> s.fontFamily);
//    private final StyleableStringProperty fontFamily = new SimpleStyleableStringProperty(FONTFAMILY);
//    public StringProperty fontFamilyProperty() { return fontFamily; }
//    public String getFontFamily() { return fontFamily.getValue(); }
//    public void setFontFamily(String value) {
//        fontFamily.set(value);
//    }

    //Icon literal
    private static final CssMetaData<SymbolLabel, String> SYMBOL = FACTORY.createStringCssMetaData("symbol", s -> s.symbol);
    private final StyleableStringProperty symbol = new SimpleStyleableStringProperty(SYMBOL);
    public StringProperty symbolProperty() {
        return symbol;
    }
    public String getSymbol() {
        return symbol.getValue();
    }
    public void setSymbol(String value) {
        symbol.set(value);
    }

    //Icon literal
    private static final CssMetaData<SymbolLabel, String> SYMBOLCOLOR = FACTORY.createStringCssMetaData("symbol-color", s -> s.symbolColor);
    private final StyleableStringProperty symbolColor = new SimpleStyleableStringProperty(SYMBOLCOLOR);
    public StringProperty symbolColorProperty() {
        return symbolColor;
    }
    public String getSymbolColor() {
        return symbolColor.getValue();
    }
    public void setSymbolColor(String value) {
        symbolColor.set(value);
    }

    //Icon literal
    private static final CssMetaData<SymbolLabel, String> SYMBOLFAMILY = FACTORY.createStringCssMetaData("symbol-family", s -> s.symbolFamily);
    private final StyleableStringProperty symbolFamily = new SimpleStyleableStringProperty(SYMBOLFAMILY);
    public StringProperty symbolFamilyProperty() {
        return symbolFamily;
    }
    public String getSymbolFamily() {
        return symbolFamily.getValue();
    }
    public void setSymbolFamily(String value) {
        symbolFamily.set(value);
    }

    //Icon size
    private static final CssMetaData<SymbolLabel, Number> SYMBOLSIZE = FACTORY.createSizeCssMetaData("symbol-size", s -> s.symbolSize, 12);
    private final StyleableDoubleProperty symbolSize = new SimpleStyleableDoubleProperty(SYMBOLSIZE);
    public DoubleProperty symbolSizeProperty() {
        return symbolSize;
    }
    public double getSymbolSize() {
        return symbolSize.getValue();
    }
    public void setSymbolSize(double value) {
        symbolSize.set(value);
    }

    public SymbolLabel() {
        super();
        symbol.addListener((observable, oldValue, newValue) -> {
            //Character written literally as \\u????
            if (newValue.startsWith("\\u")) {
                char c = (char) Integer.parseInt(newValue.substring(2), 16);
                this.setText(Character.toString(c));
            }
            //Character written literally as &#x????;
            else if (newValue.startsWith("&#x")) {
                char c = (char) Integer.parseInt(newValue.substring(3, 7), 16);
                this.setText(Character.toString(c));
            }
            //A symbol alias defined in FontSymbol
            else if (newValue.length() > 1) {
                try {
//                    double fontSize = this.getFont().getSize();
                    FontSymbols fontSymbol = FontSymbols.valueOf(newValue);
//                    Font font = Font.loadFont(FontSymbols.class.getResourceAsStream(fontSymbol.getFontFamily() + ".ttf"), fontSize);
//                    this.setFont(font);
                    setSymbolFamily(fontSymbol.getFontFamily());
                    this.setText(fontSymbol.getSymbol());
//                    updateStyle();
//                    this.setStyle("-fx-font-family: " + fontSymbol.getFontFamily() + ";");
                } catch (IllegalArgumentException e) {
                    this.setText(newValue);
                }
            } else {
                this.setText(newValue);
            }
        });
        symbolSize.addListener((observable, oldValue, newValue) -> {
//            Font font = this.getFont();
//            this.setFont(Font.font(font.getFamily(),(double) newValue));
            updateStyle();
        });
        symbolFamily.addListener((observable, oldValue, newValue) -> {
//            double fontSize = this.getFont().getSize();
//            Font font = Font.loadFont(FontSymbols.class.getResourceAsStream(newValue + ".ttf"), fontSize);
//            this.setFont(font);
            updateStyle();
        });
        symbolColor.addListener((observable, oldValue, newValue) -> {
            updateStyle();
        });
    }

    public SymbolLabel(String symbol, double size) {
        this();
        setSymbol(symbol);
        setSymbolSize(size);
    }

    private void updateStyle() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("-fx-font-family: '%s'; ", getSymbolFamily()));
        if (getSymbolSize() != 0.0) {
            sb.append(String.format("-fx-font-size: %.0fpx; ", getSymbolSize()));
        }
        if (getSymbolColor() != null && !getSymbolColor().equals("")) {
            sb.append(String.format("-fx-text-fill: %s; ", getSymbolColor()));
        }
        this.setStyle(sb.toString());
    }
}

