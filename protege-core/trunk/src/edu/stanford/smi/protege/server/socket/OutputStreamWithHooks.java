package edu.stanford.smi.protege.server.socket;

import java.io.IOException;

public interface OutputStreamWithHooks {
    
    void socketCloseHook() throws IOException;

}
