package edu.stanford.smi.protege.storage.jdbc.framedb;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

/**
 * Implementation of the Storage interface for in memory use.  Delegates the real storage operations to FrameDB.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameDBStorage implements Storage {
    private static final int INIT_SIZE = 32771;

    private FrameDB _database = new FrameDB(INIT_SIZE);
    private Map _frameIDToReferenceMap = new HashMap(INIT_SIZE);
    private Map _referenceToFrameIDMap = new HashMap(INIT_SIZE);
    private java.lang.ref.ReferenceQueue _queue;
    private boolean _usingWeakReferences = false;

    /**
     * Add an existing frame to the frame container
     */
    public void addFrame(Frame frame) {
        Object reference;
        if (_queue == null) {
            reference = frame;
        } else {
            checkQueue();
            if (_usingWeakReferences) {
                reference = new java.lang.ref.WeakReference(frame, _queue);
            } else {
                reference = new java.lang.ref.SoftReference(frame, _queue);
            }
        }
        FrameID id = id(frame);
        Object o = _frameIDToReferenceMap.put(id, reference);
        if (o != null) {
            if (o instanceof java.lang.ref.Reference) {
                o = ((java.lang.ref.Reference) o).get();
            }
            if (o != null) {
                if (o == frame) {
                    Log.stack("duplicate insertion: " + id, this, "addFrame", frame);
                } else {
                    Log.stack("duplicate frame id: " + o + ", id: " + id, this, "addFrame", frame);
                }
            }
        }
        _referenceToFrameIDMap.put(reference, id);
    }

    public void addValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        _database.addValue(id(frame), id(slot), id(facet), isTemplate, toDBValue(value));
    }

    public void addValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value, int index) {
        _database.addValueAt(id(frame), id(slot), id(facet), isTemplate, toDBValue(value), index);
    }

    public void addValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate, Collection values) {
        _database.addValues(frame, slot, facet, isTemplate, values);
    }

    public boolean beginTransaction() {
        return true;
    }

    private void checkQueue() {
        if (_queue != null) {
            java.lang.ref.Reference ref;
            while ((ref = _queue.poll()) != null) {
                FrameID id = (FrameID) _referenceToFrameIDMap.get(ref);
                // Log.trace("removing garbage: " + id, this, "checkQueue");
                _database.removeFrame(id);
                _referenceToFrameIDMap.remove(ref);
                _frameIDToReferenceMap.remove(id);
            }
        }
    }

    public boolean containsFrame(Frame frame) {
        return _database.getFrames().contains(id(frame));
    }

    public boolean containsFrame(String name) {
        return getFrameID(name) != null;
    }

    private Reference createReference(Record record) {
        FrameID frameID = record.getFrame();
        FrameID slotID = record.getSlot();
        FrameID facetID = record.getFacet();
        boolean isTemplate = record.getIsTemplate();

        return new ReferenceImpl(getFrame(frameID), (Slot) getFrame(slotID), (Facet) getFrame(facetID), isTemplate);
    }

    public void dispose() {
        _database = null;
    }

    public boolean endTransaction(boolean doCommit) {
        return doCommit;
    }

    private boolean equals(FrameID id, Frame frame) {
        return (id == null) ? frame == null : id.equals(frame.getFrameID());
    }

    public void flush() {
        _database.removeUserRecords();
        removeUserFramesFromMaps();
    }

    private Object fromDBValue(Object o) {
        return (o instanceof FrameID) ? getFrame((FrameID) o) : o;
    }

    private Collection fromFrameIDs(Collection frameIDs) {
        Collection result;
        if (frameIDs.isEmpty()) {
            result = Collections.EMPTY_LIST;
        } else {
            result = new ArrayList(frameIDs.size());
            Iterator i = new ArrayList(frameIDs).iterator();
            while (i.hasNext()) {
                FrameID id = (FrameID) i.next();
                Frame frame = getFrame(id);
                if (frame == null) {
                    // Log.stack("Unable to get frame for id: " + id, this, "fromFrameIDs");
                    result.add(id);
                } else {
                    result.add(frame);
                }
            }
        }

        return result;
    }

    private Frame getFrame(Record record) {
        return getFrame(record.getFrame());
    }

    public Frame getFrame(FrameID id) {
        checkQueue();
        Object reference = _frameIDToReferenceMap.get(id);
        if (reference instanceof java.lang.ref.Reference) {
            reference = ((java.lang.ref.Reference) reference).get();
        }
        return (Frame) reference;
    }

    public Frame getFrame(String name) {
        FrameID id = getFrameID(name);
        return (id == null) ? (Frame) null : getFrame(id);
    }

    public int getFrameCount() {
        return _database.getFrames().size();
    }

    private int getFrameCount(Class type) {
        int count = 0;
        Iterator i = getFrames().iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (type.isInstance(o)) {
                ++count;
            }
        }
        return count;
    }

    public int getFacetCount() {
        return getFrameCount(Facet.class);
    }

    public int getSlotCount() {
        return getFrameCount(Slot.class);
    }

    public int getClsCount() {
        return getFrameCount(Cls.class);
    }

    private FrameID getFrameID(String name) {
        FrameID result = null;
        List records = _database.getRecords(name);
        int nRecords = (records == null) ? 0 : records.size();
        // This method is called a lot so we optimize for the common case
        if (nRecords == 0) {
            // do nothing
        } else if (nRecords == 1) {
            Record record = (Record) records.get(0);
            if (equals(record.getSlot(), Model.SlotID.NAME)
                && record.getFacet() == null
                && record.getIsTemplate() == false) {
                result = record.getFrame();
            }
        } else {
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                if (equals(record.getSlot(), Model.SlotID.NAME)
                    && record.getFacet() == null
                    && record.getIsTemplate() == false) {
                    result = record.getFrame();
                }
            }
        }
        return result;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public Collection getFrames() {
        checkQueue();
        return fromFrameIDs(_database.getFrames());
    }

    public List getLoadedValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate) {
        return _database.getValues(frame, slot, facet, isTemplate);
    }

    public Collection getFramesWithValue(Slot slot, Facet facet, boolean isTemplate, Object value) {
        Iterator i = getRecords(slot, facet, isTemplate, value, 0).iterator();
        Collection frames = new HashSet();
        while (i.hasNext()) {
            Record record = (Record) i.next();
            frames.add(getFrame(record.getFrame()));
        }
        return frames;
    }

    public Collection getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String s, int maxMatches) {
        Collection matchingFrames = new ArrayList();
        StringMatcher matcher = new SimpleStringMatcher(s);
        FrameID facetID = id(facet);
        Collection records = _database.getSlotRecords(id(slot));
        Iterator i = records.iterator();
        while (i.hasNext()) {
            Record record = (Record) i.next();
            if (equals(record.getFacet(), facetID) && record.getIsTemplate() == isTemplate) {
                if (hasMatch(record.getValues(), matcher)) {
                    matchingFrames.add(getFrame(record));
                }
            }
        }
        return matchingFrames;
    }

    public Collection getRecords() {
        return _database.getRecords();
    }

    public Collection getRecords(Slot slot, Facet facet, boolean isTemplate, Object o, int maxReferences) {
        ArrayList result = new ArrayList();
        Collection records = _database.getRecords(toDBValue(o));
        if (records != null) {
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                if (equals(record.getSlot(), slot)
                    && equals(record.getFacet(), facet)
                    && record.getIsTemplate() == isTemplate) {
                    result.add(record);
                }
            }
        }
        return result;
    }

    public Collection getReferences(Object o, int maxReferences) {
        Collection references;
        if (o instanceof String && ((String) o).indexOf("*") >= 0) {
            references = getMatchingDbReferences((String) o, maxReferences);
        } else {
            references = getDbReferences(o, maxReferences);
        }
        return references;
    }

    private Collection getMatchingDbReferences(String s, int maxReferences) {
        SimpleStringMatcher matcher = new SimpleStringMatcher(s);
        Collection matchingRecords = new ArrayList();
        Collection records = _database.getRecords();
        Iterator i = records.iterator();
        while (i.hasNext()) {
            Record record = (Record) i.next();
            if (hasMatch(record.getValues(), matcher)) {
                matchingRecords.add(record);
            }
        }
        return recordsToReferences(matchingRecords);
    }

    private Collection getDbReferences(Object o, int maxReferences) {
        Collection records = _database.getRecords(toDBValue(o));
        return recordsToReferences(records);
    }

    private Collection recordsToReferences(Collection records) {
        Collection result;
        if (records == null) {
            result = Collections.EMPTY_LIST;
        } else {
            result = new ArrayList(records.size());
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                result.add(createReference(record));
            }
        }
        return result;
    }

    public Object getValue(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Object result;
        List values = _database.getValues(id(frame), id(slot), id(facet), isTemplate);
        if (values == null || values.isEmpty()) {
            result = null;
        } else {
            result = fromDBValue(values.get(0));
        }
        return result;
    }

    public int getValueCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return _database.countValues(id(frame), id(slot), id(facet), isTemplate);
    }

    public ArrayList getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        List values = _database.getValues(id(frame), id(slot), id(facet), isTemplate);
        ArrayList result;
        if (values == null || values.isEmpty()) {
            result = CollectionUtilities.EMPTY_ARRAY_LIST;
        } else {
            result = new ArrayList(values.size());
            Iterator i = values.iterator();
            while (i.hasNext()) {
                Object dbValue = i.next();
                Object value = fromDBValue(dbValue);
                if (value == null) {
                    // Log.warning("null value", this, "getValues", frame, slot, facet, dbValue);
                    result.add(dbValue);
                } else {
                    result.add(value);
                }
            }
        }
        return result;
    }

    private boolean hasMatch(List values, StringMatcher matcher) {
        boolean result = false;
        Iterator i = values.iterator();
        while (i.hasNext() && !result) {
            Object value = i.next();
            if (value instanceof String) {
                String valueString = value.toString();
                result = matcher.isMatch(valueString);
            }
        }
        return result;
    }

    public boolean hasValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Object dbValue = toDBValue(value);
        return _database.hasValue(id(frame), id(slot), id(facet), isTemplate, dbValue);
    }

    public boolean hasValueAtSomeFrame(Slot slot, Facet facet, boolean isTemplate) {
        FrameID facetID = id(facet);
        boolean result = false;
        Collection records = _database.getSlotRecords(slot.getFrameID());
        if (records != null) {
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                if (equals(record.getFacet(), facetID)
                    && record.getIsTemplate() == isTemplate
                    && !record.getValues().isEmpty()) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private static FrameID id(Frame frame) {
        return (frame == null) ? (FrameID) null : frame.getFrameID();
    }

    public boolean isCaching() {
        return _queue != null;
    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        _database.updateValuePosition(id(frame), id(slot), id(facet), isTemplate, from, to);
    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value, int toIndex) {
        Object dbValue = toDBValue(value);
        FrameID frameID = id(frame);
        FrameID slotID = id(slot);
        FrameID facetID = id(facet);
        int fromIndex = _database.getValues(frameID, slotID, facetID, isTemplate).indexOf(dbValue);
        _database.updateValuePosition(frameID, slotID, facetID, isTemplate, fromIndex, toIndex);
    }

    public void remove(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        _database.removeValues(id(frame), id(slot), id(facet), isTemplate);
    }

    /**
     * Removes all references to a frame from storage.  This includes both places where this frame references
     * other frames and where other places reference this frame.
     */
    public void removeFrame(Frame frame) {
        FrameID id = id(frame);
        _database.removeFrame(id);
        if (frame instanceof Slot) {
            _database.removeSlot(id);
        } else if (frame instanceof Facet) {
            _database.removeFacet(id);
        }
        _database.removeValue(id);
        removeFrameFromIDMap(frame);
    }

    private void removeFrameFromIDMap(Frame frame) {
        FrameID id = id(frame);
        Object reference = _frameIDToReferenceMap.remove(id);
        Assert.assertNotNull("reference", reference);
        Object removedID = _referenceToFrameIDMap.remove(reference);
        Assert.assertSame("ids", id, removedID);
    }

    public void removeSingleValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        _database.removeSingleValue(id(frame), id(slot), id(facet), isTemplate, toDBValue(value));
    }

    private void removeUserFramesFromMaps() {
        Iterator i = _frameIDToReferenceMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            FrameID id = (FrameID) entry.getKey();
            if (id.isUser()) {
                Object ref = entry.getValue();
                _referenceToFrameIDMap.remove(ref);
                i.remove();
            }
        }
    }

    public Object removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int index) {
        Object o = _database.removeValueAt(id(frame), id(slot), id(facet), isTemplate, index);
        return fromDBValue(o);
    }

    public void removeValues(Slot slot, Facet facet, boolean isTemplate, Cls cls) {
        FrameID facetID = id(facet);
        Collection records = _database.getSlotRecords(slot.getFrameID());
        if (records != null) {
            Iterator i = new ArrayList(records).iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                if (equals(record.getFacet(), facetID) && record.getIsTemplate() == isTemplate) {
                    if (cls == null) {
                        _database.removeRecord(record);
                    } else {
                        FrameID recordFrameID = record.getFrame();
                        Instance instance = (Instance) getFrame(recordFrameID);
                        if (instance.hasType(cls)) {
                            _database.removeRecord(record);
                        }
                    }
                }
            }
        }
    }

    /**
     * replaceValue method comment.
     */
    public void replace(Frame from, Frame to) {
        removeFrameFromIDMap(from);
        addFrame(to);
        _database.replace(id(from), id(to));
    }

    /**
     * replaceValue method comment.
     */
    public void replaceValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int index, Object value) {
        _database.updateValueAt(id(frame), id(slot), id(facet), isTemplate, index, toDBValue(value));
    }

    /**
     * replaceValue method comment.
     */
    public void replaceValue(
        Frame frame,
        Slot slot,
        Facet facet,
        boolean isTemplate,
        Object fromValue,
        Object toValue) {
        _database.updateValue(id(frame), id(slot), id(facet), isTemplate, toDBValue(fromValue), toDBValue(toValue));
    }

    public void setCaching(boolean caching) {
        if (caching) {
            if (_queue == null) {
                _queue = new java.lang.ref.ReferenceQueue();
            }
        } else {
            _queue = null;
        }
    }

    /**
     * Used for testing only.  Do not call.
     */
    public void setUseWeakReference(boolean b) {
        Log.enter(this, "setUseWeakReference", new Boolean(b));
        _usingWeakReferences = b;
    }

    /**
     * setValue method comment.
     */
    public void setValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        _database.setValues(id(frame), id(slot), id(facet), isTemplate, toDBValue(value));
    }

    /**
     * setValues method comment.
     */
    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        _database.setValues(id(frame), id(slot), id(facet), isTemplate, toDBValues(values));
    }

    /**
     * setValues method comment.
     */
    public void setValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate, Collection values) {
        _database.setValues(frame, slot, facet, isTemplate, values);
    }

    public boolean supportsTransactions() {
        return false;
    }

    private static Object toDBValue(Object o) {
        Object result;
        if (o instanceof Frame) {
            Frame frame = (Frame) o;
            result = frame.getFrameID();
        } else {
            result = o;
        }
        return result;
    }

    private static ArrayList toDBValues(Collection values) {
        ArrayList result;
        if (values.isEmpty()) {
            result = CollectionUtilities.EMPTY_ARRAY_LIST;
        } else {
            result = new ArrayList(values.size());
            Iterator i = values.iterator();
            while (i.hasNext()) {
                Object value = i.next();
                result.add(toDBValue(value));
            }
        }
        return result;
    }

    public String toString() {
        return "FrameDBStorage";
    }
}
