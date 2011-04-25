package edu.stanford.smi.protege.server.socket;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TeeOutputStream extends OutputStream {
    public static String TEE_OUTPUT_OPTION="edu.stanford.bmir.protege.server.tee.output";
    private OutputStream os;
    private OutputStream fileOutput;
    private static int counter = 0;
    
    public TeeOutputStream(OutputStream os) throws FileNotFoundException {
        this.os = os;
        String fileName = System.getProperty(TEE_OUTPUT_OPTION);
        synchronized (TeeOutputStream.class) {
            fileName = fileName + "-" + (counter++) + ".iostream";
        }
        File f = new File(fileName);
        fileOutput = new BufferedOutputStream(new FileOutputStream(f));
    }
    
    @Override
    public void write(int b) throws IOException {
        fileOutput.write(b);
        os.write(b);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        fileOutput.write(b, off, len);
        os.write(b, off, len);
    }
    
    @Override
    public void flush() throws IOException {
        fileOutput.flush();
        os.flush();
    }
    
    @Override
    public void close() throws IOException {
        fileOutput.flush();
        fileOutput.close();
        os.close();
    }

    
    
}
