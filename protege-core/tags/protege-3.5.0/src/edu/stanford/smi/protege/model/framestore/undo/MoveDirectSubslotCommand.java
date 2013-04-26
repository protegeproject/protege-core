package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class MoveDirectSubslotCommand extends AbstractCommand {
    private Slot slot;
    private int index;
    private Slot subslot;
    private int oldIndex;

    MoveDirectSubslotCommand(FrameStore delegate, Slot slot, int index, Slot subslot) {
        super(delegate);
        this.slot = slot;
        this.index = index;
        this.subslot = subslot;
        oldIndex = getDelegate().getDirectSubslots(slot).indexOf(subslot);
        setDescription("Move subslot " + getText(subslot) + " of slot " + getText(slot) + " to index " + index);
    }

    public Object doIt() {
        getDelegate().moveDirectSubslot(slot, subslot, index);
        return null;
    }

    public void undoIt() {
        getDelegate().moveDirectSubslot(slot, subslot, oldIndex);
    }

    public void redoIt() {
        getDelegate().moveDirectSubslot(slot, subslot, index);
    }
}
