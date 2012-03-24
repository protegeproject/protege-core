package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class RemoveDirectSuperslotCommand extends AbstractCommand {
    private Slot superslot;
    private Slot slot;

    RemoveDirectSuperslotCommand(FrameStore delegate, Slot superslot, Slot slot) {
        super(delegate);
        this.superslot = superslot;
        this.slot = slot;
        setDescription("Remove superslot " + getText(superslot) + " from slot " + getText(slot));
    }

    public Object doIt() {
        // TODO should save "facet" values
        getDelegate().removeDirectSuperslot(slot, superslot);
        return null;
    }

    public void undoIt() {
        // TODO should restore "facet" values
        getDelegate().addDirectSuperslot(slot, superslot);
    }

    public void redoIt() {
        getDelegate().removeDirectSuperslot(slot, superslot);
    }
}