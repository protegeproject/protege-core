package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.GroupInstance;
import edu.stanford.smi.protege.server.metaproject.UserInstance;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;
import edu.stanford.smi.protege.util.Log;

public class UserInstanceImpl implements UserInstance, Serializable {
  private static final long serialVersionUID = -4416984896523630762L;
  
  String name;
  String password;
  Set<GroupInstance> groups;
  
  protected UserInstanceImpl(MetaProjectImpl mp, Instance ui) {
    if (!ui.getDirectTypes().contains(ClsEnum.User.getCls(mp))) {
      throw new IllegalArgumentException("" + ui + " should be an user instance");
    }
    name = (String) ui.getOwnSlotValue(SlotEnum.name.getSlot(mp));
    password = (String) ui.getOwnSlotValue(SlotEnum.password.getSlot(mp));
    
    groups = new HashSet<GroupInstance>();
    for (Object instance : ui.getOwnSlotValues(SlotEnum.groups.getSlot(mp))) {
      try {
        groups.add(new GroupInstanceImpl(mp, (Instance) instance));
      } catch (ClassCastException cce) {
        Log.getLogger().warning("Metaproject invalid because the allowed Operations of a group should be instances of operations");
      }
    }
  }
  
  public UserInstanceImpl(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getPassword() {
    return password;
  }
  
  public Set<GroupInstance> getGroups() {
    return groups;
  }
  
  public boolean equals(Object o) { 
    if (!(o instanceof UserInstance)) {
      return false;
    }
    UserInstance other = (UserInstance) o;
    return name.equals(other.getName());
  }
  
  public int hashCode() {
    return name.hashCode();
  }
  
  public String toString() {
    return name;
  }
  
}
