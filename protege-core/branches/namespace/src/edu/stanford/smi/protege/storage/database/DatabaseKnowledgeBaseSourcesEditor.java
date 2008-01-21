package edu.stanford.smi.protege.storage.database;

import java.awt.Component;
import java.awt.GridLayout;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.model.KnowledgeBaseSourcesEditor;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseFactory.DatabaseProperty;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.PropertyList;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 *  Description of the class
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DatabaseKnowledgeBaseSourcesEditor extends KnowledgeBaseSourcesEditor {
    private static final long serialVersionUID = -1850928285684275958L;
    private JTextField _driverComponent;
    private JTextField _urlComponent;
    private JTextField _tableNameComponent;
    private JTextField _usernameComponent;
    private JTextField _passwordComponent;
    
    public static String getProperty(DatabaseProperty property) {
        return ApplicationProperties.getString(getPropertyName(property.getName()), property.getDefaultValue());
    }
    
    public static void setProperty(DatabaseProperty property, String value) {
        ApplicationProperties.setString(getPropertyName(property.getName()), value);
    }

    private static String getPropertyName(String baseName) {
        return DatabaseKnowledgeBaseSourcesEditor.class.getName() + "." + baseName;
    }

    public DatabaseKnowledgeBaseSourcesEditor(String projectURIString, PropertyList sources) {
        super(projectURIString, sources);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));
        panel.add(createDriverComponent());
        panel.add(createURLComponent());
        panel.add(createTableNameComponent());
        panel.add(createUsernameComponent());
        panel.add(createPasswordComponent());
        add(panel);
    }

    public Component createDriverComponent() {
        _driverComponent = ComponentFactory.createTextField();
        String text = DatabaseKnowledgeBaseFactory.getDriver(getSources());
        if (text == null) {
            text = getProperty(DatabaseProperty.DRIVER_PROPERTY);
        }
        _driverComponent.setText(text);
        return new LabeledComponent(DatabaseProperty.DRIVER_PROPERTY.getTitle(), _driverComponent);
    }

    private Component createPasswordComponent() {
        _passwordComponent = ComponentFactory.createPasswordField();
        String text = DatabaseKnowledgeBaseFactory.getPassword(getSources());
        _passwordComponent.setText(text);
        return new LabeledComponent(DatabaseProperty.PASSWORD_PROPERTY.getTitle(), _passwordComponent);
    }

    private Component createTableNameComponent() {
        _tableNameComponent = ComponentFactory.createTextField();
        String text = DatabaseKnowledgeBaseFactory.getTableName(getSources());
        if (text == null) {
            text = getDefaultTableName();
        }
        _tableNameComponent.setText(text);
        return new LabeledComponent(DatabaseProperty.TABLENAME_PROPERTY.getTitle(), _tableNameComponent);
    }

    private Component createURLComponent() {
        _urlComponent = ComponentFactory.createTextField();
        String text = DatabaseKnowledgeBaseFactory.getURL(getSources());
        if (text == null) {
            text = getProperty(DatabaseProperty.URL_PROPERTY);
        }
        _urlComponent.setText(text);
        return new LabeledComponent(DatabaseProperty.URL_PROPERTY.getTitle(), _urlComponent);
    }

    private Component createUsernameComponent() {
        _usernameComponent = ComponentFactory.createTextField();
        String text = DatabaseKnowledgeBaseFactory.getUsername(getSources());
        if (text == null) {
            text = getProperty(DatabaseProperty.USERNAME_PROPERTY);
        }
        if (text == null) {
            text = SystemUtilities.getUserName();
        }
        _usernameComponent.setText(text);
        return new LabeledComponent(DatabaseProperty.USERNAME_PROPERTY.getTitle(), _usernameComponent);
    }

    private String getDefaultTableName() {
        String tableName = getProperty(DatabaseProperty.TABLENAME_PROPERTY);
        if (tableName == null) {
            String path = getProjectPath();
            if (path != null) {
                tableName = FileUtilities.getBaseName(path);
            }
        }
        if (tableName == null) {
            tableName = DatabaseProperty.TABLENAME_PROPERTY.getDefaultValue();
        }
        return tableName;
    }

    protected void onProjectURIChange(URI oldURI, URI newURI) {
        // do nothing
    }

    public void saveContents() {
        String driver = _driverComponent.getText();
        DatabaseKnowledgeBaseFactory.setDriver(getSources(), driver);
        setProperty(DatabaseProperty.DRIVER_PROPERTY, driver);

        String url = _urlComponent.getText();
        DatabaseKnowledgeBaseFactory.setURL(getSources(), url);
        setProperty(DatabaseProperty.URL_PROPERTY, url);

        String tableName = _tableNameComponent.getText();
        DatabaseKnowledgeBaseFactory.setTablename(getSources(), tableName);
        setProperty(DatabaseProperty.TABLENAME_PROPERTY, tableName);

        String userName = _usernameComponent.getText();
        DatabaseKnowledgeBaseFactory.setUsername(getSources(), userName);
        setProperty(DatabaseProperty.USERNAME_PROPERTY, userName);

        DatabaseKnowledgeBaseFactory.setPassword(getSources(), _passwordComponent.getText());
    }
    


    public boolean validateContents() {
        boolean isValid = true;
        String driver = _driverComponent.getText();
        Class clas = SystemUtilities.forName(driver);
        if (clas == null) {
            isValid = false;
            String text = LocalizedText.getText(ResourceKey.DATABASE_CONFIGURE_FAILED_DIALOG_DRIVER_NOT_FOUND_TEXT);
            complain(text);
        } else {
            try {
                String url = _urlComponent.getText();
                String userName = _usernameComponent.getText();
                String password = _passwordComponent.getText();
                Connection c = DriverManager.getConnection(url, userName, password);
                isValid = confirmOverwriteIfNecessary(c);
                c.close();
            } catch (SQLException e) {
                isValid = false;
                String text = LocalizedText.getText(
                        ResourceKey.DATABASE_CONFIGURE_FAILED_DIALOG_CANNOT_CREATE_CONNECTION_TEXT, e.getMessage());
                complain(text);
            }
        }
        return isValid;
    }

    private boolean confirmOverwriteIfNecessary(Connection c) {
        boolean canCreate = true;
        String tableName = _tableNameComponent.getText();
        if (newTable(tableName) && tableExists(c, tableName)) {
            String text = "Table already exists.  Overwrite it?";
            int rval = ModalDialog.showMessageDialog(this, text, ModalDialog.MODE_YES_NO);
            canCreate = rval == ModalDialog.OPTION_YES;
        }
        return canCreate;
    }

    private boolean newTable(String tableName) {
        String existingTableName = DatabaseKnowledgeBaseFactory.getTableName(getSources());
        return !tableName.equals(existingTableName);
    }

    private static boolean tableExists(Connection connection, String tableName) {
        boolean exists = false;
        try {
            Statement statement = connection.createStatement();
            String query = "SELECT COUNT(*) FROM " + tableName;
            ResultSet rs = statement.executeQuery(query);
            rs.close();
            statement.close();
            exists = true;
        } catch (SQLException e) {
            // do nothing
        }
        return exists;
    }

    private void complain(String text) {
        String title = LocalizedText.getText(ResourceKey.ERROR_DIALOG_TITLE);
        ModalDialog.showMessageDialog(this, text, title);
    }
}
