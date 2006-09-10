package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.GroupInstance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;
import edu.stanford.smi.protege.util.Log;

public class GroupInstanceImpl implements GroupInstance {
  public static final String WORLD = "World";

  private String name;
  Set<Operation> operations;
  
  protected GroupInstanceImpl(MetaProjectImpl mp, Instance group) 
  throws OntologyException {
    if (!group.getDirectTypes().contains(ClsEnum.Group.getCls(mp))) {
      throw new IllegalArgumentException("" + group + " should be a group instance");
    }
    name = (String)  group.getOwnSlotValue(SlotEnum.name.getSlot(mp));
    
    operations = new HashSet<Operation>();
    for (Object instance : group.getOwnSlotValues(SlotEnum.allowedOperation.getSlot(mp))) {
      try {
        operations.add(new OperationImpl(mp, (Instance) instance));
      } catch (ClassCastException cce) {
        Log.getLogger().warning("Metaproject invalid because the allowed Operations of a group should be instances of operations");
      }
    }
  }

  public String getName() {
    return name;
  }
  
  public Set<Operation> getAllowedOperations() {
    return operations;
  }

  public boolean equals(Object o) {
    if (!(o instanceof GroupInstance))  {
      return false;
    }
    GroupInstance other = (GroupInstance) o;
    return name.equals(other.getName());
  }
  
  public int hashCode() {
    return name.hashCode();
  }
  
  public String toString() {
    return name;
  }
}
