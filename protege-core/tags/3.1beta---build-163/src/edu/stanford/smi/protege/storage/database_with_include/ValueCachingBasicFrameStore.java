package edu.stanford.smi.protege.storage.database_with_include;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.model.query.*;
import edu.stanford.smi.protege.util.*;

/**
 * @author Ray Fergerson
 *
 * Description of this class
 */

/*
class Fsft {
    private Frame frame;
    private Slot slot;
    private Facet facet;
    private boolean isTemplate;
    private int hashCode;

    Fsft(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        set(frame, slot, facet, isTemplate);
    }
    Fsft() {
    }

    void set(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        this.frame = frame;
        this.slot = slot;
        this.facet = facet;
        this.isTemplate = isTemplate;
        hashCode = HashUtils.getHash(frame, slot, facet, isTemplate);
    }

    boolean contains(Frame frame) {
        return frame.equals(this.frame) || frame.equals(this.slot) || frame.equals(this.facet);
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof Fsft) {
            Fsft rhs = (Fsft) o;
            result =
                equals(facet, rhs.facet)
                    && equals(slot, rhs.slot)
                    && equals(facet, rhs.facet)
                    && isTemplate == rhs.isTemplate;
        }
        return result;
    }

    private static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }
}

public class ValueCachingBasicFrameStore implements BasicFrameStore {
    private DatabaseFrameDb _delegate;
    private final Fsft _lookupFsft = new Fsft();
    private CacheMap _fsftToValuesMap = new CacheMap();

    public void close() {
        _delegate.close();
        _delegate = null;
        _fsftToValuesMap = null;
    }

    public ValueCachingBasicFrameStore(DatabaseFrameDb delegate) {
        _delegate = delegate;
    }

    public BasicFrameStore getDelegate() {
        return _delegate;
    }

    public FrameID generateFrameID() {
        return _delegate.generateFrameID();
    }
    public Frame getFrame(FrameID id) {
        return _delegate.getFrame(id);
    }

    private List lookup(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        _lookupFsft.set(frame, slot, facet, isTemplate);
        return (List) _fsftToValuesMap.get(_lookupFsft);
    }

    public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        List values = lookup(frame, slot, facet, isTemplate);
        if (values == null) {
            values = _delegate.getValues(frame, slot, facet, isTemplate);
            setCacheValues(frame, slot, facet, isTemplate, values);
        }
        if (values == null) {
            values = Collections.EMPTY_LIST;
        }
        return values;
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

    public List getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        return _delegate.getFrames(slot, facet, isTemplate, value);
    }

    public List getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
        return _delegate.getFramesWithAnyValue(slot, facet, isTemplate);
    }

    public List getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) {
        return _delegate.getMatchingFrames(slot, facet, isTemplate, value, maxMatches);
    }

    public List getReferences(Object value) {
        return _delegate.getReferences(value);
    }

    public List getMatchingReferences(String value, int maxMatches) {
        return _delegate.getMatchingReferences(value, maxMatches);
    }

    public void deleteFrame(Frame frame) {
        deleteCacheFrame(frame);
        _delegate.deleteFrame(frame);
    }

    public List executeQuery(Query query) {
        return _delegate.executeQuery(query);
    }

    private void setCacheValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Fsft fsft = new Fsft(frame, slot, facet, isTemplate);
        _fsftToValuesMap.put(fsft, new ArrayList(values));
    }

    private void addCacheValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        List currentValues = lookup(frame, slot, facet, isTemplate);
        if (currentValues != null) {
            currentValues.addAll(values);
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
        Iterator i = _fsftToValuesMap.getKeys().iterator();
        while (i.hasNext()) {
            Fsft fsft = (Fsft) i.next();
            if (fsft.contains(frame)) {
                _fsftToValuesMap.remove(fsft);
            } else {
                List values = (List) _fsftToValuesMap.get(fsft);
                values.remove(frame);
            }
        }
    }

    public boolean beginTransaction() {
        return getDelegate().beginTransaction();
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

    public void clearCache() {
        _fsftToValuesMap.clear();
    }

    public void replaceFrame(Frame frame) {
        clearCache();
        getDelegate().replaceFrame(frame);
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
}
*/
// /*
public class ValueCachingBasicFrameStore implements NarrowFrameStore {
    private DatabaseFrameDb _delegate;
    private final Sft _lookupSft = new Sft();
    private CacheMap _frameToSftToValuesMap = new CacheMap();

