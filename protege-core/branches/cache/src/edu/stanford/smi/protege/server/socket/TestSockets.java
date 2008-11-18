package edu.stanford.smi.protege.server.socket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Random;

public class TestSockets {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        PipedOutputStream os = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream(os);
        Writer writer = new Writer(os, "/tmp/in");
        Reader reader = new Reader(is, "/tmp/out");
        new Thread(writer, "Writing Thread").start();
        new Thread(reader, "ReadingThread").start();
    }
    
    public static void copy01(InputStream is, OutputStream os) throws IOException {
        int c;
        float p = 0.001f;
        Random r = new Random();
        while ((c = is.read()) != -1) {
            os.write(c);
            if (r.nextFloat() < p) {
                os.flush();
            }
        }
        os.flush();
        os.close();
    }
    
    public static void copy02(InputStream is, OutputStream os) throws IOException {
        int bufsize = 64 * 1024;
        byte[] buffer = new byte[bufsize];
        Random r = new Random();
        while (true) {
            int offset = r.nextInt(bufsize - 2);
            int len  = r.nextInt(bufsize - offset);
            int realLength = is.read(buffer, offset, len);
            if (realLength == -1) {
                break;
            }
            os.write(buffer, offset, realLength);
        }
        os.flush();
        os.close();
    }
    
    public static class Writer implements Runnable {
        private InputStream is;
        private OutputStream os;

        public Writer(OutputStream os, String file) throws IOException {
            this.os = new CompressingOutputStream(os);
            this.is = new FileInputStream(file);
        }

        public void run() {
            try {
                copy01(is, os);
            }
            catch (Throwable ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static class Reader implements Runnable {
        private InputStream is;
        private OutputStream os;
        
        public Reader(InputStream is, String file) throws IOException {
            this.is = new CompressingInputStream(is);
            this.os = new FileOutputStream(file);
        }
        
        public void run() {
            try {
                copy02(is, os);
            }
            catch (Throwable ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
