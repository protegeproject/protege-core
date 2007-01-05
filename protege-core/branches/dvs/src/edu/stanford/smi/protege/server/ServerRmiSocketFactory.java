package edu.stanford.smi.protege.server;

import java.io.*;
import java.net.*;
import java.rmi.server.*;

import edu.stanford.smi.protege.util.*;

/* This code is based on an idea from here:
 * http://www.javacoding.net/articles/technical/rmi-firewall.html
 *
 * Author: Tim Goffings 
 * Date: Oct 3, 2002 - 3:51:34 PM 
 */

public class ServerRmiSocketFactory extends RMISocketFactory {
    private static ServerRmiSocketFactory instance;
    private int fixedPort;

    private ServerRmiSocketFactory() {
        fixedPort = getServerPort();
        if (fixedPort != 0) {
            Log.getLogger().config("fixed port=" + fixedPort);
        }
    }
    
    public static ServerRmiSocketFactory getInstance() {
        if (instance == null) {
            instance = new ServerRmiSocketFactory();
        }
        return instance;
    }
    
    public static int getServerPort() {
        return Integer.getInteger("protege.rmi.server.port", 0).intValue();
    }
    
    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        if (fixedPort != 0) {
            Log.getLogger().config("local port: " + socket.getLocalPort());
        }
        return socket;
    }

    /*
     * This method gets passed a 0 to indicate "allocate any port".  If the user has specfied a
     * port then we use it.
     */
    public ServerSocket createServerSocket(int requestedPort) throws IOException {
        int port = requestedPort == 0 ? fixedPort : requestedPort;
        ServerSocket socket;
        if (SSLSettings.useSSL()) {
            socket = new SSLSettings().createSSLServerSocket(port);
        }
        else {
            socket = new ServerSocket(port);
        }
        if (fixedPort != 0) {
            Log.getLogger().config("local port: " + socket.getLocalPort());
        }
        return socket;
    }
    
    public String toString() {
        return StringUtilities.getClassName(this);
    }
}
