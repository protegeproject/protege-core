package edu.stanford.smi.protege.model.framestore;

import java.io.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

public class ReferenceImpl implements Reference, Serializable, Localizable {
    private static final long serialVersionUID = 8659902573681918672L;
    private Frame _frame;
    private Slot _slot;
    private Facet _facet;
    private boolean _isTemplate;
    private int _hashCode;

    public ReferenceImpl(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        set(frame, slot, facet, isTemplate);
    }

    public ReferenceImpl() {
    }

    public void replace(Frame frame) {
        if (frame.equals(_frame)) {
            _frame = frame;
        } else if (frame.equals(_slot)) {
            _slot = (Slot) frame;
        } else if (frame.equals(_facet)) {
            _facet = (Facet) frame;
        }
    }

    public void set(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        if (frame == null) {
            throw new RuntimeException("null frame");
        } else if (slot == null) {
            throw new RuntimeException("null slot");
        }
        _frame = frame;
        _slot = slot;
        _facet = facet;
        _isTemplate = isTemplate;
        _hashCode = HashUtils.getHash(frame, slot, facet, isTemplate);
    }

    public Frame getFrame() {
        return _frame;
    }

    public Slot getSlot() {
        return _slot;
    }

    public Facet getFacet() {
        return _facet;
    }

    public String toString() {
        return "Reference(" + _frame + ", " + _slot + ", " + _facet + ")";
    }

    public boolean isTemplate() {
        return _isTemplate;
    }

    public boolean equals(Object o) {
        ReferenceImpl rhs = (ReferenceImpl) o;
        return equals(_frame, rhs._frame) && equals(_slot, rhs._slot) && equals(_facet, rhs._facet)
                && _isTemplate == rhs._isTemplate;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public int hashCode() {
        return _hashCode;
    }

    public boolean usesFrame(Frame frame) {
        return frame.equals(_frame) || frame.equals(_slot) || frame.equals(_facet);
    }

    public void localize(KnowledgeBase kb) {
        LocalizeUtils.localize(_frame, kb);
        LocalizeUtils.localize(_slot, kb);
        LocalizeUtils.localize(_facet, kb);
    }
}