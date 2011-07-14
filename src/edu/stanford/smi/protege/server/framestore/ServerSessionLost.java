package edu.stanford.smi.protege.server.framestore;

import java.rmi.RemoteException;

public class ServerSessionLost extends RemoteException {
  private static final long serialVersionUID = 6920815385470039048L;

public ServerSessionLost() { }
  
  public ServerSessionLost(String msg)  {
    super(msg);
  }

}
