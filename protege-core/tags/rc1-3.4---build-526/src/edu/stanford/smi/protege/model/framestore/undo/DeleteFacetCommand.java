package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class DeleteFacetCommand extends SimpleCommand {
    private Facet facet;
    private FrameID id;
    private Collection directTypes;

    DeleteFacetCommand(FrameStore delegate, Facet facet) {
        super(delegate);
        this.facet = facet;
        this.id = facet.getFrameID();
        this.directTypes = new ArrayList(facet.getDirectTypes());
        setDescription("Delete facet " + getText(facet));
    }

    public Object doIt() {
        getDelegate().deleteFacet(facet);
        facet.markDeleted(true);
        return null;
    }

    public void undoIt() {
        getDelegate().createFacet(id, directTypes, false);
        facet.markDeleted(false);
    }
}