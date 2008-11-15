package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.smi.protege.util.Log;

public class RmiSocketFactory implements RMIClientSocketFactory,
        RMIServerSocketFactory, Serializable {
    private static final long serialVersionUID = -2237049150168090129L;
    // Port Settings
    public static final String SERVER_PORT = "protege.rmi.server.port";
    public static final String SERVER_SSL_PORT = "protege.rmi.server.ssl.port";
    public static final String REGISTRY_PORT = "protege.rmi.registry.port";

    private boolean use_ssl = false;
    
    // Stream Factories starting with the innermost first.
    private List<StreamAspect> aspects = new ArrayList<StreamAspect>();
    
    public RmiSocketFactory(SSLFactory.Context context) {
        use_ssl = SSLFactory.useSSL(context);
        if (CompressionAspect.useCompression()) {
            aspects.add(new CompressionAspect());
        }
        reportPorts();
    }

    public Socket createSocket(String host, int port) throws IOException {
        SocketAddress serverAddress = new InetSocketAddress(host, port);
        SocketAddress localAddress = new InetSocketAddress(0);
        Socket socket = createBareSocket();
        socket.setReuseAddress(true);
        socket.bind(localAddress);
        socket.connect(serverAddress);
        return socket;
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port) {
            @Override
            public Socket accept() throws IOException {
                Socket socket = createBareSocket();
                implAccept(socket);
                return socket;
            }
        };
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof RmiSocketFactory)) {
            return false;
        }
        RmiSocketFactory other = (RmiSocketFactory) o;
        return use_ssl == other.use_ssl;
    }
    
    public int hashCode() {
        return (use_ssl ? 1 : 0);
    }

    
    private Socket createBareSocket() {
        return new Socket() {
            private InputStream is;
            private OutputStream os;
            private List<OutputStreamWithHooks> hooks = new ArrayList<OutputStreamWithHooks>();
            
            @Override
            public InputStream getInputStream() throws IOException {
                if (is == null) {
                    is = super.getInputStream();
                    for (StreamAspect aspect : aspects) {
                        is = aspect.getInputStream(is);
                    }
                }
                return is;
            }
            
            @Override
            public OutputStream getOutputStream() throws IOException {
                if (os == null) {
                    os = super.getOutputStream();
                    for (int i = aspects.size() - 1; i >= 0; i--) {
                        os = aspects.get(i).getOutputStream(os);
                        if (os instanceof OutputStreamWithHooks) {
                            hooks.add((OutputStreamWithHooks) os);
                        }
                    }
                }
                return os;
            }
            
            @Override
            public synchronized void close() throws IOException {
                for (int i = hooks.size() - 1; i >= 0; i--) {
                    hooks.get(i).socketCloseHook();
                }
                super.close();
            }
        };
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
    
    private static int getPort(String name, int defaultValue) {
        Integer i = Integer.getInteger(name);
        return i == null ? defaultValue : i.intValue();
    }
}
