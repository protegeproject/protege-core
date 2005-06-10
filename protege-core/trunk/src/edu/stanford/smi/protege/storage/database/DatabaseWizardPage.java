package edu.stanford.smi.protege.storage.database;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import javax.swing.*;

import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DatabaseWizardPage extends WizardPage {
    private JTextField driverField;
    private JTextField urlField;
    private JTextField tableField;
    private JTextField usernameField;
    private JTextField passwordField;
    private JTextArea errorArea;
    private DatabasePlugin plugin;

    public DatabaseWizardPage(Wizard wizard, DatabasePlugin plugin) {
        super("datbase", wizard);
        this.plugin = plugin;
        createComponents();
        layoutComponents();
        updateSetPageComplete();
    }

    private void createComponents() {
        driverField = createTextField("database_wizard.driver");
        urlField = createTextField("database_wizard.url");
        tableField = createTextField("database_wizard.table");
        usernameField = createTextField("database_wizard.username");
        passwordField = createTextField("database_wizard.password");

        errorArea = ComponentFactory.createTextArea();
        errorArea.setEditable(false);
    }

    private JTextField createTextField(final String name) {
        String value = ApplicationProperties.getString(name);
        final JTextField field = ComponentFactory.createTextField(value);
        field.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent event) {
                updateSetPageComplete();
                ApplicationProperties.setString(name, field.getText());

            }
        });
        return field;
    }

    private void updateSetPageComplete() {
        setErrorText(null);
        boolean isComplete = hasValidDriver() && hasValidUrl() && hasValidTable();
        setPageComplete(isComplete);
    }

    private boolean hasValidDriver() {
        boolean isValid = false;
        String text = driverField.getText();
        if (text.length() == 0) {
            setErrorText("Driver class required");
        } else {
            Class clas = SystemUtilities.forName(text);
            isValid = clas != null;
            if (clas == null) {
                setErrorText("Driver class not found");
            }
        }
        return isValid;
    }

    private boolean hasValidUrl() {
        boolean isValid = false;
        String url = urlField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (url.length() == 0) {
            setErrorText("URL is required");
        } else if (username.length() == 0) {
            setErrorText("Username is required");
        } else {
            try {
                Connection c = DriverManager.getConnection(url, username, password);
                c.close();
                isValid = true;
            } catch (SQLException e) {
                setErrorText("Invalid URL, username, or password:\n" + e.getMessage());
            }
        }
        return isValid;
    }

    private static int MAX_TABLE_NAME_LENGTH = 20;

    private boolean hasValidTable() {
        boolean isValid = false;
        String text = tableField.getText();
        if (text.length() == 0) {
            setErrorText("Table name is required.");
        } else if (text.length() > MAX_TABLE_NAME_LENGTH) {
            setErrorText("Table name is too long.");
        } else {
            isValid = true;
            for (int i = 0; i < text.length(); ++i) {
                char c = text.charAt(i);
                if (!Character.isJavaIdentifierPart(c)) {
                    isValid = false;
                    break;
                }
            }
            if (!isValid) {
                setErrorText("Invalid table name");
            }
        }
        return isValid;
    }

    private void setErrorText(String text) {
        errorArea.setText(text);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        Box panel = Box.createVerticalBox();
        addField(panel, driverField, "JDBC Driver Class Name");
        addField(panel, urlField, "JDBC Driver URL");
        addField(panel, tableField, "Table");
        addField(panel, usernameField, "Username");
        addField(panel, passwordField, "Password");

        errorArea.setPreferredSize(new Dimension(10, 50));
        errorArea.setBackground(getBackground());
        panel.add(Box.createVerticalStrut(20));
        panel.add(ComponentFactory.createScrollPane(errorArea));

        add(panel, BorderLayout.NORTH);
    }

    private static void addField(Box panel, JComponent component, String text) {
        panel.add(new LabeledComponent(text, component));
    }

    public void onFinish() {
        plugin.setDriver(driverField.getText());
        plugin.setTable(tableField.getText());
        plugin.setUsername(usernameField.getText());
        plugin.setPassword(passwordField.getText());
        plugin.setURL(urlField.getText());
    }

}