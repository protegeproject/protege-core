package edu.stanford.smi.protege.util;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MultiplexingClassLoader extends ClassLoader {
    private Collection childLoaders;

    public MultiplexingClassLoader(Collection childLoaders) {
        this.childLoaders = new ArrayList(childLoaders);
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        Class clas = null;
        Iterator i = childLoaders.iterator();
        while (i.hasNext() && clas == null) {
            ClassLoader child = (ClassLoader) i.next();
            try {
                clas = child.loadClass(name);
            } catch (ClassNotFoundException e) {
                // do nothing
            }
        }
        if (clas == null) {
            clas = super.findClass(name);
        }
        return clas;
    }

    protected URL findResource(String name) {
        URL resource = null;
        Iterator i = childLoaders.iterator();
        while (i.hasNext() && resource == null) {
            ClassLoader child = (ClassLoader) i.next();
            resource = child.getResource(name);
        }
        if (resource == null) {
            resource = super.findResource(name);
        }
        return resource;
    }

    protected Enumeration findResources(String name) throws IOException {
        Collection resources = new LinkedHashSet();
        Iterator i = childLoaders.iterator();
        while (i.hasNext()) {
            ClassLoader child = (ClassLoader) i.next();
            Collection childResources = Collections.list(child.getResources(name));
            resources.addAll(childResources);
        }
        Collection superResources = Collections.list(super.findResources(name));
        resources.addAll(superResources);
        return Collections.enumeration(resources);
    }
}
