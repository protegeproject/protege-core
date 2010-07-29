package edu.stanford.smi.protege.storage.database;

import static edu.stanford.smi.protege.server.framestore.ServerFrameStore.getCurrentSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.ProtegeError;
import edu.stanford.smi.protege.exception.TransactionException;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.model.query.QueryCallback;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public abstract class AbstractDatabaseFrameDb implements DatabaseFrameDb {
	public static Logger log	= Log.getLogger( AbstractDatabaseFrameDb.class );
	public static final String SLOW_QUERY_PROPERTY = "slow.jdbc.query.milliseconds";
	private static final int slowQueryTime = ApplicationProperties.getIntegerProperty(SLOW_QUERY_PROPERTY, 15 * 1000);

	private static int	traceCount	= 0;

	private static void traceUpdate(PreparedStatement stmt, Level level) {
	    traceUpdate(stmt, "", level);
	}

	private static void traceUpdate(PreparedStatement stmt, String append, Level level) {
	  if (log.isLoggable(level)) {
	    trace(stmt, append, level);
	  }
	}

	private static void traceQuery(PreparedStatement stmt, Level level) {
	  if (log.isLoggable(level)) {
	    trace(stmt, "", level);
	  }
	}

	private static void trace(PreparedStatement stmt, String append, Level level) {
	    if (log.isLoggable(level)) {
	        String text = stmt.toString();
	        if (text.indexOf("PreparedStatement") != -1) {
	            int index = text.indexOf(' ');
	            if (index != -1) {
	                text = text.substring(index);
	            }
	        }
	        trace(text + append, level);
	    }
	}

	private static void trace(String text, Level level) {
        if (log.isLoggable(level)) {
            log.log(level, ++traceCount + " SQL: " + text);
        }
	}

	private static void traceQuery(String text, Level level) {
	  if (log.isLoggable(level)) {
	    trace(text, level);
	  }
	}

	private static void traceUpdate(String text, Level level) {
	  if (log.isLoggable(level)) {
	    trace(text, level);
	  }
	}

	protected static ResultSet executeQuery(PreparedStatement stmt) throws SQLException {
	    Level traceLevel = Level.FINER;
	    traceQuery(stmt, traceLevel);
	    
	    long startTime = System.nanoTime();
	    ResultSet ret = stmt.executeQuery();
        double t = (System.nanoTime() - startTime)/1000000.0;
        
        if (t > slowQueryTime) {
            if (!log.isLoggable(traceLevel)) {
                traceQuery(stmt, Level.INFO);
            }
            log.info("*** SLOW QUERY: " + t + " msec ***");
        }
        else if (log.isLoggable(traceLevel)) {
            log.log(traceLevel, "Query took " + t + " milliseconds (more or less)");	  
        }
	    return ret;
	}

	protected static int executeUpdate(PreparedStatement stmt) throws SQLException {
        Level traceLevel = Level.FINE;
	    traceUpdate(stmt, traceLevel);

		long startTime = System.nanoTime();
		int ret =  stmt.executeUpdate();
        double t = (System.nanoTime() - startTime)/1000000.0;

        if (t > slowQueryTime) {
            if (!log.isLoggable(traceLevel)) {
                traceQuery(stmt, Level.INFO);
            }
            log.info("*** SLOW QUERY: " + t + " msec ***");
        }
        else if (log.isLoggable(traceLevel)) {
            log.log(traceLevel, "Query took " + t + " milliseconds (more or less)");	  
        }
		return ret;
	}

	private static boolean isDead(RemoteSession session) {
    	return session != null && !Server.getInstance().isActive( session );
    }

    protected static boolean isNullValue(Object o) {
	    boolean isNull = o == null;
	    if (o instanceof String) {
	        String s = (String) o;
	        isNull = s.trim().length() == 0;
	    }
	    return isNull;
	}

	protected final Map<RemoteSession, RobustConnection>	_connections	= new HashMap<RemoteSession, RobustConnection>();
	protected String										_driver;
	protected FrameFactory									_frameFactory;
	protected boolean										_isInclude;
	protected String										_password;
	protected String										_table;
	protected String										_url;
	protected String										_user;
	private String											frameDbName;
	private TransactionMonitor								transactionMonitor;

	public boolean beginTransaction(String name) {
		if( log.isLoggable( Level.FINE ) ) {
			log.fine( "begin transaction " + name );
		}
		try {
			boolean success = getCurrentConnection().beginTransaction();
			return success;
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}

	}

	private void clearDeadConnections() throws SQLException {
		Iterator<Map.Entry<RemoteSession, RobustConnection>> i = _connections.entrySet().iterator();
		while( i.hasNext() ) {
			Map.Entry<RemoteSession, RobustConnection> entry = i.next();
			RemoteSession session = entry.getKey();
			if( isDead( session ) ) {
				RobustConnection connection = entry.getValue();
				// Log.getLogger().info("Clearing dead connection: " + session);
				connection.dispose();
				i.remove();
			}
		}
	}

	public void close() {
		_frameFactory = null;
		try {
			for( Map.Entry<RemoteSession, RobustConnection> entry : _connections.entrySet() ) {
				RemoteSession session = entry.getKey();
				RobustConnection connection = entry.getValue();
				connection.dispose();
				if (log.isLoggable(Level.FINE)) {
				    log.fine( "Closed DB connection for session: " + session );
				}
			}
			_connections.clear();
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}
	}

	public boolean commitTransaction() {
		if( log.isLoggable( Level.FINE ) ) {
			log.fine( "commit transaction" );
		}
		try {
			return getCurrentConnection().commitTransaction();
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}

	}

	protected RobustConnection createConnection() throws SQLException {
		clearDeadConnections();
		RemoteSession currentSession = getCurrentSession();
		RobustConnection connection = new RobustConnection( _driver, _url, _user, _password,
				getTransactionStatusMonitor(), currentSession );
		_connections.put( currentSession, connection );
		if( log.isLoggable( Level.FINE ) ) {
			log.fine( "Created connection for " + currentSession );
		}
		return connection;
	}

	protected RuntimeException createRuntimeException(SQLException e) {
		try {
			if( getCurrentConnection() != null ) {
				getCurrentConnection().checkConnection();
			}
		} catch( SQLException ex ) {
			// do nothing
		}
		log.info( Log.toString( e ) );

		RuntimeException runtimeEx = new RuntimeException( e.getMessage() );
		runtimeEx.initCause( e );
		return runtimeEx;
	}

	public RobustConnection getCurrentConnection() throws SQLException {
		RemoteSession currentSession = getCurrentSession();
		RobustConnection connection = _connections.get( currentSession );
		if( connection == null ) {
			connection = createConnection();
			_connections.put( currentSession, connection );
		}
		return connection;
	}

	public FrameFactory getFrameFactory() {
		return _frameFactory;
	}

	public String getName() {
		return frameDbName;
	}

	public TransactionMonitor getTransactionStatusMonitor() {
		if( transactionMonitor == null ) {
			transactionMonitor = new TransactionMonitor() {

				@Override
				public TransactionIsolationLevel getTransationIsolationLevel()
						throws TransactionException {
					int jdbcLevel = Connection.TRANSACTION_NONE;
					RobustConnection connection = null;
					try {
						connection = getCurrentConnection();
						jdbcLevel = connection.getTransactionIsolationLevel();
					} catch( SQLException sqle ) {
						throw new TransactionException( sqle );
					}
					finally {
                        // WARNING... what if this is called while the database nfs is doing something?
					    connection.setIdle(true);
					}
					return TransactionIsolationLevel.getTransactionLevel( jdbcLevel );
				}

				@Override
				public void setTransactionIsolationLevel(TransactionIsolationLevel level)
						throws TransactionException {
					int jdbcLevel = level.getJdbcLevel();
					RobustConnection connection = null;
					try {
						connection = getCurrentConnection();
						if( connection != null ) {
							connection.setTransactionIsolationLevel( jdbcLevel );
						}
					} catch( SQLException e ) {
						throw new TransactionException( e );
					}
					finally {
                        // WARNING... what if this is called while the database nfs is doing something?
					    connection.setIdle(true);
					}
					
				}

			};
		}
		return transactionMonitor;
	}

	public void initialize(FrameFactory factory, String driver, String url, String user,
			String pass, String table, boolean isInclude) {
		if( log.isLoggable( Level.FINE ) ) {
			log.fine( "Constructing database frame narrow frame store for " + driver + " " + url
					+ " " + table );
			log.fine( "No delegates" );
		}
		_isInclude = isInclude;
		_table = table;
		_frameFactory = factory;
		_driver = driver;
		_url = url;
		_user = user;
		_password = pass;
		try {
			createConnection();
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}
	}

	public boolean rollbackTransaction() {
		if( log.isLoggable( Level.FINE ) ) {
			log.fine( "roll back transaction" );
		}
		try {
			return getCurrentConnection().rollbackTransaction();
		} catch( SQLException e ) {
			throw createRuntimeException( e );
		}

	}

	public void setName(String name) {
		frameDbName = name;
	}

	public String getTableName() {
        return _table;
    }

    protected ResultSet executeQuery(String text) throws SQLException {
	    return executeQuery(text, 0);
	}

	protected ResultSet executeQuery(String text, int maxRows) throws SQLException {
	    Level traceLevel = Level.FINER;
	    traceQuery(text, traceLevel);	    

	    Statement statement = getCurrentConnection().getStatement();
	    long startTime = System.nanoTime();
	    ResultSet ret = statement.executeQuery(text);
	    double t = (System.nanoTime() - startTime)/1000000.0;

        if (t > slowQueryTime) {
            if (!log.isLoggable(traceLevel)) {
                traceQuery(text, Level.INFO);
            }
            log.info("*** SLOW QUERY: " + t + " msec ***");
        }
        else if (log.isLoggable(traceLevel)) {
            log.log(traceLevel, "Query took " + t + " milliseconds (more or less)");	  
        }
	    return ret;
	}

	/*
	 * WARNING - this routine does not have the slow query logging logic because it is 
	 *           used in initialization routines such as createIndicies(), createTable(),
	 *           dropTableIfExists(), and overwriteKB(KnowledgeBase, boolean).
	 */
	protected int executeUpdate(String text) throws SQLException {
	    Level traceLevel = Level.FINE;
		traceUpdate(text, traceLevel);

		long startTime = System.nanoTime();
		int ret =  getCurrentConnection().getStatement().executeUpdate(text);
        double t = (System.nanoTime() - startTime)/1000000.0;

        if (log.isLoggable(traceLevel)) {
            log.log(traceLevel, "Query took " + t + " milliseconds (more or less)");	  
        }
		return ret;
	}

	public void executeQuery(Query query, final QueryCallback callback) {
	  new Thread(new Runnable() {
	      public void run() {
	          callback.handleError(new ProtegeError("Not implemented yet"));
	      }
	    },
	             "Vacuous Callback Results Thread");
	}
}
