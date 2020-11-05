/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordervschaos.particletrieur.app.controls;

import java.io.IOException;
import java.util.HashMap;

import ordervschaos.particletrieur.app.helpers.SimpleExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.CustomTextField;
//import org.kordamp.ikonli.javafx.FontIcon;

/**
 * FXML Controller class
 *
 * @author Ross Marchant <ross.g.marchant@gmail.com>
 */
public class ExpressionBuilderControl extends VBox {

    @FXML private ComboBox<String> comboBoxParameter;
    @FXML private ComboBox<String> comboBoxLength;
    @FXML private Button buttonAdd;
    @FXML private CustomTextField textFieldPattern;
    @FXML private Label labelText;
    @FXML private Label labelMatchedText;
    @FXML private Label labelPartsText;

    private final String[] lengthStrings = {"Any", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
//    private final String skippable = "[a-zA-Z0-9\\[\\\\\\^\\$\\.\\|\\?\\*\\+\\(\\)\\{\\}\\_\\-\\~]";
    private int oldCaretPosition = 0;

    public SimpleExpression simpleExpression = new SimpleExpression();
    
    public StringProperty textProperty = new SimpleStringProperty("");
    public String getText() { return textProperty.get(); }
    public void setDisplayText(String value) {
        textProperty.set(value);
        refresh();
    }
    
    public String getSimpleRegex() { return textFieldPattern.getText(); }    
    public void setSimpleRegex(String value) { 
        textFieldPattern.setText(value); 
        refresh(); 
    }

    private ObservableList<String> parameters = FXCollections.observableArrayList();
    public String[] getParameters() { return parameters.toArray(new String[parameters.size()]); }
    public void setParameters(String[] values) {
        parameters.clear();
        parameters.addAll(values);
        comboBoxParameter.getSelectionModel().selectFirst();
    }
        
//    FontIcon badIcon;
//    FontIcon goodIcon;
    SymbolLabel badIcon;
    SymbolLabel goodIcon;

    public ExpressionBuilderControl() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ExpressionBuilderView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.load();

        textFieldPattern.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                oldCaretPosition = textFieldPattern.getCaretPosition();
            }
        });

        comboBoxLength.getItems().addAll(lengthStrings);
        comboBoxLength.getSelectionModel().selectFirst();

        labelText.textProperty().bind(textProperty);

        badIcon = new SymbolLabel("featherxcircle", 16);
        badIcon.setSymbolColor("red");
        badIcon = new SymbolLabel("feathercheckcircle", 16);
        badIcon.setSymbolColor("green");

//        badIcon = new FontIcon("fth-circle-cross:16:RED");
//        goodIcon = new FontIcon("fth-circle-check:16:GREEN");

        comboBoxParameter.setItems(parameters);

        textFieldPattern.textProperty().addListener((observable, oldValue, newValue) -> {
            refresh();
        });
    }
    
    public void refresh() {
        simpleExpression.setSimpleRegex(textFieldPattern.getText());
        String input = getText();
        if (!input.isEmpty() && simpleExpression.canMatch(input)) {
            textFieldPattern.setLeft(goodIcon);
        } else {
            textFieldPattern.setLeft(badIcon);
        }

        try {
            HashMap<String,String> parts = simpleExpression.parse(getText(), getParameters());
            if (parts.size() > 0) {
                labelMatchedText.setText(parts.get("matched"));
                String partsStr = "";
                for (String id : parameters) {
                    if (parts.containsKey(id)) {
                        partsStr += id + ":" + parts.get(id) + "   ";
                    }
                }
                labelPartsText.setText(partsStr);
            }
            else {
                labelMatchedText.setText("");
                labelPartsText.setText("");
            }
        } catch (Exception ex) {
            labelMatchedText.setText("Not a complete match yet");
        }
    }
    
    @FXML
    public void initialize() {
    }
    
    @FXML
    private void handleAdd(ActionEvent event) {
        String id = comboBoxParameter.getValue();
        Integer count = comboBoxLength.getSelectionModel().getSelectedIndex();
        String countStr = "";
        if (count > 0) {
            countStr = ":" + count;
        }

        String exp = "$" + id + countStr + "$";
        textFieldPattern.insertText(oldCaretPosition, exp);
        oldCaretPosition += exp.length();
        comboBoxLength.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleAddSkip(ActionEvent event) {
        String id = "skip";
        Integer count = comboBoxLength.getSelectionModel().getSelectedIndex();
        String countStr = "";
        if (count > 0) {
            countStr = ":" + count;
        }
        String exp = "$" + id + countStr + "$";
        textFieldPattern.insertText(oldCaretPosition, exp);
        oldCaretPosition += exp.length();
        comboBoxLength.getSelectionModel().selectFirst();
    }
    
    @FXML
    private void handleAddEnd(ActionEvent event) {
        String id = "end";
        String exp = "$" + id + "$";
        textFieldPattern.insertText(oldCaretPosition, exp);
        oldCaretPosition += exp.length();
        comboBoxLength.getSelectionModel().selectFirst();
    }
}
