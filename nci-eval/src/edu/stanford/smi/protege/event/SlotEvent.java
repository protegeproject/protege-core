package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Event generated when a slot changes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotEvent extends AbstractEvent {
    private final static int BASE = 400;
    public final static int TEMPLATE_SLOT_CLS_ADDED = BASE + 1;
    public final static int TEMPLATE_SLOT_CLS_REMOVED = BASE + 2;
    public final static int DIRECT_SUBSLOT_ADDED = BASE + 5;
    public final static int DIRECT_SUBSLOT_REMOVED = BASE + 6;
    public final static int DIRECT_SUPERSLOT_ADDED = BASE + 7;
    public final static int DIRECT_SUPERSLOT_REMOVED = BASE + 8;
    public final static int DIRECT_SUBSLOT_MOVED = BASE + 9;

    public SlotEvent(Slot slot, int type, Frame frame) {
        super(slot, type, frame);
    }

    public Cls getCls() {
        return (Cls) getArgument();
    }

    public Frame getFrame() {
        return (Frame) getArgument();
    }

    public Slot getSlot() {
        return (Slot) getSource();
    }

    public Slot getSubslot() {
        return (Slot) getArgument();
    }

    public Slot getSuperslot() {
        return (Slot) getArgument();
    }
    
    public boolean isDeletingSlotEvent() {
        return getSlot().isBeingDeleted();
    }
}
