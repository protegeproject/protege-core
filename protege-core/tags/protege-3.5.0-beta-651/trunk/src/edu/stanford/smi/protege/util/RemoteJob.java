package edu.stanford.smi.protege.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.User;

public abstract class RemoteJob {
    private Logger log = Log.getLogger(RemoteJob.class);
    
    /**
     * This class is overridden by the sub class and defines 
     * the fuunctionality of this job.  The intention is that the
     * sub-class will define this method and callers will use the 
     * execute() method.
     * 
     * @return 
     * @throws ProtegeException
     */
    public abstract Object run() throws ProtegeException;

    public abstract void fixLoader();
    
    public boolean serverSideCheckOperationAllowed(Operation op, ProjectInstance projectInstance) {
        Policy policy = Server.getPolicy();
        String userName = ServerFrameStore.getCurrentSession().getUserName();
        User user = policy.getUserByName(userName);
        boolean allowed = policy.isOperationAuthorized(user, op, projectInstance);
        if (!allowed) {
            log.warning("User " + userName + " attempted the operation " + op);
            log.warning("Permission denied");
        }
        return allowed;
    }
}
