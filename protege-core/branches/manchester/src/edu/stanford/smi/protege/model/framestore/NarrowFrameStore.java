package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.query.*;

public interface NarrowFrameStore {

    String getName();

    void setName(String name);

    NarrowFrameStore getDelegate();

    FrameID generateFrameID();

    int getFrameCount();

    int getClsCount();

    int getSlotCount();

    int getFacetCount();

    int getSimpleInstanceCount();

    Set getFrames();

    Frame getFrame(FrameID id);

    List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate);

    void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values);

    void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to);

    void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value);

    void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values);

    Set getFrames(Slot slot, Facet facet, boolean isTemplate, Object value);

    Set getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate);

    Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches);

    Set getReferences(Object value);

    Set getMatchingReferences(String value, int maxMatches);

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