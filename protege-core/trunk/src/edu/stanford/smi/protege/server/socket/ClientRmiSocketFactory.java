package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;

/*
 * TODO this code should be made more extensible (ssl+compression, pluggable socket factories...)
 */

/*
 * This code is based on an idea from here:
 * http://www.javacoding.net/articles/technical/rmi-firewall.html
 * 
 * Author: Tim Goffings Date: Oct 3, 2002 - 3:51:34 PM
 */

public class ClientRmiSocketFactory implements RMIClientSocketFactory, Serializable {
    private static final long serialVersionUID = -2237049150168090129L;
    // Port Settings
    public static final String SERVER_PORT = "protege.rmi.server.port";
    public static final String SERVER_SSL_PORT = "protege.rmi.server.ssl.port";
    public static final String REGISTRY_PORT = "protege.rmi.registry.port";

    private boolean use_ssl = false;
    private boolean use_compression = false;
    private RMIClientSocketFactory delegate;


    public ClientRmiSocketFactory(SSLFactory.Context context) {
        if (use_ssl = SSLFactory.useSSL(context)) {
            delegate = new SSLFactory();
        }
        else if (use_compression = CompressingSocketFactory.useCompression()) {
            delegate = new CompressingSocketFactory();
        }
        reportPorts();
    }

    public Socket createSocket(String host, int port) throws IOException {
        Socket socket;
        if (delegate != null) {
            return delegate.createSocket(host, port);
        }
        else {
            SocketAddress serverAddress = new InetSocketAddress(host, port);
            SocketAddress localAddress = new InetSocketAddress(0);
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(localAddress);
            socket.connect(serverAddress);
        }
        return socket;
    }
    
    public String toString() {
        return StringUtilities.getClassName(this);
    }
    

    
    public boolean equals(Object o) {
        if (!(o instanceof ClientRmiSocketFactory)) {
            return false;
        }
        ClientRmiSocketFactory other = (ClientRmiSocketFactory) o;
        return use_ssl == other.use_ssl && use_compression == other.use_compression;
    }
    
    public int hashCode() {
        return (use_ssl ? 1 : 0) + (use_compression ? 1 : 0);
    }
    
    private static int getPort(String name, int defaultValue) {
        Integer i = Integer.getInteger(name);
        return i == null ? defaultValue : i.intValue();
    }

    private static boolean portsReported = false;
    private void reportPorts() {
        if (!portsReported) {
            int serverPort   = getPort(SERVER_PORT, 0);
            int registryPort = getPort(REGISTRY_PORT, Registry.REGISTRY_PORT);
            if (!(serverPort == 0 && registryPort == Registry.REGISTRY_PORT)) {
                Log.getLogger().config("server=" + serverPort + ", registryPort=" + registryPort);
            }
            portsReported = true;
        }
    }
}