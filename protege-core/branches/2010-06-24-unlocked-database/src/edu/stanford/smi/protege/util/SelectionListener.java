package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * Listener interface for the generic {@link Selectable} interface.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface SelectionListener extends EventListener {

    void selectionChanged(SelectionEvent event);
}
