package edu.stanford.smi.protege.server;

import java.rmi.*;
import java.security.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class LaxSecurityManager extends RMISecurityManager {

    public void checkPropertiesAccess() {
        // do nothing
    }
    public void checkCreateClassLoader() {
        // do nothing
    }
    public void checkPermission(Permission p) {
        // do nothing
    }
    public void checkRead() {
        // do nothing
    }
    public void checkPropertyAccess(String s) {
        // do nothing
    }
    public void checkSetFactory() {
        // do nothing
    }
    public void checkConnect(String host, int port) {
        // do nothing
    }
    public void checkConnect(String host, int port, Object context) {
        // do nothing
    }
}
