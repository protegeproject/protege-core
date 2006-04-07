package edu.stanford.smi.protege.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.LogManager;


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
 *                                                  class Foo {
 *                                                      void bar(Object o) {
 *                                                          ...
 *                                                          Log.trace(&quot;my message&quot;, this, &quot;bar&quot;, o);
 *                                                          ...
 *                                                      }
 *                                                      void static baz(Object o1, String s1) {
 *                                                          ...
 *                                                          Log.trace(&quot;my message&quot;, Foo.class, &quot;baz&quot;, o1, s1);
 *                                                          ...
 *                                                      }
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
    static {
      String debugLogProperty = ApplicationProperties.LOG_DEBUG_PROPERTY;
      if (System.getProperty(debugLogProperty) != null) {
          debug = true;
      }
    }
  
    private static Logger logger;
    private static LegacyLogger legacyLogger;
    private static Handler consoleHandler;
    private static Handler fileHandler;
    private static boolean configuredByFile = false;
    
    static {
      String logProperty = ApplicationProperties.LOG_FILE_PROPERTY;
      String rootDir = System.getProperty(ApplicationProperties.APPLICATION_INSTALL_DIRECTORY);
      try {
        if (System.getProperty(logProperty) != null) {
          if (debug) {
            System.out.println("Already configured...");
          }
          // already configured...
          configuredByFile = true;
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
            if (debug) {
              System.out.println("Configuration done by util.Log class ");
            }
          } else {
            if (Log.getLogger().isLoggable(Level.FINE)) {
              Log.getLogger().fine("No log configuration file available");
            }
            Log.getLogger().setLevel(Level.CONFIG);
          }
        }
      } catch (Exception e) {
        Log.getLogger().log(Level.WARNING, "Could not set up class specific logging", e);
      }
      // Example of programatic level setting
      // Logger.getLogger("edu.stanford.smi.protege.model.framestore").setLevel(Level.FINEST);
    }

    private Log() {

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

    /**
     * Make an entry into the log with the message that <code>methodName
     *  </code>
     * has been called (see the {@link Log} class example).
     * 
     * @deprecated Use #getLogger().enter()
     */
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
    public static void warning(String description, Object thisOrClass, String methodName, Object arg1, Object arg2,
            Object arg3, Object arg4) {
        getLegacyLogger().warning(description, thisOrClass, methodName, new Object[] { arg1, arg2, arg3, arg4 });
    }

    private static LegacyLogger getLegacyLogger() {
        if (legacyLogger == null) {
            legacyLogger = new LegacyLoggerImpl(getLogger());
        }
        return legacyLogger;
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
              } catch (SecurityException e) {
                // do nothing, happens in applets
                // NOTE - empty catch blocks are VERY DANGEROUS
                // but this might be ok...
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
          } catch (IOException e) {
            getLogger().warning("IO exception getting logger" + e);
          }
          l.addHandler(getConsoleHandler());
        }
        return l;
    }

    public static String toString(Throwable t) {
        Writer writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        t.printStackTrace(printer);
        printer.flush();
        return writer.toString();
    }

    private static void addConsoleHandler() {
        logger.addHandler(getConsoleHandler());
    }
    
    private static Handler getConsoleHandler() {
        if (consoleHandler == null) {
            consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new ConsoleFormatter());
            consoleHandler.setLevel(Level.ALL);
        }
        return consoleHandler;
    }

    private static void addFileHandler() {
        try {
            Handler handler = getFileHandler();
            logger.addHandler(handler);
            handler.publish(new LogRecord(Level.INFO, "*** SYSTEM START ***"));
        } catch (IOException e) {
            // do nothing, happens in applets
          // NOTE - empty catch blocks are VERY DANGEROUS
          // but this might be ok...
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
}