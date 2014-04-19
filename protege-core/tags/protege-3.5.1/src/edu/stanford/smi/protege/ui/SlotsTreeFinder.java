package edu.stanford.smi.protege.ui;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotsTreeFinder extends FrameTreeFinder {

    private static final long serialVersionUID = -7353189621803272208L;

    public SlotsTreeFinder(KnowledgeBase kb, JTree tree) {
        super(kb, tree, ResourceKey.SLOT_SEARCH_FOR);
    }
    public SlotsTreeFinder(KnowledgeBase kb, JTree tree, String description) {
        super(kb, tree, description, Icons.getFindSlotIcon());
    }

    protected Collection getParents(Frame frame) {
        return ((Slot) frame).getDirectSuperslots();
    }

    protected Collection getAncestors(Frame frame) {
        return ((Slot) frame).getSuperslots();
    }

    protected boolean isCorrectType(Frame frame) {
        return frame instanceof Slot;
    }

    protected Slot getBrowserSlot(KnowledgeBase kb) {
        Slot slot;
        Cls cls = kb.getDefaultSlotMetaCls();
        if (cls == null) {
            slot = kb.getNameSlot();
        } else {
            slot = cls.getBrowserSlotPattern().getFirstSlot();
        }
        return slot;
    }
}
