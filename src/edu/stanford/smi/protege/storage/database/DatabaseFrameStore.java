package edu.stanford.smi.protege.storage.database;

import java.sql.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

public class DatabaseFrameStore extends SimpleFrameStore {
    private DatabaseFrameDb _databaseFrameDb;
    private Map nameToFrameMap = new HashMap();

    public DatabaseFrameStore(DefaultKnowledgeBase kb, String driver, String url, String username, String password,
            String tablename, boolean modifiable) {
        super(kb, getBasicFrameStore(new DatabaseFrameDb(getFrameFactory(kb), driver, url, username, password,
                tablename)));
        _databaseFrameDb = getDatabaseFrameDb();
        _databaseFrameDb.setModifiable(modifiable);
        // addSystemFrames();
    }

    public void close() {
        _databaseFrameDb.close();
        _databaseFrameDb = null;
    }

    private static FrameFactory getFrameFactory(DefaultKnowledgeBase kb) {
        return (kb == null) ? null : kb.getFrameFactory();
    }

    private DatabaseFrameDb getDatabaseFrameDb() {
        ClosureCachingBasicFrameStore closureDelegate = (ClosureCachingBasicFrameStore) getHelper();
        ValueCachingBasicFrameStore valuesDelegate = (ValueCachingBasicFrameStore) closureDelegate.getDelegate();
        return (DatabaseFrameDb) valuesDelegate.getDelegate();
    }

    public void saveKnowledgeBase(KnowledgeBase kb) throws SQLException {
        _databaseFrameDb.saveKnowledgeBase(kb);
    }

    public void reinitializeTable() {
        _databaseFrameDb.createNewTableAndIndices();
        setHelper(getBasicFrameStore(_databaseFrameDb));
        addSystemFrames();
    }

    private static ClosureCachingBasicFrameStore getBasicFrameStore(DatabaseFrameDb db) {
        return new ClosureCachingBasicFrameStore(new ValueCachingBasicFrameStore(db));
    }

    public String getTableName() {
        return getDatabaseFrameDb().getTableName();
    }

    public Frame getFrame(String name) {
        Frame frame = (Frame) nameToFrameMap.get(name);
        if (!isGood(frame, name)) {
            frame = super.getFrame(name);
            nameToFrameMap.put(name, frame);
        }
        return frame;
    }

    public void setFrameName(Frame frame, String name) {
        super.setFrameName(frame, name);
        nameToFrameMap.put(name, frame);
    }

    private static boolean isGood(Frame frame, String name) {
        return frame != null && !frame.isDeleted() && frame.getName().equals(name);
    }

}
