package edu.stanford.smi.protege.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;

/*
 * This code is based on an idea from here:
 * http://www.javacoding.net/articles/technical/rmi-firewall.html
 * 
 * Author: Tim Goffings Date: Oct 3, 2002 - 3:51:34 PM
 */

public class ClientRmiSocketFactory implements RMIClientSocketFactory, Serializable {
    private static final long serialVersionUID = 1237035652027282759L;
    
    private static ClientRmiSocketFactory instance;
    // Port Settings
    public static final String SERVER_PORT = "protege.rmi.server.port";
    public static final String REGISTRY_PORT = "protege.rmi.registry.port";

    private int serverPort;
    private int registryPort;
    private boolean use_ssl;
    
    private static Set<Thread> authorized = new HashSet<Thread>();

    private ClientRmiSocketFactory() {
        serverPort   = getPort(SERVER_PORT, 0);
        registryPort = getPort(REGISTRY_PORT, Registry.REGISTRY_PORT);
        use_ssl      = SSLSettings.useSSL();
        if (!isDefault()) {
            Log.getLogger().config("server=" + serverPort + ", registryPort=" + registryPort);
        }
    }
    
    public static ClientRmiSocketFactory getInstance() {
        if (instance == null) {
            instance = new ClientRmiSocketFactory();
        }
        return instance;
    }

    private static int getPort(String name, int defaultValue) {
        Integer i = Integer.getInteger(name);
        return i == null ? defaultValue : i.intValue();
    }

    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = createSocket(host, port, 0);
        return socket;
    }

    private Socket createSocket(String host, int hostPort, int localPort) throws IOException {
        SocketAddress serverAddress = new InetSocketAddress(host, hostPort);
        SocketAddress localAddress = new InetSocketAddress(localPort);
        Socket socket;
        if (use_ssl) {
            socket = new SSLSettings().createSSLClientSocket();
        }
        else {
            socket = new Socket();
        }
        socket.setReuseAddress(true);
        socket.bind(localAddress);
        socket.connect(serverAddress);
        if (use_ssl) {
            authorized.add(Thread.currentThread());
        }
        return socket;
    }

    private boolean isDefault() {
        return serverPort == 0 && registryPort == Registry.REGISTRY_PORT;
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
    
    public static void resetAuth() {
        authorized.remove(Thread.currentThread());
    }
    
    public static boolean checkAuth() throws SecurityException {
        return authorized.contains(Thread.currentThread());
    }
}