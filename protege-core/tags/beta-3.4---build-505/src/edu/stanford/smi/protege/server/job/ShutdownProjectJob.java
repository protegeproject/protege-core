/**
 * 
 */
package edu.stanford.smi.protege.server.job;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;

public class ShutdownProjectJob extends ProtegeJob {
    private static final long serialVersionUID = -4989229809698939487L;
    
    private String projectToKill;

    public ShutdownProjectJob(KnowledgeBase kb, String projectName) {
        super(kb);
        projectToKill = projectName;
    }

    @Override
    public Object run() throws ProtegeException {
        Server server = Server.getInstance();
        if (!serverSideCheckOperationAllowed(MetaProjectConstants.OPERATION_CONFIGURE_SERVER)) {
            return null;
        }
        server.closeProject(projectToKill);
        return null;
    }
    
    @Override
    public Object execute() throws ProtegeException {
        try {
            super.execute();
            throw new ProtegeException("Didn't the server project die?");
        }
        catch (NullPointerException expectedBecauseServerIsDead) {
            Log.emptyCatchBlock(expectedBecauseServerIsDead);
        }
        return null;
    }
    
    
}