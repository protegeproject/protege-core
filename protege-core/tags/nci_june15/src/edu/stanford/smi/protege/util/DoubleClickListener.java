package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * Callback interface for double-clicks
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface DoubleClickListener extends EventListener {

    /** called when an item is double-clicked. */
    void onDoubleClick(Object item);
}
