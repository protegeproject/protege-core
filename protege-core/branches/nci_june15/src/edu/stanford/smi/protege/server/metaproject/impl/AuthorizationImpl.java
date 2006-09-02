package edu.stanford.smi.protege.server.metaproject.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Authorization;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.UserInstance;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;

public class AuthorizationImpl implements Authorization, Serializable {
  boolean           allowed;
  int               priority;
  Set<UserInstance> actors;
  
  protected AuthorizationImpl(MetaProjectImpl mp, Instance auth) 
  throws OntologyException {
    allowed  = (Boolean) auth.getOwnSlotValue(SlotEnum.allow.getSlot(mp));
    priority = (Integer) auth.getOwnSlotValue(SlotEnum.priority.getSlot(mp));
    
    actors   = new HashSet<UserInstance>();
    try {
      for (Object userOrGroup : auth.getOwnSlotValues(SlotEnum.actor.getSlot(mp))) {
        if (((Instance) userOrGroup).getDirectTypes().contains(ClsEnum.group.getCls(mp))) {
          for (Object user : ((Instance) userOrGroup).getOwnSlotValues(SlotEnum.members.getSlot(mp))) {
            actors.add(new UserInstanceImpl(mp, (Instance) user));
          }
        } else {
          actors.add(new UserInstanceImpl(mp, (Instance) userOrGroup));
        }
      }
      
      
    } catch (ClassCastException classCast) {
      OntologyException oe = new OntologyException("MetaOntology Problem: " +
                                                   "actor, group and members slots should have user and group values");
      oe.initCause(classCast);
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
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public Set<MetaProject> getProjects() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
