package edu.stanford.smi.protege.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * A utility class that prints trace messages of various sorts to a log. By
 * default the "log" is the err console but it could be directed elsewhere.
 * <p>
 *
 * The following code is an example of the use of Log.
 *
 * <blockquote>
 *
 * <pre>
 *
 *    class Foo {
 *       void bar(Object o) {
 *             ...
 *             Log.trace(&quot;my message&quot;, this, &quot;bar&quot;, o);
 *             ...
 *       }
 *
 *       void static baz(Object o1, String s1) {
 *            ...
 *            Log.trace(&quot;my message&quot;, Foo.class, &quot;baz&quot;, o1, s1);
 *            ...
 *       }
 * </pre>
 *
 * </blockquote>
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */

public class Log {
  /*
   * Generally speaking System.out.println is bad practice.  But debugging
   * the Logger seems like a reasonable exception.  The debug flag controls
   * whether the Log class generates System.out.println statements during its
   * execution.
   */
    private static boolean debug = false;


    /**
     * This flag is used to give only one warning in the console if the
     * Log.getLogger() throws IO exceptions, rather than having this warning
     * repeated many times in the console.
     */
    private static boolean displayedIOWarning = false;

    static {
      String debugLogProperty = ApplicationProperties.LOG_DEBUG_PROPERTY;
      try {
    	  if (System.getProperty(debugLogProperty) != null) {
    		  debug = true;
    	  }
      } catch (Throwable t) {
    	  debug = false;  // this is not really recommended.
      }
    }

    private static Logger logger;
    private static LegacyLogger legacyLogger;
    private static Handler consoleHandler;
    private static Handler fileHandler;
    private static boolean configuredByFile = false;

    static {
      boolean configured = false;
      String logProperty = ApplicationProperties.LOG_FILE_PROPERTY;
      //String rootDir = System.getProperty(ApplicationProperties.APPLICATION_INSTALL_DIRECTORY);
      //String rootDir = ApplicationProperties.getApplicationDirectory().getAbsolutePath();
      //this call avoids premature initialization of ApplicationProperties and SystemUtilities which caused other initializatino problems (look and feel)
      //TODO: find a better way to do the initialization
      try {
        File applicationDirectory = getApplicationDirectory();
        String rootDir = applicationDirectory == null ? null  :  applicationDirectory.getAbsolutePath();
        if (System.getProperty(logProperty) != null) {
          if (debug) {
            System.out.println("Logger already configured...");
          }
          // already configured...
          configuredByFile = true;
          configured = true;
        } else if (rootDir != null) {
          File logconfig = new File(rootDir + File.separator + "logging.properties");
          if (debug) {
            System.out.println("Logging file = " + logconfig);
          }
          if (logconfig.canRead()) {
            if  (debug) {
              System.out.println("Logging file readable");
            }
            System.setProperty(logProperty, logconfig.getAbsolutePath());
            LogManager.getLogManager().readConfiguration();
            configuredByFile = true;
            configured = true;
            if (debug) {
              System.out.println("Configuration done by util.Log class ");
            }
          }
        }
      } catch (Throwable e) {
    	  System.out.println("Could not set up class specific logging");
      }
      if (!configured) {
    	  if (debug) {
    		  System.out.println("using default configuration.");
    	  }
    	  try {
    		  Log.getLogger().setLevel(Level.CONFIG);
    	  } catch (Throwable t) {
    		  System.out.println("Could not set logger level");
    	  }
      }
      // Example of programatic level setting
      // Logger.getLogger("edu.stanford.smi.protege.model.framestore").setLevel(Level.FINEST);
    }

    private Log() {

    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("protege.system");
            if (!configuredByFile) {
              try {
                logger.setUseParentHandlers(false);
                logger.setLevel(Level.ALL);
                addConsoleHandler();
                addFileHandler();
              } catch (Throwable e) {
            	  System.out.println("Exception configuring logger");
              }
            }
        }
        return logger;
    }

    public static void emptyCatchBlock(Throwable t) {
    	if (getLogger().isLoggable(Level.FINE)) {
    		getLogger().log(Level.FINE, "Exception Caught", t);
    	}
    }

    public static Logger getLogger(Class c) {
        Logger l = Logger.getLogger(c.getName());
        if (!configuredByFile) {
          try {
            l.addHandler(getFileHandler());

            Handler consoleHandler = getConsoleHandler();
            if (l != null && consoleHandler != null) {
          	  l.addHandler(consoleHandler);
            }

          } catch (Throwable e) {
        	  if (!Log.displayedIOWarning) {
        		  System.err.println("Warning: IO exception getting logger. " + e.getMessage());
        		  Log.displayedIOWarning = true;
        	  }
          }
        }
        return l;
    }


    public static void makeInheritedLoggersLocal(Logger logger) {
    	logger.setUseParentHandlers(false);
    	if (logger.getHandlers() != null && logger.getHandlers().length > 0) {
    		return;
    	}

    	Handler[] inheritedHandlers = getInheritedHandlers(logger);
    	for (Handler inheritedHandler : inheritedHandlers) {
			logger.addHandler(inheritedHandler);
		}
    }


    private static Handler[] getInheritedHandlers(Logger logger) {
    	Handler[] inheritedHandlers = logger.getHandlers();

    	while (inheritedHandlers.length == 0) {
    		Logger parentLogger = logger.getParent();
    		if (parentLogger == null) { //root
    			return inheritedHandlers;
    		}
    		logger = parentLogger;
    		inheritedHandlers = parentLogger.getHandlers();
    	}

		return inheritedHandlers;
	}

	/**
     * This method is to ease  the debugging of junits.  It does allow reliable and
     * programatic setting of logging levels but it is probably only useful for debug.
     */
    public static void setLoggingLevel(Class<?> c, Level level) {
        Logger.getLogger(c.getName()).setLevel(level);
    }


    @SuppressWarnings("unchecked")
    public static void handleErrors(Logger log, Level level, Collection errors) {
        if (errors == null || errors.size() == 0) {
            return;
        }
        log.log(level, "Errors found performing operation.\n\n");
        for (Object o: errors) {
            if (o instanceof MessageError) {
                MessageError me = (MessageError) o;
                log.log(level, me.getMessage(), me.getException());
            } else if (o instanceof Throwable) {
                Throwable t = (Throwable) o;
                log.log(level, "Exception caught", t);
            } else {
                log.log(level, ((o == null) ? "(missing error message)" : o.toString()));
            }
        }
    }


    public static String toString(Throwable t) {
        Writer writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        t.printStackTrace(printer);
        printer.flush();
        return writer.toString();
    }

    private static void addConsoleHandler() {
        Handler consoleHandler = getConsoleHandler();
        if (logger != null && consoleHandler != null) {
      	  logger.addHandler(consoleHandler);
        }
    }

    private static Handler getConsoleHandler() {
    	try {
    		if (consoleHandler == null) {
    			consoleHandler = new ConsoleHandler();
    			consoleHandler.setFormatter(new ConsoleFormatter());
    			consoleHandler.setLevel(Level.ALL);
    		}
    		return consoleHandler;
    	}catch (Throwable e) {
    		// When does this happen?
    		System.err.println("Warning: Cannot set console log debugger handler.");
    	}

    	return null;
    }

    private static void addFileHandler() {
        try {
            Handler handler = getFileHandler();
            logger.addHandler(handler);
            handler.publish(new LogRecord(Level.INFO, "*** SYSTEM START ***"));
        } catch (Throwable e) {
            System.err.println("Error adding file handler to logger");
        }
    }

    private static Handler getFileHandler() throws IOException {

        if (fileHandler == null) {
            String path;
            File file = ApplicationProperties.getLogFileDirectory();
            if (file == null) {
                path = "%t"; // the temp directory. Better somewhere than
                // nowhere!
            } else {
                path = file.getPath();
            }
            fileHandler = new FileHandler(path + File.separatorChar + "protege_%u.log", true);
            fileHandler.setFormatter(new FileFormatter());
            fileHandler.setLevel(Level.ALL);
        }
        return fileHandler;
    }


    private static File getApplicationDirectory() {
        String dir = getSystemProperty(ApplicationProperties.APPLICATION_INSTALL_DIRECTORY);
        if (dir == null) {
            dir = getSystemProperty(ApplicationProperties.CURRENT_WORKING_DIRECTORY);
        }
        return dir == null ? null : new File(dir);
    }


    private static String getSystemProperty(String property) {
        String value;
        try {
            value = System.getProperty(property);
        } catch (SecurityException e) {
            value = null;
            // WARNING: Empty catch block
        }
        return value;
    }

    /**
     * Description of the Class
     *
     * @author Ray Fergerson <fergerson@smi.stanford.edu>
     */
    interface LegacyLogger {
        void enter(Object object, String methodName, Object[] args);

        void exit(Object object, String methodName, Object[] args);

        void trace(String entry, Object object, String methodName, Object[] args);

        void warning(String entry, Object object, String methodName, Object[] args);

        void error(String entry, Object object, String methodName, Object[] args);

        void exception(Throwable e, Object object, String methodName, Object[] args);

        void stack(String entry, Object object, String methodName, Object[] args);
    }


    private static LegacyLogger getLegacyLogger() {
        if (legacyLogger == null) {
            legacyLogger = new LegacyLoggerImpl(getLogger());
        }
        return legacyLogger;
    }

    /* --------------------------------------------------------------------------------------
     * Deprecated Legacy Methods
     */

    /**
     * Make an entry into the log with the message that <code>methodName
     *  </code>
     * has been called (see the {@link Log} class example).
     *
     * @deprecated Use #getLogger().enter()
     */
    @Deprecated
    public static void enter(Object thisOrClass, String methodName) {
        getLegacyLogger().enter(thisOrClass, methodName, new Object[] {});
    }

    /**
     * Make an entry into the log with the message that <code>methodName
     *  </code>
     * has been called and passed the listed argument (see the {@link Log} class
     * example).
     *
     * @deprecated Use #getLogger().enter()
     */
    @Deprecated
    public static void enter(Object thisOrClass, String methodName, Object arg1) {
        getLegacyLogger().enter(thisOrClass, methodName, new Object[] { arg1 });
    }

    /**
     * Make an entry into the log with the message that <code>methodName
     *  </code>
     * has been called and passed the listed arguments (see the {@link Log}
     * class example).
     *
     * @deprecated Use #getLogger().enter()
     */
    @Deprecated
    public static void enter(Object thisOrClass, String methodName, Object arg1, Object arg2) {
        getLegacyLogger().enter(thisOrClass, methodName, new Object[] { arg1, arg2 });
    }

    /**
     * Make an entry into the log with the message that <code>methodName
     *  </code>
     * has been called and passed the listed arguments (see the {@link Log}
     * class example).
     *
     * @deprecated Use #getLogger().enter()
     */
    @Deprecated
    public static void enter(Object thisOrClass, String methodName, Object arg1, Object arg2, Object arg3) {
        getLegacyLogger().enter(thisOrClass, methodName, new Object[] { arg1, arg2, arg3 });
    }

    /**
     * Make an entry into the log with the message that <code>methodName
     *  </code>
     * has been called and passed the listed arguments (see the {@link Log}
     * class example).
     *
     * @deprecated Use #getLogger().enter()
     */
    @Deprecated
    public static void enter(Object thisOrClass, String methodName, Object arg1, Object arg2, Object arg3, Object arg4) {
        getLegacyLogger().enter(thisOrClass, methodName, new Object[] { arg1, arg2, arg3, arg4 });
    }

    /**
     * Make an entry into the log with the message that <code>methodName
     *  </code>
     * has been called and passed the listed arguments (see the {@link Log}
     * class example).
     *
     * @deprecated Use #getLogger().enter()
     */
    @Deprecated
    public static void enter(Object thisOrClass, String methodName, Object arg1, Object arg2, Object arg3, Object arg4,
            Object arg5) {
        getLegacyLogger().enter(thisOrClass, methodName, new Object[] { arg1, arg2, arg3, arg4, arg5 });
    }

    /**
     * Put a message into the log that an error with the given description
     * occurred from inside of <code>methodName</code> which was called with
     * the listed arguments (see the {@link Log} class example).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void error(String description, Object thisOrClass, String methodName) {
        getLegacyLogger().error(description, thisOrClass, methodName, new Object[] {});
    }

    /**
     * Put a message into the log that an error with the given description
     * occurred from inside of <code>methodName</code> which was called with
     * the listed arguments (see the {@link Log} class example).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void error(String description, Object thisOrClass, String methodName, Object arg1) {
        getLegacyLogger().error(description, thisOrClass, methodName, new Object[] { arg1 });
    }

    /**
     * Put a message into the log that an error with the given description
     * occurred from inside of <code>methodName</code> which was called with
     * the listed arguments (see the {@link Log} class example}).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void error(String description, Object thisOrClass, String methodName, Object arg1, Object arg2) {
        getLegacyLogger().error(description, thisOrClass, methodName, new Object[] { arg1, arg2 });
    }

    /**
     * Put a message into the log that an error with the given description
     * occurred from inside of <code>methodName</code> which was called with
     * the listed arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void error(String description, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3) {
        getLegacyLogger().error(description, thisOrClass, methodName, new Object[] { arg1, arg2, arg3 });
    }

    /**
     * Put a message into the log that an error with the given description
     * occurred from inside of <code>methodName</code> which was called with
     * the listed arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void error(String description, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3, Object arg4) {
        getLegacyLogger().error(description, thisOrClass, methodName, new Object[] { arg1, arg2, arg3, arg4 });
    }

    /**
     * Put a message into the log that an error with the given description
     * occurred from inside of <code>methodName</code> which was called with
     * the listed arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void error(String description, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3, Object arg4, Object arg5) {
        getLegacyLogger().error(description, thisOrClass, methodName, new Object[] { arg1, arg2, arg3, arg4, arg5 });
    }

    /**
     * Put a message into the log that an unexpected exception was caught from
     * inside of <code>methodName</code> (see the {@link Log Log class
     * example}).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void exception(Throwable exception, Object thisOrClass, String methodName) {
        getLegacyLogger().exception(exception, thisOrClass, methodName, new Object[] {});
    }

    /**
     * Put a message into the log that an unexpected exception was caught from
     * inside of <code>methodName</code> which was called with the listed
     * arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void exception(Throwable exception, Object thisOrClass, String methodName, Object arg1) {
        getLegacyLogger().exception(exception, thisOrClass, methodName, new Object[] { arg1 });
    }

    /**
     * Put a message into the log that an unexpected exception was caught from
     * inside of <code>methodName</code> which was called with the listed
     * arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void exception(Throwable exception, Object thisOrClass, String methodName, Object arg1, Object arg2) {
        getLegacyLogger().exception(exception, thisOrClass, methodName, new Object[] { arg1, arg2 });
    }

    /**
     * Put a message into the log that an unexpected exception was caught from
     * inside of <code>methodName</code> which was called with the listed
     * arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void exception(Throwable exception, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3) {
        getLegacyLogger().exception(exception, thisOrClass, methodName, new Object[] { arg1, arg2, arg3 });
    }

    /**
     * Put a message into the log that an unexpected exception was caught from
     * inside of <code>methodName</code> which was called with the listed
     * arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void exception(Throwable exception, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3, Object arg4) {
        getLegacyLogger().exception(exception, thisOrClass, methodName, new Object[] { arg1, arg2, arg3, arg4 });
    }

    /**
     * Put a message into the log that an unexpected exception was caught from
     * inside of <code>methodName</code> which was called with the listed
     * arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().severe();
     */
    @Deprecated
    public static void exception(Throwable exception, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3, Object arg4, Object arg5) {
        getLegacyLogger().exception(exception, thisOrClass, methodName, new Object[] { arg1, arg2, arg3, arg4, arg5 });
    }

    /**
     * Make an entry into the log with the message that <code>methodName
     *  </code>
     * has returned (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().exiting();
     */
    @Deprecated
    public static void exit(Object thisOrClass, String methodName) {
        getLegacyLogger().exit(thisOrClass, methodName, new Object[] {});
    }

    /**
     * Put a stack dump and message "description" into the log with the
     * additional information that the message is occuring from inside of
     * <code>methodName</code> (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().info();
     */
    @Deprecated
    public static void stack(String description, Object thisOrClass, String methodName) {
        getLegacyLogger().stack(description, thisOrClass, methodName, new Object[] {});
    }

    /**
     * Put a stack dump and message "description" into the log with the
     * additional information that the message is occuring from inside of
     * <code>methodName</code> which was called with the listed argument (see
     * the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().info();
     */
    @Deprecated
    public static void stack(String description, Object thisOrClass, String methodName, Object arg1) {
        getLegacyLogger().stack(description, thisOrClass, methodName, new Object[] { arg1 });
    }

    /**
     * Put a stack dump and message "description" into the log with the
     * additional information that the message is occuring from inside of
     * <code>methodName</code> which was called with the listed arguments (see
     * the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().info();
     */
    @Deprecated
    public static void stack(String description, Object thisOrClass, String methodName, Object arg1, Object arg2) {
        getLegacyLogger().stack(description, thisOrClass, methodName, new Object[] { arg1, arg2 });
    }

    /**
     * Put a stack dump and message "description" into the log with the
     * additional information that the message is occuring from inside of
     * <code>methodName</code> which was called with the listed arguments (see
     * the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().info();
     */
    @Deprecated
    public static void stack(String description, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3) {
        getLegacyLogger().stack(description, thisOrClass, methodName, new Object[] { arg1, arg2, arg3 });
    }

    /**
     * Put a stack dump and message "description" into the log with the
     * additional information that the message is occuring from inside of
     * <code>methodName</code> which was called with the listed arguments (see
     * the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().info();
     */
    @Deprecated
    public static void stack(String description, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3, Object arg4) {
        getLegacyLogger().stack(description, thisOrClass, methodName, new Object[] { arg1, arg2, arg3, arg4 });
    }

    /**
     * Put a trace message "description" into the log with the additional
     * information that the message is occuring from inside of <code>methodName
     *  </code>
     * (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().info();
     */
    @Deprecated
    public static void trace(String description, Object thisOrClass, String methodName) {
        getLegacyLogger().trace(description, thisOrClass, methodName, new Object[] {});
    }

    /**
     * Put a trace message "description" into the log with the additional
     * information that the message is occuring from inside of <code>methodName
     *  </code>
     * which was called with the listed arguments (see the {@link Log class
     * example}).
     *
     * @deprecated Use getLogger().info();
     */
    @Deprecated
    public static void trace(String description, Object thisOrClass, String methodName, Object arg1) {
        getLegacyLogger().trace(description, thisOrClass, methodName, new Object[] { arg1 });
    }

    /**
     * Put a trace message "description" into the log with the additional
     * information that the message is occuring from inside of <code>methodName
     *  </code>
     * which was called with the listed arguments (see the {@link Log class
     * example}).
     *
     * @deprecated Use getLogger().info();
     */
    @Deprecated
    public static void trace(String description, Object thisOrClass, String methodName, Object arg1, Object arg2) {
        getLegacyLogger().trace(description, thisOrClass, methodName, new Object[] { arg1, arg2 });
    }

    /**
     * Put a trace message "description" into the log with the additional
     * information that the message is occuring from inside of <code>methodName
     *  </code>
     * which was called with the listed arguments (see the {@link Log class
     * example}).
     *
     * @deprecated Use getLogger().info();
     */
    @Deprecated
    public static void trace(String description, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3) {
        getLegacyLogger().trace(description, thisOrClass, methodName, new Object[] { arg1, arg2, arg3 });
    }

    /**
     * Put a trace message "description" into the log with the additional
     * information that the message is occuring from inside of <code>methodName
     *  </code>
     * which was called with the listed arguments (see the {@link Log class
     * example}).
     *
     * @deprecated Use getLogger().info();
     */
    @Deprecated
    public static void trace(String description, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3, Object arg4) {
        getLegacyLogger().trace(description, thisOrClass, methodName, new Object[] { arg1, arg2, arg3, arg4 });
    }

    /**
     * Put a trace message "description" into the log with the additional
     * information that the message is occuring from inside of <code>methodName
     *  </code>
     * which was called with the listed arguments (see the {@link Log class
     * example}).
     *
     * @deprecated Use getLogger().info();
     */
    @Deprecated
    public static void trace(String description, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3, Object arg4, Object arg5) {
        getLegacyLogger().trace(description, thisOrClass, methodName, new Object[] { arg1, arg2, arg3, arg4, arg5 });
    }

    /**
     * Put a message into the log that a warning with the given description
     * occurred from inside of <code>methodName</code> (see the {@link Log Log
     * class example}).
     *
     * @deprecated Use getLogger().warning();
     */
    @Deprecated
    public static void warning(String description, Object thisOrClass, String methodName) {
        getLegacyLogger().warning(description, thisOrClass, methodName, new Object[] {});
    }

    /**
     * Put a message into the log that a warning with the given description
     * occurred from inside of <code>methodName</code> which was called with
     * the listed arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().warning();
     */
    @Deprecated
    public static void warning(String description, Object thisOrClass, String methodName, Object arg1) {
        getLegacyLogger().warning(description, thisOrClass, methodName, new Object[] { arg1 });
    }

    /**
     * Put a message into the log that a warning with the given description
     * occurred from inside of <code>methodName</code> which was called with
     * the listed arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().warning();
     */
    @Deprecated
    public static void warning(String description, Object thisOrClass, String methodName, Object arg1, Object arg2) {
        getLegacyLogger().warning(description, thisOrClass, methodName, new Object[] { arg1, arg2 });
    }

    /**
     * Put a message into the log that a warning with the given description
     * occurred from inside of <code>methodName</code> which was called with
     * the listed arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().warning();
     */
    @Deprecated
    public static void warning(String description, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3) {
        getLegacyLogger().warning(description, thisOrClass, methodName, new Object[] { arg1, arg2, arg3 });
    }

    /**
     * Put a message into the log that a warning with the given description
     * occurred from inside of <code>methodName</code> which was called with
     * the listed arguments (see the {@link Log Log class example}).
     *
     * @deprecated Use getLogger().warning();
     */
    @Deprecated
    public static void warning(String description, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3, Object arg4) {
        getLegacyLogger().warning(description, thisOrClass, methodName, new Object[] { arg1, arg2, arg3, arg4 });
    }
}