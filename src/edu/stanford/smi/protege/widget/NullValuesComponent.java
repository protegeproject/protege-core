package edu.stanford.smi.protege.widget;

import java.util.*;

/**
 * An implementation of ValuesComponent for value types that do not need a component.  This class does nothing.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class NullValuesComponent extends AbstractValuesComponent {

    private static final long serialVersionUID = -6644014809869230896L;

    public void clearSelection() {
        // do nothing
    }

    public Collection getSelection() {
        return Collections.EMPTY_LIST;
    }
}
