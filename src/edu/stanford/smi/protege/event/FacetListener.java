package edu.stanford.smi.protege.event;

import java.util.*;

/**
 *  Listener interface for facet events 
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface FacetListener extends EventListener {

    public void frameSlotReferenceAdded(FacetEvent event);

    public void frameSlotReferenceRemoved(FacetEvent event);
}
