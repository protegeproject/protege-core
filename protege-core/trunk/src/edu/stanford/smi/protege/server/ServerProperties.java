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
  public static final String MIN_PRELOAD_FRAMES = "preload.frame.limit";
  public static final String DISABLE_HEARTBEAT = "server.disable.heartbeat";
  public static final String TX_LEVEL = "transaction.level";
  public final static String SERVER_ALLOW_CREATE_USERS = "server.allow.create.users";
  public final static String SERVER_NEW_PROJECTS_SAVE_DIRECTORY_PROTEGE_PROPERTY = "server.newproject.save.directory";

  
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
  
  public static int delayInMilliseconds() {
    return SystemUtilities.getSystemIntegerProperty(DELAY_MSEC , 0);
  }
  
  public static int minimumPreloadedFrames() {
    return SystemUtilities.getSystemIntegerProperty(MIN_PRELOAD_FRAMES, 5000);
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
  
}
