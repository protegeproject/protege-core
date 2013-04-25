package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;


public interface ProjectInstance extends PolicyControlledObject {

  String getLocation();

  User getOwner();

  Set<User> getOwners();

  ProjectInstance getAnnotationProject();

  void setLocation(String location);

  void setOwner(User owner);

  void setOwners(Set<User> owners);

  void setAnnotationProject(ProjectInstance annotationProject);
}
