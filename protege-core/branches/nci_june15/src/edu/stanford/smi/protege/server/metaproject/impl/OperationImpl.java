package edu.stanford.smi.protege.server.metaproject.impl;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class OperationImpl implements Operation {
  String name;
  
  protected OperationImpl(MetaProjectImpl mp, Instance ui) {
    name = (String) ui.getOwnSlotValue(SlotEnum.name.getSlot(mp));
    
  }

  public String getName() {
    return name;
  }
}
