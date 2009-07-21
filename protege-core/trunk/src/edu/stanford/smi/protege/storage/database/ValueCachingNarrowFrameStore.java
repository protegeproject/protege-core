package edu.stanford.smi.protege.storage.database;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
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
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ArrayListMultiMap;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MultiMap;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;
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
    private WeakHashMap<String, SoftReference<ValueCache>> cacheMap 
                     = new WeakHashMap<String, SoftReference<ValueCache>>();
    private List<SerializedCacheUpdate<RemoteSession, Sft, List>> transactions = new ArrayList<SerializedCacheUpdate<RemoteSession, Sft, List>>();
    private Sft directInstancesSft;
    
    // Hacks to keep things in memory
    private MultiMap<RemoteSession, String> framesModifiedInTransaction = new ArrayListMultiMap<RemoteSession, String>();
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
    
    public void setFrameDb(DatabaseFrameDb framedb) {
        this.framedb = framedb;
    }
    
    public DatabaseFrameDb getFrameDb() {
        return framedb;
    }


    public DatabaseFrameDb getDelegate() {
        return framedb;
    }
    
    private ValueCache getCache(Frame  frame, boolean create) {
        RemoteSession session = ServerFrameStore.getCurrentSession();
        SoftReference<ValueCache> reference = cacheMap.get(frame.getFrameID().getName());
        ValueCache cache = null;
        if (reference != null) {
                cache = reference.get();
        }
        if (reference != null && cache == null) {
            if (log.isLoggable(Level.FINER)) {
                log.finer("Cache for frame " + frame.getFrameID().getName() + " garbage collected");
            }
            cacheLost++;
        }
        if (cache == null && create) {
            cache = new ValueCache(getTransactionStatusMonitor().getTransationIsolationLevel(), transactions);
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
            String frameName = frame.getFrameID().getName();
            cacheMap.put(frameName, new SoftReference(cache));
            cacheBuilds++;
            logStats(Level.FINE);
            if (log.isLoggable(Level.FINER)) {
                log.finer("Created and filled cache " + cache.getCacheId() + " for frame " + frame.getFrameID().getName());
            }
        }
        return cache;
    }
    
    private void logStats(Level level) {
        if (log.isLoggable(level) && (cacheBuilds % 300 == 0)) {
            log.log(level, "------------------- Database ValueCaching Stats");
            log.log(level, "Caches built " + cacheBuilds + " caches lost " + cacheLost);
            log.log(level, "Garbage collection tax = " + (100.0 * cacheLost) / cacheBuilds + "%");
            log.log(level, "Ave time per build = " + (((float) totalBuildTime) / (1000 * 1000 * cacheBuilds)) + "ms.");
            log.log(level, "Ave hits per build = " + (cacheHits / cacheBuilds));
            log.log(level, "Potentially cached frame count = " + cacheMap.size());
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
        
        /* ****************************************************************************
         * Routines for holding stuff in memory so that  we do not forget.   There are two cases.
         * 1. when a cache is modified while in a transaction, we don't want to forget this cache.
         *    Otherwise we may start a new cache and forget the changes when the transaction commits.
         *    This seems like an unlikely issue to arise but it might be possible in the client-server.
         * 2. On the server side, we need a mechanism to keep things in the cache.  Otherwise the 
         *    server has no reason to remember cache values.  I have  no idea how big this saved cache should
         *    be.
         */
    private void saveFramesModifiedInTransaction(RemoteSession session, Frame frame) {
        if (getTransactionStatusMonitor().inTransaction()) {
            framesModifiedInTransaction.addValue(session, frame.getFrameID().getName());
        }
    }
    
    private void unsaveFramesModifiedInTransaction() {
        RemoteSession session = ServerFrameStore.getCurrentSession();
        if (!getTransactionStatusMonitor().inTransaction()) {
            framesModifiedInTransaction.removeKey(session);
        }
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
            ValueCache cache = getCache(frame, getTransactionStatusMonitor().inTransaction());
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
                saveFramesModifiedInTransaction(session, frame);
            }
        }

        public boolean beginTransaction(String name) {
            transactions.add(new CacheBeginTransaction<RemoteSession, Sft, List>(ServerFrameStore.getCurrentSession()));
            return getDelegate().beginTransaction(name);
        }
        
        public boolean commitTransaction() {
            transactions.add(new CacheCommitTransaction<RemoteSession, Sft, List>(ServerFrameStore.getCurrentSession()));
            try {
                return getDelegate().commitTransaction();
            }
            finally {
                unsaveFramesModifiedInTransaction();
            }
        }
        
        public boolean rollbackTransaction() {
            transactions.add(new CacheRollbackTransaction<RemoteSession, Sft, List>(ServerFrameStore.getCurrentSession()));
            try {
                return getDelegate().rollbackTransaction();
            }
            finally {
                unsaveFramesModifiedInTransaction();
            }
        }

        public void close() {
            framedb.close();
            framedb = null;
            cacheMap.clear();
            transactions.clear();
            directInstancesSft = null;
            framesModifiedInTransaction.clear();
        }



        public void deleteFrame(Frame frame) {
                ValueCache cache = getCache(frame, false);
                if (cache != null) {
                        RemoteSession session = ServerFrameStore.getCurrentSession();
                        cache.delete(session);
                }
            getDelegate().deleteFrame(frame);
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

        public List getValues(Frame frame, Slot slot, Facet facet,
                              boolean isTemplate) {
        ValueCache cache = getCache(frame, true);
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
        ValueCache cache = getCache(frame, true);
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
            ValueCache cache = getCache(frame, getTransactionStatusMonitor().inTransaction());
            if (cache != null) {
                RemoteSession session = ServerFrameStore.getCurrentSession();
                Sft sft = new Sft(slot, facet, isTemplate);
                cache.modifyCache(session, sft);
                saveFramesModifiedInTransaction(session, frame);
            }
            getDelegate().moveValue(frame, slot, facet, isTemplate, from, to);
        }

        public void reinitialize() {
            cacheMap.clear();
        }

        public void removeValue(Frame frame, Slot slot, Facet facet,
                                boolean isTemplate, Object value) {
        ValueCache cache = getCache(frame, getTransactionStatusMonitor().inTransaction());
        if (cache != null) {
            RemoteSession session = ServerFrameStore.getCurrentSession();
            Sft sft = new Sft(slot, facet, isTemplate);
            CacheResult result = cache.readCache(session, sft);
            if (result.isValid()) {
                List oldValues = new ArrayList(getValues(result));
                oldValues.remove(value);
                cache.modifyCache(session, sft, oldValues);
                saveFramesModifiedInTransaction(session, frame);
            }
        }
        getDelegate().removeValue(frame, slot, facet, isTemplate, value);
                
        }

        public void replaceFrame(Frame frame) {
            getDelegate().replaceFrame(frame);
        }

        public void replaceFrame(Frame original, Frame replacement) {
            cacheMap.remove(original.getFrameID().getName());
            getDelegate().replaceFrame(original, replacement);
        }

        public void setName(String name) {
            getDelegate().setName(name);
        }

        public void setValues(Frame frame, Slot slot, Facet facet,
                              boolean isTemplate, Collection values) {
            ValueCache  cache = getCache(frame, getTransactionStatusMonitor().inTransaction());
            if (cache != null) {
                RemoteSession session = ServerFrameStore.getCurrentSession();
                Sft  sft = new Sft(slot, facet, isTemplate);
                cache.modifyCache(session, sft, new ArrayList(values));
                saveFramesModifiedInTransaction(session, frame);
            }
            getDelegate().setValues(frame, slot, facet, isTemplate, values);
        }
}

