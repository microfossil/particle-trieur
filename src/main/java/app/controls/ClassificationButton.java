package main.java.app.controls;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class ClassificationButton extends StackPane {

    @FXML
    Button button;
    @FXML
    SymbolLabel decorationGrid;
    @FXML
    SymbolLabel decorationCPU;
    @FXML
    Pane paneGrid;
    @FXML
    Pane paneCPU;

    public final String paneGridStyle = "-fx-background-color: #F2594B; -fx-opacity: 0.9; -fx-background-radius: 2 2 0 0;";
    public final String paneCPUStyle = "-fx-background-color: #0067A8; -fx-opacity: 0.9; -fx-background-radius: 0 0 2 2;";


    public ClassificationButton(String text) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ClassificationButton.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        button.setText(text);
        button.setMnemonicParsing(false);
        reset();
    }

    public Button getButton() {
        return button;
    }

    public void reset() {
        setIsHighlighted(false);
        setIsKNN(false);
        setIsCNN(false);
        setKNNScore(0);
        setCNNScore(0);
    }

    public void setIsHighlighted(boolean isHighlighted) {
        if (isHighlighted) {
            button.setStyle("-fx-base: derive(-fx-accent, 50%)");
        }
        else {
            button.setStyle("");
        }
    }

    public void setIsKNN(boolean isKNN) {
        decorationGrid.setVisible(isKNN);
    }

    public void setIsCNN(boolean isCNN) {
        decorationCPU.setVisible(isCNN);
    }

    public void setKNNScore(double score) {
        paneGrid.setVisible(score != 0.0);
        String extra = String.format("-fx-background-insets: 0 %d 0 0;",
                (int) (button.getWidth() * (1.0 - score)));
        paneGrid.setStyle(paneGridStyle + extra);
    }

    public void setCNNScore(double score) {
        paneCPU.setVisible(score != 0.0);
        String extra = String.format("-fx-background-insets: 0 %d 0 0;",
                (int) (button.getWidth() * (1.0 - score)));
        paneCPU.setStyle(paneCPUStyle + extra);
    }
}
