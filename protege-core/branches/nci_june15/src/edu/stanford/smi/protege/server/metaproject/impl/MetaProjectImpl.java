package edu.stanford.smi.protege.server.metaproject.impl;

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
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.MetaProjectInstance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.UserInstance;

public class MetaProjectImpl implements MetaProject {
  private KnowledgeBase kb;
  private Policy policy;
  
  protected enum ClsEnum {
    Project, User, Group, Operation;
    
    public Cls getCls(MetaProjectImpl mp) {
      return mp.kb.getCls(toString());
    }
  }
  
  protected enum SlotEnum {
    name, password, location, groups, allowedOperation;
    
    public Slot getSlot(MetaProjectImpl mp) {
      return mp.kb.getSlot(toString());
    }
  }
  
  public MetaProjectImpl(URI metaprojectURI) {
    Collection errors = new ArrayList();
    Project project = Project.loadProjectFromURI(metaprojectURI, errors);
    if (!errors.isEmpty()) {
        throw new RuntimeException(errors.iterator().next().toString());
    }
    kb = project.getKnowledgeBase();
  }
  
  protected KnowledgeBase getKnowledgeBase() {
    return kb;
  }
  
  public Set<MetaProjectInstance> getProjectInstances() {
    Set<MetaProjectInstance> projectInstances = new HashSet<MetaProjectInstance>();
    for (Instance pi : kb.getInstances(ClsEnum.Project.getCls(this))) {
      projectInstances.add(new MetaProjectInstanceImpl(this, pi));
    }
    return projectInstances;
  }
 
  public Set<UserInstance> getUserInstances() {
    Set<UserInstance> userInstances = new HashSet<UserInstance>();
    for (Instance ui : kb.getInstances(ClsEnum.User.getCls(this))) {
      userInstances.add(new UserInstanceImpl(this, ui));
    }
    return userInstances;
  }
  
  public Set<Operation> getOperations() {
    Set<Operation> operations = new HashSet<Operation>();
    for (Instance ui : kb.getInstances(ClsEnum.Operation.getCls(this))) {
      operations.add(new OperationImpl(this, ui));
    }
    return operations;
  }
  
  public Policy getPolicy() {
    if (policy == null) {
      policy = new  PolicyImpl(this);
    }
    return policy;
  }
 
}
