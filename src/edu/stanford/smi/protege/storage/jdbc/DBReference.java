package edu.stanford.smi.protege.storage.jdbc;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * The equalvalent of a "reference" (see {@link edu.stanford.smi.protege.model.Reference}) the the database.  This means
 * that frames are reference by FrameID rather than by pointer.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DBReference {
    private FrameID _frame;
    private FrameID _slot;
    private FrameID _facet;
    private boolean _isTemplate;
    private int _hashCode;

    public DBReference(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        _frame = frame.getFrameID();
        _slot = slot.getFrameID();
        _facet = (facet == null) ? (FrameID) null : facet.getFrameID();
        _isTemplate = isTemplate;
        _hashCode = calculateHash();
    }

    public DBReference(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate) {
        _frame = frame;
        _slot = slot;
        _facet = facet;
        _isTemplate = isTemplate;
        _hashCode = calculateHash();
    }

    private int calculateHash() {
        return HashUtils.getHash(_frame, _slot, _facet, _isTemplate);
    }

    public boolean equals(Object o) {
        DBReference other = (DBReference) o;
        return equals(getFrame(), other._frame)
            && equals(getSlot(), other._slot)
            && equals(getFacet(), other._facet)
            && getIsTemplate() == other._isTemplate;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    /**
     * Return the value of the field facet
     */
    public FrameID getFacet() {
        return _facet;
    }

    /**
     * Return the value of the field frame
     */
    public FrameID getFrame() {
        return _frame;
    }

    /**
     * Return the value of the field isTemplate
     */
    public boolean getIsTemplate() {
        return _isTemplate;
    }

    /**
     * Return the value of the field slot
     */
    public FrameID getSlot() {
        return _slot;
    }

    public int hashCode() {
        return _hashCode;
    }
}
