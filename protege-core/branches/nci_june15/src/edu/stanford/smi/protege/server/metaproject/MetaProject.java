package edu.stanford.smi.protege.server.metaproject;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;

public class MetaProject {
  private KnowledgeBase kb;
  
  private Cls projectCls;
  private Cls userCls;
  
  private Slot nameSlot;
  private Slot passwordSlot;
  private Slot locationSlot;
  
  public MetaProject(URI metaprojectURI) {
    Collection errors = new ArrayList();
    Project project = Project.loadProjectFromURI(metaprojectURI, errors);
    if (!errors.isEmpty()) {
        throw new RuntimeException(errors.iterator().next().toString());
    }
    kb = project.getKnowledgeBase();
    
    projectCls = kb.getCls("Project");
    userCls = kb.getCls("User");
    
    nameSlot = kb.getSlot("name");
    passwordSlot = kb.getSlot("password");
    locationSlot = kb.getSlot("location");
  }
  
  
  public Set<MetaProjectInstance> getProjectInstances() {
    Set<MetaProjectInstance> projectInstances = new HashSet<MetaProjectInstance>();
    for (Instance pi : kb.getInstances(projectCls)) {
      projectInstances.add(new MetaProjectInstanceImpl(pi));
    }
    return projectInstances;
  }
  
  private class MetaProjectInstanceImpl implements MetaProjectInstance {
    Instance pi;
    
    public MetaProjectInstanceImpl(Instance pi) {
      this.pi = pi;
    }

    public String getName() {
      return (String) pi.getOwnSlotValue(nameSlot);
    }

    public String getLocation() {
      String location = (String) pi.getOwnSlotValue(locationSlot);
      return localizeLocation(location);
    }
    
  }
  
  private static String localizeLocation(String location) {
    if (File.separatorChar != '\\') {
        location = location.replace('\\', File.separatorChar);
    }
    return location;
  }
  
  public Set<UserInstance> getUserInstances() {
    Set<UserInstance> userInstances = new HashSet<UserInstance>();
    for (Instance ui : kb.getInstances(userCls)) {
      userInstances.add(new UserInstanceImpl(ui));
    }
    return userInstances;
  }
  
  private class UserInstanceImpl implements UserInstance {
    Instance ui;
    
    public UserInstanceImpl(Instance ui) {
      this.ui = ui;
    }

    public String getName() {
      return (String) ui.getOwnSlotValue(nameSlot);
    }

    public String getPassword() {
      return (String) ui.getOwnSlotValue(passwordSlot);
    }
    
  }
}
