package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.query.*;
import edu.stanford.smi.protege.util.*;

public class InMemoryFrameDb_new implements NarrowFrameStore {
    private static final int INITIAL_MAP_SIZE = 32771;
    private Map referenceToRecordMap = new HashMap(INITIAL_MAP_SIZE);
    private Map frameToRecordsMap = new HashMap(INITIAL_MAP_SIZE);
    private Map slotToRecordsMap = new HashMap(INITIAL_MAP_SIZE);
    private Map facetToRecordsMap = new HashMap(INITIAL_MAP_SIZE);
    private Map valueToRecordsMap = new LinkedHashMap(INITIAL_MAP_SIZE);

    private ReferenceImpl lookupReference = new ReferenceImpl();
    private int counter = FrameID.INITIAL_USER_FRAME_ID;

    private String frameDBName;

    public String getName() {
        return frameDBName;
    }

    public void setName(String name) {
        frameDBName = name;
    }

    public InMemoryFrameDb_new(String name) {
        frameDBName = name;
    }

    public FrameID generateFrameID() {
        return FrameID.createLocal(counter++);
    }

    public List createList() {
        return new ArrayList();
    }

    public Set createSet() {
        return new LinkedHashSet();
    }

    public void close() {
        referenceToRecordMap = null;
        frameToRecordsMap = null;
        slotToRecordsMap = null;
        facetToRecordsMap = null;
        valueToRecordsMap = null;
        lookupReference = null;
    }

    private Record lookup(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        lookupReference.set(frame, slot, facet, isTemplate);
        return (Record) referenceToRecordMap.get(lookupReference);
    }

    private void remove(Record record) {
        removeRecordFromReferenceMap(record);
        removeRecordFromValuesMap(record);
    }

    private void removeRecordFromReferenceMap(Record record) {
        Frame frame = record.getFrame();
        Slot slot = record.getSlot();
        Facet facet = record.getFacet();
        boolean isTemplate = record.isTemplate();
        lookupReference.set(frame, slot, facet, isTemplate);
        referenceToRecordMap.remove(lookupReference);
    }

    private void removeRecordFromValuesMap(Record record) {
        Iterator i = record.getInternalValues().iterator();
        while (i.hasNext()) {
            Object value = i.next();
            Set records = lookupRecords(valueToRecordsMap, value);
            records.remove(record);
        }
    }

    private void addRecord(Map map, Object key, Record record) {
        Set set = (Set) map.get(key);
        if (set == null) {
            set = new HashSet();
            map.put(key, set);
        }
        set.add(record);
    }

    public void removeRecord(Map map, Object key, Record record) {
        Set set = (Set) map.get(key);
        if (set != null) {
            set.remove(record);
        }
    }

