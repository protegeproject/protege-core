package edu.stanford.smi.protege.event;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Event generated when a frame changes. 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameEvent extends AbstractEvent {
    private static final long serialVersionUID = 5250534765271586389L;
    private static final int BASE = 100;
    /*
     * @deprecated Use FrameEvent.REPLACE_FRAME instead
     */
    @Deprecated public static final int NAME_CHANGED = BASE + 1;
    public static final int REPLACE_FRAME = BASE + 1;
    public static final int DELETED = BASE + 2;
    public static final int VISIBILITY_CHANGED = BASE + 3;
    public static final int BROWSER_TEXT_CHANGED = BASE + 5;

    public static final int OWN_SLOT_ADDED = BASE + 6;
    public static final int OWN_SLOT_REMOVED = BASE + 7;
    public static final int OWN_FACET_ADDED = BASE + 8;
    public static final int OWN_FACET_REMOVED = BASE + 9;
    public static final int OWN_SLOT_VALUE_CHANGED = BASE + 10;
    public static final int OWN_FACET_VALUE_CHANGED = BASE + 11;

    public FrameEvent(Frame frame, int type) {
        super(frame, type);
    }

    public FrameEvent(Frame frame, int type, Object argument) {
        super(frame, type, argument);
    }

    public FrameEvent(Frame frame, int type, Object argument, Object argument2) {
        super(frame, type, argument, argument2);
    }

    public Facet getFacet() {
        return (Facet) getArgument2();
    }

    public Frame getFrame() {
        return (Frame) getSource();
    }

    public String getOldName() {
        return (String) getArgument1();
    }

    public List getOldValues() {
        return (List) getArgument2();
    }
    
    public Slot getSlot() {
        return (Slot) getArgument1();
    }
    
    public Frame getNewFrame() {
        return (Frame) getArgument2();
    }
    
    public boolean isDeletingFrameEvent() {
        return getFrame().isBeingDeleted();
    }
}
