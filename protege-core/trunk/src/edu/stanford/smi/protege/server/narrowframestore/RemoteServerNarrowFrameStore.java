package edu.stanford.smi.protege.server.narrowframestore;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.exception.TransactionException;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

/**
 * A variation of the NarrowFrameStore interface that throws the RemoteException.
 *
 * Unfortunate that I need to write this.
 *
 * @author tredmond
 *
 */
public interface RemoteServerNarrowFrameStore extends Remote {


  String getName(RemoteSession session) throws RemoteException;

  void setName(String name, RemoteSession session) throws RemoteException;

  NarrowFrameStore getDelegate(RemoteSession session) throws RemoteException;

  int getFrameCount(RemoteSession session) throws RemoteException;

  int getClsCount(RemoteSession session) throws RemoteException;

  int getSlotCount(RemoteSession session) throws RemoteException;

  int getFacetCount(RemoteSession session) throws RemoteException;

  int getSimpleInstanceCount(RemoteSession session) throws RemoteException;

  Set<Frame> getFrames(RemoteSession session) throws RemoteException;

  Frame getFrame(FrameID id, RemoteSession session) throws RemoteException;

  List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, RemoteSession session)
    throws RemoteException;

  int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate, RemoteSession session)
    throws RemoteException;

  void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values, RemoteSession session)
    throws RemoteException;

  void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to, RemoteSession session)
    throws RemoteException;

  void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value, RemoteSession session)
    throws RemoteException;

  void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values, RemoteSession session)
    throws RemoteException;

  Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate, Object value, RemoteSession session)
    throws RemoteException;

  Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate, RemoteSession session)
    throws RemoteException;

  Set<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches, RemoteSession session)
    throws RemoteException;

  Set<Reference> getReferences(Object value, RemoteSession session) throws RemoteException;

  Set<Reference> getMatchingReferences(String value, int maxMatches, RemoteSession session)
    throws RemoteException;

  Collection<Frame> executeQuery(Query query, RemoteSession session) throws RemoteException;

  void deleteFrame(Frame frame, RemoteSession session) throws RemoteException;

  void close(RemoteSession session) throws RemoteException;

  Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate, RemoteSession session)
    throws RemoteException;

  void replaceFrame(Frame frame, RemoteSession session) throws RemoteException;

  boolean beginTransaction(String name, RemoteSession session) throws RemoteException;

  boolean commitTransaction(RemoteSession session) throws RemoteException;

  boolean rollbackTransaction(RemoteSession session) throws RemoteException;

  TransactionMonitor getTransactionStatusMonitor(RemoteSession session) throws RemoteException, TransactionException;

   void replaceFrame(Frame original, Frame replacement, RemoteSession session) throws RemoteException;

   boolean setCaching(RemoteSession session, boolean doCache, RemoteSession invokingSession) throws RemoteException;
}
