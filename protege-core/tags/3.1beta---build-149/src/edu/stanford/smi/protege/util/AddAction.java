package edu.stanford.smi.protege.util;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Base class support for the "Add" action available on most widgets.  The semantics of "add" for cardinality single are
 * actually "set" (remove and add).
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AddAction extends AllowableAction {

    public AddAction(ResourceKey key) {
        super(key);
    }
    
    public AddAction(String text) {
        super(text, null);
        if (getIcon() == null) {
            setIcon(Icons.getAddIcon());
        }
    }

    public AddAction(String text, Icon icon) {
        super("Add", text, icon, null);
    }

    public void actionPerformed(ActionEvent event) {
        if (isAllowed()) {
            onAdd();
        }
    }

    public abstract void onAdd();
}
