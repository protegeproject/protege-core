package edu.stanford.smi.protege.server.auth;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class ProtegeCallbackHandler implements CallbackHandler {
    private String user;
    private String password;
    
    public ProtegeCallbackHandler(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public void handle(Callback[] callbacks) throws IOException,
                                       UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length;  i++) {
            Callback callback = callbacks[i];
            if (callback instanceof NameCallback) {
                NameCallback ncb = (NameCallback) callback;
                String prompt = ncb.getPrompt().toLowerCase();
                if (prompt.contains("user")) {
                    ncb.setName(user);
                }
            }
            else if (callback instanceof PasswordCallback) {
                PasswordCallback pcb = (PasswordCallback) callback;
                pcb.setPassword(password.toCharArray());
            }
        }
    }

}
