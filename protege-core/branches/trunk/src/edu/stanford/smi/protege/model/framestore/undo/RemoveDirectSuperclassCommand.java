package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class RemoveDirectSuperclassCommand extends AbstractCommand {
    private Cls superclass;
    private Cls cls;

    RemoveDirectSuperclassCommand(FrameStore delegate, Cls superclass, Cls cls) {
        super(delegate);
        this.superclass = superclass;
        this.cls = cls;
        setDescription("Remove superclass " + getText(superclass) + " from class " + getText(cls));
    }

    public Object doIt() {
        // TODO should remove own slot values from instances of subclasses
        getDelegate().removeDirectSuperclass(cls, superclass);
        return null;
    }

    public void undoIt() {
        // TODO should restore own slot values from instances of subclasses
        getDelegate().addDirectSuperclass(cls, superclass);
    }

    public void redoIt() {
        getDelegate().removeDirectSuperclass(cls, superclass);
    }
}