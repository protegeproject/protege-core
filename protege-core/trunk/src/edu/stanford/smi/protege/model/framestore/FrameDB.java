package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.query.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameDB implements NarrowFrameStore {
    Map fsftToRecord = new HashMap();
    Map frameToRecords = new HashMap();
    Map slotToRecords = new HashMap();
    Map facetToRecords = new HashMap();
    Map valueToRecords = new HashMap();
    Slot nameSlot;
    Fsft lookupFsft;
    String frameDBName;

    public String getName() {
        return frameDBName;
    }

    public void setName(String name) {
        frameDBName = name;
    }

    public FrameDB(String name) {
        this.frameDBName = name;
    }

    private Record lookup(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        lookupFsft.set(frame, slot, facet, isTemplate);
        return (Record) fsftToRecord.get(lookupFsft);
    }

    public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Record record = lookup(frame, slot, facet, isTemplate);
        return (record == null) ? Collections.EMPTY_LIST : record.getValues();
    }

    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, List values) {
        Record record = lookup(frame, slot, facet, isTemplate);
        if (record == null) {
            addRecord(frame, slot, facet, isTemplate, values);
        } else {
            removeRecordValues(record);
            record.setValues(values);
            addRecordValues(record);
        }
    }

    public Frame getFrame(String name) {
        Frame frame = null;
        Set records = (Set) valueToRecords.get(name);
        Iterator i = records.iterator();
        while (i.hasNext()) {
            Record record = (Record) i.next();
            if (record.getSlot().equals(nameSlot) && record.getFacet() == null
                    && !record.isTemplate()) {
                frame = record.getFrame();
                break;
            }
        }
        return frame;
    }

    private void insertRecord(Map map, Object key, Record record) {
        Set set = (Set) map.get(key);
        if (set == null) {
            set = new HashSet();
            map.put(key, set);
        }
        set.add(record);
    }

    private void removeRecord(Map map, Object key, Record record) {
        Set set = (Set) map.get(key);
        if (set != null) {
            set.remove(record);
        }

    }

    private void addRecord(Frame frame, Slot slot, Facet facet, boolean isTemplate, List values) {
        Record record = new Record(frame, slot, facet, isTemplate, values);
        fsftToRecord.put(new Fsft(frame, slot, facet, isTemplate), record);
        insertRecord(frameToRecords, frame, record);
        insertRecord(slotToRecords, slot, record);
        insertRecord(facetToRecords, facet, record);
        addRecordValues(record);
    }

    private void addRecordValues(Record record) {
        Iterator i = record.getInternalValues().iterator();
        while (i.hasNext()) {
            Object value = i.next();
            insertRecord(valueToRecords, value, record);
        }
    }

    private void removeRecordValues(Record record) {
        Iterator i = record.getInternalValues().iterator();
        while (i.hasNext()) {
            Object value = i.next();
            removeRecord(valueToRecords, value, record);
        }
    }

    public NarrowFrameStore getDelegate() {
        // TODO Auto-generated method stub
        return null;
    }

    public FrameID generateFrameID() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getFrameCount() {
        return frameToRecords.size();
    }

    public int getClsCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getSlotCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getFacetCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getSimpleInstanceCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Set getFrames() {
        // TODO Auto-generated method stub
        return null;
    }

    public Frame getFrame(FrameID id) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        // TODO Auto-generated method stub

    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
        // TODO Auto-generated method stub

    }

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        // TODO Auto-generated method stub

    }

    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        // TODO Auto-generated method stub

    }

    public Set getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    public Set getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
        // TODO Auto-generated method stub
        return null;
    }

    public Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value,
            int maxMatches) {
        // TODO Auto-generated method stub
        return null;
    }

    public Set getReferences(Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    public Set getMatchingReferences(String value, int maxMatches) {
        // TODO Auto-generated method stub
        return null;
    }

    public Set executeQuery(Query query) {
        // TODO Auto-generated method stub
        return null;
    }

    public void deleteFrame(Frame frame) {
        // TODO Auto-generated method stub

    }

    public void close() {
        // TODO Auto-generated method stub

    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        // TODO Auto-generated method stub
        return null;
    }

    public void replaceFrame(Frame frame) {
        // TODO Auto-generated method stub

    }

    public boolean beginTransaction(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean commitTransaction() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean rollbackTransaction() {
        // TODO Auto-generated method stub
        return false;
    }
}

class Fsft {
    private Frame frame;
    private Slot slot;
    private Facet facet;
    private boolean isTemplate;

    public Fsft(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        set(frame, slot, facet, isTemplate);
    }

    public Frame getFrame() {
        return frame;
    }

    public Slot getSlot() {
        return slot;
    }

    public Facet getFacet() {
        return facet;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void set(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        this.frame = frame;
        this.slot = slot;
        this.facet = facet;
        this.isTemplate = isTemplate;
    }
}

