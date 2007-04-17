package edu.stanford.smi.protege.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FileHandler extends java.util.logging.FileHandler {
  static String path;
  static {
    File file = ApplicationProperties.getLogFileDirectory();
    if (file == null) {
      path = "%t"; // the temp directory. Better somewhere than
      // nowhere!
    } else {
      path = file.getPath();
    }
    path = path + File.separatorChar + "protege_%u.log";
  }
  
  public FileHandler() throws IOException {
    super(path);
    setFormatter(new FileFormatter());
    publish(new LogRecord(Level.INFO, "*** SYSTEM START ***"));
  }

}
