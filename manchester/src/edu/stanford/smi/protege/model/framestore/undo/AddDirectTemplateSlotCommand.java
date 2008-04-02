package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class AddDirectTemplateSlotCommand extends SimpleCommand {
    private Cls cls;
    private Slot slot;

    AddDirectTemplateSlotCommand(FrameStore delegate, Cls cls, Slot slot) {
        super(delegate);
        this.cls = cls;
        this.slot = slot;
        setDescription("Add template slot " + getText(slot) + " to " + getText(cls));
    }

    public Object doIt() {
        getDelegate().addDirectTemplateSlot(cls, slot);
        return null;
    }

    public void undoIt() {
        getDelegate().removeDirectTemplateSlot(cls, slot);
    }

}