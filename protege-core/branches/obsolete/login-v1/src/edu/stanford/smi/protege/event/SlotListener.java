package edu.stanford.smi.protege.event;

import java.util.*;

/**
 * Listener interface for slot events.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface SlotListener extends EventListener {

    void templateSlotClsAdded(SlotEvent event);

    void templateSlotClsRemoved(SlotEvent event);

    void directSubslotAdded(SlotEvent event);
    void directSubslotRemoved(SlotEvent event);
    void directSubslotMoved(SlotEvent event);
    void directSuperslotAdded(SlotEvent event);
    void directSuperslotRemoved(SlotEvent event);
}
