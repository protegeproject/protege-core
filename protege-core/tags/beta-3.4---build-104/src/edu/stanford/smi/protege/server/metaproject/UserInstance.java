package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;

public interface UserInstance {
  
  String getName();
  
  String getPassword();
  
  Set<GroupInstance> getGroups();
  
  void setName(String name);
  
  void setPassword(String password);

}
