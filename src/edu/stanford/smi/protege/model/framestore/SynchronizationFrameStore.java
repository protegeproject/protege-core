package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public class SynchronizationFrameStore extends AbstractFrameStore {
	private ReadWriteLock locks = new ReentrantReadWriteLock();
	
	public Lock getReaderLock() {
		return locks.readLock();
	}
	
	public Lock getWriterLock() {
		return locks.writeLock();
	}
	

	public void addDirectSuperclass(Cls cls, Cls superclass) {
		locks.writeLock().lock();
		try {
			getDelegate().addDirectSuperclass(cls, superclass);
		}
		finally {
			locks.writeLock().unlock();
		}
	}

	public void addDirectSuperslot(Slot slot, Slot superslot) {
        locks.writeLock().lock();
        try {
            getDelegate().addDirectSuperslot(slot, superslot);
        }
        finally {
            locks.writeLock().unlock();
        }
	}

	public void addDirectTemplateSlot(Cls cls, Slot slot) {
        locks.writeLock().lock();
        try {
            getDelegate().addDirectTemplateSlot(cls, slot);
        }
        finally {
            locks.writeLock().unlock();
        }
	}

	public void addDirectType(Instance instance, Cls type) {
        locks.writeLock().lock();
        try {
            getDelegate().addDirectType(instance, type);
        }
        finally {
            locks.writeLock().unlock();
        }
	}

	public boolean beginTransaction(String name) {
        locks.writeLock().lock();
        try {
            return getDelegate().beginTransaction(name);
        }
        finally {
            locks.writeLock().unlock();
        }
	}
	
	public boolean commitTransaction() {
        locks.writeLock().lock();
        try {
            return getDelegate().commitTransaction();
        }
        finally {
            locks.writeLock().unlock();
        }
	}

	@SuppressWarnings("unchecked")
	public Cls createCls(FrameID id, Collection directTypes,
                         Collection directSuperclasses, 
                         boolean loadDefaultValues) {
        locks.writeLock().lock();
        try {
		return getDelegate().createCls(id, directTypes, directSuperclasses,
				loadDefaultValues);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	@SuppressWarnings("unchecked")
	public Facet createFacet(FrameID id, Collection directTypes,
                             boolean loadDefaultValues) {
        locks.writeLock().lock();
        try {
            return getDelegate().createFacet(id, directTypes, loadDefaultValues);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	@SuppressWarnings("unchecked")
	public SimpleInstance createSimpleInstance(FrameID id,
			Collection directTypes, boolean loadDefaultValues) {
        locks.writeLock().lock();
        try {
            return getDelegate()
				.createSimpleInstance(id, directTypes, loadDefaultValues);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	@SuppressWarnings("unchecked")
	public Slot createSlot(FrameID id, Collection directTypes,
			Collection directSuperslots, boolean loadDefaultValues) {
        locks.writeLock().lock();
        try {
            return getDelegate().createSlot(id, directTypes, directSuperslots,
                                       loadDefaultValues);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void deleteCls(Cls cls) {
        locks.writeLock().lock();
        try {
            getDelegate().deleteCls(cls);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void deleteFacet(Facet facet) {
        locks.writeLock().lock();
        try {
            getDelegate().deleteFacet(facet);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        locks.writeLock().lock();
        try {
            getDelegate().deleteSimpleInstance(simpleInstance);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void deleteSlot(Slot slot) {
        locks.writeLock().lock();
        try {
            getDelegate().deleteSlot(slot);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void executeQuery(Query query, QueryCallback callback) {
        locks.readLock().lock();
        try {
            getDelegate().executeQuery(query, callback);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getClsCount() {
        locks.readLock().lock();
        try {
            return getDelegate().getClsCount();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Cls> getClses() {
        locks.readLock().lock();
        try {
            return getDelegate().getClses();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Cls> getClsesWithAnyDirectTemplateSlotValue(Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getClsesWithAnyDirectTemplateSlotValue(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	@SuppressWarnings("unchecked")
	public Set getClsesWithDirectTemplateFacetValue(Slot slot, Facet facet,
			Object value) {
        locks.readLock().lock();
        try {
            return getDelegate()
				.getClsesWithDirectTemplateFacetValue(slot, facet, value);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value) {
        locks.readLock().lock();
        try {
            return getDelegate().getClsesWithDirectTemplateSlotValue(slot, value);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Cls> getClsesWithMatchingBrowserText(String string,
			Collection superclasses, int maxMatches) {
        locks.readLock().lock();
        try {
            return getDelegate().getClsesWithMatchingBrowserText(string, superclasses,
                                                            maxMatches);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getClsesWithMatchingDirectTemplateFacetValue(Slot slot,
			Facet facet, String value, int maxMatches) {
        locks.readLock().lock();
        try {
            return getDelegate().getClsesWithMatchingDirectTemplateFacetValue(slot,
                                                                         facet, value, maxMatches);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getClsesWithMatchingDirectTemplateSlotValue(Slot slot,
			String value, int maxMatches) {
        locks.readLock().lock();
        try {
            return getDelegate().getClsesWithMatchingDirectTemplateSlotValue(slot,
                                                                        value, maxMatches);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectDomain(Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectDomain(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List<Instance> getDirectInstances(Cls cls) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectInstances(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectlyOverriddenTemplateFacets(cls, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getDirectlyOverriddenTemplateSlots(Cls cls) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectlyOverriddenTemplateSlots(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectOwnSlotValues(Frame frame, Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectOwnSlotValues(frame, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectOwnSlotValuesClosure(frame, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectOwnSlotValuesCount(frame, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List<Cls> getDirectSubclasses(Cls cls) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectSubclasses(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectSubslots(Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectSubslots(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List<Cls> getDirectSuperclasses(Cls cls) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectSuperclasses(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectSuperslots(Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectSuperslots(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectTemplateFacetValues(cls, slot, facet);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectTemplateSlots(Cls cls) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectTemplateSlots(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectTemplateSlotValues(cls, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectTypes(Instance instance) {
        locks.readLock().lock();
        try {
            return getDelegate().getDirectTypes(instance);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getDomain(Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getDomain(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List<AbstractEvent> getEvents() {
        locks.readLock().lock();
        try {
            return getDelegate().getEvents();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getFacetCount() {
        locks.readLock().lock();
        try {
            return getDelegate().getFacetCount();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Facet> getFacets() {
        locks.readLock().lock();
        try {
            return getDelegate().getFacets();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Frame getFrame(FrameID id) {
        locks.readLock().lock();
        try {
            return getDelegate().getFrame(id);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Frame getFrame(String name) {
        locks.readLock().lock();
        try {
            return getDelegate().getFrame(name);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getFrameCount() {
        locks.readLock().lock();
        try {
            return getDelegate().getFrameCount();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public String getFrameName(Frame frame) {
        locks.readLock().lock();
        try {
            return getDelegate().getFrameName(frame);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Frame> getFrames() {
        locks.readLock().lock();
        try {
            return getDelegate().getFrames();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Frame> getFramesWithAnyDirectOwnSlotValue(Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getFramesWithAnyDirectOwnSlotValue(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Frame> getFramesWithDirectOwnSlotValue(Slot slot, Object value) {
        locks.readLock().lock();
        try {
            return getDelegate().getFramesWithDirectOwnSlotValue(slot, value);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Frame> getFramesWithMatchingDirectOwnSlotValue(Slot slot,
			String value, int maxMatches) {
        locks.readLock().lock();
        try {
            return getDelegate().getFramesWithMatchingDirectOwnSlotValue(slot, value,
                                                                    maxMatches);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Instance> getInstances(Cls cls) {
        locks.readLock().lock();
        try {
            return getDelegate().getInstances(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Reference> getMatchingReferences(String string, int maxMatches) {
        locks.readLock().lock();
        try {
            return getDelegate().getMatchingReferences(string, maxMatches);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getOverriddenTemplateFacets(Cls cls, Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getOverriddenTemplateFacets(cls, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getOverriddenTemplateSlots(Cls cls) {
        locks.readLock().lock();
        try {
            return getDelegate().getOverriddenTemplateSlots(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getOwnFacets(Frame frame, Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getOwnFacets(frame, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet) {
        locks.readLock().lock();
        try {
            return getDelegate().getOwnFacetValues(frame, slot, facet);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Slot> getOwnSlots(Frame frame) {
        locks.readLock().lock();
        try {
            return getDelegate().getOwnSlots(frame);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Collection getOwnSlotValues(Frame frame, Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getOwnSlotValues(frame, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Reference> getReferences(Object object) {
        locks.readLock().lock();
        try {
            return getDelegate().getReferences(object);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getSimpleInstanceCount() {
        locks.readLock().lock();
        try {
            return getDelegate().getSimpleInstanceCount();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getSlotCount() {
        locks.readLock().lock();
        try {
            return getDelegate().getSlotCount();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Slot> getSlots() {
        locks.readLock().lock();
        try {
            return getDelegate().getSlots();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Cls> getSubclasses(Cls cls) {
        locks.readLock().lock();
        try {
            return getDelegate().getSubclasses(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getSubslots(Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getSubslots(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getSuperclasses(Cls cls) {
        locks.readLock().lock();
        try {
            return getDelegate().getSuperclasses(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getSuperslots(Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getSuperslots(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Facet> getTemplateFacets(Cls cls, Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getTemplateFacets(cls, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        locks.readLock().lock();
        try {
            return getDelegate().getTemplateFacetValues(cls, slot, facet);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getTemplateSlots(Cls cls) {
        locks.readLock().lock();
        try {
            return getDelegate().getTemplateSlots(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Collection getTemplateSlotValues(Cls cls, Slot slot) {
        locks.readLock().lock();
        try {
            return getDelegate().getTemplateSlotValues(cls, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public TransactionMonitor getTransactionStatusMonitor() {
        locks.readLock().lock();
        try {
            return getDelegate().getTransactionStatusMonitor();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getTypes(Instance instance) {
        locks.readLock().lock();
        try {
            return getDelegate().getTypes(instance);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public void moveDirectOwnSlotValue(Frame frame, Slot slot, int indexFrom,
			int indexTo) {
        locks.writeLock().lock();
        try {
            getDelegate().moveDirectOwnSlotValue(frame, slot, indexFrom, indexTo);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        locks.writeLock().lock();
        try {
            getDelegate().moveDirectSubclass(cls, subclass, index);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        locks.writeLock().lock();
        try {
            getDelegate().moveDirectSubslot(slot, subslot, index);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        locks.writeLock().lock();
        try {
            getDelegate().moveDirectTemplateSlot(cls, slot, index);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void moveDirectType(Instance instance, Cls type, int index) {
        locks.writeLock().lock();
        try {
            getDelegate().moveDirectType(instance, type, index);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void reinitialize() {
        locks.writeLock().lock();
        try {
            getDelegate().reinitialize();
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void removeDirectSuperclass(Cls cls, Cls superclass) {
        locks.writeLock().lock();
        try {
            getDelegate().removeDirectSuperclass(cls, superclass);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void removeDirectSuperslot(Slot slot, Slot superslot) {
        locks.writeLock().lock();
        try {
            getDelegate().removeDirectSuperslot(slot, superslot);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        locks.writeLock().lock();
        try {
            getDelegate().removeDirectTemplateFacetOverrides(cls, slot);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        locks.writeLock().lock();
        try {
            getDelegate().removeDirectTemplateSlot(cls, slot);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void removeDirectType(Instance instance, Cls type) {
        locks.writeLock().lock();
        try {
            getDelegate().removeDirectType(instance, type);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void replaceFrame(Frame original, Frame replacement) {
        locks.writeLock().lock();
        try {
            getDelegate().replaceFrame(original, replacement);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public boolean rollbackTransaction() {
        locks.writeLock().lock();
        try {
            return getDelegate().rollbackTransaction();
        }
        finally {
            locks.writeLock().unlock();
        }
    }


	public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        locks.writeLock().lock();
        try {
            getDelegate().setDirectOwnSlotValues(frame, slot, values);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet,
			Collection values) {
        locks.writeLock().lock();
        try {
            getDelegate().setDirectTemplateFacetValues(cls, slot, facet, values);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	@SuppressWarnings("unchecked")
	public void setDirectTemplateSlotValues(Cls cls, Slot slot, Collection values) {
        locks.writeLock().lock();
        try {
            getDelegate().setDirectTemplateSlotValues(cls, slot, values);
        }
        finally {
            locks.writeLock().unlock();
        }
    }
	
	
}
