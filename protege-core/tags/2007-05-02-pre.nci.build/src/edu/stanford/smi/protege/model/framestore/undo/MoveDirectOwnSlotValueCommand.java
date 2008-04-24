package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class MoveDirectOwnSlotValueCommand extends AbstractCommand {
    private Frame frame;
    private Slot slot;
    private int from;
    private int to;

    MoveDirectOwnSlotValueCommand(FrameStore delegate, Frame frame, Slot slot, int from, int to) {
        super(delegate);
        this.frame = frame;
        this.slot = slot;
        this.from = from;
        this.to = to;
        setDescription("Move own slot value of instance " + getText(frame) + " and slot " + getText(slot)
                + " from index " + from + " to index " + to);
    }

    public Object doIt() {
        getDelegate().moveDirectOwnSlotValue(frame, slot, from, to);
        return null;
    }

    public void undoIt() {
        getDelegate().moveDirectOwnSlotValue(frame, slot, to, from);
    }

    public void redoIt() {
        getDelegate().moveDirectOwnSlotValue(frame, slot, from, to);
    }
}