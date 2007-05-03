package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class SetFrameNameCommand extends AbstractCommand {
    private String name;
    private Frame frame;
    private String oldName;

    SetFrameNameCommand(FrameStore delegate, String name, Frame frame) {
        super(delegate);
        this.name = name;
        this.frame = frame;
        oldName = getDelegate().getFrameName(frame);
        setDescription("Change frame name from " + oldName + " to " + name);
    }

    public Object doIt() {
        getDelegate().setFrameName(frame, name);
        return null;
    }

    public void undoIt() {
        getDelegate().setFrameName(frame, oldName);
    }

    public void redoIt() {
        getDelegate().setFrameName(frame, name);
    }
}