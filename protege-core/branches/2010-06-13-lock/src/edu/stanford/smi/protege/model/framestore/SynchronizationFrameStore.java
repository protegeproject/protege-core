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

public class SynchronizationFrameStore implements FrameStore {
	private FrameStore delegate;
	private ReadWriteLock locks = new ReentrantReadWriteLock();
	
	public Lock getReaderLock() {
		return locks.readLock();
	}
	
	public Lock getWriterLock() {
		return locks.writeLock();
	}
	
	public FrameStore getDelegate() {
		return delegate;
	}

	public void setDelegate(FrameStore delegate) {
		this.delegate = delegate;
    }
	

	public void addDirectSuperclass(Cls cls, Cls superclass) {
		locks.writeLock().lock();
		try {
			delegate.addDirectSuperclass(cls, superclass);
		}
		finally {
			locks.writeLock().unlock();
		}
	}

	public void addDirectSuperslot(Slot slot, Slot superslot) {
        locks.writeLock().lock();
        try {
            delegate.addDirectSuperslot(slot, superslot);
        }
        finally {
            locks.writeLock().unlock();
        }
	}

	public void addDirectTemplateSlot(Cls cls, Slot slot) {
        locks.writeLock().lock();
        try {
            delegate.addDirectTemplateSlot(cls, slot);
        }
        finally {
            locks.writeLock().unlock();
        }
	}

	public void addDirectType(Instance instance, Cls type) {
        locks.writeLock().lock();
        try {
            delegate.addDirectType(instance, type);
        }
        finally {
            locks.writeLock().unlock();
        }
	}

	public boolean beginTransaction(String name) {
        locks.writeLock().lock();
        try {
            return delegate.beginTransaction(name);
        }
        finally {
            locks.writeLock().unlock();
        }
	}

	public void close() {
		delegate.close();
	}

