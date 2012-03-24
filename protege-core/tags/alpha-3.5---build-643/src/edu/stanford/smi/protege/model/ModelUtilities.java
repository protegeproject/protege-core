package edu.stanford.smi.protege.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;

/**
 * Convenience utilities for dealing with the Knowledge Base. Most of these
 * methods take frame names (strings) rather than frames and convert them. Note
 * that there is a serious performance penalty for using these methods if they
 * are used repeatedly to perform some operation. For repeat operations you
 * should look up the needed frames once and then store them and use the stored
 * values. Nevertheless, for some rare or one-off operations these methods can
 * be useful and convenient.
 *
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
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

    public List<Cls> getPath(Cls cls, List<Cls> list) {
        list.add(0, cls);
        Cls superclass = CollectionUtilities.getFirstItem(cls.getDirectSuperclasses());
        if (superclass != null) {
            getPath(superclass, list);
        }
        return list;
    }

    public static void getPropertyClosureToRoot(Instance resource, Slot parentSlot, Instance rootResource,
            List<Instance> path, Collection<List<Instance>> pathLists) {
        path.add(0, resource);

        Collection<?> parents = resource.getOwnSlotValues(parentSlot);

        for (Object parentObject : parents) {
            if (parentObject instanceof Instance) {
                Instance parentResource = (Instance) parentObject;
                if (parentResource.equals(rootResource)) {
                    List<Instance> copyPathList = new ArrayList<Instance>(path);
                    copyPathList.add(0, parentResource);
                    pathLists.add(copyPathList);
                } else if (!path.contains(parentResource)) {
                    //if (ModelUtilities.isVisibleInGUI(parentResource)) { //TODO: do we want this?
                    List<Instance> copyPath = new ArrayList<Instance>(path);
                    getPropertyClosureToRoot(parentResource, parentSlot, rootResource, copyPath, pathLists);
                    //}
                }
            }
        }
    }

    /**
     * Computes all paths from an instance to the "root instance" node by navigating on a
     * given slot.
     *
     * @param resource - an instance
     * @param parentSlot - slot to navigate on towards a "root node"
     * @param rootResource - an instance considered as the root of the navigation tree,
     *              necessary to stop the navigation.
     *
     * @return a collection of the paths from the resource to the root resource
     */
    public static Collection<List<Instance>> getPropertyClosureToRoot(Instance resource, Slot parentSlot, Instance rootResource) {
        Collection<List<Instance>> results = new ArrayList<List<Instance>>();
        if (resource.equals(rootResource)) {
            results.add(Collections.singletonList(rootResource));
            return results;
        }
        getPropertyClosureToRoot(resource, parentSlot, rootResource, new LinkedList<Instance>(), results);
        return results;
    }

    /**
     * Returns all own slot values for the slot <code>slot</code> for every instance
     * in any of the paths between an instance (<code>resource</code>) and
     * a root instances (<code>rootResource</code>) following the relationships
     * defined by the <code>parentSlot</code> slot.
     *
     * @param resource - a resource
     * @param parentSlot - the slot that is used to traverse the instance graph to the <code>rootResource</code>
     * @param rootResource - the resource that is the
     * @param slot - a slot
     *
     * @return a map containing all own slot values as keys, and each value is mapped to a list
     *      of instances which contained the value as their own slot value
     */
    public static Map<Object, List<Instance>> getPropertyValuesOnPropertyClosureToRoot(
            Instance resource, Slot parentSlot, Instance rootResource, Slot slot) {
        Map<Object, List<Instance>> result = new HashMap<Object, List<Instance>>();

        Collection<List<Instance>> propertyClosureToRoot = getPropertyClosureToRoot(resource, parentSlot, rootResource);
        Set<Instance> allNodes = new HashSet<Instance>();
        for (List<Instance> path : propertyClosureToRoot) {
            allNodes.addAll(path);
        }

        for (Instance node : allNodes) {
            Collection<?> values = node.getOwnSlotValues(slot);
            for (Object value : values) {
                if (value != null) {
                    List<Instance> nodeList = result.get(value);
                    if (nodeList == null) {
                        nodeList = new ArrayList<Instance>();
                        result.put(value, nodeList);
                    }
                    nodeList.add(node);
                }
            }
        }

        return result;
    }

    /**
     * Returns all own slot values for the slot <code>slot</code> for every superclass of a class.
     *
     * @param cls - a class
     * @param slot - a slot
     *
     * @return a map containing all own slot values as keys, and each value is mapped to a list
     *      of classes which contained the value as their own slot value
     */
    public static Map<Object, List<Instance>> getPropertyValuesOnAllSuperclasses(Cls cls, Slot slot) {
        KnowledgeBase kb = cls.getKnowledgeBase();
        Cls rootCls = kb.getRootCls();
        Slot parentSlot = kb.getSystemFrames().getDirectSuperclassesSlot();

        return getPropertyValuesOnPropertyClosureToRoot(cls, parentSlot, rootCls, slot);
    }

    public static List<Cls> getPathToRoot(Cls cls) {
        return getPathToRoot(cls, new LinkedList<Cls>());
    }

    private static List<Cls> getPathToRoot(Cls cls, LinkedList<Cls> list) {
        list.add(0, cls);
        Iterator<Cls> i = cls.getDirectSuperclasses().iterator();
        Cls rootCls = cls.getKnowledgeBase().getRootCls();
        while (i.hasNext()) {
            Cls superclass = i.next();
            if (list.contains(superclass)) {
                continue;
            }
            if (cls.isVisible()) {
                List<Cls> copy = new ArrayList<Cls>(list);
                getPathToRoot(superclass, list);
                if (list.getFirst().equals(rootCls)) {
                    break;
                }
                // Backtracking
                list.clear();
                list.addAll(copy);
            }
        }
        return list;
    }

    /**
     * Computes all paths from a class to the root node by navigating on the
     * direct superclasses slot.
     *
     * @param cls
     *            - the resource
     *
     * @return a collection of the paths from the resource to the root
     */
    public static Collection<List<Cls>> getPathsToRoot(Cls cls) {
        Collection<List<Cls>> results = new ArrayList<List<Cls>>();
        if (cls.equals(cls.getKnowledgeBase().getRootCls())) {
            results.add((List<Cls>) cls.getKnowledgeBase().getRootClses());
            return results;
        }
        getPathsToRoot(cls, new LinkedList<Cls>(), results);
        return results;
    }

    private static void getPathsToRoot(Cls resource, List<Cls> path, Collection<List<Cls>> pathLists) {
        path.add(0, resource);

        Cls rootCls = resource.getKnowledgeBase().getRootCls();
        Collection<Cls> parents = resource.getDirectSuperclasses();

        for (Cls parent : parents) {
            if (parent.equals(rootCls)) {
                List<Cls> copyPathList = new ArrayList<Cls>(path);
                copyPathList.add(0, parent);
                pathLists.add(copyPathList);
            } else if (!path.contains(parent)) {
                //if (ModelUtilities.isVisibleInGUI(parent)) { //TODO: do we want this?
                List<Cls> copyPath = new ArrayList<Cls>(path);
                getPathsToRoot(parent, copyPath, pathLists);
                //}
            }
        }
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

    public static boolean isVisibleInGUI(Frame frame) {
        return frame.getProject().getDisplayHiddenFrames() || frame.isVisible();
    }

    private static boolean isCopyable(Frame frame) {
        // deep copy does not copy clses, slots, or facets within same kb
        return frame instanceof SimpleInstance;
    }

    public static void removeOwnSlotValue(Frame frame, String slotName, Object value) {
        frame.removeOwnSlotValue(getSlot(frame, slotName), value);
    }

    public static void setOwnSlotValue(Frame frame, String slotName, Object value) {
        Slot slot = getSlot(frame, slotName);
        if (slot != null) {
            frame.setOwnSlotValue(slot, value);
        } else {
            Log.getLogger().warning(
                    "Cannot set value of slot " + slotName + " at frame " + frame + " to " + value
                            + " Inexistent slot.");
        }
    }

    public static void setOwnSlotValues(Frame frame, String slotName, Collection values) {
        Slot slot = getSlot(frame, slotName);
        if (slot != null) {
            frame.setOwnSlotValues(slot, values);
        } else {
            Log.getLogger().warning(
                    "Cannot set values of slot " + slotName + " at frame " + frame + " to " + values
                            + " Inexistent slot.");
        }
    }

    public static void setTemplateFacetValue(Cls cls, Slot slot, String facetName, Object value) {
        cls.setTemplateFacetValue(slot, getFacet(cls, facetName), value);
    }

    public static void setTemplateFacetValues(Cls cls, Slot slot, String facetName, Collection values) {
        cls.setTemplateFacetValues(slot, getFacet(cls, facetName), values);
    }
}
