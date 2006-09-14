package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.Set;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.GroupAndOperation;
import edu.stanford.smi.protege.server.metaproject.GroupInstance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class GroupAndOperationImpl extends WrappedProtegeInstance implements GroupAndOperation {
  
  public GroupAndOperationImpl(MetaProjectImpl mp, Instance goi) {
    super(mp, goi, ClsEnum.GroupOperation);
  }

  public GroupInstance allowedGroup() {
    return (GroupInstance) getSlotValues(SlotEnum.allowedGroup, ClsEnum.Group).iterator().next();
  }

  @SuppressWarnings("unchecked")
  public Set<Operation> allowedOperations() {
    return (Set<Operation>) getSlotValues(SlotEnum.allowedOperation, ClsEnum.Operation);                                                        
  }
  
  public String toString() {
    return "[Operations For " + allowedGroup() + "]";
  }
}
