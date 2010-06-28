package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.GroupOperation;
import edu.stanford.smi.protege.server.metaproject.PolicyControlledObject;

public class PolicyControlledObjectImpl extends WrappedProtegeInstanceWithPropsImpl implements PolicyControlledObject,
        Serializable {
    private static final long serialVersionUID = -6929685166227007784L;

    private String name;

    protected PolicyControlledObjectImpl(MetaProjectImpl mp, Instance pi) {
        this(mp, pi, MetaProjectImpl.ClsEnum.PolicyControlledObject);
    }

    protected PolicyControlledObjectImpl(MetaProjectImpl mp, Instance pi, MetaProjectImpl.ClsEnum cls) {
        super(mp, pi, cls);
        name = (String) pi.getOwnSlotValue(mp.getSlot(MetaProjectImpl.SlotEnum.name));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setSlotValue(MetaProjectImpl.SlotEnum.name, name);
    }

    public String getDescription() {
        Object value = getProtegeInstance().getOwnSlotValue(
                getMetaProject().getSlot(MetaProjectImpl.SlotEnum.description));
        if (value == null) {
            return "";
        }
        if (!(value instanceof String)) {
            throw new OntologyException("The " + MetaProjectImpl.SlotEnum.description
                    + " slot should take on string values");
        }
        return (String) value;
    }

    public void setDescription(String description) {
        setSlotValue(MetaProjectImpl.SlotEnum.description, description);
    }

    public void setAllowedGroupOperations(Collection<GroupOperation> groupOperations) {
        setSlotValuesAsProtegeInstances(MetaProjectImpl.SlotEnum.allowedGroupOperation, groupOperations);
    }

    public void addAllowedGroupOperations(GroupOperation groupOperation) {
        addSlotValue(MetaProjectImpl.SlotEnum.allowedGroupOperation, groupOperation);
    }

    @SuppressWarnings("unchecked")
    public Set<GroupOperation> getAllowedGroupOperations() {
        return getSlotValues(MetaProjectImpl.SlotEnum.allowedGroupOperation, MetaProjectImpl.ClsEnum.GroupOperation);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PolicyControlledObject)) {
            return false;
        }
        PolicyControlledObject other = (PolicyControlledObject) o;
        return name.equals(other.getName());
    }

    @Override
    public int hashCode() {
        return name == null ? 41 : name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

}
