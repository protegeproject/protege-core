package edu.stanford.smi.protege.server.framestore;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.exception.ProtegeIOException;
import edu.stanford.smi.protege.exception.TransactionException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculatorStats;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.update.OntologyUpdate;
import edu.stanford.smi.protege.server.update.RemoteResponse;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.ProtegeJob;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;

public interface RemoteServerFrameStore extends Remote {
  
    public static final long HEARTBEAT_POLL_INTERVAL=3000;  // 15 seconds
    public static final long HEARTBEAT_CLIENT_DIED = 8 * HEARTBEAT_POLL_INTERVAL;

    Map<RemoteSession, Boolean> getUserInfo() throws RemoteException;
    
    FrameCalculatorStats getStats() throws RemoteException;

    int getClsCount(RemoteSession session) throws RemoteException;

    int getSlotCount(RemoteSession session) throws RemoteException;

    int getFacetCount(RemoteSession session) throws RemoteException;

    int getSimpleInstanceCount(RemoteSession session) throws RemoteException;

    int getFrameCount(RemoteSession session) throws RemoteException;

    // frame access
    Set<Cls> getClses(RemoteSession session) throws RemoteException;

    Set<Facet> getFacets(RemoteSession session) throws RemoteException;

    Set<Frame> getFrames(RemoteSession session) throws RemoteException;

    RemoteResponse<Frame> getFrame(String name, RemoteSession session) throws RemoteException;

    String getFrameName(Frame frame, RemoteSession session) throws RemoteException;

    // frame creation/deletion

    RemoteResponse<Cls> createCls(FrameID id, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaultValues, RemoteSession session) throws RemoteException;

    RemoteResponse<Slot> createSlot(FrameID id, Collection directTypes, Collection directSuperslots,
            boolean loadDefaultValues, RemoteSession session) throws RemoteException;

