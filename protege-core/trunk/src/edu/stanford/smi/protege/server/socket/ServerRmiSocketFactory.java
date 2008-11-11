package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

import edu.stanford.smi.protege.util.StringUtilities;

/*
 * TODO this code should be made more extensible (ssl+compression, pluggable socket factories...)
 */

/* This code is based on an idea from here:
 * http://www.javacoding.net/articles/technical/rmi-firewall.html
 *
 * Author: Tim Goffings 
 * Date: Oct 3, 2002 - 3:51:34 PM 
 */

public class ServerRmiSocketFactory implements RMIServerSocketFactory {
	
    private boolean use_ssl = false;
    private boolean use_compression = false;
    private RMIServerSocketFactory delegate;

    public ServerRmiSocketFactory(SSLFactory.Context context) {
    	if (use_ssl = SSLFactory.useSSL(context)) {
    	    delegate = new SSLFactory();
    	}
    	else if (use_compression = CompressingSocketFactory.useCompression()) {
    	    delegate = new CompressingSocketFactory();
    	}
    }
    
    public static int getServerPort(SSLFactory.Context context) {
    	if (SSLFactory.useSSL(context)) {
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
        if (delegate != null) {
            socket = delegate.createServerSocket(port);
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
    	return use_ssl == other.use_ssl && use_compression == other.use_compression;
    }
    
    public int hashCode() {
    	return (use_ssl ? 1 : 0) + (use_compression ? 2 : 0);
    }
}
