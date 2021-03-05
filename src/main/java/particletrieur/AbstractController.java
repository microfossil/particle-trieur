package particletrieur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public abstract class AbstractController {

    private Stage stage;

    @FXML
    public Parent root;

    private void createStage() {
        Scene scene = new Scene(root);
        stage = new Stage();
        stage.setScene(scene);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/icons/icon.png")));
    }

    public void show() {
        createStage();
        stage.initModality(Modality.NONE);
        stage.centerOnScreen();
        stage.show();
    }

    public void showAndWait() {
        createStage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.centerOnScreen();
        stage.showAndWait();
    }

    public void showAndWait(Pane root) {
        createStage();
        stage.initOwner(root.getScene().getWindow());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.centerOnScreen();
        stage.showAndWait();
    }

    public static <T extends AbstractController> T create(Class<T> cls) throws IOException {
        FxmlLocation location = cls.getAnnotation(FxmlLocation.class);
        FXMLLoader loader = new FXMLLoader(App.class.getResource(location.value()));
        loader.setControllerFactory(instantiatedClass -> App.getInstance().injector.getInstance(instantiatedClass));
        Parent root = loader.load();
        T controller = loader.getController();
        controller.root = root;
        return controller;
    }

    public static <T extends AbstractController> T create(Class<T> cls, ResourceBundle resources) throws IOException {
        FxmlLocation location = cls.getAnnotation(FxmlLocation.class);
        FXMLLoader loader = new FXMLLoader(App.class.getResource(location.value()), resources);
        loader.setControllerFactory(instantiatedClass -> App.getInstance().injector.getInstance(instantiatedClass));
        Parent root = loader.load();
        T controller = loader.getController();
        controller.root = root;
        return controller;
    }
}
