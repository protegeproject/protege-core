package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.DefaultSlot;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public class ImmutableNamesNarrowFrameStore implements NarrowFrameStore {

	private String name;
	
	private NarrowFrameStore delegate;

	private Slot nameSlot;
	
	public ImmutableNamesNarrowFrameStore(KnowledgeBase kb, NarrowFrameStore delegate) {
		this.delegate = delegate;
		nameSlot = new DefaultSlot(kb, Model.SlotID.NAME);
	}
	
	private boolean isNameSlot(Slot slot) {
		return slot != null && slot.getFrameID().equals(Model.SlotID.NAME);
	}
	
	private boolean isNameSft(Slot slot, Facet facet, boolean isTemplate) {
		return isNameSlot(slot) && facet == null && !isTemplate;
	}
	
	private void checkNotNameSft(Slot slot, Facet facet, boolean isTemplate) {
		if (isNameSft(slot, facet, isTemplate)) {
			throw new IllegalArgumentException("Should not be modifying name slot values");
		}
	}
	
	/*
	 *	Narrow Frame Store Interfaces 
	 */

	public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
		checkNotNameSft(slot, facet, isTemplate);
		delegate.addValues(frame, slot, facet, isTemplate, values);
	}

	public boolean beginTransaction(String name) {
		return delegate.beginTransaction(name);
	}

	public void close() {
		delegate.close();
		delegate = null;
	}

	public boolean commitTransaction() {
		return delegate.commitTransaction();
	}

	public void deleteFrame(Frame frame) {
		delegate.deleteFrame(frame);
	}

	public void executeQuery(Query query, QueryCallback callback) {
		delegate.executeQuery(query, callback);
	}

	public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
		if (isNameSft(slot, facet, isTemplate)) {
			return Collections.singleton(frame.getName());
		}
		return delegate.getClosure(frame, slot, facet, isTemplate);
	}

	public int getClsCount() {
		return delegate.getClsCount();
	}

	public NarrowFrameStore getDelegate() {
		return delegate;
	}

	public int getFacetCount() {
		return delegate.getFacetCount();
	}

	public Frame getFrame(FrameID id) {
		return delegate.getFrame(id);
	}

	public int getFrameCount() {
		return delegate.getFrameCount();
	}

	public Set<Frame> getFrames() {
		return delegate.getFrames();
	}

	public Set<Frame> getFrames(Slot slot, Facet facet, boolean isTemplate,
								Object value) {
		if (!isNameSft(slot, facet, isTemplate)) {
			return delegate.getFrames(slot, facet, isTemplate, value);
		}
		if (value == null || !(value instanceof String)) {
			return Collections.emptySet();
		}
		Frame frame = getFrame(new FrameID((String) value));
		if (frame == null) {
			return Collections.emptySet();
		}
		else {
			return Collections.singleton(frame);
		}
	}

	public Set<Frame> getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
		if (isNameSft(slot, facet, isTemplate)) {
			return getFrames();
		}
		return delegate.getFramesWithAnyValue(slot, facet, isTemplate);
	}

	public Set<Frame> getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) {
		if (!isNameSft(slot, facet, isTemplate)) {
			return delegate.getMatchingFrames(slot, facet, isTemplate, value, maxMatches);
		}
		Frame frame = getFrame(new FrameID((String) value));
		if (frame == null) {
			return Collections.emptySet();
		}
		else {
			return Collections.singleton(frame);
		}
	}

	public Set<Reference> getMatchingReferences(String value, int maxMatches) {
		Set<Reference> references = delegate.getMatchingReferences(value, maxMatches);
		Frame frame = getFrame(new FrameID(value));
		if (frame != null) {
			references.add(new ReferenceImpl(frame, nameSlot, null, false));
		}
		return references;
	}

	public String getName() {
		return name;
	}

	public Set<Reference> getReferences(Object value) {
		Set<Reference> references = delegate.getReferences(value);
		if (value instanceof String) {
			Frame frame = getFrame(new FrameID((String) value));
			if (frame != null) {
				references.add(new ReferenceImpl(frame, nameSlot, null, false));
			}
		}
		return references;
	}

	public int getSimpleInstanceCount() {
		return delegate.getSimpleInstanceCount();
	}

	public int getSlotCount() {
		return delegate.getSlotCount();
	}

	public TransactionMonitor getTransactionStatusMonitor() {
		return delegate.getTransactionStatusMonitor();
	}

	public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
		if (isNameSft(slot, facet, isTemplate)) {
			return Collections.singletonList(frame.getFrameID().getName());
		}
		return delegate.getValues(frame, slot, facet, isTemplate);
	}

	public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
		if (isNameSft(slot, facet, isTemplate)) {
			return 1;
		}
		else {
			return delegate.getValuesCount(frame, slot, facet, isTemplate);
		}
	}

	public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int to) {
		checkNotNameSft(slot, facet, isTemplate);
		delegate.moveValue(frame, slot, facet, isTemplate, from, to);
	}

	public void reinitialize() {
		delegate.reinitialize();
	}

	public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
		checkNotNameSft(slot, facet, isTemplate);
		delegate.removeValue(frame, slot, facet, isTemplate, value);
	}

	public void replaceFrame(Frame frame) {
		delegate.replaceFrame(frame);
	}

	public void replaceFrame(Frame original, Frame replacement) {
		delegate.replaceFrame(original, replacement);
	}

	public boolean rollbackTransaction() {
		return delegate.rollbackTransaction();
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
		checkNotNameSft(slot, facet, isTemplate);
		delegate.setValues(frame, slot, facet, isTemplate, values);
	}

}
