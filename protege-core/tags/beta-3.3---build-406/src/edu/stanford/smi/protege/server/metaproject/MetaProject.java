package edu.stanford.smi.protege.server.metaproject;

import java.util.Collection;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;

public interface MetaProject {
  public enum ClsEnum {
    Project, User, Group, Operation, GroupOperation;
  }
  
  public enum SlotEnum {
    name, password, location, group, member,  allowedGroup, allowedOperation, allowedGroupOperation;
  }
  
  Cls getCls(ClsEnum cls);
  
  Slot getSlot(SlotEnum slot);
  
  Set<MetaProjectInstance> getProjectInstances();
  
  MetaProjectInstance getProjectInstance(String name);

  Set<UserInstance> getUserInstances();
  
  Set<Operation> getOperations();
  
  Policy getPolicy();
  
  KnowledgeBase getKnowledgeBase();
  
  MetaProjectInstance createMetaProjectInstance(String name);
  
  boolean save(Collection errors);
  
}


