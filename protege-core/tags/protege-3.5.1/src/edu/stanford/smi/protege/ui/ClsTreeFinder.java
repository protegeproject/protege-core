package edu.stanford.smi.protege.ui;

import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;

/**
 * Implementation of the Finder interface to locate and highlight classes whose names match a given string.  
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ClsTreeFinder extends FrameTreeFinder {

    private static final long serialVersionUID = 4059780789067261580L;

    public ClsTreeFinder(KnowledgeBase kb, JTree tree) {
        this(kb, tree, ResourceKey.CLASS_SEARCH_FOR);
    }

    public ClsTreeFinder(KnowledgeBase kb, JTree tree, ResourceKey key) {
        super(kb, tree, key);
    }

    public ClsTreeFinder(KnowledgeBase kb, JTree tree, String description) {
        this(kb, tree, description, Icons.getFindClsIcon());
    }

    public ClsTreeFinder(KnowledgeBase kb, JTree tree, String description, Icon icon) {
        super(kb, tree, description, icon);
    }

    protected Collection getParents(Frame frame) {
        return ((Cls) frame).getDirectSuperclasses();
    }

    protected Collection getAncestors(Frame frame) {
        return ((Cls) frame).getSuperclasses();
    }

    protected boolean isCorrectType(Frame frame) {
        return frame instanceof Cls;
    }

    protected Slot getBrowserSlot(KnowledgeBase kb) {
        Slot slot;
        Cls cls = kb.getDefaultClsMetaCls();
        if (cls == null) {
            slot = kb.getNameSlot();
        } else {
            slot = cls.getBrowserSlotPattern().getFirstSlot();
        }
        return slot;
    }

    protected Set<Frame> getMatchingFrames(String text, int maxMatches) {
        if (!text.endsWith("*")) {
            text += '*';
        }
        return new HashSet<Frame>(getKnowledgeBase().getClsesWithMatchingBrowserText(text, Collections.EMPTY_LIST, maxMatches));
    }

}
