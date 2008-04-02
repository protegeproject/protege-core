package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class CreateSimpleInstanceCommand extends AbstractCommand {
    private FrameID id;
    private String name;
    private Collection types;
    private boolean loadDefaults;
    private SimpleInstance createdInstance;

    public CreateSimpleInstanceCommand(
        FrameStore delegate,
        FrameID id,
        String name,
        Collection types,
        boolean loadDefaults) {
        super(delegate);
        this.id = id;
        this.name = name;
        this.types = types;
        this.loadDefaults = loadDefaults;
    }

    public Object doIt() {
        createdInstance = getDelegate().createSimpleInstance(id, name, types, loadDefaults);
        name = getDelegate().getFrameName(createdInstance);
        id = createdInstance.getFrameID();
        setDescription("Create instance " + getText(createdInstance) + " of type " + getText(types));
        return createdInstance;
    }
    public void undoIt() {
        getDelegate().deleteSimpleInstance(createdInstance);
        createdInstance.markDeleted(true);
    }
    public void redoIt() {
        getDelegate().createSimpleInstance(id, name, types, loadDefaults);
        createdInstance.markDeleted(false);
    }
}