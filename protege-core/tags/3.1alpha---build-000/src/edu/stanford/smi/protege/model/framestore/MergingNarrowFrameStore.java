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
    public static final String SYSTEM_NAME = "system frames";
    private static final Object ROOT_NODE = new Object();

    private NarrowFrameStore activeFrameStore;
    private NarrowFrameStore systemFrameStore;
    private Collection availableFrameStores;
    private Tree frameStoreTree = new Tree(ROOT_NODE);

    public MergingNarrowFrameStore() {
        systemFrameStore = new InMemoryFrameDb(SYSTEM_NAME);
        addActiveFrameStore(systemFrameStore);
    }

    /**
     * A utility hack to get the merging frame store from a kb until I can decide what sort of "real" API access to
     * provide.
     */
    public static MergingNarrowFrameStore get(KnowledgeBase kb) {
        MergingNarrowFrameStore mergingFrameStore = null;
        if (kb instanceof DefaultKnowledgeBase) {
            SimpleFrameStore store = (SimpleFrameStore) ((DefaultKnowledgeBase) kb)
                    .getTerminalFrameStore();
            mergingFrameStore = (MergingNarrowFrameStore) store.getHelper().getDelegate();
        }
        return mergingFrameStore;
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

    private NarrowFrameStore getFrameStore(String name) {
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
        frameStoreTree.addChild(parentFs, childFs);
        updateAvailableFrameStores();
    }

    public void addActiveFrameStore(NarrowFrameStore frameStore) {
        addActiveFrameStore(frameStore, CollectionUtilities.EMPTY_ARRAY_LIST);
    }

    public void addActiveFrameStore(NarrowFrameStore parent, Collection childNames) {
        frameStoreTree.addChild(ROOT_NODE, parent);
        Iterator i = childNames.iterator();
        while (i.hasNext()) {
            String name = i.next().toString();
            NarrowFrameStore child = getFrameStore(name);
            frameStoreTree.addChild(parent, child);
        }
        setActiveFrameStore(parent);
    }

    public NarrowFrameStore setActiveFrameStore(NarrowFrameStore nfs) {
        NarrowFrameStore oldActiveFrameStore = activeFrameStore;
        if (nfs != null) {
            activeFrameStore = nfs;
            updateAvailableFrameStores();
        }
        dumpFrameStores();
        return oldActiveFrameStore;
    }

    private void updateAvailableFrameStores() {
        availableFrameStores = new LinkedHashSet();
        availableFrameStores.add(systemFrameStore);
        availableFrameStores.addAll(frameStoreTree.getNodeAndDescendents(activeFrameStore));
    }

    public NarrowFrameStore setActiveFrameStore(String name) {
        NarrowFrameStore nfs = getFrameStore(name);
        return setActiveFrameStore(nfs);
    }

    private void dumpFrameStores() {
        if (false)
            dumpFrameStoreList();
        if (false)
            dumpFrameStoreTree();
    }

    private void dumpFrameStoreTree() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("FrameStore tree");
        dumpFrameStoreTree(activeFrameStore, buffer, 1);
        Log.getLogger().info(buffer.toString());
    }

    private void dumpFrameStoreTree(NarrowFrameStore store, StringBuffer buffer, int indent) {
        output(buffer, store.getName(), indent);
        Iterator i = frameStoreTree.getChildren(store).iterator();
        while (i.hasNext()) {
            NarrowFrameStore child = (NarrowFrameStore) i.next();
            dumpFrameStoreTree(child, buffer, indent + 1);
        }
    }

    private void output(StringBuffer buffer, String name, int indent) {
        buffer.append("\n");
        for (int i = 0; i < indent; ++i) {
            buffer.append(" ");
        }
        buffer.append(name);
    }

    private void dumpFrameStoreList() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("FrameStores");
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore nfs = (NarrowFrameStore) i.next();
            buffer.append("\n\t" + nfs.getName());
        }

        Log.getLogger().info(buffer.toString());
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

    private List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate,
            boolean skipActive) {
        List values = new ArrayList();
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext()) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            if (fs != activeFrameStore || !skipActive) {
                values.addAll(fs.getValues(frame, slot, facet, isTemplate));
            }
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
        getDelegate().moveValue(frame, slot, facet, isTemplate, from, to);
    }

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        getDelegate().removeValue(frame, slot, facet, isTemplate, value);
    }

    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Collection newValues = new ArrayList(values);
        newValues.removeAll(getSecondaryValues(frame, slot, facet, isTemplate));
        getDelegate().setValues(frame, slot, facet, isTemplate, newValues);
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

    public Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value,
            int maxMatches) {
        Set frames = new HashSet();
        Iterator i = availableFrameStores.iterator();
        while (i.hasNext() && !hasEnoughMatches(frames.size(), maxMatches)) {
            NarrowFrameStore fs = (NarrowFrameStore) i.next();
            frames.addAll(fs.getMatchingFrames(slot, facet, isTemplate, value, maxMatches
                    - frames.size()));
        }
        return frames;
    }

    private boolean hasEnoughMatches(int size, int limit) {
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
        getDelegate().replaceFrame(frame);
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