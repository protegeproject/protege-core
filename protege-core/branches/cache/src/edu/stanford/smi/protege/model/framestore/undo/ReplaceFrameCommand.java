package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.framestore.FrameStore;

public class ReplaceFrameCommand extends SimpleCommand {
    private Frame original;
    private Frame replacement;
    
    public ReplaceFrameCommand(FrameStore delegate, Frame original, Frame replacement) {
        super(delegate);
        this.original = original;
        this.replacement  = replacement;
    }

    public Object doIt() {
        getDelegate().replaceFrame(original, replacement);
        return null;
    }

    public void undoIt() {
        getDelegate().replaceFrame(replacement, original);
    }



}
