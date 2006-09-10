package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class OperationImpl implements Operation, Serializable {
  public final static OperationImpl READ = new OperationImpl("Read");
  public final static OperationImpl WRITE = new OperationImpl("Write");
  
  private String name;
  
  protected OperationImpl(MetaProjectImpl mp, Instance op) {
    if (!op.hasType(ClsEnum.Operation.getCls(mp))) {
      throw new IllegalArgumentException("" + op + " should be an operation instance");
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
