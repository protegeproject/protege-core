package edu.stanford.smi.protege.storage.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.IncludedFrameLookup;
import edu.stanford.smi.protege.model.framestore.IncludingKBSupport;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.CacheMap;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */

public class ValueCachingNarrowFrameStore implements NarrowFrameStore, IncludingKBSupport {
    private Logger log = Log.getLogger(ValueCachingNarrowFrameStore.class);
    private NarrowFrameStore _delegate;
    private DatabaseFrameDb _framedb;
    private CacheMap<Frame, Map<Sft, List>> valuesMap 
        = new CacheMap<Frame, Map<Sft, List>>();
    private Map<RemoteSession, CacheMap<Frame, Map<Sft, List>>> transactedValuesMap
                = new HashMap<RemoteSession, CacheMap<Frame, Map<Sft, List>>>();
    private Map<RemoteSession, Set<Frame>> transactedWrites
                = new HashMap<RemoteSession, Set<Frame>>();

    public ValueCachingNarrowFrameStore(DatabaseFrameDb delegate) {
        if (log.isLoggable(Level.FINE)) {
                log.fine("Constructing ValueCachingNarrowFrameStore with delegate " + delegate);
        }
      _delegate = delegate;
      _framedb  = delegate;
    }
    
    public void setFrameDb(DatabaseFrameDb framedb) {
      _framedb = framedb;
    }
    
    public DatabaseFrameDb getFrameDb() {
      return _framedb;
    }
    
    /*
     * ---------------------------
     * Logic for finding right cache to use.  Looks at the transactions in progress, etc.
     */
    
    private CacheMap<Frame, Map<Sft, List>> getFrameToSftToValuesMap() {
        if (log.isLoggable(Level.FINE)) {
            log.fine("getting frame to stf to values map (session = " + ServerFrameStore.getCurrentSession() + ")");
        }
        TransactionMonitor tm = getTransactionStatusMonitor();
        if (!tm.inTransaction()) {
            return valuesMap;
        }
        TransactionIsolationLevel level = tm.getTransationIsolationLevel();
        if (level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
            return valuesMap;
        }
        else {
            if (log.isLoggable(Level.FINE)) {
                log.fine("getting session local map");
            }
            return transactedValuesMap.get(ServerFrameStore.getCurrentSession());
        }
    }
    
    private void markWritten(Frame frame) {
        TransactionMonitor tm = getTransactionStatusMonitor();
        if (!tm.inTransaction()) {
            return;
        } 
        TransactionIsolationLevel level = tm.getTransationIsolationLevel();
        if (level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
            return;
        }
        RemoteSession session = ServerFrameStore.getCurrentSession();
        Set<Frame> writtenFrames = transactedWrites.get(session);
        if (writtenFrames == null) {
            writtenFrames = new HashSet<Frame>();
            transactedWrites.put(session, writtenFrames);
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("frame = " + frame.getFrameID() + " modified in this transaction");
        }
        writtenFrames.add(frame);
    }


