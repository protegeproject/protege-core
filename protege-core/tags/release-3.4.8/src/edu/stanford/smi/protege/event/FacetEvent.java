package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Event generated when a facet changes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FacetEvent extends AbstractEvent {
    private static final long serialVersionUID = -4537516153578794206L;
    private static final int BASE = 500;
    public static final int FRAME_SLOT_REFERENCE_ADDED = BASE + 1;
    public static final int FRAME_SLOT_REFERENCE_REMOVED = BASE + 2;

    public FacetEvent(Facet facet, int type, Frame frame, Slot slot) {
        super(facet, type, frame, slot);
    }

    public Frame getFrame() {
        return (Frame) getArgument1();
    }

    public Slot getSlot() {
        return (Slot) getArgument2();
    }
}
