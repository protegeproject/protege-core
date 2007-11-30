package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;

public interface User {
  
  String getName();
  
  String getPassword();
  
  Set<Group> getGroups();
  
  String getDescription();
  
  void setDescription(String description);
  
  void setName(String name);
  
  void setPassword(String password);

}
