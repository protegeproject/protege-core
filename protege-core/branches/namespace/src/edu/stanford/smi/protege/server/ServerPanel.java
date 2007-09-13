package edu.stanford.smi.protege.server;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.AllowableAction;
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
	private JComponent _registerUserPanel;

	private RemoteServer _server;
	private RemoteSession _session;

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
		JPanel panel = new JPanel(new GridLayout(4, 0));
		panel.add(new LabeledComponent("Host Machine Name", _hostNameField));
		panel.add(new LabeledComponent("User Name", _usernameField));
		panel.add(new LabeledComponent("Password", _passwordField));

		 _registerUserPanel = getRegisterUserPanel();		 
		panel.add(_registerUserPanel);

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
		}
		return session;
	}


	private JComponent getRegisterUserPanel() {			
		AllowableAction registerUserAction = createRegisterUserAction();
		JButton registerUserButton = ComponentFactory.createButton(registerUserAction);
		registerUserButton.setText("New user");
		registerUserButton.setToolTipText("Register new user (Checks first if server allows the creation of new users)");
	
		LabeledComponent registerUserPanel = new LabeledComponent("", null);
		registerUserPanel.add(registerUserButton, BorderLayout.EAST);
		
		return registerUserPanel;
	}

	private AllowableAction createRegisterUserAction() {
		AllowableAction action = new AllowableAction(new ResourceKey("Create new user")) {

			public void actionPerformed(ActionEvent e) {
				final String hostName = _hostNameField.getText();

				if (_hostNameField == null || hostName.length() == 0) {
					ModalDialog.showMessageDialog(ServerPanel.this,
							"Server host name is empty. Please set first the server host name before registering.",
							"Error: Empty Server Name");
					return;
				}

				if (!serverAllowsUserCreate(hostName)) {					
					return;
				}
				
				try {
					_server = connectToHost(hostName);
				} catch (Exception ex) {
					Log.getLogger().log(Level.WARNING, "Error at connecting to host " + hostName, ex);
					ModalDialog.showMessageDialog(ServerPanel.this, "Cannot connect to server: " + hostName
							+ "\nPlease check that the server name is correct." + "\nThis error may also indicate firewall problems.",
							"Error: Connect to server");
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

	private boolean serverAllowsUserCreate(String serverName) {
		
		if (serverName == null) {
			return false;
		}
		
		serverName = serverName.trim();
		
		if (serverName.length() == 0) {
			return false;
		}
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		try {			
			RemoteServer server = (RemoteServer) Naming.lookup("//" + serverName + "/" + Server.getBoundName());
			boolean allowsUserCreate = server.allowsCreateUsers();
			server = null;
			if (!allowsUserCreate) {
				setCursor(Cursor.getDefaultCursor());
				ModalDialog.showMessageDialog(this, "Server "+ serverName + " does not allow the creation of new users.\n", "Warning");
			}
			return allowsUserCreate;			
		} catch (Exception e) {
			Log.getLogger().warning("Server not found or does not allow creation of new users");
			setCursor(Cursor.getDefaultCursor());
			ModalDialog.showMessageDialog(this, "Cannot connect to server "+ serverName, "Error: Connect to server");
			return false;
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

}