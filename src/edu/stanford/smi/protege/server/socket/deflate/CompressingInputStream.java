package edu.stanford.smi.protege.server.socket.deflate;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import edu.stanford.smi.protege.util.Log;

public class CompressingInputStream extends InputStream {
    private static Logger log = Log.getLogger(CompressingInputStream.class);
    
    private InputStream is;
    
    private byte buffer[];
    private int offset;
    
    private Inflater inflater;
    
    private static int counter = 0;
    private int id;
    
    public CompressingInputStream(InputStream is) {
        this.is = is;
        inflater = new Inflater();
        synchronized (CompressingInputStream.class) {
            id = counter++;
        }
    }

    @Override
    public int read() throws IOException {
        if (buffer == null || offset == buffer.length) {
            readBuffer();
        }
        if (buffer == null) {
            return -1;
        }
        return buffer[offset++];
    }
    
    public void readBuffer() throws IOException {
        inflater.reset();
        buffer = null;
        PacketHeader header = PacketHeader.read(is);
        buffer = new byte[header.getSize()];
        byte compressedBuffer[] = new byte[header.getCompressedSize()];
        int bytesRead = is.read(compressedBuffer);
        if (bytesRead != header.getCompressedSize()) {
            throw new IOException("Incomplete compressed buffer.  Expected " 
                                  + header.getCompressedSize() 
                                  + " bytes but found " 
                                  + bytesRead 
                                  + " bytes");
        }
        inflater.setInput(compressedBuffer);
        try {
            int inflatedSize = inflater.inflate(buffer);
            if (inflatedSize != header.getSize()) {
                throw new IOException("Inflated to the wrong size, expected " 
                                      + header.getSize() 
                                      + " bytes but got " 
                                      + inflatedSize
                                      + " bytes");
            }
        }
        catch (DataFormatException dfe) {
            IOException ioe = new IOException("Compressed Data format bad: " + dfe.getMessage());
            ioe.initCause(dfe);
            throw ioe;
        }
        if (!inflater.needsInput()) {
            throw new IOException("Inflater thinks that there is more data to decompress");
        }
        logPacket(compressedBuffer);
    }
    
    private void logPacket(byte [] compressedBuffer) {
        if (!log.isLoggable(Level.FINEST)) {
            return;
        }
        try {
          log.finest("----------------------------------------");
          log.finest("Incoming packet for reader " + id);
          StringBuffer sb = new StringBuffer();
          sb.append("Uncompressed buffer of size ");
          sb.append(buffer.length);
          sb.append(": ");
          for (int i = 0; i < offset; i++) {
              sb.append(buffer[i]);
              sb.append(" ");
          }
          log.finest(sb.toString());
          sb = new StringBuffer();
          sb.append("Compressed buffer of size ");
          sb.append(compressedBuffer.length);
          sb.append(": ");
          for (int i = 0; i < compressedBuffer.length; i++) {
              sb.append(compressedBuffer[i]);
              sb.append(" ");
          }
          log.finest(sb.toString());
          log.finest("----------------------------------------");
        }
        catch (Throwable t) {
            log.finest("Could not log exitting packet");
        }
    }

}
