package edu.stanford.smi.protege.util;

import java.util.*;

import javax.swing.*;

/**
 *  Implementation of drag source for list boxes that does nothing.  Doing nothing is the default.
 */

class DefaultListDragSourceListener extends ListDragSourceListener {

    public void doCopy(JComponent c, int[] indices, Collection collection) {
    }

    public void doMove(JComponent c, int[] indices, Collection collection) {
    }
}
