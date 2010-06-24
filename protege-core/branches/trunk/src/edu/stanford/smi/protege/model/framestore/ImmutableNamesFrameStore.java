package edu.stanford.smi.protege.model.framestore;

import java.util.Collection;
import java.util.Random;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;

public class ImmutableNamesFrameStore extends FrameStoreAdapter {
	private KnowledgeBase kb;
	private int nextName;

	public ImmutableNamesFrameStore(KnowledgeBase kb) {
		this.kb = kb;
	}

	public FrameID adjustFrameId(FrameID id) {
		if (id == null || id.getName() == null) {
			return new FrameID(generateUniqueName("Class"));
		} else {
			checkUniqueness(id.getName());
			return id;
		}
	}

	protected void checkUniqueness(String name) {
		if (getFrame(name) != null) {
			throw new IllegalArgumentException(name + " not unique");
		}
	}

	protected String generateUniqueName(String baseName) {
		String uniqueName = null;
		baseName = kb.getName() + "_" + baseName;

		while (uniqueName == null) {
			String s = baseName + nextName;
			if (getFrame(s) == null) {
				uniqueName = s;
				++nextName;
			} else {
			    nextName += 10000;
			}
		}
		return uniqueName;
	}



	/*
	 * ---------------------------------------------------------------------------
	 * FrameStore calls.
	 */

	public String getFrameName(Frame frame) {
		return frame.getFrameID().getName();
	}

	public Cls createCls(FrameID id, 
			Collection directTypes, 
			Collection directSuperclasses, 
			boolean loadDefaultValues) {
		id = adjustFrameId(id);
		Cls cls = getDelegate().createCls(id, directTypes, directSuperclasses, loadDefaultValues);
		return cls;
	}

	public Slot createSlot(FrameID id, Collection directTypes, Collection directSuperslots, boolean loadDefaultValues) {
		id = adjustFrameId(id);
		Slot slot = getDelegate().createSlot(id, directTypes, directSuperslots, loadDefaultValues);
		return slot;
	}

	public Facet createFacet(FrameID id, Collection directTypes, boolean loadDefaultValues) {
		id = adjustFrameId(id);
		Facet facet =  getDelegate().createFacet(id, directTypes, loadDefaultValues);
		return facet;
	}

	public SimpleInstance createSimpleInstance(FrameID id,
			Collection directTypes,
			boolean loadDefaultValues) {
		id = adjustFrameId(id);
		SimpleInstance instance = getDelegate().createSimpleInstance(id, directTypes, loadDefaultValues);
		return instance;
	}
}
