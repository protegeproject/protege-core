package edu.stanford.smi.protege.server.narrowframestore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.TransactionMonitor;
import edu.stanford.smi.protege.util.exceptions.TransactionException;

public class ServerNarrowFrameStore 
  extends UnicastRemoteObject implements RemoteServerNarrowFrameStore {
  private static transient Logger log = Log.getLogger(ServerNarrowFrameStore.class);
  NarrowFrameStore delegate;
  NarrowFrameStore fixedDelegate;
  KnowledgeBase kb;
  private final Object kbLock;

  
  public ServerNarrowFrameStore(NarrowFrameStore delegate, 
                                KnowledgeBase kb,
                                Object kbLock) throws RemoteException {
    this.delegate = delegate;
    this.kb = kb;
    this.kbLock = kbLock;
    fixedDelegate 
      = (NarrowFrameStore) Proxy.newProxyInstance(kb.getClass().getClassLoader(),
                                                  new Class[] {NarrowFrameStore.class},
                                                  new ServerInvocationHandler());
  }
  

  
  private class ServerInvocationHandler implements InvocationHandler {

    public Object invoke(Object object, Method method, Object[] args) throws Throwable {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Server invoking client requested method " + method + " on " + object.getClass());
        if (args != null) {
          for (Object arg : args) {
            log.fine("\tArgument = " + arg);
          }
        } else {
          log.fine("No args");
        }
      }
      ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
      ClassLoader correctLoader = kb.getClass().getClassLoader();
      if (currentLoader != correctLoader) {
        if (log.isLoggable(Level.FINE)) {
          Log.getLogger().fine("Changing loader from " + currentLoader + " to " + correctLoader);
        }
        Thread.currentThread().setContextClassLoader(correctLoader);
      }
      localize(args);
      try {
        synchronized (kbLock) {
          return method.invoke(delegate, args);
        }
      } catch (InvocationTargetException ite) {
        throw ite.getCause();
      }
    }
    
  }

  private void localize(Object[] args) {
    if (args != null) {
        for (int i = 0; i < args.length; ++i) {
            Object o = args[i];
            LocalizeUtils.localize(o, kb);
        }
    }
  }


  public String getName(RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getName();
  }



  public void setName(String name, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    fixedDelegate.setName(name);
  }



  public NarrowFrameStore getDelegate(RemoteSession session) throws RemoteException {
    throw new UnsupportedOperationException("Not implemented yet");
  }



  public FrameID generateFrameID(RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.generateFrameID();
  }



  public int getFrameCount(RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getFrameCount();
  }



  public int getClsCount(RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getClsCount();
  }



  public int getSlotCount(RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getSlotCount();
  }



  public int getFacetCount(RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getFacetCount();
  }



  public int getSimpleInstanceCount(RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getSimpleInstanceCount();
  }



  public Set<Frame> getFrames(RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getFrames();
  }



  public Frame getFrame(FrameID id, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getFrame(id);
  }



  public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getValues(frame, slot, facet, isTemplate);
  }



  public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getValuesCount(frame, slot, facet, isTemplate);
  }



  public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values, RemoteSession session) 
    throws RemoteException {
    ServerFrameStore.recordCall(session);
    fixedDelegate.addValues(frame, slot, facet, isTemplate, values);
  }



  public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to, RemoteSession session) 
    throws RemoteException {
    ServerFrameStore.recordCall(session);
    fixedDelegate.moveValue(frame, slot, facet, isTemplate, from, to);
  }



  public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value, RemoteSession session) 
    throws RemoteException {
    ServerFrameStore.recordCall(session);
    fixedDelegate.removeValue(frame, slot, facet, isTemplate, value);
  }



  public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values, RemoteSession session) 
    throws RemoteException {
    ServerFrameStore.recordCall(session);
    fixedDelegate.setValues(frame, slot, facet, isTemplate, values);
  }



  public Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate, Object value, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getFrames(slot, facet, isTemplate, value);
  }



  public Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getFramesWithAnyValue(slot, facet, isTemplate);
  }



  public Set<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches, RemoteSession session)
    throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getMatchingFrames(slot, facet, isTemplate, value, maxMatches);
  }



  public Set<Reference> getReferences(Object value, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getReferences(value);
  }



  public Set<Reference> getMatchingReferences(String value, int maxMatches, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getMatchingReferences(value, maxMatches);
  }



  public Set executeQuery(Query query, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.executeQuery(query);
  }



  public void deleteFrame(Frame frame, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    fixedDelegate.deleteFrame(frame);
  }



  public void close(RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    fixedDelegate.close();
  }



  public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.getClosure(frame, slot, facet, isTemplate);
  }



  public void replaceFrame(Frame frame, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    fixedDelegate.replaceFrame(frame);
  }



  public boolean beginTransaction(String name, RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.beginTransaction(name);
  }



  public boolean commitTransaction(RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.commitTransaction();
  }



  public boolean rollbackTransaction(RemoteSession session) throws RemoteException {
    ServerFrameStore.recordCall(session);
    return fixedDelegate.rollbackTransaction();
  }

  public TransactionMonitor getTransactionStatusMonitor() throws TransactionException {
    throw new UnsupportedOperationException();
  }
}