    private void createRecord(Frame frame, Slot slot, Facet facet, boolean isTemplate,
            Collection values) {
        Record record = new Record(frame, slot, facet, isTemplate, values);
        referenceToRecordMap.put(new ReferenceImpl(frame, slot, facet, isTemplate), record);
        addRecord(frameToRecordsMap, frame, record);
        addRecord(slotToRecordsMap, slot, record);
        if (facet != null) {
            addRecord(facetToRecordsMap, facet, record);
        }
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            addRecord(valueToRecordsMap, value, record);
        }
    }

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Record record = lookup(frame, slot, facet, isTemplate);
        if (record != null) {
            record.removeValue(value);
            removeRecord(valueToRecordsMap, value, record);
        }

    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        Record record = lookup(frame, slot, facet, isTemplate);
        if (record != null) {
            record.moveValue(from, to);
        }
    }

    private Set lookupRecords(Map map, Object value) {
        return (Set) map.get(value);
    }

    public Set getReferences(Object value) {
        Set records = lookupRecords(valueToRecordsMap, value);
        return recordsToReferences(records);
    }

    public Set getMatchingReferences(String value, int maxMatches) {
        Set records = getMatchingRecords(value, maxMatches);
        return recordsToReferences(records);
    }

    private Set getMatchingRecords(String value, int maxMatches) {
        if (maxMatches < 1) {
            maxMatches = Integer.MAX_VALUE;
        }
        SimpleStringMatcher matcher = new SimpleStringMatcher(value);
        Set matches = new HashSet();
        Iterator i = valueToRecordsMap.entrySet().iterator();
        while (i.hasNext() && matches.size() < maxMatches) {
            Map.Entry entry = (Map.Entry) i.next();
            Object o = entry.getKey();
            if (o instanceof String && matcher.isMatch((String) o)) {
                Set records = (Set) entry.getValue();
                matches.addAll(records);
            }
        }
        return matches;
    }

    private Set recordsToReferences(Set records) {
        Set references;
        if (records == null) {
            references = Collections.EMPTY_SET;
        } else {
            references = new HashSet(records.size());
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                references.add(recordToReference(record));
            }
        }
        return references;
    }

    private Reference recordToReference(Record record) {
        Frame frame = record.getFrame();
        Slot slot = record.getSlot();
        Facet facet = record.getFacet();
        boolean isTemplate = record.isTemplate();
        return new ReferenceImpl(frame, slot, facet, isTemplate);
    }

    /** TODO implement executeQuery */
    public Set executeQuery(Query query) {
        return null;
    }

    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Record record = lookup(frame, slot, facet, isTemplate);
        if (record == null) {
            createRecord(frame, slot, facet, isTemplate, values);
        } else {
            removeRecordValues(record);
            record.setValues(values);
            addRecordValues(record);
        }
    }

    private void removeRecordValues(Record record) {
        Iterator i = record.getInternalValues().iterator();
        while (i.hasNext()) {
            Object value = i.next();
            removeRecord(valueToRecordsMap, value, record);
        }
    }

    private void addRecordValues(Record record) {
        Iterator i = record.getInternalValues().iterator();
        while (i.hasNext()) {
            Object value = i.next();
            addRecord(valueToRecordsMap, value, record);
        }
    }

    public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            addValue(frame, slot, facet, isTemplate, value);
        }
    }

    public void addValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Record record = lookup(frame, slot, facet, isTemplate);
        if (record == null) {
            createRecord(frame, slot, facet, isTemplate, CollectionUtilities.createList(value));
        } else {
            record.addValue(value);
            addRecord(valueToRecordsMap, value, record);
        }
    }

    public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        List values = Collections.EMPTY_LIST;
        Record record = lookup(frame, slot, facet, isTemplate);
        if (record != null) {
            values = record.getValues();
        }
        return values;
    }

    public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        int count = 0;
        Record record = lookup(frame, slot, facet, isTemplate);
        if (record != null) {
            count = record.getValueCount();
        }
        return count;
    }

    public void deleteFrame(Frame frame) {
        removeFrameReference(frameToRecordsMap, frame);
        removeFrameReference(slotToRecordsMap, frame);
        removeFrameReference(facetToRecordsMap, frame);
        removeFrameValue(valueToRecordsMap, frame);
    }

    private void removeFrameReference(Map map, Frame frame) {
        Set records = lookupRecords(map, frame);
        if (records != null) {
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                remove(record);
            }
            map.remove(frame);
        }
    }

    private void removeFrameValue(Map map, Frame frame) {
        Set records = lookupRecords(map, frame);
        if (records != null) {
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                record.removeValue(frame);
            }
            map.remove(frame);
        }
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    private static boolean matches(Record record, Slot slot, Facet facet, boolean isTemplate) {
        boolean matches = equals(slot, record.getSlot()) && equals(facet, record.getFacet())
                && isTemplate == record.isTemplate();
        return matches;
    }

    public Set getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        Set frames = new HashSet();
        Set records = lookupRecords(valueToRecordsMap, value);
        if (records != null) {
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                if (matches(record, slot, facet, isTemplate)) {
                    frames.add(record.getFrame());
                }
            }
        }
        return frames;
    }

    public Set getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
        Set frames = new HashSet();
        Collection records;
        if (facet != null) {
            records = lookupRecords(facetToRecordsMap, facet);
        } else {
            records = lookupRecords(slotToRecordsMap, slot);
        }
        if (records != null) {
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                if (matches(record, slot, facet, isTemplate)) {
                    frames.add(record.getFrame());
                }
            }
        }
        return frames;
    }

    public Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value,
            int maxMatches) {
        if (maxMatches < 1) {
            maxMatches = Integer.MAX_VALUE;
        }
        Set frames = new HashSet();
        Iterator i = getMatchingRecords(value, FrameStore.UNLIMITED_MATCHES).iterator();
        while (i.hasNext() && frames.size() < maxMatches) {
            Record record = (Record) i.next();
            if (matches(record, slot, facet, isTemplate)) {
                frames.add(record.getFrame());
            }
        }
        return frames;
    }

    public boolean beginTransaction(String name) {
        return false;
    }

    public boolean commitTransaction() {
        return true;
    }

    public boolean rollbackTransaction() {
        return false;
    }

    private void replaceFrame(Map map, Frame frame, boolean replaceValues) {
        Collection records = lookupRecords(map, frame);
        if (records != null) {
            map.put(frame, records);
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                record.replaceFrame(frame, replaceValues);
                if (!replaceValues) {
                    replaceReference(record);
                }
            }
        }
    }

    private void replaceReference(Record record) {
        Frame frame = record.getFrame();
        Slot slot = record.getSlot();
        Facet facet = record.getFacet();
        boolean isTemplate = record.isTemplate();
        Reference reference = new ReferenceImpl(frame, slot, facet, isTemplate);
        referenceToRecordMap.put(reference, record);
    }

    public void replaceFrame(Frame frame) {
        replaceFrame(frameToRecordsMap, frame, false);
        replaceFrame(slotToRecordsMap, frame, false);
        replaceFrame(facetToRecordsMap, frame, false);
        replaceFrame(valueToRecordsMap, frame, true);
    }

    public int getClsCount() {
        return countFrames(Cls.class);
    }

    public int getSlotCount() {
        return countFrames(Slot.class);
    }

    public int getFacetCount() {
        return countFrames(Facet.class);
    }

    public int getFrameCount() {
        return frameToRecordsMap.size();
    }

    public Set getFrames() {
        return new HashSet(frameToRecordsMap.keySet());
    }

    public int getSimpleInstanceCount() {
        return countFrames(SimpleInstance.class);
    }

    private int countFrames(Class clas) {
        return frames(clas).size();
    }

    private Set frames(Class clas) {
        Set uniqueValues = new HashSet();
        Iterator i = frameToRecordsMap.keySet().iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (clas.isInstance(o)) {
                uniqueValues.add(o);
            }
        }
        return uniqueValues;
    }

    public Frame getFrame(FrameID id) {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return ClosureUtils.calculateClosure(this, frame, slot, facet, isTemplate);
    }

    public NarrowFrameStore getDelegate() {
        return null;
    }

}