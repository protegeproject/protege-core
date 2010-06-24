package edu.stanford.smi.protege.server.socket.deflate;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class PacketHeader {
    private static Logger log = Log.getLogger(PacketHeader.class);
    
    public static byte [] ALIGNMENT = { 0x4c, 0x3a, 0x74, 0x58 };
    
    private static int BYTES_IN_INT = 4;
    private static int BITS_IN_BYTE = 8;
    private static int BYTE_MASK = 0x0ff;
    
    private int size;
    private int compressedSize;
     
    public PacketHeader(int size, int compressedSize) {
        this.size = size;
        this.compressedSize = compressedSize;
    }

    public static PacketHeader read(InputStream is) throws IOException {
        for (byte b : ALIGNMENT) {
            int alignCheck = is.read();
            if (alignCheck == -1) {
                throw new EOFException("No packet found");
            }
            if ((byte) alignCheck != b) {
                throw new IOException("Packet header out of alignment between reader and writer (Thread = " + Thread.currentThread().getName() + ")");
            }
        }
       int size = readInt(is);
       int compressedSize = readInt(is);
       if (log.isLoggable(Level.FINEST)) {
           log.finest("Read compressed packet header, size = " + size + " compressed size = " + compressedSize);
       }
       return new PacketHeader(size, compressedSize);
    }
    
    public void write(OutputStream os) throws IOException {
        for (byte b : ALIGNMENT) {
            os.write(b);
        }
        writeInt(os, size);
        writeInt(os, compressedSize);
        if (log.isLoggable(Level.FINEST)) {
            log.finest("Wrote compressed packet header, size = " + size + " compressed size = " + compressedSize);
        }
    }

    public int getSize() {
        return size;
    }

    public int getCompressedSize() {
        return compressedSize;
    }
    
    private static int readInt(InputStream is) throws IOException {
        int result = 0;
        int[] buffer = new int[BYTES_IN_INT];
        for (int i = 0; i < BYTES_IN_INT; i++) {
            int c = is.read();
            if (c == -1) {
                throw new EOFException("Could not read compressed packet header");
            }        
            buffer[i] = c;
        }
        
        for (int i = BYTES_IN_INT - 1; i >= 0; i--) {
            result = result << BITS_IN_BYTE;
            int b = buffer[i];
            result += b < 0 ? 256 + b : b;
        }
        return result;
    }
    
    private static void writeInt(OutputStream os, int v) throws IOException {
        for (int i = 0; i < BYTES_IN_INT - 1; i++) {
            os.write(v & BYTE_MASK);
            v = v >> BITS_IN_BYTE;
        }
        os.write(v);
    }

}
