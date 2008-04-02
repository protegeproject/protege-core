package edu.stanford.smi.protege.server.metaproject.impl;

import java.util.HashSet;
import java.util.Set;

import sun.security.jca.GetInstance;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.server.metaproject.GroupInstance;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.UserInstance;
import edu.stanford.smi.protege.server.metaproject.MetaProject.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.MetaProject.SlotEnum;
import edu.stanford.smi.protege.util.Log;

public class GroupInstanceImpl extends WrappedProtegeInstanceImpl implements GroupInstance {
  public static final String WORLD = "World";
  
  protected GroupInstanceImpl(MetaProjectImpl mp, Instance group) 
  throws OntologyException {
    super(mp, group, ClsEnum.Group);
 
  }

  public String getName() throws OntologyException {
    Object value = getProtegeInstance().getOwnSlotValue(getMetaProject().getSlot(SlotEnum.name));
    if (!(value instanceof String)) {
      throw new OntologyException("The " + SlotEnum.name + " slot should take on string values");
    }
    return (String) value;
  }
  
  @SuppressWarnings("unchecked")
  public Set<UserInstance> getMembers() {
    return (Set<UserInstance>) getSlotValues(SlotEnum.member, ClsEnum.User);
  }
  
  public String toString() {
    return getName();
  }
}
