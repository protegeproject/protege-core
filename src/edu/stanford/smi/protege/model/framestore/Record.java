package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class Record {
    private Frame frame;
    private Slot slot;
    private Facet facet;
    private boolean isTemplate;
    private List values;
    
    public Record(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        this.frame = frame;
        this.slot = slot;
        this.facet = facet;
        this.isTemplate = isTemplate;
        setValues(values);
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
    
    public void removeValue(Object o) {
        boolean succeeded = values.remove(o);
        if (!succeeded) {
            System.err.println("Unable to remove " + o);
        }
    }
    
    public void moveValue(int from, int to) {
        Object value = values.remove(from);
        values.add(to, value);
    }
    
    public void replaceFrame(Frame replacementFrame, boolean replaceValues) {
        if (replacementFrame.equals(frame)) {
            frame = replacementFrame;
        }
        if (replacementFrame.equals(slot)) {
            slot = (Slot) replacementFrame;
        }
        if (replacementFrame.equals(facet)) {
            facet = (Facet) replacementFrame;
        }
        if (replaceValues) {
            ListIterator i = values.listIterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (replacementFrame.equals(o)) {
                    i.set(replacementFrame);
                }
            }
        }
    }
}
