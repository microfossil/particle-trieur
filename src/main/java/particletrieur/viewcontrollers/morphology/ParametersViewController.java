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
import particletrieur.AppController;
import particletrieur.controls.dialogs.AlertEx;
import particletrieur.controls.dialogs.BasicDialogs;
import particletrieur.models.Supervisor;
import particletrieur.services.ParametersFromCSVService;
import particletrieur.services.ParametersFromTXTService;
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
        items.clear();
        if (parameters != null) {
            List<ParameterViewModel> newItems = parameters.entrySet().stream().map(v -> new ParameterViewModel(v.getKey(), v.getValue())).collect(Collectors.toList());
            items.addAll(newItems);
        }
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
                File file = fc.showOpenDialog(AppController.getWindow());
                if (file != null) {
                    Service<LinkedHashMap<String, LinkedHashMap<String, String>>> service = ParametersFromCSVService.getParametersFromCSV(file);
                    service.setOnSucceeded(event -> {
                        BasicDialogs.ShowYesNo(
                                "Overwrite?",
                                "Do you wish to overwrite the existing particle settings (label, sample, etc.) with the CSV values?",
                                () -> {
                                    supervisor.project.updateParticleParameters(service.getValue(), true);
                                    update(selectionViewModel.getCurrentParticle().getParameters());
                                },
                                () -> {
                                    supervisor.project.updateParticleParameters(service.getValue(), false);
                                    update(selectionViewModel.getCurrentParticle().getParameters());
                                });
                    });
                    service.setOnFailed(event -> {
                        Exception ex = new Exception(service.getException());
                        ex.printStackTrace();
                        BasicDialogs.ShowException("Error", ex);
                    });
                    BasicDialogs.ProgressDialogWithCancel2(
                            "Add Parameters",
                            "Add Parameters",
                            service).start();
                }
            }
            return null;
        });
    }

    /* Thang DQ LE
     * add parameters (in text files) for Zooscan images
     */
    public void handleAddParametersFromTXTFile(ActionEvent actionEvent) {
        AlertEx alertEx = BasicDialogs.ShowInfo("Add Parameters", "Select one or many TXT files from which to add parameters values to the particles.\n\n This function is only applied for Zooscan images with the parameters are stored in the TXT files such as f102_inf_1000_1_meas.txt, f102_sup_1000_1_meas.txt");
        alertEx.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                FileChooser fc = new FileChooser();
                String path = App.getPrefs().getProjectPath();
                if (path != null && Files.exists(Paths.get(path))) {
                    fc.setInitialDirectory(new File(path));
                }
                fc.setTitle("Choose parameters txt file");
                fc.getExtensionFilters().add
                        (
                                new FileChooser.ExtensionFilter("Text file (*.txt)", "*.txt")
                        );

                List<File> files = fc.showOpenMultipleDialog(AppController.getWindow());
                if (files != null)
                {
                    for (File file : files)
                    {
                        Service<LinkedHashMap<String, LinkedHashMap<String, String>>> service = ParametersFromTXTService.getParametersFromTextFile(file);
                        service.setOnSucceeded(event -> {
                            BasicDialogs.ShowYesNo(
                                    "Overwrite?",
                                    "Do you wish to overwrite the existing particle settings (label, sample, etc.) with the TXT values?",
                                    () -> {
                                        supervisor.project.updateParticleParameters(service.getValue(), true);
                                        update(selectionViewModel.getCurrentParticle().getParameters());
                                    },
                                    () -> {
                                        supervisor.project.updateParticleParameters(service.getValue(), false);
                                        update(selectionViewModel.getCurrentParticle().getParameters());
                                    });
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
                                service).start();
                    }
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
