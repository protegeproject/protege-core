package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.server.metaproject.GroupOperation;
import edu.stanford.smi.protege.server.metaproject.PolicyControlledObject;
import edu.stanford.smi.protege.server.metaproject.PolicyControlledObjectCollection;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class PolicyControlledObjectImpl extends WrappedProtegeInstanceWithPropsImpl implements PolicyControlledObject,
        Serializable {

    private static final long serialVersionUID = 881458997973667317L;

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

    public boolean isPolicyModifiableByClient() {
        return (Boolean) getSlotValue(MetaProjectImpl.SlotEnum.policyModifiableByClient, null);
    }

    public void setPolicyModifiableByClient(boolean modifiable) {
        setSlotValue(MetaProjectImpl.SlotEnum.policyModifiableByClient, modifiable);
    }


    public void setAllowedGroupOperations(Collection<GroupOperation> groupOperations) {
        Collection<GroupOperation> oldGroupOperations = getAllowedGroupOperations();

        setSlotValuesAsProtegeInstances(MetaProjectImpl.SlotEnum.allowedGroupOperation, groupOperations);

        for (GroupOperation oldGroupOperation : oldGroupOperations) {
            Instance groupOperationInst = ((WrappedProtegeInstanceImpl)oldGroupOperation).getProtegeInstance();
            if (canDelete(groupOperationInst)) {
                groupOperationInst.delete();
            }
        }
    }

    private boolean canDelete(Instance groupOperation) {
        Collection<Reference> references = groupOperation.getReferences();
        for (Iterator<Reference> iterator = references.iterator(); iterator.hasNext();) {
            Reference ref = iterator.next();
            if (ref.getSlot().equals(getMetaProject().getSlot(SlotEnum.allowedGroupOperation))) {
                return false;
            }
        }
        return true;
    }

    public void addAllowedGroupOperations(GroupOperation groupOperation) {
        addSlotValue(MetaProjectImpl.SlotEnum.allowedGroupOperation, groupOperation);
    }

    @SuppressWarnings("unchecked")
    public Set<GroupOperation> getAllowedGroupOperations() {
        return getSlotValues(MetaProjectImpl.SlotEnum.allowedGroupOperation, MetaProjectImpl.ClsEnum.GroupOperation);
    }


    @Override
    public String toString() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public Set<PolicyControlledObjectCollection> getInCollection() {
        return getSlotValues(MetaProjectImpl.SlotEnum.inCollection, MetaProjectImpl.ClsEnum.PolicyControlledObjectCollection);
    }

    public void addInCollection(PolicyControlledObjectCollection collection) {
        addSlotValue(MetaProjectImpl.SlotEnum.inCollection, collection);
    }

    public void removeInCollection(PolicyControlledObjectCollection collection) {
        removeSlotValue(MetaProjectImpl.SlotEnum.inCollection, collection);
    }

    public void setInCollection(Set<PolicyControlledObjectCollection> collections) {
        setSlotValue(MetaProjectImpl.SlotEnum.inCollection, collections);
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


}
