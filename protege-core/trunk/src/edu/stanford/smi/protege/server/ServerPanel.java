package edu.stanford.smi.protege.server;

import java.awt.*;
import java.rmi.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ServerPanel extends JPanel implements Validatable {
    private static String _lastUserName = "Guest";
    private static String _lastHostName = "localhost";
    private static String _lastPassword = "guest";
    private JTextField _usernameField;
    private JTextField _passwordField;
    private JTextField _hostNameField;
    private RemoteServer _server;
    private RemoteSession _session;

    public ServerPanel() {
        _usernameField = ComponentFactory.createTextField();
        _usernameField.setText(_lastUserName);
        _passwordField = ComponentFactory.createPasswordField();
        _passwordField.setText(_lastPassword);
        _hostNameField = ComponentFactory.createTextField();
        _hostNameField.setText(_lastHostName);

        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(3, 0));
        panel.add(new LabeledComponent("User Name", _usernameField));
        panel.add(new LabeledComponent("Password", _passwordField));
        panel.add(new LabeledComponent("Host Machine Name", _hostNameField));
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
            _session = createSession(username, password);
            if (_session == null) {
                String title = LocalizedText.getText(ResourceKey.REMOTE_SESSION_CREATE_FAILED_DIALOG_TITLE);
                String text = LocalizedText.getText(ResourceKey.REMOTE_SESSION_CREATE_FAILED_DIALOG_TEXT);
                ModalDialog.showMessageDialog(this, text, title);
            } else {
                isValid = true;
            }
        }
        return isValid;
    }

    public void saveContents() {
        _lastUserName = _usernameField.getText();
        _lastHostName = _hostNameField.getText();
        _lastPassword = _passwordField.getText();
    }

    public RemoteServer getServer() {
        return _server;
    }

    public RemoteSession getSession() {
        return _session;
    }

    private static RemoteServer connectToHost(String serverName) {
        RemoteServer server = null;
        try {
            server = (RemoteServer) Naming.lookup("//" + serverName + "/" + Server.getBoundName());
        } catch (Exception e) {
            e.printStackTrace();
            // do nothing
        }
        return server;
    }

    private RemoteSession createSession(String username, String password) {
        RemoteSession session = null;
        try {
            session = _server.openSession(username, SystemUtilities.getMachineIpAddress(), password);
        } catch (RemoteException e) {
            e.printStackTrace();
            // do nothing
        }
        return session;
    }
}