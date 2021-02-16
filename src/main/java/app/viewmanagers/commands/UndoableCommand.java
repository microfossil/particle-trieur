package main.java.app.viewmanagers.commands;

import main.java.app.models.project.Project;

public abstract class UndoableCommand {

    public String name;

    public UndoableCommand(String name) {
        this.name = name;
    }

    public abstract boolean apply() throws Project.TaxonDoesntExistException;

    public abstract boolean revert();
}
