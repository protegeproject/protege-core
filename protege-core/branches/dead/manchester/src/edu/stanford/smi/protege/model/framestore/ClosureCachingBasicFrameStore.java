package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.query.*;
import edu.stanford.smi.protege.util.*;

/**
 * @author Ray Fergerson
 * 
 * Description of this class
 */
public class ClosureCachingBasicFrameStore implements NarrowFrameStore {
    private NarrowFrameStore _delegate;

    private final Sft _lookupSft = new Sft();
    private CacheMap _sftToFrameToClosureMap = new CacheMap();

    public ClosureCachingBasicFrameStore(NarrowFrameStore delegate) {
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

    public FrameID generateFrameID() {
        return _delegate.generateFrameID();
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
        _delegate.deleteFrame(frame);
        deleteFrameFromCache(frame);
    }

    public Set executeQuery(Query query) {
        return _delegate.executeQuery(query);
    }

    public void close() {
        _delegate.close();
        _delegate = null;
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Set closure = lookup(frame, slot, facet, isTemplate);
        if (closure == null) {
            closure = ClosureUtils.calculateClosure(this, frame, slot, facet, isTemplate);
            insert(frame, slot, facet, isTemplate, closure);
        } else {
            // Log.trace("closure cache hit", this, "getClosure", frame, slot,
            // facet);
        }
        return closure;
    }

    private Map lookup(Slot slot, Facet facet, boolean isTemplate) {
        _lookupSft.set(slot, facet, isTemplate);
        return (Map) _sftToFrameToClosureMap.get(_lookupSft);
    }

    private Set lookup(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Set closure = null;
        Map frameToClosureMap = lookup(slot, facet, isTemplate);
        if (frameToClosureMap != null) {
            closure = (Set) frameToClosureMap.get(frame);
        }
        return closure;
    }

    private void insert(Frame frame, Slot slot, Facet facet, boolean isTemplate, Set closure) {
        Map frameToClosureMap = lookup(slot, facet, isTemplate);
        if (frameToClosureMap == null) {
            frameToClosureMap = new HashMap();
            _sftToFrameToClosureMap.put(new Sft(slot, facet, isTemplate), frameToClosureMap);
        }
        frameToClosureMap.put(frame, closure);
    }

    private void updateClosureCache(Slot slot, Facet facet, boolean isTemplate) {
        _lookupSft.set(slot, facet, isTemplate);
        _sftToFrameToClosureMap.remove(_lookupSft);
    }

    private void deleteFrameFromCache(Frame frame) {
        removeFrameFromSft(frame);
        Iterator i = _sftToFrameToClosureMap.getKeys().iterator();
        while (i.hasNext()) {
            Sft sft = (Sft) i.next();
            Map frameToClosureMap = (Map) _sftToFrameToClosureMap.get(sft);
            if (frameToClosureMap != null) {
                frameToClosureMap.remove(frame);
                removeFrameAsValueFromMap(frame, frameToClosureMap);
            }
        }
    }

    private static void removeFrameAsValueFromMap(Frame frame, Map frameToClosureMap) {
        Iterator i = frameToClosureMap.values().iterator();
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
            Iterator i = _sftToFrameToClosureMap.getKeys().iterator();
            while (i.hasNext()) {
                Sft sft = (Sft) i.next();
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
        return _delegate.beginTransaction(name);
    }

    public boolean commitTransaction() {
        return _delegate.commitTransaction();
    }

    public boolean rollbackTransaction() {
        return _delegate.rollbackTransaction();
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

}