package edu.stanford.smi.protege.server.socket;

import static edu.stanford.smi.protege.server.socket.deflate.HybridCompressingOutputStream.TOO_SMALL_TO_COMPRESS;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import edu.stanford.smi.protege.server.socket.deflate.CompressingInputStream;
import edu.stanford.smi.protege.server.socket.deflate.CompressingOutputStream;
import edu.stanford.smi.protege.server.socket.deflate.HybridCompressingInputStream;
import edu.stanford.smi.protege.server.socket.deflate.HybridCompressingOutputStream;
import edu.stanford.smi.protege.util.Log;

public class Socket_Test extends TestCase {
    private static Logger log = Log.getLogger(Socket_Test.class);
    private Random r = new Random();
    
    public void testReadWrite() throws IOException, InterruptedException {
        // debug();
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        CompressingInputStream cin = new CompressingInputStream(in);
        CompressingOutputStream cout = new CompressingOutputStream(out);
        for (int j = 0; j < 3; j++) {
            doReadWriteAndCheck(cin, cout, createTestBuffer(368));
        }
        cleanup(cin, cout);
    }

    public void testNegativeSize() throws IOException, InterruptedException {
        // debug();
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        CompressingInputStream cin = new CompressingInputStream(in);
        CompressingOutputStream cout = new CompressingOutputStream(out);
        for (int j = 0; j < 4; j++) {
            doReadWriteAndCheck(cin, cout, createTestBuffer(250));
        }
        for (int j = 0; j < 4; j++) {
            doReadWriteAndCheck(cin, cout, createTestBuffer(255));
        }
        for (int j = 0; j < 4; j++) {
            doReadWriteAndCheck(cin, cout, createTestBuffer(512 + 250));
        }
        cleanup(cin, cout);
    }
    
    public void testParticular() throws IOException, InterruptedException {
        // debug();
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        CompressingInputStream cin = new CompressingInputStream(in);
        CompressingOutputStream cout = new CompressingOutputStream(out);
        for (int j = 0; j < 3; j++) {
            byte[] testBuffer = {78, 0, 13, 49, 55,
                    49, 46, 54, 53, 46,
                    51, 50, 46, 49, 49,
                    49, 0, 0, -27, -85};
            doReadWriteAndCheck(cin, cout, testBuffer);
        }
        cleanup(cin, cout);
    }
    
    public void testUnbuffered() throws IOException, InterruptedException {
        // debug();
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        DebufferingInputStream din = new DebufferingInputStream(in);
        CompressingInputStream cin = new CompressingInputStream(din);
        CompressingOutputStream cout = new CompressingOutputStream(out);
        for (int j = 0; j < 3; j++) {
            doReadWriteAndCheck(cin, cout, createTestBuffer(368));
        }
        cleanup(cin,cout);
    }
    
    public void badTestHybrid() throws IOException, InterruptedException {
        // debug();
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        HybridCompressingInputStream cin = new HybridCompressingInputStream(in);
        HybridCompressingOutputStream cout = new HybridCompressingOutputStream(out);
        
        int [] sizes = { TOO_SMALL_TO_COMPRESS/2, 
                         TOO_SMALL_TO_COMPRESS -2, TOO_SMALL_TO_COMPRESS -1,
                         TOO_SMALL_TO_COMPRESS,
                         TOO_SMALL_TO_COMPRESS + 1,
                         TOO_SMALL_TO_COMPRESS + 2,
                         2 * TOO_SMALL_TO_COMPRESS
        };  
        for (int size : sizes) {
            for (int j = 0; j < 3; j++) {
                doReadWriteAndCheck(cin, cout, createTestBuffer(size));
            }
        }
        cleanup(cin, cout);
    }
    
    public void badTestHybridUnbuffered() throws IOException, InterruptedException {
        // debug();
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        DebufferingInputStream din = new DebufferingInputStream(in);
        HybridCompressingInputStream cin = new HybridCompressingInputStream(din);
        HybridCompressingOutputStream cout = new HybridCompressingOutputStream(out);
        
        int [] sizes = { TOO_SMALL_TO_COMPRESS/2, 
                         TOO_SMALL_TO_COMPRESS -2, TOO_SMALL_TO_COMPRESS -1,
                         TOO_SMALL_TO_COMPRESS,
                         TOO_SMALL_TO_COMPRESS + 1,
                         TOO_SMALL_TO_COMPRESS + 2,
                         2 * TOO_SMALL_TO_COMPRESS
        };  
        for (int size : sizes) {
            for (int j = 0; j < 3; j++) {
                doReadWriteAndCheck(cin, cout, createTestBuffer(size));
            }
        }
        cleanup(cin, cout);
    }
    
    public void doReadWriteAndCheck(InputStream is, OutputStream os, byte[] buffer) 
    throws IOException, InterruptedException {
        try {
            byte[] bufferRead = new byte[buffer.length];
            ReadRunnable reader = new ReadRunnable(is, bufferRead);
            Thread th = new Thread(reader, "Read Test Thread");
            th.start();
            Thread.sleep(1000); // awkward - reader has to be blocked...
                                // there doesn't seem to be an advertised 
                                // way to do this.
            os.write(buffer);
            os.flush();
            th.join(7000);
            assertTrue(reader.isDone());
            if (reader.getError() != null) {
                log.log(Level.WARNING, "Exception caught in other thread", reader.getError());
                fail();
            }
            for (int i = 0; i < buffer.length; i++) {
                assertTrue(buffer[i] == bufferRead[i]);
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
            log.log(Level.WARNING, "Junit test failed because of exception thrown", t);
        }
    }
    
    public static class ReadRunnable implements Runnable {
        private Throwable error;
        private boolean done = false;
        private byte [] buffer;
        private InputStream is;
        
        public ReadRunnable(InputStream is, byte[] buffer) {
            this.is = is;
            this.buffer = buffer;
        }

        public void run() {
            try {
                int len = buffer.length;
                int bytesRead = 0;
                while (bytesRead < len) {
                    int readThisTime = is.read(buffer, bytesRead, len-bytesRead);
                    if (readThisTime == -1) {
                        throw new EOFException("Didn't read to end of stream");
                    }
                    bytesRead += readThisTime;
                }
            }
            catch (Throwable t) {
                synchronized (this) {
                    error = t;
                }
                log.log(Level.WARNING, "Exception caught in reader thread", t);
            }
            synchronized (this) {
                done = true;
            }
        }
        
        public synchronized void waitUntilReady() {
            
        }

        public synchronized Throwable getError() {
            return error;
        }
        
        public synchronized boolean isDone() {
            return done;
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
    
    private void cleanup(InputStream in, OutputStream os)  {
        try {
            os.close();
        }
        catch (Throwable t) {
            log.log(Level.FINE, "Exception caught closing output stream", t);
        }
        try {
            in.close();
        }
        catch (Throwable t) {
            log.log(Level.FINE, "Exception caught closing input stream", t);
        }     
    }

}
