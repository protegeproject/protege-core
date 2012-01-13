package edu.stanford.smi.protege.server.framestore;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import edu.stanford.smi.protege.exception.OntologyException;
import edu.stanford.smi.protege.exception.ProtegeError;
import edu.stanford.smi.protege.exception.ProtegeIOException;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.framestore.AbstractFrameStoreInvocationHandler;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculator;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.User;

public class LastUsageInvocationHandler extends AbstractFrameStoreInvocationHandler {
    private final MetaProject metaproject;
    private final Map<String, Date> lastAccessTimeMap = new HashMap<String, Date>();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
         public Thread newThread(Runnable r) {
             Thread th = new Thread(r, "Update MetaProject Thread");
             th.setDaemon(true);
             return th;
        }
    });
    private FrameCalculator frameCalculator;

    public LastUsageInvocationHandler(ProjectInstance projectInstance, FrameCalculator frameCalculator) {
        this.frameCalculator = frameCalculator;
        metaproject = projectInstance.getMetaProject();
        final long updateFrequency = ServerProperties.getMetaProjectLastAccessTimeUpdateFrequency();
        executor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                Map<String, Date> lastAccessTimeUpdates;
                synchronized (lastAccessTimeMap) {
                    lastAccessTimeUpdates = new HashMap<String, Date>(lastAccessTimeMap);
                    lastAccessTimeMap.clear();
                }
                for (Entry<String, Date> entry : lastAccessTimeUpdates.entrySet()) {
                    String userName = entry.getKey();
                    Date date = entry.getValue();
                    User u = metaproject.getUser(userName);
                    u.setLastAccess(date);
                }
            }
        }, updateFrequency, updateFrequency, TimeUnit.MILLISECONDS);
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
        if (frameCalculator != null && frameCalculator.inFrameCalculatorThread()) {
            return;
        }
        RemoteSession session = ServerFrameStore.getCurrentSession();
        if (session == null) {
            return;
        }
        Date now = new Date();
        String userName = session.getUserName();
        synchronized (lastAccessTimeMap) {
            lastAccessTimeMap.put(userName, now);
        }
    }

}
