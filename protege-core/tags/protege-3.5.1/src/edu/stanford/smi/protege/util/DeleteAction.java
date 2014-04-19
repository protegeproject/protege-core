package edu.stanford.smi.protege.util;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Base class for the delete action that is available on most widgets.  This class handles the case of a multiselect 
 * delete by looping over the items to be deleted and calling #onDelete(Object) on each one separately. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class DeleteAction extends AllowableAction {

    private static final long serialVersionUID = -5040421731435729618L;

    protected DeleteAction(String text, Selectable selectable, Icon icon) {
        super(text, text, icon, selectable);
    }

    protected DeleteAction(String text, Selectable selectable) {
        super(text, selectable);
        if (getIcon() == null) {
            setIcon(Icons.getDeleteIcon());
        }
    }

    protected DeleteAction(ResourceKey key, Selectable selectable) {
        super(key, selectable);
    }

    public void actionPerformed(ActionEvent event) {
        if (isAllowed() && confirmDelete()) {
            onDelete();
        }
    }

    private boolean confirmDelete() {
        String text = LocalizedText.getText(ResourceKey.DIALOG_CONFIRM_DELETE_TEXT);
        int result = ModalDialog.showMessageDialog((JComponent) getSelectable(), text, ModalDialog.MODE_YES_NO);
        return result == ModalDialog.OPTION_YES;
    }

    public void onDelete() {
        WaitCursor cursor = new WaitCursor((JComponent) getSelectable());
        try {
            onDelete(new ArrayList(getSelection()));
        } finally {
            cursor.hide();
        }
    }

    protected void onDelete(Collection items) {
        Iterator i = getSelection().iterator();
        while (i.hasNext()) {
            onDelete(i.next());
        }
    }

    //ESCA-JAVA0130 
    public void onDelete(Object o) {
        Log.getLogger().warning("onDelete called: should have been overridden");
    }
}
