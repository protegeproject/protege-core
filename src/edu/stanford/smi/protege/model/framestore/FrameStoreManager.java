package edu.stanford.smi.protege.model.framestore;

//ESCA*JAVA0100

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.exception.ProtegeIOException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.cleandispatch.CleanDispatchFrameStore;
import edu.stanford.smi.protege.model.framestore.undo.UndoFrameStore;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.Log;

/**
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameStoreManager {
    private static transient Logger log = Log.getLogger(FrameStoreManager.class);
    private FrameStore immutableNamesFrameStore;
    private FrameStore deleteSimplificationFrameStore;
    private FrameStore argumentCheckingFrameStore;
    private FrameStore cachingFrameStore;
    private FrameStore cleanDispatchFrameStore;
    private EventDispatchFrameStore eventDispatchFrameStore;
    private EventGeneratorFrameStore eventGeneratorFrameStore;
    private FrameStore eventSinkFrameStore;
    private FrameStore facetCheckingFrameStore;
    private FrameStore journalingFrameStore;
    private ModificationRecordFrameStore modificationRecordFrameStore;
    private FrameStore readonlyFrameStore;
    private UndoFrameStore undoFrameStore;
    private ChangeMonitorFrameStore changeMonitorFrameStore;
    private FrameStore traceFrameStore;

    private FrameStore terminalFrameStore;
    private FrameStore headFrameStore;

    private List<FrameStore> frameStores = new LinkedList<FrameStore>();
    private KnowledgeBase kb;

    public FrameStoreManager(KnowledgeBase kb) {
        this.kb = kb;
        createSystemFrameStores();
        terminalFrameStore = create(InMemoryFrameStore.class);
        headFrameStore = terminalFrameStore;
        addSystemFrameStores();
    }

    public FrameStore getHeadFrameStore() {
        return headFrameStore;
    }

    public<X> X getFrameStoreFromClass(Class<? extends X> clazz) {
      for (FrameStore fs = headFrameStore;  fs != null ; fs = fs.getDelegate()) {
        Object o = fs;
        Class<?> fsClass = fs.getClass();
        if (Proxy.isProxyClass(fsClass)) {
          Object invocationHandler = Proxy.getInvocationHandler(fs);
          fsClass = invocationHandler.getClass();
          o = invocationHandler;
        }
        if (clazz.isAssignableFrom(fsClass)) {
          return clazz.cast(o);
        }
      }
      return null;
    }

    protected FrameStore create(Class clas) {
        FrameStore frameStore = null;
        try {
            if (isHandlerClass(clas)) {
                frameStore = AbstractFrameStoreInvocationHandler.newInstance(clas, kb);
            } else {
                frameStore = (FrameStore) AbstractFrameStoreInvocationHandler.getInstance(clas, kb);
            }
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return frameStore;
    }

    private static boolean isHandlerClass(Class clas) {
        return AbstractFrameStoreInvocationHandler.class.isAssignableFrom(clas);
    }

    /*
     * During the early phase of initialization when there is the knowledge base has
     * no project, we assume that we are not multiuser client.  We will fix  any
     * mistakes in KnowledgeBase.setProject(Project).
     */
    private boolean isMultiUserClient() {
        return kb.getProject() != null && kb.getProject().isMultiUserClient();
    }

    private void addSystemFrameStores() {
        // closest to terminal frame store
        add(cachingFrameStore, false);
        add(eventGeneratorFrameStore, true);
        add(eventSinkFrameStore, false);
        add(journalingFrameStore, false);
        add(modificationRecordFrameStore, false);
        add(eventDispatchFrameStore, true);
        add(undoFrameStore, false);
        add(facetCheckingFrameStore, false);
        add(argumentCheckingFrameStore, true);
        add(readonlyFrameStore, false);
        add(changeMonitorFrameStore, true);
        add(cleanDispatchFrameStore, true);
        add(deleteSimplificationFrameStore, true);
        add(immutableNamesFrameStore, true);

        // for testing
        add(traceFrameStore, false);
        // head frame store
    }

    private void add(FrameStore frameStore, boolean enable) {
        Assert.assertNotNull("frame store", frameStore);
        frameStores.add(0, frameStore);
        if (enable) {
            connect(null, frameStore);
        }
    }

    private FrameStore getPreceedingEnabledFrameStore(FrameStore frameStore) {
        int index = frameStores.indexOf(frameStore);
        return getPreceedingEnabledFrameStore(index);
    }

    private FrameStore getPreceedingEnabledFrameStore(int index) {
        FrameStore preceedingEnabled = null;
        ListIterator i = frameStores.listIterator(index);
        //ESCA-JAVA0281
        while (i.hasPrevious()) {
            FrameStore prev = (FrameStore) i.previous();
            if (isEnabled(prev)) {
                preceedingEnabled = prev;
                break;
            }
        }
        return preceedingEnabled;
    }

    private boolean enable(FrameStore frameStore) {
        FrameStore preceeding = getPreceedingEnabledFrameStore(frameStore);
        boolean wasEnabled = (preceeding == null) ? headFrameStore.equals(frameStore) : preceeding.getDelegate()
                .equals(frameStore);
        if (!wasEnabled) {
            connect(preceeding, frameStore);
            frameStore.reinitialize();
        }
        return wasEnabled;
    }

    private boolean disable(FrameStore frameStore) {
        boolean wasEnabled = isEnabled(frameStore);
        if (wasEnabled) {
            FrameStore preceeding = getPreceedingEnabledFrameStore(frameStore);
            if (preceeding == null) {
                headFrameStore = frameStore.getDelegate();
                frameStore.setDelegate(null);
            } else {
                disconnect(preceeding, frameStore);
            }
            frameStore.reinitialize();
        }
        return wasEnabled;
    }

    public static boolean isEnabled(FrameStore frameStore) {
        return frameStore.getDelegate() != null;
    }

    public void close() {
        eventDispatchFrameStore.clearListeners();
        closeFrameStores();
        frameStores = null;
        kb = null;
        immutableNamesFrameStore = null;
        deleteSimplificationFrameStore = null;
        argumentCheckingFrameStore = null;
        cachingFrameStore = null;
        cleanDispatchFrameStore = null;
        eventDispatchFrameStore = null;
        eventGeneratorFrameStore = null;
        eventSinkFrameStore = null;
        facetCheckingFrameStore = null;
        journalingFrameStore = null;
        modificationRecordFrameStore = null;
        readonlyFrameStore = null;
        undoFrameStore = null;
        changeMonitorFrameStore = null;
        traceFrameStore = null;
        terminalFrameStore = null;
        headFrameStore = null;
    }

    private void closeFrameStores() {
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            FrameStore frameStore = (FrameStore) i.next();
            frameStore.close();
        }
        frameStores.clear();
        terminalFrameStore.close();
    }

    public boolean getDispatchEventsEnabled() {
        return isEnabled(eventDispatchFrameStore);
    }

    public boolean getGenerateEventsEnabled() {
        if (!isMultiUserClient()) {
            return isEnabled(eventGeneratorFrameStore);
        }
        else {
            return !isEnabled(eventSinkFrameStore);
        }
    }

    public boolean getFacetCheckingEnabled() {
        return isEnabled(facetCheckingFrameStore);
    }

    public UndoFrameStore getUndoFrameStore() {
        return undoFrameStore;
    }

    public void insertFrameStore(FrameStore newFrameStore, int position) {
        frameStores.add(position, newFrameStore);
        FrameStore fs = getPreceedingEnabledFrameStore(position);
        connect(fs, newFrameStore);
    }

    private void connect(FrameStore frameStore1, FrameStore frameStore2) {
        if (frameStore1 == null) {
            frameStore2.setDelegate(headFrameStore);
            headFrameStore = frameStore2;
        } else {
            frameStore2.setDelegate(frameStore1.getDelegate());
            frameStore1.setDelegate(frameStore2);
        }
        if (log.isLoggable(Level.FINE)) {
          log.fine("connected " + frameStore2);
          dumpFrameStores();
        }

    }

    private void disconnect(FrameStore frameStore1, FrameStore frameStore2) {
        if (frameStore1 == null) {
            headFrameStore = frameStore2.getDelegate();
        } else {
            frameStore1.setDelegate(frameStore2.getDelegate());
        }
        frameStore2.setDelegate(null);
        if (log.isLoggable(Level.FINE)) {
          log.fine("disconnected " + frameStore2);
          dumpFrameStores();
        }
    }

    public void removeFrameStore(FrameStore frameStore) {
        int position = frameStores.indexOf(frameStore);
        FrameStore preceeding = getPreceedingEnabledFrameStore(position);
        disconnect(preceeding, frameStore);
        frameStore.close();
        frameStores.remove(frameStore);
    }

    public void insertFrameStore(FrameStore newFrameStore) {
        insertFrameStore(newFrameStore, 0);
    }

    public boolean isUndoEnabled() {
        return isEnabled(undoFrameStore);
    }

    public boolean isCallCachingEnabled() {
        return isEnabled(cachingFrameStore);
    }


    public boolean setEnabled(FrameStore fs, boolean b) {
        return b ? enable(fs) : disable(fs);
    }

    public boolean setArgumentCheckingEnabled(boolean b) {
        return setEnabled(argumentCheckingFrameStore, b);
    }

    public boolean setCallCachingEnabled(boolean b) {
        return setEnabled(cachingFrameStore, b);
    }


    public boolean setCleanDispatchEnabled(boolean b) {
        return setEnabled(cleanDispatchFrameStore, b);
    }

    public boolean setEventDispatchEnabled(boolean b) {
        return setEnabled(eventDispatchFrameStore, b);
    }

    public boolean setChangeMonitorEnabled(boolean b) {
        return setEnabled(changeMonitorFrameStore, b);
    }

    public boolean setFacetCheckingEnabled(boolean enabled) {
        return setEnabled(facetCheckingFrameStore, enabled);
    }

    public boolean setGenerateEventsEnabled(boolean b) {
        if (!isMultiUserClient()) {
            return setEnabled(eventGeneratorFrameStore, b);
        }
        else {
            return !setEnabled(eventSinkFrameStore, !b);
        }
    }

    public boolean setJournalingEnabled(boolean b) {
        return setEnabled(journalingFrameStore, b);
    }

    public boolean isJournalingEnabled() {
        return isEnabled(journalingFrameStore);
    }

    public boolean setModificationRecordUpdatingEnabled(boolean enabled) {
        return setEnabled(modificationRecordFrameStore, enabled);
    }

    public boolean setUndoEnabled(boolean b) {
        return setEnabled(undoFrameStore, b);
    }

    public void setPollForEvents(boolean b) {
        eventDispatchFrameStore.setPollForEvents(b);
    }

    public void setDispatchEventsPassThrough(boolean b) {
        eventDispatchFrameStore.setPassThrough(b);
    }

    public void flushEvents() throws ProtegeException {
      try {
        eventDispatchFrameStore.flushEvents();
      } catch (InterruptedException e) {
        throw new ProtegeIOException(e);  // arguable - who interrupted this?
      }
    }

    public void removeListener(Class c, Object o, EventListener listener) {
        eventDispatchFrameStore.removeListener(c, o, listener);
    }

    public void notifyInstancesOfBrowserTextChange(Cls cls) {
        eventDispatchFrameStore.notifyInstancesOfBrowserTextChange(cls);
    }

    public void addListener(Class c, Object o, EventListener listener) {
        eventDispatchFrameStore.addListener(c, o, listener);
    }

    public List<FrameStore> getFrameStores() {
        return Collections.unmodifiableList(frameStores);
    }

    private void createSystemFrameStores() {
        immutableNamesFrameStore = create(ImmutableNamesFrameStore.class);
        deleteSimplificationFrameStore = create(DeleteSimplificationFrameStore.class);
        argumentCheckingFrameStore = create(ArgumentCheckingFrameStore.class);
        cachingFrameStore = create(CallCachingFrameStore.class);
        cleanDispatchFrameStore = create(CleanDispatchFrameStore.class);
        eventDispatchFrameStore = (EventDispatchFrameStore) create(EventDispatchFrameStore.class);
        eventGeneratorFrameStore = (EventGeneratorFrameStore) create(EventGeneratorFrameStore.class);
        eventSinkFrameStore = create(EventSinkFrameStore.class);
        facetCheckingFrameStore = create(FacetCheckingFrameStore.class);
        journalingFrameStore = create(JournalingFrameStoreHandler.class);
        modificationRecordFrameStore = (ModificationRecordFrameStore) create(ModificationRecordFrameStore.class);
        readonlyFrameStore = create(ReadOnlyFrameStoreHandler.class);
        undoFrameStore = (UndoFrameStore) create(UndoFrameStore.class);
        changeMonitorFrameStore = (ChangeMonitorFrameStore) create(ChangeMonitorFrameStore.class);
        traceFrameStore = create(TraceFrameStoreHandler.class);
    }

    public void setTerminalFrameStore(FrameStore newTerminalFrameStore) {
        FrameStore preceedingFrameStore = getPreceedingEnabledFrameStore(frameStores.size());
        preceedingFrameStore.setDelegate(newTerminalFrameStore);
        if (terminalFrameStore != newTerminalFrameStore) {
            terminalFrameStore.close();
        }
        terminalFrameStore = newTerminalFrameStore;
    }

    public void reinitialize() {
        Iterator i = frameStores.iterator();
        while (i.hasNext()) {
            FrameStore store = (FrameStore) i.next();
            store.reinitialize();
        }
        terminalFrameStore.reinitialize();
    }

    public boolean setCaching(RemoteSession session, boolean doCache) {
        return ((InMemoryFrameStore) terminalFrameStore).getHelper().setCaching(session, doCache);
    }

    public FrameStore getTerminalFrameStore() {
        return terminalFrameStore;
    }

    public void setAuthor(String userName) {
        modificationRecordFrameStore.setAuthor(userName);
    }

    public void clearAllListeners() {
        eventDispatchFrameStore.clearListeners();
    }

    public boolean hasChanged() {
        return changeMonitorFrameStore.isChanged();
    }

    public void setChanged(boolean changed) {
        changeMonitorFrameStore.setChanged(changed);
    }

    private static InvocationHandler getHandler(FrameStore fs) {
        return java.lang.reflect.Proxy.getInvocationHandler(fs);
    }

    public void startJournaling(URI journal) {
        setJournalingEnabled(true);
        JournalingFrameStoreHandler handler = (JournalingFrameStoreHandler) getHandler(journalingFrameStore);
        handler.start(journal);
    }

    public void stopJournaling() {
        JournalingFrameStoreHandler handler = (JournalingFrameStoreHandler) getHandler(journalingFrameStore);
        handler.stop();
    }

    public boolean setGenerateDeletingFrameEventsEnabled(boolean b) {
        return eventGeneratorFrameStore.setDeletingFrameEventsEnabled(b);
    }

    public void dumpFrameStores() {
      if (log.isLoggable(Level.FINE)) {
        log.fine("+-+-+-+-+-+-+-+-+-+-+-+-Dumping Frame Stores+-+-+-+-+-+-+-+-+-+-+-+-");
        log.fine("Knowledge base = " + kb);
      }
      if (log.isLoggable(Level.FINE)) {
        for (FrameStore fs = headFrameStore; fs != null; fs = fs.getDelegate()) {
          Class clazz = fs.getClass();
          if (Proxy.isProxyClass(clazz)) {
            clazz = Proxy.getInvocationHandler(fs).getClass();
          }
          log.fine("Frame store: " + clazz);
        }
      }
      if (log.isLoggable(Level.FINE)) {
        log.fine("+-+-+-+-+-+-+-+-+-+-+-+-End Frame Store Dump+-+-+-+-+-+-+-+-+-+-+-+-");
      }
    }

}
