package edu.stanford.smi.protege.util;

import java.awt.event.*;

import javax.swing.*;

/**
 * An adapter that listeners for popup menu mouse events and pops up a menu.  The actual menu popped up is delegated to
 * a subclass. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class PopupMenuMouseListener extends MouseAdapter {
    private JComponent _component;

    protected PopupMenuMouseListener(JComponent c) {
        _component = c;
    }

    private void checkEvent(MouseEvent event) {
        if (event.isPopupTrigger()) {
            setSelection(_component, event.getX(), event.getY());
            JPopupMenu menu = getPopupMenu();
            if (menu != null) {
                menu.show(_component, event.getX(), event.getY());
            }
        }
    }

    protected abstract JPopupMenu getPopupMenu();

    public void mouseClicked(MouseEvent event) {
        checkEvent(event);
    }

    public void mousePressed(MouseEvent event) {
        checkEvent(event);
    }

    public void mouseReleased(MouseEvent event) {
        checkEvent(event);
    }

    protected abstract void setSelection(JComponent c, int x, int y);
}
