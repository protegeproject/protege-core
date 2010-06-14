package edu.stanford.smi.protege.util;

/**
 * An adapter that causes the raw "mouse clicked" event to fire a double click event. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ModalDialogCloseDoubleClickAdapter extends DoubleClickAdapter {

    public void onDoubleClick(Object o) {
        ModalDialog.attemptDialogClose(ModalDialog.OPTION_OK);
    }
}