	public boolean commitTransaction() {
        locks.writeLock().lock();
        try {
            return delegate.commitTransaction();
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
		return delegate.createCls(id, directTypes, directSuperclasses,
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
            return delegate.createFacet(id, directTypes, loadDefaultValues);
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
            return delegate
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
            return delegate.createSlot(id, directTypes, directSuperslots,
                                       loadDefaultValues);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void deleteCls(Cls cls) {
        locks.writeLock().lock();
        try {
            delegate.deleteCls(cls);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void deleteFacet(Facet facet) {
        locks.writeLock().lock();
        try {
            delegate.deleteFacet(facet);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void deleteSimpleInstance(SimpleInstance simpleInstance) {
        locks.writeLock().lock();
        try {
            delegate.deleteSimpleInstance(simpleInstance);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void deleteSlot(Slot slot) {
        locks.writeLock().lock();
        try {
            delegate.deleteSlot(slot);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void executeQuery(Query query, QueryCallback callback) {
        locks.readLock().lock();
        try {
            delegate.executeQuery(query, callback);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getClsCount() {
        locks.readLock().lock();
        try {
            return delegate.getClsCount();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Cls> getClses() {
        locks.readLock().lock();
        try {
            return delegate.getClses();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Cls> getClsesWithAnyDirectTemplateSlotValue(Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getClsesWithAnyDirectTemplateSlotValue(slot);
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
            return delegate
				.getClsesWithDirectTemplateFacetValue(slot, facet, value);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getClsesWithDirectTemplateSlotValue(Slot slot, Object value) {
        locks.readLock().lock();
        try {
            return delegate.getClsesWithDirectTemplateSlotValue(slot, value);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Cls> getClsesWithMatchingBrowserText(String string,
			Collection superclasses, int maxMatches) {
        locks.readLock().lock();
        try {
            return delegate.getClsesWithMatchingBrowserText(string, superclasses,
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
            return delegate.getClsesWithMatchingDirectTemplateFacetValue(slot,
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
            return delegate.getClsesWithMatchingDirectTemplateSlotValue(slot,
                                                                        value, maxMatches);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectDomain(Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getDirectDomain(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List<Instance> getDirectInstances(Cls cls) {
        locks.readLock().lock();
        try {
            return delegate.getDirectInstances(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getDirectlyOverriddenTemplateFacets(Cls cls, Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getDirectlyOverriddenTemplateFacets(cls, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getDirectlyOverriddenTemplateSlots(Cls cls) {
        locks.readLock().lock();
        try {
            return delegate.getDirectlyOverriddenTemplateSlots(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectOwnSlotValues(Frame frame, Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getDirectOwnSlotValues(frame, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getDirectOwnSlotValuesClosure(Frame frame, Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getDirectOwnSlotValuesClosure(frame, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getDirectOwnSlotValuesCount(Frame frame, Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getDirectOwnSlotValuesCount(frame, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List<Cls> getDirectSubclasses(Cls cls) {
        locks.readLock().lock();
        try {
            return delegate.getDirectSubclasses(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectSubslots(Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getDirectSubslots(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List<Cls> getDirectSuperclasses(Cls cls) {
        locks.readLock().lock();
        try {
            return delegate.getDirectSuperclasses(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectSuperslots(Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getDirectSuperslots(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        locks.readLock().lock();
        try {
            return delegate.getDirectTemplateFacetValues(cls, slot, facet);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectTemplateSlots(Cls cls) {
        locks.readLock().lock();
        try {
            return delegate.getDirectTemplateSlots(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectTemplateSlotValues(Cls cls, Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getDirectTemplateSlotValues(cls, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List getDirectTypes(Instance instance) {
        locks.readLock().lock();
        try {
            return delegate.getDirectTypes(instance);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getDomain(Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getDomain(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public List<AbstractEvent> getEvents() {
        locks.readLock().lock();
        try {
            return delegate.getEvents();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getFacetCount() {
        locks.readLock().lock();
        try {
            return delegate.getFacetCount();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Facet> getFacets() {
        locks.readLock().lock();
        try {
            return delegate.getFacets();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Frame getFrame(FrameID id) {
        locks.readLock().lock();
        try {
            return delegate.getFrame(id);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Frame getFrame(String name) {
        locks.readLock().lock();
        try {
            return delegate.getFrame(name);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getFrameCount() {
        locks.readLock().lock();
        try {
            return delegate.getFrameCount();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public String getFrameName(Frame frame) {
        locks.readLock().lock();
        try {
            return delegate.getFrameName(frame);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Frame> getFrames() {
        locks.readLock().lock();
        try {
            return delegate.getFrames();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Frame> getFramesWithAnyDirectOwnSlotValue(Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getFramesWithAnyDirectOwnSlotValue(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Frame> getFramesWithDirectOwnSlotValue(Slot slot, Object value) {
        locks.readLock().lock();
        try {
            return delegate.getFramesWithDirectOwnSlotValue(slot, value);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Frame> getFramesWithMatchingDirectOwnSlotValue(Slot slot,
			String value, int maxMatches) {
        locks.readLock().lock();
        try {
            return delegate.getFramesWithMatchingDirectOwnSlotValue(slot, value,
                                                                    maxMatches);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Instance> getInstances(Cls cls) {
        locks.readLock().lock();
        try {
            return delegate.getInstances(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Reference> getMatchingReferences(String string, int maxMatches) {
        locks.readLock().lock();
        try {
            return delegate.getMatchingReferences(string, maxMatches);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public String getName() {
        locks.readLock().lock();
        try {
            return delegate.getName();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getOverriddenTemplateFacets(Cls cls, Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getOverriddenTemplateFacets(cls, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getOverriddenTemplateSlots(Cls cls) {
        locks.readLock().lock();
        try {
            return delegate.getOverriddenTemplateSlots(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getOwnFacets(Frame frame, Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getOwnFacets(frame, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Collection getOwnFacetValues(Frame frame, Slot slot, Facet facet) {
        locks.readLock().lock();
        try {
            return delegate.getOwnFacetValues(frame, slot, facet);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Slot> getOwnSlots(Frame frame) {
        locks.readLock().lock();
        try {
            return delegate.getOwnSlots(frame);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Collection getOwnSlotValues(Frame frame, Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getOwnSlotValues(frame, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Reference> getReferences(Object object) {
        locks.readLock().lock();
        try {
            return delegate.getReferences(object);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getSimpleInstanceCount() {
        locks.readLock().lock();
        try {
            return delegate.getSimpleInstanceCount();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public int getSlotCount() {
        locks.readLock().lock();
        try {
            return delegate.getSlotCount();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Slot> getSlots() {
        locks.readLock().lock();
        try {
            return delegate.getSlots();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Cls> getSubclasses(Cls cls) {
        locks.readLock().lock();
        try {
            return delegate.getSubclasses(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getSubslots(Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getSubslots(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getSuperclasses(Cls cls) {
        locks.readLock().lock();
        try {
            return delegate.getSuperclasses(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getSuperslots(Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getSuperslots(slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set<Facet> getTemplateFacets(Cls cls, Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getTemplateFacets(cls, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Collection getTemplateFacetValues(Cls cls, Slot slot, Facet facet) {
        locks.readLock().lock();
        try {
            return delegate.getTemplateFacetValues(cls, slot, facet);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getTemplateSlots(Cls cls) {
        locks.readLock().lock();
        try {
            return delegate.getTemplateSlots(cls);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Collection getTemplateSlotValues(Cls cls, Slot slot) {
        locks.readLock().lock();
        try {
            return delegate.getTemplateSlotValues(cls, slot);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public TransactionMonitor getTransactionStatusMonitor() {
        locks.readLock().lock();
        try {
            return delegate.getTransactionStatusMonitor();
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public Set getTypes(Instance instance) {
        locks.readLock().lock();
        try {
            return delegate.getTypes(instance);
        }
        finally {
            locks.readLock().unlock();
        }
    }

	public void moveDirectOwnSlotValue(Frame frame, Slot slot, int indexFrom,
			int indexTo) {
        locks.writeLock().lock();
        try {
            delegate.moveDirectOwnSlotValue(frame, slot, indexFrom, indexTo);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void moveDirectSubclass(Cls cls, Cls subclass, int index) {
        locks.writeLock().lock();
        try {
            delegate.moveDirectSubclass(cls, subclass, index);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void moveDirectSubslot(Slot slot, Slot subslot, int index) {
        locks.writeLock().lock();
        try {
            delegate.moveDirectSubslot(slot, subslot, index);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void moveDirectTemplateSlot(Cls cls, Slot slot, int index) {
        locks.writeLock().lock();
        try {
            delegate.moveDirectTemplateSlot(cls, slot, index);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void moveDirectType(Instance instance, Cls type, int index) {
        locks.writeLock().lock();
        try {
            delegate.moveDirectType(instance, type, index);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void reinitialize() {
        locks.writeLock().lock();
        try {
            delegate.reinitialize();
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void removeDirectSuperclass(Cls cls, Cls superclass) {
        locks.writeLock().lock();
        try {
            delegate.removeDirectSuperclass(cls, superclass);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void removeDirectSuperslot(Slot slot, Slot superslot) {
        locks.writeLock().lock();
        try {
            delegate.removeDirectSuperslot(slot, superslot);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void removeDirectTemplateFacetOverrides(Cls cls, Slot slot) {
        locks.writeLock().lock();
        try {
            delegate.removeDirectTemplateFacetOverrides(cls, slot);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void removeDirectTemplateSlot(Cls cls, Slot slot) {
        locks.writeLock().lock();
        try {
            delegate.removeDirectTemplateSlot(cls, slot);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void removeDirectType(Instance instance, Cls type) {
        locks.writeLock().lock();
        try {
            delegate.removeDirectType(instance, type);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void replaceFrame(Frame original, Frame replacement) {
        locks.writeLock().lock();
        try {
            delegate.replaceFrame(original, replacement);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public boolean rollbackTransaction() {
        locks.writeLock().lock();
        try {
            return delegate.rollbackTransaction();
        }
        finally {
            locks.writeLock().unlock();
        }
    }


	public void setDirectOwnSlotValues(Frame frame, Slot slot, Collection values) {
        locks.writeLock().lock();
        try {
            delegate.setDirectOwnSlotValues(frame, slot, values);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void setDirectTemplateFacetValues(Cls cls, Slot slot, Facet facet,
			Collection values) {
        locks.writeLock().lock();
        try {
            delegate.setDirectTemplateFacetValues(cls, slot, facet, values);
        }
        finally {
            locks.writeLock().unlock();
        }
    }

	public void setDirectTemplateSlotValues(Cls cls, Slot slot,
			Collection values) {
        locks.writeLock().lock();
        try {
            delegate.setDirectTemplateSlotValues(cls, slot, values);
        }
        finally {
            locks.writeLock().unlock();
        }
    }
	
	
}
