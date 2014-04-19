package edu.stanford.smi.protege.server.metaproject.impl;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.PolicyControlledObjectCollection;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;


public class PolicyControlledObjectCollectionImpl extends WrappedProtegeInstanceImpl implements PolicyControlledObjectCollection {

    private static final long serialVersionUID = -8122490272670961493L;

    public PolicyControlledObjectCollectionImpl(MetaProjectImpl mp, Instance i) {
        super(mp, i, ClsEnum.PolicyControlledObjectCollection);
    }

    public String getName() {
        return (String) getProtegeInstance().getOwnSlotValue(mp.getSlot(MetaProjectImpl.SlotEnum.name));
    }

    public void setName(String name) {
        getProtegeInstance().setOwnSlotValue(mp.getSlot(MetaProjectImpl.SlotEnum.name), name);
    }

}
