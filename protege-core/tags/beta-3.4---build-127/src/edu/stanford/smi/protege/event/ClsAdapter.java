package edu.stanford.smi.protege.event;

/**
 *  Adapter for the ClsListener interface.  Subclass this if you only want to catch a few events from the listener.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class ClsAdapter implements ClsListener {

    public void directInstanceAdded(ClsEvent event) {
    }

    public void directInstanceRemoved(ClsEvent event) {
    }

    public void directSubclassAdded(ClsEvent event) {
    }

    public void directSubclassMoved(ClsEvent event) {
    }

    public void directSubclassRemoved(ClsEvent event) {
    }

    public void directSuperclassAdded(ClsEvent event) {
    }

    public void directSuperclassRemoved(ClsEvent event) {
    }

    public void templateFacetAdded(ClsEvent event) {
    }

    public void templateFacetRemoved(ClsEvent event) {
    }

    public void templateFacetValueChanged(ClsEvent event) {
    }

    public void templateSlotAdded(ClsEvent event) {
    }

    public void templateSlotRemoved(ClsEvent event) {
    }

    public void templateSlotValueChanged(ClsEvent event) {
    }
}
