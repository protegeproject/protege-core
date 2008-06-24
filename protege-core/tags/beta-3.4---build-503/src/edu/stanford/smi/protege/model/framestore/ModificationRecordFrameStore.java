package edu.stanford.smi.protege.model.framestore;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ModificationRecordFrameStore extends ModificationFrameStore {
    private Collection _authors = Collections.singleton(ApplicationProperties.getUserName());
    private Slot _creationAuthorSlot;
    private Slot _creationTimestampSlot;
    private Slot _modificationAuthorSlot;
    private Slot _modificationTimestampSlot;

    public void setAuthor(String name) {
        _authors = Collections.singleton(name);
    }

    public void reinitialize() {
        // do nothing
    }

    public void close() {
        _authors = null;
        _creationAuthorSlot = null;
        _creationTimestampSlot = null;
        _modificationAuthorSlot = null;
        _modificationTimestampSlot = null;
    }

    public void onSetDelegate() {
        FrameStore delegate = getDelegate();
        if (delegate != null) {
            _creationAuthorSlot = (Slot) delegate.getFrame(Model.Slot.CREATOR);
            _creationTimestampSlot = (Slot) delegate.getFrame(Model.Slot.CREATION_TIMESTAMP);
            _modificationAuthorSlot = (Slot) delegate.getFrame(Model.Slot.MODIFIER);
            _modificationTimestampSlot = (Slot) delegate.getFrame(Model.Slot.MODIFICATION_TIMESTAMP);
        }
    }

    private void updateCreationRecord(Frame frame) {
        updateFrameRecord(frame, _creationAuthorSlot, _creationTimestampSlot);
    }

    private void updateModificationRecord(Frame frame) {
        updateFrameRecord(frame, _modificationAuthorSlot, _modificationTimestampSlot);
    }

    private void updateDeletionRecord(Frame frame) {

    }

    private void updateFrameRecord(Frame frame, Slot authorSlot, Slot timestampSlot) {
        if (frame.hasOwnSlot(authorSlot)) {
            getDelegate().setDirectOwnSlotValues(frame, authorSlot, _authors);
        }
        if (frame.hasOwnSlot(timestampSlot)) {
            String timestamp = new StandardDateFormat().format(new Date());
            Collection timestamps = Collections.singleton(timestamp);
            getDelegate().setDirectOwnSlotValues(frame, timestampSlot, timestamps);
        }
    }

    public Cls createCls(FrameID id, Collection types, Collection superclasses, boolean loadDefaults) {
        Cls cls = getDelegate().createCls(id, types, superclasses, loadDefaults);
        updateCreationRecord(cls);
        return cls;
    }

    public Slot createSlot(FrameID id, Collection types, Collection superslots, boolean loadDefaults) {
        Slot slot = getDelegate().createSlot(id, types, superslots, loadDefaults);
        updateCreationRecord(slot);
        return slot;
    }

    public Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues) {
        Facet facet = getDelegate().createFacet(id, directTypes, loadDefaultValues);
        updateCreationRecord(facet);
        return facet;
    }

    public SimpleInstance createSimpleInstance(FrameID id, Collection types, boolean loadDefaultValues) {
        SimpleInstance simpleInstance = getDelegate().createSimpleInstance(id, types, loadDefaultValues);
        updateCreationRecord(simpleInstance);
        return simpleInstance;
    }

    public void deleteCls(Cls cls) {
        updateDeletionRecord(cls);
        getDelegate().deleteCls(cls);
    }

    public void deleteSlot(Slot slot) {
        updateDeletionRecord(slot);
        getDelegate().deleteSlot(slot);
    }

    public void deleteFacet(Facet facet) {
        updateDeletionRecord(facet);
        getDelegate().deleteFacet(facet);
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        updateDeletionRecord(simpleInstance);
        getDelegate().deleteSimpleInstance(simpleInstance);
    }

    public void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to) {
        getDelegate().moveDirectOwnSlotValue(frame, slot, from, to);
        updateModificationRecord(frame);
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
        updateModificationRecord(frame);
    }

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        getDelegate().addDirectTemplateSlot(cls, slot);
        updateModificationRecord(cls);
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        getDelegate().removeDirectTemplateSlot(cls, slot);
        updateModificationRecord(cls);
    }

    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
        updateModificationRecord(cls);
    }

    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
        updateModificationRecord(cls);
    }

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
        updateModificationRecord(cls);
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
        updateModificationRecord(cls);
    }

    public void addDirectSuperclass(Cls cls, Cls superclass) {
        getDelegate().addDirectSuperclass(cls, superclass);
        updateModificationRecord(cls);
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        getDelegate().removeDirectSuperclass(cls, superclass);
        updateModificationRecord(cls);
    }

    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        getDelegate().moveDirectSubclass(cls, subclass, index);
        updateModificationRecord(cls);
    }

    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        getDelegate().moveDirectSubslot(slot, subslot, index);
        updateModificationRecord(slot);
    }

    public void addDirectSuperslot(Slot slot, Slot superslot) {
        getDelegate().addDirectSuperslot(slot, superslot);
        updateModificationRecord(slot);
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        getDelegate().removeDirectSuperslot(slot, superslot);
        updateModificationRecord(slot);
    }

    public void addDirectType(Instance instance, Cls type) {
        getDelegate().addDirectType(instance, type);
        updateModificationRecord(instance);
    }

    public void removeDirectType(Instance instance, Cls type) {
        getDelegate().removeDirectType(instance, type);
        updateModificationRecord(instance);
    }

    public void moveDirectType(Instance instance, Cls type, int index) {
        getDelegate().moveDirectType(instance, type, index);
        updateModificationRecord(instance);
    }

    public boolean beginTransaction(String name) {
        return getDelegate().beginTransaction(name);
    }

    public boolean commitTransaction() {
        return getDelegate().commitTransaction();
    }

    public boolean rollbackTransaction() {
        return getDelegate().rollbackTransaction();
    }

    public void replaceFrame(Frame original, Frame replacement) {
      getDelegate().replaceFrame(original, replacement);
      updateDeletionRecord(original);
      updateCreationRecord(replacement);
    }

}