package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Event generated when the knowledge base changes.  Note that this just covers instance creation/deletion.  Changes
 * to existing instances generate separate events.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class KnowledgeBaseEvent extends AbstractEvent {
    private final static int BASE = 600;
    public final static int CLS_CREATED = BASE + 1;
    public final static int CLS_DELETED = BASE + 2;
    public final static int SLOT_CREATED = BASE + 3;
    public final static int SLOT_DELETED = BASE + 4;
    public final static int FACET_CREATED = BASE + 5;
    public final static int FACET_DELETED = BASE + 6;
    public final static int INSTANCE_CREATED = BASE + 7;
    public final static int INSTANCE_DELETED = BASE + 8;
    public final static int FRAME_NAME_CHANGED = FrameEvent.NAME_CHANGED;

    public final static int DEFAULT_CLS_METACLASS_CHANGED = BASE + 10;
    public final static int DEFAULT_SLOT_METACLASS_CHANGED = BASE + 11;
    public final static int DEFAULT_FACET_METACLASS_CHANGED = BASE + 12;

    public KnowledgeBaseEvent(KnowledgeBase kb, int type, Frame frame) {
        super(kb, type, frame);
    }

    public KnowledgeBaseEvent(KnowledgeBase kb, int type, Frame frame, Object arg2) {
        super(kb, type, frame, arg2);
    }

    public KnowledgeBaseEvent(KnowledgeBase kb, int type, Frame frame, Object arg2, Object arg3) {
        super(kb, type, frame, arg2, arg3);
    }

    public Cls getCls() {
        return (Cls) getArgument1();
    }

    public Facet getFacet() {
        return (Facet) getArgument1();
    }

    public Frame getFrame() {
        return (Frame) getArgument1();
    }

    public Cls getOldMetaCls() {
        return (Cls) getArgument2();
    }

    public String getOldName() {
        return (String) getArgument2();
    }

    public Slot getSlot() {
        return (Slot) getArgument1();
    }

    public void localize(KnowledgeBase kb) {
        super.localize(kb);
        setSource(kb);
    }
}
