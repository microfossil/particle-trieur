package ordervschaos.particletrieur.app;

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

    public Stage stage;

    @FXML
    public Parent root;

    public void show() {
        stage.initModality(Modality.NONE);
        stage.centerOnScreen();
        stage.show();
    }

    public void showAndWait() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.centerOnScreen();
        stage.showAndWait();
    }

    public void showAndWait(Pane root) {
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
        Scene scene = new Scene(root);
        controller.stage = new Stage();
        controller.stage.setScene(scene);
        controller.stage.getIcons().add(new Image(App.class.getResourceAsStream("resources/icon.png" )));
        return controller;
    }

    public static <T extends AbstractController> T create(Class<T> cls, ResourceBundle resources) throws IOException {
        FxmlLocation location = cls.getAnnotation(FxmlLocation.class);
        FXMLLoader loader = new FXMLLoader(App.class.getResource(location.value()), resources);
        loader.setControllerFactory(instantiatedClass -> App.getInstance().injector.getInstance(instantiatedClass));
        Parent root = loader.load();
        T controller = loader.getController();
        controller.root = root;
        Scene scene = new Scene(root);
        controller.stage = new Stage();
        controller.stage.setScene(scene);
        controller.stage.getIcons().add(new Image(App.class.getResourceAsStream("resources/icon.png" )));
        return controller;
    }
}
