package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;

public interface FrameStore {

    String getName();

    void setDelegate(FrameStore delegate);

    FrameStore getDelegate();

    void reinitialize();

    int UNLIMITED_MATCHES = KnowledgeBase.UNLIMITED_MATCHES;

    // frame metrics
    int getClsCount();

    int getSlotCount();

    int getFacetCount();

    int getSimpleInstanceCount();

    int getFrameCount();

    // frame access
    Set getClses();

    Set getSlots();

    Set getFacets();

    Set getFrames();

    Frame getFrame(FrameID id);

    Frame getFrame(String name);

    String getFrameName(Frame frame);

    void setFrameName(Frame frame, String name);

    // frame creation/deletion
    Cls createCls(FrameID id, String name, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaultValues);

    Slot createSlot(FrameID id, String name, Collection directTypes, Collection directSuperslots,
            boolean loadDefaultValues);

    Facet createFacet(FrameID id, String name, Collection directTypes, boolean loadDefaultValues);

    SimpleInstance createSimpleInstance(FrameID id, String name, Collection directTypes, boolean loadDefaultValues);

    /**
     * Delete a single leaf class. The operation fails if the class has subclasses or instances.
     * 
     * @param cls
     *            Class to be deleted
     * @throws IllegalArgumentException
     *             if the class has either subclasses or instances.
     */
    void deleteCls(Cls cls);

    void deleteSlot(Slot slot);

    void deleteFacet(Facet facet);

    void deleteSimpleInstance(SimpleInstance simpleInstance);

    // own slots
    Set getOwnSlots(Frame frame);

    Collection getOwnSlotValues(Frame frame, Slot slot);

    List getDirectOwnSlotValues(Frame frame, Slot slot);

    int getDirectOwnSlotValuesCount(Frame frame, Slot slot);

    void moveDirectOwnSlotValue(Frame frame, Slot slot, int indexFrom, int indexTo);

    void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values);

    // own facets
    Set getOwnFacets(Frame frame, Slot slot);

    Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet);

    // template slots
    Set getTemplateSlots(Cls cls);

    List getDirectTemplateSlots(Cls cls);

    List getDirectDomain(Slot slot);

    Set getDomain(Slot slot);

    // Set getInheritedTemplateSlots(Cls cls);
    Set getOverriddenTemplateSlots(Cls cls);

    Set getDirectlyOverriddenTemplateSlots(Cls cls);

    void addDirectTemplateSlot(Cls cls, Slot slot);

    void removeDirectTemplateSlot(Cls cls, Slot slot);

    void moveDirectTemplateSlot(Cls cls, Slot slot, int index);

    // template slot values
    Collection getTemplateSlotValues(Cls cls, Slot slot);

    List getDirectTemplateSlotValues(Cls cls, Slot slot);

    void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values);

    // template facets
    Set getTemplateFacets(Cls cls, Slot slot);

    Set getOverriddenTemplateFacets(Cls cls, Slot slot);

    Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot);

    void removeDirectTemplateFacetOverrides(Cls cls, Slot slot);

    Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet);

    List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet);

    void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values);

    // class hierarchy
    List getDirectSuperclasses(Cls cls);

    Set getSuperclasses(Cls cls);

    List getDirectSubclasses(Cls cls);

    Set getSubclasses(Cls cls);

    void addDirectSuperclass(Cls cls, Cls superclass);

    void removeDirectSuperclass(Cls cls, Cls superclass);

    void moveDirectSubclass(Cls cls, Cls subclass, int index);

    // slot hierarchy
    List getDirectSuperslots(Slot slot);

    Set getSuperslots(Slot slot);

    List getDirectSubslots(Slot slot);

    Set getSubslots(Slot slot);

    void addDirectSuperslot(Slot slot, Slot superslot);

    void removeDirectSuperslot(Slot slot, Slot superslot);

    void moveDirectSubslot(Slot slot, Slot subslot, int index);

    // type hierarchy
    List getDirectTypes(Instance instance);

    Set getTypes(Instance instance);

    List getDirectInstances(Cls cls);

    Set getInstances(Cls cls);

    void addDirectType(Instance instance, Cls type);

    void removeDirectType(Instance instance, Cls type);

    void moveDirectType(Instance instance, Cls type, int index);

    // events
    List getEvents();

    // arbitrary queries
    Set executeQuery(Query query);

    Set getReferences(Object object);

    Set getMatchingReferences(String string, int maxMatches);

    Set getClsesWithMatchingBrowserText(String string, Collection superclasses, int maxMatches);

    Set getFramesWithDirectOwnSlotValue(Slot slot, Object value);

    Set getFramesWithAnyDirectOwnSlotValue(Slot slot);

    Set getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches);

    Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value);

    Set getClsesWithAnyDirectTemplateSlotValue(Slot slot);

    Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches);

    Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value);

    Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value, int maxMatches);

    // closures
    Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot);

    // transactions
    boolean beginTransaction(String name);

    boolean commitTransaction();

    boolean rollbackTransaction();

    void close();
}