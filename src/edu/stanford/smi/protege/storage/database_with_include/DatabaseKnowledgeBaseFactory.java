package edu.stanford.smi.protege.storage.database_with_include;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Description of the class
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DatabaseKnowledgeBaseFactory implements KnowledgeBaseFactory {
    final static String USERNAME = "username";
    final static String PASSWORD = "password";
    final static String URL = "url";
    final static String DRIVER = "driver";
    final static String TABLENAME = "table";

    public DatabaseKnowledgeBaseFactory() {
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
        KnowledgeBase inputKb,
        String driver,
        String url,
        String tablename,
        String username,
        String password,
        Collection errors) {
        try {
            DefaultKnowledgeBase outputKb = new DefaultKnowledgeBase();
            DatabaseFrameStore store =
                new DatabaseFrameStore(outputKb, getHelper(), driver, url, username, password, tablename, true);
            // store.reinitializeTable();
            outputKb.setTerminalFrameStore(store);
            store.saveKnowledgeBase(inputKb);
        } catch (Exception e) {
            errors.add(e);
        }
    }

    private FrameIDHelper getHelper() {
        return new FrameIDHelper();
    }

    public KnowledgeBase createKnowledgeBase(Collection errors) {
        DefaultKnowledgeBase kb = new DefaultKnowledgeBase(this);
        return kb;
    }

    public KnowledgeBaseSourcesEditor createKnowledgeBaseSourcesEditor(String projectName, PropertyList sources) {
        return new DatabaseKnowledgeBaseSourcesEditor(projectName, sources);
    }

    public String getDescription() {
        return "Database w/Include (alpha)";
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
        String driver = getDriver(sources);
        String url = getURL(sources);
        if (driver != null && url != null) {
            String username = getUsername(sources);
            String password = getPassword(sources);
            String tablename = getTableName(sources);
            includeKnowledgeBase(kb, driver, tablename, url, username, password, errors);
        }
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
    }

    public void includeKnowledgeBase(
        KnowledgeBase kb,
        String driver,
        String table,
        String url,
        String user,
        String password,
        Collection errors) {
        try {
            throw new UnsupportedOperationException();
        } catch (Exception e) {
            errors.add(e);
        }
    }

    public void loadKnowledgeBase(
        KnowledgeBase kb,
        String driver,
        String table,
        String url,
        String user,
        String password,
        Collection errors) {
        try {
            DefaultKnowledgeBase dkb = (DefaultKnowledgeBase) kb;
            DatabaseFrameStore store =
                new DatabaseFrameStore(dkb, getHelper(), driver, url, user, password, table, true);
            dkb.setTerminalFrameStore(store);
            boolean enabled = dkb.setCleanDispatchEnabled(false);
            KnowledgeBaseUtils.update(dkb);
            dkb.setCleanDispatchEnabled(enabled);
            dkb.setTerminalFrameStore(store);
        } catch (Exception e) {
            errors.add(e);
        }
    }

    public void saveKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        if (kb instanceof DefaultKnowledgeBase) {
            FrameStore store = ((DefaultKnowledgeBase) kb).getTerminalFrameStore();
            if (store instanceof DatabaseFrameStore) {
                DatabaseFrameStore databaseStore = (DatabaseFrameStore) store;
                if (!databaseStore.getTableName().equals(getTableName(sources))) {
                    copyKnowledgeBase(kb, sources, errors);
                }
            } else {
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
}
