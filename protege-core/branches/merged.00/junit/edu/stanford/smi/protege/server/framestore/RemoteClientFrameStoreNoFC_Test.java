package edu.stanford.smi.protege.server.framestore;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.server.Server;

public class RemoteClientFrameStoreNoFC_Test extends
    RemoteClientFrameStore_Test {
  
  protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
    FrameStore fs = super.createFrameStore(kb);
    Server.getInstance().setFrameCalculatorDisabled(true);
    return fs;
  }

}
