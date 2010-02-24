package edu.stanford.smi.protege.storage.database;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.SystemFrames;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.update.DeferredTransactionsCache;
import edu.stanford.smi.protege.server.util.FifoReader;
import edu.stanford.smi.protege.server.util.FifoWriter;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;
import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheFactory;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheBeginTransaction;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheCommitTransaction;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheRollbackTransaction;
import edu.stanford.smi.protege.util.transaction.cache.serialize.SerializedCacheUpdate;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */

public class ValueCachingNarrowFrameStore implements NarrowFrameStore {
    private static Logger log = Log.getLogger(ValueCachingNarrowFrameStore.class);
    private DatabaseFrameDb framedb;
    private WeakHashMap<String, SoftReference<DeferredTransactionsCache>> cacheMap  
                      = new WeakHashMap<String, SoftReference<DeferredTransactionsCache>>();
    private Sft directInstancesSft;
    private FifoWriter<SerializedCacheUpdate<RemoteSession, Sft,  List>> transactions
         = new FifoWriter<SerializedCacheUpdate<RemoteSession, Sft,  List>>();
    
    private Set<RemoteSession> unCachingSessions;
    /*
     * see svn revision 14782 for code to keep server-side frames in memory...
     */

    // stats
    private long totalBuildTime = 0;
    private long cacheBuilds = 0;
    private long cacheLost = 0;
    private long cacheHits = 0;

