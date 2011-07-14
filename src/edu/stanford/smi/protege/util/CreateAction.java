package edu.stanford.smi.protege.util;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Base class for the create operation that is available on most widgets.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class CreateAction extends AllowableAction {

    private static final long serialVersionUID = -4451021338422592927L;

    protected CreateAction(ResourceKey key) {
        super(key);
    }

    protected CreateAction(String text) {
        this(text, Icons.getCreateIcon());
    }

    protected CreateAction(String text, Icon icon) {
        super(text, text, icon, null);
    }

    public void actionPerformed(ActionEvent event) {
        if (isAllowed()) {
            onCreate();
        }
    }

    public abstract void onCreate();
}
