package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class AddDirectTypeCommand extends SimpleCommand {
    private Cls type;
    private Instance instance;

    AddDirectTypeCommand(FrameStore delegate, Cls type, Instance instance) {
        super(delegate);
        this.type = type;
        this.instance = instance;
        setDescription("Add type " + getText(type) + " to instance " + getText(instance));
    }

    public Object doIt() {
        getDelegate().addDirectType(instance, type);
        return null;
    }

    public void undoIt() {
        getDelegate().removeDirectType(instance, type);
    }
}