    public ValueCachingNarrowFrameStore(DatabaseFrameDb delegate) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Constructing ValueCachingNarrowFrameStore with delegate " + delegate);
        }
        framedb  = delegate;
    }
    
    public void addUnCachingSession(RemoteSession session) {
        if (unCachingSessions == null) {
            unCachingSessions = new HashSet<RemoteSession>();
        }
        unCachingSessions.add(session);
    }
    
    public boolean removeUnCachingSession(RemoteSession session) {
        if (unCachingSessions == null) {
            return false;
        }
        boolean ret = unCachingSessions.remove(session);
        if (unCachingSessions.isEmpty()) {
            unCachingSessions = null;
        }
        return ret;
    }
    
    private boolean cachingDisabledForSession() {
        return unCachingSessions != null && unCachingSessions.contains(ServerFrameStore.getCurrentSession());
    }

    public void setFrameDb(DatabaseFrameDb framedb) {
        this.framedb = framedb;
    }

    public DatabaseFrameDb getFrameDb() {
        return framedb;
    }


    public DatabaseFrameDb getDelegate() {
        return framedb;
    }

    @SuppressWarnings("unchecked")
    private DeferredTransactionsCache getCache(Frame  frame, boolean create) {
        RemoteSession session = ServerFrameStore.getCurrentSession();
        SoftReference<DeferredTransactionsCache> reference = cacheMap.get(frame.getFrameID().getName());
        DeferredTransactionsCache cache = null;
        if (reference != null) {
            cache = reference.get();
        }
        if (cache != null && cache.isInvalid()) {  // Cache's created in a transaction are short-lived.
            cache = null;
            cacheMap.remove(frame.getFrameID().getName());
        }
        if (reference != null && cache == null) {
            if (log.isLoggable(Level.FINER)) {
                log.finer("Cache for frame " + frame.getFrameID().getName() + " garbage collected");
            }
            cacheLost++;
        }
        if (cache == null && create) {
            
            String frameName = frame.getFrameID().getName();
            Cache delegateCache = CacheFactory.createEmptyCache(getTransactionStatusMonitor().getTransationIsolationLevel());
            cache = new DeferredTransactionsCache(delegateCache, new FifoReader<SerializedCacheUpdate<RemoteSession,Sft,List>>(transactions));
            if (log.isLoggable(Level.FINER)) {
                log.finer("Created cache " + cache.getCacheId() + " for frame " + frame.getFrameID().getName());
            }
            cacheMap.put(frameName, new SoftReference(cache));
            
            if (!cachingDisabledForSession()) {
                // sorry - this is a bit ugly...
                // better would be to have a setter for the system frames.
                if (directInstancesSft == null && frame.getKnowledgeBase() != null) {
                    SystemFrames frames = frame.getKnowledgeBase().getSystemFrames();
                    Slot directInstancesSlot = frames.getDirectInstancesSlot();
                    directInstancesSft = new Sft(directInstancesSlot, null, false);
                }
                if (!getTransactionStatusMonitor().inTransaction() && directInstancesSft != null) {
                    long startTime = System.nanoTime();
                    Map<Sft,List> values = framedb.getFrameValues(frame);
                    cache.startCompleteCache();
                    cache.updateCache(session, directInstancesSft);
                    for (Entry<Sft, List> entry : values.entrySet()) {
                        cache.updateCache(session, entry.getKey(), entry.getValue());
                    }
                    cache.finishCompleteCache();
                    totalBuildTime += (System.nanoTime() - startTime);
                }

                cacheBuilds++;
                logStats(Level.FINE);
                if (log.isLoggable(Level.FINER)) {
                    log.finer("Filled cache " + cache.getCacheId() + " for frame " + frame.getFrameID().getName());
                }
            }
        }
        return cache;
    }

    private void logStats(Level level) {
        if (log.isLoggable(level) && (cacheBuilds % 300 == 0)) {
            log.log(level, "------------------- Database ValueCaching Stats");
            log.log(level, "Caches built = " + cacheBuilds + " but only " + cacheMap.size() + " cache references still present.");
            log.log(level, "Cache references found but were invalid at time of use = " + cacheLost);
            log.log(level, "Ave time per build = " + (((float) totalBuildTime) / (1000 * 1000 * cacheBuilds)) + "ms.");
            log.log(level, "Ave hits per build = " + (cacheHits / cacheBuilds));
            log.log(level, "------------------- Database ValueCaching Stats");
        }
    }

    private List getValues(CacheResult<List> result) {
        List values = result.getResult();
        if (values == null) {
            values = new ArrayList();
        }
        return values;
    }



    @Override
    public String toString() {
        return "ValueCachingFrameStore(" + getName() + ")";
    }

    public void debugOutOfMemory() {
        List<Integer> junk = new ArrayList<Integer>();
        junk.add(8);
        try {
            while (true) {
                junk.addAll(junk);
            }
        }
        catch (OutOfMemoryError oops) {
            log.info("Out of memory achieved");
        }
    }

    /* ****************************************************************************
     * Narrow Frame Store interfaces
     */

    public void addValues(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, Collection values) {
        getDelegate().addValues(frame, slot, facet, isTemplate, values);
        DeferredTransactionsCache cache = getCache(frame, getTransactionStatusMonitor().inTransaction());
        if (cache != null) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            Sft sft = new Sft(slot, facet, isTemplate);
            CacheResult<List> result = cache.readCache(session, sft);
            if (result.isValid()) {
                List newValues = new ArrayList(getValues(result));
                newValues.addAll(values);
                cache.modifyCache(session, sft, newValues);
            }
            else {
                cache.modifyCache(session, sft);
            }
        }
    }

    public boolean beginTransaction(String name) {
        try {
            return getDelegate().beginTransaction(name);
        }
        finally {
            transactions.write(new CacheBeginTransaction<RemoteSession, Sft, List>(ServerFrameStore.getCurrentSession()));
        }
    }

    public boolean commitTransaction() {
        try {
            return getDelegate().commitTransaction();
        }
        finally {
            transactions.write(new CacheCommitTransaction<RemoteSession, Sft, List>(ServerFrameStore.getCurrentSession()));
        }
    }

    public boolean rollbackTransaction() {
        try {
            return getDelegate().rollbackTransaction();
        }
        finally {
            transactions.write(new CacheRollbackTransaction<RemoteSession, Sft, List>(ServerFrameStore.getCurrentSession()));
        }
    }

    public void close() {
        framedb.close();
        framedb = null;
        cacheMap.clear();
        directInstancesSft = null;
        transactions = null;
    }



    public void deleteFrame(Frame frame) {
        removeFrameReferences(frame);
        getDelegate().deleteFrame(frame);
    }
    
    private void removeFrameReferences(Frame frame) {
        if (frame instanceof Slot || frame instanceof Facet) {
            cacheMap.clear(); // OUCH!
        }
        else {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            DeferredTransactionsCache cache = getCache(frame, false);
            if (cache != null) {
                cache.invalidate(session);
            }
            for (Reference reference : framedb.getReferences(frame)) {
                cache = getCache(reference.getFrame(),false);
                if (cache != null) {
                    cache.invalidate(session);
                }
            }
        }
    }

    public void executeQuery(Query query, QueryCallback callback) {
        getDelegate().executeQuery(query, callback);
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate) {
        return getDelegate().getClosure(frame, slot, facet, isTemplate);
    }

    public int getClsCount() {
        return getDelegate().getClsCount();
    }

    public int getFacetCount() {
        return getDelegate().getFacetCount();
    }

    public Frame getFrame(FrameID id) {
        return getDelegate().getFrame(id);
    }

    public int getFrameCount() {
        return getDelegate().getFrameCount();
    }

    public Set<Frame> getFrames() {
        return getDelegate().getFrames();
    }

    public Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate,
                                Object value) {
        return getDelegate().getFrames(slot, facet, isTemplate, value);
    }

    public Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet,
                                            boolean isTemplate) {
        return getDelegate().getFramesWithAnyValue(slot, facet, isTemplate);
    }

    public Set<Frame> getMatchingFrames(Slot slot, Facet facet,
                                        boolean isTemplate, String value, int maxMatches) {
        return getDelegate().getMatchingFrames(slot, facet, isTemplate, value, maxMatches);
    }

    public Set<Reference> getMatchingReferences(String value, int maxMatches) {
        return getDelegate().getMatchingReferences(value, maxMatches);
    }

    public String getName() {
        return getDelegate().getName();
    }

    public Set<Reference> getReferences(Object value) {
        return getDelegate().getReferences(value);
    }

    public int getSimpleInstanceCount() {
        return getDelegate().getSimpleInstanceCount();
    }

    public int getSlotCount() {
        return getDelegate().getSlotCount();
    }

    public TransactionMonitor getTransactionStatusMonitor() {
        return getDelegate().getTransactionStatusMonitor();
    }

    @SuppressWarnings("unchecked")
    public List getValues(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate) {
        DeferredTransactionsCache cache = getCache(frame, true);
        RemoteSession session = ServerFrameStore.getCurrentSession();
        Sft sft = new Sft(slot, facet, isTemplate);
        CacheResult<List> result = cache.readCache(session, sft);
        if (result.isValid()) {
            cacheHits++;
            return new ArrayList(getValues(result));
        }
        else {
            List values = getDelegate().getValues(frame, slot, facet, isTemplate);
            cache.updateCache(session, sft, values);
            return values;
        }
    }

    public int getValuesCount(Frame frame, Slot slot, Facet facet,
                              boolean isTemplate) {
        DeferredTransactionsCache cache = getCache(frame, true);
        RemoteSession session = ServerFrameStore.getCurrentSession();
        Sft sft = new Sft(slot, facet, isTemplate);
        CacheResult<List> result = cache.readCache(session, sft);
        if (result.isValid()) {
            cacheHits++;
            return getValues(result).size();
        }
        else {
            return getDelegate().getValuesCount(frame, slot, facet, isTemplate);
        }
    }

    public void moveValue(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, int from, int to) {
        DeferredTransactionsCache cache = getCache(frame, getTransactionStatusMonitor().inTransaction());
        if (cache != null) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            Sft sft = new Sft(slot, facet, isTemplate);
            cache.modifyCache(session, sft);
        }
        getDelegate().moveValue(frame, slot, facet, isTemplate, from, to);
    }

    public void reinitialize() {
        cacheMap.clear();
    }

    public void removeValue(Frame frame, Slot slot, Facet facet,
                            boolean isTemplate, Object value) {
        DeferredTransactionsCache cache = getCache(frame, getTransactionStatusMonitor().inTransaction());
        if (cache != null) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            Sft sft = new Sft(slot, facet, isTemplate);
            CacheResult result = cache.readCache(session, sft);
            if (result.isValid()) {
                List oldValues = new ArrayList(getValues(result));
                oldValues.remove(value);
                cache.modifyCache(session, sft, oldValues);
            }
        }
        getDelegate().removeValue(frame, slot, facet, isTemplate, value);

    }

    public void replaceFrame(Frame frame) {
        getDelegate().replaceFrame(frame);
    }

    public void replaceFrame(Frame original, Frame replacement) {
        removeFrameReferences(original);
        getDelegate().replaceFrame(original, replacement);
    }

    public void setName(String name) {
        getDelegate().setName(name);
    }

    public void setValues(Frame frame, Slot slot, Facet facet,
                          boolean isTemplate, Collection values) {
        DeferredTransactionsCache cache = getCache(frame, getTransactionStatusMonitor().inTransaction());
        if (cache != null) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            Sft  sft = new Sft(slot, facet, isTemplate);
            cache.modifyCache(session, sft, new ArrayList(values));
        }
        getDelegate().setValues(frame, slot, facet, isTemplate, values);
    }
}

