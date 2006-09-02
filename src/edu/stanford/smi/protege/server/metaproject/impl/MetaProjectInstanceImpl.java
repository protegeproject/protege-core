package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.File;
import java.io.Serializable;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.MetaProjectInstance;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class MetaProjectInstanceImpl implements MetaProjectInstance, Serializable {
  private static final long serialVersionUID = 8666270295698053695L;
  
  String name;
  String location;
  
  protected MetaProjectInstanceImpl(MetaProjectImpl mp, Instance pi) {
    location = (String) pi.getOwnSlotValue(SlotEnum.location.getSlot(mp));
    localizeLocation(location);
    name = (String) pi.getOwnSlotValue(SlotEnum.name.getSlot(mp));
  }

  public String getName() {
    return name;
  }

  public String getLocation() {
    return location;
  }
  
  private static String localizeLocation(String location) {
    if (File.separatorChar != '\\') {
        location = location.replace('\\', File.separatorChar);
    }
    return location;
  }
  
  
}