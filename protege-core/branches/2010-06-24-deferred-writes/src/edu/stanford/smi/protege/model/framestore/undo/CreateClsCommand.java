package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class CreateClsCommand extends AbstractCommand {
    private Cls createdCls;
    private FrameID id;
    private Collection types;
    private Collection superclasses;
    private boolean loadDefaults;

    CreateClsCommand(FrameStore delegate, FrameID id, Collection types, Collection superclasses,
            boolean loadDefaults) {
        super(delegate);
        this.id = id;
        this.types = new ArrayList(types);
        this.superclasses = new ArrayList(superclasses);
        this.loadDefaults = loadDefaults;
    }

    public Object doIt() {
        createdCls = getDelegate().createCls(id, types, superclasses, loadDefaults);
        id = createdCls.getFrameID();
        setDescription("Create class " + getText(createdCls));
        return createdCls;
    }

    public void undoIt() {
        getDelegate().deleteCls(createdCls);
        createdCls.markDeleted(true);
    }

    public void redoIt() {
        getDelegate().createCls(id, types, superclasses, loadDefaults);
        createdCls.markDeleted(false);
    }
}