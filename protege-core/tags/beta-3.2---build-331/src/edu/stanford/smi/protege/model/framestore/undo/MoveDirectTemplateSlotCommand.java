package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class MoveDirectTemplateSlotCommand extends AbstractCommand {
    private Slot slot;
    private Cls cls;
    private int index;
    private int oldIndex;

    MoveDirectTemplateSlotCommand(FrameStore delegate, Slot slot, Cls cls, int index) {
        super(delegate);
        this.slot = slot;
        this.cls = cls;
        this.index = index;
        setDescription("Move template slot " + getText(slot) + " to index " + index);
    }

    public Object doIt() {
        oldIndex = getDelegate().getDirectTemplateSlots(cls).indexOf(slot);
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
        return null;
    }

    public void undoIt() {
        getDelegate().moveDirectTemplateSlot(cls, slot, oldIndex);
    }

    public void redoIt() {
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
    }
}