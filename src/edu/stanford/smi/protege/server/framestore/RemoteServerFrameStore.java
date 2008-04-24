package edu.stanford.smi.protege.server.framestore;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.ProtegeJob;

public interface RemoteServerFrameStore extends Remote {

    int getClsCount(RemoteSession session) throws RemoteException;

    int getSlotCount(RemoteSession session) throws RemoteException;

    int getFacetCount(RemoteSession session) throws RemoteException;

    int getSimpleInstanceCount(RemoteSession session) throws RemoteException;

    int getFrameCount(RemoteSession session) throws RemoteException;

    // frame access
    Set getClses(RemoteSession session) throws RemoteException;

    Set getSlots(RemoteSession session) throws RemoteException;

    Set getFacets(RemoteSession session) throws RemoteException;

    Set getFrames(RemoteSession session) throws RemoteException;

    Frame getFrame(String name, RemoteSession session) throws RemoteException;

    String getFrameName(Frame frame, RemoteSession session) throws RemoteException;

    void setFrameName(Frame frame, String name, RemoteSession session) throws RemoteException;

    // frame creation/deletion
    Cls createCls(FrameID id, String name, Collection directTypes, Collection directSuperclasses,
            boolean loadDefaultValues, RemoteSession session) throws RemoteException;

    Slot createSlot(FrameID id, String name, Collection directTypes, Collection directSuperslots,
            boolean loadDefaultValues, RemoteSession session) throws RemoteException;

    Facet createFacet(FrameID id, String name, Collection directTypes, boolean loadDefaultValues, RemoteSession session)
            throws RemoteException;

    SimpleInstance createSimpleInstance(FrameID id, String name, Collection directTypes, boolean loadDefaultValues,
            RemoteSession session) throws RemoteException;

    void deleteCls(Cls cls, RemoteSession session) throws RemoteException;

    void deleteSlot(Slot slot, RemoteSession session) throws RemoteException;

    void deleteFacet(Facet facet, RemoteSession session) throws RemoteException;

    void deleteSimpleInstance(SimpleInstance simpleInstance, RemoteSession session) throws RemoteException;

    // own slots
    Set getOwnSlots(Frame frame, RemoteSession session) throws RemoteException;

    Collection getOwnSlotValues(Frame frame, Slot slot, RemoteSession session) throws RemoteException;

    List getDirectOwnSlotValues(Frame frame, Slot slot, RemoteSession session) throws RemoteException;

    int getDirectOwnSlotValuesCount(Frame frame, Slot slot, RemoteSession session) throws RemoteException;

