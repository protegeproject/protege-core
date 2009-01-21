package edu.stanford.smi.protege.server.socket.deflate;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;

public class PacketHeader {
    private static Logger log = Log.getLogger(PacketHeader.class);
    
    private static byte ALIGNMENT = 0x4c;
    
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
       int alignCheck = is.read();
       if (alignCheck == -1) {
           throw new EOFException("No packet found");
       }
       if (alignCheck != ALIGNMENT) {
           throw new IOException("Packet header out of alignment between reader and writer");
       }
       int size = readInt(is);
       int compressedSize = readInt(is);
       if (log.isLoggable(Level.FINEST)) {
           log.finest("Read compressed packet header, size = " + size + " compressed size = " + compressedSize);
       }
       return new PacketHeader(size, compressedSize);
    }
    
    public void write(OutputStream os) throws IOException {
        os.write(ALIGNMENT);
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
