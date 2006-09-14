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
    Project, User, Group, Operation, GroupOperation;
  }
  
  protected enum SlotEnum {
    name, password, location, group, member,  allowedGroup, allowedOperation, allowedGroupOperation;
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
  
  protected Cls getCls(ClsEnum cls) {
    return kb.getCls(cls.toString());
  }
  
  protected Slot getSlot(SlotEnum slot) {
    return kb.getSlot(slot.toString());
  }
  
  protected WrappedProtegeInstance wrapInstance(ClsEnum cls, Instance i) {
    if (!i.hasType(getCls(cls))) {
      throw new IllegalArgumentException("" + i + " should be a " + cls + " instance");
    }
    switch (cls) {
    case GroupOperation:
      return new GroupAndOperationImpl(this, i);
    case Group:
      return new GroupInstanceImpl(this, i);
    case Project:
      return new MetaProjectInstanceImpl(this, i);
    case Operation:
      return new OperationImpl(this, i);
    case User:
      return new UserInstanceImpl(this, i);
    default:
      throw new UnsupportedOperationException("Unexpected cls " + cls);
    }
  }
  
  @SuppressWarnings("unchecked")
  protected Set getWrappedInstances(ClsEnum cls) {
    Set instances = new HashSet();
    for (Instance i : kb.getInstances(getCls(cls))) {
      instances.add(wrapInstance(cls, i));
    }
    return instances;
  }
  
  @SuppressWarnings("unchecked")
  public Set<MetaProjectInstance> getProjectInstances() {
    return (Set<MetaProjectInstance>) getWrappedInstances(ClsEnum.Project);
  }
 
  @SuppressWarnings("unchecked")
  public Set<UserInstance> getUserInstances() {
    return (Set<UserInstance>) getWrappedInstances(ClsEnum.User);
  }
  
  @SuppressWarnings("unchecked")
  public Set<Operation> getOperations() {
    return (Set<Operation>) getWrappedInstances(ClsEnum.Operation);
  }
  
  public Policy getPolicy() {
    if (policy == null) {
      policy = new  PolicyImpl(this);
    }
    return policy;
  }
 
}
