package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.MetaProject.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.MetaProject.SlotEnum;

public class WrappedProtegeInstanceImpl {
  MetaProjectImpl mp;
  private Instance i;
  private ClsEnum cls;
  
  public WrappedProtegeInstanceImpl() {
    
  }
  
  public WrappedProtegeInstanceImpl(MetaProjectImpl mp, Instance i, ClsEnum cls) {
    if (!i.hasType(mp.getCls(cls))) {
      throw new IllegalArgumentException("" + i + " should be of type " + cls);
    }
    this.i = i;
    this.cls = cls;
    this.mp = mp;
  }
  
  public MetaProjectImpl getMetaProject() {
    return mp;
  }
  
  public Instance getProtegeInstance() {
    return i;
  }

  public ClsEnum getCls() {
    return cls;
  }
  
  @SuppressWarnings("unchecked")
  protected Set getSlotValues(SlotEnum slot, ClsEnum rangeCls) {
    Set results = new HashSet();
    for (Object o : i.getOwnSlotValues(mp.getSlot(slot))) {
      results.add(mp.wrapInstance(rangeCls, (Instance) o));
    }
    return results;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof WrappedProtegeInstanceImpl)) {
      return false;
    }
    WrappedProtegeInstanceImpl other = (WrappedProtegeInstanceImpl) o;
    return mp == other.mp && getProtegeInstance().equals(other.getProtegeInstance());
  }
  
  public int hashCode() {
    return getProtegeInstance().hashCode();
  }
  
}
