package edu.stanford.smi.protege.resource;

import java.io.*;
import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * Utility class for accessing assorted text strings.
 *
 * @author Ray Fergerson
 * @author Jennifer Vendetti
 */
public final class Text {
    private static String buildFile = "build.properties";
    private static String directory = "files";
    private static Properties props;
    private static final String PROGRAM_NAME = "Prot\u00E9g\u00E9";
    private static final String PROGRAM_ASCII_NAME = "Protege";
    public static final String PROGRAM_NAME_PROPERTY = "resource.text.program_name";

    static {
        try {
            props = new Properties();
            InputStream stream = FileUtilities.getResourceStream(Text.class, directory, buildFile);
            props.load(stream);
        } catch (IOException e) {
            Log.getLogger().severe(Log.toString(e));
        }
    }

    public static String getBuildInfo() {
        return "Build " + getBuildNumber();
    }

    public static String getProgramName() {
        return SystemUtilities.getSystemProperty(PROGRAM_NAME_PROPERTY, PROGRAM_NAME);
    }

    public static String getProgramTextName() {
        return SystemUtilities.getSystemProperty(PROGRAM_NAME_PROPERTY, PROGRAM_ASCII_NAME);
    }

    public static String getProgramNameAndVersion() {
        return getProgramName() + " " + getVersion() + " " + getStatus();
    }

    public static String getBuildNumber() {
        return props.getProperty("build.number", "?");
    }

    public static String getStatus() {
        return props.getProperty("build.status");
    }

    public static String getVersion() {
        return props.getProperty("build.version", "?");
    }
    
    public static String getTrademark() {
    	return props.getProperty("trademark");
    }

    public static String getCopyright() {
    	return props.getProperty("copyright");
    }

    public static URL getAboutURL() {
        return Text.class.getResource("files/about.html");
    }
}
