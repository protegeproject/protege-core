package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.HashMap;
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
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.CacheMap;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */
public class ClosureCachingBasicFrameStore implements NarrowFrameStore {
    private static Logger log = Log.getLogger(ClosureCachingBasicFrameStore.class);

    private NarrowFrameStore _delegate;

    private final CacheMap<Sft, Map<Frame,Set>> _sftToFrameToClosureMap
      = new CacheMap<Sft, Map<Frame, Set>>();

    public ClosureCachingBasicFrameStore(NarrowFrameStore delegate) {
    	if (log.isLoggable(Level.FINEST)) {
    		log.fine("Constructing NarrowFrameStore " + this + " with delegate " + delegate);
    	}
        _delegate = delegate;
    }

    public String getName() {
        return StringUtilities.getClassName(this);
    }

    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    public NarrowFrameStore getDelegate() {
        return _delegate;
    }

    public Frame getFrame(FrameID id) {
        return _delegate.getFrame(id);
    }


    public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return _delegate.getValues(frame, slot, facet, isTemplate);
    }

    public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return _delegate.getValuesCount(frame, slot, facet, isTemplate);
    }

    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        _delegate.setValues(frame, slot, facet, isTemplate, values);
        updateClosureCache(slot, facet, isTemplate);
    }

    public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        _delegate.addValues(frame, slot, facet, isTemplate, values);
        updateClosureCache(slot, facet, isTemplate);
    }

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        _delegate.removeValue(frame, slot, facet, isTemplate, value);
        updateClosureCache(slot, facet, isTemplate);
    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        _delegate.moveValue(frame, slot, facet, isTemplate, from, to);
    }

    public Set getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        return _delegate.getFrames(slot, facet, isTemplate, value);
    }

    public Set getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
        return _delegate.getFramesWithAnyValue(slot, facet, isTemplate);
    }

    public int getFrameCount() {
        return _delegate.getFrameCount();
    }

    public int getClsCount() {
        return _delegate.getClsCount();
    }

    public int getSlotCount() {
        return _delegate.getSlotCount();
    }

    public int getFacetCount() {
        return _delegate.getFacetCount();
    }

    public int getSimpleInstanceCount() {
        return _delegate.getSimpleInstanceCount();
    }

    public Set getFrames() {
        return _delegate.getFrames();
    }

    public Set<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) {
        return _delegate.getMatchingFrames(slot, facet, isTemplate, value, maxMatches);
    }

    public Set<Reference> getReferences(Object value) {
        return _delegate.getReferences(value);
    }

    public Set<Reference> getMatchingReferences(String value, int maxMatches) {
        return _delegate.getMatchingReferences(value, maxMatches);
    }

    public void deleteFrame(Frame frame) {
        _delegate.deleteFrame(frame);
        deleteFrameFromCache(frame);
    }

    public void executeQuery(Query query, QueryCallback callback) {
        _delegate.executeQuery(query, callback);
    }

    public void close() {
        _delegate.close();
        _delegate = null;
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Set closure = lookup(frame, slot, facet, isTemplate);
        if (closure == null) {
            closure = ClosureUtils.calculateClosure(this, frame, slot, facet, isTemplate);
            TransactionMonitor transactionMonitor = getTransactionStatusMonitor();
            if (transactionMonitor == null || !transactionMonitor.existsTransaction()) {
              insert(frame, slot, facet, isTemplate, closure);
            }
        } else {
          if (log.isLoggable(Level.FINER)) {
            log.finer("closure cache hit for frame = " + frame + " slot = " + slot +
                      "facet = " + facet + " isTemplate = " + isTemplate);
          }
        }
        return closure;
    }

    private Map<Frame,Set> lookup(Slot slot, Facet facet, boolean isTemplate) {
        Sft lookupSft = new Sft(slot, facet, isTemplate);
        return _sftToFrameToClosureMap.get(lookupSft);
    }

    private Set lookup(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Set closure = null;
        Map<Frame,Set> frameToClosureMap = lookup(slot, facet, isTemplate);
        if (frameToClosureMap != null) {
            closure = frameToClosureMap.get(frame);
        }
        return closure;
    }

    private void insert(Frame frame, Slot slot, Facet facet, boolean isTemplate, Set closure) {
        Map<Frame, Set> frameToClosureMap = lookup(slot, facet, isTemplate);
        if (frameToClosureMap == null) {
            frameToClosureMap = new HashMap();
            _sftToFrameToClosureMap.put(new Sft(slot, facet, isTemplate), frameToClosureMap);
        }
        frameToClosureMap.put(frame, closure);
    }

    private void updateClosureCache(Slot slot, Facet facet, boolean isTemplate) {
        Sft lookupSft = new Sft(slot, facet, isTemplate);
        _sftToFrameToClosureMap.remove(lookupSft);
    }

    private void deleteFrameFromCache(Frame frame) {
        removeFrameFromSft(frame);
        Iterator<Sft> i = _sftToFrameToClosureMap.keySet().iterator();
        while (i.hasNext()) {
            Sft sft = i.next();
            Map<Frame, Set> frameToClosureMap =  _sftToFrameToClosureMap.get(sft);
            if (frameToClosureMap != null) {
                frameToClosureMap.remove(frame);
                removeFrameAsValueFromMap(frame, frameToClosureMap);
            }
        }
    }

    private static void removeFrameAsValueFromMap(Frame frame, Map frameToClosureMap) {
        Iterator<Sft> i = frameToClosureMap.values().iterator();
        while (i.hasNext()) {
            Set closure = (Set) i.next();
            if (closure.contains(frame)) {
                i.remove();
            }
        }
    }

    private void removeFrameFromSft(Frame frame) {
        if (frame instanceof Slot || frame instanceof Facet) {
            boolean isSlot = frame instanceof Slot;
            Iterator<Sft> i = _sftToFrameToClosureMap.keySet().iterator();
            while (i.hasNext()) {
                Sft sft = i.next();
                if (isSlot) {
                    if (equals(frame, sft.getSlot())) {
                        _sftToFrameToClosureMap.remove(sft);
                    }
                } else if (equals(frame, sft.getFacet())) {
                    _sftToFrameToClosureMap.remove(sft);
                }
            }
        }
    }

    public boolean beginTransaction(String name) {
      TransactionMonitor monitor = getTransactionStatusMonitor();
      /*
       * Ensure that if the transaction isolation level is serializable then the database
       * is aware of all reads.
       */
      try {
        if (monitor != null && !monitor.existsTransaction() &&
            monitor.getTransationIsolationLevel() == TransactionIsolationLevel.SERIALIZABLE) {
          clearCache();
        }
      } catch (TransactionException te) {
        clearCache();
      }
      return _delegate.beginTransaction(name);
    }

    public boolean commitTransaction() {
        boolean ret = _delegate.commitTransaction();
        if (!ret) {
          clearCache();
        }
        return ret;
    }

    public boolean rollbackTransaction() {
        return _delegate.rollbackTransaction();
    }

    public TransactionMonitor getTransactionStatusMonitor() {
      return _delegate.getTransactionStatusMonitor();
    }


    private void clearCache() {
        _sftToFrameToClosureMap.clear();
    }

    public void replaceFrame(Frame frame) {
        clearCache();
        _delegate.replaceFrame(frame);
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public void reinitialize() {
    	clearCache();
    	if (getDelegate() != null) {
    		getDelegate().reinitialize();
    	}
    }

    public void replaceFrame(Frame original, Frame replacement) {
      clearCache();
      _delegate.replaceFrame(original, replacement);
    }

    public boolean setCaching(RemoteSession session, boolean doCache) {
        return _delegate.setCaching(session, doCache);
    }

}
