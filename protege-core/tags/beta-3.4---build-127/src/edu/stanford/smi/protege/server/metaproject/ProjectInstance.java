package edu.stanford.smi.protege.server.metaproject;

import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;

public interface ProjectInstance {
  
  String getName();
  
  String getLocation();
  
  User getOwner();
  
  Set<GroupOperation> getAllowedGroupOperations();
  
  Instance getProtegeInstance();
  
  ProjectInstance getAnnotationProject();
  
  MetaProject getMetaProject();
  
  String getDescription();
  
  void setDescription(String description);
  
  void setName(String name);
  
  void setLocation(String location);
  
  void setAllowedGroupOperations(Collection<GroupOperation> groupOperations);
  
  void addAllowedGroupOperations(GroupOperation groupOperation);
  
  void setOwner(User owner);
  
  void setAnnotationProject(ProjectInstance annotationProject);
}
