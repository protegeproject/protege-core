package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class AddDirectSuperclassCommand extends SimpleCommand {
    private Cls superclass;
    private Cls cls;

    AddDirectSuperclassCommand(FrameStore delegate, Cls superclass, Cls cls) {
        super(delegate);
        this.superclass = superclass;
        this.cls = cls;
        setDescription("Add superclass " + getText(superclass) + " to class " + getText(cls));
    }

    public Object doIt() {
        getDelegate().addDirectSuperclass(cls, superclass);
        return null;
    }

    public void undoIt() {
        getDelegate().removeDirectSuperclass(cls, superclass);
    }
}