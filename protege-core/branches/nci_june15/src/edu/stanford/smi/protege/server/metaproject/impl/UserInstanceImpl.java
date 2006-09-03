package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.UserInstance;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class UserInstanceImpl implements UserInstance, Serializable {
  private static final long serialVersionUID = -4416984896523630762L;
  
  String name;
  String password;
  
  protected UserInstanceImpl(MetaProjectImpl mp, Instance ui) {
    if (!ui.getDirectTypes().contains(ClsEnum.User.getCls(mp))) {
      throw new IllegalArgumentException("" + ui + " should be an user instance");
    }
    name = (String) ui.getOwnSlotValue(SlotEnum.name.getSlot(mp));
    password = (String) ui.getOwnSlotValue(SlotEnum.password.getSlot(mp));
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
