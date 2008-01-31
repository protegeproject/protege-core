package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.GroupOperation;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.MetaProject.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.MetaProject.SlotEnum;

public class GroupOperationImpl extends WrappedProtegeInstanceImpl implements GroupOperation {
    private static final long serialVersionUID = -3306223096316205528L;

    public GroupOperationImpl(MetaProjectImpl mp, Instance goi) {
		super(mp, goi, ClsEnum.GroupOperation);
	}

	public Group getAllowedGroup() {
		return (Group) getSlotValue(SlotEnum.allowedGroup, ClsEnum.Group);
	}

	@SuppressWarnings("unchecked")
	public Set<Operation> getAllowedOperations() {
		return getSlotValues(SlotEnum.allowedOperation, ClsEnum.Operation);                                                        
	}

	@Override
    public String toString() {
		return "[Operations For " + getAllowedGroup() + "]";
	}

	public void addAllowedOperation(Operation operation) {
		addSlotValue(SlotEnum.allowedOperation, operation);
	}

	public void setAllowedOperations(Collection<Operation> operations) {
		setSlotValuesAsProtegeInstances(SlotEnum.allowedOperation, operations);
	}

	public void setAllowedGroup(Group group) {
		setSlotValue(SlotEnum.allowedGroup, group);
	}
}
