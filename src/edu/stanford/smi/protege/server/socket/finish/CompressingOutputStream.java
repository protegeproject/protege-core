package edu.stanford.smi.protege.server.socket.finish;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.util.Log;

public class CompressingOutputStream extends OutputStream {
    private static Logger log = Log.getLogger(CompressingOutputStream.class);
    public static final int BUFFER_SIZE = 1024 * 1024;
    public static final int BYTES_IN_SIZE = 4;
    public static final int BYTE_MASK = 0x0ff;
    
    private OutputStream os;
    private int smallSize = ServerProperties.tooSmallToCompress();
    private byte[] buffer = new byte[BUFFER_SIZE];
    private int offset = 0;
    
    public CompressingOutputStream(OutputStream os) {
        this.os = os;
    }

    @Override
    public void write(int b) throws IOException {
        ensureNotFull();
        buffer[offset++] = (byte) b;
        ensureNotFull();
    }
    
    @Override
    public void flush() throws IOException {
        if (offset > 0) {
            int d = offset;
            for (int i = 0; i < BYTES_IN_SIZE; i++) {
                os.write(d & BYTE_MASK);
                d = d >> 8;
            }
            GZIPOutputStream compressing = new GZIPOutputStream(os);
            compressing.write(buffer, 0, offset);
            compressing.finish();
            os.flush();
        }
    }
    
    private void ensureNotFull() throws IOException {
        if (offset >= BUFFER_SIZE) {
            flush();
        }
    }

}
