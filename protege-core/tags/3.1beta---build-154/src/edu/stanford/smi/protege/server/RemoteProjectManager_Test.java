package edu.stanford.smi.protege.server;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.test.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class RemoteProjectManager_Test extends APITestCase {
    private static final String HOST = "localhost";
    private static final String USER = "Ray Fergerson";
    private static final String PASSWORD = "claudia";
    private static final String PROJECT_NAME = "Newspaper";

    public void testGetProject() {
        Project p = RemoteProjectManager.getInstance().getProject(HOST, USER, PASSWORD, PROJECT_NAME, true);
        assertNotNull(p);
        KnowledgeBase kb = p.getKnowledgeBase();
        Cls cls = kb.getCls("Editor");
        assertNotNull(cls);
        p.dispose();
    }

}
