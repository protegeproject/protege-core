package edu.stanford.smi.protege.storage.jdbc;

import java.sql.*;
import java.util.*;

import edu.stanford.smi.protege.util.*;

/**
 * A connection that can reestablish itself if it gets disconnected.  It also caches
 * prepared statements.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class RobustConnection {
    private String _username;
    private String _password;
    private String _url;
    private String _driver;
    private Connection _connection;
    private Map _statements = new HashMap();

    public RobustConnection(String driver, String url, String username, String password) throws SQLException {
        _url = url;
        _username = username;
        _password = password;
        _driver = driver;

        Class clas = SystemUtilities.forName(_driver);
        if (clas == null) {
            throw new RuntimeException("class not found: " + _driver);
        }
        setupConnection();
    }

    private void setupConnection() throws SQLException {
        _connection = null;
        _statements.clear();
        _connection = DriverManager.getConnection(_url, _username, _password);
    }

    public void close() throws SQLException {
        // Log.trace("Closing " + _statements.size() + " statements", this, "closeAll");
        Iterator i = _statements.values().iterator();
        while (i.hasNext()) {
            PreparedStatement stmt = (PreparedStatement) i.next();
            stmt.close();
        }
        _statements.clear();
        _connection.close();
        _connection = null;
    }

    private void checkConnection() throws SQLException {
        if (!connectionIsOpen()) {
            Log.getLogger().info("database connection lost, reestablishing...");
            setupConnection();
        }
    }

    private boolean connectionIsOpen() throws SQLException {
        return _connection != null &&! _connection.isClosed();
    }

    public PreparedStatement prepareStatement(String stmtText) throws SQLException {
        checkConnection();
        PreparedStatement result = (PreparedStatement) _statements.get(stmtText);
        if (result == null) {
            result = _connection.prepareStatement(stmtText);
            _statements.put(stmtText, result);
        } else if (stmtText.indexOf('?') >= 0) {
            result.clearParameters();
        }
        return result;
    }

    public void setAutoCommit(boolean b) throws SQLException {
        checkConnection();
        _connection.setAutoCommit(b);
    }

    public Statement createStatement() throws SQLException {
        checkConnection();
        return _connection.createStatement();
    }

    public void commit() throws SQLException {
        checkConnection();
        _connection.commit();
    }

    public void rollback() throws SQLException {
        checkConnection();
        _connection.rollback();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        checkConnection();
        return _connection.getMetaData();
    }
}
