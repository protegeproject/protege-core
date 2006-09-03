package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.GroupInstance;
import edu.stanford.smi.protege.server.metaproject.UserInstance;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class GroupInstanceImpl implements GroupInstance {
  public static final String WORLD = "World";

  private String name;
  private Set<UserInstance> members;
  
  protected GroupInstanceImpl(MetaProjectImpl mp, Instance group) 
  throws OntologyException {
    if (!group.getDirectTypes().contains(ClsEnum.Group.getCls(mp))) {
      throw new IllegalArgumentException("" + group + " should be a group instance");
    }
    name = (String)  group.getOwnSlotValue(SlotEnum.name.getSlot(mp));
    
    if (name.equals(WORLD)) {
      members = mp.getUserInstances();
    } else {
      members   = new HashSet<UserInstance>();  
      for (Object user : group.getOwnSlotValues(SlotEnum.members.getSlot(mp))) {
        if (user instanceof Instance) {
          members.add(new UserInstanceImpl(mp, (Instance) user));
        }
      }
    }
  }

  public String getName() {
    return name;
  }

  public Set<UserInstance> getMembers() {
    return members;
  }
  
  public String toString() {
    return name;
  }
}
