package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class DeleteSimpleInstanceCommand extends SimpleCommand {
    private SimpleInstance simpleInstance;
    private FrameID id;
    private Collection directTypes;

    DeleteSimpleInstanceCommand(FrameStore delegate, SimpleInstance simpleInstance) {
        super(delegate);
        this.simpleInstance = simpleInstance;
        this.id = simpleInstance.getFrameID();
        this.directTypes = new ArrayList(simpleInstance.getDirectTypes());
        setDescription("Delete instance " + getText(simpleInstance));
    }

    public Object doIt() {
        getDelegate().deleteSimpleInstance(simpleInstance);
        simpleInstance.markDeleted(true);
        return null;
    }

    public void undoIt() {
        getDelegate().createSimpleInstance(id, directTypes, false);
        simpleInstance.markDeleted(false);
    }
}