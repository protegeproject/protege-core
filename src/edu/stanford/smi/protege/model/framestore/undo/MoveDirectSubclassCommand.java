package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class MoveDirectSubclassCommand extends AbstractCommand {
    private Cls cls;
    private int index;
    private Cls subclass;
    private int oldIndex;

    MoveDirectSubclassCommand(FrameStore delegate, Cls cls, int index, Cls subclass) {
        super(delegate);
        this.cls = cls;
        this.index = index;
        this.subclass = subclass;
        oldIndex = getDelegate().getDirectSubclasses(cls).indexOf(subclass);
        setDescription("Move subclass " + getText(subclass) + " of class " + getText(cls) + " to index " + index);
    }

    public Object doIt() {
        getDelegate().moveDirectSubclass(cls, subclass, index);
        return null;
    }

    public void undoIt() {
        getDelegate().moveDirectSubclass(cls, subclass, oldIndex);
    }

    public void redoIt() {
        getDelegate().moveDirectSubclass(cls, subclass, index);
    }
}
