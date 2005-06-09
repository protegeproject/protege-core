package edu.stanford.smi.protege.model;

import java.util.*;

import javax.swing.event.*;

import edu.stanford.smi.protege.model.framestore.undo.*;

public interface CommandManager {
    void addChangeListener(ChangeListener listener);

    void removeChangeListener(ChangeListener listener);

    Collection getDoneCommands();

    Collection getUndoneCommands();

    Command getUndoCommand();

    Command getRedoCommand();

    void undo();

    void redo();

    boolean canUndo();

    boolean canRedo();
}
