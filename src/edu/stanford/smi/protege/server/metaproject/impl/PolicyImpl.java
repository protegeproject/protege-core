package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.HashSet;
import java.util.Set;

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
  
  private UserInstance normalizeUser(UserInstance user) {
    for (UserInstance normalizedUser : mp.getUserInstances()) {
      if (user.getName().equals(normalizedUser.getName())) {
        return normalizedUser;
      }
    }
    throw new IllegalArgumentException("Unknown user " + user);
  }

  /*
   * The project is ignored in this implementation.
   */
  public boolean isOperationAuthorized(UserInstance user, Operation op, MetaProjectInstance project) {
    user = normalizeUser(user);
    if (!getKnownOperations().contains(op)) {
      return true;
    }
    for (GroupInstance group  : user.getGroups()) {
      if (group.getAllowedOperations().contains(op)) {
        return true;
      }
    }
    return false;
  }

  /*
   * The project is ignored in this implementation.
   */
  public Set<Operation> getAllowedOperations(UserInstance user, MetaProjectInstance project) {
    user = normalizeUser(user);
    Set<Operation> allowed = new HashSet<Operation>();
    for (GroupInstance group  : user.getGroups()) {
      allowed.addAll(group.getAllowedOperations());
    }
    return allowed;
  }

  public Set<Operation> getKnownOperations() {
    return mp.getOperations();
  }

}
