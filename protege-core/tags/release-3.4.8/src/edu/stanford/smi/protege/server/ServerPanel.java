package edu.stanford.smi.protege.server;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.UnmarshalException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.server.socket.SSLFactory;
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

    private static final long serialVersionUID = 4729412021203886589L;

    private static final transient Logger log = Log.getLogger(ServerPanel.class);

    private static String _lastPassword = null;

	private JTextField _usernameField;
	private JTextField _passwordField;
	private JTextField _hostNameField;
	private JComponent _registerUserPanel;
	private JCheckBox _administerServerChekBox;

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
		JPanel panel = new JPanel(new GridLayout(5, 0));
		panel.add(new LabeledComponent("Host Machine Name", _hostNameField));
		panel.add(new LabeledComponent("User Name", _usernameField));
		panel.add(new LabeledComponent("Password", _passwordField));

		 _registerUserPanel = getRegisterUserPanel();
		panel.add(_registerUserPanel);

		_administerServerChekBox = ComponentFactory.createCheckBox("Administer server (requires privileges)");
		_administerServerChekBox.setSelected(false);
		panel.add(_administerServerChekBox);

		add(panel, BorderLayout.NORTH);
	}

	public boolean validateContents() {
		boolean isValid = false;
		try {
			isValid = isValidConfiguration();
		} catch (Exception e) {
		    log.log(Level.WARNING, "Could not log in", e);
		}
		return isValid;
	}

	private boolean isValidConfiguration() {
		boolean isValid = false;
		String serverName = _hostNameField.getText();

		_server = connectToHost(serverName);

		if (_server != null) {
		    String username = _usernameField.getText();
			String password = _passwordField.getText();

			_session = createSession(username, password);
			 isValid = (_session != null);
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

	public boolean isAdminsterServerActivated() {
		return _administerServerChekBox.isSelected();
	}

	private RemoteServer connectToHost(String serverName) {
		RemoteServer server = null;
		try {
		    if (SSLFactory.useSSL(SSLFactory.Context.LOGIN)) {
		        SSLFactory.resetAuth();
		    }
		    server = (RemoteServer) Naming.lookup("//" + serverName + "/" + Server.getBoundName());
		    if (SSLFactory.useSSL(SSLFactory.Context.LOGIN) && !SSLFactory.checkAuth()) {
		        log.severe("Requested ssl security but server is not secured.");
		        return null;
		    }
		} catch (UnknownHostException e) {
		    log("Trying to connect to an unknown host: " + serverName, e);
		    ModalDialog.showMessageDialog(this, "You are trying to connect to an unknown host: " + serverName + "\n\n" +
		    		"A possible cause of this error is that the specified hostname and port are not correct.",
		            "Unkown host", ModalDialog.MODE_CLOSE);
		} catch (ConnectException e) {
            log("Cannot connect to: " + serverName + ". " + e.getMessage(), e);
            ModalDialog.showMessageDialog(this, "Cannot connect to: " + serverName + "\n\n" +
                    "Possible causes of this error are firewall or network configuration problems.\n" +
                    "Please check also that the hostname and port are correct.",
                    "Unable to connect", ModalDialog.MODE_CLOSE);
        } catch (ConnectIOException e) {
            log("Cannot connect to: " + serverName + ". " + e.getMessage(), e);
            ModalDialog.showMessageDialog(this, "Cannot connect to: " + serverName + "\n\n" +
                    "Possible causes of this error are firewall or network configuration problems.\n" +
                    "Please check also that the hostname and port are correct.",
                    "IO connection error", ModalDialog.MODE_CLOSE);
        } catch (UnmarshalException e) {
            log("Unmarshalling exception. Possibly, the server and client use different versions of Protege " + e.getMessage(), e);
            ModalDialog.showMessageDialog(this, "There was a connection problem to: " + serverName + "\n\n" +
                    "Most likely cause is that the server and client are running different versions of Protege.",
                    "Unmarshalling exception", ModalDialog.MODE_CLOSE);
        } catch (NotBoundException e) {
            log("Not bound exception: " + serverName + ". " + e.getMessage() + " Is the Protege server started?", e);
            ModalDialog.showMessageDialog(this, "Cannot connect to: " + serverName + "\n\n" +
                    "Most likely cause of this problem is that the Protege server is not running.\n" +
                    "Please check also that the hostname and port are correct.\n" +
                    "This error can also be caused by firewall or network problems.",
                    "Not bound exception", ModalDialog.MODE_CLOSE);
        } catch (Exception e) {
            log("Error at connecting to: " + serverName + ". " + e.getMessage(), e);
            ModalDialog.showMessageDialog(this, "An error occured at connecting to: " + serverName + "\n\n" +
                    "See console for more details.",
                    "Error at connect", ModalDialog.MODE_CLOSE);
        }
		return server;
	}

	private RemoteSession createSession(String username, String password) {
	    //check the real connection by pinging the server
        try {
            getAllowsUserCreate(_server);
        } catch (Exception e) {
            log("Firewall or network problems when connecting to the server. " + e.getMessage() + " Server ref: " + _server, e);
            ModalDialog.showMessageDialog(this, "Cannot connect to server.\n" +
                    "Possible causes of this error are firewall or network configuration problems.\n",
                    "Unable to connect", ModalDialog.MODE_CLOSE);
            return null;
        }

	    RemoteSession session = null;
		try {
			session = _server.openSession(username, SystemUtilities.getMachineIpAddress(), password);
			if (session == null) {
			    log("Invalid login for " + username, null);
			    ModalDialog.showMessageDialog(this, "Invalid username and password. Please try again.", "Login failed");
			}
		} catch (UnknownHostException e) {
            log("Connection to RMI registry successful, but retrieved an unknown hostname from the RMI registry.\n" +
            		"Most probable cause is a misconfiguration of the java.rmi.server.hostname Java argument on the server.\n" +
            		"Server reference: " + _server, e);
            ModalDialog.showMessageDialog(this, "Connection to RMI registry successful, but retrieved an unknown hostname from the RMI registry.\n" +
                    "Most probable cause is a misconfiguration of the java.rmi.server.hostname Java argument on the server.\n" +
                    "Server reference: " + _server,
                    "Unkown host", ModalDialog.MODE_CLOSE);
        } catch (ConnectException e) {
            log("Connection to RMI registry successful, but cannot connect to the server.\n" +
                    "Possible cause is a misconfiguration of the java.rmi.server.hostname Java argument on the server, or\n" +
                    "a network or firwall problem" +
                    "Server reference: " + _server, e);
            ModalDialog.showMessageDialog(this, "Connection to RMI registry successful, but retrieved an unknown hostname from the RMI registry.\n" +
                    "Possible cause is a misconfiguration of the java.rmi.server.hostname Java argument on the server, or\n" +
                    "a network or firwall problem" +
                    "Server reference: " + _server,
                    "Unkown host", ModalDialog.MODE_CLOSE);
        } catch (ConnectIOException e) {
            log("Connection to RMI registry successful, but cannot connect to the server.\n" +
                    "Possible cause is a misconfiguration of the java.rmi.server.hostname Java argument on the server, or\n" +
                    "a network or firwall problem" +
                    "Server reference: " + _server, e);
            ModalDialog.showMessageDialog(this, "Connection to RMI registry successful, but retrieved an unknown hostname from the RMI registry.\n" +
                    "Possible cause is a misconfiguration of the java.rmi.server.hostname Java argument on the server, or\n" +
                    "a network or firwall problem" +
                    "Server reference: " + _server,
                    "Unkown host", ModalDialog.MODE_CLOSE);
        } catch (UnmarshalException e) {
            log("Connection to RMI registry successful, but there was an unmarshalling exception connecting to the server.\n" +
            		"Possibly, the server and client use different versions of Protege." + e.getMessage(), e);
            ModalDialog.showMessageDialog(this, "Connection to RMI registry successful, but there was an unmarshalling exception connecting to the server.\n" +
                    "Possibly, the server and client use different versions of Protege.",
                    "Unmarshalling exception", ModalDialog.MODE_CLOSE);
        } catch (RemoteException e) {
			log.log(Level.WARNING, "Connection to RMI registry successful, but at error at creating session for " + username + " Server ref: " + _server, e);
			 ModalDialog.showMessageDialog(this, "There was an error at checking the login credentails.", "Error at login");
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

			private static final long serialVersionUID = -5124239014343816470L;

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
					log.log(Level.WARNING, "Error at connecting to host " + hostName, ex);
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

		RemoteServer server = connectToHost(serverName);
		if (server == null) {
		    return false;
		}

		try {
			boolean allowsUserCreate = getAllowsUserCreate(server);
			if (!allowsUserCreate) {
				setCursor(Cursor.getDefaultCursor());
				ModalDialog.showMessageDialog(this, "Server "+ serverName + " does not allow the creation of new users.\n", "Warning");
			}
			return allowsUserCreate;
		} catch (Exception e) {
		    //This case should really not happen; just being overcautious
			log("There was an error at querying the server whether it allows the creation of new users", e);
			setCursor(Cursor.getDefaultCursor());
			ModalDialog.showMessageDialog(this, "There was an error at querying the server whether it allows the creation of users", "Warning");
			return false;
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}


	private boolean getAllowsUserCreate(RemoteServer server) throws RemoteException {
	    boolean allowsUserCreate = false;
	    try {
	        allowsUserCreate = server.allowsCreateUsers();
        } catch (RemoteException e) {
            log("Error at pinging server", e);
            throw e;
        }
        return allowsUserCreate;
	}


	private void log(String message, Exception e) {
	    if (log.isLoggable(Level.FINE)) {
	        log.log(Level.FINE, message, e);
	    }
	    log.warning(message);
	}

}