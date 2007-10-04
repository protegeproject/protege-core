package edu.stanford.smi.protege.event;

/**
 *  Adapter for frame listeners.  Subclass this if you only want to catch a few frame events.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class FrameAdapter implements FrameListener {

    public void browserTextChanged(FrameEvent event) {
    }

    public void deleted(FrameEvent event) {
    }

    /**
     * @deprecated Override frameReplaced instead.
     */
    @Deprecated
    public void nameChanged(FrameEvent event) {
        frameReplaced(event);
    }
    
    public void frameReplaced(FrameEvent event) {
    }

    public void ownFacetAdded(FrameEvent event) {
    }

    public void ownFacetRemoved(FrameEvent event) {
    }

    public void ownFacetValueChanged(FrameEvent event) {
    }

    public void ownSlotAdded(FrameEvent event) {
    }

    public void ownSlotRemoved(FrameEvent event) {
    }

    public void ownSlotValueChanged(FrameEvent event) {
    }

    public void visibilityChanged(FrameEvent event) {
    }
}
