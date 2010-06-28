package edu.stanford.smi.protege.model;

import edu.stanford.smi.protege.util.*;

/**
 * A holder for a frame-slot pair.  This combination comes up often enought that this class is useful.  The particular
 * reason why the frame and the slot are being held together is not specified by this class but should be evident from
 * the use.  Typical reasons are things like "a facet override goes on a class-slot pair" or an "own slot value is from
 * a particular frame-slot pair".
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FrameSlotCombination {
    private Frame _frame;
    private Slot _slot;

    public FrameSlotCombination(Frame frame, Slot slot) {
        this._frame = frame;
        this._slot = slot;
    }

    public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof FrameSlotCombination) {
            FrameSlotCombination other = (FrameSlotCombination) o;
            equals = equals(other._frame, _frame) && equals(other._slot, _slot);
        }
        return equals;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public Frame getFrame() {
        return _frame;
    }

    public Slot getSlot() {
        return _slot;
    }

    public int hashCode() {
        return _frame.hashCode() ^ _slot.hashCode();
    }
}
