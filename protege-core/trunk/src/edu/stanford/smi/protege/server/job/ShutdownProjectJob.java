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

    public ShutdownProjectJob(KnowledgeBase kb) {
        super(kb);
    }

    @Override
    public Object run() throws ProtegeException {
        if (!serverSideCheckOperationAllowed(MetaProjectConstants.OPERATION_CONFIGURE_SERVER)) {
            return null;
        }
        Server server = Server.getInstance();
        String projectName = getMetaProjectInstance().getName();
        server.closeProject(projectName);
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