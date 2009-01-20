package edu.stanford.smi.protege.server.socket.deflate;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

import edu.stanford.smi.protege.util.Log;


public class CompressingOutputStream extends OutputStream {
    private static Logger log = Log.getLogger(CompressingOutputStream.class);
    
    public static int BUFFER_SIZE = 128 * 1024;
    private OutputStream os;
    private Deflater deflater;
    
    private static int counter = 0;
    private int id;
    
    private byte buffer[] = new byte[BUFFER_SIZE];
    int offset = 0;
    
    public CompressingOutputStream(OutputStream os) {
        this.os = os;
        deflater = new Deflater();
        synchronized (CompressingOutputStream.class) {
            id = counter++;
        }
    }

    @Override
    public void write(int b) throws IOException {
        ensureBufferNotFull();
        buffer[offset++] = (byte) b;
        ensureBufferNotFull();
    }
    
    @Override
    public void flush() throws IOException {
        if (offset > 0) {
            deflater.reset();
            deflater.setInput(buffer, 0, offset);
            deflater.finish();
            List<CompressedBuffer> outputBuffers = getCompressedBuffers();
            PacketHeader header = new PacketHeader((int) deflater.getBytesRead(), 
                                                   (int) deflater.getBytesWritten());
            logPacket(outputBuffers);
            header.write(os);
            for (CompressedBuffer compressed : outputBuffers) {
                compressed.write(os);
            }
        }
        os.flush();
    }
    
    public List<CompressedBuffer> getCompressedBuffers() {
        List<CompressedBuffer> buffers = new ArrayList<CompressedBuffer>();
        CompressedBuffer compressed = new CompressedBuffer(offset);
        int compressedSize = deflater.deflate(compressed.getBuffer());
        compressed.setSize(compressedSize);
        buffers.add(compressed);
        while (!deflater.needsInput()) {
            if (log.isLoggable(Level.FINEST)) {
                log.finest("Making extra compressed buffers - insufficient pad");
            }
            compressed = new CompressedBuffer(0);
            compressedSize = deflater.deflate(compressed.getBuffer());
            compressed.setSize(compressedSize);
            buffers.add(compressed);
        }
        return buffers;
    }
    
    private void ensureBufferNotFull() throws IOException {
        if (offset >= BUFFER_SIZE) {
            flush();
        }
    }
    
    private void logPacket(List<CompressedBuffer> buffers) {
        if (!log.isLoggable(Level.FINEST)) {
            return;
        }
        try {
          log.finest("----------------------------------------");
          log.finest("Outgoing packet for writer " + id);
          StringBuffer sb = new StringBuffer();
          sb.append("Uncompressed buffer of size ");
          sb.append(offset);
          sb.append(": ");
          for (int i = 0; i < offset; i++) {
              sb.append(buffer[i]);
              sb.append(" ");
          }
          log.finest(sb.toString());
          sb = new StringBuffer();
          sb.append("Compressed buffer: ");
          for (CompressedBuffer cbuffer : buffers) {
              for (int i = 0; i < cbuffer.getSize(); i++) {
                  sb.append(cbuffer.getBuffer()[i]);
                  sb.append(" ");
              }
          }
          log.finest(sb.toString());
          log.finest("----------------------------------------");
        }
        catch (Throwable t) {
            log.finest("Could not log exitting packet");
        }
    }

}
