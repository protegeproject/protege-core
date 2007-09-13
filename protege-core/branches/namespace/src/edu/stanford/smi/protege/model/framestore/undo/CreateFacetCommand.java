package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class CreateFacetCommand extends AbstractCommand {
    private boolean loadDefaults;
    private FrameID id;
    private Collection types;
    private Facet createdFacet;

    CreateFacetCommand(FrameStore delegate, FrameID id, Collection types, boolean loadDefaults) {
        super(delegate);
        this.id = id;
        this.types = new ArrayList(types);
        this.loadDefaults = loadDefaults;
    }

    public Object doIt() {
        createdFacet = getDelegate().createFacet(id, types, loadDefaults);
        id = createdFacet.getFrameID();
        setDescription("Create facet " + getText(createdFacet));
        return createdFacet;
    }

    public void undoIt() {
        getDelegate().deleteFacet(createdFacet);
        createdFacet.markDeleted(true);
    }

    public void redoIt() {
        getDelegate().createFacet(id, types, loadDefaults);
        createdFacet.markDeleted(false);
    }
}