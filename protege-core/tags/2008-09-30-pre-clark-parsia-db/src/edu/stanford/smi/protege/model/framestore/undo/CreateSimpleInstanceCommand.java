package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class CreateSimpleInstanceCommand extends AbstractCommand {
    private FrameID id;
    private Collection types;
    private boolean loadDefaults;
    private SimpleInstance createdInstance;

    CreateSimpleInstanceCommand(FrameStore delegate, FrameID id, Collection types, boolean loadDefaults) {
        super(delegate);
        this.id = id;
        this.types = new ArrayList(types);
        this.loadDefaults = loadDefaults;
    }

    public Object doIt() {
        createdInstance = getDelegate().createSimpleInstance(id, types, loadDefaults);
        id = createdInstance.getFrameID();
        setDescription("Create instance " + getText(createdInstance) + " of type " + getText(types));
        return createdInstance;
    }

    public void undoIt() {
        getDelegate().deleteSimpleInstance(createdInstance);
        createdInstance.markDeleted(true);
    }

    public void redoIt() {
        getDelegate().createSimpleInstance(id, types, loadDefaults);
        createdInstance.markDeleted(false);
    }
}