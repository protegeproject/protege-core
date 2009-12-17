package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.GroupOperation;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.PropertyValue;

public class GroupOperationImpl extends WrappedProtegeInstanceImpl implements GroupOperation {
    private static final long serialVersionUID = -3306223096316205528L;

    public GroupOperationImpl(MetaProjectImpl mp, Instance goi) {
		super(mp, goi, MetaProjectImpl.ClsEnum.GroupOperation);
	}

	public Group getAllowedGroup() {
		return (Group) getSlotValue(MetaProjectImpl.SlotEnum.allowedGroup, MetaProjectImpl.ClsEnum.Group);
	}

	@SuppressWarnings("unchecked")
	public Set<Operation> getAllowedOperations() {
		return getSlotValues(MetaProjectImpl.SlotEnum.allowedOperation, MetaProjectImpl.ClsEnum.Operation);                                                        
	}

	@Override
    public String toString() {
		return "[Operations For " + getAllowedGroup() + "]";
	}

	public void addAllowedOperation(Operation operation) {
		addSlotValue(MetaProjectImpl.SlotEnum.allowedOperation, operation);
	}

	public void setAllowedOperations(Collection<Operation> operations) {
		setSlotValuesAsProtegeInstances(MetaProjectImpl.SlotEnum.allowedOperation, operations);
	}

	public void setAllowedGroup(Group group) {
		setSlotValue(MetaProjectImpl.SlotEnum.allowedGroup, group);
	}
	
	@SuppressWarnings("unchecked")
	public Set<PropertyValue> getPropertyValues() {
		return getSlotValues(MetaProjectImpl.SlotEnum.properties, MetaProjectImpl.ClsEnum.PropertyValue);
	}

	public void setPropertyValues(Collection<PropertyValue> propertyValues) {
		setSlotValuesAsProtegeInstances(MetaProjectImpl.SlotEnum.properties, propertyValues);		
	}
	
	 public void addPropertyValue(PropertyValue propertyValue) {
    	 addSlotValue(MetaProjectImpl.SlotEnum.properties, propertyValue);		
	}
    
}
