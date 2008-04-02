package edu.stanford.smi.protege.storage.database;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class KnowledgeBaseUtils {

    /**
     * Check that the knowledge base passed in has all of the correct current system classes, slots, facets and instances.
     * If it doesn't then update it so that it does.  Note that some sorts of transformations are not handled.  For example
     * a class that changes into a slot is not handled.  These however will never happen for system classes (he says
     * confidently...).
     * 
     * @param kb The Knowledge base to update.
     */
    public static void update(KnowledgeBase kb) {
        boolean enabled = kb.setCleanDispatchEnabled(false);
        KnowledgeBase goodKB = new DefaultKnowledgeBase();
        Iterator i = goodKB.getFrames().iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            updateFrame(frame, kb);
        }
        kb.setCleanDispatchEnabled(enabled);
    }

    private static void updateFrame(Frame goodFrame, KnowledgeBase kb) {
        updateOwnSlots(goodFrame, kb);
        if (goodFrame instanceof Cls) {
            updateTemplateSlots((Cls) goodFrame, kb);
        }
    }

    private static void updateOwnSlots(Frame goodFrame, KnowledgeBase kb) {
        Iterator i = goodFrame.getOwnSlots().iterator();
        while (i.hasNext()) {
            Slot goodSlot = (Slot) i.next();
            updateOwnSlotValues(goodFrame, goodSlot, kb);
        }
    }

    private static void updateTemplateSlots(Cls goodCls, KnowledgeBase kb) {
        Iterator i = goodCls.getTemplateSlots().iterator();
        while (i.hasNext()) {
            Slot goodSlot = (Slot) i.next();
            updateTemplateSlotValues(goodCls, goodSlot, kb);
            updateTemplateFacets(goodCls, goodSlot, kb);
        }

    }

    private static List getNewValues(List goodValues, List currentValues, KnowledgeBase kb) {
        List values = null;
        if (!goodValues.equals(currentValues)) {
            values = new ArrayList();
            Iterator i = goodValues.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (o instanceof Frame) {
                    Frame goodFrame = (Frame) o;
                    values.add(getFrame(goodFrame, kb));
                } else {
                    values.add(o);
                }
            }
            List unusedCurrentValues = new ArrayList(currentValues);
            unusedCurrentValues.removeAll(values);
            values.addAll(getCurrentUserFrames(unusedCurrentValues));
            if (equalsSet(values, currentValues)) {
                values = null;
            }
        }
        return values;
    }

    private static boolean equalsSet(Collection c1, Collection c2) {
        return new HashSet(c1).equals(new HashSet(c2));
    }

    private static Collection getCurrentUserFrames(Collection values) {
        Collection userFrames = new ArrayList();
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Frame) {
                Frame frame = (Frame) o;
                if (!frame.isSystem()) {
                    userFrames.add(frame);
                }
            }
        }
        return userFrames;
    }

    private static Frame getFrame(Frame goodFrame, KnowledgeBase kb) {
        Frame frame = kb.getFrame(goodFrame.getName());
        if (frame == null) {
            Cls goodType = ((Instance) goodFrame).getDirectType();
            if (goodType.equals(goodFrame)) {
                Instance instance =
                    kb.createCls(
                        goodFrame.getFrameID(),
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        false);
                instance.setDirectType((Cls) instance);
                frame = instance;
            } else {
                Cls type = (Cls) getFrame(goodType, kb);
                frame = kb.createInstance(goodFrame.getFrameID(), type, false);
            }
            trace("create frame: " + goodFrame);
        }
        return frame;
    }

    private static void updateTemplateSlotValues(Cls goodCls, Slot goodSlot, KnowledgeBase kb) {
        List goodValues = goodCls.getDirectTemplateSlotValues(goodSlot);
        Cls cls = (Cls) getFrame(goodCls, kb);
        Slot slot = (Slot) getFrame(goodSlot, kb);
        List currentValues = cls.getDirectTemplateSlotValues(slot);
        List newValues = getNewValues(goodValues, currentValues, kb);
        if (newValues != null) {
            cls.setTemplateSlotValues(slot, newValues);
            trace("update template slot values: " + cls + ", " + slot + " to " + newValues);
        }
    }

    private static void updateTemplateFacets(Cls goodCls, Slot goodSlot, KnowledgeBase kb) {
        Iterator i = goodCls.getTemplateFacets(goodSlot).iterator();
        while (i.hasNext()) {
            Facet goodFacet = (Facet) i.next();
            updateTemplateFacetValues(goodCls, goodSlot, goodFacet, kb);
        }
    }

    private static void updateTemplateFacetValues(Cls goodCls, Slot goodSlot, Facet goodFacet, KnowledgeBase kb) {
        List goodValues = goodCls.getDirectTemplateFacetValues(goodSlot, goodFacet);
        Cls cls = (Cls) getFrame(goodCls, kb);
        Slot slot = (Slot) getFrame(goodSlot, kb);
        Facet facet = (Facet) getFrame(goodFacet, kb);
        List currentValues = cls.getDirectTemplateFacetValues(slot, facet);
        List newValues = getNewValues(goodValues, currentValues, kb);
        if (newValues != null) {
            cls.setTemplateFacetValues(slot, facet, newValues);
            trace("update template facet values: " + cls + ", " + slot + ", " + facet + " to " + newValues);
        }
    }

    private static void updateOwnSlotValues(Frame goodFrame, Slot goodSlot, KnowledgeBase kb) {
        List goodValues = goodFrame.getDirectOwnSlotValues(goodSlot);
        Frame frame = getFrame(goodFrame, kb);
        Slot slot = (Slot) getFrame(goodSlot, kb);
        List currentValues = frame.getDirectOwnSlotValues(slot);
        List newValues = getNewValues(goodValues, currentValues, kb);
        if (newValues != null) {
            frame.setOwnSlotValues(slot, newValues);
            trace("update own slot values: " + frame + ", " + slot + " to " + newValues);
        }
    }

    private static void trace(String s) {
        if (false) {
            Log.getLogger().info(s);
        }
    }
}
