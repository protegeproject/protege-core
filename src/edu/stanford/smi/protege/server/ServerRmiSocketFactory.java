package edu.stanford.smi.protege.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;

/* This code is based on an idea from here:
 * http://www.javacoding.net/articles/technical/rmi-firewall.html
 *
 * Author: Tim Goffings 
 * Date: Oct 3, 2002 - 3:51:34 PM 
 */

public class ServerRmiSocketFactory extends RMISocketFactory {
    private int fixedPort;

    public ServerRmiSocketFactory() {
        fixedPort = Integer.getInteger(ServerProperties.SERVER_PORT, 0).intValue();
        if (fixedPort != 0) {
            Log.getLogger().config("fixed port=" + fixedPort);
        }
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
        ServerSocket socket = new ServerSocket(port);
        if (fixedPort != 0) {
            Log.getLogger().config("local port: " + socket.getLocalPort());
        }
        return socket;
    }
    
    public String toString() {
        return StringUtilities.getClassName(this);
    }
}
