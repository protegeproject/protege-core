package edu.stanford.smi.protege.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;

public class SSLSettings {
	private static Logger log = Log.getLogger(SSLSettings.class);
	
	public enum Context {
		NONE, LOGIN, ALWAYS;
	}
	private static Context policy;
	
    // SSL Settings
    public static final String SSL_POLICY="protege.rmi.ssl.policy";
    public static final String SSL_KEYSTORE = "protege.rmi.ssl.keystore";
    public static final String SSL_PASSWORD = "protege.rmi.ssl.password";
    
    public static final String KEYSTORE_TYPE = "protege.rmi.ssl.keystore.type";
    public static final String DEFAULT_KEYSTORE_TYPE="JKS";
    public static final String KEYMANAGER_ALGORITHM = "protege.rmi.ssl.keymanager.algorithm";
    public static final String DEFAULT_KEYMANAGER_ALGORITHM = "SunX509";
    public static final String SSL_PROTOCOL = "protege.rmi.ssl.protocol";
    public static final String DEFAULT_SSL_PROTOCOL = "TLS";
    
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
    
    public ServerSocket createSSLServerSocket(int port) throws IOException {
        if (factory == null) {
            initializeSSLServerFactory();
        }
        return factory.createServerSocket(port); 
    }
    
    public Socket createSSLClientSocket() throws IOException {
        return SSLSocketFactory.getDefault().createSocket();
    }
    
    public static File getKeyStore() throws IOException {
        String keystore = ApplicationProperties.getApplicationOrSystemProperty(SSL_KEYSTORE);
        if (keystore == null) {
            throw new IOException("keystore not specified. Set " + SSL_KEYSTORE);
        }
        return new File(keystore);
    }
    
    private static String getPassword() {
        return ApplicationProperties.getApplicationOrSystemProperty(SSL_PASSWORD);
    }
    
    public static String getKeystoreType() {
        return ApplicationProperties.getApplicationOrSystemProperty(KEYSTORE_TYPE, DEFAULT_KEYSTORE_TYPE);
    }
    
    public static String getKeymanagerAlgorithm() {
        return ApplicationProperties.getApplicationOrSystemProperty(KEYMANAGER_ALGORITHM, DEFAULT_KEYMANAGER_ALGORITHM);
    }
    
    public static String getSSLProtocol() {
        return ApplicationProperties.getApplicationOrSystemProperty(SSL_PROTOCOL, DEFAULT_SSL_PROTOCOL);
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

}
