package edu.stanford.smi.protege.model.framestore;

import edu.stanford.smi.protege.model.*;

class Fsft {
    private Frame frame;
    private Slot slot;
    private Facet facet;
    private boolean isTemplate;

    public Fsft(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        set(frame, slot, facet, isTemplate);
    }

    public Frame getFrame() {
        return frame;
    }

    public Slot getSlot() {
        return slot;
    }

    public Facet getFacet() {
        return facet;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void set(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        this.frame = frame;
        this.slot = slot;
        this.facet = facet;
        this.isTemplate = isTemplate;
    }
}
