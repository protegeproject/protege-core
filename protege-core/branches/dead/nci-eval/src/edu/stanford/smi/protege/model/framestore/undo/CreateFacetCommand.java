package edu.stanford.smi.protege.model.framestore.undo;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

class CreateFacetCommand extends AbstractCommand {
    private boolean loadDefaults;
    private String name;
    private FrameID id;
    private Collection types;
    private Facet createdFacet;

    public CreateFacetCommand(FrameStore delegate, FrameID id, String name, Collection types, boolean loadDefaults) {
        super(delegate);
        this.id = id;
        this.name = name;
        this.types = types;
        this.loadDefaults = loadDefaults;
    }

    public Object doIt() {
        createdFacet = getDelegate().createFacet(id, name, types, loadDefaults);
        id = createdFacet.getFrameID();
        name = getDelegate().getFrameName(createdFacet);
        setDescription("Create facet " + getText(createdFacet));
        return createdFacet;
    }
    public void undoIt() {
        getDelegate().deleteFacet(createdFacet);
        createdFacet.markDeleted(true);
    }
    public void redoIt() {
        getDelegate().createFacet(id, name, types, loadDefaults);
        createdFacet.markDeleted(false);
    }
}