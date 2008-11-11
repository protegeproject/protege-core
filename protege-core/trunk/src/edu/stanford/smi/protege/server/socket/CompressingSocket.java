package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressingSocket extends Socket {
    
    public CompressingSocket() {

    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ZipInputStream(super.getInputStream());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new ZipOutputStream(super.getOutputStream());
    }
}
