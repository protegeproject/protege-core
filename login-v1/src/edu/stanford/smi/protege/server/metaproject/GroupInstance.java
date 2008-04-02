package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;



public interface GroupInstance {
  
  String getName();
  
  Set<UserInstance> getMembers();
}