    private Map<Sft, List> lookup(Frame frame) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Getting sft to list map for frame = " + frame.getFrameID());
        }
        TransactionMonitor tm = getTransactionStatusMonitor();
        CacheMap<Frame, Map<Sft,List>> map = getFrameToSftToValuesMap();
        Map<Sft,List> values = map.get(frame);
        if (tm.inTransaction() && 
                tm.getTransationIsolationLevel() == TransactionIsolationLevel.READ_COMMITTED &&
                values == null &&
                valuesMap.get(frame) != null && 
                (transactedWrites.get(frame) == null ||
                 !transactedWrites.get(frame).contains(ServerFrameStore.getCurrentSession()))) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Copying values from untransacted cache");
            }                                  // the following looks expensive but the alternative is loadFrameIntoCache
            values = new HashMap<Sft,List>();  // remember to repair this  in commit transaction.
            for (Entry<Sft, List> entry : valuesMap.get(frame).entrySet()) {
                Sft sft = entry.getKey();
                List evalues = entry.getValue();
                values.put(sft, new ArrayList(evalues));
            }
            map.put(frame, values);
        }
        return values;
    }

    private List lookup(Map<Sft, List> map, Slot slot, Facet facet, boolean isTemplate) {
        Sft lookupSft = new Sft(slot, facet, isTemplate);
        List values =  map.get(lookupSft);
        if (log.isLoggable(Level.FINE)) {
            log.fine("finding values for map and slot = " + slot.getFrameID() +
                     " facet = " + (facet != null ? facet.getFrameID() : null) +
                     " isTemplate = " + isTemplate);
            if (values == null) log.finest("null found");
            else {
                for (Object o : values) {
                    if (o instanceof Frame) {
                        log.finest("\tFrame Value = " + ((Frame) o).getFrameID());
                    }
                    else log.finest("Value = " + o);
                }
            }
        }
        return values;
    }

    private List lookup(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("finding values for frame = " + frame.getFrameID() +
                     "and slot = " + slot.getFrameID() +
                     " facet = " + (facet != null ? facet.getFrameID() : null) +
                     " isTemplate = " + isTemplate);
        }
        List values = null;
        Map<Sft, List> sftToValuesMap = lookup(frame);
        if (sftToValuesMap != null) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Trying cache");
            }
            values = lookup(sftToValuesMap, slot, facet, isTemplate);
        }
        if (log.isLoggable(Level.FINE)) {
            if (values == null) log.finest("null found");
            else {
                for (Object o : values) {
                    if (o instanceof Frame) {
                        log.finest("\tFrame Value = " + ((Frame) o).getFrameID());
                    }
                    else log.finest("Value = " + o);
                }
            }
        }
        return values;
    }


    private static boolean isSpecial(Slot slot, Facet facet, boolean isTemplate) {
        return facet == null
            && !isTemplate
            && equals(slot, Model.SlotID.DIRECT_INSTANCES);
            // || equals(slot, Model.SlotID.DIRECT_SUBCLASSES));
    }

    private static boolean equals(Slot slot, FrameID id) {
        return slot.getFrameID().equals(id);
    }

    private List loadSpecialValuesIntoCache(Map<Sft,List> map, Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("loading special values frame = " + frame.getFrameID() +
                     " slot = " + slot.getFrameID() + 
                     " facet = " + (facet != null ? facet.getFrameID() : null) +
                     " isTemplate = " + isTemplate);
        }
        List values = _delegate.getValues(frame, slot, facet, isTemplate);
        insert(map, slot, null, false, values);
        return values;
    }




    private void setCacheValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Setting cache values for frame = " + frame.getFrameID() +
                     " slot = " + slot.getFrameID() +
                     " facet = " + (facet != null ? facet.getFrameID() : null) + 
                     " isTemplate = " + isTemplate);
            if (values == null) log.finest("null values put");
            else {
                for (Object o : values) {
                    if (o instanceof Frame) {
                        log.finest("\tFrame Value = " + ((Frame) o).getFrameID());
                    }
                    else log.finest("Value = " + o);
                }
            }
        }
        Map sftToValuesMap = lookup(frame);
        TransactionMonitor transactionMonitor = getTransactionStatusMonitor();
        if (sftToValuesMap != null) {
            insert(sftToValuesMap, slot, facet, isTemplate, values);
        }
    }

    private void insert(Map<Sft, List> map, Slot slot, Facet facet, boolean isTemplate, 
                        Collection values) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Inserting cache values for " +
                     " slot = " + slot.getFrameID() +
                     " facet = " + (facet != null ? facet.getFrameID() : null) + 
                     " isTemplate = " + isTemplate);
        }
        // if (values == null || values.size() == 0) {
        if (values == null) {
            remove(map, slot, facet, isTemplate);
        } else {
            List valueList = lookup(map, slot, facet, isTemplate);
            if (valueList == null) {
                map.put(new Sft(slot, facet, isTemplate), new ArrayList(values));
            } else {
                valueList.clear();
                valueList.addAll(values);
            }
        }
    }

    private void remove(Map<Sft, List> map, Slot slot, Facet facet, boolean isTemplate) {
        Sft lookupSft = new Sft(slot, facet, isTemplate);
        map.remove(lookupSft);
    }

    private void addCacheValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Adding cache values for frame = " + frame.getFrameID() +
                     " slot = " + slot.getFrameID() +
                     " facet = " + (facet != null ? facet.getFrameID() : null) + 
                     " isTemplate = " + isTemplate);
            if (values == null) log.finest("null values put");
            else {
                for (Object o : values) {
                    if (o instanceof Frame) {
                        log.finest("\tFrame Value = " + ((Frame) o).getFrameID());
                    }
                    else log.finest("Value = " + o);
                }
            }
        }
        Map sftToValuesMap = lookup(frame);
        if (sftToValuesMap != null) {
            List list = lookup(sftToValuesMap, slot, facet, isTemplate);
            if (list == null) {
                if (!isSpecial(slot, facet, isTemplate)) {
                    insert(sftToValuesMap, slot, facet, isTemplate, values);
                }
            } else {
                list.addAll(values);
            }
        }
    }

    private void removeCacheValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        List list = lookup(frame, slot, facet, isTemplate);
        if (list != null) {
            list.remove(value);
        }
    }
    
    private void moveCacheValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        List list = lookup(frame, slot, facet, isTemplate);
        if (list != null) {
            Object value = list.remove(from);
            list.add(to, value);
        }
    }
    
    private void deleteCacheFrame(Frame frame) {
        getFrameToSftToValuesMap().remove(frame);
        for (Frame frameKey : getFrameToSftToValuesMap().getKeys()) {
            Map<Sft, List> sftToValuesMap =  getFrameToSftToValuesMap().get(frameKey);
            if (sftToValuesMap != null) {
                for ( Map.Entry<Sft,List> entry : sftToValuesMap.entrySet()) {
                    Sft sft = entry.getKey();
                    if (contains(sft, frame)) {
                        getFrameToSftToValuesMap().remove(frameKey);
                    } else {
                        List values = (List) entry.getValue();
                        values.remove(frame);
                    }
                }
            }
        }
    }

    private static boolean contains(Sft sft, Frame frame) {
        return frame.equals(sft.getSlot()) || frame.equals(sft.getFacet());
    }

    private Map<Sft,List> loadFrameIntoCache(Frame frame) {
        Map<Sft, List> sftToValuesMap = _framedb.getFrameValues(frame);
        getFrameToSftToValuesMap().put(frame, sftToValuesMap);
        return sftToValuesMap;
    }

    

    public void clearCache() {
        valuesMap.clear();
        for (CacheMap<Frame, Map<Sft, List>> map : transactedValuesMap.values()) {
            map.clear();
        }
        transactedWrites.clear();
    }

    
    

  /*----------------------------IncludingKBSupport Interfaces */

    public void setIncludedFrames(IncludedFrameLookup iframes) {
      if (_delegate instanceof IncludingKBSupport) {
        ((IncludingKBSupport) _delegate).setIncludedFrames(iframes);
      }
    }

  /*----------------------------NarrowFrameStore Interfaces */

    public String getName() {
        return _delegate.getName();
    }

    public void setName(String name) {
        _delegate.setName(name);
    }


    public NarrowFrameStore getDelegate() {
        return _delegate;
    }

    public FrameID generateFrameID() {
        return _delegate.generateFrameID();
    }

    public int getFrameCount() {
        return getDelegate().getFrameCount();
    }
    public int getClsCount() {
        return getDelegate().getClsCount();
    }
    public int getSlotCount() {
        return getDelegate().getSlotCount();
    }
    public int getFacetCount() {
        return getDelegate().getFacetCount();
    }
    public int getSimpleInstanceCount() {
        return getDelegate().getSimpleInstanceCount();
    }

    public Set getFrames() {
        return getDelegate().getFrames();
    }

    public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        TransactionMonitor transactionMonitor = getTransactionStatusMonitor();
        Map<Sft, List> sftToValuesMap = lookup(frame);
        if (sftToValuesMap == null) {
            sftToValuesMap = loadFrameIntoCache(frame);
        }
        List values = lookup(sftToValuesMap, slot, facet, isTemplate);
        if (values == null) {
            if (isSpecial(slot, facet, isTemplate)) {
                values = loadSpecialValuesIntoCache(sftToValuesMap, frame, slot, facet, isTemplate);
            } else {
                values = Collections.EMPTY_LIST;
            }
        }
        return values;
    }

    // I don't really want to cache the return value here but I don't want to have to keep going back to the db
    // if it is small. Thus is the value is small we load the frame, otherwise we just go to the db on every call 
    private static final int LOAD_THRESHOLD = 10;
    public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        int count;
        Map<Sft, List> sftToValuesMap = lookup(frame);
        if (sftToValuesMap == null) {
            TransactionMonitor transactionMonitor = getTransactionStatusMonitor();
            count = getDelegate().getValuesCount(frame, slot, facet, isTemplate);
            if (count < LOAD_THRESHOLD) {
                sftToValuesMap = loadFrameIntoCache(frame);
            }
        } else {
            List values = lookup(sftToValuesMap, slot, facet, isTemplate);
            if (values == null) {
                count = getDelegate().getValuesCount(frame, slot, facet, isTemplate);
            } else {
                count = values.size();
            }
        }
        return count;
    }

    public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        _delegate.addValues(frame, slot, facet, isTemplate, values);
        addCacheValues(frame, slot, facet, isTemplate, values);
        markWritten(frame);
    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        // _delegate.moveValue(frame, slot, facet, isTemplate, from, to);
        List values = new ArrayList(getValues(frame, slot, facet, isTemplate));
        Object o = values.remove(from);
        values.add(to, o);
        _delegate.setValues(frame, slot, facet, isTemplate, values);

        moveCacheValue(frame, slot, facet, isTemplate, from, to);
        markWritten(frame);
    }

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        _delegate.removeValue(frame, slot, facet, isTemplate, value);
        removeCacheValue(frame, slot, facet, isTemplate, value);
        markWritten(frame);
    }

    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        _delegate.setValues(frame, slot, facet, isTemplate, values);
        setCacheValues(frame, slot, facet, isTemplate, values);
        markWritten(frame);
    }

    public Set getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        return _delegate.getFrames(slot, facet, isTemplate, value);
    }


    public Frame getFrame(FrameID id) {
        return _delegate.getFrame(id);
    }


    public Set getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
        return _delegate.getFramesWithAnyValue(slot, facet, isTemplate);
    }



    public Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) {
        return _delegate.getMatchingFrames(slot, facet, isTemplate, value, maxMatches);
    }

    public Set getReferences(Object value) {
        return _delegate.getReferences(value);
    }

    public Set getMatchingReferences(String value, int maxMatches) {
        return _delegate.getMatchingReferences(value, maxMatches);
    }


    public void executeQuery(Query query, QueryCallback callback) {
      _delegate.executeQuery(query, callback);
    }

    public void deleteFrame(Frame frame) {
        deleteCacheFrame(frame);
        _delegate.deleteFrame(frame);
        markWritten(frame);
    }

    public void close() {
        _delegate.close();
        _delegate = null;
        valuesMap = null;
        transactedValuesMap = null;
        transactedWrites = null;
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        throw new UnsupportedOperationException();
    }


    public void replaceFrame(Frame frame) {
        clearCache();
        getDelegate().replaceFrame(frame);
    }


    public boolean beginTransaction(String name) {
      TransactionMonitor monitor = getTransactionStatusMonitor();
      try {
          if (!monitor.inTransaction()) {
              TransactionIsolationLevel level = monitor.getTransationIsolationLevel();
              CacheMap<Frame, Map<Sft,List>> newValuesMap = null;
              if (level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
                  newValuesMap = new CacheMap<Frame, Map<Sft,List>>();
                  RemoteSession session = ServerFrameStore.getCurrentSession();
                  transactedValuesMap.put(session, newValuesMap);
              }
          }
      } catch (Throwable t) {
          Log.getLogger().log(Level.WARNING, "Exception caught opening transaction", t);
          Log.getLogger().warning("Trying to be conservative...");
          clearCache();
      }
      return getDelegate().beginTransaction(name);
    }

    public boolean commitTransaction() {
        TransactionMonitor monitor = getTransactionStatusMonitor();
        int oldNesting = monitor.getNesting();
        boolean committed = getDelegate().commitTransaction();
        try {
            if (committed && oldNesting == 1) { // exitted transaction
                TransactionIsolationLevel level = monitor.getTransationIsolationLevel();
                RemoteSession session = ServerFrameStore.getCurrentSession();
                Set<Frame> writes = transactedWrites.get(session);
                if (writes != null) {
                    if (level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
                        for (Frame frame : writes) {
                            valuesMap.remove(frame);
                        }
                    }
                    if (level == TransactionIsolationLevel.READ_COMMITTED) {
                        for (Frame frame : writes) {
                            for (RemoteSession othersession : transactedValuesMap.keySet()) {
                                transactedValuesMap.get(othersession).remove(frame);
                            }
                        }
                    }
                }
            }
            if (oldNesting == 1) {
                RemoteSession session = ServerFrameStore.getCurrentSession();
                transactedValuesMap.remove(session);
                transactedWrites.remove(session);               
            }
        } catch (Throwable t) {
            Log.getLogger().log(Level.WARNING, "Exception caught closing transaction", t);
            clearCache();
        }
        return committed;
    }

    public boolean rollbackTransaction() {
        TransactionMonitor monitor = getTransactionStatusMonitor();
        int oldNesting = monitor.getNesting();
        boolean rolledback = getDelegate().rollbackTransaction();
        if (oldNesting == 1) {
            if (!rolledback) {  // if the rollback failed then everything is very bad
                clearCache();   // even this might not be conservative enough
            }
            RemoteSession session = ServerFrameStore.getCurrentSession();
            transactedValuesMap.remove(session);
            transactedWrites.remove(session);
        }
        return rolledback;
    }

    public TransactionMonitor getTransactionStatusMonitor() {
      return getDelegate().getTransactionStatusMonitor();
    }


}

