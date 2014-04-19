package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.framestore.*;

abstract class SimpleCommand extends AbstractCommand {

    protected SimpleCommand(FrameStore delegate) {
        super(delegate);
    }

    public void redoIt() {
        doIt();
    }
}
