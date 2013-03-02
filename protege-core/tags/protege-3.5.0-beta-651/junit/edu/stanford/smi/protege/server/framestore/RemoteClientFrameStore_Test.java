package edu.stanford.smi.protege.server.framestore;

import java.util.logging.Level;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStore_Test;
import edu.stanford.smi.protege.server.Server_Test;
import edu.stanford.smi.protege.util.Log;

public class RemoteClientFrameStore_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        String user = "Ray Fergerson";
        String password = "claudia";
        String host = "localhost";
        String projectName = "Newspaper";
        
        try {
          Server_Test.startServer();
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Exception setting up server - tests will fail", e);
            return null;
        }
        // Tests are a litle more deterministic if the disabled flag is turned on
        // Is there a way to make this happen without using a static? (problem = junit and fork)
        // FrameCalculator.setDisabled(true); 
        return new RemoteClientFrameStore(host, user, password, projectName, kb, false);
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
