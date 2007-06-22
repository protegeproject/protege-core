package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;

import edu.stanford.smi.protege.model.Instance;

public interface MetaProjectInstance {
  
  String getName();
  
  String getLocation();
  
  Set<GroupAndOperation> getAllowedGroupOperations();
  
  Instance getProtegeInstance();
  
  MetaProject getMetaProject();
  
  void setName(String name);
  
  void setLocation(String location);
}
