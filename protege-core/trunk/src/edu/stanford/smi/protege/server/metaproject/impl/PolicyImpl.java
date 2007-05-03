package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.server.metaproject.GroupAndOperation;
import edu.stanford.smi.protege.server.metaproject.GroupInstance;
import edu.stanford.smi.protege.server.metaproject.MetaProjectInstance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.UserInstance;

public class PolicyImpl implements Policy {
  MetaProjectImpl mp;
  
  public PolicyImpl(MetaProjectImpl mp) {
    this.mp = mp;
  }
  
  private UserInstance fillFields(UserInstance user) {
    for (UserInstance realUser : mp.getUserInstances()) {
      if (user.getName().equals(realUser.getName())) {
        return realUser;
      }
    }
    throw new IllegalArgumentException("Unknown user " + user);
  }
  
  private MetaProjectInstance fillFields(MetaProjectInstance project) {
    for (MetaProjectInstance realProject : mp.getProjectInstances()) {
      if (realProject.getName().equals(project.getName())) {
        return realProject;
      }
    }
    throw new IllegalArgumentException("Unknown project " + project);
  }
  

  /*
   * The project is ignored in this implementation.
   */
  public boolean isOperationAuthorized(UserInstance user, 
                                       Operation op, 
                                       MetaProjectInstance project) {
    if (!getKnownOperations().contains(op)) {
      return true;
    }
    user = fillFields(user);
    project = fillFields(project);
    Set<GroupInstance> userGroups = user.getGroups();
    for (GroupAndOperation ga : project.getAllowedGroupOperations()) {
      if (userGroups.contains(ga.allowedGroup()) && ga.allowedOperations().contains(op)) {
        return true;
      }
    }
    return false;
  }

  /*
   * The project is ignored in this implementation.
   */
  public Set<Operation> getAllowedOperations(UserInstance user, MetaProjectInstance project) {
    Set<Operation> allowed = new HashSet<Operation>();
    user = fillFields(user);
    project = fillFields(project);
    Set<GroupInstance> userGroups = user.getGroups();
    for (GroupAndOperation ga : project.getAllowedGroupOperations()) {
      if (userGroups.contains(ga.allowedGroup())) {
        allowed.addAll(ga.allowedOperations());
      }
    }
    return allowed;
  }

  public Set<Operation> getKnownOperations() {
    return mp.getOperations();
  }

}
