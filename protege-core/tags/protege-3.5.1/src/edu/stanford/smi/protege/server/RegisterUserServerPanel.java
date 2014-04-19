package edu.stanford.smi.protege.server;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.rmi.Naming;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.Validatable;

public class RegisterUserServerPanel extends JPanel implements Validatable {
 
    private static final long serialVersionUID = -325336435948601667L;
    private JTextField _usernameField;
    private JTextField _password1Field;
    private JTextField _password2Field; 
    
    private String _serverName;
    
    
    public RegisterUserServerPanel(String serverName) {
    	this._serverName = serverName;
    	
        _usernameField = ComponentFactory.createTextField();            
        _password1Field = ComponentFactory.createPasswordField();
        _password2Field = ComponentFactory.createPasswordField();
        
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(4, 0));        
        panel.add(new LabeledComponent("User Name", _usernameField));
        panel.add(new LabeledComponent("Password", _password1Field));
        panel.add(new LabeledComponent("Retype password", _password2Field));
        
        add(panel, BorderLayout.NORTH);
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

	public boolean validateContents() {
		String username = _usernameField.getText();
	    String password1 = _password1Field.getText();
	    String password2 = _password2Field.getText();
	    
	    if (username == null || username.length() == 0) {
	    	ModalDialog.showMessageDialog(this, "Username field cannot be empty.\nPlease provide a user name.", "Error: Empty user name");
	    	
	    	return false;
	    }
	    
	    if (!password1.equals(password2)) {
	    	ModalDialog.showMessageDialog(this, "Passwords in the two password fields do not match. Please retype passwords.", "Error: Passwords do not match");
	    	_password1Field.setText("");
	    	_password2Field.setText("");
	  
	    	return false;
	    }
	    
		boolean success = false;
		RemoteServer _server = null;
		
	    try {
	    	_server = connectToHost(_serverName);					
		} catch (Exception ex) {
			Log.getLogger().log(Level.WARNING, "Error at connecting to host " + _serverName, ex);
			ModalDialog.showMessageDialog(this, "Cannot connect to server: " + _serverName + "\nPlease check that the server name is correct." +
					"\nThis error may also indicate firewall problems.", "Error: Connect to server");
			return false;
		}

		try {
			success = _server.createUser(username, password1);
		} catch (Exception e) {			
			success = false;
			Log.getLogger().warning("Error at creating user with username: " + username);
		} finally {
			if (success) {
				ModalDialog.showMessageDialog(this, "New user " + username + " created successfully.\n You can now login to the server " + _serverName + " using the created account.", "Created new user");
			} else {
				ModalDialog.showMessageDialog(this, "Username already exist. Please choose another user name and try again.", "Error: User already exists");
				_usernameField.setText("");				
				_password1Field.setText("");
				_password2Field.setText("");
			}
		}		
		
		return success;
	}
	
	public void saveContents() {
		//do nothing
	}

	public String getUsername() {
		return _usernameField.getText();
	}

	public String getPassword() {
		return _password1Field.getText();
	}

	
}
