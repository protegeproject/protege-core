package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class Record {
    private Frame frame;
    private Slot slot;
    private Facet facet;
    private boolean isTemplate;
    private List values;
    private int hashCode;

    public Record(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        set(frame, slot, facet, isTemplate);
        setValues(values);
    }

    Record() {
    }

    void set(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        this.frame = frame;
        this.slot = slot;
        this.facet = facet;
        this.isTemplate = isTemplate;
        this.hashCode = HashUtils.getHash(frame, slot, facet, isTemplate);
    }

    public int hashCode() {
        return hashCode;
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

    public List getInternalValues() {
        //ESCA-JAVA0259 
        return values;
    }

    public int getValueCount() {
        return values.size();
    }

    public List getValues() {
        return (values.isEmpty()) ? Collections.EMPTY_LIST : new ArrayList(values);
    }

    public void setValues(Collection values) {
        this.values = new ArrayList(values);
    }

    public void addValue(Object o) {
        values.add(o);
    }

    public boolean removeValue(Object o) {
        return values.remove(o);
    }

    public void moveValue(int from, int to) {
        Object value = values.remove(from);
        values.add(to, value);
    }

    @SuppressWarnings("unchecked")
    public void replaceFrameReference(Frame replacementFrame) {
        if (replacementFrame.equals(frame)) {
            frame = replacementFrame;
        }
        if (replacementFrame.equals(slot)) {
            slot = (Slot) replacementFrame;
        }
        if (replacementFrame.equals(facet)) {
            facet = (Facet) replacementFrame;
        }
    }

    public void replaceFrameValue(Frame replacementFrame) {
        ListIterator i = values.listIterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (replacementFrame.equals(o)) {
                i.set(replacementFrame);
            }
        }
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }
    
    public String toString() {
      return "<Record " + frame.getFrameID().getName() + ":" + slot.getFrameID().getName()
                 + ":" + (facet == null ? "null" : facet.getFrameID().getName())
                 + ":" + isTemplate + ">";
    }

    public boolean equals(Object o) {
        boolean areEqual = false;
        if (o instanceof Record) {
            Record otherRecord = (Record) o;
            areEqual = equals(frame, otherRecord.frame) && equals(slot, otherRecord.slot)
                    && equals(facet, otherRecord.facet) && isTemplate == otherRecord.isTemplate;
        }
        return areEqual;
    }

    private static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }
}