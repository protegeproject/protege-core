package edu.stanford.smi.protege.storage.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.TransactionException;
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
import edu.stanford.smi.protege.util.CacheMap;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */

public class ValueCachingNarrowFrameStore implements NarrowFrameStore, IncludingKBSupport {
    private Logger log = Log.getLogger(ValueCachingNarrowFrameStore.class);
    private DatabaseFrameDb _delegate;
    private CacheMap<Frame, Map<Sft, List>> _frameToSftToValuesMap 
        = new CacheMap<Frame, Map<Sft, List>>();

    public String getName() {
        return _delegate.getName();
    }

    public void setName(String name) {
         _delegate.setName(name);
    }

    public void close() {
        _delegate.close();
        _delegate = null;
        _frameToSftToValuesMap = null;
    }

    public ValueCachingNarrowFrameStore(DatabaseFrameDb delegate) {
    	if (log.isLoggable(Level.FINE)) {
    		log.fine("Constructing ValueCachingNarrowFrameStore with delegate " + delegate);
    	}
        _delegate = delegate;
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        throw new UnsupportedOperationException();
    }

    public DatabaseFrameDb getDelegate() {
        return _delegate;
    }

    public FrameID generateFrameID() {
        return _delegate.generateFrameID();
    }
    public Frame getFrame(FrameID id) {
        return _delegate.getFrame(id);
    }

    private Map<Sft, List> lookup(Frame frame) {
        return _frameToSftToValuesMap.get(frame);
    }

    private List lookup(Map<Sft, List> map, Slot slot, Facet facet, boolean isTemplate) {
        Sft lookupSft = new Sft(slot, facet, isTemplate);
        return map.get(lookupSft);
    }

    private List lookup(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        List values = null;
        Map<Sft, List> sftToValuesMap = lookup(frame);
        if (sftToValuesMap != null) {
            values = lookup(sftToValuesMap, slot, facet, isTemplate);
        }
        return values;
    }

    public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        TransactionMonitor transactionMonitor = getTransactionStatusMonitor();
        if (transactionMonitor != null && transactionMonitor.existsTransaction()) {
          return getDelegate().getValues(frame, slot, facet, isTemplate);
        }
        Map sftToValuesMap = lookup(frame);
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

    private static boolean isSpecial(Slot slot, Facet facet, boolean isTemplate) {
        return facet == null
            && !isTemplate
            && equals(slot, Model.SlotID.DIRECT_INSTANCES);
            // || equals(slot, Model.SlotID.DIRECT_SUBCLASSES));
    }

    private static boolean equals(Slot slot, FrameID id) {
        return slot.getFrameID().equals(id);
    }

    private List loadSpecialValuesIntoCache(Map map, Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        List values = _delegate.getValues(frame, slot, facet, isTemplate);
        insert(map, slot, null, false, values);
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
            if (count < LOAD_THRESHOLD && (transactionMonitor == null ||
                                           !transactionMonitor.existsTransaction())) {
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

    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        _delegate.setValues(frame, slot, facet, isTemplate, values);
        setCacheValues(frame, slot, facet, isTemplate, values);
    }

    public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        _delegate.addValues(frame, slot, facet, isTemplate, values);
        addCacheValues(frame, slot, facet, isTemplate, values);
    }

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        _delegate.removeValue(frame, slot, facet, isTemplate, value);
        removeCacheValue(frame, slot, facet, isTemplate, value);
    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        // _delegate.moveValue(frame, slot, facet, isTemplate, from, to);
        List values = new ArrayList(getValues(frame, slot, facet, isTemplate));
        Object o = values.remove(from);
        values.add(to, o);
        _delegate.setValues(frame, slot, facet, isTemplate, values);

