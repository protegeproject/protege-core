package edu.stanford.smi.protege.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipSocket extends Socket {
  
  private InputStream in;
  private OutputStream out;
  
  public ZipSocket() {
    super();
  }
  
  public ZipSocket(String host, int port) throws UnknownHostException, IOException {
    super(host, port);
  }
  
  public InputStream getInputStream() throws IOException {
    if (in == null) {
      in = new ZipInputStream(super.getInputStream());
    }
    return in;
  }

  public OutputStream getOutputStream() throws IOException {
    if (out == null) {
      out = new ZipOutputStream(super.getOutputStream());
    }
    return out;
  }
  
  public synchronized void close() throws IOException {
    OutputStream o = getOutputStream();
    o.flush();
    super.close();
  }
}
