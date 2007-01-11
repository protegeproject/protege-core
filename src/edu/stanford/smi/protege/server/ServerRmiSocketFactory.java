package edu.stanford.smi.protege.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;

/* This code is based on an idea from here:
 * http://www.javacoding.net/articles/technical/rmi-firewall.html
 *
 * Author: Tim Goffings 
 * Date: Oct 3, 2002 - 3:51:34 PM 
 */

public class ServerRmiSocketFactory implements RMIServerSocketFactory {
	
    private boolean use_ssl;

    public ServerRmiSocketFactory(SSLSettings.Context context) {
    	use_ssl = SSLSettings.useSSL(context);
        if (getServerPort(context) != 0) {
            Log.getLogger().config("fixed port=" + getServerPort(context));
        }
    }
    
    public static int getServerPort(SSLSettings.Context context) {
    	if (SSLSettings.useSSL(context)) {
    		return Integer.getInteger(ClientRmiSocketFactory.SERVER_SSL_PORT, 0).intValue();
    	}
        return Integer.getInteger(ClientRmiSocketFactory.SERVER_PORT, 0).intValue();
    }

    /*
     * This method gets passed a 0 to indicate "allocate any port".  If the user has specfied a
     * port then we use it.
     */
    public ServerSocket createServerSocket(int port) throws IOException {
        ServerSocket socket;
        if (use_ssl) {
            socket = new SSLSettings().createSSLServerSocket(port);
        }
        else {
            socket = new ServerSocket(port);
        }
        return socket;
    }
    
    public String toString() {
        return StringUtilities.getClassName(this);
    }
    
    public boolean equals(Object o) {
    	if (!(o instanceof ServerRmiSocketFactory)) {
    		return false;
    	}
    	ServerRmiSocketFactory other = (ServerRmiSocketFactory) o;
    	return use_ssl == other.use_ssl;
    }
    
    public int hashCode() {
    	return use_ssl ? 1 : 0;
    }
}
