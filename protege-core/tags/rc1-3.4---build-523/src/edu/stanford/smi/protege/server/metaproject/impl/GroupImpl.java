package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.User;

public class GroupImpl extends WrappedProtegeInstanceImpl implements Group {
    private static final long serialVersionUID = -6623180400376787848L;

    protected GroupImpl(MetaProjectImpl mp, Instance group) 
	throws OntologyException {
		super(mp, group, MetaProjectImpl.ClsEnum.Group);

	}

	public String getName() throws OntologyException {
		Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(MetaProjectImpl.SlotEnum.name));
		if (!(value instanceof String)) {
			throw new OntologyException("The " + MetaProjectImpl.SlotEnum.name + " slot should take on string values");
		}
		return (String) value;
	}

	@SuppressWarnings("unchecked")
	public Set<User> getMembers() {
		return getSlotValues(MetaProjectImpl.SlotEnum.member, MetaProjectImpl.ClsEnum.User);
	}

	@Override
    public String toString() {
		return getName();
	}

	public void addMember(User member) {
		addSlotValue(MetaProjectImpl.SlotEnum.member, member);
	}

	public void setMembers(Collection<User> members) {
		setSlotValuesAsProtegeInstances(MetaProjectImpl.SlotEnum.member, members);
	}

	public void setName(String name) {
		setSlotValue(MetaProjectImpl.SlotEnum.name, name);
	}
	
	public String getDescription() {
		Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(MetaProjectImpl.SlotEnum.description));
		if (!(value instanceof String)) {
			throw new OntologyException("The " + MetaProjectImpl.SlotEnum.name + " slot should take on string values");
		}
		return (String) value;		
	}
	
	public void setDescription(String description) {
		setSlotValue(MetaProjectImpl.SlotEnum.description, description);
	}
}
