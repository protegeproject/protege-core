package edu.stanford.smi.protege.server;

import java.io.*;
import java.net.*;
import java.rmi.registry.*;
import java.rmi.server.*;

import edu.stanford.smi.protege.util.*;

/*
 * This code is based on an idea from here:
 * http://www.javacoding.net/articles/technical/rmi-firewall.html
 * 
 * Author: Tim Goffings Date: Oct 3, 2002 - 3:51:34 PM
 */

public class ClientRmiSocketFactory extends RMISocketFactory {
    private static final String SERVER_PORT = "protege.rmi.server.port";
    private static final String SERVER_LOCAL_PORT = "protege.rmi.server.local.port";
    private static final String REGISTRY_PORT = "protege.rmi.registry.port";
    private static final String REGISTRY_LOCAL_PORT = "protege.rmi.registry.local.port";

    private int serverPort;
    private int serverLocalPort;
    private int registryPort;
    private int registryLocalPort;

    public ClientRmiSocketFactory() {
        serverPort = getPort(SERVER_PORT, 0);
        serverLocalPort = getPort(SERVER_LOCAL_PORT, 0);
        registryPort = getPort(REGISTRY_PORT, Registry.REGISTRY_PORT);
        registryLocalPort = getPort(REGISTRY_LOCAL_PORT, 0);
        if (!isDefault()) {
            Log.getLogger().config("server=" + serverPort + ", serverLocal= "
                    + serverLocalPort + ", registryPort=" + registryPort
                    + ", registryLocal=" + registryLocalPort);
        }
    }

    private int getPort(String name, int defaultValue) {
        Integer i = Integer.getInteger(name);
        return i == null ? defaultValue : i.intValue();
    }

    public Socket createSocket(String host, int port) throws IOException {
        int localPort;
        if (port == serverPort) {
            localPort = serverLocalPort;
        } else if (port == registryPort) {
            localPort = registryLocalPort;
        } else {
            localPort = 0;
        }
        Socket socket = createSocket(host, port, localPort);
        if (!isDefault()) {
            Log.getLogger().config("localPort=" + socket.getLocalPort());
        }
        return socket;
    }
    
    private Socket createSocket(String host, int hostPort, int localPort) throws IOException {
        SocketAddress serverAddress = new InetSocketAddress(host, hostPort);
        SocketAddress localAddress = new InetSocketAddress(localPort);
        Socket socket = new Socket();
        socket.setReuseAddress(true);
        socket.bind(localAddress);
        socket.connect(serverAddress);
        return socket;
    }

    private boolean isDefault() {
        return serverPort == 0 && serverLocalPort == 0
                && registryPort == Registry.REGISTRY_PORT
                && registryLocalPort == 0;
    }

    public ServerSocket createServerSocket(int requestedPort)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}