package edu.stanford.smi.protege.action;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Action to attempt to delete an instance.  If the instance being deleted is a class then that class cannot have
 * any instance.  The reason for this restriction is that people have been known to delete a metaclass without realizing
 * that all of its instances will disappear.  It might be better to just do the check on metaclasses...
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DeleteInstancesAction extends DeleteAction {

    public DeleteInstancesAction(Selectable selectable) {
        this(ResourceKey.INSTANCE_DELETE, selectable);
    }
    
    public DeleteInstancesAction(ResourceKey key, Selectable selectable) {
        super(key, selectable);
    }
    
    /**
     * @deprecated Use ResourceKey version
     */
    public DeleteInstancesAction(String text, Selectable selectable) {
        super(text, selectable);
    }

    private boolean canDelete(Instance instance) {
        boolean result = true;
        if (instance instanceof Cls) {
            Cls cls = (Cls) instance;
            int instanceCount = cls.getInstanceCount();
            result = instanceCount == 0;
            if (!result) {
                String text = LocalizedText.getText(ResourceKey.DELETE_CLASS_FAILED_DIALOG_TEXT);
                ModalDialog.showMessageDialog((JComponent) getSelectable(), text);
            }
        }
        return result;
    }

    protected void onAboutToDelete(Object o) {
        // do nothing by default
    }

    protected void onAfterDelete(Object o) {
        // do nothing
    }

    public void onDelete(Object o) {
        Instance instance = (Instance) o;
        if (canDelete(instance)) {
            onAboutToDelete(instance);
            instance.getKnowledgeBase().deleteFrame(instance);
            onAfterDelete(o);
        }
    }

    public void onSelectionChange() {
        boolean isEditable = true;
        Iterator i = getSelection().iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            if (!instance.isEditable()) {
                isEditable = false;
                break;
            }
        }
        setAllowed(isEditable);
    }
}
