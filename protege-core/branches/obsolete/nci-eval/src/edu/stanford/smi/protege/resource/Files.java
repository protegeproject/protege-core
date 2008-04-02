package edu.stanford.smi.protege.resource;

import java.io.*;

import edu.stanford.smi.protege.util.*;

/**
 * Utility class for accessing files in protege jar.
 * 
 * @author Ray Fergerson
 * @author Jennifer Vendetti
 */
public class Files {
    private final static String CLSES = "standard_project.pont";
    private final static String INSTANCES = "standard_project.pins";
    private final static String DIRECTORY = "files";

    private static Reader getReader(String name) {
        return FileUtilities.getResourceReader(Files.class, DIRECTORY, name);
    }

    public static Reader getSystemClsesReader() {
        return getReader(CLSES);
    }

    public static Reader getSystemInstancesReader() {
        return getReader(INSTANCES);
    }

}
