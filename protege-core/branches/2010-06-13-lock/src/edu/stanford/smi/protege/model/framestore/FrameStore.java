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
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

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
    Set<Cls> getClses();

    Set<Slot> getSlots();

    Set<Facet> getFacets();

    Set<Frame> getFrames();

    Frame getFrame(FrameID id);

    Frame getFrame(String name);

    String getFrameName(Frame frame);

    // frame creation/deletion
    Cls createCls(FrameID id, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaultValues);

    Slot createSlot(FrameID id, Collection directTypes, Collection directSuperslots,
            boolean loadDefaultValues);

    Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues);

    SimpleInstance createSimpleInstance(FrameID id, Collection directTypes, boolean loadDefaultValues);

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
    Set<Slot> getOwnSlots(Frame frame);

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
    Set<Facet> getTemplateFacets(Cls cls, Slot slot);

    Set getOverriddenTemplateFacets(Cls cls, Slot slot);

    Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot);

    void removeDirectTemplateFacetOverrides(Cls cls, Slot slot);

    Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet);

    List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet);

    void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values);

    // class hierarchy
    List<Cls> getDirectSuperclasses(Cls cls);

    Set getSuperclasses(Cls cls);

    List<Cls> getDirectSubclasses(Cls cls);

    Set<Cls> getSubclasses(Cls cls);

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

    List<Instance> getDirectInstances(Cls cls);

    Set<Instance> getInstances(Cls cls);

    void addDirectType(Instance instance, Cls type);

    void removeDirectType(Instance instance, Cls type);

    void moveDirectType(Instance instance, Cls type, int index);

    // events
    List<AbstractEvent> getEvents();

  /**
   * The executeQuery method allows for complex queries.  It is asynchronous 
   * so that in server-client mode the server knowledge base lock will not be
   * held for an excessive amount of time.
   *
   * The contract specifies that the implementor must call one of the 
   * QueryCallback methods in a separate thread.  This makes it possible 
   * for the caller to know how to retrieve the results in a synchronous way
   * without worrying about deadlock.
   * 
   * @param Query  the query to be executed.
   * @param QueryCallback the callback that receives the results of the query.
   */
    void executeQuery(Query query, QueryCallback callback);

    Set<Reference> getReferences(Object object);

    Set<Reference> getMatchingReferences(String string, int maxMatches);

    Set<Cls> getClsesWithMatchingBrowserText(String string, Collection superclasses, int maxMatches);

    Set<Frame> getFramesWithDirectOwnSlotValue(Slot slot, Object value);

    Set<Frame> getFramesWithAnyDirectOwnSlotValue(Slot slot);

    Set<Frame> getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches);

    Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value);

    Set<Cls> getClsesWithAnyDirectTemplateSlotValue(Slot slot);

    Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches);

    Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value);

    Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value, int maxMatches);

    // closures
    Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot);

    // transactions
    boolean beginTransaction(String name);

    boolean commitTransaction();

    boolean rollbackTransaction();

    /**
     * Retrieves a transaction status monitor for transactions.  If this call returns null
     * then it means that transactions are not supported.
     * 
     * @return A TransactionMonitor object that tracks the status of transactions.
     */
    public TransactionMonitor getTransactionStatusMonitor();

    void close();

    
    void replaceFrame(Frame original, Frame replacement);
}
