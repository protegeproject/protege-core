package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.query.*;
import edu.stanford.smi.protege.util.*;

public class InMemoryFrameDb implements NarrowFrameStore {
    private static final int INITIAL_MAP_SIZE = 32771;
    private Map referenceToRecordMap = new HashMap(INITIAL_MAP_SIZE);
    private Map frameToRecordsMap = new HashMap(INITIAL_MAP_SIZE);
    private Map slotToRecordsMap = new HashMap(INITIAL_MAP_SIZE);
    private Map facetToRecordsMap = new HashMap(INITIAL_MAP_SIZE);
    private Map valueToRecordsMap = new LinkedHashMap(INITIAL_MAP_SIZE);

    private Record lookupRecord = new Record();
    private static int counter = FrameID.INITIAL_USER_FRAME_ID;

    private String frameDBName;

    public Collection getRecords() {
        return new ArrayList(referenceToRecordMap.keySet());
    }

    public String getName() {
        return frameDBName;
    }

    public void setName(String name) {
        frameDBName = name;
    }

    public InMemoryFrameDb(String name) {
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
        lookupRecord = null;
    }

    private Record lookup(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        lookupRecord.set(frame, slot, facet, isTemplate);
        return (Record) referenceToRecordMap.get(lookupRecord);
    }

    private void addRecord(Map map, Object key, Record record) {
        if (key != null) {
            Set set = (Set) map.get(key);
            if (set == null) {
                set = new HashSet();
                map.put(key, set);
            }
            set.add(record);
        }
    }

    public void removeRecord(Map map, Object key, Record record) {
        if (key != null) {
            Set set = (Set) map.get(key);
            if (set != null) {
                set.remove(record);
            }
        }
    }

    private void createRecord(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Record record = new Record(frame, slot, facet, isTemplate, values);
        addRecord(record);
    }

    private void addRecord(Record record) {
        referenceToRecordMap.put(record, record);
        addRecord(frameToRecordsMap, record.getFrame(), record);
        addRecord(slotToRecordsMap, record.getSlot(), record);
        addRecord(facetToRecordsMap, record.getFacet(), record);
        Iterator i = record.getValues().iterator();
        while (i.hasNext()) {
            Object value = i.next();
            addRecord(valueToRecordsMap, value, record);
        }
    }

    private void removeRecord(Record record) {
        referenceToRecordMap.remove(record);
        removeRecord(frameToRecordsMap, record.getFrame(), record);
        removeRecord(slotToRecordsMap, record.getSlot(), record);
        removeRecord(facetToRecordsMap, record.getFacet(), record);
        Iterator i = record.getValues().iterator();
        while (i.hasNext()) {
            Object value = i.next();
            removeRecord(valueToRecordsMap, value, record);
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
        removeRecords(frameToRecordsMap, frame);
        removeRecords(slotToRecordsMap, frame);
        removeRecords(facetToRecordsMap, frame);
        removeFrameValue(valueToRecordsMap, frame);
    }

    private void removeRecords(Map map, Frame frame) {
        Collection records = lookupRecords(map, frame);
        if (records != null) {
            records = new ArrayList(records);
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                removeRecord(record);
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

    public Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) {
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

    private void replaceFrameKey(Map map, Frame frame) {
        Collection records = (Collection) map.remove(frame);
        if (records != null) {
            map.put(frame, records);
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                record.replaceFrameReference(frame);
            }
        }
    }

    private void replaceFrameValues(Frame frame) {
        Collection records = (Collection) valueToRecordsMap.remove(frame);
        if (records != null) {
            valueToRecordsMap.put(frame, records);
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                record.replaceFrameValue(frame);
            }
        }
    }

    public void replaceFrame(Frame frame) {
        replaceFrameKey(frameToRecordsMap, frame);
        replaceFrameKey(slotToRecordsMap, frame);
        replaceFrameKey(facetToRecordsMap, frame);
        replaceFrameKey(valueToRecordsMap, frame);
        replaceFrameValues(frame);
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
        int count = 0;
        Slot slot = getNameSlot();
        if (slot != null) {
            Collection records = (Collection) slotToRecordsMap.get(slot);
            if (records != null) {
                count = records.size();
            }
        }
        return count;
    }

    public Set getFrames() {
        return new HashSet(frameToRecordsMap.keySet());
    }

    public int getSimpleInstanceCount() {
        return countFrames(SimpleInstance.class);
    }

    private Slot cachedNameSlot;

    private Slot getNameSlot() {
        if (cachedNameSlot == null) {
            Iterator i = referenceToRecordMap.keySet().iterator();
            if (i.hasNext()) {
                Record record = (Record) i.next();
                Frame frame = record.getFrame();
                KnowledgeBase kb = frame.getKnowledgeBase();
                cachedNameSlot = kb.getSlot(Model.Slot.NAME);
            }
        }
        return cachedNameSlot;
    }

    private int countFrames(Class clas) {
        int frameCount = 0;
        Slot nameSlot = getNameSlot();
        if (nameSlot != null) {
            Collection records = (Collection) slotToRecordsMap.get(nameSlot);
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                if (clas.isInstance(record.getFrame())) {
                    ++frameCount;
                }
            }
        }
        return frameCount;
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