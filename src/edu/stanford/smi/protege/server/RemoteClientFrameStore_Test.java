package edu.stanford.smi.protege.server;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

public class RemoteClientFrameStore_Test extends FrameStore_Test {

    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        String user = "Ray Fergerson";
        String password = "claudia";
        String host = "localhost";
        String projectName = "Newspaper";

        return new RemoteClientFrameStore(host, user, password, projectName, kb, false);
    }
}
