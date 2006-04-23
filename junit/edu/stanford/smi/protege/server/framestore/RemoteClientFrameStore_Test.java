package edu.stanford.smi.protege.server.framestore;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStore_Test;
import edu.stanford.smi.protege.server.Server_Test;

public class RemoteClientFrameStore_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        String user = "Ray Fergerson";
        String password = "claudia";
        String host = "localhost";
        String projectName = "Newspaper";
        
        try {
          Server_Test.startServer();
        } catch (Exception e) {
          return null;
        }

        return new RemoteClientFrameStore(host, user, password, projectName, kb, false);
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
