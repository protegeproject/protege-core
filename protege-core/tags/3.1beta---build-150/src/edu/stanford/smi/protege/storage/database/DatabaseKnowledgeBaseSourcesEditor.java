package edu.stanford.smi.protege.storage.database;

import java.awt.*;
import java.net.*;
import java.sql.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Description of the class
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DatabaseKnowledgeBaseSourcesEditor extends KnowledgeBaseSourcesEditor {
    private final static String DEFAULT_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";
    private final static String DEFAULT_URL = "jdbc:odbc:ProtegeDB";
    private final static String DEFAULT_PASSWORD = "";
    private final static String DEFAULT_TABLE_NAME = "ProtegeTable";

    private JTextField _driverComponent;
    private JTextField _urlComponent;
    private JTextField _tableNameComponent;
    private JTextField _usernameComponent;
    private JTextField _passwordComponent;

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
            text = getProperty(DatabaseKnowledgeBaseFactory.DRIVER, DEFAULT_DRIVER);
        }
        _driverComponent.setText(text);
        return new LabeledComponent("JDBC Driver Class Name", _driverComponent);
    }

    private Component createPasswordComponent() {
        _passwordComponent = ComponentFactory.createPasswordField();
        String text = DatabaseKnowledgeBaseFactory.getPassword(getSources());
        _passwordComponent.setText(text);
        if (text == null) {
            text = DEFAULT_PASSWORD;
        }
        return new LabeledComponent("Password", _passwordComponent);
    }

    private Component createTableNameComponent() {
        _tableNameComponent = ComponentFactory.createTextField();
        String text = DatabaseKnowledgeBaseFactory.getTableName(getSources());
        if (text == null) {
            text = getDefaultTableName();
        }
        _tableNameComponent.setText(text);
        return new LabeledComponent("Table", _tableNameComponent);
    }

    private Component createURLComponent() {
        _urlComponent = ComponentFactory.createTextField();
        String text = DatabaseKnowledgeBaseFactory.getURL(getSources());
        if (text == null) {
            text = getProperty(DatabaseKnowledgeBaseFactory.URL, DEFAULT_URL);
        }
        _urlComponent.setText(text);
        return new LabeledComponent("JDBC URL", _urlComponent);
    }

    private Component createUsernameComponent() {
        _usernameComponent = ComponentFactory.createTextField();
        String text = DatabaseKnowledgeBaseFactory.getUsername(getSources());
        if (text == null) {
            text = getProperty(DatabaseKnowledgeBaseFactory.USERNAME, null);
        }
        if (text == null) {
            text = SystemUtilities.getUserName();
        }
        _usernameComponent.setText(text);
        return new LabeledComponent("Username", _usernameComponent);
    }

    private String getDefaultTableName() {
        String tableName = getProperty(DatabaseKnowledgeBaseFactory.TABLENAME, null);
        if (tableName == null) {
            String path = getProjectPath();
            if (path != null) {
                tableName = FileUtilities.getBaseName(path);
            }
        }
        if (tableName == null) {
            tableName = DEFAULT_TABLE_NAME;
        }
        return tableName;
    }

    private String getProperty(String name, String defaultValue) {
        return ApplicationProperties.getString(getPropertyName(name), defaultValue);
    }

    private String getPropertyName(String baseName) {
        return getClass().getName() + "." + baseName;
    }

    protected void onProjectURIChange(URI oldURI, URI newURI) {
        // do nothing
    }

    public void saveContents() {
        String driver = _driverComponent.getText();
        DatabaseKnowledgeBaseFactory.setDriver(getSources(), driver);
        setProperty(DatabaseKnowledgeBaseFactory.DRIVER, driver);

        String url = _urlComponent.getText();
        DatabaseKnowledgeBaseFactory.setURL(getSources(), url);
        setProperty(DatabaseKnowledgeBaseFactory.URL, url);

        String tableName = _tableNameComponent.getText();
        DatabaseKnowledgeBaseFactory.setTablename(getSources(), tableName);
        setProperty(DatabaseKnowledgeBaseFactory.TABLENAME, tableName);

        String userName = _usernameComponent.getText();
        DatabaseKnowledgeBaseFactory.setUsername(getSources(), userName);
        setProperty(DatabaseKnowledgeBaseFactory.USERNAME, userName);

        DatabaseKnowledgeBaseFactory.setPassword(getSources(), _passwordComponent.getText());
    }

    private void setProperty(String name, String value) {
        ApplicationProperties.setString(getPropertyName(name), value);
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
                String username = _usernameComponent.getText();
                String password = _passwordComponent.getText();
                Connection c = DriverManager.getConnection(url, username, password);
                c.close();
            } catch (SQLException e) {
                isValid = false;
                String text = LocalizedText.getText(ResourceKey.DATABASE_CONFIGURE_FAILED_DIALOG_CANNOT_CREATE_CONNECTION_TEXT, e.getMessage());
                complain(text);
            }
        }
        return isValid;
    }
    
    private void complain(String text) {
        String title = LocalizedText.getText(ResourceKey.ERROR_DIALOG_TITLE);
        ModalDialog.showMessageDialog(this, text, title);
    }
}