    public void close() {
        _delegate.close();
        _delegate = null;
        _frameToSftToValuesMap = null;
    }

    public ValueCachingBasicFrameStore(DatabaseFrameDb delegate) {
        _delegate = delegate;
    }

    public NarrowFrameStore getDelegate() {
        return _delegate;
    }

    public FrameID generateFrameID() {
        return _delegate.generateFrameID();
    }
    public Frame getFrame(FrameID id) {
        return _delegate.getFrame(id);
    }

    private Map lookup(Frame frame) {
        return (Map) _frameToSftToValuesMap.get(frame);
    }

    private List lookup(Map map, Slot slot, Facet facet, boolean isTemplate) {
        _lookupSft.set(slot, facet, isTemplate);
        return (List) map.get(_lookupSft);
    }

    private List lookup(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        List values = null;
        Map sftToValuesMap = lookup(frame);
        if (sftToValuesMap != null) {
            values = lookup(sftToValuesMap, slot, facet, isTemplate);
        }
        return values;
    }

    public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
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

    private boolean isSpecial(Slot slot, Facet facet, boolean isTemplate) {
        return facet == null
            && !isTemplate
            && (equals(slot, Model.SlotID.DIRECT_INSTANCES) || equals(slot, Model.SlotID.DIRECT_SUBCLASSES));
    }

    private boolean equals(Slot slot, FrameID id) {
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
        Map sftToValuesMap = lookup(frame);
        if (sftToValuesMap == null) {
            count = getDelegate().getValuesCount(frame, slot, facet, isTemplate);
            if (count < LOAD_THRESHOLD) {
                sftToValuesMap = loadFrameIntoCache(frame);
            }
        } else {
            List values = lookup(sftToValuesMap, slot, facet, isTemplate);
            count = (values == null) ? 0 : values.size();
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
        if (sftToValuesMap != null) {
            insert(sftToValuesMap, slot, facet, isTemplate, values);
        }
    }

    private void insert(Map map, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        if (values == null || values.size() == 0) {
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
        _lookupSft.set(slot, facet, isTemplate);
        map.remove(_lookupSft);
    }

    private void addCacheValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
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
        _frameToSftToValuesMap.remove(frame);
        Iterator i = _frameToSftToValuesMap.getKeys().iterator();
        while (i.hasNext()) {
            Frame frameKey = (Frame) i.next();
            Map sftToValuesMap = (Map) _frameToSftToValuesMap.get(frameKey);
            if (sftToValuesMap != null) {
                Iterator j = sftToValuesMap.entrySet().iterator();
                while (j.hasNext()) {
                    Map.Entry entry = (Map.Entry) j.next();
                    Sft sft = (Sft) entry.getKey();
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

    private Map loadFrameIntoCache(Frame frame) {
        Map sftToValuesMap = _delegate.getFrameValues(frame);
        _frameToSftToValuesMap.put(frame, sftToValuesMap);
        return sftToValuesMap;
    }

    public boolean beginTransaction(String name) {
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

    public void clearCache() {
        _frameToSftToValuesMap.clear();
    }

    public void replaceFrame(Frame frame) {
        clearCache();
        getDelegate().replaceFrame(frame);
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

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return ClosureUtils.calculateClosure(this, frame, slot, facet, isTemplate);
    }
    
    public Set getFrames() {
        Set frames;
        int count = getFrameCount();
        Set cachedFrames = getCachedFrames();
        if (cachedFrames.size() == count) {
            frames = cachedFrames;
        } else {
            loadFramesIntoCache();
            cachedFrames = getCachedFrames();
            if (cachedFrames.size() == count) {
                frames = cachedFrames;
            }
            return getDelegate().getFrames();
        }
        return frames;
    }
    
    private void loadFramesIntoCache() {
        _frameToSftToValuesMap = _delegate.getFrameValues();
    }
    
    private Set getCachedFrames() {
        return new HashSet(_frameToSftToValuesMap.getKeys());
    }
    

}
//*/
