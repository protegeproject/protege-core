package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.undo.*;
import edu.stanford.smi.protege.resource.*;

/**
 * Redo the previous undone action.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class RedoAction extends ProjectAction {

    private static final long serialVersionUID = -3530704885116941344L;

    public RedoAction(boolean large) {
        super(ResourceKey.REDO_ACTION, large);
        CommandManager manager = getCommandManager();
        if (manager == null) {
            setEnabled(false);
        } else {
            setEnabled(manager.canRedo());
            manager.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    setEnabled(getCommandManager().canRedo());
                }
            });
        }
    }

    public void actionPerformed(ActionEvent event) {
        getKnowledgeBase().getCommandManager().redo();
    }

    public Object getValue(String key) {
        Object value = super.getValue(key);
        if (key.equals(NAME)) {
            // Log.stack("", this, "getValue", key);
            CommandManager commandManager = getCommandManager();
            if (commandManager != null) {
                Command command = commandManager.getRedoCommand();
                if (command != null) {
                    String s = command.getDescription();
                    value = value.toString() + " (" + s + ")";
                }
            }
        }
        return value;
    }

}
