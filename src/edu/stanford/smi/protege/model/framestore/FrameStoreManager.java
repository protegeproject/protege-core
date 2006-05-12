package edu.stanford.smi.protege.model.framestore;

//ESCA*JAVA0100

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.cleandispatch.*;
import edu.stanford.smi.protege.model.framestore.undo.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameStoreManager {
    private FrameStore deleteSimplificationFrameStore;
    private FrameStore argumentCheckingFrameStore;
    private FrameStore cachingFrameStore;
    private FrameStore cleanDispatchFrameStore;
    private EventDispatchFrameStore eventDispatchFrameStore;
    private EventGeneratorFrameStore eventGeneratorFrameStore;
    private FrameStore facetCheckingFrameStore;
    private FrameStore journalingFrameStore;
    private ModificationRecordFrameStore modificationRecordFrameStore;
    private FrameStore readonlyFrameStore;
    private UndoFrameStore undoFrameStore;
    private ChangeMonitorFrameStore changeMonitorFrameStore;
    private FrameStore traceFrameStore;
    private FrameStore busyFrameStore;

    private FrameStore terminalFrameStore;
    private FrameStore headFrameStore;

    private List frameStores = new LinkedList();
    private KnowledgeBase kb;

    public FrameStoreManager(KnowledgeBase kb) {
        this.kb = kb;
        createSystemFrameStores();
        terminalFrameStore = create(InMemoryFrameStore.class);
        headFrameStore = terminalFrameStore;
        addSystemFrameStores();
    }

    /*
     * private void dumpFrameStoreChain(String text) { Assert.assertNotNull("head frame store", headFrameStore);
     * Assert.assertNotNull("terminal frame store", terminalFrameStore);
     * 
     * System.out.println("Frame Store Chain at " + text); Set visitedFrameStores = new HashSet(); for (FrameStore fs =
     * headFrameStore; !fs.equals(terminalFrameStore); fs = fs.getDelegate()) { boolean changed =
     * visitedFrameStores.add(fs); Assert.true("loop in framestores", changed); System.out.println("\t" + fs); }
     * System.out.println("\t* End of chain"); }
     */

    public FrameStore getHeadFrameStore() {
        return headFrameStore;
    }

    public boolean isEventGeneratorFrameStoreEnabled() {
        return isEnabled(eventGeneratorFrameStore);
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

    private void addSystemFrameStores() {
        // closest to terminal frame store
        add(cachingFrameStore, false);
        add(eventGeneratorFrameStore, true);
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
        add(busyFrameStore, false);

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
            } else {
                disconnect(preceeding, frameStore);
            }
            frameStore.reinitialize();
        }
        return wasEnabled;
    }

    private static boolean isEnabled(FrameStore frameStore) {
        return frameStore.getDelegate() != null;
    }

    public void close() {
        eventDispatchFrameStore.clearListeners();
        closeFrameStores();
        frameStores = null;
        kb = null;
        deleteSimplificationFrameStore = null;
        argumentCheckingFrameStore = null;
        cachingFrameStore = null;
        cleanDispatchFrameStore = null;
        eventDispatchFrameStore = null;
        eventGeneratorFrameStore = null;
        facetCheckingFrameStore = null;
        journalingFrameStore = null;
        modificationRecordFrameStore = null;
        readonlyFrameStore = null;
        undoFrameStore = null;
        changeMonitorFrameStore = null;
        traceFrameStore = null;
        terminalFrameStore = null;
        headFrameStore = null;
        busyFrameStore = null;
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
        return isEnabled(eventGeneratorFrameStore);
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
    }

    private void disconnect(FrameStore frameStore1, FrameStore frameStore2) {
        if (frameStore1 == null) {
            headFrameStore = frameStore2.getDelegate();
        } else {
            frameStore1.setDelegate(frameStore2.getDelegate());
        }
        frameStore2.setDelegate(null);
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
    
    public boolean isBusyFlagEnabled() {
      return isEnabled(busyFrameStore);
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
    
    public boolean setBusyFlagEnabled(boolean b) {
      return setEnabled(busyFrameStore, b);
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
        return setEnabled(eventGeneratorFrameStore, b);
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

    public void removeListener(Class c, Object o, EventListener listener) {
        eventDispatchFrameStore.removeListener(c, o, listener);
    }

    public void notifyInstancesOfBrowserTextChange(Cls cls) {
        eventDispatchFrameStore.notifyInstancesOfBrowserTextChange(cls);
    }

    public void addListener(Class c, Object o, EventListener listener) {
        eventDispatchFrameStore.addListener(c, o, listener);
    }

    public List getFrameStores() {
        return Collections.unmodifiableList(frameStores);
    }

    private void createSystemFrameStores() {
        deleteSimplificationFrameStore = create(DeleteSimplificationFrameStore.class);
        argumentCheckingFrameStore = create(ArgumentCheckingFrameStore.class);
        cachingFrameStore = create(CallCachingFrameStore.class);
        busyFrameStore = create(BusyFlagFrameStore.class);
        cleanDispatchFrameStore = create(CleanDispatchFrameStore.class);
        eventDispatchFrameStore = (EventDispatchFrameStore) create(EventDispatchFrameStore.class);
        eventGeneratorFrameStore = (EventGeneratorFrameStore) create(EventGeneratorFrameStore.class);
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

}