    RemoteResponse<Facet> createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues, RemoteSession session)
            throws RemoteException;

    RemoteResponse<SimpleInstance> createSimpleInstance(FrameID id, Collection directTypes, boolean loadDefaultValues,
            RemoteSession session) throws RemoteException;

    OntologyUpdate deleteCls(Cls cls, RemoteSession session) throws RemoteException;

    OntologyUpdate deleteSlot(Slot slot, RemoteSession session) throws RemoteException;

    OntologyUpdate deleteFacet(Facet facet, RemoteSession session) throws RemoteException;

    OntologyUpdate deleteSimpleInstance(SimpleInstance simpleInstance, RemoteSession session) throws RemoteException;

    // own slots
    Set<Slot> getOwnSlots(Frame frame, RemoteSession session) throws RemoteException;

    RemoteResponse<List> getDirectOwnSlotValues(Frame frame, Slot slot, RemoteSession session) throws RemoteException;

    int getDirectOwnSlotValuesCount(Frame frame, Slot slot, RemoteSession session) throws RemoteException;

    OntologyUpdate moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to, RemoteSession session) throws RemoteException;

    OntologyUpdate setDirectOwnSlotValues(Frame frame, Slot slot, Collection values, RemoteSession session)
            throws RemoteException;

    // Set getInheritedTemplateSlots(Cls cls, Session session) throws RemoteException;
    Set getOverriddenTemplateSlots(Cls cls, RemoteSession session) throws RemoteException;

    Set getDirectlyOverriddenTemplateSlots(Cls cls, RemoteSession session) throws RemoteException;

    OntologyUpdate addDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    OntologyUpdate removeDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    OntologyUpdate moveDirectTemplateSlot(Cls cls, Slot slot, int index, RemoteSession session) throws RemoteException;

    RemoteResponse<List> getDirectTemplateSlotValues(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    OntologyUpdate setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values, RemoteSession session)
            throws RemoteException;

    // template facets
    Set<Facet> getTemplateFacets(Cls cls, Slot slot, RemoteSession session) 
      throws RemoteException;

    Set getOverriddenTemplateFacets(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    OntologyUpdate removeDirectTemplateFacetOverrides(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet, RemoteSession session) throws RemoteException;

    RemoteResponse<List> getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, RemoteSession session) throws RemoteException;

    OntologyUpdate setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values, RemoteSession session)
            throws RemoteException;

    OntologyUpdate addDirectSuperclass(Cls cls, Cls superclass, RemoteSession session) throws RemoteException;

    OntologyUpdate removeDirectSuperclass(Cls cls, Cls superclass, RemoteSession session) throws RemoteException;

    OntologyUpdate moveDirectSubclass(Cls cls, Cls subclass, int index, RemoteSession session) throws RemoteException;

    OntologyUpdate addDirectSuperslot(Slot slot, Slot superslot, RemoteSession session) throws RemoteException;

    OntologyUpdate removeDirectSuperslot(Slot slot, Slot superslot, RemoteSession session) throws RemoteException;

    OntologyUpdate moveDirectSubslot(Slot slot, Slot subslot, int index, RemoteSession session) throws RemoteException;

    RemoteResponse<Set<Instance>> getInstances(Cls cls, RemoteSession session) throws RemoteException;

    OntologyUpdate addDirectType(Instance instance, Cls type, RemoteSession session) throws RemoteException;

    OntologyUpdate removeDirectType(Instance instance, Cls type, RemoteSession session) throws RemoteException;

    OntologyUpdate moveDirectType(Instance instance, Cls type, int index, RemoteSession session) throws RemoteException;

    // events
    RemoteResponse<List<AbstractEvent>> getEvents(RemoteSession session) throws RemoteException;

    // arbitrary queries
    Set<Reference> getReferences(Object object, RemoteSession session) throws RemoteException;

    Set<Cls> getClsesWithMatchingBrowserText(String text, Collection superclasses, int maxMatches, RemoteSession session)
            throws RemoteException;

    Set<Reference> getMatchingReferences(String string, int maxMatches, RemoteSession session) throws RemoteException;

    Set<Frame> getFramesWithDirectOwnSlotValue(Slot slot, Object value, RemoteSession session) throws RemoteException;

    Set<Frame> getFramesWithAnyDirectOwnSlotValue(Slot slot, RemoteSession session) throws RemoteException;

    Set<Frame> getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches, RemoteSession session)
            throws RemoteException;

    Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value, RemoteSession session) throws RemoteException;

    Set<Cls> getClsesWithAnyDirectTemplateSlotValue(Slot slot, RemoteSession session) throws RemoteException;

    Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches, RemoteSession session)
            throws RemoteException;

    Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value, RemoteSession session)
            throws RemoteException;

    Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value, int maxMatches,
                                                     RemoteSession session) throws RemoteException;

    RemoteResponse<Collection<Frame>> executeQuery(Query query, RemoteSession session) throws OntologyException, ProtegeIOException, RemoteException;

    // closures
    RemoteResponse<Set> getDirectOwnSlotValuesClosure(Frame frame, Slot slot, Set<Frame> missing, RemoteSession session) 
      throws RemoteException;
    
    RemoteResponse<Set> getDirectOwnSlotValuesClosure(Collection<Frame> frame, 
                                                      Slot slot, 
                                                      Set<Frame> missing, 
                                                      RemoteSession session) throws RemoteException;

    void close(RemoteSession session) throws RemoteException;
    
    Object executeProtegeJob(ProtegeJob job, RemoteSession session) throws ProtegeException, RemoteException;

    RemoteResponse<Boolean> beginTransaction(String name, RemoteSession session) throws RemoteException;

    RemoteResponse<Boolean> commitTransaction(RemoteSession session) throws RemoteException;

    RemoteResponse<Boolean> rollbackTransaction(RemoteSession session) throws RemoteException;

    Frame getFrame(FrameID id, RemoteSession session) throws RemoteException;

    OntologyUpdate replaceFrame(Frame original, Frame replacement, RemoteSession session) throws RemoteException;
    
    OntologyUpdate preload(Set<String> userFrames, boolean all, RemoteSession session) throws RemoteException;
    
    void requestValueCache(Set<Frame> frames, boolean skipDirectInstances, RemoteSession session) throws RemoteException;
    
    TransactionIsolationLevel getTransactionIsolationLevel() throws TransactionException, RemoteException;
    
   /**
   * Sets the transaction isolation level for the current connection.  Returns false if 
   * transactions are not supported.
   * 
   * @param level The desired TransactionIsolationLevel
   * @throws TransactionException 
   * @return true if transactions are supported
   */
    boolean setTransactionIsolationLevel(TransactionIsolationLevel level) throws TransactionException, RemoteException;

    void heartBeat(RemoteSession session) throws RemoteException;
    
    Set<Operation> getAllowedOperations(RemoteSession session) throws RemoteException;
    
    Set<Operation> getKnownOperations(RemoteSession session) throws RemoteException;
}
