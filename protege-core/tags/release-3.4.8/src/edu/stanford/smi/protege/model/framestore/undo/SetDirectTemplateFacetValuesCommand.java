package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class SetDirectTemplateFacetValuesCommand extends AbstractCommand {
    private Slot slot;
    private Cls cls;
    private Facet facet;
    private Collection values;
    private List _oldValues;

    SetDirectTemplateFacetValuesCommand(FrameStore delegate, Slot slot, Cls cls, Facet facet, Collection values) {
        super(delegate);
        this.slot = slot;
        this.cls = cls;
        this.facet = facet;
        this.values = new ArrayList(values);
        String description = "Set template facet " + getText(facet) + " at class " + getText(cls) + " and slot "
                + getText(slot) + " to values " + getText(values);
        setDescription(description);
    }

    public Object doIt() {
        _oldValues = getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
        return null;
    }

    public void undoIt() {
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, _oldValues);
    }

    public void redoIt() {
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
    }
}