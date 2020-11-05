package ordervschaos.particletrieur.app.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.function.*;

public class ServiceHelper {

    public static <T,U> Service<T> createService(BiFunction<U, Task<T>, T> method, U payload) {
        Service<T> service = new Service<T>() {
            @Override
            protected Task<T> createTask() {
                Task<T> task = new Task<T>() {
                    @Override
                    protected T call() throws Exception {
                        return method.apply(payload, this);
                    }
                };
                return task;
            }
        };
        return service;
    }
}
