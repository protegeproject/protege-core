package edu.stanford.smi.protege.model.framestore;

import java.lang.reflect.*;

public class ReadOnlyFrameStoreHandler extends AbstractFrameStoreInvocationHandler {

    public Object handleInvoke(Method method, Object[] args) {
        if (isModification(method)) {
            throw new ModificationException(method.getName());
        }
        return invoke(method, args);
    }
}
/*
    public Slot createSlot(String name, Collection directTypes, Collection directSuperslots, Session session) throws Exception {
        throw new ModificationException("createSlot");
    }

    public Facet createFacet(String name, Collection directTypes, Session session) throws Exception {
        throw new ModificationException("createFacet");
    }

    public Cls createCls(String name, Collection directTypes, Collection directSuperclasses, Session session) throws Exception {
        throw new ModificationException("createCls");
    }

    public SimpleInstance createSimpleInstance(String name, Collection directTypes, Session session) throws Exception {
        throw new ModificationException("createSimpleInstance");
    }

    public void deleteFrame(Frame frame, Session session) throws Exception {
        throw new ModificationException("deleteFrame");
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values, Session session) throws Exception  {
        throw new ModificationException("setDirectOwnSlotValues");
    }

    public void addDirectSuperclass(Cls cls, Cls superclass, Session session) throws Exception {
        throw new ModificationException("addDirectSubclass");
    }
    public void removeDirectSuperclass(Cls cls, Cls superclass, Session session) throws Exception {
        throw new ModificationException("removeDirectSubclass");
    }
    public void moveDirectSubclass(Cls cls, Cls subclass, int index, Session session) throws Exception {
        throw new ModificationException("moveDirectSubclass");
    }
    public void addDirectSuperslot(Slot slot, Slot superslot, Session session) throws Exception {
        throw new ModificationException("addDirectSuperslot");
    }
    public void removeDirectSuperslot(Slot slot, Slot superslot, Session session) throws Exception {
        throw new ModificationException("removeDirectSubslot");
    }
    public void addDirectType(Instance instance, Cls type, Session session) throws Exception {
        throw new ModificationException("addDirectInstance");
    }
    public void removeDirectType(Instance instance, Cls type, Session session) throws Exception {
        throw new ModificationException("removeDirectInstance");
    }
    public void addDirectTemplateSlot(Cls cls, Slot slot, Session session) throws Exception {
        throw new ModificationException("addDirectTemplateSlot");
    }
    public void removeDirectTemplateSlot(Cls cls, Slot slot, Session session) throws Exception {
        throw new ModificationException("removeDirectTemplateSlot");
    }
    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index, Session session) throws Exception {
        throw new ModificationException("moveDirectTemplateSlot");
    }
    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values, Session session) throws Exception {
        throw new ModificationException("setDirectTemplateSlotValues");
    }
    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values, Session session) throws Exception{
        throw new ModificationException("setDirectTemplateFacetValues");
    }
    public void setFrameName(Frame frame, String name, Session session) throws Exception {
        throw new ModificationException("setFrameName");
    }
    public void setAssociatedFacet(Slot slot, Facet facet, Session session) throws Exception {
        throw new ModificationException("setAssociatedFacet");
    }
    public void setInverseSlot(Slot slota, Slot slotb, Session session) throws Exception {
        throw new ModificationException("setInverseSlot");
    }
}
*/
