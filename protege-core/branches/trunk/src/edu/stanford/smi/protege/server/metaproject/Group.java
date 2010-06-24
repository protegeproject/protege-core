package edu.stanford.smi.protege.server.metaproject;

import java.util.Collection;
import java.util.Set;


public interface Group extends PolicyControlledObject {

  String getName();

  Set<User> getMembers();

  String getDescription();

  void setDescription(String description);

  void setName(String name);

  void addMember(User member);

  void setMembers(Collection<User> members);

  MetaProject getMetaProject();
}
