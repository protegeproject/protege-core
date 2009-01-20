/**
 * 
 */
package edu.stanford.smi.protege.server.socket.deflate;

import java.io.IOException;
import java.io.OutputStream;

public class CompressedBuffer {
    public static int PAD_SIZE = 1024;
    
    private byte[] buffer;
    private int size;
    
    public CompressedBuffer(int uncompresssedSize) {
        buffer = new byte[guessOutputBufferSize(uncompresssedSize)];
    }
    
    public void write(OutputStream os) throws IOException {
        os.write(buffer, 0, size);
    }
    
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    private int guessOutputBufferSize(int inputSize) {
        return inputSize + CompressedBuffer.PAD_SIZE;
    }
    
}