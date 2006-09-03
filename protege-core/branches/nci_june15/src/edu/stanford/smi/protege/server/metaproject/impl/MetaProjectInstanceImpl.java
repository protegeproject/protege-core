package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.File;
import java.io.Serializable;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.MetaProjectInstance;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class MetaProjectInstanceImpl implements MetaProjectInstance, Serializable {
  private static final long serialVersionUID = 8666270295698053695L;
  
  String name;
  String location;
  
  protected MetaProjectInstanceImpl(MetaProjectImpl mp, Instance pi) {
    if (!pi.getDirectTypes().contains(ClsEnum.Project.getCls(mp))) {
      throw new IllegalArgumentException("" + pi + " should be a project instance");
    }
    location = (String) pi.getOwnSlotValue(SlotEnum.location.getSlot(mp));
    localizeLocation(location);
    name = (String) pi.getOwnSlotValue(SlotEnum.name.getSlot(mp));
  }
  
  public MetaProjectInstanceImpl(String name) {
    this.name = name;
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
  
  public boolean equals(Object o) {
    if (!(o instanceof MetaProjectInstance)) {
      return false;
    }
    MetaProjectInstance other = (MetaProjectInstance) o;
    return name.equals(other.getName());
  }
  
  public int hashCode() {
    return name.hashCode();
  }
  
  public String toString() {
    return name;
  }
  
}