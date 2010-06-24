package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * Low-level holder of frames.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Storage extends Disposable {

    void addFrame(Frame frame);

    void addValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value);

    //ESCA-JAVA0138 
    void addValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value, int index);

    boolean beginTransaction();

    boolean containsFrame(Frame frame);

    boolean containsFrame(String name);

    boolean endTransaction(boolean doCommit);

    Frame getFrame(FrameID id);

    Frame getFrame(String name);

    int getFrameCount();

    int getFacetCount();

    int getSlotCount();

    int getClsCount();

    Collection getFrames();

    Collection<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String s, int maxMatches);

    Collection getFramesWithValue(Slot slot, Facet facet, boolean isTemplate, Object o);

    // back references
    Collection<Reference> getReferences(Object o, int maxReferences);

    Object getValue(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    int getValueCount(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    ArrayList getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    boolean hasValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value);

    boolean hasValueAtSomeFrame(Slot slot, Facet facet, boolean isTemplate);

    //ESCA-JAVA0138 
    void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to);

    void remove(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    void removeFrame(Frame frame);

    /** remove a single occurance of value from the slot/facet value */
    void removeSingleValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value);

    void removeValues(Slot slot, Facet facet, boolean isTemplate, Cls cls);

    void replace(Frame from, Frame to);

    void setValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value);

    void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values);

    boolean supportsTransactions();
}
