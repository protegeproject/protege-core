package edu.stanford.smi.protege.event;

import java.util.*;

/**
 *  Listener for events resulting from a class change.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface ClsListener extends EventListener {

    void directInstanceAdded(ClsEvent event);

    void directInstanceRemoved(ClsEvent event);

    void directSubclassAdded(ClsEvent event);

    void directSubclassMoved(ClsEvent event);

    void directSubclassRemoved(ClsEvent event);

    void directSuperclassAdded(ClsEvent event);

    void directSuperclassRemoved(ClsEvent event);

    void templateFacetAdded(ClsEvent event);

    void templateFacetRemoved(ClsEvent event);

    void templateFacetValueChanged(ClsEvent event);

    void templateSlotAdded(ClsEvent event);

    void templateSlotRemoved(ClsEvent event);

    void templateSlotValueChanged(ClsEvent event);
}
