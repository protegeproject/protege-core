package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class OperationImpl extends WrappedProtegeInstance implements Operation, Serializable {
  public final static OperationImpl READ = new OperationImpl("Read");
  public final static OperationImpl WRITE = new OperationImpl("Write");
  
  //TODO: Where should we move this, especiallt that this might be NCI specific?
  public final static OperationImpl PROPERTY_TAB_READ = new OperationImpl("PropertyTabRead");
  public final static OperationImpl PROPERTY_TAB_WRITE = new OperationImpl("PropertyTabWrite");

  public final static OperationImpl ONTOLOGY_TAB_READ = new OperationImpl("OntologyTabRead");
  public final static OperationImpl ONTOLOGY_TAB_WRITE = new OperationImpl("OntologyTabWrite");
  
  
  private String name;
  
  protected OperationImpl(MetaProjectImpl mp, Instance op) {
    super(mp, op, ClsEnum.Operation);
    name = (String) op.getOwnSlotValue(mp.getSlot(SlotEnum.name));
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
