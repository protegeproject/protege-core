package edu.stanford.smi.protege.storage.jdbc.framedb;


import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A simple custom database for storing frames
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameDB {
    private static final int DEFAULT_MAP_SIZE = 32771;
    // indices
    private MultiMap _frameToRecordsMap;
    private MultiMap _slotToRecordsMap;
    private MultiMap _facetToRecordsMap;
    private MultiMap _valueToRecordsMap;
    private Map _fsftToRecordMap;

    private Record _cachedKey = new Record();
    private List _cachedSingleElementList = new ArrayList(1);

    {
        _cachedSingleElementList.add(null);
    }

    public FrameDB() {
        this(DEFAULT_MAP_SIZE);
    }

    public FrameDB(int initSize) {
        _frameToRecordsMap = new SetMultiMap(initSize);
        _slotToRecordsMap = new SetMultiMap(initSize);
        _facetToRecordsMap = new SetMultiMap(initSize);
        _valueToRecordsMap = new ListMultiMap(initSize);
        _fsftToRecordMap = new HashMap(initSize);
    }

    public void addValue(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate, Object value) {
        Record record = getOrCreateRecord(frame, slot, facet, isTemplate);
        record.addValue(value);
        _valueToRecordsMap.addValue(value, record);
    }

    public void addValueAt(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate, Object value, int position) {
        Record record = getExistingRecord(frame, slot, facet, isTemplate);
        record.addValue(position, value);
        _valueToRecordsMap.addValue(value, record);
    }

    public void addValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate, Collection values) {
        Record record = getOrCreateRecord(frame, slot, facet, isTemplate);
        record.addValues(values);
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            _valueToRecordsMap.addValue(value, record);
        }
    }

    public int countValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate) {
        Record record = getExistingRecord(frame, slot, facet, isTemplate);
        return (record == null) ? 0 : record.countValues();
    }

    private Record createRecord(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate) {
        Record record = new Record(frame, slot, facet, isTemplate);
        _frameToRecordsMap.addValue(frame, record);
        _slotToRecordsMap.addValue(slot, record);
        if (facet != null) {
            _facetToRecordsMap.addValue(facet, record);
        }
        _fsftToRecordMap.put(record, record);
        return record;
    }

    private Record getExistingRecord(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate) {
        _cachedKey.load(frame, slot, facet, isTemplate);
        return (Record) _fsftToRecordMap.get(_cachedKey);
    }

    public Collection getFrames() {
        return _frameToRecordsMap.getKeys();
    }

    private Record getOrCreateRecord(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate) {
        Record record = getExistingRecord(frame, slot, facet, isTemplate);
        if (record == null) {
            record = createRecord(frame, slot, facet, isTemplate);
        }
        return record;
    }

    public Collection getRecords() {
        return _fsftToRecordMap.keySet();
    }

    public List getRecords(Object value) {
        return (List) _valueToRecordsMap.getValues(value);
    }

    public Collection getSlotRecords(FrameID slot) {
        return _slotToRecordsMap.getValues(slot);
    }

    public List getValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate) {
        Record record = getExistingRecord(frame, slot, facet, isTemplate);
        return (record == null) ? (List) null : record.getValues();
    }

    public boolean hasValue(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate, Object value) {
        boolean result;
        Record record = getExistingRecord(frame, slot, facet, isTemplate);
        if (record == null) {
            result = false;
        } else {
            result = record.getValues().contains(value);
        }
        return result;
    }

    private boolean isUserRecord(Record record) {
        return record.getFrame().isUser() || record.getSlot().isUser() ||
            (record.getFacet() != null && record.getFacet().isUser());
    }

    public void removeFacet(FrameID frame) {
        Collection records = _facetToRecordsMap.removeKey(frame);
        if (records != null) {
            removeRecords(records);
        }
    }

    public void removeFrame(FrameID frame) {

        Collection c = _frameToRecordsMap.removeKey(frame);
        if (c == null) {
            Log.getLogger().warning("no records: " + frame);
        } else {
            removeRecords(c);
        }
        Assert.assertFalse("removed frame", getFrames().contains(frame));
    }

    public void removeRecord(Record record) {
        _frameToRecordsMap.removeValue(record.getFrame(), record);
        _slotToRecordsMap.removeValue(record.getSlot(), record);
        FrameID facet = record.getFacet();
        if (facet != null) {
            _facetToRecordsMap.removeValue(facet, record);
        }
        _fsftToRecordMap.remove(record);
        Iterator i = record.getValues().iterator();
        while (i.hasNext()) {
            Object value = i.next();
            _valueToRecordsMap.removeValue(value, record);
        }
    }

    private void removeRecords(Collection records) {
        Iterator i = records.iterator();
        while (i.hasNext()) {
            Record record = (Record) i.next();
            removeRecord(record);
        }
    }

    public void removeSingleValue(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate, Object value) {
        Record record = getExistingRecord(frame, slot, facet, isTemplate);
        if (record != null) {
            record.removeSingleValue(value);
            _valueToRecordsMap.removeValue(value, record);
        }
    }

    public void removeSlot(FrameID frame) {
        Collection records = _slotToRecordsMap.removeKey(frame);
        if (records != null) {
            removeRecords(records);
        }
    }

    public void removeUserRecords() {
        removeUserRecords(_frameToRecordsMap);
        removeUserRecords(_slotToRecordsMap);
        removeUserRecords(_facetToRecordsMap);
        removeUserRecords(_valueToRecordsMap);
        removeUserRecords(_fsftToRecordMap);
    }

    private void removeUserRecords(MultiMap xToRecordsMap) {
        Iterator i = xToRecordsMap.getKeys().iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof FrameID && ((FrameID) o).isUser()) {
                i.remove();
            } else {
                Collection records = xToRecordsMap.getValues(o);
                Iterator j = records.iterator();
                while (j.hasNext()) {
                    Record record = (Record) j.next();
                    if (isUserRecord(record)) {
                        j.remove();
                    } else {
                        removeUserValues(record.getValues());
                    }
                }
            }
        }
    }

    private void removeUserRecords(Map xToRecordMap) {
        Iterator i = xToRecordMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            Record key = (Record) entry.getKey();
            if (isUserRecord(key)) {
                i.remove();
            }
        }
    }

    private void removeUserValues(Collection values) {
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof FrameID) {
                FrameID id = (FrameID) o;
                if (id.isUser()) {
                    i.remove();
                }
            }
        }
    }

    public void removeValue(Object o) {
        Collection records = _valueToRecordsMap.removeKey(o);
        if (records != null) {
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                record.removeSingleValue(o);
            }
        }
    }

    public Object removeValueAt(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate, int position) {
        Record record = getExistingRecord(frame, slot, facet, isTemplate);
        Object value = record.getValues().remove(position);
        _valueToRecordsMap.removeValue(value, record);
        return value;
    }

    public void removeValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate) {
        Record record = getExistingRecord(frame, slot, facet, isTemplate);
        Iterator i = record.removeValues().iterator();
        while (i.hasNext()) {
            Object value = i.next();
            _valueToRecordsMap.removeValue(value, record);
        }
    }

    public void replace(FrameID from, FrameID to) {
        // frames
        Collection frameRecords = _frameToRecordsMap.removeKey(from);
        if (frameRecords != null) {
            Iterator i = new ArrayList(frameRecords).iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                removeRecord(record);
                addValues(to, record.getSlot(), record.getFacet(), record.getIsTemplate(), record.getValues());
            }
        }
        // should also do slots and facets

        // values
        Collection valueRecords = _valueToRecordsMap.removeKey(from);
        if (valueRecords != null) {
            Iterator i = valueRecords.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                replaceValue(record, from, to);
                _valueToRecordsMap.addValue(to, record);
            }
        }
    }

    private static void replaceValue(Record record, FrameID from, FrameID to) {
        List values = record.getValues();
        if (values != null) {
            ListIterator i = values.listIterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (equals(o, from)) {
                    i.set(to);
                }
            }
        }
    }
    
    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public void setValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate, Object newValue) {
        Collection values;
        if (newValue == null) {
            values = CollectionUtilities.EMPTY_ARRAY_LIST;
        } else {
            _cachedSingleElementList.set(0, newValue);
            values = _cachedSingleElementList;
        }
        setValues(frame, slot, facet, isTemplate, values);
    }

    public void setValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate, Collection newValues) {
        Record record = getOrCreateRecord(frame, slot, facet, isTemplate);
        List oldValues = record.setValues(newValues);

        // avoid the iterator creation, if possible, for this common operation
        if (oldValues instanceof ArrayList) {
            ArrayList oldValuesArray = (ArrayList) oldValues;
            for (int i = 0; i < oldValuesArray.size(); ++i) {
                Object value = oldValuesArray.get(i);
                _valueToRecordsMap.removeValue(value, record);
            }
        } else {
            Iterator i = oldValues.iterator();
            while (i.hasNext()) {
                Object value = i.next();
                _valueToRecordsMap.removeValue(value, record);
            }
        }

        // avoid the iterator creation, if possible, for this common operation
        if (newValues instanceof ArrayList) {
            ArrayList newValueList = (ArrayList) newValues;
            for (int i = 0; i < newValueList.size(); ++i) {
                Object value = newValueList.get(i);
                _valueToRecordsMap.addValue(value, record);
            }
        } else {
            Iterator i = newValues.iterator();
            while (i.hasNext()) {
                Object value = i.next();
                _valueToRecordsMap.addValue(value, record);
            }
        }
    }

    public void updateValue(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate,  Object oldValue, Object newValue) {
        Record record = getExistingRecord(frame, slot, facet, isTemplate);
        ListIterator i = record.getValues().listIterator();
        while (i.hasNext()) {
            Object currentValue = i.next();
            if (currentValue.equals(oldValue)) {
                i.set(newValue);
                _valueToRecordsMap.removeValue(oldValue, record);
                _valueToRecordsMap.addValue(newValue, record);
            }
        }
    }

    public void updateValueAt(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate, int position, Object newValue) {
        Record record = getExistingRecord(frame, slot, facet, isTemplate);
        List list = record.getValues();
        Object oldValue = list.set(position, newValue);
        _valueToRecordsMap.removeValue(oldValue, record);
        _valueToRecordsMap.addValue(newValue, record);
    }

    /**
     * old index is the index of the item to move before it is moved
     * new index is the index that the item will have after it is moved
     */
    public void updateValuePosition(
        FrameID frame,
        FrameID slot,
        FrameID facet,
        boolean isTemplate,
        int oldIndex,
        int newIndex) {
        Assert.assertTrue("oldIndex", oldIndex >= 0);
        Assert.assertTrue("newIndex", newIndex >= 0);
        List values = getExistingRecord(frame, slot, facet, isTemplate).getValues();
        Object value = values.remove(oldIndex);
        values.add(newIndex, value);
    }
}
