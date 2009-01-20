package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Random;

import junit.framework.TestCase;
import edu.stanford.smi.protege.server.socket.deflate.CompressingInputStream;
import edu.stanford.smi.protege.server.socket.deflate.CompressingOutputStream;

public class Socket_Test extends TestCase {
    private Random r = new Random();
    
    public void testReadWrite() throws IOException {
        for (int j = 0; j < 4; j++) {
            byte[] testBuffer = createTestBuffer(368);
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(in);
            CompressingInputStream cin = new CompressingInputStream(in);
            CompressingOutputStream cout = new CompressingOutputStream(out);
            cout.write(testBuffer);
            cout.flush();
            byte[] bufferRead = new byte[1024];
            cin.read(bufferRead);
            assertTrue(testBuffer.length == bufferRead.length);
            for (int i = 0; i < testBuffer.length; i++) {
                assertTrue(testBuffer[i] == bufferRead[i]);
            }
        }
    }
    
    private byte [] createTestBuffer(int size) {
        byte [] testBuffer = new byte[size];
        r.nextBytes(testBuffer);
        return testBuffer;
    }

}
