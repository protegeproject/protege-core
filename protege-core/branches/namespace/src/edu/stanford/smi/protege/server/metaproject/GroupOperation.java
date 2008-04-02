package edu.stanford.smi.protege.server.metaproject;

import java.util.Collection;
import java.util.Set;

public interface GroupOperation {
  Group getAllowedGroup();

  Set<Operation> getAllowedOperations();
  
  void setAllowedGroup(Group group);
  
  void setAllowedOperations(Collection<Operation> operations);
  
  void addAllowedOperation(Operation operation);
}
