package edu.stanford.smi.protege.server.socket.deflate;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

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
public class CompressingInputStream extends InputStream {
    private static Logger log = Log.getLogger(CompressingInputStream.class);
    
    protected InputStream is;
    
    protected byte buffer[];
    protected int offset;
    
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
            fillBuffer();
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
            fillBuffer();
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
    
    private void fillBuffer() throws IOException {
        buffer = null;
        offset = 0;
        PacketHeader header = PacketHeader.read(is);
        buffer = new byte[header.getSize()];
        fillBuffer(header);
    }
    
    protected void fillBuffer(PacketHeader header) throws IOException {
        inflater.reset();
        
        int compressedSize = header.getCompressedSize();
        byte compressedBuffer[] = new byte[compressedSize];

        readFully(compressedBuffer, compressedSize);
        
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
    
    protected void readFully(byte[] b, int len) throws IOException {
        int bytesRead = 0;
        while (bytesRead < len) {
            int readThisTime = is.read(b, bytesRead, len - bytesRead);
            if (readThisTime == -1) {
                throw new EOFException("Unabled to read entire compressed packet contents");
            }
            bytesRead += readThisTime;
        }
    }
    
    protected void logPacket(byte [] compressedBuffer) {
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
