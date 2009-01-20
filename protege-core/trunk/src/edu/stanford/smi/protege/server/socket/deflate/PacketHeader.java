package edu.stanford.smi.protege.server.socket.deflate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PacketHeader {
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
       int size = readInt(is);
       int compressedSize = readInt(is);
       return new PacketHeader(size, compressedSize);
    }
    
    public void write(OutputStream os) throws IOException {
        writeInt(os, size);
        writeInt(os, compressedSize);
    }

    public int getSize() {
        return size;
    }

    public int getCompressedSize() {
        return compressedSize;
    }
    
    private static int readInt(InputStream is) throws IOException {
        byte [] buffer = new byte[BYTES_IN_INT];
        int result = 0;
        is.read(buffer);
        int i = BYTES_IN_INT - 1;
        result = buffer[i--];
        for (; i >= 0; i--) {
            result = result << BITS_IN_BYTE;
            result += buffer[i];
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
