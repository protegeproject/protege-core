package edu.stanford.smi.protege.storage.database;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface DatabasePlugin {
    void setDriver(String driver);
    void setTable(String table);
    void setUsername(String username);
    void setPassword(String password);
    void setURL(String url);
}
