package particletrieur.viewcontrollers.morphology;

import com.google.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import particletrieur.App;
import particletrieur.controls.dialogs.AlertEx;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.controls.dialogs.DialogEx;
import particletrieur.models.Supervisor;
import particletrieur.services.ParametersFromCSVService;
import particletrieur.services.ProjectService;
import particletrieur.viewmodels.SelectionViewModel;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ParametersViewController implements Initializable {

    @FXML
    TableView<ParameterViewModel> tableView;

    @Inject
    SelectionViewModel selectionViewModel;
    @Inject
    Supervisor supervisor;

    ObservableList<ParameterViewModel> items = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        TableColumn<ParameterViewModel, String> tableColumnParameterName = new TableColumn<>("Parameter");
        TableColumn<ParameterViewModel, String> tableColumnParameterValue = new TableColumn<>("Value");

        tableColumnParameterName.setCellValueFactory(item -> item.getValue().key);
        tableColumnParameterValue.setCellValueFactory(item -> item.getValue().value);

        tableColumnParameterName.setPrefWidth(160);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableView.getColumns().addAll(tableColumnParameterName, tableColumnParameterValue);

        selectionViewModel.currentParticleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                update(newValue.parameters);
            } else {
                items.clear();
            }
        });

        tableView.setItems(items);
    }


    public void update(LinkedHashMap<String, String> parameters) {
        List<ParameterViewModel> newItems = parameters.entrySet().stream().map(v -> new ParameterViewModel(v.getKey(), v.getValue())).collect(Collectors.toList());

        items.clear();
        items.addAll(newItems);
    }

    public void handleAddParametersFromCSV(ActionEvent actionEvent) {
        AlertEx alertEx = BasicDialogs.ShowInfo("Add Parameters", "Select a CSV from which to add parameters values to the particles.\n\nThe CSV must contain a column with header \"file\", \"filename\", or \"fichier\" containing the full path to the particle image, or the filename of the particle image if the filenames are unique.");
        alertEx.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                FileChooser fc = new FileChooser();
                String path = App.getPrefs().getProjectPath();
                if (path != null && Files.exists(Paths.get(path))) {
                    fc.setInitialDirectory(new File(path));
                }
                fc.setTitle("Choose parameters CSV file");
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv"));
                File file = fc.showOpenDialog(App.getWindow());
                if (file != null) {
                    Service<LinkedHashMap<String, LinkedHashMap<String, String>>> service = ParametersFromCSVService.getParametersFromCSV(file);
                    service.setOnSucceeded(event -> {
                        supervisor.project.updateParticleParameters(service.getValue());
                        update(selectionViewModel.getCurrentParticle().getParameters());
                    });
                    service.setOnFailed(event -> {
                        Exception ex = new Exception(service.getException());
                        ex.printStackTrace();
                        BasicDialogs.ShowException("Error", ex);
                    });
                    BasicDialogs.ProgressDialogWithCancel2(
                            "Add Parameters",
                            "Add Parameters",
                            App.getRootPane(),
                            service).start();
                }
            }
            return null;
        });
    }

    public class ParameterViewModel {

        StringProperty key = new SimpleStringProperty("");
        StringProperty value = new SimpleStringProperty("");

        public ParameterViewModel(String key, String value) {
            this.key.set(key);
            this.value.set(value);
        }
    }
}
