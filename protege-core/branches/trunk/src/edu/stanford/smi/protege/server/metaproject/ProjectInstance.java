package edu.stanford.smi.protege.server.metaproject;


public interface ProjectInstance extends PolicyControlledObject {
    
  String getLocation();
  
  User getOwner();  
  
  ProjectInstance getAnnotationProject();
    
  void setLocation(String location);  
  
  void setOwner(User owner);
  
  void setAnnotationProject(ProjectInstance annotationProject);
}
