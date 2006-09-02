package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;

public interface MetaProject {
  
  Set<MetaProjectInstance> getProjectInstances();

  Set<UserInstance> getUserInstances();
  
}
