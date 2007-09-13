package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class DeleteClsCommand extends SimpleCommand {
    private Cls cls;
    private FrameID id;
    private Collection directTypes;
    private Collection directSuperclasses;

    DeleteClsCommand(FrameStore delegate, Cls cls) {
        super(delegate);
        this.cls = cls;
        this.id = cls.getFrameID();
        this.directTypes = new ArrayList(cls.getDirectTypes());
        this.directSuperclasses = new ArrayList(cls.getDirectSuperclasses());
        setDescription("Delete class " + getText(cls));
        // Log.enter(this, "DeleteClsCommand", cls);
    }

    public Object doIt() {
        // Log.enter(this, "doIt", name);
        getDelegate().deleteCls(cls);
        cls.markDeleted(true);
        return null;
    }
    public void undoIt() {
        // Log.enter(this, "undoIt", name);
        getDelegate().createCls(id, directTypes, directSuperclasses, false);
        cls.markDeleted(false);
    }
}