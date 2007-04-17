package edu.stanford.smi.protege.server;
//ESCA*JAVA0085

import java.net.*;
import java.rmi.server.*;

import edu.stanford.smi.protege.plugin.*;

/**
 * TODO Class Comment
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProtegeRmiClassLoaderSpi extends RMIClassLoaderSpi {

    public ProtegeRmiClassLoaderSpi() {
    }

    public Class loadClass(String codebase, String name, ClassLoader defaultLoader) throws MalformedURLException {
        Class clas;
        try {
            clas = sun.rmi.server.LoaderHandler.loadClass(codebase, name, defaultLoader);
        } catch (ClassNotFoundException e) {
            clas = PluginUtilities.forName(name, true);
        }
        return clas;
    }

    public Class loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader)
            throws MalformedURLException, ClassNotFoundException {
        return sun.rmi.server.LoaderHandler.loadProxyClass(codebase, interfaces, defaultLoader);
    }

    public ClassLoader getClassLoader(String codebase) throws MalformedURLException {
        return sun.rmi.server.LoaderHandler.getClassLoader(codebase);
    }

    public String getClassAnnotation(Class<?> cl) {
        return sun.rmi.server.LoaderHandler.getClassAnnotation(cl);
    }
}
