package edu.stanford.smi.protege.server.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ZipServerSocket extends ServerSocket {
  
  public ZipServerSocket(int port) throws IOException {
    super(port);
  }
  
  public Socket accept() throws IOException {
    ZipSocket socket = new ZipSocket();
    implAccept(socket);
    return socket;
  }

}
