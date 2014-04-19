package edu.stanford.smi.protege.action;

import java.awt.event.ActionEvent;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.smi.protege.model.CommandManager;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.undo.Command;
import edu.stanford.smi.protege.resource.ResourceKey;

/**
 * Request that the previous operation be undone.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class UndoAction extends ProjectAction {

    private static final long serialVersionUID = 2910477435427456525L;

    public UndoAction(boolean large) {
        super(ResourceKey.UNDO_ACTION, large);
        CommandManager manager = getCommandManager();        
        if (manager == null || isUndoEnabled()) {
            setEnabled(false);
        } else {
            setEnabled(isUndoEnabled() & manager.canUndo());
            manager.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    setEnabled(isUndoEnabled() & getCommandManager().canUndo());
                }
            });
        }
    }

    private boolean isUndoEnabled() {
    	KnowledgeBase kb = getKnowledgeBase();    	
    	return (kb!=null && kb instanceof DefaultKnowledgeBase && ((DefaultKnowledgeBase)kb).isUndoEnabled() && kb.getProject().isUndoOptionEnabled());
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
                    if (s!= null && s.length() > 75) {
                        s = s.substring(0, 75) + "...";
                    }
                    value = value.toString() + " (" + s == null ? "no description" : s + ")";
                }
            }
        }
        return value;
    }

    public void actionPerformed(ActionEvent event) {
        getCommandManager().undo();
    }
}