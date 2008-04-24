package edu.stanford.smi.protege.server;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ServerPanel extends JPanel implements Validatable {
    private static String _lastPassword = null;
    private JTextField _usernameField;
    private JTextField _passwordField;
    private JTextField _hostNameField;
    
    private JButton _registerUserButton;
    
    private RemoteServer _server;
    private RemoteSession _session;
    
    private AllowableAction _registerUserAction;
    
    private static final String USER_NAME = ServerPanel.class.getName() + ".user_name";
    private static final String HOST_NAME = ServerPanel.class.getName() + ".host_name";

    public ServerPanel() {
        _usernameField = ComponentFactory.createTextField();
        _usernameField.setText(ApplicationProperties.getString(USER_NAME, "Guest"));    
        _passwordField = ComponentFactory.createPasswordField();
        
        _registerUserAction = createRegisterUserAction();
        _registerUserButton = ComponentFactory.createButton(_registerUserAction);
        _registerUserButton.setText("Register new user");
        
        if (_lastPassword != null) {
            _passwordField.setText(_lastPassword);
        } else if (_usernameField.getText().equals("Guest")) {
        	_passwordField.setText("guest");
        }
        
        _hostNameField = ComponentFactory.createTextField();
        String defaultHostName = "localhost";
        _hostNameField.setText(ApplicationProperties.getString(HOST_NAME, defaultHostName));

        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(4, 0));
        panel.add(new LabeledComponent("Host Machine Name", _hostNameField));
        panel.add(new LabeledComponent("User Name", _usernameField));
        panel.add(new LabeledComponent("Password", _passwordField));
        
        /* Change this later. This property is right now read from the client
         * but it should be read from the server.         
         */
        boolean defaultAllowCreateUsers = (SystemUtilities.isApplet() ? true : false);
        boolean showNewUserPanel = ApplicationProperties.getBooleanProperty(Server.SERVER_ALLOW_CREATE_USERS, defaultAllowCreateUsers);
        
        if (showNewUserPanel) {
        	JComponent registerUserPanel = getRegisterUserPanel();                
        	panel.add(new LabeledComponent("Register new user", registerUserPanel));
        }
        
        add(panel, BorderLayout.NORTH);
    }

    private JComponent getRegisterUserPanel() {
        JPanel registerUserPanel = new JPanel(new BorderLayout());
        registerUserPanel.setBorder(ComponentFactory.createThinStandardBorder());
        
        JTextPane newUserTextArea = new JTextPane();
        newUserTextArea.setBackground(getBackground());
        newUserTextArea.setForeground(Color.DARK_GRAY);
        newUserTextArea.setText("If you are a new user and do not have an account on the server, please register.");
        newUserTextArea.setEditable(false);
       
        registerUserPanel.add(newUserTextArea, BorderLayout.CENTER);
        registerUserPanel.add(_registerUserButton, BorderLayout.EAST);
        
        return registerUserPanel;
    	
    }
     
    private AllowableAction createRegisterUserAction() {
    	AllowableAction action = new AllowableAction(new ResourceKey("Create new user")){

			public void actionPerformed(ActionEvent e) {
				final String hostName = _hostNameField.getText();
				
				if (_hostNameField == null || hostName.length() == 0) {
					ModalDialog.showMessageDialog(ServerPanel.this, "Server host name is empty. Please set first the server host name before registering.", "Error: Empty Server Name");
					return;
				}
				
			    try {
			    	_server = connectToHost(hostName);					
				} catch (Exception ex) {
					Log.getLogger().log(Level.WARNING, "Error at connecting to host " + hostName, ex);
					ModalDialog.showMessageDialog(ServerPanel.this, "Cannot connect to server: " + hostName + "\nPlease check that the server name is correct." +
							"\nThis error may also indicate firewall problems.", "Error: Connect to server");
					return;
				}
								
				RegisterUserServerPanel regUserPanel = new RegisterUserServerPanel(hostName);
				int ret = ModalDialog.showDialog(ServerPanel.this, regUserPanel, "Register new user", ModalDialog.MODE_OK_CANCEL);
				
				if (ret == ModalDialog.OPTION_OK) {
					_usernameField.setText(regUserPanel.getUsername());
					_passwordField.setText(regUserPanel.getPassword());
				}
			}
    	};
    	
    	return action;
    }


	public boolean validateContents() {
        boolean isValid = false;
        try {
            isValid = isValidConfiguration();
        } catch (Exception e) {
            // do nothing
        	Log.getLogger().warning("The configuration in the server panel is not valid. Error message: " + e.getMessage());
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

    public RemoteSession getSession() {
        return _session;
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

    private RemoteSession createSession(String username, String password) {
        RemoteSession session = null;
        try {
            session = _server.openSession(username, SystemUtilities.getMachineIpAddress(), password);
        } catch (RemoteException e) {
            Log.getLogger().severe(Log.toString(e));
        } catch (Exception e) {
        	Log.getLogger().log(Level.SEVERE, "Errors at opening session for user " + username, e);
        }
        return session;
    }
}