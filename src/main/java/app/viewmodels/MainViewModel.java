package main.java.app.viewmodels;

import main.java.app.AbstractDialogController;
import main.java.app.helpers.CSEvent;
import main.java.app.models.Supervisor;
import main.java.app.viewcontrollers.network.CNNTrainingViewController;
import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import main.java.app.viewcontrollers.tools.FlowcamSegmenterViewController;

import java.io.IOException;

public class MainViewModel {

    public CSEvent addImageRequested = new CSEvent();
    public CSEvent removeImageRequested = new CSEvent();

    public CNNTrainingViewController cnnTrainingViewController;
    public FlowcamSegmenterViewController flowcamSegmenterViewController;

    private final BooleanProperty operationInProgress = new SimpleBooleanProperty(false);
    public BooleanProperty operationInProgressProperty() { return operationInProgress; }
    public void setOperationInProgress(Boolean value) { operationInProgress.set(value); }
    public Boolean getOperationInProgress() { return operationInProgress.get(); }

    @Inject
    public Supervisor supervisor;

    public MainViewModel() {
        try {
            cnnTrainingViewController = AbstractDialogController.create(CNNTrainingViewController.class);
            flowcamSegmenterViewController = AbstractDialogController.create(FlowcamSegmenterViewController.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
