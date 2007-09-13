package edu.stanford.smi.protege.server.narrowframestore;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.model.query.Query;

/**
 * A variation of the NarrowFrameStore interface that throws the RemoteException.
 * 
 * Unfortunate that I need to write this.
 * 
 * @author tredmond
 *
 */
public interface RemoteServerNarrowFrameStore extends Remote {


    String getName() throws RemoteException;

    void setName(String name) throws RemoteException;

    NarrowFrameStore getDelegate() throws RemoteException;

    int getFrameCount() throws RemoteException;

    int getClsCount() throws RemoteException;

    int getSlotCount() throws RemoteException;

    int getFacetCount() throws RemoteException;

    int getSimpleInstanceCount() throws RemoteException;

    Set<Frame> getFrames() throws RemoteException;

    Frame getFrame(FrameID id) throws RemoteException;

    List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) 
      throws RemoteException;

    int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) 
      throws RemoteException;

    void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) 
      throws RemoteException;

    void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) 
      throws RemoteException;

    void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) 
      throws RemoteException;

    void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) 
      throws RemoteException;

    Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) 
      throws RemoteException;

    Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) 
      throws RemoteException;

    Set<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) 
      throws RemoteException;

    Set<Reference> getReferences(Object value) throws RemoteException;

    Set<Reference> getMatchingReferences(String value, int maxMatches) 
      throws RemoteException;

    Set executeQuery(Query query) throws RemoteException;

    void deleteFrame(Frame frame) throws RemoteException;

    void close() throws RemoteException;

    Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) 
      throws RemoteException;

    void replaceFrame(Frame frame) throws RemoteException;

    boolean beginTransaction(String name) throws RemoteException;

    boolean commitTransaction() throws RemoteException;

    boolean rollbackTransaction() throws RemoteException;


}
