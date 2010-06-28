package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * A generic interface for things that are selectable.  Often (always?) in this app listeners don't really care if they
 * are listening to a List or a Table or a Tree.  They just want to find out when the selection changes and to be able
 * to find out what the current selection list.  This interface allows listeners to do this.  There are a variety of
 * adapters available that turn component specific selection events into Selectable events.  There are also Selectable
 * versions of all of the standard components.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Selectable {

    void addSelectionListener(SelectionListener listener);

    void clearSelection();

    Collection getSelection();

    void notifySelectionListeners();

    void removeSelectionListener(SelectionListener listener);
}
