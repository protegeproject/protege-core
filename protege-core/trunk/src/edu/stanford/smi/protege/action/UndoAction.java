package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.undo.*;
import edu.stanford.smi.protege.resource.*;

/**
 *  Request that the previous operation be undone. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class UndoAction extends ProjectAction {

    public UndoAction(boolean large) {
        super(ResourceKey.UNDO_ACTION, large);
        CommandManager manager = getCommandManager();
        if (manager == null) {
            setEnabled(false);
        } else {
            setEnabled(manager.canUndo());
            manager.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    setEnabled(getCommandManager().canUndo());
                }
            });
        }
    }

    public Object getValue(String key) {
        Object value = super.getValue(key);
        if (key.equals(NAME)) {
            // Log.stack("", this, "getValue", key);
            CommandManager commandManager = getCommandManager();
            if (commandManager != null) {
                Command command = commandManager.getUndoCommand();
                if (command != null) {
                    String s = command.getDescription();
                    value = value.toString() + " (" + s + ")";
                }
            }
        }
        return value;
    }

    public void actionPerformed(ActionEvent event) {
        getCommandManager().undo();
    }
}
