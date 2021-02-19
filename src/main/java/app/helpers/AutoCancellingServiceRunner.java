package main.java.app.helpers;

import javafx.concurrent.Service;
import javafx.concurrent.Worker;

public class AutoCancellingServiceRunner<T> {

//    ExecutorService service;
//    Task<T> task;
//    Future<?> future;

    Service service;
    String name;
    
    public AutoCancellingServiceRunner(String name) {
//        this.service = service;
        this.name = name;
    }
    
//    public void run(Task<T> task) {
//        if (future != null
////                &&
////                this.task.getState() != Worker.State.SUCCEEDED &&
////                this.task.getState() != Worker.State.FAILED
//        ) {
//
//            future.cancel(true);
//        }
//
//        future = service.submit(task);
//    }

    public void run(Service<T> service) {
        if (this.service != null
                &&
                this.service.getState() != Worker.State.SUCCEEDED &&
                this.service.getState() != Worker.State.FAILED
        ) {
            this.service.cancel();
        }

        this.service = service;
        this.service.start();
    }
}
