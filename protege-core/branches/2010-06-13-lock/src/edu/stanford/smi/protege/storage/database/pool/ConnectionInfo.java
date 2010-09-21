package edu.stanford.smi.protege.storage.database.pool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;



public class ConnectionInfo {
    private static int nextId = 0;
    private int id = nextId++;

    private Connection connection;
    private Map<String, PreparedStatement> preparedStatementMap = new HashMap<String, PreparedStatement>();
    private Statement genericStatement;
    
    private long lastAccessTime;
    private boolean InformedUserOfLongConnectionTime = false;
    private Exception connectionCallStack;
    
    public ConnectionInfo(Connection connection) {
        this.connection = connection;
    }
    
    public int getId() {
        return id;
    }
    
    
    /**
     * Synchronized by the connection pool.
     */
    public void touch() {
        lastAccessTime = System.currentTimeMillis();
    }
    
    /**
     * Synchronized by the connection pool.
     */
    public long getLastAccessTime() {
        return lastAccessTime;
    }
    
    public boolean getInformedUserOfLongConnectionTime() {
        return InformedUserOfLongConnectionTime;
    }
    
    public void setInformedUserOfLongConnectionTime(boolean informedUserOfLongConnectionTime) {
        InformedUserOfLongConnectionTime = informedUserOfLongConnectionTime;
    }
    
    public Exception getConnectionCallStack() {
        return connectionCallStack;
    }
    
    public void setConnectionCallStack(Exception connectionCallStack) {
        this.connectionCallStack = connectionCallStack;
    }
    
    public Connection getConnection() {
        return connection;
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
