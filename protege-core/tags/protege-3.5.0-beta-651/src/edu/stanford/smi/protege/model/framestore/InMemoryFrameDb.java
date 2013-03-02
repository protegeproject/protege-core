package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.ProtegeError;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SimpleStringMatcher;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

//ESCA-JAVA0100
public class InMemoryFrameDb implements NarrowFrameStore {
    private static Logger log = Log.getLogger(InMemoryFrameDb.class);

    private static final int INITIAL_MAP_SIZE = 32771;
    private final Map<FrameID, Frame> idToFrameMap = new HashMap<FrameID, Frame>(INITIAL_MAP_SIZE);
    private Map<Record, Record> referenceToRecordMap = new HashMap<Record, Record>(INITIAL_MAP_SIZE);
    private Map<Frame, Set<Record>> frameToRecordsMap = new HashMap<Frame, Set<Record>>(INITIAL_MAP_SIZE);
    private Map<Slot, Set<Record>> slotToRecordsMap = new HashMap<Slot, Set<Record>>(INITIAL_MAP_SIZE);
    private Map<Facet, Set<Record>> facetToRecordsMap = new HashMap<Facet, Set<Record>>(INITIAL_MAP_SIZE);
    private Map<Object, Set<Record>> valueToRecordsMap = new LinkedHashMap<Object, Set<Record>>(INITIAL_MAP_SIZE);

    private Record lookupRecord = new Record();
    private String frameDBName;

    public InMemoryFrameDb(String name) {
    	if (log.isLoggable(Level.FINE)) {
    		log.fine("Constructing InMemoryFrameDb with name " + name + " No delegate...");
    	}
        frameDBName = name;
    }


    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
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



    @Override
    public String toString() {
        return StringUtilities.getClassName(this) + "(" + frameDBName + ")";
    }

    /*
     * Search
     */


    private Set<Record> getMatchingRecords(String value, int maxMatches) {
        if (maxMatches < 1) {
            maxMatches = Integer.MAX_VALUE;
        }
        SimpleStringMatcher matcher = new SimpleStringMatcher(value);
        Set<Record> matches = new HashSet<Record>();
        Iterator<Map.Entry<Object, Set<Record>>> i = valueToRecordsMap.entrySet().iterator();
        while (i.hasNext() && matches.size() < maxMatches) {
            Map.Entry<Object, Set<Record>> entry = i.next();
            Object o = entry.getKey();
            if (o instanceof String && matcher.isMatch((String) o)) {
                Set<Record> records = entry.getValue();
                matches.addAll(records);
            }
        }
        return matches;
    }

    private static boolean matches(Record record, Slot slot, Facet facet, boolean isTemplate) {
        boolean matches = equals(slot, record.getSlot()) && equals(facet, record.getFacet())
                && isTemplate == record.isTemplate();
        return matches;
    }


    private int countFrames(Class clas) {
        int frameCount = 0;
        Slot nameSlot = getNameSlot();
        if (nameSlot != null) {
            Collection records = slotToRecordsMap.get(nameSlot);
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


    private Record lookup(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        lookupRecord.set(frame, slot, facet, isTemplate);
        return referenceToRecordMap.get(lookupRecord);
    }


    private static <X, Y>  Set<Y> lookupRecords(Map<X, Set<Y>> map, Object value) {
        return map.get(value);
    }

    /*
     * Creating, adding, removing records
     */


    public void addValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Record record = lookup(frame, slot, facet, isTemplate);
        if (record == null) {
            createRecord(frame, slot, facet, isTemplate, CollectionUtilities.createList(value));
        } else {
            record.addValue(value);
            addRecord(valueToRecordsMap, value, record);
        }
    }

    private static <X> void removeFrameValue(Map<X, Set<Record>> map, Frame frame) {
        Set<Record> records = lookupRecords(map, frame);
        if (records != null) {
            Iterator<Record> i = records.iterator();
            while (i.hasNext()) {
                Record record = i.next();
                record.removeValue(frame);
            }
            map.remove(frame);
        }
    }


    private void addRecordValues(Record record) {
        Iterator i = record.getInternalValues().iterator();
        while (i.hasNext()) {
            Object value = i.next();
            addRecord(valueToRecordsMap, value, record);
        }
    }


    private void removeRecordValues(Record record) {
        Iterator i = record.getInternalValues().iterator();
        while (i.hasNext()) {
            Object value = i.next();
            removeRecord(valueToRecordsMap, value, record);
        }
    }


    private void createRecord(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Record record = new Record(frame, slot, facet, isTemplate, values);
        addRecord(record);
    }

    private void addRecord(Record record) {
        referenceToRecordMap.put(record, record);
        idToFrameMap.put(record.getFrame().getFrameID(), record.getFrame());
        addRecord(frameToRecordsMap, record.getFrame(), record);
        addRecord(slotToRecordsMap, record.getSlot(), record);
        addRecord(facetToRecordsMap, record.getFacet(), record);
        for (Object value : record.getValues()) {
            addRecord(valueToRecordsMap, value, record);
        }
    }

    private static <X> void addRecord(Map<X, Set<Record>> map, X key, Record record) {
        if (key != null) {
            Set<Record> set = map.get(key);
            if (set == null) {
                set = new HashSet<Record>();
                map.put(key, set);
            }
            set.add(record);
        }
    }

    private void removeRecord(Record record) {
        referenceToRecordMap.remove(record);
        removeRecord(frameToRecordsMap, record.getFrame(), record);
        removeRecord(slotToRecordsMap, record.getSlot(), record);
        removeRecord(facetToRecordsMap, record.getFacet(), record);
        for (Object value : record.getValues()) {
            removeRecord(valueToRecordsMap, value, record);
        }
        Set<Record> records = lookupRecords(frameToRecordsMap, record.getFrame());
        if (records == null || records.isEmpty()) {
          idToFrameMap.remove(record.getFrame().getFrameID());
        }

    }

    public static <X> void removeRecord(Map<X, Set<Record>> map, Object key, Record record) {
        if (key != null) {
            Set<Record> set = map.get(key);
            if (set != null) {
                set.remove(record);
            }
        }
    }



    private <X> void removeRecords(Map<X, Set<Record>> map, Frame frame) {
        Collection<Record> records = lookupRecords(map, frame);
        if (records != null) {
            records = new ArrayList<Record>(records);
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                removeRecord(record);
            }
            map.remove(frame);
        }
    }

