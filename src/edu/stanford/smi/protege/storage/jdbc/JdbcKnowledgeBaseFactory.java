package edu.stanford.smi.protege.storage.jdbc;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.storage.jdbc.framedb.*;
import edu.stanford.smi.protege.util.*;

/**
 * Storage backend for relational databases.  This class doesn't actually contain the SQL code but delegates to the 
 * class that does (an implementation of {@link DatabaseManager}).  It simply translates between the two interfaces.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class JdbcKnowledgeBaseFactory implements KnowledgeBaseFactory {
    final static String USERNAME = "username";
    final static String PASSWORD = "password";
    final static String URL = "url";
    final static String DRIVER = "driver";
    final static String TABLENAME = "table";

    public JdbcKnowledgeBaseFactory() {
        // Log.enter(this, "JdbcKnowledgeBaseFactory");
    }

    private void copyKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        String driver = getDriver(sources);
        String url = getURL(sources);
        String username = getUsername(sources);
        String password = getPassword(sources);
        String tablename = getTableName(sources);
        copyKnowledgeBase(kb, driver, url, tablename, username, password, errors);
    }

    private void copyKnowledgeBase(
        KnowledgeBase kb,
        String driver,
        String url,
        String tablename,
        String username,
        String password,
        Collection errors) {
        try {
            DatabaseManager manager = new SimpleJdbcDatabaseManager(driver, url, tablename, username, password, errors);
            manager.saveKnowledgeBase(kb);
            manager.dispose();
        } catch (Exception e) {
            errors.add(e);
        }
    }

    public KnowledgeBase createKnowledgeBase(Collection errors) {
        OldJdbcDefaultKnowledgeBase kb = new OldJdbcDefaultKnowledgeBase(this);
        return kb;
    }

    public KnowledgeBaseSourcesEditor createKnowledgeBaseSourcesEditor(String projectURIString, PropertyList sources) {
        return new JdbcKnowledgeBaseSourcesEditor(projectURIString, sources);
    }

    public String getDescription() {
        return "Legacy Database (Protege 1.X)";
    }

    public static String getDriver(PropertyList sources) {
        return sources.getString(DRIVER);
    }

    public static String getPassword(PropertyList sources) {
        return sources.getString(PASSWORD);
    }

    public String getProjectFilePath() {
        return null;
    }

    public static String getTableName(PropertyList sources) {
        return sources.getString(TABLENAME);
    }

    public static String getURL(PropertyList sources) {
        return sources.getString(URL);
    }

    public static String getUsername(PropertyList sources) {
        return sources.getString(USERNAME);
    }

    public void includeKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        // TODO
    }

    public boolean isComplete(PropertyList sources) {
        String tableName = getTableName(sources);
        String url = getURL(sources);
        String driver = getDriver(sources);
        return tableName != null && url != null && driver != null;
    }

    public void loadKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        String driver = getDriver(sources);
        String url = getURL(sources);
        if (driver != null && url != null) {
            String username = getUsername(sources);
            String password = getPassword(sources);
            String tablename = getTableName(sources);
            loadKnowledgeBase(kb, driver, tablename, url, username, password, errors);
        }
        // hack to help prevent user from attempting to modify the database
        kb.getProject().setIsReadonly(true);
    }

    public void loadKnowledgeBase(
        KnowledgeBase kb,
        String driver,
        String tableName,
        String url,
        String username,
        String password,
        Collection errors) {
        try {
            OldJdbcDefaultKnowledgeBase dkb = (OldJdbcDefaultKnowledgeBase) kb;
            SimpleJdbcDatabaseManager manager =
                new SimpleJdbcDatabaseManager(driver, url, tableName, username, password, errors);
            FrameDBStorage memoryStorage = (FrameDBStorage) dkb.getStorage();
            memoryStorage.setCaching(true);
            dkb.setStorage(new DatabaseStorage(dkb, manager, memoryStorage));
        } catch (Exception e) {
            errors.add(e);
        }
    }

    public void saveKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        Storage storage = ((OldJdbcDefaultKnowledgeBase) kb).getStorage();
        if (storage instanceof DatabaseStorage) {
            DatabaseManager databaseManager = ((DatabaseStorage) storage).getDatabaseManager();
            if (!databaseManager.getTableName().equals(getTableName(sources))) {
                copyKnowledgeBase(kb, sources, errors);
            }
        } else {
            copyKnowledgeBase(kb, sources, errors);
        }
    }

    public static void setDriver(PropertyList sources, String driver) {
        sources.setString(DRIVER, driver);
    }

    public static void setPassword(PropertyList sources, String password) {
        sources.setString(PASSWORD, password);
    }

    public static void setTablename(PropertyList sources, String tablename) {
        sources.setString(TABLENAME, tablename);
    }

    public static void setURL(PropertyList sources, String url) {
        sources.setString(URL, url);
    }

    public static void setUsername(PropertyList sources, String username) {
        sources.setString(USERNAME, username);
    }

    public static void setSources(
        PropertyList sources,
        String driver,
        String url,
        String table,
        String user,
        String password) {
        setDriver(sources, driver);
        setURL(sources, url);
        setTablename(sources, table);
        setUsername(sources, user);
        setPassword(sources, password);
    }

    public String toString() {
        return "JdbcKnowledgeBaseFactory";
    }
}
