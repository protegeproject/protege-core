package edu.stanford.smi.protege.model.framestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;


public class EventGeneratorFrameStore extends ModificationFrameStore {

    private List<AbstractEvent> _events = new ArrayList<AbstractEvent>();
    private DefaultKnowledgeBase _kb;
    private SystemFrames _systemFrames;
    private boolean generateEventsOnDeletingFrames = false;
    private TransactionMonitor transactionMonitor;
    private boolean inReplaceFrameOperation = false;

    public EventGeneratorFrameStore(KnowledgeBase kb) {
        _kb = (DefaultKnowledgeBase) kb;
        _systemFrames = _kb.getSystemFrames();
    }

    @Override
    public void setDelegate(FrameStore delegate) {
    	super.setDelegate(delegate);
    	if (delegate != null) {
    		transactionMonitor = delegate.getTransactionStatusMonitor();
    	}
    }


    public void reinitialize() {
        _events.clear();
    }

    @Override
	public void close() {
        super.close();
        _events.clear();
        _events = null;
        _kb = null;
        _systemFrames = null;
    }

    @Override
	public Frame getFrame(String name) {
        return getDelegate().getFrame(name);
    }

    public Cls createCls(FrameID id, Collection directTypes, Collection superClasses, boolean loadDefaults) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        Cls cls = getDelegate().createCls(id, directTypes, superClasses, loadDefaults);
        generateCreateClsEvents(cls, directTypes, level);
        return cls;
    }

    public Slot createSlot(FrameID id, Collection directTypes, Collection superslots, boolean loadDefaults) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        Slot slot = getDelegate().createSlot(id, directTypes, superslots, loadDefaults);
        generateCreateSlotEvents(slot, directTypes, level);
        return slot;
    }

    public Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaults) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        Facet facet = getDelegate().createFacet(id, directTypes, loadDefaults);
        generateCreateFacetEvents(facet, directTypes, level);
        return facet;
    }

    public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        generateSetDirectOwnSlotValuesEvents(frame, slot, values, level);
        getDelegate().setDirectOwnSlotValues(frame, slot, values);
    }

    private void generateSetDirectOwnSlotValuesEvents(Frame frame, Slot slot, Collection values, TransactionIsolationLevel level) {
        generateOwnSlotValuesChangedEvent(frame, slot, level);
        generateFrameEvent(FrameEvent.BROWSER_TEXT_CHANGED, frame, level);
        Slot inverseSlot = (Slot) CollectionUtilities.getFirstItem(getDirectOwnSlotValues(slot, _systemFrames
                .getInverseSlotSlot()));
        if (inverseSlot != null) {
            Collection oldValues = getDirectOwnSlotValues(frame, slot);
            // back references from new values
            Iterator i = values.iterator();
            while (i.hasNext()) {
                Frame newValue = (Frame) i.next();
                if (!oldValues.contains(newValue)) {
                    generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, newValue, inverseSlot, level);
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
                    generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, oldValue, inverseSlot, level);
                }
            }
        }
    }

    private void generateDeleteFrameKbEvent(int type, Frame frame, TransactionIsolationLevel level) {
        generateKbEvent(type, frame, frame.getName(), level);
    }

    private void generateDeleteClsEvents(Cls cls, TransactionIsolationLevel level) {
        // generateDeleteSubclassEvents(cls);
        generateDeleteFrameKbEvent(KnowledgeBaseEvent.CLS_DELETED, cls, level);
        Iterator i = getDirectSuperclasses(cls).iterator();
        while (i.hasNext()) {
            Cls superCls = (Cls) i.next();
            generateClsEvent(ClsEvent.DIRECT_SUBCLASS_REMOVED, superCls, cls, level);
        }
        generateDeleteInstanceEvents(cls, level);
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


    private void generateDeleteSimpleInstanceEvents(SimpleInstance simpleInstance,
                                                    TransactionIsolationLevel level) {
        generateDeleteFrameKbEvent(KnowledgeBaseEvent.INSTANCE_DELETED, simpleInstance, level);
        generateDeleteInstanceEvents(simpleInstance, level);
    }

    private void generateDeleteSlotEvents(Slot slot,
                                          TransactionIsolationLevel level) {
        generateDeleteFrameKbEvent(KnowledgeBaseEvent.SLOT_DELETED, slot, level);
        /** @todo other slot events */
        for (Object  o : getDelegate().getFramesWithDirectOwnSlotValue(_systemFrames.getDirectTemplateSlotsSlot(), slot)) {
            if (o instanceof Cls) {
                generateClsEvent(ClsEvent.TEMPLATE_SLOT_REMOVED, (Cls) o, slot, level);
            }
        }
        Iterator i = getDirectSuperslots( slot ).iterator();
        while (i.hasNext()) {
        	Slot superSlot = (Slot) i.next();
        	generateSlotEvent(SlotEvent.DIRECT_SUPERSLOT_REMOVED, slot, superSlot, level);
        }
        generateDeleteInstanceEvents(slot, level);
    }

    private void generateDeleteFacetEvents(Facet facet, TransactionIsolationLevel level) {
        generateDeleteFrameKbEvent(KnowledgeBaseEvent.FACET_DELETED, facet, level);
        /** @todo other facet events */
        generateDeleteInstanceEvents(facet, level);
    }

    private void generateDeleteInstanceEvents(Instance instance, TransactionIsolationLevel level) {
        generateRemoveDirectInstanceEvents(instance, level);
        Iterator<Reference> i = getReferences(instance).iterator();
        while (i.hasNext()) {
            Reference ref = i.next();
            removeReference(ref, instance, level);
        }
        generateFrameEvent(FrameEvent.DELETED, instance, instance.getName(), level);
    }

    private void generateRemoveDirectInstanceEvents(Instance instance,
                                                    TransactionIsolationLevel level) {
        Iterator i = getDirectTypes(instance).iterator();
        while (i.hasNext()) {
            Cls type = (Cls) i.next();
            generateClsEvent(ClsEvent.DIRECT_INSTANCE_REMOVED, type, instance, level);
        }
    }

    private void removeReference(Reference ref, Instance instance, TransactionIsolationLevel level) {
        Frame frame = ref.getFrame();
        Slot slot = ref.getSlot();
        Facet facet = ref.getFacet();
        boolean isTemplate = ref.isTemplate();
        removeReference(frame, slot, facet, isTemplate, instance, level);
    }

    private void removeReference(Frame frame, Slot slot, Facet facet, boolean isTemplate, Instance instance,
    							 TransactionIsolationLevel  level) {
        if (facet == null) {
            if (isTemplate) {
                removeTemplateSlotValueReference(frame, slot, instance, level);
            } else {
                removeOwnSlotValueReference(frame, slot, instance, level);
            }
        } else {
            if (isTemplate) {
                removeTemplateFacetValueReference(frame, slot, facet, instance, level);
            } else {
                // ???
            }
        }
    }

    private void removeOwnSlotValueReference(Frame frame, Slot slot, Instance instance,
    										 TransactionIsolationLevel level) {
        generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, frame, slot, level);
    }

    private void removeTemplateSlotValueReference(Frame frame, Slot slot, Instance instance,
    											  TransactionIsolationLevel level) {
        // TODO
    }

    private void removeTemplateFacetValueReference(Frame frame, Slot slot, Facet facet, Instance instance,
    											   TransactionIsolationLevel level) {
        // TODO
    }

    public void deleteCls(Cls cls) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        generateDeleteClsEvents(cls, level);
        getDelegate().deleteCls(cls);
    }

    public void deleteSlot(Slot slot) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        generateDeleteSlotEvents(slot, level);
        getDelegate().deleteSlot(slot);
    }

    public void deleteFacet(Facet facet) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        generateDeleteFacetEvents(facet, level);
        getDelegate().deleteFacet(facet);
    }

    public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        generateDeleteSimpleInstanceEvents(simpleInstance, level);
        getDelegate().deleteSimpleInstance(simpleInstance);
    }

    public SimpleInstance createSimpleInstance(FrameID id, Collection directTypes, boolean loadDefaults) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        SimpleInstance simpleInstance = getDelegate().createSimpleInstance(id,directTypes, loadDefaults);
        generateCreateSimpleInstanceEvents(simpleInstance, directTypes, level);
        return simpleInstance;
    }

    private void generateCreateClsEvents(Cls newCls, Collection directTypes, TransactionIsolationLevel level) {
        generateCreateInstanceEvents(KnowledgeBaseEvent.CLS_CREATED, newCls, directTypes, level);
        Iterator i = newCls.getDirectSuperclasses().iterator();
        while (i.hasNext()) {
            Cls superclass = (Cls) i.next();
            generateClsEvent(ClsEvent.DIRECT_SUBCLASS_ADDED, superclass, newCls, level);
            generateOwnSlotValuesChangedEvent(superclass, _systemFrames.getDirectSubclassesSlot(), level);
        }
    }

    private void generateCreateFacetEvents(Facet newFacet, Collection directTypes, TransactionIsolationLevel level) {
        generateCreateInstanceEvents(KnowledgeBaseEvent.FACET_CREATED, newFacet, directTypes, level);
    }

    private void generateCreateSlotEvents(Slot newSlot, Collection directTypes,
                                          TransactionIsolationLevel level) {
        generateCreateInstanceEvents(KnowledgeBaseEvent.SLOT_CREATED, newSlot, directTypes, level);
        for (Object  o : getDelegate().getFramesWithDirectOwnSlotValue(_systemFrames.getDirectTemplateSlotsSlot(), newSlot)) {
            if (o instanceof Cls) {
                generateClsEvent(ClsEvent.TEMPLATE_SLOT_ADDED, (Cls) o, newSlot, level);
            }
        }
        Iterator i = newSlot.getDirectSuperslots().iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            generateSlotEvent(SlotEvent.DIRECT_SUBSLOT_ADDED, superslot, newSlot, level);
            generateOwnSlotValuesChangedEvent(superslot, _systemFrames.getDirectSubslotsSlot(), level);
        }
    }

    private void generateCreateSimpleInstanceEvents(SimpleInstance newFrame, Collection directTypes, TransactionIsolationLevel level) {
        generateCreateInstanceEvents(KnowledgeBaseEvent.INSTANCE_CREATED, newFrame, directTypes, level);
    }

    private void generateCreateInstanceEvents(int type, Frame newFrame, Collection directTypes,
    										  TransactionIsolationLevel level) {
        generateKbEvent(type, newFrame, level);
        Iterator i = directTypes.iterator();
        while (i.hasNext()) {
            Cls directType = (Cls) i.next();
            generateClsEvent(ClsEvent.DIRECT_INSTANCE_ADDED, directType, newFrame, level);
            generateOwnSlotValuesChangedEvent(directType, _systemFrames.getDirectInstancesSlot(), level);
        }
    }

    private void generateReplacedFrameEvents(Frame original, Frame replacement, TransactionIsolationLevel level) {
        String oldName = original.getName();
        generateFrameEvent(FrameEvent.REPLACE_FRAME, original, oldName, replacement, level);
        generateKbEvent(KnowledgeBaseEvent.FRAME_REPLACED, original, oldName, replacement);
    }

    private void generateReplacingFrameEvents(Frame replacement, Frame original, TransactionIsolationLevel level) {
    	generateReplacingFrameAsValueEvents(replacement, original, level);
    	if (replacement instanceof Slot) {
    		generateReplacingSlotEvents((Slot) replacement, level);
    	}
    }

    private void generateReplacingFrameAsValueEvents(Frame replacement, Frame original, TransactionIsolationLevel level) {
    	Set<Reference> references = getDelegate().getReferences(replacement);
    	if (references == null) {
			return;
		}
    	for (Reference reference : references) {
    		if (!reference.isTemplate()) {
    			Frame sourceFrame = reference.getFrame();
    			Slot sourceSlot = reference.getSlot();
    			Collection values = getDelegate().getOwnSlotValues(sourceFrame, sourceSlot);
    			if (values == null || !values.contains(replacement)) {
					continue;
				}
    			Collection oldValues = new ArrayList(values);
    			oldValues.remove(replacement);
    			oldValues.add(original);  //TODO you need to add inferred events here + junits.
    			 					      //     inverse slots and super-slots.
    			generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, sourceFrame, sourceSlot, oldValues, level);
    		}
    	}
    }

    private void generateReplacingSlotEvents(Slot replacement, TransactionIsolationLevel level) {
    	Set<Frame> frames = getDelegate().getFramesWithAnyDirectOwnSlotValue(replacement);
    	if (frames == null) {
			return;
		}
    	for (Frame frame : frames) {
    		generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, frame, replacement, level);
    	}
    }

    private void generateKbEvent(int type, Frame frame, TransactionIsolationLevel level) {
        addEvent(new KnowledgeBaseEvent(_kb, type, frame), level);
    }

    private void generateKbEvent(int type, Frame frame, String s, TransactionIsolationLevel level) {
        addEvent(new KnowledgeBaseEvent(_kb, type, frame, s), level);
    }

    private void generateKbEvent(int type, Frame frame, String s, Frame oldFrame) {
        addEvent(new KnowledgeBaseEvent(_kb, type, frame, s, oldFrame));
    }

    private boolean generateEvent(Frame frame) {
        return !frame.isBeingDeleted() || generateEventsOnDeletingFrames;
    }

    private void generateFrameEvent(int type, Frame frame, TransactionIsolationLevel level) {
        generateFrameEvent(type, frame, null, null, level);
    }

    private void generateFrameEvent(int type, Frame frame, Object o2, TransactionIsolationLevel level) {
        generateFrameEvent(type, frame, o2, null, level);
    }

    private void generateFrameEvent(int type, Frame frame, Object o2, Object o3,
    								TransactionIsolationLevel level) {
        if (generateEvent(frame)) {
            addEvent(new FrameEvent(frame, type, o2, o3), level);
        }
    }

    private void generateClsEvent(int type, Cls cls, Frame frame1, Frame frame2,
    							  TransactionIsolationLevel level) {
        if (generateEvent(cls)) {
            addEvent(new ClsEvent(cls, type, frame1, frame2), level);
        }
    }

    private void generateClsEvent(int type, Cls cls, Frame frame,
    							  TransactionIsolationLevel level) {
        generateClsEvent(type, cls, frame, null, level);
    }

    private void generateSlotEvent(int type, Slot slot, Frame frame, TransactionIsolationLevel level) {
        if (generateEvent(slot)) {
            addEvent(new SlotEvent(slot, type, frame), level);
        }
    }

    private void generateInstanceEvent(int type, Instance instance, Frame frame,
                                       TransactionIsolationLevel level) {
        if (generateEvent(instance)) {
            addEvent(new InstanceEvent(instance, type, frame), level);
        }
    }

    @Override
	public List<AbstractEvent> getEvents() {
    	List<AbstractEvent> events = _events;
    	_events = new ArrayList<AbstractEvent>();
    	return events;
    }

    public void addDirectSuperclass(Cls cls, Cls superclass) {
        Collection addedSlots = getSlotsToBeAdded(cls, superclass);
        getDelegate().addDirectSuperclass(cls, superclass);
        TransactionIsolationLevel level = getTransactionIsolationLevel();

        generateClsEvent(ClsEvent.DIRECT_SUPERCLASS_ADDED, cls, superclass, level);
        generateClsEvent(ClsEvent.DIRECT_SUBCLASS_ADDED, superclass, cls, level);
        generateSuperclassTemplateSlotChangedEvents(cls, ClsEvent.TEMPLATE_SLOT_ADDED, addedSlots, level);
        generateOwnSlotValuesChangedEvent(cls, superclass, _systemFrames.getDirectSuperclassesSlot(), level);
    }

    private Collection getSlotsToBeAdded(Cls cls, Cls superclass) {
        Collection slots = new HashSet(getTemplateSlots(superclass));
        slots.removeAll(getTemplateSlots(cls));
        return slots;
    }

    private void generateSuperclassTemplateSlotChangedEvents(Cls cls,
                                                             int type,
                                                             Collection addedSlots,
                                                             TransactionIsolationLevel level) {
        Iterator i = addedSlots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            generateClsEvent(type, cls, slot, level);
        }
    }

    private void generateOwnSlotValuesChangedEvent(Frame frame1,
                                                   Frame frame2,
                                                   Slot slot,
                                                   TransactionIsolationLevel level) {
        generateOwnSlotValuesChangedEvent(frame1, slot, level);
        generateOwnSlotValuesChangedEvent(frame2, slot.getInverseSlot(), level);
    }

    private void generateOwnSlotValuesChangedEvent(Frame frame, Slot slot,
                                                   TransactionIsolationLevel level) {
        Collection oldValues = null;
        if (getValues(frame, slot)) {
            oldValues = new ArrayList(getDirectOwnSlotValues(frame, slot));
        }
        generateOwnSlotValuesChangedEvent(frame, slot, oldValues, level);
    }

    private boolean getValues(Frame frame, Slot slot) {
        return !slot.equals(_systemFrames.getDirectSubclassesSlot())
                && !slot.equals(_systemFrames.getDirectInstancesSlot());
    }

    private void generateOwnSlotValuesChangedEvent(Frame frame, Slot slot, Collection oldValues,
    											   TransactionIsolationLevel level) {
        generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, frame, slot, oldValues, level);
        Iterator i = slot.getSuperslots().iterator();
        while (i.hasNext()) {
            Slot superslot = (Slot) i.next();
            generateFrameEvent(FrameEvent.OWN_SLOT_VALUE_CHANGED, frame, superslot, oldValues, level);
        }
    }

    public void removeDirectSuperclass(Cls cls, Cls superclass) {
    	TransactionIsolationLevel level = getTransactionIsolationLevel();
        HashSet removedSlots = new HashSet(cls.getTemplateSlots());
        getDelegate().removeDirectSuperclass(cls, superclass);
        removedSlots.removeAll(cls.getTemplateSlots());
        generateClsEvent(ClsEvent.DIRECT_SUPERCLASS_REMOVED, cls, superclass, level);
        generateClsEvent(ClsEvent.DIRECT_SUBCLASS_REMOVED, superclass, cls, level);
        generateSuperclassTemplateSlotChangedEvents(cls, ClsEvent.TEMPLATE_SLOT_REMOVED, removedSlots, level);
        generateOwnSlotValuesChangedEvent(cls, superclass, _systemFrames.getDirectSuperclassesSlot(), level);
    }

    public void moveDirectOwnSlotValue(Frame frame, Slot slot, int from, int to) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        getDelegate().moveDirectOwnSlotValue(frame, slot, from, to);
        generateOwnSlotValuesChangedEvent(frame, slot, level);
    }

    public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();

        getDelegate().moveDirectSubclass(cls, subclass, index);
        generateClsEvent(ClsEvent.DIRECT_SUBCLASS_MOVED, cls, subclass, level);
        generateOwnSlotValuesChangedEvent(cls, _systemFrames.getDirectSubclassesSlot(), level);
    }

    public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        getDelegate().moveDirectSubslot(slot, subslot, index);
        generateSlotEvent(SlotEvent.DIRECT_SUBSLOT_MOVED, slot, subslot, level);
        generateOwnSlotValuesChangedEvent(slot, _systemFrames.getDirectSubslotsSlot(), level);
    }

    public void addDirectSuperslot(Slot slot, Slot superslot) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        getDelegate().addDirectSuperslot(slot, superslot);
        generateSlotEvent(SlotEvent.DIRECT_SUPERSLOT_ADDED, slot, superslot, level);
        generateSlotEvent(SlotEvent.DIRECT_SUBSLOT_ADDED, superslot, slot, level);
        generateOwnSlotValuesChangedEvent(slot, superslot, _systemFrames.getDirectSuperslotsSlot(), level);
    }

    public void removeDirectSuperslot(Slot slot, Slot superslot) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        getDelegate().removeDirectSuperslot(slot, superslot);
        generateSlotEvent(SlotEvent.DIRECT_SUPERSLOT_REMOVED, slot, superslot, level);
        generateSlotEvent(SlotEvent.DIRECT_SUBSLOT_REMOVED, superslot, slot, level);
        generateOwnSlotValuesChangedEvent(slot, superslot, _systemFrames.getDirectSuperslotsSlot(), level);
    }

    public void addDirectType(Instance instance, Cls type) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        getDelegate().addDirectType(instance, type);
        generateInstanceEvent(InstanceEvent.DIRECT_TYPE_ADDED, instance, type, level);
        generateClsEvent(ClsEvent.DIRECT_INSTANCE_ADDED, type, instance, level);
        generateOwnSlotValuesChangedEvent(instance, type, _systemFrames.getDirectTypesSlot(), level);
    }

    public void removeDirectType(Instance instance, Cls type) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();

        getDelegate().removeDirectType(instance, type);
        generateInstanceEvent(InstanceEvent.DIRECT_TYPE_REMOVED, instance, type, level);
        generateClsEvent(ClsEvent.DIRECT_INSTANCE_REMOVED, type, instance, level);
        generateOwnSlotValuesChangedEvent(instance, type, _systemFrames.getDirectTypesSlot(), level);
    }

    public void moveDirectType(Instance instance, Cls cls, int index) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();

        getDelegate().moveDirectType(instance, cls, index);
        generateOwnSlotValuesChangedEvent(instance, _systemFrames.getDirectTypesSlot(), level);
    }

    public void addDirectTemplateSlot(Cls cls, Slot slot) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        getDelegate().addDirectTemplateSlot(cls, slot);
        generateClsEvent(ClsEvent.TEMPLATE_SLOT_ADDED, cls, slot, level);
        generateSlotEvent(SlotEvent.TEMPLATE_SLOT_CLS_ADDED, slot, cls, level);
        generateOwnSlotValuesChangedEvent(cls, slot, _systemFrames.getDirectTemplateSlotsSlot(), level);
    }

    public void removeDirectTemplateSlot(Cls cls, Slot slot) {
    	TransactionIsolationLevel level = getTransactionIsolationLevel();
        getDelegate().removeDirectTemplateSlot(cls, slot);
        generateClsEvent(ClsEvent.TEMPLATE_SLOT_REMOVED, cls, slot, level);
        generateSlotEvent(SlotEvent.TEMPLATE_SLOT_CLS_REMOVED, slot, cls, level);
        generateOwnSlotValuesChangedEvent(cls, slot, _systemFrames.getDirectTemplateSlotsSlot(), level);
    }

    public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();

        getDelegate().moveDirectTemplateSlot(cls, slot, index);
        // generateClsEvent(ClsEvent.TEMPLATE_SLOT_MOVED, cls, slot);
        generateOwnSlotValuesChangedEvent(cls, _systemFrames.getDirectTemplateSlotsSlot(), level);
    }

    public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();

        getDelegate().setDirectTemplateSlotValues(cls, slot, values);
        generateClsEvent(ClsEvent.TEMPLATE_SLOT_VALUE_CHANGED, cls, slot, level);
    }

    public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet, Collection values) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();

        getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
        generateClsEvent(ClsEvent.TEMPLATE_FACET_VALUE_CHANGED, cls, slot, facet, level);
    }

    public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();

        getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
        Iterator i = getTemplateFacets(cls, slot).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            generateClsEvent(ClsEvent.TEMPLATE_FACET_VALUE_CHANGED, cls, slot, facet, level);
        }
    }

    public boolean beginTransaction(String name) {
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        generateTransactionEvent(TransactionEvent.TRANSACTION_BEGIN, name, Boolean.FALSE, level);
        boolean allowsTransactions = getDelegate().beginTransaction(name);
        return allowsTransactions;
    }

    private void generateTransactionEvent(int type, String name, Boolean commit,
                                          TransactionIsolationLevel level) {
        addEvent(new TransactionEvent(_kb, type, name, commit), level);
    }

    public boolean commitTransaction() {
        boolean commitTransaction = getDelegate().commitTransaction();
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        generateTransactionEvent(TransactionEvent.TRANSACTION_END, null, commitTransaction, level);
        return commitTransaction;
    }

    public boolean rollbackTransaction() {
        boolean rollbackTransaction = getDelegate().rollbackTransaction();
        TransactionIsolationLevel level = getTransactionIsolationLevel();
        generateTransactionEvent(TransactionEvent.TRANSACTION_END, null, Boolean.FALSE, level);
        return rollbackTransaction;
    }

    public boolean setDeletingFrameEventsEnabled(boolean b) {
        boolean oldValue = generateEventsOnDeletingFrames;
        generateEventsOnDeletingFrames = b;
        return oldValue;
    }

    /*
     * This is complicated...
	 */
    public void replaceFrame(Frame original, Frame replacement) {
    	TransactionIsolationLevel level = getTransactionIsolationLevel();
    	String newName = replacement.getFrameID().getName();
    	if (getFrame(newName) != null) {
    		return;
    	}
    	try {
    		generateReplacedFrameEvents(original, replacement, level);    	
    	}
    	finally {
    		inReplaceFrameOperation=false;
    		getDelegate().replaceFrame(original, replacement);
    	}    
    }

    public void addCustomEvent(AbstractEvent event) {
        TransactionIsolationLevel level = transactionMonitor == null ? null : transactionMonitor.getTransationIsolationLevel();
        boolean visible = TransactionMonitor.updatesSeenByUntransactedClients(transactionMonitor, level);
        event.setHiddenByTransaction(!visible);
        _events.add(event);
    }

    private void addEvent(AbstractEvent event, TransactionIsolationLevel level) {
    	if (inReplaceFrameOperation) {
    		event.setReplacementEvent(true);
    	}
    	_events.add(event);
    	boolean visible = TransactionMonitor.updatesSeenByUntransactedClients(transactionMonitor, level);
    	event.setHiddenByTransaction(!visible);
    }

    private void addEvent(AbstractEvent event) {
    	_events.add(event);
    }


    private TransactionIsolationLevel getTransactionIsolationLevel() {
    	if (transactionMonitor == null) {
    		return TransactionIsolationLevel.NONE;
    	}
    	else {
    		return transactionMonitor.getTransationIsolationLevel();
    	}
    }


}