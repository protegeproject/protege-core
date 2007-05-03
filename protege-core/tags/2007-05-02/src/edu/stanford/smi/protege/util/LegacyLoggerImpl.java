package edu.stanford.smi.protege.util;

import java.util.logging.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class LegacyLoggerImpl implements Log.LegacyLogger {
    private Level OLD_TRACE = Level.INFO;
    private Level OLD_WARNING = Level.WARNING;
    private Level OLD_ERROR = Level.WARNING;
    private Level OLD_EXCEPTION = Level.WARNING;
    private Level OLD_ENTER = Level.INFO;
    private Level OLD_EXIT = Level.INFO;
    private Logger logger;

    public LegacyLoggerImpl(Logger logger) {
        this.logger = logger;
    }

    private static String getClassName(Object o) {
        String name;
        if (o instanceof Class) {
            name = ((Class)o).getName();
        } else {
            name = o.getClass().getName();
        }
        return name;
    }

    public void enter(Object object, String methodName, Object[] args) {
        logger.logp(OLD_ENTER, getClassName(object), methodName, "ENTER", args);
    }

    public void exit(Object object, String methodName, Object[] args) {
        logger.logp(OLD_EXIT, getClassName(object), methodName, "EXIT", args);
    }

    public void trace(String entry, Object object, String methodName, Object[] args) {
        logger.logp(OLD_TRACE, getClassName(object), methodName, entry, args);
    }

    public void warning(String entry, Object object, String methodName, Object[] args) {
        logger.logp(OLD_WARNING, getClassName(object), methodName, entry, args);
    }

    public void error(String entry, Object object, String methodName, Object[] args) {
        logger.logp(OLD_ERROR, getClassName(object), methodName, entry, args);
    }

    public void exception(Throwable e, Object object, String methodName, Object[] args) {
        logger.logp(OLD_EXCEPTION, getClassName(object), methodName, "Exception Caught", e);
    }

    public void stack(String entry, Object object, String methodName, Object[] args) {
        new RuntimeException(entry).printStackTrace();
    }
}
