package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.UserInstance;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class UserInstanceImpl implements UserInstance, Serializable {
  private static final long serialVersionUID = -4416984896523630762L;
  
  String name;
  String password;
  
  protected UserInstanceImpl(MetaProjectImpl mp, Instance ui) {
    name = (String) ui.getOwnSlotValue(SlotEnum.name.getSlot(mp));
    password = (String) ui.getOwnSlotValue(SlotEnum.password.getSlot(mp));
  }

  public String getName() {
    return name;
  }

  public String getPassword() {
    return password;
  }
  
}
