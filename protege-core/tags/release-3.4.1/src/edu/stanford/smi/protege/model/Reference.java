package edu.stanford.smi.protege.model;

/**
 * A reference to a frame, slot, facet binding.  The facet may be null.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface Reference {

    Facet getFacet();

    Frame getFrame();

    Slot getSlot();

    boolean isTemplate();
}
