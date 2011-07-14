package edu.stanford.smi.protege.util;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;

/**
 * An action to remove an item.  Note that this is different from deleting the item.  Removing it causes the reference
 * to the item to be removed from this particular place but the same item may still be refered to elsewhere.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class RemoveAction extends AllowableAction {

    private static final long serialVersionUID = -5762542587316652756L;

    protected RemoveAction(ResourceKey key, Selectable selectable) {
        super(key, selectable);
    }

    protected RemoveAction(String text, Selectable selectable) {
        this(text, selectable, Icons.getRemoveIcon());
    }

    protected RemoveAction(String text, Selectable selectable, Icon icon) {
        this("Remove", text, selectable, icon);
    }

    protected RemoveAction(String name, String text, Selectable selectable, Icon icon) {
        super(name, text, icon, selectable);
    }

    public void actionPerformed(ActionEvent event) {
        if (isAllowed() && confirmRemove()) {
            onRemove();
        }
    }

    private boolean confirmRemove() {
        boolean result;
        Project project = ProjectManager.getProjectManager().getCurrentProject();
        if (project != null && project.getDisplayConfirmationOnRemove()) {
            String text = LocalizedText.getText(ResourceKey.DIALOG_CONFIRM_REMOVE_TEXT);
            int choice = ModalDialog.showMessageDialog((JComponent) getSelectable(), text, ModalDialog.MODE_YES_NO);
            result = choice == ModalDialog.OPTION_YES;
        } else {
            result = true;
        }
        return result;
    }

    public void onRemove(Collection objects) {
        Iterator i = objects.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            onRemove(o);
        }
    }

    //ESCA-JAVA0130 
    public void onRemove(Object o) {
        Log.getLogger().warning("onRemove called: should have been overridden");
    }

    public final void onRemove() {
        Collection objects = new ArrayList(getSelection());
        onRemove(objects);
    }
}
