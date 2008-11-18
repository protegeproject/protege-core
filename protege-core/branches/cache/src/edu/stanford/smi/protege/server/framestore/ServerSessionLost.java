package edu.stanford.smi.protege.server.framestore;

import java.rmi.RemoteException;

public class ServerSessionLost extends RemoteException {
  public ServerSessionLost() { }
  
  public ServerSessionLost(String msg)  {
    super(msg);
  }

}
