package edu.stanford.smi.protege.server.socket.deflate;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

import edu.stanford.smi.protege.util.Log;


public class CompressingOutputStream extends OutputStream {
    private static Logger log = Log.getLogger(CompressingOutputStream.class);
    
    public static int COMPRESSION_PAD = 1024;
    public static int BUFFER_SIZE = 128 * 1024;
    public static int KB = 1024;
    
    private OutputStream os;
    private Deflater deflater;
    
    private static int counter = 0;
    private int id;
    
    private byte buffer[] = new byte[BUFFER_SIZE];
    int offset = 0;
    
    int totalBytesWritten = 0;
    int totalCompressedBytesWritten = 0;
    
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
        try {
            if (offset > 0) {
                deflater.reset();
                deflater.setInput(buffer, 0, offset);
                deflater.finish();
                byte [] compressedBuffer = new byte[offset + COMPRESSION_PAD];
                deflater.deflate(compressedBuffer);
                if (!deflater.needsInput()) {
                    throw new IOException("Insufficient pad for compression");
                }
                int compressedSize = (int) deflater.getBytesWritten();
                PacketHeader header = new PacketHeader((int) deflater.getBytesRead(), 
                                                       (int) compressedSize);
                logPacket(compressedBuffer, compressedSize);
                header.write(os);
                os.write(compressedBuffer, 0, compressedSize);
            }
        }
        finally {
            offset = 0;
        }
        os.flush();
    }

    private void ensureBufferNotFull() throws IOException {
        if (offset >= BUFFER_SIZE) {
            flush();
        }
    }
    
    private void logPacket(byte [] compressedBuffer, int compressedSize) {
        logCompressionRatios(compressedSize);
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
            sb.append("Compressed buffer of size ");
            sb.append(compressedSize);
            sb.append(": ");
            for (int i = 0; i < compressedSize; i++) {
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

    private void logCompressionRatios(int compressedSize) {
        if (!log.isLoggable(Level.FINE)) {
            return;
        }
        int previousMB = totalBytesWritten / (KB * KB);
        totalBytesWritten += offset;
        totalCompressedBytesWritten += compressedSize;
        if (previousMB < (totalBytesWritten  / (KB * KB))) {
            log.fine(String.format("%d MBytes written: Compression ratio = %.3f to 1", 
                                   totalBytesWritten / (KB * KB),
                                   ((double) totalBytesWritten) / ((double) totalCompressedBytesWritten)));
        }
    }
}
