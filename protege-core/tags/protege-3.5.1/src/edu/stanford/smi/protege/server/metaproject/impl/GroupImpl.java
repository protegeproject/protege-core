package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.User;

public class GroupImpl extends PolicyControlledObjectImpl implements Group {
    private static final long serialVersionUID = -6623180400376787848L;

    protected GroupImpl(MetaProjectImpl mp, Instance group)
	throws OntologyException {
		super(mp, group, MetaProjectImpl.ClsEnum.Group);

	}

	@SuppressWarnings("unchecked")
	public Set<User> getMembers() {
		return getSlotValues(MetaProjectImpl.SlotEnum.member, MetaProjectImpl.ClsEnum.User);
	}


	public void addMember(User member) {
		addSlotValue(MetaProjectImpl.SlotEnum.member, member);
	}

	public void setMembers(Collection<User> members) {
		setSlotValuesAsProtegeInstances(MetaProjectImpl.SlotEnum.member, members);
	}

	@Override
    public String getDescription() {
		Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(MetaProjectImpl.SlotEnum.description));
		if (!(value instanceof String)) {
			throw new OntologyException("The " + MetaProjectImpl.SlotEnum.name + " slot should take on string values");
		}
		return (String) value;
	}
}
