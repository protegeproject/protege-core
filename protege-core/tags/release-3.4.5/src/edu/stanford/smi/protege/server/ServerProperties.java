package edu.stanford.smi.protege.server;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;

/**
 * This class is a central repository of the server properties.
 * 
 * The server has a huge collection of properties that can be used to 
 * set its configuration.  We have put all these properties in a central place
 * to ease the problem of documenting these properties.  I plan to add a listing
 * of all of these properties to a wiki page soon.
 * 
 * @author tredmond
 *
 */
public class ServerProperties {

  public static final String SERVER_PORT = "protege.rmi.server.port";
  public static final String SERVER_LOCAL_PORT = "protege.rmi.server.local.port";
  public static final String REGISTRY_PORT = "protege.rmi.registry.port";
  public static final String REGISTRY_LOCAL_PORT = "protege.rmi.registry.local.port";
  public static final String USER_PRELOAD = "server.client.preload";
  public static final String SKIP_PRELOAD = "server.client.preload.skip";
  public static final String DELAY_MSEC = "server.delay";
  public static final String DELAY_KBS_DOWNLOAD = "server.download.kilobytes.second";
  public static final String DELAY_KBS_UPLOAD = "server.upload.kilobytes.second";
  public static final String PRELOAD_FRAME_LIMIT = "preload.frame.limit";
  public static final String DISABLE_HEARTBEAT = "server.disable.heartbeat";
  public static final String TX_LEVEL = "transaction.level";
  public final static String SERVER_ALLOW_CREATE_USERS = "server.allow.create.users";
  public final static String SERVER_NEW_PROJECTS_SAVE_DIRECTORY_PROTEGE_PROPERTY = "server.newproject.save.directory";

  public final static String PROTEGE_RMI_USE_COMPRESSION = "server.use.compression";
  public final static String PROTEGE_RMI_TOO_SMALL_TO_COMPRESS = "server.too.small.to.compress";
  
  public final static String METAPROJECT_LAST_ACCESS_TIME_UPDATE_FREQUENCY = "metaproject.last.accesstime.update.freq"; //default 15 secs
  
  public static boolean skipPreload() {
	  return SystemUtilities.getSystemBooleanProperty(SKIP_PRELOAD, false);
  }
  
  public static Set<String> preloadUserFrames() {
    return getStringSet(ServerProperties.USER_PRELOAD);
  }
    
  private static Set<String> getStringSet(String property) {
    Set<String> values = new HashSet<String>();
    boolean noMoreValues = false;
    for (int i = 0; !noMoreValues; i++) {
      String value = SystemUtilities.getSystemProperty(property + i);
      if (value == null) {
        noMoreValues = true;
      } else {
        values.add(value);
      }
    }
    return values;
  }
  
  public static boolean heartbeatDisabled() {
    // disabled for now.
    // return Boolean.getBoolean(DISABLE_HEARTBEAT);
    return true;
  }
  
  public static int getPreloadFrameLimit() {
    return SystemUtilities.getSystemIntegerProperty(PRELOAD_FRAME_LIMIT, 5000);
  }
  
  public static TransactionIsolationLevel getDefaultTransactionIsolationLevel() {
    String levelAsString = System.getProperty(TX_LEVEL);
    if (levelAsString == null) { 
      return null;
    }
    for (TransactionIsolationLevel level : TransactionIsolationLevel.values()) {
      if (levelAsString.equals(level.toString())) {
        return level;
      }
    }
    Log.getLogger().warning("transaction level " + levelAsString + " does not match any of the available levels");
    return null;
  }
  
  public static boolean getAllowsCreateUsers() {
	  return ApplicationProperties.getBooleanProperty(SERVER_ALLOW_CREATE_USERS, false);
  }
  
  public static String getDefaultNewProjectSaveDirectory() {
      String defaultSaveDir = ApplicationProperties.getApplicationDirectory().getAbsolutePath();
      
      return ApplicationProperties.getApplicationOrSystemProperty(SERVER_NEW_PROJECTS_SAVE_DIRECTORY_PROTEGE_PROPERTY, defaultSaveDir);
  }
  
  public static boolean useCompression() {
      String val = ApplicationProperties.getApplicationOrSystemProperty(PROTEGE_RMI_USE_COMPRESSION, "TRUE");
      return val.toLowerCase().equals("true");
  }
  
  public static int tooSmallToCompress() {
      int defaultValue = 1024;
      String small = System.getProperty(PROTEGE_RMI_TOO_SMALL_TO_COMPRESS, 
                                        new StringBuffer().append(defaultValue).toString());
      try {
          return Integer.parseInt(small);
      }
      catch (NumberFormatException nfe) {
          return defaultValue;
      }
  }
  
  public static int delayInMilliseconds() {
      return SystemUtilities.getSystemIntegerProperty(DELAY_MSEC , 0);
    }
  
  public static int getKiloBytesPerSecondDownload() {
      String val = ApplicationProperties.getApplicationOrSystemProperty(DELAY_KBS_DOWNLOAD, "0");
      try {
          return Integer.parseInt(val);
      }
      catch (NumberFormatException nfe) {
          return 0;
      }
  }
  
  public static int getKiloBytesPerSecondUpload() {
      String val = ApplicationProperties.getApplicationOrSystemProperty(DELAY_KBS_UPLOAD, "0");
      try {
          return Integer.parseInt(val);
      }
      catch (NumberFormatException nfe) {
          return 0;
      }
  }
  
  public static long getMetaProjectLastAccessTimeUpdateFrequency() {      
      String val = ApplicationProperties.getApplicationOrSystemProperty(DELAY_KBS_UPLOAD, "15000");
      try {
          return Long.parseLong(val);
      }
      catch (NumberFormatException nfe) {
          return 15 * 1000;
      }  
  }
}
