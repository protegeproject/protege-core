package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class SetDirectOwnSlotValuesCommand extends AbstractCommand {
    private Collection values;
    private Slot slot;
    private Frame frame;
    private List _oldValues;

    SetDirectOwnSlotValuesCommand(FrameStore delegate, Collection values, Slot slot, Frame frame) {
        super(delegate);
        this.values = new ArrayList(values);
        this.slot = slot;
        this.frame = frame;
        setDescription("Set own slot " + getText(slot) + " at frame " + getText(frame) + " to {" + getText(values)
                + "}");
    }

    public Object doIt() {
        _oldValues = getDelegate().getDirectOwnSlotValues(frame, slot);
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
        return null;
    }

    public void undoIt() {
        getDelegate().setDirectOwnSlotValues(frame, slot, _oldValues);
    }

    public void redoIt() {
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
    }
}