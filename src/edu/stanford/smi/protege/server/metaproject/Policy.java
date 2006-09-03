package edu.stanford.smi.protege.server.metaproject;

public interface Policy {
  
  public boolean isOperationAuthorized(UserInstance user, Operation op);

}