        moveCacheValue(frame, slot, facet, isTemplate, from, to);
    }

    public Set getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        return _delegate.getFrames(slot, facet, isTemplate, value);
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

    public void deleteFrame(Frame frame) {
        deleteCacheFrame(frame);
        _delegate.deleteFrame(frame);
    }

    public Set executeQuery(Query query) {
        return _delegate.executeQuery(query);
    }

    private void setCacheValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Map sftToValuesMap = lookup(frame);
        TransactionMonitor transactionMonitor = getTransactionStatusMonitor();
        if (sftToValuesMap != null) {
          if (transactionMonitor == null || !transactionMonitor.existsTransaction()) {
            insert(sftToValuesMap, slot, facet, isTemplate, values);
          } else {
            _frameToSftToValuesMap.remove(frame);
          }
        }
    }

    private void insert(Map map, Slot slot, Facet facet, boolean isTemplate, Collection values) {
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

    private void remove(Map map, Slot slot, Facet facet, boolean isTemplate) {
        Sft lookupSft = new Sft(slot, facet, isTemplate);
        map.remove(lookupSft);
    }

    private void addCacheValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        TransactionMonitor transactionMonitor = getTransactionStatusMonitor();
        Map sftToValuesMap = lookup(frame);
        if (sftToValuesMap != null) {
          if (transactionMonitor == null || !transactionMonitor.existsTransaction()) {
            List list = lookup(sftToValuesMap, slot, facet, isTemplate);
            if (list == null) {
                if (!isSpecial(slot, facet, isTemplate)) {
                    insert(sftToValuesMap, slot, facet, isTemplate, values);
                }
            } else {
                list.addAll(values);
            }
          } else {
            _frameToSftToValuesMap.remove(frame);
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
        _frameToSftToValuesMap.remove(frame);
        Iterator<Frame> i = _frameToSftToValuesMap.getKeys().iterator();
        while (i.hasNext()) {
            Frame frameKey =  i.next();
            Map<Sft, List> sftToValuesMap =  _frameToSftToValuesMap.get(frameKey);
            if (sftToValuesMap != null) {
                Iterator<Map.Entry<Sft, List>> j = sftToValuesMap.entrySet().iterator();
                while (j.hasNext()) {
                    Map.Entry<Sft,List> entry = (Map.Entry) j.next();
                    Sft sft = entry.getKey();
                    if (contains(sft, frame)) {
                        _frameToSftToValuesMap.remove(frameKey);
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

    private void loadFramesIntoCache() {
        _frameToSftToValuesMap = null;
        SystemUtilities.gc();
        _frameToSftToValuesMap = _delegate.getFrameValues();
    }
    
    private Map<Sft,List> loadFrameIntoCache(Frame frame) {
        Map<Sft, List> sftToValuesMap = _delegate.getFrameValues(frame);
        _frameToSftToValuesMap.put(frame, sftToValuesMap);
        return sftToValuesMap;
    }

    public boolean beginTransaction(String name) {
      TransactionMonitor monitor = getTransactionStatusMonitor();
      try {
        if (monitor != null && !monitor.inTransaction() &&
            monitor.getTransationIsolationLevel() == TransactionIsolationLevel.SERIALIZABLE) {
          clearCache();
        }
      } catch (TransactionException te) {
        clearCache();
      }
      return getDelegate().beginTransaction(name);
    }

    public boolean commitTransaction() {
        boolean committed = getDelegate().commitTransaction();
        if (!committed) {
            clearCache();
        }
        return committed;
    }

    public boolean rollbackTransaction() {
        boolean rolledback = getDelegate().rollbackTransaction();
        if (rolledback) {
            clearCache();
        }
        return rolledback;
    }
    
    public TransactionMonitor getTransactionStatusMonitor() {
      return getDelegate().getTransactionStatusMonitor();
    }

    public void clearCache() {
        _frameToSftToValuesMap.clear();
    }

    public void replaceFrame(Frame frame) {
        clearCache();
        getDelegate().replaceFrame(frame);
    }
    
    private Set getCachedFrames() {
        return new HashSet(_frameToSftToValuesMap.getKeys());
    }
    
    public Set getFrames() {
        Set frames;
        int count = getFrameCount();
        Set cachedFrames = getCachedFrames();
        TransactionMonitor transactionMonitor = getTransactionStatusMonitor();
        if (cachedFrames.size() == count) {
            frames = cachedFrames;
        } else if (transactionMonitor == null || !transactionMonitor.existsTransaction()) {
            loadFramesIntoCache();
            cachedFrames = getCachedFrames();
            if (cachedFrames.size() == count) {
                frames = cachedFrames;
            } else {
                if (log.isLoggable(Level.FINE)) {
                  log.fine("Not enough memory to cache all frames");
                }
                frames = getDelegate().getFrames();
            }
        } else {
          frames = getDelegate().getFrames();
        }
        return frames;
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

    public void setIncludedFrames(IncludedFrameLookup iframes) {
      if (_delegate instanceof IncludingKBSupport) {
        ((IncludingKBSupport) _delegate).setIncludedFrames(iframes);
      }
    }

}