    /*
     * Frame Replacement
     */

    @SuppressWarnings("unchecked")
    private <X> void replaceFrameKey(Map<X, Set<Record>> map, Frame frame) {
        Set<Record> records = map.remove(frame);
        if (records != null) {
            map.put((X) frame, records);
            for (Record record : records) {
                record.replaceFrameReference(frame);
                referenceToRecordMap.remove(record);
                referenceToRecordMap.put(record, record);
            }
        }
    }


    private void replaceRecords(Frame original, Frame replacement, Set<Record> records) {
      for (Record r : records) {
        Record newRecord = new Record(r.getFrame().equals(original) ? replacement : r.getFrame(),
                                      r.getSlot().equals(original) ? (Slot) replacement : r.getSlot(),
                                      (r.getFacet() != null && r.getFacet().equals(original)) ?
                                          (Facet) replacement : r.getFacet(),
                                      r.isTemplate(),
                                      r.getValues());
        if (newRecord.getFrame().equals(replacement)
            && newRecord.getSlot().getFrameID().equals(Model.SlotID.NAME)
            && newRecord.getFacet() == null
            && !newRecord.isTemplate()) {
          List values = Collections.singletonList(replacement.getName());
          newRecord.setValues(values);
        }
        removeRecord(r);
        addRecord(newRecord);
      }
      original.markDeleted(true);
    }

    /*
     * Records and References
     */


    public Collection<Record> getRecords() {
        return new ArrayList<Record>(referenceToRecordMap.keySet());
    }


    private static Set<Reference> recordsToReferences(Set<Record> records) {
        Set<Reference> references;
        if (records == null) {
            references = Collections.EMPTY_SET;
        } else {
            references = new HashSet<Reference>(records.size());
            Iterator i = records.iterator();
            while (i.hasNext()) {
                Record record = (Record) i.next();
                references.add(recordToReference(record));
            }
        }
        return references;
    }

    private static Reference recordToReference(Record record) {
        Frame frame = record.getFrame();
        Slot slot = record.getSlot();
        Facet facet = record.getFacet();
        boolean isTemplate = record.isTemplate();
        return new ReferenceImpl(frame, slot, facet, isTemplate);
    }



    /* ---------------------------------------------------------------
     * Frame Store methods
     */

    public String getName() {
        return frameDBName;
    }

    public void setName(String name) {
        frameDBName = name;
    }

    public NarrowFrameStore getDelegate() {
        return null;
    }

    public int getFrameCount() {
        int count = 0;
        Slot slot = getNameSlot();
        if (slot != null) {
            Collection records = slotToRecordsMap.get(slot);
            if (records != null) {
                count = records.size();
            }
        }
        return count;
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

    public int getSimpleInstanceCount() {
        return countFrames(SimpleInstance.class);
    }

    public Set<Frame> getFrames() {
        return new HashSet<Frame>(frameToRecordsMap.keySet());
    }

    public Frame getFrame(FrameID id) {
      return idToFrameMap.get(id);
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

    public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            addValue(frame, slot, facet, isTemplate, value);
        }
    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        Record record = lookup(frame, slot, facet, isTemplate);
        if (record != null) {
            record.moveValue(from, to);
        }
    }

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Record record = lookup(frame, slot, facet, isTemplate);
        if (record != null) {
            record.removeValue(value);
            removeRecord(valueToRecordsMap, value, record);
            if (record.isEmpty()) {
                removeRecord(record);
            }
        }

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

