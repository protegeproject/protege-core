package edu.stanford.smi.protege.util;

import java.awt.event.*;

/**
 * An adapter that causes the raw "mouse clicked" event to fire a double click event. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class DoubleClickAdapter extends MouseAdapter implements DoubleClickListener {

    public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2 && !event.isMetaDown()) {
            onDoubleClick(event.getSource());
        }
    }
}
