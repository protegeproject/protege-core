package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.server.metaproject.MetaProject.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.MetaProject.SlotEnum;

public class GroupImpl extends WrappedProtegeInstanceImpl implements Group {

	protected GroupImpl(MetaProjectImpl mp, Instance group) 
	throws OntologyException {
		super(mp, group, ClsEnum.Group);

	}

	public String getName() throws OntologyException {
		Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(SlotEnum.name));
		if (!(value instanceof String)) {
			throw new OntologyException("The " + SlotEnum.name + " slot should take on string values");
		}
		return (String) value;
	}

	@SuppressWarnings("unchecked")
	public Set<User> getMembers() {
		return (Set<User>) getSlotValues(SlotEnum.member, ClsEnum.User);
	}

	public String toString() {
		return getName();
	}

	public void addMember(User member) {
		addSlotValue(SlotEnum.member, member);
	}

	public void setMembers(Collection<User> members) {
		setSlotValuesAsProtegeInstances(SlotEnum.member, members);
	}

	public void setName(String name) {
		setSlotValue(SlotEnum.name, name);
	}
	
	public String getDescription() {
		Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(SlotEnum.description));
		if (!(value instanceof String)) {
			throw new OntologyException("The " + SlotEnum.name + " slot should take on string values");
		}
		return (String) value;		
	}
	
	public void setDescription(String description) {
		setSlotValue(SlotEnum.description, description);
	}
}
