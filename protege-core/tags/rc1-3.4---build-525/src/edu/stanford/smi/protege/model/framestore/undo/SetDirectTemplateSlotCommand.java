package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class SetDirectTemplateSlotCommand extends AbstractCommand {
    private Cls cls;
    private Slot slot;
    private Collection values;
    private List oldValues;

    SetDirectTemplateSlotCommand(FrameStore delegate, Cls cls, Collection values, Slot slot) {
        super(delegate);
        this.cls = cls;
        this.values = new ArrayList(values);
        this.slot = slot;
        String description = "Set template slot " + getText(slot) + " at class " + getText(cls) + " to values "
                + getText(values);
        setDescription(description);
    }

    public Object doIt() {
        oldValues = getDelegate().getDirectTemplateSlotValues(cls, slot);
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
        return null;
    }

    public void undoIt() {
        getDelegate().setDirectTemplateSlotValues(cls, slot, oldValues);
    }

    public void redoIt() {
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
    }
}