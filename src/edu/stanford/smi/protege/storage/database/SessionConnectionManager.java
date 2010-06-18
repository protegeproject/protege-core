package edu.stanford.smi.protege.storage.database;

//ESCA*JAVA0100

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.storage.database.pool.ConnectionPool;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public abstract class SessionConnectionManager {
    private static final transient Logger log = Log.getLogger(RobustConnection.class);
    private static int idCounter = 0;


    private int id;

    private Object          connectionMonitor = new Object();
    private int             referenceCounter = 0;
    private Thread          connectionOwner;
    private Connection      connection;
    private ConnectionPool  pool;
    
    private int    referenceCount = 0;
    
    private RemoteSession session;
    private TransactionMonitor transactionMonitor;
    
    
    private Integer transactionIsolationLevel = null;

    @SuppressWarnings("unchecked")
    public SessionConnectionManager(String driver, String url, String username, String password,
                                    TransactionMonitor transactionMonitor, RemoteSession session) throws SQLException {
        id = idCounter++;
        pool = ConnectionPool.getConnectionPool(driver, url, username, password);
        this.transactionMonitor = transactionMonitor;
        this.session = session;


    }


    public abstract boolean supportsTransactions();
    public abstract boolean isMsAccess() throws SQLException;


    private Connection newConnection() throws SQLException {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Opening connection for robust connection manager #" + id);
        }
        Connection connection = pool.getConnection();
        TransactionIsolationLevel defaultLevel = ServerProperties.getDefaultTransactionIsolationLevel();
        if (defaultLevel != null) {
            connection.setTransactionIsolation(defaultLevel.getJdbcLevel());
        }
        return connection;
    }


    public void dispose() throws SQLException {
    	if (connection != null) {
    		pool.ungetConnection(connection);
    	}
    	pool.dereference();
    }
    
    public void closeStatements() throws SQLException {
    	pool.closeStatements(connection);
    }


    public PreparedStatement getPreparedStatement(String text) throws SQLException {
        return pool.getPreparedStatement(getConnection(), text);
    }

    public Statement getStatement() throws SQLException {
        return pool.getStatement(getConnection());
    }


    public boolean beginTransaction() {
        if (!sessionOk()) {
            return false;
        }
        boolean begun = false;
        try {
            reference();
            if (supportsTransactions()) {
                if (transactionMonitor.getNesting() == 0) {
                    if (isMsAccess()) {
                        pool.closeStatements(getConnection());
                    }
                    getConnection().setAutoCommit(false);
                }
                transactionMonitor.beginTransaction();
            }
            begun = true;
            if (log.isLoggable(Level.FINE)) {
                log.fine("Thead " + Thread.currentThread() + " locking connection " + pool.getId(getConnection()));
            }
        } 
        catch (SQLException e) {
            Log.getLogger().warning(e.toString());
        }
        finally {
            dereference();
        }
        return begun;
    }

    public boolean commitTransaction() {
        if (!sessionOk()) {
            return false;
        }
        boolean committed = false;
        try {
            reference();
            if (supportsTransactions() && transactionMonitor.getNesting() > 0) {
                transactionMonitor.commitTransaction();
                if (transactionMonitor.getNesting() == 0) {
                    getConnection().commit();
                    getConnection().setAutoCommit(true);
                }
            }
            committed = true;
        } 
        catch (SQLException e) {
            Log.getLogger().warning(e.toString());
        }
        finally {
            dereference();
        }
        return committed;
    }

    public boolean rollbackTransaction() {
        if (!sessionOk()) {
            return false;
        }
        boolean rolledBack = false;
        try {
            reference();
            if (supportsTransactions() && transactionMonitor.getNesting() > 0) {
                transactionMonitor.rollbackTransaction();
                if (transactionMonitor.getNesting() == 0) {
                    getConnection().rollback();
                    getConnection().setAutoCommit(true);
                }
            }
            rolledBack = true;
        } 
        catch (SQLException e) {
            Log.getLogger().warning(e.toString());
        }
        finally {
            dereference();
        }
        return rolledBack;
    }
    
    protected synchronized Connection getConnection() throws SQLException {
    	synchronized (connectionMonitor) {
    		if (connection == null) {
    			connection = newConnection();
    		}
    		return connection;
    	}
    }
    
	public void reference() {
		synchronized (connectionMonitor) {
			Thread me = Thread.currentThread();
			while (connectionOwner != null && !connectionOwner.equals(me)) {
				try {
					connectionMonitor.wait();
				} catch (InterruptedException e) {
					log.log(Level.WARNING, "shouldn't", e);
				}
			}
			referenceCount++;
			connectionOwner = me;
		}
    }
    
    public synchronized void dereference() {
    	synchronized (connectionMonitor) {
    		referenceCount--;
    		
    		if (referenceCount == 0) {
    			if (connection != null) {
    				pool.ungetConnection(connection);
    			}
    			connectionOwner = null;
    			connectionMonitor.notifyAll();
    		}
    	}
    }


    private boolean sessionOk() {
	    if (ServerFrameStore.getCurrentSession() == null) {
	        return session == null;
	    } else {
	        return ServerFrameStore.getCurrentSession().equals(session);
	    }
	}


	public int getTransactionIsolationLevel() throws SQLException {
        if (transactionIsolationLevel != null) {
            return transactionIsolationLevel;
        }
        try {
            reference();
            return transactionIsolationLevel = getConnection().getTransactionIsolation();
        }
        finally {
            dereference();
        }
    }



    public void setTransactionIsolationLevel(int level) throws SQLException {
        transactionIsolationLevel = null;
        try {
            reference();
            getConnection().setTransactionIsolation(level);
        } catch (SQLException sqle) {
            Log.getLogger().log(Level.WARNING, "Problem setting the transaction isolation level", sqle);
            transactionIsolationLevel = null;
            throw sqle;
        }
        finally {
            dereference();
        }
    }

    


}
