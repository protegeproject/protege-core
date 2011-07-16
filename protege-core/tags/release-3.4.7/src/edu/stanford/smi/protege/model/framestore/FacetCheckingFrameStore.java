package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FacetCheckingFrameStore extends FrameStoreAdapter {
    public static class AbstractClassException extends IllegalArgumentException {

        private static final long serialVersionUID = 6154760101118221711L;
    }

    public static class FacetException extends IllegalArgumentException {

        private static final long serialVersionUID = -2526457416740648312L;
    }

    private static void checkAbstract(Collection types) {
        Iterator i = types.iterator();
        while (i.hasNext()) {
            Cls cls = (Cls) i.next();
            if (cls.isAbstract()) {
                throw new AbstractClassException();
            }
        }
    }

    public Cls createCls(FrameID id, Collection types, Collection superclasses, boolean loadDefaults) {
        checkAbstract(types);
        return getDelegate().createCls(id, types, superclasses, loadDefaults);
    }

    public Slot createSlot(FrameID id, Collection types, Collection superslots, boolean loadDefaults) {
        checkAbstract(types);
        return getDelegate().createSlot(id, types, superslots, loadDefaults);
    }

    public Facet createFacet(FrameID id, Collection types, boolean loadDefaults) {
        checkAbstract(types);
        return getDelegate().createFacet(id, types, loadDefaults);
    }

    public SimpleInstance createSimpleInstance(FrameID id, Collection types, boolean loadDefaults) {
        checkAbstract(types);
        return getDelegate().createSimpleInstance(id, types, loadDefaults);
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        checkOwnSlotValues(frame, slot, values);
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
    }

    private void checkOwnSlotValues(Frame frame, Slot slot, Collection values) {
        Iterator i = getOwnFacets(frame, slot).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            if (!facet.areValidValues(frame, slot, values)) {
                throw new FacetException();
            }
        }
    }

    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        checkTemplateSlotValues(cls, slot, values);
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
    }

    private void checkTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        Iterator i = getTemplateFacets(cls, slot).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            if (!facet.areValidValues(cls, slot, values)) {
                throw new FacetException();
            }
        }
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        checkTemplateFacetValues(cls, slot, facet, values);
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
    }

    private void checkTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        Slot associatedSlot = facet.getAssociatedSlot();
        if (associatedSlot == null) {
            throw new FacetException();
        }
        checkOwnSlotValues(slot, associatedSlot, values);
    }
}
