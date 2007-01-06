package edu.stanford.smi.protege.server.auth;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import edu.stanford.smi.protege.server.Server;

public class ProtegeLoginModule implements LoginModule {
    private CallbackHandler callbackHandler;
    private String user;
    private String password;

    public boolean abort() throws LoginException {
        return true;
    }

    public boolean commit() throws LoginException {
        return true;
    }

    public void initialize(Subject subject, 
                           CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, 
                           Map<String, ?> options) {
        this.callbackHandler = callbackHandler;
    }

    public boolean login() throws LoginException {
        try {
            NameCallback     ncb = new NameCallback("Username");
            PasswordCallback pcb = new PasswordCallback("Password", false);
            callbackHandler.handle(new Callback[] {ncb, pcb});
            user = ncb.getName();
            if (pcb.getPassword() != null) {
                password = new String(pcb.getPassword());
            }
            if (user == null || password == null) {
                return false;
            }
            Server server = Server.getInstance();
            return server.metaprojectAuthCheck(user, password);
        } 
        catch (Exception e) {
            LoginException le = new LoginException("Login error " + e);
            le.initCause(e);
            throw le;           
        }
    }

    public boolean logout() throws LoginException {
        return true;
    }

}
