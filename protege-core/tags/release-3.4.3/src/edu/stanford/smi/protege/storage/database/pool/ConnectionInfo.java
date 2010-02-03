package edu.stanford.smi.protege.storage.database.pool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;



public class ConnectionInfo {
    private Connection connection;
    private long lastAccessTime;
    private Map<String, PreparedStatement> preparedStatementMap = new HashMap<String, PreparedStatement>();
    private Statement genericStatement;
    
    public ConnectionInfo(Connection connection) {
        this.connection = connection;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void touch() {
        lastAccessTime = System.currentTimeMillis();
    }
    
    public long getLastAccessTime() {
        return lastAccessTime;
    }
    
    public PreparedStatement getPreparedStatement(String text) throws SQLException {
        PreparedStatement stmt = (PreparedStatement) preparedStatementMap.get(text);
        if (stmt == null) {
            stmt = connection.prepareStatement(text);
            preparedStatementMap.put(text, stmt);
        }
        return stmt;
    }
    
    public synchronized Statement getStatement() throws SQLException {
        if (genericStatement == null) {
            genericStatement = connection.createStatement();
        }
        return genericStatement;
    }
    
    public void close() throws SQLException {
        closeStatements();
        connection.close();
    }
    
    public synchronized void closeStatements() throws SQLException {
        for (PreparedStatement stmt : preparedStatementMap.values()) {
            stmt.close();
        }
        preparedStatementMap.clear();
        if (genericStatement != null) {
            genericStatement.close();
            genericStatement = null;
        }
    }
    

}
