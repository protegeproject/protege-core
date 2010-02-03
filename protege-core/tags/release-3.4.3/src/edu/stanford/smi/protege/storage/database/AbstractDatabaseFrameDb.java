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
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public abstract class AbstractDatabaseFrameDb implements DatabaseFrameDb {

	public static Logger log	= Log.getLogger( AbstractDatabaseFrameDb.class );
	private static int	traceCount	= 0;

	private static void traceUpdate(PreparedStatement stmt) {
	    traceUpdate(stmt, "");
	}

	private static void traceUpdate(PreparedStatement stmt, String append) {
	  if (log.isLoggable(Level.FINE)) {
	    trace(stmt, append, Level.FINE);
	  }
	}

	private static void traceQuery(PreparedStatement stmt) {
	  if (log.isLoggable(Level.FINER)) {
	    trace(stmt, "", Level.FINER);
	  }
	}

	private static boolean isDead(RemoteSession session) {
		return session != null && !Server.getInstance().isActive( session );
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
	  log.log(level, ++traceCount + " SQL: " + text);
	}

	private static void traceQuery(String text) {
	  if (log.isLoggable(Level.FINER)) {
	    trace(text, Level.FINER);
	  }
	}

	private static void traceUpdate(String text) {
	  if (log.isLoggable(Level.FINE)) {
	    trace(text, Level.FINE);
	  }
	}

	protected static ResultSet executeQuery(PreparedStatement stmt) throws SQLException {
	    long startTime = 0;
	    traceQuery(stmt);
	    if (log.isLoggable(Level.FINER)) {
	      startTime = System.nanoTime();
	    }
	    ResultSet ret = stmt.executeQuery();
	    if (log.isLoggable(Level.FINER)) {
	    	float t = (System.nanoTime() - startTime)/1000000;
	    	if (t > 10000) {
	    		Log.getLogger().finer("*** SLOW QUERY: " + t + " msec ***");
	    	}
	    	log.finer("Query took " + t
	                  + " milliseconds (more or less)");	  
	    }
	    return ret;
	}

	protected static int executeUpdate(PreparedStatement stmt) throws SQLException {
	    traceUpdate(stmt);
		long startTime = 0;
		if (log.isLoggable(Level.FINE)) {
			startTime = System.nanoTime();
		}
		int ret =  stmt.executeUpdate();
		if (log.isLoggable(Level.FINE)) {
			float t = (System.nanoTime() - startTime)/1000000;
	    	if (t > 10000) {
	    		Log.getLogger().fine("*** SLOW QUERY: " + t + " msec ***");
	    	}
	    	log.finer("Query took " + t
	                  + " milliseconds (more or less)");
		}
		return ret;
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
					try {
						RobustConnection connection = getCurrentConnection();
						jdbcLevel = connection.getTransactionIsolationLevel();
					} catch( SQLException sqle ) {
						throw new TransactionException( sqle );
					}
					return TransactionIsolationLevel.getTransactionLevel( jdbcLevel );
				}

				@Override
				public void setTransactionIsolationLevel(TransactionIsolationLevel level)
						throws TransactionException {
					int jdbcLevel = level.getJdbcLevel();
					try {
						RobustConnection connection = getCurrentConnection();
						if( connection != null ) {
							connection.setTransactionIsolationLevel( jdbcLevel );
						}
					} catch( SQLException e ) {
						throw new TransactionException( e );
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

	protected ResultSet executeQuery(String text) throws SQLException {
	    return executeQuery(text, 0);
	}

	protected ResultSet executeQuery(String text, int maxRows) throws SQLException {
	    long startTime = 0;
	    traceQuery(text);	    
	    Statement statement = getCurrentConnection().getStatement();
	    // statement.setMaxRows(maxRows);
	    if (log.isLoggable(Level.FINER)) {
	      startTime = System.nanoTime();
	    }
	    ResultSet ret = statement.executeQuery(text);
	    if (log.isLoggable(Level.FINER)) {
	      log.finer("Query took " + (System.nanoTime() - startTime)/1000000.0
	                  + " milliseconds (more or less)");
	    }
	    return ret;
	}

	public String getTableName() {
	    return _table;
	}

	protected int executeUpdate(String text) throws SQLException {
		traceUpdate(text);
		long startTime = 0;
		if (log.isLoggable(Level.FINE)) {
			startTime = System.nanoTime();
		}
		int ret =  getCurrentConnection().getStatement().executeUpdate(text);
		if (log.isLoggable(Level.FINE)) {
			log.fine("Query took " + (System.nanoTime() - startTime)/1000000.0
					+ " milliseconds (more or less)");
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
