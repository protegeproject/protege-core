package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;

public interface Authorization {
  
  boolean isAllowed();
  
  int getPriority();
  
  Set<UserInstance> getActors();
  
  Set<Operation> getOperations();

  Set<MetaProject> getProjects();
}