    void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to, RemoteSession session) throws RemoteException;

    void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values, RemoteSession session)
            throws RemoteException;

    // own facets
    Set getOwnFacets(Frame frame, Slot slot, RemoteSession session) throws RemoteException;

    Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet, RemoteSession session) throws RemoteException;

    // template slots
    Set getTemplateSlots(Cls cls, RemoteSession session) throws RemoteException;

    List getDirectTemplateSlots(Cls cls, RemoteSession session) throws RemoteException;

    List getDirectDomain(Slot slot, RemoteSession session) throws RemoteException;

    Set getDomain(Slot slot, RemoteSession session) throws RemoteException;

    // Set getInheritedTemplateSlots(Cls cls, Session session) throws RemoteException;
    Set getOverriddenTemplateSlots(Cls cls, RemoteSession session) throws RemoteException;

    Set getDirectlyOverriddenTemplateSlots(Cls cls, RemoteSession session) throws RemoteException;

    void addDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    void removeDirectTemplateSlot(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    void moveDirectTemplateSlot(Cls cls, Slot slot, int index, RemoteSession session) throws RemoteException;

    // template slot values
    Collection getTemplateSlotValues(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    List getDirectTemplateSlotValues(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values, RemoteSession session)
            throws RemoteException;

    // template facets
    Set getTemplateFacets(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    Set getOverriddenTemplateFacets(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    void removeDirectTemplateFacetOverrides(Cls cls, Slot slot, RemoteSession session) throws RemoteException;

    Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet, RemoteSession session) throws RemoteException;

    List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, RemoteSession session) throws RemoteException;

    void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values, RemoteSession session)
            throws RemoteException;

    // class hierarchy
    List getDirectSuperclasses(Cls cls, RemoteSession session) throws RemoteException;

    Set getSuperclasses(Cls cls, RemoteSession session) throws RemoteException;

    List getDirectSubclasses(Cls cls, RemoteSession session) throws RemoteException;

    Set getSubclasses(Cls cls, RemoteSession session) throws RemoteException;

    void addDirectSuperclass(Cls cls, Cls superclass, RemoteSession session) throws RemoteException;

    void removeDirectSuperclass(Cls cls, Cls superclass, RemoteSession session) throws RemoteException;

    void moveDirectSubclass(Cls cls, Cls subclass, int index, RemoteSession session) throws RemoteException;

    // slot hierarchy
    List getDirectSuperslots(Slot slot, RemoteSession session) throws RemoteException;

    Set getSuperslots(Slot slot, RemoteSession session) throws RemoteException;

    List getDirectSubslots(Slot slot, RemoteSession session) throws RemoteException;

    Set getSubslots(Slot slot, RemoteSession session) throws RemoteException;

    void addDirectSuperslot(Slot slot, Slot superslot, RemoteSession session) throws RemoteException;

    void removeDirectSuperslot(Slot slot, Slot superslot, RemoteSession session) throws RemoteException;

    void moveDirectSubslot(Slot slot, Slot subslot, int index, RemoteSession session) throws RemoteException;

    // type hierarchy
    List getDirectTypes(Instance instance, RemoteSession session) throws RemoteException;

    Set getTypes(Instance instance, RemoteSession session) throws RemoteException;

    List getDirectInstances(Cls cls, RemoteSession session) throws RemoteException;

    Set getInstances(Cls cls, RemoteSession session) throws RemoteException;

    void addDirectType(Instance instance, Cls type, RemoteSession session) throws RemoteException;

    void removeDirectType(Instance instance, Cls type, RemoteSession session) throws RemoteException;

    void moveDirectType(Instance instance, Cls type, int index, RemoteSession session) throws RemoteException;

    // events
    List<EventObject> getEvents(RemoteSession session) throws RemoteException;

    // arbitrary queries
    Set getReferences(Object object, RemoteSession session) throws RemoteException;

    Set getClsesWithMatchingBrowserText(String text, Collection superclasses, int maxMatches, RemoteSession session)
            throws RemoteException;

    Set getMatchingReferences(String string, int maxMatches, RemoteSession session) throws RemoteException;

    Set getFramesWithDirectOwnSlotValue(Slot slot, Object value, RemoteSession session) throws RemoteException;

    Set getFramesWithAnyDirectOwnSlotValue(Slot slot, RemoteSession session) throws RemoteException;

    Set getFramesWithMatchingDirectOwnSlotValue(Slot slot, String value, int maxMatches, RemoteSession session)
            throws RemoteException;

    Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value, RemoteSession session) throws RemoteException;

    Set getClsesWithAnyDirectTemplateSlotValue(Slot slot, RemoteSession session) throws RemoteException;

    Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot, String value, int maxMatches, RemoteSession session)
            throws RemoteException;

    Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet, Object value, RemoteSession session)
            throws RemoteException;

    Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot, Facet facet, String value, int maxMatches,
            RemoteSession session) throws RemoteException;

    Set executeQuery(Query query, RemoteSession session) throws RemoteException;

    // closures
    Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot, RemoteSession session) throws RemoteException;

    void close(RemoteSession session) throws RemoteException;

    boolean beginTransaction(String name, RemoteSession session) throws RemoteException;

    boolean commitTransaction(RemoteSession session) throws RemoteException;

    boolean rollbackTransaction(RemoteSession session) throws RemoteException;

    Frame getFrame(FrameID id, RemoteSession session) throws RemoteException;

    Map getFrameValues(Collection frames, RemoteSession session) throws RemoteException;
    
    Object executeProtegeJob(ProtegeJob job, 
                             RemoteSession session) throws RemoteException;

    Map preload(boolean all, RemoteSession session) throws RemoteException;
}