package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class AddDirectSuperslotCommand extends AbstractCommand {
    private Slot superslot;
    private Slot slot;

    AddDirectSuperslotCommand(FrameStore delegate, Slot superslot, Slot slot) {
        super(delegate);
        this.superslot = superslot;
        this.slot = slot;
        setDescription("Add superslot " + getText(superslot) + " to slot " + getText(slot));
    }

    public Object doIt() {
        getDelegate().addDirectSuperslot(slot, superslot);
        return null;
    }

    public void undoIt() {
        getDelegate().removeDirectSuperslot(slot, superslot);
    }

    public void redoIt() {
        getDelegate().addDirectSuperslot(slot, superslot);
    }
}