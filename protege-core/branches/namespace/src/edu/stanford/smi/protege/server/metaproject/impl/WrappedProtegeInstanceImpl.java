package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.server.metaproject.MetaProject.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.MetaProject.SlotEnum;
import edu.stanford.smi.protege.util.LocalizeUtils;

public class WrappedProtegeInstanceImpl implements Localizable, Serializable {
    private static final long serialVersionUID = -1976680694911360227L;
    MetaProjectImpl mp;
	private Instance i;
	private ClsEnum cls;

	public WrappedProtegeInstanceImpl(MetaProjectImpl mp, Instance i, ClsEnum cls) {
		if (!i.hasType(mp.getCls(cls))) {
			throw new IllegalArgumentException("" + i + " should be of type " + cls);
		}
		this.i = i;
		this.cls = cls;
		this.mp = mp;
	}

	public MetaProjectImpl getMetaProject() {
		return mp;
	}

	public Instance getProtegeInstance() {
		return i;
	}
	
	public ClsEnum getCls() {
		return cls;
	}

	@SuppressWarnings("unchecked")
	protected Set getSlotValues(SlotEnum slot, ClsEnum rangeCls) {
		Set results = new HashSet();
		for (Object o : i.getOwnSlotValues(mp.getSlot(slot))) {
		    if (o instanceof Instance)
			results.add(mp.wrapInstance(rangeCls, (Instance) o));
		}
		return results;
	}

	protected Object getSlotValue(SlotEnum slot, ClsEnum rangeCls) {
		Object o = i.getOwnSlotValue(mp.getSlot(slot));
		
		if (o != null) {
			return mp.wrapInstance(rangeCls, (Instance) o);
		}
		return null;
	}


	protected void setSlotValuesAsProtegeInstances(SlotEnum slot, Collection valueCollection) {
		Slot protege_slot = mp.getSlot(slot);
		i.setDirectOwnSlotValues(protege_slot, getProtegeCollection(valueCollection));
	}
	
	protected void setSlotValue(SlotEnum slot, Object value) {
		Slot protege_slot = mp.getSlot(slot);
		
		if (value instanceof WrappedProtegeInstanceImpl) {
			value = ((WrappedProtegeInstanceImpl) value).getProtegeInstance();
		} 
		
		i.setDirectOwnSlotValue(protege_slot, value);
	}

	protected void addSlotValue(SlotEnum slot, Object value) {
		Slot protege_slot = mp.getSlot(slot);
		
		if (value instanceof WrappedProtegeInstanceImpl) {
			Instance protegeValue = ((WrappedProtegeInstanceImpl) value).getProtegeInstance();
			
			if (protegeValue == null) {
				throw new OntologyException("Failed to add for " + i.getName() + " slot " + protege_slot.getName() + " value: " + value + 
						". Probably " + value + " is not in the metaproject.");
			}
			
			i.addOwnSlotValue(protege_slot, protegeValue);			
		} else {		
			i.addOwnSlotValue(protege_slot, value);
		}		
	}


	@Override
    public boolean equals(Object o) {
		if (!(o instanceof WrappedProtegeInstanceImpl)) {
			return false;
		}
		WrappedProtegeInstanceImpl other = (WrappedProtegeInstanceImpl) o;
		return mp == other.mp && getProtegeInstance().equals(other.getProtegeInstance());
	}

	@Override
    public int hashCode() {
		return getProtegeInstance().hashCode();
	}
	
	
	public Collection<Instance> getProtegeCollection(Collection collection) {
		ArrayList<Instance> protegeColl = new ArrayList<Instance>();
		
		for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			
			if (o instanceof WrappedProtegeInstanceImpl) {
				WrappedProtegeInstanceImpl wpi = (WrappedProtegeInstanceImpl) o;
				protegeColl.add(wpi.getProtegeInstance());
			}			
		}
		
		return protegeColl;
	}

    public void localize(KnowledgeBase kb) {
        mp.localize(kb);
        LocalizeUtils.localize(i, kb);
    }
    
    @Override
    public String toString() {
        return "[" + cls + ": " + i.getName() + "]";
    }
	

}
