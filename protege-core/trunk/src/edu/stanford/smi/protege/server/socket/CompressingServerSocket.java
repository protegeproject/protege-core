package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class CompressingServerSocket extends ServerSocket {
    
    public CompressingServerSocket(int port) throws IOException {
        super(port);
    }
    
    @Override
    public Socket accept() throws IOException {
        Socket compressingSocket = new CompressingSocket();
        implAccept(compressingSocket);
        return compressingSocket;
    }

}
