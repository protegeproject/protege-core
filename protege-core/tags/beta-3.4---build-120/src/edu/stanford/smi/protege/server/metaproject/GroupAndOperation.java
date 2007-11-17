package edu.stanford.smi.protege.server.metaproject;

import java.util.Set;

public interface GroupAndOperation {
  GroupInstance allowedGroup();

  Set<Operation> allowedOperations();
}
