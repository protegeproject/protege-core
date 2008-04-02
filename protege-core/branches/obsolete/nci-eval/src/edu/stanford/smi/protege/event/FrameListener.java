package edu.stanford.smi.protege.event;

import java.util.*;

/**
 * Listener interface for frame events.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface FrameListener extends EventListener {

    public void browserTextChanged(FrameEvent event);

    public void deleted(FrameEvent event);

    public void nameChanged(FrameEvent event);

    public void ownFacetAdded(FrameEvent event);

    public void ownFacetRemoved(FrameEvent event);

    public void ownFacetValueChanged(FrameEvent event);

    public void ownSlotAdded(FrameEvent event);

    public void ownSlotRemoved(FrameEvent event);

    public void ownSlotValueChanged(FrameEvent event);

    public void visibilityChanged(FrameEvent event);
}
