package edu.stanford.smi.protege.util;

//ESCA*JAVA0130

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;

/**
 * An action for the standard view button that appears on many slot widgets.  Typically this action causes a form
 * to pop up for the selected frame.  The actually implementation though is delegated to a template method.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class ViewAction extends AllowableAction {

    private static final long serialVersionUID = 2185080283598996673L;

    protected ViewAction(ResourceKey key, Selectable selectable) {
        super(key, selectable);
    }

    protected ViewAction(Selectable selectable) {
        super(ResourceKey.VALUE_VIEW, selectable);
    }

    protected ViewAction(String text, Selectable selectable) {
        super(text, Icons.getViewIcon(), selectable);
    }

    protected ViewAction(String text, Selectable selectable, Icon icon) {
        super(text, text, icon, selectable);
    }

    public void actionPerformed(ActionEvent event) {
        if (isAllowed()) {
            WaitCursor cursor = new WaitCursor((JComponent) getSelectable());
            try {
                onView();
            } finally {
                cursor.hide();
            }
        }
    }

    public void onView() {
        Iterator i = getSelection().iterator();
        while (i.hasNext()) {
            onView(i.next());
        }
    }

    public void onView(Object o) {
        Log.getLogger().warning("onView called: should have been overridden");
    }
}
