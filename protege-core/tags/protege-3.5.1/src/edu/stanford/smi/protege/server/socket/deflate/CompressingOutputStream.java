package edu.stanford.smi.protege.server.socket.deflate;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;

import edu.stanford.smi.protege.util.Log;

/**
 * This code is based on the ideas presented in {@link http://javatechniques.com/blog/compressing-data-sent-over-a-socket/} by 
 * Philip Isenhour.  I am very grateful for the approach that he presented.  The key idea is to avoid using the GZip and the Zip streams
 * and to use the Deflater and Inflater methods directly.  In addition, Philip Isenhour essentially defines a packet type that has a 
 * header indicating the size and compressed size of the packet contents.  I took these two key ideas and wrote the following code 
 * by scratch without reference to Philip Isenhour's documents.  I think that some version of Philip Isenhour's ideas should find their way 
 * into the core java libraries because otherwise people will continue struggling with this problem.
 * <p>
 * I have tried several other approaches to a compressing input and compressing output stream.  The first approach was to base the 
 * input and output streams on the GZip input and output stream.  There are web pages on the internet that suggest that calling the GZipOutputStream's
 * finish() method during the flush() will work.  I had trouble with this approach when a write occurs on the stream after
 * the flush() (which calls finish()). I would get exceptions indicating that the GZip Output stream was finished and therefore unwriteable.
 * <p>
 * I then tried to use the ZIPInput/OutputStreams.  I would flush data by creating a ZipEntry and writing it out.  This approach
 * actually worked very well.  But it had a mysterious bug where some data was either not fully written out or not read. In the rmi
 * context things would hang.  This bug was relatively rare and only happened on certain machines.  I never found out what the problem was.
 * <p>
 * The beauty of Philip Isenhour's approach is that the developer can completely control how data is flushed and fully written out.  The developer
 * can also ensure that on the read method all the data is fully read.  So there should not be any more rmi hangs.  The only issue is whether the 
 * deflate/inflate logic is correct.  This is pretty thoroughly tested in our server-client testing (though there are *always* bugs hidden somewhere).
 * 
 * @author tredmond
 *
 */
public class CompressingOutputStream extends OutputStream {
    private static Logger log = Log.getLogger(CompressingOutputStream.class);
    
    public static int COMPRESSION_PAD = 1024;
    public static int BUFFER_SIZE = 128 * 1024;
    public static int KB = 1024;
    
    protected OutputStream os;
    private Deflater deflater;
    
    private static int counter = 0;
    private int id;
    
    protected byte buffer[] = new byte[BUFFER_SIZE];
    protected int offset = 0;
    
    private static int totalBytesWritten = 0;
    private static int totalCompressedBytesWritten = 0;
    
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
            if (offset == 0) {
                ;
            }
            else {
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
    
    protected void logPacket(byte [] compressedBuffer, int compressedSize) {
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
            if (compressedSize > offset) {
                sb.append(" (compression increased size)");
            }
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
        synchronized (CompressingOutputStream.class) {
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
}
