package edu.stanford.smi.protege.server.util;

import edu.stanford.smi.protege.server.util.FifoReader;
import edu.stanford.smi.protege.server.util.FifoWriter;
import junit.framework.TestCase;


public class FifoTestCase extends TestCase {
    
    public void testWriterFifo() {
        FifoWriter<Integer> writer = new FifoWriter<Integer>();
        FifoReader<Integer> reader1 = new FifoReader<Integer>(writer);
        writer.write(1);
        writer.write(2);
        writer.write(3);
        assertTrue(reader1.read() == 1);
        FifoReader<Integer> reader2 = new FifoReader<Integer>(writer);
        assertNull(reader2.read());
        assertTrue(reader1.read() == 2);
        writer.write(4);
        assertTrue(reader1.read() == 3);
        assertTrue(reader2.read() == 4);
        writer.write(5);
        assertTrue(reader1.read() == 4);
        writer.write(6);
        assertTrue(reader1.read() == 5);
        assertTrue(reader2.read() == 5);
        writer.write(7);
    }
    
    public void testReaderFifo() {
        FifoWriter<Integer> writer = new FifoWriter<Integer>();
        writer.write(1);
        FifoReader<Integer> reader1 = new FifoReader<Integer>(writer);
        writer.write(2);
        writer.write(3);
        assertTrue(reader1.read() == 2);
        FifoReader<Integer> reader2 = new FifoReader<Integer>(reader1);
        writer.write(4);
        assertTrue(reader2.read() == 3);
        assertTrue(reader1.read() == 3);
        writer.write(5);
        assertTrue(reader1.read() == 4);
        assertTrue(reader2.read() == 4);
    }
}
