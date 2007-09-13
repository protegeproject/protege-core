package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.InstanceEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.SlotEvent;
import edu.stanford.smi.protege.event.TransactionEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.SystemFrames;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public class EventGeneratorFrameStore extends ModificationFrameStore {
    private List _events = new ArrayList<AbstractEvent>();
    private static final int NO_VALUE = -1;
    private int _transactionStartSize = NO_VALUE;
    private DefaultKnowledgeBase _kb;
    private SystemFrames _systemFrames;
    private boolean generateEventsOnDeletingFrames = false;
    private boolean serverMode = false;

    public EventGeneratorFrameStore(KnowledgeBase kb) {
        _kb = (DefaultKnowledgeBase) kb;
        _systemFrames = _kb.getSystemFrames();
    }
    
    public void serverMode() {
      serverMode = true;
    }

    public void reinitialize() {
        _events.clear();
        _transactionStartSize = NO_VALUE;
    }

    public void close() {
        super.close();
        _events.clear();
        _events = null;
        _kb = null;
        _systemFrames = null;
    }

    public Frame getFrame(String name) {
        return getDelegate().getFrame(name);
    }

    public Cls createCls(FrameID id, Collection directTypes, Collection superClasses, boolean loadDefaults) {
        Cls cls = getDelegate().createCls(id, directTypes, superClasses, loadDefaults);
        generateCreateClsEvents(cls, directTypes);
        return cls;
    }

    public Slot createSlot(FrameID id, Collection directTypes, Collection superslots, boolean loadDefaults) {
        Slot slot = getDelegate().createSlot(id, directTypes, superslots, loadDefaults);
        generateCreateSlotEvents(slot, directTypes);
        return slot;
    }

    public Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaults) {
        Facet facet = getDelegate().createFacet(id, directTypes, loadDefaults);
        generateCreateFacetEvents(facet, directTypes);
        return facet;
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        generateSetDirectOwnSlotValuesEvents(frame, slot, values);
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
    }

    private void generateSetFrameNameEvents(Frame frame, String oldName, String newName) {
        generateFrameEvent(FrameEvent.NAME_CHANGED, frame, oldName);
        generateFrameEvent(FrameEvent.BROWSER_TEXT_CHANGED, frame);
        generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, frame, _systemFrames.getNameSlot());
        generateKbEvent(KnowledgeBaseEvent.FRAME_NAME_CHANGED, frame, oldName);

    }

    private void generateSetDirectOwnSlotValuesEvents(Frame frame, Slot slot, Collection values) {
        generateOwnSlotValuesChangedEvent(frame, slot);
        generateFrameEvent(FrameEvent.BROWSER_TEXT_CHANGED, frame);
        Slot inverseSlot = (Slot) CollectionUtilities.getFirstItem(getDirectOwnSlotValues(slot, _systemFrames
                .getInverseSlotSlot()));
        if (inverseSlot != null) {
            Collection oldValues = getDirectOwnSlotValues(frame, slot);
            // back references from new values
            Iterator i = values.iterator();
            while (i.hasNext()) {
                Frame newValue = (Frame) i.next();
                if (!oldValues.contains(newValue)) {
                    generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, newValue, inverseSlot);
                    // if (cardinalitySingle) {
                    // current references to new values
                    // }
                }
            }
            // back references from old values
            i = oldValues.iterator();
            while (i.hasNext()) {
                Frame oldValue = (Frame) i.next();
                if (!values.contains(oldValue)) {
                    generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, oldValue, inverseSlot);
                }
            }
        }
    }

    private void generateDeleteFrameKbEvent(int type, Frame frame) {
        generateKbEvent(type, frame, frame.getName());
    }

    private void generateDeleteClsEvents(Cls cls) {
        // generateDeleteSubclassEvents(cls);
        generateDeleteFrameKbEvent(KnowledgeBaseEvent.CLS_DELETED, cls);
        Iterator i = getDirectSuperclasses(cls).iterator();
        while (i.hasNext()) {
            Cls superCls = (Cls) i.next();
            generateClsEvent(ClsEvent.DIRECT_SUBCLASS_REMOVED, superCls, cls);
        }
        generateDeleteInstanceEvents(cls);
    }

    /*
     * TODO We should actually generate some subclass removed events but not all. The problem is that it is problematic
     * to generate subclass removed events on superclasses that have also been deleted. Receivers of these events are
     * going to get the "changed" event first and process it when in fact the superclass has also been deleted but they
     * haven't gotten that event yet. This will lead them to make calls with deleted frames have have trouble.
     * 
     * It would seem that the only way around this is to change the definition of "delete class" so that it only works
     * on leaf classes (just as it only works on classes with no instances at the moment). This would probably be
     * cleaner and would simplify the interface. The differences could be handled at either the KB or the
     * FrameStoreManager level.
     * 
     * For now though we hack it...
     */
    /*
     * private void generateDeleteSubclassEvents(Cls cls) { Iterator i = SimpleFrameStore.getClsesToBeDeleted(cls,
     * this).iterator(); while (i.hasNext()) { Cls subclass = (Cls) i.next(); if (!subclass.equals(cls)) {
     * generateLimitedDeleteEventsForCls(subclass); } } }
     * 
     * private void generateLimitedDeleteEventsForCls(Cls cls) {
     * generateDeleteFrameKbEvent(KnowledgeBaseEvent.CLS_DELETED, cls); generateDeleteInstanceEvents(cls); }
     */

    private void generateDeleteSimpleInstanceEvents(SimpleInstance simpleInstance) {
        generateDeleteFrameKbEvent(KnowledgeBaseEvent.INSTANCE_DELETED, simpleInstance);
        generateDeleteInstanceEvents(simpleInstance);
    }

    private void generateDeleteSlotEvents(Slot slot) {
        generateDeleteFrameKbEvent(KnowledgeBaseEvent.SLOT_DELETED, slot);
        /** @todo other slot events */
        generateDeleteInstanceEvents(slot);
    }

    private void generateDeleteFacetEvents(Facet facet) {
        generateDeleteFrameKbEvent(KnowledgeBaseEvent.FACET_DELETED, facet);
        /** @todo other facet events */
        generateDeleteInstanceEvents(facet);
    }

    private void generateDeleteInstanceEvents(Instance instance) {
        generateRemoveDirectInstanceEvents(instance);
        Iterator i = getReferences(instance).iterator();
        while (i.hasNext()) {
            Reference ref = (Reference) i.next();
            removeReference(ref, instance);
        }
        generateFrameEvent(FrameEvent.DELETED, instance, instance.getName());
    }

    private void generateRemoveDirectInstanceEvents(Instance instance) {
        Iterator i = getDirectTypes(instance).iterator();
        while (i.hasNext()) {
            Cls type = (Cls) i.next();
            generateClsEvent(ClsEvent.DIRECT_INSTANCE_REMOVED, type, instance);
        }
    }

    private void removeReference(Reference ref, Instance instance) {
        Frame frame = ref.getFrame();
        Slot slot = ref.getSlot();
        Facet facet = ref.getFacet();
        boolean isTemplate = ref.isTemplate();
        removeReference(frame, slot, facet, isTemplate, instance);
    }

    private void removeReference(Frame frame, Slot slot, Facet facet, boolean isTemplate, Instance instance) {
        if (facet == null) {
            if (isTemplate) {
                removeTemplateSlotValueReference(frame, slot, instance);
            } else {
                removeOwnSlotValueReference(frame, slot, instance);
            }
        } else {
            if (isTemplate) {
                removeTemplateFacetValueReference(frame, slot, facet, instance);
            } else {
                // ???
            }
        }
    }

    private void removeOwnSlotValueReference(Frame frame, Slot slot, Instance instance) {
        generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, frame, slot);
    }

    private void removeTemplateSlotValueReference(Frame frame, Slot slot, Instance instance) {
        // TODO
    }

    private void removeTemplateFacetValueReference(Frame frame, Slot slot, Facet facet, Instance instance) {
        // TODO
    }

    public void deleteCls(Cls cls) {
        generateDeleteClsEvents(cls);
        getDelegate().deleteCls(cls);
    }

    public void deleteSlot(Slot slot) {
        generateDeleteSlotEvents(slot);
        getDelegate().deleteSlot(slot);
    }

    public void deleteFacet(Facet facet) {
        generateDeleteFacetEvents(facet);
        getDelegate().deleteFacet(facet);
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        generateDeleteSimpleInstanceEvents(simpleInstance);
        getDelegate().deleteSimpleInstance(simpleInstance);
    }

    public SimpleInstance createSimpleInstance(FrameID id, Collection directTypes, boolean loadDefaults) {
        SimpleInstance simpleInstance = getDelegate().createSimpleInstance(id,directTypes, loadDefaults);
        generateCreateSimpleInstanceEvents(simpleInstance, directTypes);
        return simpleInstance;
    }

    private void generateCreateClsEvents(Cls newCls, Collection directTypes) {
        generateCreateInstanceEvents(KnowledgeBaseEvent.CLS_CREATED, newCls, directTypes);
        Iterator i = newCls.getDirectSuperclasses().iterator();
        while (i.hasNext()) {
            Cls superclass = (Cls) i.next();
            generateClsEvent(ClsEvent.DIRECT_SUBCLASS_ADDED, superclass, newCls);
            generateOwnSlotValuesChangedEvent(superclass, _systemFrames.getDirectSubclassesSlot());
        }
    }

    private void generateCreateFacetEvents(Facet newFacet, Collection directTypes) {
        generateCreateInstanceEvents(KnowledgeBaseEvent.FACET_CREATED, newFacet, directTypes);
    }

    private void generateCreateSlotEvents(Slot newSlot, Collection directTypes) {
        generateCreateInstanceEvents(KnowledgeBaseEvent.SLOT_CREATED, newSlot, directTypes);
        Iterator i = newSlot.getDirectSuperslots().iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            generateSlotEvent(SlotEvent.DIRECT_SUBSLOT_ADDED, superslot, newSlot);
            generateOwnSlotValuesChangedEvent(superslot, _systemFrames.getDirectSubslotsSlot());
        }
    }

    private void generateCreateSimpleInstanceEvents(SimpleInstance newFrame, Collection directTypes) {
        generateCreateInstanceEvents(KnowledgeBaseEvent.INSTANCE_CREATED, newFrame, directTypes);
    }

    private void generateCreateInstanceEvents(int type, Frame newFrame, Collection directTypes) {
        generateKbEvent(type, newFrame);
        Iterator i = directTypes.iterator();
        while (i.hasNext()) {
            Cls directType = (Cls) i.next();
            generateClsEvent(ClsEvent.DIRECT_INSTANCE_ADDED, directType, newFrame);
            generateOwnSlotValuesChangedEvent(directType, _systemFrames.getDirectInstancesSlot());
        }
    }

    private void generateKbEvent(int type, Frame frame) {
        _events.add(new KnowledgeBaseEvent(_kb, type, frame));
    }

    private void generateKbEvent(int type, Frame frame, String s) {
        _events.add(new KnowledgeBaseEvent(_kb, type, frame, s));
    }

    private boolean generateEvent(Frame frame) {
        return !frame.isBeingDeleted() || generateEventsOnDeletingFrames;
    }

    private void generateFrameEvent(int type, Frame frame) {
        generateFrameEvent(type, frame, null, null);
    }

    private void generateFrameEvent(int type, Frame frame, Object o2) {
        generateFrameEvent(type, frame, o2, null);
    }

    private void generateFrameEvent(int type, Frame frame, Object o2, Object o3) {
        if (generateEvent(frame)) {
            _events.add(new FrameEvent(frame, type, o2, o3));
        }
    }

    private void generateClsEvent(int type, Cls cls, Frame frame1, Frame frame2) {
        if (generateEvent(cls)) {
            _events.add(new ClsEvent(cls, type, frame1, frame2));
        }
    }

    private void generateClsEvent(int type, Cls cls, Frame frame) {
        generateClsEvent(type, cls, frame, null);
    }

    private void generateSlotEvent(int type, Slot slot, Frame frame) {
        if (generateEvent(slot)) {
            _events.add(new SlotEvent(slot, type, frame));
        }
    }

    private void generateInstanceEvent(int type, Instance instance, Frame frame) {
        if (generateEvent(instance)) {
            _events.add(new InstanceEvent(instance, type, frame));
        }
    }

    public List<AbstractEvent> getEvents() {
        List events;
        if (!serverMode && isInTransaction()) {
            events = Collections.EMPTY_LIST;
        } else {
            events = _events;
            _events = new ArrayList<AbstractEvent>();
            return events;
        }
        return events;
    }

    private boolean isInTransaction() {
      if (serverMode) {
        throw new UnsupportedOperationException("can't determine transaction status here as a server");
      } else {
        return _transactionStartSize != NO_VALUE;
      }
    }

    public void addDirectSuperclass(Cls cls, Cls superclass) {
        Collection addedSlots = getSlotsToBeAdded(cls, superclass);
        getDelegate().addDirectSuperclass(cls, superclass);
        generateClsEvent(ClsEvent.DIRECT_SUPERCLASS_ADDED, cls, superclass);
        generateClsEvent(ClsEvent.DIRECT_SUBCLASS_ADDED, superclass, cls);
        generateSuperclassTemplateSlotChangedEvents(cls, ClsEvent.TEMPLATE_SLOT_ADDED, addedSlots);
        generateOwnSlotValuesChangedEvent(cls, superclass, _systemFrames.getDirectSuperclassesSlot());
    }

    private Collection getSlotsToBeAdded(Cls cls, Cls superclass) {
        Collection slots = new HashSet(getTemplateSlots(superclass));
        slots.removeAll(getTemplateSlots(cls));
        return slots;
    }

    private void generateSuperclassTemplateSlotChangedEvents(Cls cls, int type, Collection addedSlots) {
        Iterator i = addedSlots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            generateClsEvent(type, cls, slot);
        }
    }

    private void generateOwnSlotValuesChangedEvent(Frame frame1, Frame frame2, Slot slot) {
        generateOwnSlotValuesChangedEvent(frame1, slot);
        generateOwnSlotValuesChangedEvent(frame2, slot.getInverseSlot());
    }

    private void generateOwnSlotValuesChangedEvent(Frame frame, Slot slot) {
        Collection oldValues = null;
        if (getValues(frame, slot)) {
            oldValues = new ArrayList(getDirectOwnSlotValues(frame, slot));
        }
        generateOwnSlotValuesChangedEvent(frame, slot, oldValues);
    }

    private boolean getValues(Frame frame, Slot slot) {
        return !slot.equals(_systemFrames.getDirectSubclassesSlot())
                && !slot.equals(_systemFrames.getDirectInstancesSlot());
    }

    private void generateOwnSlotValuesChangedEvent(Frame frame, Slot slot, Collection oldValues) {
        generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, frame, slot, oldValues);
        Iterator i = slot.getSuperslots().iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, frame, superslot, oldValues);
        }
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
        HashSet removedSlots = new HashSet(cls.getTemplateSlots());
        getDelegate().removeDirectSuperclass(cls, superclass);
        removedSlots.removeAll(cls.getTemplateSlots());
        generateClsEvent(ClsEvent.DIRECT_SUPERCLASS_REMOVED, cls, superclass);
        generateClsEvent(ClsEvent.DIRECT_SUBCLASS_REMOVED, superclass, cls);
        generateSuperclassTemplateSlotChangedEvents(cls, ClsEvent.TEMPLATE_SLOT_REMOVED, removedSlots);
        generateOwnSlotValuesChangedEvent(cls, superclass, _systemFrames.getDirectSuperclassesSlot());
    }

    public void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to) {
        getDelegate().moveDirectOwnSlotValue(frame, slot, from, to);
        generateOwnSlotValuesChangedEvent(frame, slot);
    }

    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        getDelegate().moveDirectSubclass(cls, subclass, index);
        generateClsEvent(ClsEvent.DIRECT_SUBCLASS_MOVED, cls, subclass);
        generateOwnSlotValuesChangedEvent(cls, _systemFrames.getDirectSubclassesSlot());
    }

    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        getDelegate().moveDirectSubslot(slot, subslot, index);
        generateSlotEvent(SlotEvent.DIRECT_SUBSLOT_MOVED, slot, subslot);
        generateOwnSlotValuesChangedEvent(slot, _systemFrames.getDirectSubslotsSlot());
    }

    public void addDirectSuperslot(Slot slot, Slot superslot) {
        getDelegate().addDirectSuperslot(slot, superslot);
        generateSlotEvent(SlotEvent.DIRECT_SUPERSLOT_ADDED, slot, superslot);
        generateSlotEvent(SlotEvent.DIRECT_SUBSLOT_ADDED, superslot, slot);
        generateOwnSlotValuesChangedEvent(slot, superslot, _systemFrames.getDirectSuperslotsSlot());
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        getDelegate().removeDirectSuperslot(slot, superslot);
        generateSlotEvent(SlotEvent.DIRECT_SUPERSLOT_REMOVED, slot, superslot);
        generateSlotEvent(SlotEvent.DIRECT_SUBSLOT_REMOVED, superslot, slot);
        generateOwnSlotValuesChangedEvent(slot, superslot, _systemFrames.getDirectSuperslotsSlot());
    }

    public void addDirectType(Instance instance, Cls type) {
        getDelegate().addDirectType(instance, type);
        generateInstanceEvent(InstanceEvent.DIRECT_TYPE_ADDED, instance, type);
        generateClsEvent(ClsEvent.DIRECT_INSTANCE_ADDED, type, instance);
        generateOwnSlotValuesChangedEvent(instance, type, _systemFrames.getDirectTypesSlot());
    }

    public void removeDirectType(Instance instance, Cls type) {
        getDelegate().removeDirectType(instance, type);
        generateInstanceEvent(InstanceEvent.DIRECT_TYPE_REMOVED, instance, type);
        generateClsEvent(ClsEvent.DIRECT_INSTANCE_REMOVED, type, instance);
        generateOwnSlotValuesChangedEvent(instance, type, _systemFrames.getDirectTypesSlot());
    }

    public void moveDirectType(Instance instance, Cls cls, int index) {
        getDelegate().moveDirectType(instance, cls, index);
        generateOwnSlotValuesChangedEvent(instance, _systemFrames.getDirectTypesSlot());
    }

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        getDelegate().addDirectTemplateSlot(cls, slot);
        generateClsEvent(ClsEvent.TEMPLATE_SLOT_ADDED, cls, slot);
        generateSlotEvent(SlotEvent.TEMPLATE_SLOT_CLS_ADDED, slot, cls);
        generateOwnSlotValuesChangedEvent(cls, slot, _systemFrames.getDirectTemplateSlotsSlot());
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        getDelegate().removeDirectTemplateSlot(cls, slot);
        generateClsEvent(ClsEvent.TEMPLATE_SLOT_REMOVED, cls, slot);
        generateSlotEvent(SlotEvent.TEMPLATE_SLOT_CLS_REMOVED, slot, cls);
        generateOwnSlotValuesChangedEvent(cls, slot, _systemFrames.getDirectTemplateSlotsSlot());
    }

    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        getDelegate().moveDirectTemplateSlot(cls, slot, index);
        // generateClsEvent(ClsEvent.TEMPLATE_SLOT_MOVED, cls, slot);
        generateOwnSlotValuesChangedEvent(cls, _systemFrames.getDirectTemplateSlotsSlot());
    }

    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
        generateClsEvent(ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED, cls, slot);
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
        generateClsEvent(ClsEvent.TEMPLATE_FACET_VALUE_CHANGED, cls, slot, facet);
    }

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
        Iterator i = getTemplateFacets(cls, slot).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            generateClsEvent(ClsEvent.TEMPLATE_FACET_VALUE_CHANGED, cls, slot, facet);
        }
    }

    public boolean beginTransaction(String name) {
        boolean allowsTransactions = getDelegate().beginTransaction(name);
        generateTransactionEvent(TransactionEvent.TRANSACTION_BEGIN, name);
        if (!serverMode) {
          if (allowsTransactions) {
            _transactionStartSize = _events.size();
          }
        }
        return allowsTransactions;
    }
    
    private void generateTransactionEvent(int type, String name) {
        _events.add(new TransactionEvent(_kb, type, name));
    }

    public boolean commitTransaction() {
        boolean commitTransaction = getDelegate().commitTransaction();
        generateTransactionEvent(TransactionEvent.TRANSACTION_END, null);
        if (!serverMode) {
          if (!commitTransaction && _transactionStartSize != NO_VALUE) {
            _events.subList(_transactionStartSize + 1, _events.size()).clear();
          }
          _transactionStartSize = NO_VALUE;
        }
        return commitTransaction;
    }

    public boolean rollbackTransaction() {
        boolean rollbackTransaction = getDelegate().rollbackTransaction();
        generateTransactionEvent(TransactionEvent.TRANSACTION_END, null);
        if (!serverMode) {
          if (rollbackTransaction && _transactionStartSize != NO_VALUE) {
            _events.subList(_transactionStartSize + 1, _events.size()).clear();
          }
          _transactionStartSize = NO_VALUE;
        }
        return rollbackTransaction;
    }

    public boolean setDeletingFrameEventsEnabled(boolean b) {
        boolean oldValue = generateEventsOnDeletingFrames;
        generateEventsOnDeletingFrames = b;
        return oldValue;
    }

    public void replaceFrame(Frame original, Frame replacement) {
      String newName = replacement.getFrameID().getName();
      if (getFrame(newName) != null) {
        return;
      }
      
      if (original instanceof Cls) {
        generateDeleteClsEvents((Cls) original);
      }
      else if (original instanceof Slot) {
        generateDeleteSlotEvents((Slot) original);
      }
      else if (original instanceof Facet) {
        generateDeleteFacetEvents((Facet) original);
      }
      else if (original instanceof SimpleInstance) {
        generateDeleteSimpleInstanceEvents((SimpleInstance) original);
      }
      
      getDelegate().replaceFrame(original, replacement);
      
      Collection directTypes = getDelegate().getDirectTypes((Instance) replacement);
      if (original instanceof Cls) {
        generateCreateClsEvents((Cls) replacement, directTypes); 
      }
      if (original instanceof Slot) {
        generateCreateSlotEvents((Slot) replacement, directTypes);
      }
      if (original instanceof Facet) {
        generateCreateFacetEvents((Facet) replacement, directTypes);
      }
      if (original instanceof SimpleInstance) {
        generateCreateSimpleInstanceEvents((SimpleInstance) replacement, directTypes);
      }
      
    }
}