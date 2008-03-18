package edu.stanford.smi.protege.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FileHandler extends java.util.logging.FileHandler {
    public static final String ROTATION_COUNT_PROPERTY="edu.stanford.smi.protege.util.Log.RotationCount";
    public static final Integer ROTATION_COUNT = ApplicationProperties.getIntegerProperty(ROTATION_COUNT_PROPERTY, 10);
    public static final String PREFIX = "protege_";
    public static final String SUFFIX = ".log";
    
    private static File directory;
    
    static String path;

    static {
        directory = ApplicationProperties.getLogFileDirectory();
        if (directory == null) {
            directory = new File(System.getProperty("java.io.tmpdir")); // the temp directory. Better somewhere than
            // nowhere!
        }
    }

    public FileHandler() throws IOException {
        super(getNextLogFile());
        setFormatter(new FileFormatter());
        publish(new LogRecord(Level.INFO, "*** SYSTEM START ***"));
    }
    
    
    
    private static String getNextLogFile() {
        Map<Integer, File> matchingLogFiles = new HashMap<Integer, File>();
        int lastMatch = -1;
        for (File f : directory.getAbsoluteFile().listFiles()) {
            Integer count = matchingFile(f);
            if (count == null) continue;
            matchingLogFiles.put(count, f);
            if (lastMatch < count) {
                lastMatch = count;
            }
        }
        for (Map.Entry<Integer, File> entry : matchingLogFiles.entrySet()) {
            if (entry.getKey() <= lastMatch - ROTATION_COUNT + 1) {
                entry.getValue().delete();
                File lockFile = new File(entry.getValue().getAbsolutePath() + ".lck");
                if (lockFile.exists()) {
                    lockFile.delete();
                }
            }
        }
        return new File(directory, PREFIX + (lastMatch + 1) + SUFFIX).getAbsolutePath();
    }
    

    private static  Integer matchingFile(File f) {
        String relativeName = f.getName();
        if (!relativeName.startsWith(PREFIX) || !relativeName.endsWith(SUFFIX) || 
                relativeName.length() < PREFIX.length() + SUFFIX.length()) {
            return null;
        }
        String countString = relativeName.substring(PREFIX.length(), relativeName.length() - SUFFIX.length());
        try {
            return Integer.parseInt(countString);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }


}
