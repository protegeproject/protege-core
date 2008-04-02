package edu.stanford.smi.protege.server;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.rmi.Naming;
import java.rmi.RemoteException;

import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.server.auth.LoginToken;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.Validatable;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ServerPanel extends JPanel implements Validatable {
    private static String _lastPassword = null;
    private JTextField _usernameField;
    private JTextField _passwordField;
    private JTextField _hostNameField;
    private RemoteServer _server;
    private LoginToken loginToken;
    private static final String USER_NAME = ServerPanel.class.getName() + ".user_name";
    private static final String HOST_NAME = ServerPanel.class.getName() + ".host_name";

    public ServerPanel() {
        _usernameField = ComponentFactory.createTextField();
        _usernameField.setText(ApplicationProperties.getString(USER_NAME, "Guest"));
        _passwordField = ComponentFactory.createPasswordField();
        if (_lastPassword != null) {
            _passwordField.setText(_lastPassword);
        } else if (_usernameField.getText().equals("Guest")) {
            _passwordField.setText("guest");
        }
        _hostNameField = ComponentFactory.createTextField();
        _hostNameField.setText(ApplicationProperties.getString(HOST_NAME, "localhost"));

        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(3, 0));
        panel.add(new LabeledComponent("Host Machine Name", _hostNameField));
        panel.add(new LabeledComponent("User Name", _usernameField));
        panel.add(new LabeledComponent("Password", _passwordField));
        add(panel, BorderLayout.NORTH);
    }

    public boolean validateContents() {
        boolean isValid = false;
        try {
            isValid = isValidConfiguration();
        } catch (Exception e) {
            // do nothing
        }
        return isValid;
    }

    private boolean isValidConfiguration() {
        boolean isValid = false;
        String serverName = _hostNameField.getText();
        _server = connectToHost(serverName);
        if (_server == null) {
            String text = LocalizedText.getText(ResourceKey.REMOTE_CONNECT_FAILED_DIALOG_TEXT);
            ModalDialog.showMessageDialog(this, text);
        } else {
            String username = _usernameField.getText();
            String password = _passwordField.getText();
            loginToken = createSession(username, password);
            if (loginToken == null) {
                String title = LocalizedText.getText(ResourceKey.REMOTE_SESSION_CREATE_FAILED_DIALOG_TITLE);
                String text = LocalizedText.getText(ResourceKey.REMOTE_SESSION_CREATE_FAILED_DIALOG_TEXT);
                ModalDialog.showMessageDialog(this, text, title);
            } else {
                isValid = true;
            }
        }
        saveFields();
        return isValid;
    }

    public void saveContents() {
        // do nothing
    }

    private void saveFields() {
        ApplicationProperties.setString(USER_NAME, _usernameField.getText());
        ApplicationProperties.setString(HOST_NAME, _hostNameField.getText());
        _lastPassword = _passwordField.getText();
    }

    public RemoteServer getServer() {
        return _server;
    }

    public LoginToken getSession() {
        return loginToken;
    }

    private static RemoteServer connectToHost(String serverName) {
        RemoteServer server = null;
        try {
            server = (RemoteServer) Naming.lookup("//" + serverName + "/" + Server.getBoundName());
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return server;
    }

    private LoginToken createSession(String username, String password) {
        LoginToken token = null;
        try {
            token = _server.openSession(username, SystemUtilities.getMachineIpAddress(), password);
        } catch (RemoteException e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return token;
    }
}