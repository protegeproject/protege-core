package edu.stanford.smi.protege.storage.database;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Description of the class
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DatabaseKnowledgeBaseFactory implements KnowledgeBaseFactory {
    public static final String DESCRIPTION = Text.getProgramName() + " Database";
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
                new DatabaseFrameStore(outputKb, driver, url, username, password, tablename, true);
            // store.reinitializeTable();
            outputKb.setTerminalFrameStore(store);
            store.saveKnowledgeBase(inputKb);
        } catch (Exception e) {
            errors.add(e);
        }
    }

    public KnowledgeBase createKnowledgeBase(Collection errors) {
        DefaultKnowledgeBase kb = new DefaultKnowledgeBase(this);
        return kb;
    }

    public KnowledgeBaseSourcesEditor createKnowledgeBaseSourcesEditor(String projectName, PropertyList sources) {
        return new DatabaseKnowledgeBaseSourcesEditor(projectName, sources);
    }

    public String getDescription() {
        return DESCRIPTION;
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
            DefaultKnowledgeBase dkb = (DefaultKnowledgeBase) kb;
            ensureInitialized(dkb);
            MergingFrameStoreHandler handler = getMergingFrameStoreHandler(dkb);
            DatabaseFrameStore store = new DatabaseFrameStore(dkb, driver, url, user, password, table, false);
            handler.addSecondaryFrameStore(store);
        } catch (Exception e) {
            errors.add(e);
        }
    }

    private void ensureInitialized(DefaultKnowledgeBase dkb) {
        FrameStore fs = dkb.getTerminalFrameStore();
        if (fs instanceof InMemoryFrameStore) {
            FrameStore mergingFrameStore = MergingFrameStoreHandler.newInstance();
            mergingFrameStore.setDelegate(fs);
            dkb.setTerminalFrameStore(mergingFrameStore);
        }
    }

    private MergingFrameStoreHandler getMergingFrameStoreHandler(DefaultKnowledgeBase kb) {
        FrameStore fs = kb.getTerminalFrameStore();
        return (MergingFrameStoreHandler) Proxy.getInvocationHandler(fs);
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
            ensureInitialized(dkb);
            DatabaseFrameStore store = new DatabaseFrameStore(dkb, driver, url, user, password, table, true);
            FrameStore mergingFrameStore = dkb.getTerminalFrameStore();
            mergingFrameStore.setDelegate(store);
            updateKnowledgeBase(dkb);
            dkb.setTerminalFrameStore(mergingFrameStore);
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Unable to load knowledgebase", e);
            errors.add(e);
        }
    }
    
    protected void updateKnowledgeBase(DefaultKnowledgeBase kb) {
        KnowledgeBaseUtils.update(kb);
    }

    public void saveKnowledgeBase(KnowledgeBase kb, PropertyList sources, Collection errors) {
        if (kb instanceof DefaultKnowledgeBase) {
            FrameStore store = ((DefaultKnowledgeBase) kb).getTerminalFrameStore();
            if (isMergingFrameStore(store)) {
                DatabaseFrameStore databaseStore = (DatabaseFrameStore) store.getDelegate();
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

    private static boolean isMergingFrameStore(FrameStore fs) {
        boolean result = false;
        if (Proxy.isProxyClass(fs.getClass())) {
            result = Proxy.getInvocationHandler(fs) instanceof MergingFrameStoreHandler;
        }
        return result;
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
