package edu.stanford.smi.protege.model.framestore;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.logging.*;

import edu.stanford.smi.protege.util.*;

public class JournalingFrameStoreHandler extends AbstractFrameStoreInvocationHandler {
    private Logger journaler;
    private Handler handler;
    private boolean recordQueries = false;

    public void start(URI journalURI) {
        try {
            stop();
            handler = new FileHandler(new File(journalURI).getPath(), true);
            journaler = Logger.getLogger("protege.journal");
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