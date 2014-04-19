package edu.stanford.smi.protege.event;

import java.util.*;

/**
 *  Listener interface for facet events 
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface FacetListener extends EventListener {

    void frameSlotReferenceAdded(FacetEvent event);

    void frameSlotReferenceRemoved(FacetEvent event);
}
