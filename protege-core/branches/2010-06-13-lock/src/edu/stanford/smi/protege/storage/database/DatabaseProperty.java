/**
 * 
 */
package edu.stanford.smi.protege.storage.database;

import edu.stanford.smi.protege.util.ApplicationProperties;

public enum DatabaseProperty {
    DRIVER_PROPERTY("driver", "com.mysql.jdbc.Driver", "JDBC Driver Class Name"),
    TABLENAME_PROPERTY("table", "ProtegeTable", "Table"),
    URL_PROPERTY("url", "jdbc:mysql://localhost/protege", "JDBC URL"),
    USERNAME_PROPERTY("username", null, "Username"),
    PASSWORD_PROPERTY("password", null, "Password");
    
    private String name;
    private String defaultValue;
    private String title;
    
    private DatabaseProperty(String name, String defaultValue, String title) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.title = title;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDefaultValue() {
        return  defaultValue;
    }
    
    public String getTitle() {
        return title;
    }

    public static String getProperty(DatabaseProperty property) {
        return ApplicationProperties.getString(getPropertyName(property.getName()), property.getDefaultValue());
    }

    public static void setProperty(DatabaseProperty property, String value) {
        ApplicationProperties.setString(getPropertyName(property.getName()), value);
    }

    private static String getPropertyName(String baseName) {
        return DatabaseKnowledgeBaseSourcesEditor.class.getName() + "." + baseName;
    }
}