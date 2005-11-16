package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.query.*;
import edu.stanford.smi.protege.util.*;

/**
 * All queries go to all frame stores. Writes go to the primary (delegate) frame store.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MergingNarrowFrameStore implements NarrowFrameStore {
    private static final Object ROOT_NODE = new Object();

    private NarrowFrameStore activeFrameStore;
    private Collection removeFrameStores = new LinkedHashSet();
    private Collection availableFrameStores = new LinkedHashSet();
    private NarrowFrameStore topFrameStore;
    private NarrowFrameStore systemFrameStore;
    private boolean queryAllFrameStores = false;

    private Tree frameStoreTree = new Tree(ROOT_NODE);

    public MergingNarrowFrameStore() {
        systemFrameStore = new InMemoryFrameDb("system");
        addActiveFrameStore(systemFrameStore);
    }

    /**
     * A utility hack to get the merging frame store from a kb until I can decide 
     * what sort of "real" API access to provide.
     */
    public static MergingNarrowFrameStore get(KnowledgeBase kb) {
        MergingNarrowFrameStore mergingFrameStore = null;
        if (kb instanceof DefaultKnowledgeBase) {
            FrameStore terminalFrameStore = ((DefaultKnowledgeBase) kb).getTerminalFrameStore();
            if (terminalFrameStore instanceof SimpleFrameStore) {
                SimpleFrameStore store = (SimpleFrameStore) terminalFrameStore;
                NarrowFrameStore nfs = store.getHelper().getDelegate();
                if (nfs instanceof MergingNarrowFrameStore) {
                    mergingFrameStore = (MergingNarrowFrameStore) nfs;
                }
            }
        }
        return mergingFrameStore;
    }

    public static NarrowFrameStore getSystemFrameStore(KnowledgeBase kb) {
        return get(kb).getSystemFrameStore();
    }

    public NarrowFrameStore getSystemFrameStore() {
        return systemFrameStore;
    }

    public Collection getAvailableFrameStores() {
        return new ArrayList(availableFrameStores);
    }

    public Collection getAllFrameStores() {
        Collection frameStores = frameStoreTree.getNodes();
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
    public void setRemoveFrameStores(Collection narrowFrameStores) {
        removeFrameStores.clear();
        removeFrameStores.addAll(narrowFrameStores);
    }

    public NarrowFrameStore getFrameStore(String name) {
        NarrowFrameStore frameStore = null;
        Iterator i = frameStoreTree.getDescendents(ROOT_NODE).iterator();
        while (i.hasNext()) {
            NarrowFrameStore testFrameStore = (NarrowFrameStore) i.next();
            if (name.equals(testFrameStore.getName())) {
                frameStore = testFrameStore;
                break;
            }
        }
        return frameStore;
    }

    public void addRelation(String parent, String child) {
        NarrowFrameStore parentFs = getFrameStore(parent);
        NarrowFrameStore childFs = getFrameStore(child);
        if (parentFs == null || childFs == null) {
            String text = "Unable to add relation between " + parent + "(" + parentFs + ")";
            text += " and " + child + "(" + childFs + ")";
            Log.getLogger().warning(text);
        } else {
            frameStoreTree.addChild(parentFs, childFs);
            updateQueryableFrameStores();
        }
    }

    public void dumpFrameStores() {
        Iterator i = frameStoreTree.getNodes().iterator();
        while (i.hasNext()) {
            NarrowFrameStore nfs = (NarrowFrameStore) i.next();
            Log.getLogger().info("*" + nfs.getName() + " " + frameStoreTree.getChildren(nfs));
        }
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
    }

    public void removeFrameStore(NarrowFrameStore frameStore) {
        frameStoreTree.removeNode(frameStore);
        availableFrameStores.remove(frameStore);
        removeFrameStores.remove(frameStore);
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
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            Object o = i.next();
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

    public FrameID generateFrameID() {
        return getDelegate().generateFrameID();
    }

    public int getFrameCount() {
        int count = 0;
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            count += fs.getFrameCount();
        }
        return count;
    }

    public Set getFrames() {
        Set frames = new HashSet();
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            frames.addAll(fs.getFrames());
        }
        return frames;
    }

    public int getClsCount() {
        int count = 0;
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            count += fs.getClsCount();
        }
        return count;
    }

    public int getSlotCount() {
        int count = 0;
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            count += fs.getSlotCount();
        }
        return count;
    }

    public int getFacetCount() {
        int count = 0;
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            count += fs.getFacetCount();
        }
        return count;
    }

    public int getSimpleInstanceCount() {
        int count = 0;
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            count += fs.getSimpleInstanceCount();
        }
        return count;
    }

    public Frame getFrame(FrameID id) {
        Frame frame = null;
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext() && frame == null) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
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

    private List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, boolean skipActive) {
        List values = null;
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            if (fs != activeFrameStore || !skipActive) {
                List fsValues = fs.getValues(frame, slot, facet, isTemplate);
                if (!fsValues.isEmpty()) {
                    if (values == null) {
                        values = fsValues;
                    } else {
                        values.addAll(fsValues);
                    }
                }
            }
        }
        if (values == null) {
            values = Collections.EMPTY_LIST;
        }
        return values;
    }

    public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        int count = 0;
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
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
        Iterator i = removeFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore frameStore = (NarrowFrameStore) i.next();
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
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            frames.addAll(fs.getFrames(slot, facet, isTemplate, value));
        }
        return frames;
    }

    public Set getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
        Set frames = new HashSet();
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            frames.addAll(fs.getFramesWithAnyValue(slot, facet, isTemplate));
        }
        return frames;
    }

    public Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) {
        Set frames = new HashSet();
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext() && !hasEnoughMatches(frames.size(), maxMatches)) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            frames.addAll(fs.getMatchingFrames(slot, facet, isTemplate, value, maxMatches - frames.size()));
        }
        return frames;
    }

    private static boolean hasEnoughMatches(int size, int limit) {
        return limit != FrameStore.UNLIMITED_MATCHES && size >= limit;
    }

    public Set getReferences(Object value) {
        Set references = new HashSet();
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            references.addAll(fs.getReferences(value));
        }
        return references;
    }

    public Set getMatchingReferences(String value, int maxMatches) {
        Set references = new HashSet();
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext() && !hasEnoughMatches(references.size(), maxMatches)) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            references.addAll(fs.getMatchingReferences(value, maxMatches - references.size()));
        }
        return references;
    }

    public Set executeQuery(Query query) {
        Set results = new HashSet();
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            results.addAll(fs.executeQuery(query));
        }
        return results;
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Set frames = new HashSet();
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            frames.addAll(fs.getClosure(frame, slot, facet, isTemplate));
        }
        return frames;
    }

    public void deleteFrame(Frame frame) {
        getDelegate().deleteFrame(frame);
    }

    public void close() {
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
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
}