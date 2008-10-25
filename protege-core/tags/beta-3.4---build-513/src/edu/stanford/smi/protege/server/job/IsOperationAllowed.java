package edu.stanford.smi.protege.server.job;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.util.ProtegeJob;


public class IsOperationAllowed extends ProtegeJob {
	private static final long serialVersionUID = -8541105695722714585L;
	
	private Operation operation;
	private String userName;
	
	public IsOperationAllowed(KnowledgeBase kb, Operation operation, String user) {
		super(kb);
		this.operation = operation;
		this.userName = user;
	}
		
	@Override
	public Object run() throws ProtegeException {
		Policy policy = Server.getPolicy();
	    User user = policy.getUserByName(userName);
	    if (user == null) {
	    	return Boolean.FALSE;
	    }
	    boolean allowed = policy.isOperationAuthorized(user, operation, getMetaProjectInstance());
	    return new Boolean(allowed);	    
	}

}
