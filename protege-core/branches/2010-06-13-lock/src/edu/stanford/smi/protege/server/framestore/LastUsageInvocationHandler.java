package edu.stanford.smi.protege.server.framestore;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.exception.ProtegeError;
import edu.stanford.smi.protege.exception.ProtegeIOException;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.framestore.AbstractFrameStoreInvocationHandler;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.User;

public class LastUsageInvocationHandler extends AbstractFrameStoreInvocationHandler {
    public static long ACCESS_TIME_GRANULARITY = 15 * 1000;
    private ProjectInstance projectInstance;
    private MetaProject metaproject;
    private Map<User, Date> lastAccessTimeMap = new ConcurrentHashMap<User, Date>();
    
    public LastUsageInvocationHandler(ProjectInstance projectInstance) {
        this.projectInstance = projectInstance;
        metaproject = projectInstance.getMetaProject();
    }

    @Override
    protected void executeQuery(Query q, final QueryCallback qc) {
        updateLastAccessTime();
        QueryCallback outerCallback = new QueryCallback() {
            
            public void provideQueryResults(Collection<Frame> results) {
                updateLastAccessTime();
                qc.provideQueryResults(results);
            }
            
            public void handleError(ProtegeError error) {
                updateLastAccessTime();
                qc.handleError(error);
            }
            
            public void handleError(ProtegeIOException exception) {
                updateLastAccessTime();
                qc.handleError(exception);
            }
            
            public void handleError(OntologyException exception) {
                updateLastAccessTime();
                qc.handleError(exception);
            }
        };
        getDelegate().executeQuery(q, outerCallback);
    }

    @Override
    protected Object handleInvoke(Method method, Object[] args) {
        if (!method.getName().equals("getEvents")) {
        	updateLastAccessTime();
        }
        return invoke(method, args);
    }
    
    private void updateLastAccessTime() {
        RemoteSession session = ServerFrameStore.getCurrentSession();
        if (session == null) {
            return;
        }
        final User u = metaproject.getUser(session.getUserName());
        final Date now = new Date();
        Date then = lastAccessTimeMap.get(u);
        if (then == null || now.getTime() > then.getTime() + ACCESS_TIME_GRANULARITY) {
        	u.setLastAccess(now);
        	lastAccessTimeMap.put(u, now);
        }
    }

}
