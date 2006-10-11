package edu.stanford.smi.protege.model.framestore.undo;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class RemoveDirectTemplateFacetOverridesCommand extends AbstractCommand {
    private Cls cls;
    private Slot slot;

    RemoveDirectTemplateFacetOverridesCommand(FrameStore delegate, Cls cls, Slot slot) {
        super(delegate);
        this.cls = cls;
        this.slot = slot;
        setDescription("Remove facet overrides from slot " + getText(slot) + " at class " + getText(cls));
    }

    public Object doIt() {
        // TODO should save overrides
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
        return null;
    }

    public void undoIt() {
        // TODO should restore overrides
    }

    public void redoIt() {
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
    }
}