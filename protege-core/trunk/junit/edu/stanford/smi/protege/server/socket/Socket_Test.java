package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Random;
import java.util.logging.Level;

import junit.framework.TestCase;
import edu.stanford.smi.protege.server.socket.deflate.CompressingInputStream;
import edu.stanford.smi.protege.server.socket.deflate.CompressingOutputStream;
import edu.stanford.smi.protege.util.Log;

public class Socket_Test extends TestCase {
    private Random r = new Random();
    
    public void testReadWrite() throws IOException {
        debug();
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        CompressingInputStream cin = new CompressingInputStream(in);
        CompressingOutputStream cout = new CompressingOutputStream(out);
        for (int j = 0; j < 4; j++) {
            byte[] testBuffer = createTestBuffer(368);
            cout.write(testBuffer);
            cout.flush();
            byte[] bufferRead = new byte[1024];
            int bytesRead = cin.read(bufferRead);
            assertTrue(testBuffer.length == bytesRead);
            for (int i = 0; i < testBuffer.length; i++) {
                assertTrue(testBuffer[i] == bufferRead[i]);
            }
        }
    }
    
    public void testParticular() throws IOException {
        debug();
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        CompressingInputStream cin = new CompressingInputStream(in);
        CompressingOutputStream cout = new CompressingOutputStream(out);
        for (int j = 0; j < 3; j++) {
            byte[] testBuffer = {78, 0, 13, 49, 55,
                    49, 46, 54, 53, 46,
                    51, 50, 46, 49, 49,
                    49, 0, 0, -27, -85};
            cout.write(testBuffer);
            cout.flush();
            byte[] bufferRead = new byte[1024];
            int bytesRead = cin.read(bufferRead);
            assertTrue(testBuffer.length == bytesRead);
            for (int i = 0; i < testBuffer.length; i++) {
                assertTrue(testBuffer[i] == bufferRead[i]);
            }
        }
    }
    
    public void debug() {
        Log.setLoggingLevel(CompressingInputStream.class, Level.FINEST);
        Log.setLoggingLevel(CompressingOutputStream.class, Level.FINEST);
    }
    
    private byte [] createTestBuffer(int size) {
        byte [] testBuffer = new byte[size];
        r.nextBytes(testBuffer);
        return testBuffer;
    }

}
