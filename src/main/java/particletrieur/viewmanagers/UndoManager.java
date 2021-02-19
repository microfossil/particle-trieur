package particletrieur.viewmanagers;

import particletrieur.helpers.CSEvent;
import particletrieur.helpers.SizedStack;
import particletrieur.viewmanagers.commands.UndoableCommand;

public class UndoManager {

    public SizedStack<UndoableCommand> commands;
    public CSEvent<Integer> stackUpdated = new CSEvent<>();

    public UndoManager() {
        commands = new SizedStack<>(5);
    }

    public void undo() {
        if (commands.size() > 0) {
            UndoableCommand command = commands.pop();
            stackUpdated.broadcast(commands.size());
            command.revert();
        }
    }

    public void add(UndoableCommand command) {
        commands.push(command);
        stackUpdated.broadcast(commands.size());
    }

    public void clear() {
        commands.clear();
        stackUpdated.broadcast(commands.size());
    }
}
