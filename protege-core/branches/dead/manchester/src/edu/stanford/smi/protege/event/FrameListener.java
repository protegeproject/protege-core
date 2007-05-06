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

    void nameChanged(FrameEvent event);

    void ownFacetAdded(FrameEvent event);

    void ownFacetRemoved(FrameEvent event);

    void ownFacetValueChanged(FrameEvent event);

    void ownSlotAdded(FrameEvent event);

    void ownSlotRemoved(FrameEvent event);

    void ownSlotValueChanged(FrameEvent event);

    void visibilityChanged(FrameEvent event);
}
