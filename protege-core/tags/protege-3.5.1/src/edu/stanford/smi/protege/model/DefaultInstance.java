package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.util.*;

/**
 * Default implementation of Instance interface.  Forwards all method calls
 * to its DefaultKnowledgeBase.  This is the base class of all of the concrete
 * frame classes such as those for slot, class, etc.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class DefaultInstance extends DefaultFrame implements Instance {

    private static final long serialVersionUID = -3715893121091083687L;

    //ESCA-JAVA0016 
    DefaultInstance(KnowledgeBase kb, FrameID id) {
        super(kb, id);
    }

    protected DefaultInstance() {

    }

    public void addInstanceListener(InstanceListener listener) {
        getDefaultKnowledgeBase().addInstanceListener(this, listener);
    }

    public Frame deepCopy(KnowledgeBase targetKB, Map valueMap) {
        return copy(targetKB, valueMap, true);
    }

    public Frame shallowCopy(KnowledgeBase targetKB, Map valueMap) {
        return copy(targetKB, valueMap, false);
    }

    public Frame copy(KnowledgeBase targetKB, Map valueMap, boolean isDeep) {
        // Log.enter(this, "deepCopy", targetKB);
        KnowledgeBase sourceKB = getKnowledgeBase();
        if (targetKB == null) {
            targetKB = sourceKB;
        }
        if (valueMap == null) {
            valueMap = ModelUtilities.createValueMap(sourceKB, targetKB);
        }
        Instance copy = (Instance) valueMap.get(this);
        if (copy == null) {
            String name = null;
            Collection copyTypes = getCopyTypes(valueMap);
            copy = targetKB.createInstance(name, copyTypes);
            valueMap.put(this, copy);
        }
        return super.copy(targetKB, valueMap, isDeep);
    }

    private Collection getCopyTypes(Map valueMap) {
        Collection copyTypes = new ArrayList();
        Iterator i = getDirectTypes().iterator();
        while (i.hasNext()) {
            Cls type = (Cls) i.next();
            Cls copyType = (Cls) valueMap.get(type);
            copyTypes.add(copyType);
        }
        return copyTypes;
    }

    public String getBrowserText() {
        return getDefaultKnowledgeBase().getBrowserText(this);
    }

    public Cls getDirectType() {
        KnowledgeBase kb = getDefaultKnowledgeBase();
        return (kb == null) ? null : kb.getDirectType(this);
    }

    public Collection getDirectTypes() {
        KnowledgeBase kb = getDefaultKnowledgeBase();
        return (kb == null) ? null : kb.getDirectTypes(this);
    }

    public Object getOwnFacetValue(Slot slot, Facet facet) {
        return getDirectType().getTemplateFacetValue(slot, facet);
    }

    public Collection getOwnFacetValues(Slot slot, Facet facet) {
        return getDirectType().getTemplateFacetValues(slot, facet);
    }

    public Collection getReachableSimpleInstances() {
        Collection roots = CollectionUtilities.createCollection(this);
        return getDefaultKnowledgeBase().getReachableSimpleInstances(roots);
    }

    public boolean hasDirectType(Cls cls) {
        return getDefaultKnowledgeBase().hasDirectType(this, cls);
    }

    public boolean hasType(Cls cls) {
        return getDefaultKnowledgeBase().hasType(this, cls);
    }

    public void removeInstanceListener(InstanceListener listener) {
        getDefaultKnowledgeBase().removeInstanceListener(this, listener);
    }

    public Instance setDirectType(Cls type) {
        return getDefaultKnowledgeBase().setDirectType(this, type);
    }

    public Instance setDirectTypes(Collection types) {
        return getDefaultKnowledgeBase().setDirectTypes(this, types);
    }

    public void addDirectType(Cls type) {
        getDefaultKnowledgeBase().addDirectType(this, type);
    }

    public void removeDirectType(Cls type) {
        getDefaultKnowledgeBase().removeDirectType(this, type);
    }

    public void moveDirectType(Cls type, int index) {
        getDefaultKnowledgeBase().moveDirectType(this, type, index);
    }
}
