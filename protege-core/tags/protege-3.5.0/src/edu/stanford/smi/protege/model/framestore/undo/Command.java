package edu.stanford.smi.protege.model.framestore.undo;

public interface Command {
    Object doIt();
    void undoIt();
    void redoIt();
    String getDescription();
}
