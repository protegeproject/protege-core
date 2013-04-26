package edu.stanford.smi.protege.plugin;

import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractPlugin implements Plugin {
    public String getName() {
        return StringUtilities.getClassName(this);
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }

    public void dispose() {
    }
}
