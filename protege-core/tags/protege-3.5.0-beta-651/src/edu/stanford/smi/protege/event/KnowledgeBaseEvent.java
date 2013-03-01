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
    private static final long serialVersionUID = 6834115948515875922L;
    private static final int BASE = 600;
    public static final int CLS_CREATED = BASE + 1;
    public static final int CLS_DELETED = BASE + 2;
    public static final int SLOT_CREATED = BASE + 3;
    public static final int SLOT_DELETED = BASE + 4;
    public static final int FACET_CREATED = BASE + 5;
    public static final int FACET_DELETED = BASE + 6;
    public static final int INSTANCE_CREATED = BASE + 7;
    public static final int INSTANCE_DELETED = BASE + 8;
    /*
     * @deprecated Use KnowledgeBaseEvent.FRAME_REPLACED instead.
     */
    @Deprecated
    public static final int FRAME_NAME_CHANGED = FrameEvent.NAME_CHANGED;
    public static final int FRAME_REPLACED = FrameEvent.REPLACE_FRAME;

    public static final int DEFAULT_CLS_METACLASS_CHANGED = BASE + 10;
    public static final int DEFAULT_SLOT_METACLASS_CHANGED = BASE + 11;
    public static final int DEFAULT_FACET_METACLASS_CHANGED = BASE + 12;

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
    
    public Frame getNewFrame() {
        return (Frame) getArgument3();
    }

    public void localize(KnowledgeBase kb) {
        super.localize(kb);
        setSource(kb);
    }
}
