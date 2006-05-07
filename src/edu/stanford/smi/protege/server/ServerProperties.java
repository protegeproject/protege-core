package edu.stanford.smi.protege.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;

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
  
  public static final String SERVER_PROPERTIES_FILE="server.properties";
  
  private static final Properties properties = new Properties();
  static {
    try {
      File propertyFile = new File(ApplicationProperties.getApplicationDirectory(), SERVER_PROPERTIES_FILE);
      InputStream is = new FileInputStream(propertyFile);
      properties.load(is);
      is.close();
      for (Object property : System.getProperties().keySet()) {
        if (property instanceof String) {
          properties.put(property, System.getProperty((String) property));
        }
      }
    } catch (IOException e) {
      Log.emptyCatchBlock(e);
    } catch (SecurityException e) {
      Log.emptyCatchBlock(e);
    }
  }

  public static Set<String> preloadUserFrames() {
    return getStringSet(ServerProperties.USER_PRELOAD);
  }
  
  
  private static Set<String> getStringSet(String property) {
    Set<String> values = new HashSet<String>();
    boolean noMoreValues = false;
    for (int i = 0; !noMoreValues; i++) {
      String value = properties.getProperty(property + i);
      if (value == null) {
        noMoreValues = true;
      } else {
        values.add(value);
      }
    }
    return values;
  }
  
  public static int delayInMilliseconds() {
    return Integer.getInteger(DELAY_MSEC , 0).intValue();
  }
  
  public static int minimumPreloadedFrames() {
    return Integer.getInteger(MIN_PRELOAD_FRAMES, 5000).intValue();
  }
  
}
