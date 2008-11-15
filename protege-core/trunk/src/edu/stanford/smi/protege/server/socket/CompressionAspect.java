package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import edu.stanford.smi.protege.util.ApplicationProperties;

public class CompressionAspect implements StreamAspect, Serializable {
    public static final String USE_RMI_COMPRESSION = "protege.rmi.compression";

    public InputStream getInputStream(InputStream is) throws IOException {
        return new GZIPInputStream(is);
    }

    public OutputStream getOutputStream(OutputStream os) throws IOException {
        return new MyOutputStream(os);
    }
    
    public static boolean useCompression() {
        return ApplicationProperties.getBooleanProperty(USE_RMI_COMPRESSION, false);
    }
    
    
    public static class MyOutputStream extends GZIPOutputStream implements OutputStreamWithHooks {
        public MyOutputStream(OutputStream os) throws IOException {
            super(os);
        }
        
        public void socketCloseHook() throws IOException {
            flush();
        }
    }
}
