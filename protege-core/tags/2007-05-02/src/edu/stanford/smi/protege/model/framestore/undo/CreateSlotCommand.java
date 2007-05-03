package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class CreateSlotCommand extends AbstractCommand {
    private FrameID id;
    private String name;
    private Collection types;
    private Collection superslots;
    private boolean loadDefaults;
    private Slot createdSlot;

    CreateSlotCommand(FrameStore delegate, FrameID id, String name, Collection types, Collection superslots,
            boolean loadDefaults) {
        super(delegate);
        this.id = id;
        this.name = name;
        this.loadDefaults = loadDefaults;
        this.types = new ArrayList(types);
        this.superslots = new ArrayList(superslots);
    }

    public Object doIt() {
        createdSlot = getDelegate().createSlot(id, name, types, superslots, loadDefaults);
        id = createdSlot.getFrameID();
        name = getDelegate().getFrameName(createdSlot);
        setDescription("Create slot " + getText(createdSlot));
        return createdSlot;
    }

    public void undoIt() {
        getDelegate().deleteSlot(createdSlot);
        createdSlot.markDeleted(true);
    }

    public void redoIt() {
        getDelegate().createSlot(id, name, types, superslots, loadDefaults);
        createdSlot.markDeleted(false);
    }
}