package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Authorization;
import edu.stanford.smi.protege.server.metaproject.GroupInstance;
import edu.stanford.smi.protege.server.metaproject.MetaProjectInstance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.UserInstance;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class AuthorizationImpl implements Authorization, Serializable {
  private String                   name;    
  private boolean                  allowed;
  private int                      priority;
  private Set<UserInstance>        actors;
  private Set<Operation>           operations;
  private Set<MetaProjectInstance> projects;
  
  protected AuthorizationImpl(MetaProjectImpl mp, Instance auth) 
  throws OntologyException {
    if (!auth.getDirectTypes().contains(ClsEnum.Authorization.getCls(mp))) {
      throw new IllegalArgumentException("" + auth + " should be an authorization instance");
    }
    name     = (String)  auth.getOwnSlotValue(SlotEnum.name.getSlot(mp));
    allowed  = (Boolean) auth.getOwnSlotValue(SlotEnum.allow.getSlot(mp));
    priority = (Integer) auth.getOwnSlotValue(SlotEnum.priority.getSlot(mp));
   
    try {
      actors   = new HashSet<UserInstance>();
      for (Object userOrGroup : auth.getOwnSlotValues(SlotEnum.actor.getSlot(mp))) {
        if (((Instance) userOrGroup).getDirectTypes().contains(ClsEnum.Group.getCls(mp))) {
          GroupInstance group = new GroupInstanceImpl(mp, (Instance) userOrGroup);
          actors.addAll(group.getMembers());
        } else {
          actors.add(new UserInstanceImpl(mp, (Instance) userOrGroup));
        }
      }
    } catch (ClassCastException classCast) {
      throw new OntologyException("MetaOntology Problem: "
                                  + "actor, group, members slots should have user and group values", classCast);
    }
     
    try {
      operations = new HashSet<Operation>();
      for (Object operation : auth.getOwnSlotValues(SlotEnum.operation.getSlot(mp))) {
        operations.add(new OperationImpl(mp, (Instance) operation));
      }
    } catch (ClassCastException classCast) {
      throw new OntologyException("MetaOntology Problem: "
                                  + "operation slots should have operation instance values", classCast);
    }
      
    try {
      projects = new HashSet<MetaProjectInstance>();
      for (Object pi : auth.getOwnSlotValues(SlotEnum.project.getSlot(mp))) {
        projects.add(new MetaProjectInstanceImpl(mp, (Instance) pi));
      }
    } catch (ClassCastException classCast) {
      throw new OntologyException("MetaProject Ontology problem: " 
                                  + " the project slot should have project instances as values",
                                  classCast);
    }  
    if (projects.isEmpty()) {
      for (Instance pi : ClsEnum.Project.getCls(mp).getInstances()) {
        projects.add(new MetaProjectInstanceImpl(mp, pi));
      }
    }
  }

  public boolean isAllowed() {
    return allowed;
  }

  public int getPriority() {
    return priority;
  }

  public Set<UserInstance> getActors() {
    return actors;
  }

  public Set<Operation> getOperations() {
    return operations;
  }

  public Set<MetaProjectInstance> getProjects() {
    return projects;
  }
  
  public String toString() {
    return name;
  }

}
