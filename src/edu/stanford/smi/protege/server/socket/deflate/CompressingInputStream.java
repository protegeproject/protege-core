package edu.stanford.smi.protege.server.socket.deflate;

import java.io.EOFException;
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
        if (buffer == null) {
            readBuffer();
        }
        if (buffer == null) {
            return -1;
        }
        int ret = buffer[offset++];
        if (buffer.length == offset) {
            buffer = null;
        }
        return ret;
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (buffer == null) {
            readBuffer();
        }
        if (buffer == null) {
            return -1;
        }
        int bytesRead = 0;
        for (bytesRead = 0; offset < buffer.length && bytesRead < len; bytesRead++) {
            b[off++] = buffer[offset++];
        }
        if (buffer.length == offset) {
            buffer = null;
        }
        return bytesRead;
    }
    
    public void readBuffer() throws IOException {
        int size = offset;
        buffer = null;
        offset = 0;
        
        inflater.reset();
        
        PacketHeader header = PacketHeader.read(is);
        int compressedSize = header.getCompressedSize();
        buffer = new byte[header.getSize()];
        byte compressedBuffer[] = new byte[compressedSize];
              
        int bytesRead = 0;
        while (bytesRead < compressedSize) {
            int readThisTime = is.read(compressedBuffer, bytesRead, compressedSize - bytesRead);
            if (readThisTime == -1) {
                throw new EOFException("Unabled to read entire compressed packet contents");
            }
            bytesRead += readThisTime;
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
          for (int i = 0; i < buffer.length; i++) {
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
