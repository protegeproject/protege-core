package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;

public interface Policy {
  
  public boolean isOperationAuthorized(UserInstance user, Operation op, MetaProjectInstance project);
  
  Set<Operation> getAllowedOperations(UserInstance user, MetaProjectInstance project);

}