    public Set<Frame>getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        Set<Frame> frames = new HashSet<Frame>();
        Set<Record> records = lookupRecords(valueToRecordsMap, value);
        if (records != null) {
            Iterator<Record> i = records.iterator();
            while (i.hasNext()) {
                Record record = i.next();
                if (matches(record, slot, facet, isTemplate)) {
                    frames.add(record.getFrame());
                }
            }
        }
        return frames;
    }


    public Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
        Set<Frame> frames = new HashSet<Frame>();
        Collection<Record> records;
        if (facet != null) {
            records = lookupRecords(facetToRecordsMap, facet);
        } else {
            records = lookupRecords(slotToRecordsMap, slot);
        }
        if (records != null) {
            Iterator<Record> i = records.iterator();
            while (i.hasNext()) {
                Record record = i.next();
                if (matches(record, slot, facet, isTemplate)) {
                    frames.add(record.getFrame());
                }
            }
        }
        return frames;
    }

    public Set<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) {
        if (maxMatches < 1) {
            maxMatches = Integer.MAX_VALUE;
        }
        Set<Frame> frames = new HashSet<Frame>();
        Iterator<Record> i = getMatchingRecords(value, FrameStore.UNLIMITED_MATCHES).iterator();
        while (i.hasNext() && frames.size() < maxMatches) {
            Record record = i.next();
            if (matches(record, slot, facet, isTemplate)) {
                frames.add(record.getFrame());
            }
        }
        return frames;
    }

    public Set<Reference> getReferences(Object value) {
        Set<Record> records = lookupRecords(valueToRecordsMap, value);
        return recordsToReferences(records);
    }

    public Set<Reference> getMatchingReferences(String value, int maxMatches) {
        Set<Record> records = getMatchingRecords(value, maxMatches);
        return recordsToReferences(records);
    }

    /** TODO implement executeQuery */
    public void executeQuery(Query query, final QueryCallback callback) {
      new Thread(new Runnable() {
          public void run() {
            callback.handleError(new ProtegeError("Not implemented yet"));
          }
        },
                 "Vacuous In MemoryDb callback thread");
    }

    public void deleteFrame(Frame frame) {
        removeRecords(frameToRecordsMap, frame);
        removeRecords(slotToRecordsMap, frame);
        removeRecords(facetToRecordsMap, frame);
        removeFrameValue(valueToRecordsMap, frame);
    }

    public void close() {
        referenceToRecordMap = null;
        frameToRecordsMap = null;
        slotToRecordsMap = null;
        facetToRecordsMap = null;
        valueToRecordsMap = null;
        lookupRecord = null;
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return ClosureUtils.calculateClosure(this, frame, slot, facet, isTemplate);
    }

    public void replaceFrame(Frame frame) {
        if (idToFrameMap.get(frame.getFrameID()) != null) {
            idToFrameMap.put(frame.getFrameID(), frame);
        }
        replaceFrameKey(frameToRecordsMap, frame);
        replaceFrameKey(slotToRecordsMap, frame);
        replaceFrameKey(facetToRecordsMap, frame);
        replaceFrameKey(valueToRecordsMap, frame);

        Set<Record> records = valueToRecordsMap.get(frame);
        if (records != null) {
            for (Record record : records)  {
                record.replaceFrameValue(frame);
            }
        }
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

    public TransactionMonitor getTransactionStatusMonitor()  {
      return null;
    }

	public void reinitialize() {
	}

	public boolean setCaching(RemoteSession session, boolean doCache) {
	    return false;
	}

    @SuppressWarnings("unchecked")
    public void replaceFrame(Frame original, Frame replacement) {
      if (original.equals(replacement)) {
        return;
      }
      if (idToFrameMap.containsKey(original.getFrameID())) {
        idToFrameMap.remove(original.getFrameID());
        idToFrameMap.put(replacement.getFrameID(),  replacement);
      }

      Set<Record> recordsToChange = new HashSet<Record>();
      if (frameToRecordsMap.get(original) != null) {
        for (Record r : frameToRecordsMap.get(original)) {
          recordsToChange.add(r);
        }
      }
      if (slotToRecordsMap.get(original) != null) {
        for (Record r : slotToRecordsMap.get(original)) {
          recordsToChange.add(r);
        }
      }
      if (facetToRecordsMap.get(original) != null) {
        for (Record r : facetToRecordsMap.get(original)) {
          recordsToChange.add(r);
        }
      }
      replaceRecords(original, replacement, recordsToChange);

      if (valueToRecordsMap.get(original) != null) {
        for (Record r : valueToRecordsMap.get(original)) {
          List values = r.getValues();
          int index;
          while ((index = values.indexOf(original)) != -1) {
            values.remove(index);
            values.add(index, replacement);
          }
          r.setValues(values);
        }
        valueToRecordsMap.put(replacement, valueToRecordsMap.get(original));
        valueToRecordsMap.remove(original);
      }
      deleteFrame(original);
    }



}
