package particletrieur.viewmodels;

import particletrieur.AbstractDialogController;
import particletrieur.helpers.CSEvent;
import particletrieur.models.Supervisor;
import particletrieur.viewcontrollers.network.CNNTrainingViewController;
import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import particletrieur.viewcontrollers.tools.FlowcamSegmenterViewController;

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
