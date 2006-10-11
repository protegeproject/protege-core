package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;

public interface MetaProjectInstance {
  
  String getName();
  
  String getLocation();
  
  Set<GroupAndOperation> getAllowedGroupOperations();
}
