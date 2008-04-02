package edu.stanford.smi.protege.util;

import java.util.*;

/**
 * Interface for a generic dispatcher of events.  There should be a better (standard) way to handle this.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface EventDispatcher {

    void postEvent(Collection listeners, Object source, int type, Object arg1, Object arg2, Object arg3);
}
