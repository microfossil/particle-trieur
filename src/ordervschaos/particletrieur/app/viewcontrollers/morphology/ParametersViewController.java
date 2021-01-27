package ordervschaos.particletrieur.app.viewcontrollers.morphology;

import com.google.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ordervschaos.particletrieur.app.models.processing.Morphology;
import ordervschaos.particletrieur.app.viewmodels.SelectionViewModel;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ParametersViewController implements Initializable {

    @FXML
    TableView<ParameterViewModel> tableView;

    @Inject
    SelectionViewModel selectionViewModel;

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
            }
            else {
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

    public class ParameterViewModel {

        StringProperty key = new SimpleStringProperty("");
        StringProperty value = new SimpleStringProperty("");

        public ParameterViewModel(String key, String value) {
            this.key.set(key);
            this.value.set(value);
        }
    }
}
