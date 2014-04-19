package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class MoveDirectTypeCommand extends AbstractCommand {
    private Instance instance;
    private int index;
    private Cls type;
    private int oldIndex;

    MoveDirectTypeCommand(FrameStore delegate, Instance instance, Cls type, int index) {
        super(delegate);
        this.instance = instance;
        this.index = index;
        this.type = type;
        oldIndex = getDelegate().getDirectTypes(instance).indexOf(type);
        setDescription("Move type " + getText(type) + " of instance " + getText(instance) + " to index " + index);
    }

    public Object doIt() {
        getDelegate().moveDirectType(instance, type, index);
        return null;
    }

    public void undoIt() {
        getDelegate().moveDirectType(instance, type, oldIndex);
    }

    public void redoIt() {
        getDelegate().moveDirectType(instance, type, index);
    }
}
