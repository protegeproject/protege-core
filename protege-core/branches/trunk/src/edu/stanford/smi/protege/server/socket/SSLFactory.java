package edu.stanford.smi.protege.server.socket;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;

public class SSLFactory implements RMIClientSocketFactory, RMIServerSocketFactory {
	private static Logger log = Log.getLogger(SSLFactory.class);
	
    private static Set<Thread> authorized = new HashSet<Thread>();
	
	public enum Context {
		NONE, LOGIN, ALWAYS;
	}
	private static Context policy;
	
    // SSL Settings
    public static final String SSL_POLICY   = "protege.rmi.ssl.policy";
    public static final String SSL_KEYSTORE = "protege.rmi.ssl.keystore";
    public static final String SSL_PASSWORD = "protege.rmi.ssl.password";
    
    public static final String KEYSTORE_TYPE                = "protege.rmi.ssl.keystore.type";
    public static final String DEFAULT_KEYSTORE_TYPE        = "JKS";
    public static final String KEYMANAGER_ALGORITHM         = "protege.rmi.ssl.keymanager.algorithm";
    public static final String DEFAULT_KEYMANAGER_ALGORITHM = "SunX509";
    public static final String SSL_PROTOCOL                 = "protege.rmi.ssl.protocol";
    public static final String DEFAULT_SSL_PROTOCOL         = "TLS";
    
    private SSLServerSocketFactory factory;
    
    private void initializeSSLServerFactory() throws IOException {
        if (factory != null) return;
        try {
            SSLContext ctx;
            KeyManagerFactory kmf;
            KeyStore ks;

            char[] passphrase = getPassword().toCharArray();
            ks = KeyStore.getInstance(getKeystoreType());
            ks.load(new FileInputStream(getKeyStore()), passphrase);

            kmf = KeyManagerFactory.getInstance(getKeymanagerAlgorithm());
            kmf.init(ks, passphrase);

            ctx = SSLContext.getInstance(getSSLProtocol());
            ctx.init(kmf.getKeyManagers(), null, null);

            factory = ctx.getServerSocketFactory();
        } 
        catch (Exception e) {
            IOException ioe = new IOException("Could not initialize ssl socket factory " + e);
            ioe.initCause(e);
            throw ioe;
        }
    }
    
    public ServerSocket createServerSocket(int port) throws IOException {
        if (factory == null) {
            initializeSSLServerFactory();
        }
        return factory.createServerSocket(port); 
    }
    
    public Socket createSocket(String host, int port) throws IOException {
        SocketAddress serverAddress = new InetSocketAddress(host, port);
        SocketAddress localAddress = new InetSocketAddress(0);
        Socket socket =  SSLSocketFactory.getDefault().createSocket();
        socket.setReuseAddress(true);
        socket.bind(localAddress);
        socket.connect(serverAddress);
        authorized.add(Thread.currentThread());
        return socket;
    }
    
    public static int getServerPort(Context context) {
    	if (useSSL(context)) {
    		return Integer.getInteger(RmiSocketFactory.SERVER_SSL_PORT, 0).intValue();
    	}
        return Integer.getInteger(RmiSocketFactory.SERVER_PORT, 0).intValue();
    }

    public static boolean useSSL(Context context) {
        if (policy == null) {
            policy = Context.NONE;
            String when = ApplicationProperties.getApplicationOrSystemProperty(SSL_POLICY);
            if (when != null) {
                when = when.toUpperCase();
                if (when.equals("NONE")) policy = Context.NONE;
                else if (when.equals("LOGIN")) policy = Context.LOGIN;
                else policy = Context.ALWAYS;
                if (policy != Context.NONE) Log.getLogger().config("SSL policy set to " + policy);
            }
        }
        boolean usessl = (context.compareTo(policy) <= 0);
        if (log.isLoggable(Level.FINE)) {
            log.fine("Policy = " + policy + " context = " + context + " use ssl = " + usessl);
        }
        return usessl;
    }
    
    public static void resetAuth() {
        authorized.remove(Thread.currentThread());
    }
    
    public static boolean checkAuth() throws SecurityException {
        return authorized.contains(Thread.currentThread());
    }
    
    private static File getKeyStore() throws IOException {
        String keystore = ApplicationProperties.getApplicationOrSystemProperty(SSL_KEYSTORE);
        if (keystore == null) {
            throw new IOException("keystore not specified. Set " + SSL_KEYSTORE);
        }
        return new File(keystore);
    }
    
    private static String getPassword() {
        return ApplicationProperties.getApplicationOrSystemProperty(SSL_PASSWORD);
    }
    
    private static String getKeystoreType() {
        return ApplicationProperties.getApplicationOrSystemProperty(KEYSTORE_TYPE, DEFAULT_KEYSTORE_TYPE);
    }
    
    private static String getKeymanagerAlgorithm() {
        return ApplicationProperties.getApplicationOrSystemProperty(KEYMANAGER_ALGORITHM, DEFAULT_KEYMANAGER_ALGORITHM);
    }
    
    private static String getSSLProtocol() {
        return ApplicationProperties.getApplicationOrSystemProperty(SSL_PROTOCOL, DEFAULT_SSL_PROTOCOL);
    }
    


}
