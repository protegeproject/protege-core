package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * Convenience utilities for dealing with the Knowledge Base.  Most of these methods take frame names (strings) rather 
 * than frames and convert them.  Note that there is a serious performance penalty for using these methods if they are 
 * used repeatedly to perform some operation.  For repeat operations you should look up the needed frames once and then
 * store them and use the stored values.  Nevertheless, for some rare or one-off operations these methods can be useful
 * and convenient.
 *  
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ModelUtilities {

    public static void addOwnSlotValue(Frame frame, String slotName, Object value) {
        Assert.assertNotNull("value", value);
        frame.addOwnSlotValue(getSlot(frame, slotName), value);
    }

    // map from original frame to copied frame knowledge bases
    public static Map createValueMap(KnowledgeBase sourceKB, KnowledgeBase targetKB) {
        // Log.enter(ModelUtilities.class, "createValueMap", sourceKB, targetKB, sourceKB.getClses());
        Map valueMap = new HashMap();
        Iterator i = sourceKB.getFrames().iterator();
        while (i.hasNext()) {
            Frame sourceFrame = (Frame) i.next();
            // if (sourceKB != targetKB || !isCopyable(sourceFrame)) {
            if (!isCopyable(sourceFrame)) {
                Frame targetFrame = targetKB.getFrame(sourceFrame.getName());
                valueMap.put(sourceFrame, targetFrame);
                // Log.trace("valueMap.put " + sourceFrame.getName() + " - " + targetFrame, ModelUtilities.class, "createValueMap");
            }
        }
        valueMap.put(sourceKB, targetKB);
        return valueMap;
    }

    private static Facet getFacet(Frame frame, String facetName) {
        Facet facet = frame.getKnowledgeBase().getFacet(facetName);
        if (facet == null) {
            Log.getLogger().warning("missing facet: " + facetName);
        }
        return facet;
    }
    
    public static Object getOwnSlotValue(Frame frame, String name) {
        Object value;
       Slot slot = getSlot(frame, name);
       if (slot == null) {
           value = null;
           Log.getLogger().warning("unknown slot: " + name);
       } else {
           value = frame.getOwnSlotValue(slot);
       }
       return value;
   }

   public static Object getDirectOwnSlotValue(Frame frame, String name) {
       Object value;
       Slot slot = getSlot(frame, name);
       if (slot == null) {
           value = null;
           Log.getLogger().warning("unknown slot: " + name);
       } else {
           value = frame.getDirectOwnSlotValue(slot);
       }
       return value;
   }

   public static Collection getOwnSlotValues(Frame frame, String name) {
       Collection values;
       Slot slot = getSlot(frame, name);
       if (slot == null) {
           values = Collections.EMPTY_LIST;
           Log.getLogger().warning("unknown slot: " + name);
       } else {
           values = frame.getOwnSlotValues(slot);
       }
       return values;
   }

    public static List getDirectOwnSlotValues(Frame frame, String name) {
        List values;
        Slot slot = getSlot(frame, name);
        if (slot == null) {
            values = Collections.EMPTY_LIST;
            Log.getLogger().warning("unknown slot: " + name);
        } else {
            values = frame.getDirectOwnSlotValues(slot);
        }
        return values;
    }

    public List getPath(Cls cls, List list) {
        list.add(0, cls);
        Cls superclass = (Cls) CollectionUtilities.getFirstItem(cls.getDirectSuperclasses());
        if (superclass != null) {
            getPath(superclass, list);
        }
        return list;
    }

    public static List getPathToRoot(Cls cls) {
        return getPathToRoot(cls, new LinkedList());
    }

    private static List getPathToRoot(Cls cls, LinkedList list) {
        list.add(0, cls);
        Iterator i = cls.getDirectSuperclasses().iterator();
        Cls rootCls = cls.getKnowledgeBase().getRootCls();
        while (i.hasNext()) {
            Cls superclass = (Cls) i.next();
            if (cls.isVisible()) {
                List copy = new ArrayList(list);
                getPathToRoot(superclass, list);
                if(list.getFirst().equals(rootCls)) {
                    break;
                } 
                // Backtracking
                list.clear();
                list.addAll(copy);
            }
        }
        return list;
    }

    private static Slot getSlot(Frame frame, String slotName) {
        Assert.assertNotNull("frame", frame);
        Assert.assertNotNull("slot", slotName);
        Slot slot = frame.getKnowledgeBase().getSlot(slotName);
        if (slot == null) {
            Log.getLogger().warning("missing slot: " + slotName);
        }
        return slot;
    }

    public static Object getTemplateFacetValue(Cls cls, Slot slot, String facetName) {
        Object value;
        Facet facet = getFacet(cls, facetName);
        if (facet == null) {
            value = null;
        } else {
            value = cls.getTemplateFacetValue(slot, facet);
        }
        return value;
    }

    public static Collection getTemplateFacetValues(Cls cls, Slot slot, String facetName) {
        Collection values;
        Facet facet = getFacet(cls, facetName);
        if (facet == null) {
            values = Collections.EMPTY_LIST;
        } else {
            values = cls.getTemplateFacetValues(slot, facet);
        }
        return values;
    }

    private static boolean isCopyable(Frame frame) {
        // deep copy does not copy clses, slots, or facets within same kb
        return frame instanceof SimpleInstance;
    }

    public static void removeOwnSlotValue(Frame frame, String slotName, Object value) {
        frame.removeOwnSlotValue(getSlot(frame, slotName), value);
    }

    public static void setOwnSlotValue(Frame frame, String slotName, Object value) {
        frame.setOwnSlotValue(getSlot(frame, slotName), value);
    }

    public static void setOwnSlotValues(Frame frame, String slotName, Collection values) {
        frame.setOwnSlotValues(getSlot(frame, slotName), values);
    }

    public static void setTemplateFacetValue(Cls cls, Slot slot, String facetName, Object value) {
        cls.setTemplateFacetValue(slot, getFacet(cls, facetName), value);
    }

    public static void setTemplateFacetValues(Cls cls, Slot slot, String facetName, Collection values) {
        cls.setTemplateFacetValues(slot, getFacet(cls, facetName), values);
    }
}
