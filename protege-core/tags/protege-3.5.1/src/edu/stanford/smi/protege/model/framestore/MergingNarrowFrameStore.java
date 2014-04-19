package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.exception.ProtegeError;
import edu.stanford.smi.protege.exception.ProtegeIOException;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.model.query.SynchronizeQueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protege.util.Tree;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

/**
 * All queries go to all frame stores. Writes go to the primary (delegate) frame store.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MergingNarrowFrameStore implements NarrowFrameStore {
    private static Logger log = Log.getLogger(MergingNarrowFrameStore.class);

    private final Object kbLock;

    private static final NarrowFrameStore ROOT_NODE = new PlaceHolderNarrowFrameStore();

    private NarrowFrameStore activeFrameStore;
    private final Collection<NarrowFrameStore> removeFrameStores
                                = new LinkedHashSet<NarrowFrameStore>();
    private final Collection<NarrowFrameStore> availableFrameStores
                               = new LinkedHashSet<NarrowFrameStore>();
    private NarrowFrameStore topFrameStore;
    private final NarrowFrameStore systemFrameStore;

    private boolean queryAllFrameStores = false;
    private boolean suppressDuplicates = false;

    private final Tree<NarrowFrameStore> frameStoreTree
      = new Tree<NarrowFrameStore>(ROOT_NODE);

    public MergingNarrowFrameStore(Object kbLock) {
        systemFrameStore = new InMemoryFrameDb("system");
        addActiveFrameStore(systemFrameStore);
        this.kbLock = kbLock;
    }

    /**
     * A utility hack to get the merging frame store from a kb until I can decide
     * what sort of "real" API access to provide.
     */
    public static MergingNarrowFrameStore get(KnowledgeBase kb) {
        if (kb instanceof DefaultKnowledgeBase) {
            FrameStore terminalFrameStore = ((DefaultKnowledgeBase) kb).getTerminalFrameStore();
            if (terminalFrameStore instanceof SimpleFrameStore) {
                SimpleFrameStore store = (SimpleFrameStore) terminalFrameStore;
                for (NarrowFrameStore nfs = store.getHelper().getDelegate();
                     nfs != null;
                	 nfs = nfs.getDelegate()) {
                	if (nfs instanceof MergingNarrowFrameStore) {
                        return (MergingNarrowFrameStore) nfs;
                    }
                }
            }
        }
        return null;
    }

    public static NarrowFrameStore getSystemFrameStore(KnowledgeBase kb) {
        return get(kb).getSystemFrameStore();
    }

    public static NarrowFrameStore getNarrowFrameStore(KnowledgeBase kb, Class clazz) {
      NarrowFrameStore nfs = MergingNarrowFrameStore.get(kb);
      while ((nfs = nfs.getDelegate()) != null) {
        if (clazz.isAssignableFrom(nfs.getClass())) {
          return nfs;
        }
      }
      return null;
    }

    public NarrowFrameStore getSystemFrameStore() {
        return systemFrameStore;
    }

    public Collection<NarrowFrameStore> getAvailableFrameStores() {
        return new ArrayList<NarrowFrameStore>(availableFrameStores);
    }

    public Collection<NarrowFrameStore> getAllFrameStores() {
        Collection<NarrowFrameStore> frameStores = frameStoreTree.getNodes();
        frameStores.add(systemFrameStore);
        return frameStores;
    }

    public String getName() {
        return StringUtilities.getClassName(this);
    }

    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    public NarrowFrameStore getActiveFrameStore() {
        return activeFrameStore;
    }

    /**
     * Set frame stores to remove values from.  The collection should not include
     * the "active" frame store, which is handled separately.
     * @param narrowFrameStores Collection of NarrowFrameStores
     */
    public void setRemoveFrameStores(Collection<NarrowFrameStore> narrowFrameStores) {
        removeFrameStores.clear();
        removeFrameStores.addAll(narrowFrameStores);
    }

    public NarrowFrameStore getFrameStore(String name) {
        NarrowFrameStore frameStore = null;
        Iterator<NarrowFrameStore> i
          = frameStoreTree.getDescendents(ROOT_NODE).iterator();
        while (i.hasNext()) {
            NarrowFrameStore testFrameStore = i.next();
            if (name.equals(testFrameStore.getName())) {
                frameStore = testFrameStore;
                break;
            }
        }
        return frameStore;
    }

    public void addRelation(String parent, String child) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Adding frame store relation between "
                     + parent + " and " + child);
        }
        NarrowFrameStore parentFs = getFrameStore(parent);
        NarrowFrameStore childFs = getFrameStore(child);
        if (parentFs == null || childFs == null) {
            String text = "Unable to add relation between " + parent + "(" + parentFs + ")";
            text += " and " + child + "(" + childFs + ")";
            Log.getLogger().warning(text);
        } else {
            if (log.isLoggable(Level.FINE)) {
                log.fine("...Added");
                dumpFrameStores(Level.FINE);
            }
            frameStoreTree.addChild(parentFs, childFs);
            updateQueryableFrameStores();
        }
    }

    public void dumpFrameStores() {
        dumpFrameStores(Level.INFO);
    }

    public void dumpFrameStores(Level lev) {
        if (!log.isLoggable(lev)) {
            return;
        }
        log.log(lev, "------------Starting Merged Narrow Frame Store Dump");
        Iterator<NarrowFrameStore> i = frameStoreTree.getNodes().iterator();
        while (i.hasNext()) {
            NarrowFrameStore nfs = i.next();
            log.log(lev,"*" + nfs.getClass() + " " + nfs.getName() + " " + frameStoreTree.getChildren(nfs));
        }
        if (activeFrameStore != null) {
            log.log(lev, "Active frame store = " + activeFrameStore.getName());
        }
        log.log(lev, "------------Merged Narrow Frame Store Dump Completed");
    }

    public void addActiveFrameStore(NarrowFrameStore frameStore) {
        addActiveFrameStore(frameStore, CollectionUtilities.EMPTY_ARRAY_LIST);
    }

    public void addActiveChildFrameStore(NarrowFrameStore childFrameStore, String parentName) {
        if (childFrameStore == null) {
            throw new IllegalArgumentException("Null child");
        }
        NarrowFrameStore parentFrameStore = getFrameStore(parentName);
        if (parentFrameStore == null) {
            throw new IllegalArgumentException("Null parent: " + parentName);
        }
        frameStoreTree.addChild(parentFrameStore, childFrameStore);
        setActiveFrameStore(childFrameStore);
        if (log.isLoggable(Level.FINE)) {
            log.fine("Adding child frame store (and making active?) "
                     + childFrameStore);
            dumpFrameStores(Level.FINE);
        }
    }

    public void removeFrameStore(NarrowFrameStore frameStore) {
        frameStoreTree.removeNode(frameStore);
        availableFrameStores.remove(frameStore);
        removeFrameStores.remove(frameStore);
        if (log.isLoggable(Level.FINE)) {
            log.fine("removing frame store " + frameStore);
        }
    }

    public void addActiveFrameStore(NarrowFrameStore parent, Collection childNames) {
        if (parent == null) {
            throw new IllegalArgumentException("Null parent");
        }
        frameStoreTree.addChild(ROOT_NODE, parent);
        Iterator i = childNames.iterator();
        while (i.hasNext()) {
            String name = i.next().toString();
            NarrowFrameStore child = getFrameStore(name);
            if (child == null) {
                Log.getLogger().warning("Unable to find child FrameStore: " + name);
            } else {
                frameStoreTree.addChild(parent, child);
            }
        }
        setActiveFrameStore(parent);
        if (log.isLoggable(Level.FINE)) {
            log.fine("Added new active frame store");
            dumpFrameStores(Level.FINE);
        }
    }

    public Slot getNameSlot() {
      return (Slot) systemFrameStore.getFrame(Model.SlotID.NAME);
    }

    /**
     * Sets the top frame store, used to decide the root below which all framesstores are
     * "available". If you don't set a top frame store then the active frame store is assumed
     * to be the top.  If a top frame store is set then changing the active frame store
     * does not affect the top.
     */
    public void setTopFrameStore(String name) {
        topFrameStore = (name == null) ? null : getFrameStore(name);
        updateQueryableFrameStores();
    }

    public NarrowFrameStore getTopFrameStore() {
        return (topFrameStore == null) ? activeFrameStore : topFrameStore;
    }

    public NarrowFrameStore setActiveFrameStore(NarrowFrameStore nfs) {
    	if (log.isLoggable(Level.FINE)) {
    		log.fine("Setting the delegate for " + this + " to " + nfs);
    	}
        NarrowFrameStore oldActiveFrameStore = activeFrameStore;
        if (nfs != null) {
            activeFrameStore = nfs;
            updateQueryableFrameStores();
        }
        return oldActiveFrameStore;
    }

    public void setQueryAllFrameStores(boolean b) {
        queryAllFrameStores = b;
        updateQueryableFrameStores();
    }

    public void setSuppressDuplicates(boolean suppressDuplicates) {
        this.suppressDuplicates = suppressDuplicates;
    }

    private void updateQueryableFrameStores() {
        availableFrameStores.clear();
        availableFrameStores.add(systemFrameStore);
        if (queryAllFrameStores) {
            availableFrameStores.addAll(frameStoreTree.getDescendents(ROOT_NODE));
        } else {
            availableFrameStores.addAll(frameStoreTree.getNodeAndDescendents(getTopFrameStore()));
        }
        checkAvailable();
    }

    private void checkAvailable() {
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore o = i.next();
            if (o == null) {
                Log.getLogger().severe("Null frame store found");
                i.remove();
            }
        }
    }

    public NarrowFrameStore setActiveFrameStore(String name) {
        NarrowFrameStore nfs = getFrameStore(name);
        if (nfs == null) {
            Log.getLogger().severe("Missing frame store: " + name);
        }
        return setActiveFrameStore(nfs);
    }

    public NarrowFrameStore getDelegate() {
        return activeFrameStore;
    }

    // -----------------------------------------------------------


    public int getFrameCount() {
        int count = 0;
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            count += fs.getFrameCount();
        }
        return count;
    }

    public Set getFrames() {
        Set frames = new HashSet();
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            frames.addAll(fs.getFrames());
        }
        return frames;
    }

    public int getClsCount() {
        int count = 0;
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            count += fs.getClsCount();
        }
        return count;
    }

    public int getSlotCount() {
        int count = 0;
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            count += fs.getSlotCount();
        }
        return count;
    }

    public int getFacetCount() {
        int count = 0;
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            count += fs.getFacetCount();
        }
        return count;
    }

    public int getSimpleInstanceCount() {
        int count = 0;
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            count += fs.getSimpleInstanceCount();
        }
        return count;
    }

    public Frame getFrame(FrameID id) {
        Frame frame = null;
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext() && frame == null) {
            NarrowFrameStore fs = i.next();
            frame = fs.getFrame(id);
        }
        return frame;
    }

    public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return getValues(frame, slot, facet, isTemplate, false);
    }

    private List getSecondaryValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return getValues(frame, slot, facet, isTemplate, true);
    }

    @SuppressWarnings("unchecked")
    private List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, boolean skipActive) {
        Collection values = null;
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            if (fs != activeFrameStore || !skipActive) {
                List fsValues = fs.getValues(frame, slot, facet, isTemplate);
                if (!fsValues.isEmpty()) {
                    if (values == null && !suppressDuplicates) {
                        values = fsValues;
                    }
                    else if (values == null && suppressDuplicates) {
                        values = new LinkedHashSet(fsValues);
                    }
                    else {
                        values.addAll(fsValues);
                    }
                }
            }
        }
        if (values == null) {
            values = Collections.EMPTY_LIST;
        }
        if (!(values instanceof List)) {
            values = new ArrayList(values);
        }
        return (List) values;
    }

    public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        int count = 0;
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            count += fs.getValuesCount(frame, slot, facet, isTemplate);
        }
        return count;
    }

    public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        getDelegate().addValues(frame, slot, facet, isTemplate, values);
    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        List secondaryValues = getSecondaryValues(frame, slot, facet, isTemplate);
        if (secondaryValues.isEmpty()) {
            getDelegate().moveValue(frame, slot, facet, isTemplate, from, to);
        } else {
            List values = getValues(frame, slot, facet, isTemplate);
            Object fromObject = values.get(from);
            Object toObject = values.get(to);
            if (!secondaryValues.contains(fromObject) && !secondaryValues.contains(toObject)) {
                from -= secondaryValues.size();
                to -= secondaryValues.size();
                getDelegate().moveValue(frame, slot, facet, isTemplate, from, to);
            }
        }
    }

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        getDelegate().removeValue(frame, slot, facet, isTemplate, value);
        if (!removeFrameStores.isEmpty()) {
            removeValueFromRemoveFrameStores(frame, slot, facet, isTemplate, value);
        }
    }

    private void removeValueFromRemoveFrameStores(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Iterator<NarrowFrameStore> i = removeFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore frameStore = i.next();
            frameStore.removeValue(frame, slot, facet, isTemplate, value);
        }
    }

    private void removeValuesFromRemoveFrameStores(Frame frame, Slot slot, Facet facet, boolean isTemplate,
            Collection valuesToRemove) {
        Iterator i = valuesToRemove.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            removeValueFromRemoveFrameStores(frame, slot, facet, isTemplate, value);
        }
    }

    @SuppressWarnings("unchecked")
    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Collection secondaryValues = getSecondaryValues(frame, slot, facet, isTemplate);

        if (!removeFrameStores.isEmpty()) {
            Collection valuesToRemove = new ArrayList(secondaryValues);
            valuesToRemove.removeAll(values);
            removeValuesFromRemoveFrameStores(frame, slot, facet, isTemplate, valuesToRemove);
        }

        Collection valuesToAdd = new ArrayList(values);
        valuesToAdd.removeAll(secondaryValues);
        getDelegate().setValues(frame, slot, facet, isTemplate, valuesToAdd);

    }

    public Set getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        Set frames = new HashSet();
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            frames.addAll(fs.getFrames(slot, facet, isTemplate, value));
        }
        return frames;
    }

    public Set getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
        Set frames = new HashSet();
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            frames.addAll(fs.getFramesWithAnyValue(slot, facet, isTemplate));
        }
        return frames;
    }

    public Set<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) {
        Set<Frame> frames = new HashSet<Frame>();
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext() && !hasEnoughMatches(frames.size(), maxMatches)) {
            NarrowFrameStore fs = i.next();
            frames.addAll(fs.getMatchingFrames(slot, facet, isTemplate, value, maxMatches - frames.size()));
        }
        return frames;
    }

    private static boolean hasEnoughMatches(int size, int limit) {
        return limit != FrameStore.UNLIMITED_MATCHES && size >= limit;
    }

    public Set<Reference> getReferences(Object value) {
        Set references = new HashSet<Reference>();
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            references.addAll(fs.getReferences(value));
        }
        return references;
    }

    public Set<Reference> getMatchingReferences(String value, int maxMatches) {
        Set references = new HashSet();
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext() && !hasEnoughMatches(references.size(), maxMatches)) {
            NarrowFrameStore fs = i.next();
            references.addAll(fs.getMatchingReferences(value, maxMatches - references.size()));
        }
        return references;
    }

    public void executeQuery(final Query query, final QueryCallback callback) {
      new Thread() {
        @Override
        public void run() {
          try {
            SynchronizeQueryCallback sync = new SynchronizeQueryCallback(kbLock);
            Set<Frame> results = new HashSet<Frame>();
            Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
            while (i.hasNext()) {
              NarrowFrameStore fs = i.next();
              fs.executeQuery(query, sync);
              results.addAll(sync.waitForResults());
            }
            callback.provideQueryResults(results);
          } catch (OntologyException oe) {
            callback.handleError(oe);
          } catch (ProtegeIOException ioe) {
            callback.handleError(ioe);
          } catch (Throwable t) {
            Log.getLogger().log(Level.WARNING, "Exception found during query", t);
            callback.handleError(new ProtegeError(t));
          }
        }
      };

    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Set frames = new HashSet();
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            frames.addAll(fs.getClosure(frame, slot, facet, isTemplate));
        }
        return frames;
    }

    public void deleteFrame(Frame frame) {
        getDelegate().deleteFrame(frame);
    }

    public void close() {
        Iterator<NarrowFrameStore> i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = i.next();
            fs.close();
        }
        availableFrameStores.clear();
    }

    public void replaceFrame(Frame frame) {
        Iterator i = getAllFrameStores().iterator();
        while (i.hasNext()) {
            NarrowFrameStore narrowFrameStore = (NarrowFrameStore) i.next();
            narrowFrameStore.replaceFrame(frame);
        }
    }

    public boolean beginTransaction(String name) {
        return getDelegate().beginTransaction(name);
    }

    public boolean commitTransaction() {
        return getDelegate().commitTransaction();
    }

    public boolean rollbackTransaction() {
        return getDelegate().rollbackTransaction();
    }

    public TransactionMonitor getTransactionStatusMonitor()  {
      return getDelegate().getTransactionStatusMonitor();
    }

	public void reinitialize() {
		for (NarrowFrameStore nfs : availableFrameStores) {
			nfs.reinitialize();
		}
	}

	public boolean setCaching(RemoteSession session, boolean doCache) {
	    boolean ret = false;
        for (NarrowFrameStore nfs : availableFrameStores) {
            ret = ret | nfs.setCaching(session, doCache);
        }
        return ret;
	}

    public void replaceFrame(Frame original, Frame replacement) {
      for (NarrowFrameStore nfs : availableFrameStores) {
        nfs.replaceFrame(original, replacement);
      }
    }
}

