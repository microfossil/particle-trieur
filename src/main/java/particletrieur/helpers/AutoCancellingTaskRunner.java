package particletrieur.helpers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.image.Image;
import particletrieur.App;
import particletrieur.models.project.Particle;

public class AutoCancellingTaskRunner<T> {

    ObjectProperty<Task<T>> runningTask = new SimpleObjectProperty<>();

    public void runTask(Task<T> task) {
        if (runningTask.get() != null &&
                runningTask.get().getState() != Worker.State.SUCCEEDED &&
                runningTask.get().getState() != Worker.State.FAILED) {

            runningTask.get().cancel();
        }
        runningTask.set(task);
        App.getExecutorService().submit(task);
    }
}
