package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class RemoveDirectTypeCommand extends AbstractCommand {
    private Cls type;
    private Instance instance;

    RemoveDirectTypeCommand(FrameStore delegate, Cls type, Instance instance) {
        super(delegate);
        this.type = type;
        this.instance = instance;
        setDescription("Remove type " + getText(type) + " from instance " + getText(instance));
    }

    public Object doIt() {
        // TODO should save own slot values for slots which get lost
        getDelegate().removeDirectType(instance, type);
        return null;
    }

    public void undoIt() {
        // TODO should restore own slot values for slots which get lost
        getDelegate().addDirectType(instance, type);
    }

    public void redoIt() {
        getDelegate().removeDirectType(instance, type);
    }
}