package edu.stanford.smi.protege.server.framestore;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

import edu.stanford.smi.protege.exception.ModificationException;
import edu.stanford.smi.protege.model.framestore.AbstractFrameStoreInvocationHandler;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.User;

public class ReadAccessEnforcementFrameStore extends AbstractFrameStoreInvocationHandler {
    private ServerFrameStore serverFrameStore;
    private Collection<String> readOnlyUsers = new HashSet<String>();
    
    public ReadAccessEnforcementFrameStore(ServerFrameStore serverFrameStore) {
        this.serverFrameStore = serverFrameStore;
        loadPolicy();
    }

    public void loadPolicy() {
        MetaProject metaproject = Server.getInstance().getMetaProjectNew();
        Policy policy = Server.getPolicy();
        ProjectInstance project = serverFrameStore.getMetaProjectInstance();
        
        readOnlyUsers.clear();
        for (User user : metaproject.getUsers()) {
            if (!policy.isOperationAuthorized(user, MetaProjectConstants.OPERATION_WRITE, project)) {
                readOnlyUsers.add(user.getName());
            }
        }
    }
    
    public boolean isApplicable() {
        return !readOnlyUsers.isEmpty();
    }
    
    public Object handleInvoke(Method method, Object[] args) {
        RemoteSession session = ServerFrameStore.getCurrentSession();
        boolean readOnly = false;
        if (session != null) {
            readOnly = readOnlyUsers.contains(session.getUserName());
        }
        if (readOnly && method.getName().endsWith("Transaction")) {
            return Boolean.TRUE;
        }
        else if (readOnly && isModification(method)) {
            throw new ModificationException(method.getName());
        }
        else {
            return invoke(method, args);
        }
    }
    
    public void executeQuery(Query q, QueryCallback qc) {
      getDelegate().executeQuery(q, qc);
    }
}