package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.Authorization;
import edu.stanford.smi.protege.server.metaproject.MetaProjectInstance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.UserInstance;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.ClsEnum;
import edu.stanford.smi.protege.util.Log;

public class PolicyImpl implements Policy {
  private List<Authorization> authorizations;
  private List<Authorization> authorizationsReverseOrder;
  
  public PolicyImpl(MetaProjectImpl mp) {
    authorizations = new  ArrayList<Authorization>();
    for (Object auth : ClsEnum.Authorization.getCls(mp).getInstances()) {
      if (auth instanceof Instance) {
        authorizations.add(new AuthorizationImpl(mp, (Instance) auth));
      } else {
        Log.getLogger().warning("Object " + auth + " in " + ClsEnum.Authorization 
                                + " class is not an instnace.");
      }
    }
    Collections.sort(authorizations, new Comparator<Authorization>() {
      public int compare(Authorization a1, Authorization a2) {
        if (a1.getPriority() > a2.getPriority()) {
          return 1;
        }
        else if (a1.getPriority()  < a2.getPriority()) {
          return -1;
        } else {
          int allow1 = a1.isAllowed() ? 1 : 0;
          int allow2 = a2.isAllowed() ? 1 : 0;
          if (allow1 < allow2) {
            return 1;
          }
          else if (allow2 > allow1) {
            return -1;
          }
          else {
            return 0;
          }
        }
      }      
    });
    authorizationsReverseOrder = new ArrayList<Authorization>(authorizations);
    Collections.reverse(authorizationsReverseOrder);
  }

  public boolean isOperationAuthorized(UserInstance user, Operation op, MetaProjectInstance project) {
    for (Authorization auth : authorizationsReverseOrder) {
      if (auth.getActors().contains(user)  && auth.getOperations().contains(op) && auth.getProjects().contains(project)) {
        return auth.isAllowed();
      }
    }
    return false;
  }

  public Set<Operation> getAllowedOperations(UserInstance user, MetaProjectInstance project) {
    Set<Operation> allowedOps = new HashSet<Operation>();
    for (Authorization auth  : authorizations) {
      if (auth.getActors().contains(user) && auth.getProjects().contains(project)) {
        if (auth.isAllowed()) {
          allowedOps.addAll(auth.getOperations());
        } else  {
          allowedOps.removeAll(auth.getOperations());
        }
      }
    }
    return allowedOps;
  }

}
