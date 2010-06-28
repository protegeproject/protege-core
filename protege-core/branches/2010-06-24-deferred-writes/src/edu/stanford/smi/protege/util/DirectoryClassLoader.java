package edu.stanford.smi.protege.util;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DirectoryClassLoader extends URLClassLoader {
    private File directory;

    private static FilenameFilter jarFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    };

    public DirectoryClassLoader(File directory, ClassLoader parent) {
        super(getURLs(directory), parent);
        this.directory = directory;
    }

    private static URL[] getURLs(File directory) {
        Collection urls = new ArrayList();
        if (directory != null) {
            urls.add(FileUtilities.toURL(directory));
            File[] jarFiles = directory.listFiles(jarFilter);
            if (jarFiles != null) {
                for (int i = 0; i < jarFiles.length; ++i) {
                    File jarFile = jarFiles[i];
                    urls.add(FileUtilities.toURL(jarFile));
                }
            }
        }
        return (URL[]) urls.toArray(new URL[urls.size()]);
    }

    public File getDirectory() {
        return directory;
    }

    public URL findResource(String resource) {
        URL url = null;
        File file = new File(directory, resource);
        if (file.exists()) {
            url = FileUtilities.toURL(file);
        }
        if (url == null) {
            url = super.findResource(resource);
        }
        return url;
    }

    public String toString() {
        return StringUtilities.getClassName(this) + "(" + directory + ", " + getParent() + ")";
    }
    
    public URL[] getURLs() {
        return null;
    }

}
