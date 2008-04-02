package edu.stanford.smi.protege.event;

import java.util.*;

/**
 * Listener interface for frame events.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface FrameListener extends EventListener {

    void browserTextChanged(FrameEvent event);

    void deleted(FrameEvent event);

    /**
     * This routine should be called replaceFrame but for backwards compatibility
     * reasons we cannot change this. The closest thing in Protege frames to a rename
     * frame operation is a replace frame operation where a frame is deleted and a new 
     * frame is created which is identical to the original except for the name.  When this 
     * happens the following method is called.
     */
    void nameChanged(FrameEvent event);

    void ownFacetAdded(FrameEvent event);

    void ownFacetRemoved(FrameEvent event);

    void ownFacetValueChanged(FrameEvent event);

    void ownSlotAdded(FrameEvent event);

    void ownSlotRemoved(FrameEvent event);

    void ownSlotValueChanged(FrameEvent event);

    void visibilityChanged(FrameEvent event);
}
