package edu.stanford.smi.protege.storage.database;

//ESCA*JAVA0100

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public class RobustConnection {
	private static final transient Logger log = Log.getLogger(RobustConnection.class);
	
    private static final int ALLOWANCE = 100;
    private static final int ORACLE_MAX_VARCHAR_SIZE = 3166 - ALLOWANCE;
    private static final int SQLSERVER_MAX_VARCHAR_SIZE = 900 - ALLOWANCE;
    private static final int DEFAULT_MAX_VARCHAR_SIZE = 255;

    private Map<String, PreparedStatement> _stringToPreparedStatementMap = new HashMap<String, PreparedStatement>();
    private Object connectionLock = new Object();
    private Connection _connection;
    private long lastAccessTime;
    private ConnectionReaperAlgorithm connectionReaper;
    private Statement _genericStatement;
    private String _url;
    private String _username;
    private String _password;
    private boolean _supportsBatch;
    private char _escapeChar;
    private String _escapeClause;
    private boolean _supportsTransactions;
    private int _maxVarcharSize;
    
    private RemoteSession session;
    private TransactionMonitor transactionMonitor;
    
    private String _driverLongvarcharTypeName;
    private String _driverTinyIntTypeName;
    private String _driverBitTypeName;
    private String _driverSmallIntTypeName;
    private String _driverIntegerTypeName;
    private String _driverVarcharTypeName;
    private String _driverCharTypeName;
    public static final String OLD_PROPERTY_LONGVARCHAR_TYPE_NAME = "SimpleJdbcDatabaseManager.longvarcharname";
    public static final String PROPERTY_REFRESH_CONNECTIONS_TIME="Database.refresh.connections.interval";
    public static final String PROPERTY_LONGVARCHAR_TYPE_NAME = "Database.typename.longvarchar";
    public static final String PROPERTY_VARCHAR_TYPE_NAME = "Database.typename.varchar";
    public static final String PROPERTY_INTEGER_TYPE_NAME = "Database.typename.integer";
    public static final String PROPERTY_SMALL_INTEGER_TYPE_NAME = "Database.typename.small_integer";
    // private final static String PROPERTY_TINY_INTEGER_TYPE_NAME =
    // "Database.typename.tiny_integer";
    public static final String PROPERTY_BIT_TYPE_NAME = "Database.typename.bit";
    public static final String PROPERTY_CHAR_TYPE_NAME = "Database.typename.char";
    
    /*
     * This interval must be significantly longer than the length of time it takes to 
     * make a query to the database.  Otherwise there will exist a risk that a connection
     * or statement will be closed while a query is in progress.  
     */
    private static long connectionRefreshInterval;
    static {
    	int minutes = ApplicationProperties.getIntegerProperty(PROPERTY_REFRESH_CONNECTIONS_TIME, 60);
    	connectionRefreshInterval = minutes * 60 * 1000;
    }
    
    private Integer transactionIsolationLevel = null;

    public RobustConnection(String driver, String url, String username, String password,
                            TransactionMonitor transactionMonitor, RemoteSession session) throws SQLException {
        _url = url;
        _username = username;
        _password = password;
        
        this.transactionMonitor = transactionMonitor;
        this.session = session;

        Class clas = SystemUtilities.forName(driver);
        if (clas == null) {
            throw new RuntimeException("class not found: " + driver);
        }
        // Log.trace("initializing connection", this, "RobustConnection");
        lastAccessTime = System.currentTimeMillis();
        setupConnection();
        initializeConnectionReaper();
        initializeMaxVarcharSize();
        initializeSupportsBatch();
        initializeSupportsEscapeSyntax();
        initializeDriverTypeNames();
        initializeSupportsTransactions();
        // dumpTypes();
    }

    private void initializeConnectionReaper() {
    	connectionReaper = new ConnectionReaperAlgorithm();
    	connectionReaper.startThread();
    }
    
    public void setAutoCommit(boolean b) throws SQLException {
        getConnection().setAutoCommit(b);
    }

    public void commit() throws SQLException {
        getConnection().commit();
    }

    private void setupConnection() throws SQLException {
        _connection = DriverManager.getConnection(_url, _username, _password);
        TransactionIsolationLevel defaultLevel = ServerProperties.getDefaultTransactionIsolationLevel();
        if (defaultLevel != null) {
          _connection.setTransactionIsolation(defaultLevel.getJdbcLevel());
        }
    }

    public void dispose() throws SQLException {
    	closeConnection();
    	connectionReaper.stopThread();
    }
    
    private void closeConnection() throws SQLException {
        closeStatements();
        synchronized (connectionLock) {
        	if (_connection != null) {
                _connection.close();
                _connection = null;
        	}
        }
    }

    public void closeStatements() throws SQLException {
    	synchronized (connectionLock) {
    		Iterator<PreparedStatement> i = _stringToPreparedStatementMap.values().iterator();
    		while (i.hasNext()) {
    			PreparedStatement stmt = i.next();
    			stmt.close();
    		}
    		_stringToPreparedStatementMap.clear();
    	}
    	if (_genericStatement != null) {
    		_genericStatement.close();
    		_genericStatement = null;
    	}
    }

    private void initializeMaxVarcharSize() throws SQLException {
        String property = SystemUtilities.getSystemProperty("database.varcharsize");
        if (property != null && property.length() != 0) {
            _maxVarcharSize = Integer.parseInt(property);
        } else if (isOracle()) {
            _maxVarcharSize = ORACLE_MAX_VARCHAR_SIZE;
        } else if (isSqlServer()) {
            _maxVarcharSize = SQLSERVER_MAX_VARCHAR_SIZE;
        } else {
            _maxVarcharSize = DEFAULT_MAX_VARCHAR_SIZE;
        }
    }

    private void initializeSupportsBatch() throws SQLException {
        _supportsBatch = getConnection().getMetaData().supportsBatchUpdates();
        if (!_supportsBatch) {
            String s = "This JDBC driver does not support batch update.";
            s += " For much better performance try using a newer driver";
            Log.getLogger().warning(s);
        }
    }

    private void initializeSupportsTransactions() throws SQLException {
        _supportsTransactions = getConnection().getMetaData().supportsTransactions();
        if (!_supportsTransactions) {
            Log.getLogger().warning("This database does not support transactions");
        }
    }

    private void initializeSupportsEscapeSyntax() throws SQLException {
        _escapeChar = 0;
        _escapeClause = "";
        boolean escapeSupported = getConnection().getMetaData().supportsLikeEscapeClause();
        if (escapeSupported) {
            if (isMySql()) {
                _escapeChar = '\\';
            } else {
                _escapeChar = '|';
                _escapeClause = "{ESCAPE '" + _escapeChar + "'}";

            }
        } else {
            Log.getLogger().warning("This driver does not support SQL Escape processing.");
        }
    }

    public char getEscapeCharacter() {
        return _escapeChar;
    }

    public String getEscapeClause() {
        return _escapeClause;
    }

    public boolean supportsBatch() {
        return _supportsBatch;
    }

    public PreparedStatement getPreparedStatement(String text) throws SQLException {
        synchronized (connectionLock) {
        	PreparedStatement stmt = (PreparedStatement) _stringToPreparedStatementMap.get(text);
        	lastAccessTime = System.currentTimeMillis();
        	if (stmt == null) {
        		stmt = getConnection().prepareStatement(text);
        		_stringToPreparedStatementMap.put(text, stmt);
        	}
            return stmt;
        }
    }

    public Statement getStatement() throws SQLException {
        if (_genericStatement == null) {
            _genericStatement = getConnection().createStatement();
        }
        return _genericStatement;
    }

    public void checkConnection() throws SQLException {
    	synchronized (connectionLock) {
    		if (_connection == null) {
    			setupConnection();
    		} else if (_connection.isClosed()) {
    			Log.getLogger().warning("Found closed connection, reinitializing...");
    			closeConnection();
    		}
    	}
    }

    public boolean isOracle() throws SQLException {
        return getDatabaseProductName().equalsIgnoreCase("oracle");
    }

    public boolean isSqlServer() throws SQLException {
        return getDatabaseProductName().equalsIgnoreCase("Microsoft SQL Server");
    }

    public boolean isMsAccess() throws SQLException {
        return getDatabaseProductName().equalsIgnoreCase("access");
    }

    public boolean isMySql() throws SQLException {
        return getDatabaseProductName().equalsIgnoreCase("mysql");
    }

    public boolean isPostgres() throws SQLException {
        return getDatabaseProductName().equalsIgnoreCase("PostgreSQL");
    }

    public String getDatabaseProductName() throws SQLException {
        return getConnection().getMetaData().getDatabaseProductName();
    }
    
    public int getDatabaseMajorVersion() throws SQLException {
      return getConnection().getMetaData().getDatabaseMajorVersion();
    }
    
    public int getDatabaseMinorVersion() throws SQLException {
      return getConnection().getMetaData().getDatabaseMinorVersion();
    }

    public int getMaxVarcharSize() {
        return _maxVarcharSize;
    }

    private void initializeDriverTypeNames() throws SQLException {
        String longvarbinaryTypeName = null;
        String blobTypeName = null;
        String clobTypeName = null;

        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getTypeInfo();
        while (rs.next()) {
            String name = rs.getString("TYPE_NAME");
            int type = rs.getInt("DATA_TYPE");
            if (name.length() == 0) {
                continue;
            }
            switch (type) {
                case Types.LONGVARCHAR:
                    if (_driverLongvarcharTypeName == null) {
                        _driverLongvarcharTypeName = name;
                    }
                    break;
                case Types.LONGVARBINARY:
                    if (longvarbinaryTypeName == null) {
                        longvarbinaryTypeName = name;
                    }
                    break;
                case Types.CLOB:
                    if (clobTypeName == null) {
                        clobTypeName = name;
                    }
                    break;
                case Types.BLOB:
                    if (blobTypeName == null) {
                        blobTypeName = name;
                    }
                    break;
                case Types.TINYINT:
                    if (_driverTinyIntTypeName == null) {
                        _driverTinyIntTypeName = name;
                    }
                    break;
                case Types.BIT:
                    if (_driverBitTypeName == null) {
                        _driverBitTypeName = name;
                    }
                    break;
                case Types.SMALLINT:
                    if (_driverSmallIntTypeName == null) {
                        _driverSmallIntTypeName = name;
                    }
                    break;
                case Types.INTEGER:
                    if (_driverIntegerTypeName == null) {
                        _driverIntegerTypeName = name;
                    }
                    break;
                case Types.VARCHAR:
                    if (_driverVarcharTypeName == null) {
                        _driverVarcharTypeName = name;
                    }
                    break;
                case Types.CHAR:
                    if (_driverCharTypeName == null) {
                        _driverCharTypeName = name;
                    }
                    break;
                default:
                    // do nothing
                    break;
            }
        }
        rs.close();
        if (_driverLongvarcharTypeName == null) {
            if (longvarbinaryTypeName == null) {
                if (clobTypeName == null) {
                    _driverLongvarcharTypeName = blobTypeName;
                } else {
                    _driverLongvarcharTypeName = clobTypeName;
                }
            } else {
                _driverLongvarcharTypeName = longvarbinaryTypeName;
            }
            if (_driverLongvarcharTypeName == null && isPostgres()) {
                _driverLongvarcharTypeName = "TEXT";
            }
        }
        if (_driverIntegerTypeName == null) {
            _driverIntegerTypeName = "INTEGER";
        }
        if (_driverSmallIntTypeName == null) {
            _driverSmallIntTypeName = _driverIntegerTypeName;
        }
        if (_driverTinyIntTypeName == null) {
            _driverTinyIntTypeName = _driverSmallIntTypeName;
        }
        if (_driverBitTypeName == null) {
            _driverBitTypeName = _driverTinyIntTypeName;
        }
        if (_driverVarcharTypeName == null || isPostgres() || isSqlServer()) {
            _driverVarcharTypeName = "VARCHAR";
        }
        if (isOracle()) { 
            _driverLongvarcharTypeName = "CLOB";  // can't search on the default LONG.
        }
    }

    private static String getName(String typeName, String driverName) {
        String userTypeName = ApplicationProperties.getApplicationOrSystemProperty(typeName);
        return (userTypeName == null || userTypeName.length() == 0) ? driverName : userTypeName;
    }

    public String getLongvarcharTypeName() {
        String name = SystemUtilities.getSystemProperty(OLD_PROPERTY_LONGVARCHAR_TYPE_NAME);
        name = name;
        if (name == null || name.length() == 0) {
            name = getName(PROPERTY_LONGVARCHAR_TYPE_NAME, _driverLongvarcharTypeName);
        }
        if (name == null) {
            // Better to use something here than nothing. At least the create
            // table call will work
            name = getVarcharTypeName();
            Log.getLogger().warning("Using VARCHAR in place of LONGVARCHAR, long strings will be truncated.");
        }
        return name;
    }

    public String getSmallIntTypeName() {
        return getName(PROPERTY_SMALL_INTEGER_TYPE_NAME, _driverSmallIntTypeName);
    }

    public String getIntegerTypeName() {
        return getName(PROPERTY_INTEGER_TYPE_NAME, _driverIntegerTypeName);
    }

    public String getBitTypeName() {
        return getName(PROPERTY_BIT_TYPE_NAME, _driverBitTypeName);
    }

    public String getVarcharTypeName() {
        return getName(PROPERTY_VARCHAR_TYPE_NAME, _driverVarcharTypeName);
    }

    public String getCharTypeName() {
        return getName(PROPERTY_CHAR_TYPE_NAME, _driverCharTypeName);
    }

    //    private void dumpTypes() throws SQLException {
    //        ResultSet rs = _connection.getMetaData().getTypeInfo();
    //        while (rs.next()) {
    //            System.out.println("TYPE_NAME: " + rs.getString(1));
    //            System.out.println("\tDATA_TYPE: " + rs.getInt(2));
    //            System.out.println("\tPRECISION: " + rs.getLong(3));
    //            System.out.println("\tLITERAL_PREFIX: " + rs.getString(4));
    //            System.out.println("\tLITERAL_SUFFIX: " + rs.getString(5));
    //            System.out.println("\tCREATE_PARAMS: " + rs.getString(6));
    //            System.out.println("\tNULLABLE: " + rs.getShort(7));
    //            System.out.println("\tCASE_SENSITIVE: " + rs.getBoolean(8));
    //            System.out.println("\tSEARCHABLE: " + rs.getShort(9));
    //            System.out.println("\tUNSIGNED_ATTRIBUTE: " + rs.getBoolean(10));
    //            System.out.println("\tFIXED_PREC_SCALE: " + rs.getBoolean(11));
    //            System.out.println("\tAUTO_INCREMENT: " + rs.getBoolean(12));
    //            System.out.println("\tLOCAL_TYPE_NAME: " + rs.getString(13));
    //            System.out.println("\tMINIMUM_SCALE: " + rs.getShort(14));
    //            System.out.println("\tMAXIMUM_SCALE: " + rs.getShort(15));
    //            System.out.println("\tSQL_DATA_TYPE: " + rs.getShort(16));
    //            System.out.println("\tSQL_DATETIME_SUB: " + rs.getShort(17));
    //            System.out.println("\tNUM_PREC_RADIX: " + rs.getInt(18));
    //        }
    //        rs.close();
    //    }

    public boolean supportsCaseInsensitiveMatches() throws SQLException {
        return !(isOracle() || isPostgres());
    }

    public boolean supportsIndexOnFunction() throws SQLException {
        return isOracle() || isPostgres();
    }

    public boolean beginTransaction() {
        if (!sessionOk()) {
          return false;
        }
        boolean begun = false;
        try {
            if (_supportsTransactions) {
                if (transactionMonitor.getNesting() == 0) {
                    if (isMsAccess()) {
                        closeStatements();
                    }
                    getConnection().setAutoCommit(false);
                }
                transactionMonitor.beginTransaction();
            }
            begun = true;
        } catch (SQLException e) {
            Log.getLogger().warning(e.toString());
        }
        return begun;
    }

    public boolean commitTransaction() {
        if (!sessionOk()) {
          return false;
        }
        boolean committed = false;
        try {
            if (_supportsTransactions && transactionMonitor.getNesting() > 0) {
                transactionMonitor.commitTransaction();
                if (transactionMonitor.getNesting() == 0) {
                    getConnection().commit();
                    getConnection().setAutoCommit(true);
                }
            }
            committed = true;
        } catch (SQLException e) {
            Log.getLogger().warning(e.toString());
        }
        return committed;
    }

    public boolean rollbackTransaction() {
        if (!sessionOk()) {
          return false;
        }
        boolean rolledBack = false;
        try {
            if (_supportsTransactions && transactionMonitor.getNesting() > 0) {
                transactionMonitor.rollbackTransaction();
                 if (transactionMonitor.getNesting() == 0) {
                    getConnection().rollback();
                    getConnection().setAutoCommit(true);
                }
            }
            rolledBack = true;
        } catch (SQLException e) {
            Log.getLogger().warning(e.toString());
        }
        return rolledBack;
    }
    
    private boolean sessionOk() {
      if (ServerFrameStore.getCurrentSession() == null) {
        return session == null;
      } else {
        return ServerFrameStore.getCurrentSession().equals(session);
      }
    }
    
    public boolean supportsTransactions() {
      return _supportsTransactions;
    }
    
    public int getTransactionIsolationLevel() throws SQLException {
      if (transactionIsolationLevel != null) {
        return transactionIsolationLevel;
      }
      return transactionIsolationLevel = getConnection().getTransactionIsolation();
    }


    public void setTransactionIsolationLevel(int level) throws SQLException {
      transactionIsolationLevel = null;
      try {
        getConnection().setTransactionIsolation(level);
      } catch (SQLException sqle) {
        Log.getLogger().log(Level.WARNING, "Problem setting the transaction isolation level", sqle);
        transactionIsolationLevel = null;
        throw sqle;
      }
    }
    
    private Connection getConnection() throws SQLException {
    	synchronized (connectionLock) {
    		lastAccessTime = System.currentTimeMillis();
    		if (_connection == null) {
    			setupConnection();
    		}
    		return _connection;
    	}
    }
    
    /*
     * Thanks to Bob Dionne for this approach.
     * 
     */
    private class ConnectionReaperAlgorithm implements Runnable {
    	private boolean shuttingDown = false;
    	private Thread thread;
    	
    	public void startThread() {
    		thread = new Thread(this, "Database Connection Reaper");
    		thread.setDaemon(true);
    		thread.start();
    	}
    	
		private void stopThread() {
			synchronized (connectionLock) {
				shuttingDown = true;
			}
			thread.interrupt();
			thread = null;
		}

		public void run() {
			synchronized (connectionLock) {
				while (!shuttingDown) {
					long now = System.currentTimeMillis();
					if (_connection != null && now - lastAccessTime > connectionRefreshInterval) {
						try {
							closeConnection();
						}
						catch (Throwable t) {
							if (log.isLoggable(Level.FINE)) {
								log.log(Level.FINE, "Exception caught closing connection", t);
							}
						}
						_connection = null;
					}
					try {
						connectionLock.wait(connectionRefreshInterval);
					}
					catch (InterruptedException ie) {
						if (log.isLoggable(Level.FINE)) {
							log.log(Level.FINE, "Interrupted thread - hopefully because of a close operation", ie);
						}
						if (shuttingDown) {
							return;
						}
						else {
							log.warning("Unexpected interrupt to database connection reaper thread " + ie);
						}
					}
				}
			}
		}
		

    	
    }
    
}