package edu.stanford.smi.protege.storage.database;

import static edu.stanford.smi.protege.storage.database.DatabaseProperty.DRIVER_PROPERTY;
import static edu.stanford.smi.protege.storage.database.DatabaseProperty.PASSWORD_PROPERTY;
import static edu.stanford.smi.protege.storage.database.DatabaseProperty.TABLENAME_PROPERTY;
import static edu.stanford.smi.protege.storage.database.DatabaseProperty.URL_PROPERTY;
import static edu.stanford.smi.protege.storage.database.DatabaseProperty.USERNAME_PROPERTY;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.Wizard;
import edu.stanford.smi.protege.util.WizardPage;


/**
 * TODO Class Comment
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DatabaseWizardPage extends WizardPage {
    private static final long serialVersionUID = -6619215858750011057L;

    private Map<DatabaseProperty, JTextField> fields = new EnumMap<DatabaseProperty, JTextField>(DatabaseProperty.class);
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
        for (DatabaseProperty property : DatabaseProperty.values()) {
            if (property == PASSWORD_PROPERTY) {
                fields.put(property, createPasswordTextField());
            } else {
                fields.put(property, createTextField(property));
            }
        }

        errorArea = ComponentFactory.createTextArea();
        errorArea.setEditable(false);
    }

    private JTextField createTextField(final DatabaseProperty property) {
        String value = DatabaseProperty.getProperty(property);
        final JTextField field = ComponentFactory.createTextField(value);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
                updateSetPageComplete();
                DatabaseProperty.setProperty(property, field.getText());

            }
        });
        return field;
    }

    private JTextField createPasswordTextField() {
        final JTextField field = ComponentFactory.createPasswordField();
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
                updateSetPageComplete();
            }
        });
        return field;
    }

    protected void updateSetPageComplete() {
        setErrorText(null);
        setPageComplete(isComplete());
    }

    protected boolean isComplete() {
        return hasValidDriver() && hasValidUrl() && hasValidTable();
    }

    private boolean hasValidDriver() {
        boolean isValid = false;
        String text = getFieldText(DRIVER_PROPERTY);
        text = text.trim();
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
        String url = getFieldText(URL_PROPERTY);
        url = url.trim();
        String username = getFieldText(USERNAME_PROPERTY);
        String password = getFieldText(PASSWORD_PROPERTY);
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

    private boolean hasValidTable() {
        boolean isValid = false;
        String text = getFieldText(TABLENAME_PROPERTY);
        if (text.length() == 0) {
            setErrorText("Table name is required.");
        } else {
            isValid = true;
        }
        return isValid;
    }

    protected void setErrorText(String text) {
        errorArea.setText(text);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        Box panel = Box.createVerticalBox();
        layoutComponents(panel);
    }

    protected void layoutComponents(Box panel) {
        for (DatabaseProperty property : DatabaseProperty.values()) {
            addField(panel, property);
        }

        errorArea.setPreferredSize(new Dimension(10, 50));
        errorArea.setBackground(getBackground());
        panel.add(Box.createVerticalStrut(20));
        panel.add(ComponentFactory.createScrollPane(errorArea));

        add(panel, BorderLayout.NORTH);
    }

    private void addField(Box panel, DatabaseProperty property) {
        panel.add(new LabeledComponent(property.getTitle(), fields.get(property)));
    }

    @Override
    public void onFinish() {
        plugin.setDriver(getFieldText(DRIVER_PROPERTY));
        plugin.setTable(getFieldText(TABLENAME_PROPERTY));
        plugin.setUsername(getFieldText(USERNAME_PROPERTY));
        plugin.setPassword(getFieldText(PASSWORD_PROPERTY));
        plugin.setURL(getFieldText(URL_PROPERTY));
    }

    protected String getFieldText(DatabaseProperty property)  {
        return fields.get(property).getText();
    }

}