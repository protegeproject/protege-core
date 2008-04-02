package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class RemoveDirectTemplateSlotCommand extends SimpleCommand {
    private Slot slot;
    private Cls cls;

    RemoveDirectTemplateSlotCommand(FrameStore delegate, Slot slot, Cls cls) {
        super(delegate);
        this.slot = slot;
        this.cls = cls;
        setDescription("Remove template slot " + getText(slot) + " from " + getText(cls));
    }

    public Object doIt() {
        // TODO save own slot values for instances of the class
        getDelegate().removeDirectTemplateSlot(cls, slot);
        return null;
    }

    public void undoIt() {
        // TODO restore own slot values for instances of the class
        getDelegate().addDirectTemplateSlot(cls, slot);
    }
}