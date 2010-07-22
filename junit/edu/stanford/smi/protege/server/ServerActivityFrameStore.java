package edu.stanford.smi.protege.server;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.AbstractFrameStoreInvocationHandler;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculator;
import edu.stanford.smi.protege.util.Log;

public class ServerActivityFrameStore extends
        AbstractFrameStoreInvocationHandler {
    private static transient Logger log = Log.getLogger(ServerActivityFrameStore.class);
    
    private boolean serverActive = false;
    private KnowledgeBase kb;
    private FrameCalculator frameCalculator;
    
    public ServerActivityFrameStore(KnowledgeBase kb) {
        this.kb = kb;
    }
    
    private FrameCalculator getFrameCalculator() {
        if (frameCalculator == null) {
            frameCalculator = FrameCalculator.getFrameCalculator(kb);
        }
        return frameCalculator;
    }
    
    public void clearServerActivity() {
        if (log.isLoggable(Level.FINE)) {
            log.fine("server activity reset");
        }
        serverActive = false;
    }
    
    public boolean serverInvoked() {
        return serverActive;
    }
    

    @Override
    protected void executeQuery(Query q, QueryCallback qc) {
        getDelegate().executeQuery(q, qc);
    }

    @Override
    protected Object handleInvoke(Method method, Object[] args) {
        if (!method.getName().equals("getEvents") && 
                (getFrameCalculator() == null || !getFrameCalculator().inFrameCalculatorThread())) {
            if (log.isLoggable(Level.FINE)) {
                if (!serverActive) {
                    log.fine("Server is receiving incoming call " + method);
                    log.fine("recorded as server activity");
                }
            }
            serverActive = true;
        }
        return invoke(method, args);
    }

}
