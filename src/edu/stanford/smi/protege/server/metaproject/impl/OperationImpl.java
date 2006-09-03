package edu.stanford.smi.protege.server.metaproject.impl;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class OperationImpl implements Operation {
  public final static OperationImpl READ = new OperationImpl("READ");
  public final static OperationImpl EDIT = new OperationImpl("EDIT");
  
  private String name;
  
  protected OperationImpl(MetaProjectImpl mp, Instance op) {
    if (!op.getDirectTypes().contains(ClsEnum.Operation.getCls(mp))) {
      throw new IllegalArgumentException("" + op + " should be an authorization instance");
    }
    name = (String) op.getOwnSlotValue(SlotEnum.name.getSlot(mp));
  }
  
  public OperationImpl(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof Operation))  {
      return false;
    }
    Operation other = (Operation) o;
    return name.equals(other.getName());
  }
  
  public int hashCode() {
    return name.hashCode();
  }
  
  public String toString() {
    return name;
  }
}
