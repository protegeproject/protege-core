package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;

public interface NarrowFrameStore {

  /**
   * The Narrow Frame store has a standard set/get name interface.
   * One of the purposes of this interface is to allow the MergingNarrowFrameStore
   * match names of the narrow frame stores of including and included 
   * projects.  For this purpose the name of the narrow frame store
   * is the string representation of the uri for the project.
   * 
   * @return the name of this narrow frame store.
   */
    String getName();

    /**
     * The Narrow Frame store has a standard set/get name interface.
     * One of the purposes of this interface is to allow the MergingNarrowFrameStore
     * match names of the narrow frame stores of including and included 
     * projects.  For this purpose the name of the narrow frame store
     * is the string representation of the uri for the project.
     * 
     * @param name - the name of the Narrow Frame Store.  
     */
    void setName(String name);

    NarrowFrameStore getDelegate();

    FrameID generateFrameID();

    int getFrameCount();

    int getClsCount();

    int getSlotCount();

    int getFacetCount();

    int getSimpleInstanceCount();

    Set<Frame> getFrames();

    Frame getFrame(FrameID id);

    /**
     * Obtains the values of a slot to a frame.  It consists of a list of Strings, Integers
     * Floats and Frames.
     * 
     * @param frame - the frame
     * @param slot the slot 
     * @param facet to be determined
     * @param isTemplate to be determined
     * @return a list of the values of the slot of the frame.
     */
    List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values);

    void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to);

    void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value);

    void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values);

    Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate, Object value);

    Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate);

    Set<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches);

    Set<Reference> getReferences(Object value);

    Set<Reference> getMatchingReferences(String value, int maxMatches);

    Set executeQuery(Query query);

    void deleteFrame(Frame frame);

    void close();

    Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    /**
     * A complete hack to work around problems with the java packages feature
     */
    void replaceFrame(Frame frame);

    boolean beginTransaction(String name);

    boolean commitTransaction();

    boolean rollbackTransaction();
}