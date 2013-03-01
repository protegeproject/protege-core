package edu.stanford.smi.protege.server;

public class DBServerNoFC_Test extends DBServer_Test {
  
  public void setUp() throws Exception {
    super.setUp();
    Server.getInstance().setFrameCalculatorDisabled(true);
  }

}
