package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.GroupInstance;
import edu.stanford.smi.protege.server.metaproject.UserInstance;
import edu.stanford.smi.protege.server.metaproject.MetaProject.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.MetaProject.SlotEnum;

public class UserInstanceImpl extends WrappedProtegeInstanceImpl implements UserInstance, Serializable {
  private static final long serialVersionUID = -4416984896523630762L;
  
  private String name;
  private String password;
  private Set<GroupInstance> groups;

  
  @SuppressWarnings("unchecked")
  protected UserInstanceImpl(MetaProjectImpl mp, Instance ui) {
    super(mp, ui, ClsEnum.User);
    name = (String) ui.getOwnSlotValue(mp.getSlot(SlotEnum.name));
    password = (String) ui.getOwnSlotValue(mp.getSlot(SlotEnum.password));
    groups = (Set<GroupInstance>) getSlotValues(SlotEnum.group, ClsEnum.Group);
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
