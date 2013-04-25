package edu.stanford.smi.protege.util;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * An adapter that causes the raw "mouse clicked" event to fire an action if the real event is actually a double click. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DoubleClickActionAdapter extends MouseAdapter {
    private Action _action;

    public DoubleClickActionAdapter(Action initialAction) {
        _action = initialAction;
    }

    public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2 && !event.isMetaDown() && wantDoubleClick(event)) {
            ActionEvent actionEvent = new ActionEvent(event.getSource(), ActionEvent.ACTION_PERFORMED, null);
            _action.actionPerformed(actionEvent);
        }
    }

    private static boolean wantDoubleClick(MouseEvent event) {
        boolean wantDoubleClick = true;
        Object source = event.getSource();
        if (source instanceof JTree) {
            JTree tree = (JTree) source;
            Point point = event.getPoint();
            int eventLocation = tree.getRowForLocation(point.x, point.y);
            int[] rows = tree.getSelectionRows();
            if (rows != null && rows[0] != eventLocation) {
                wantDoubleClick = false;
            }
        }
        return wantDoubleClick;
    }
}
