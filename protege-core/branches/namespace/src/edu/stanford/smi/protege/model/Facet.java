package edu.stanford.smi.protege.model;

import java.util.*;

import edu.stanford.smi.protege.event.*;

/**
 * A description of a constraint on the value of a frame-slot binding.
 * This constraint may be placed on any number of frame-slot bindings.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Facet extends Instance {

    void addFacetListener(FacetListener listener);

    boolean areValidValues(Frame frame, Slot slot, Collection values);

    Slot getAssociatedSlot();

    FacetConstraint getConstraint();

    String getInvalidValuesText(Frame frame, Slot slot, Collection values);

    String getInvalidValueText(Frame frame, Slot slot, Object item);

    ValueType getValueType();

    boolean getAllowsMultipleValues();

    boolean isValidValue(Frame frame, Slot slot, Object value);

    void removeFacetListener(FacetListener listener);

    void setAssociatedSlot(Slot slot);

    void setConstraint(FacetConstraint constraint);

    Collection resolveValues(Collection originalValues, Collection newValues);
    
    Facet rename(String name);
}
