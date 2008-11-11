package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import edu.stanford.smi.protege.util.ApplicationProperties;

public class CompressingSocketFactory implements RMIClientSocketFactory, RMIServerSocketFactory, Serializable {
    
    public static final String USE_RMI_COMPRESSION = "protege.rmi.compression";

    public Socket createSocket(String host, int port) throws IOException {
        SocketAddress serverAddress = new InetSocketAddress(host, port);
        SocketAddress localAddress = new InetSocketAddress(0);
        Socket socket = new CompressingSocket();
        socket.setReuseAddress(true);
        socket.bind(localAddress);
        socket.connect(serverAddress);
        return socket;
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return new CompressingServerSocket(port);
    }
    
    public static boolean useCompression() {
        return ApplicationProperties.getBooleanProperty(USE_RMI_COMPRESSION, false);
    }

}
