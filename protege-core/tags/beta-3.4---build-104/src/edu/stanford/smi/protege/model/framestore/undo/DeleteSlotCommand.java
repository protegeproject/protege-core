package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class DeleteSlotCommand extends SimpleCommand {
    private Slot slot;
    private String name;
    private FrameID id;
    private Collection directTypes;
    private Collection directSuperslots;

    DeleteSlotCommand(FrameStore delegate, Slot slot) {
        super(delegate);
        this.slot = slot;
        this.name = slot.getName();
        this.id = slot.getFrameID();
        this.directTypes = new ArrayList(slot.getDirectTypes());
        this.directSuperslots = new ArrayList(slot.getDirectSuperslots());
        setDescription("Delete slot " + getText(slot));
    }
    public Object doIt() {
        getDelegate().deleteSlot(slot);
        slot.markDeleted(true);
        return null;
    }
    public void undoIt() {
        getDelegate().createSlot(id, name, directTypes, directSuperslots, false);
        slot.markDeleted(false);
    }
}