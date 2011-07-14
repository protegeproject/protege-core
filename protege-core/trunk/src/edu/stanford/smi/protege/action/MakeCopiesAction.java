package edu.stanford.smi.protege.action;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * @author    Simon Ilyushchenko <simonf@cshl.edu>
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MakeCopiesAction extends AllowableAction {

    private static final long serialVersionUID = -1874566858726067172L;

    public MakeCopiesAction(ResourceKey key, Selectable selectable) {
        super(key, selectable);
    }

    public void actionPerformed(ActionEvent event) {
        if (isAllowed()) {
            WaitCursor cursor = new WaitCursor((JComponent) getSelectable());
            try {
                onCopy();
            } finally {
                cursor.hide();
            }
        }
    }

    public void onCopy() {

        MakeCopiesPanel panel = new MakeCopiesPanel();
        String title = LocalizedText.getText(ResourceKey.COPY_DIALOG_TITLE);
        int dialogResult =
            ModalDialog.showDialog((JComponent) getSelectable(), panel, title, ModalDialog.MODE_OK_CANCEL);

        if (dialogResult == ModalDialog.OPTION_OK) {
            Integer result = panel.getNumberOfCopies();
            if (result != null) {
                int number = result.intValue();
                if (number > 0) {
                    boolean isDeep = panel.getIsDeepCopy();
                    copy(number, isDeep);
                }
            }
        }
    }
    protected void copy(int number, boolean isDeep) {
        Iterator i = getSelection().iterator();
        while (i.hasNext()) {
            Instance inst = (Instance) i.next();
            for (int n = 0; n < number; n++) {
                copy(inst, isDeep);
            }
        }
    }

    //ESCA-JAVA0130 
    protected Instance copy(Instance instance, boolean isDeep) {
        return (Instance) instance.copy(null, null, isDeep);
    }
}
