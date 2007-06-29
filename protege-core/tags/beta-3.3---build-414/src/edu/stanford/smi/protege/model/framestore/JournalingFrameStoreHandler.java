package edu.stanford.smi.protege.model.framestore;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.model.query.QueryCallbackClone;
import edu.stanford.smi.protege.util.Log;

public class JournalingFrameStoreHandler extends AbstractFrameStoreInvocationHandler {
    public final static String journaller_name = "protege.journal";
    private Logger journaler;
    private Handler handler;
    private boolean recordQueries = false;

    public void start(URI journalURI) {
        try {
            stop();
            handler = new java.util.logging.FileHandler(new File(journalURI).getPath(), true);
            journaler = Logger.getLogger(journaller_name);
            journaler.setUseParentHandlers(false);
            journaler.addHandler(handler);
            journaler.setLevel(Level.ALL);
            handler.setFormatter(new JournalFormater());
            handler.setLevel(Level.ALL);
        } catch (IOException e) {
            Log.getLogger().throwing("JournalingFrameStoreHandler", "start", e);
        }
    }

    public void stop() {
        if (handler != null) {
            journaler.removeHandler(handler);
            handler.flush();
            handler.close();
            handler = null;
        }
        journaler = null;
    }

    public void handleClose() {
        stop();
    }

    public Object handleInvoke(Method method, Object[] args) {
        Object result = invoke(method, args);
        if (doRecord(method)) {
            record(method, args, result);
        }
        return result;
    }
    
    public void executeQuery(Query q, QueryCallback qc) {
      QueryCallback myCallback = qc;
      if (recordQueries) {
        myCallback = new QueryCallbackClone(qc) {
          public void provideQueryResults(Set<Frame> results) {
            journaler.info("Query Callback returning results");
            super.provideQueryResults(results);
          }
        };
      }
      getDelegate().executeQuery(q, myCallback);
      if (recordQueries) {
          journaler.info("Executing query " + q + " (callback may log entry before this log)");
      }
    }

    protected void record(Method m, Object[] args, Object result) {
        int length = (args == null) ? 2 : args.length + 2;
        Object[] params = new Object[length];
        params[0] = m;
        if (args != null) {
            System.arraycopy(args, 0, params, 1, args.length);
        }
        params[length - 1] = result;
        journaler.log(Level.INFO, "entry", params);
    }

    protected boolean doRecord(Method method) {
        return journaler != null && (recordQueries || isModification(method));
    }
}