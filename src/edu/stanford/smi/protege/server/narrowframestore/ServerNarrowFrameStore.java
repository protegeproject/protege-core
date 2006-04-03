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
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

public class ServerNarrowFrameStore 
  extends UnicastRemoteObject implements RemoteServerNarrowFrameStore {
  private static transient Logger log = Log.getLogger(ServerNarrowFrameStore.class);
  NarrowFrameStore delegate;
  NarrowFrameStore fixedDelegate;
  KnowledgeBase kb;
  
  private static final int DELAY_MSEC = Integer.getInteger("server.delay", 0).intValue();
  
  public ServerNarrowFrameStore(NarrowFrameStore delegate, KnowledgeBase kb) throws RemoteException {
    this.delegate = delegate;
    this.kb = kb;
    fixedDelegate 
      = (NarrowFrameStore) Proxy.newProxyInstance(kb.getClass().getClassLoader(),
                                                  new Class[] {NarrowFrameStore.class},
                                                  new ServerInvocationHandler());
  }
  

  
  private class ServerInvocationHandler implements InvocationHandler {

    private int nDelayedCalls = 0;

    private void delay() {
        if (DELAY_MSEC != 0) {
            SystemUtilities.sleepMsec(DELAY_MSEC);
            if (++nDelayedCalls % 10 == 0) {
                Log.getLogger().info(nDelayedCalls + " delayed calls");
            }
        }
    }


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
        return method.invoke(delegate, args);
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


  public String getName() throws RemoteException {
    return fixedDelegate.getName();
  }



  public void setName(String name) throws RemoteException {
    fixedDelegate.setName(name);
  }



  public NarrowFrameStore getDelegate() throws RemoteException {
    throw new UnsupportedOperationException("Not implemented yet");
  }



  public FrameID generateFrameID() throws RemoteException {
    return fixedDelegate.generateFrameID();
  }



  public int getFrameCount() throws RemoteException {
    return fixedDelegate.getFrameCount();
  }



  public int getClsCount() throws RemoteException {
    return fixedDelegate.getClsCount();
  }



  public int getSlotCount() throws RemoteException {
    return fixedDelegate.getSlotCount();
  }



  public int getFacetCount() throws RemoteException {
    return fixedDelegate.getFacetCount();
  }



  public int getSimpleInstanceCount() throws RemoteException {
    return fixedDelegate.getSimpleInstanceCount();
  }



  public Set<Frame> getFrames() throws RemoteException {
    return fixedDelegate.getFrames();
  }



  public Frame getFrame(FrameID id) throws RemoteException {
    return fixedDelegate.getFrame(id);
  }



  public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) throws RemoteException {
    return fixedDelegate.getValues(frame, slot, facet, isTemplate);
  }



  public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) throws RemoteException {
    return fixedDelegate.getValuesCount(frame, slot, facet, isTemplate);
  }



  public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) 
    throws RemoteException {
    fixedDelegate.addValues(frame, slot, facet, isTemplate, values);
  }



  public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) 
    throws RemoteException {
    fixedDelegate.moveValue(frame, slot, facet, isTemplate, from, to);
  }



  public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) 
    throws RemoteException {
    fixedDelegate.removeValue(frame, slot, facet, isTemplate, value);
  }



  public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) 
    throws RemoteException {
    fixedDelegate.setValues(frame, slot, facet, isTemplate, values);
  }



  public Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) throws RemoteException {
    return fixedDelegate.getFrames(slot, facet, isTemplate, value);
  }



  public Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) throws RemoteException {
    return fixedDelegate.getFramesWithAnyValue(slot, facet, isTemplate);
  }



  public Set<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches)
    throws RemoteException {
    return fixedDelegate.getMatchingFrames(slot, facet, isTemplate, value, maxMatches);
  }



  public Set<Reference> getReferences(Object value) throws RemoteException {
    return fixedDelegate.getReferences(value);
  }



  public Set<Reference> getMatchingReferences(String value, int maxMatches) throws RemoteException {
    return fixedDelegate.getMatchingReferences(value, maxMatches);
  }



  public Set executeQuery(Query query) throws RemoteException {
    return fixedDelegate.executeQuery(query);
  }



  public void deleteFrame(Frame frame) throws RemoteException {
    fixedDelegate.deleteFrame(frame);
  }



  public void close() throws RemoteException {
    fixedDelegate.close();
  }



  public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) throws RemoteException {
    return fixedDelegate.getClosure(frame, slot, facet, isTemplate);
  }



  public void replaceFrame(Frame frame) throws RemoteException {
    fixedDelegate.replaceFrame(frame);
  }



  public boolean beginTransaction(String name) throws RemoteException {
    return fixedDelegate.beginTransaction(name);
  }



  public boolean commitTransaction() throws RemoteException {
    return fixedDelegate.commitTransaction();
  }



  public boolean rollbackTransaction() throws RemoteException {
    return fixedDelegate.rollbackTransaction();
  }
}
