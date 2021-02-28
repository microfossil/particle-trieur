package particletrieur.helpers;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class TreeEventDispatcher implements EventDispatcher {

    @Override
    public Event dispatchEvent(Event event, EventDispatchChain tail) {
        if (event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) event;
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                event.consume();
            } else {
                event = tail.dispatchEvent(event);
            }
        } else {
            event = tail.dispatchEvent(event);
        }
        return event;
    }

}
