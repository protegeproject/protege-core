package edu.stanford.smi.protege.storage.jdbc;

import java.sql.*;
import java.util.*;

/**
 * Cache for prepared statements.  Among other things this allows for all statements to be closed cleanly.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class PreparedStatementHolder {
    private Connection _connection;
    private Map _statements = new HashMap();

    public PreparedStatementHolder(Connection c) {
        _connection = c;
    }

    public void closeAll() throws SQLException {
        // Log.trace("Closing " + _statements.size() + " statements", this, "closeAll");
        Iterator i = _statements.values().iterator();
        while (i.hasNext()) {
            PreparedStatement stmt = (PreparedStatement) i.next();
            stmt.close();
        }
        _statements.clear();
    }

    public PreparedStatement get(String stmtText) throws SQLException {
        PreparedStatement result = (PreparedStatement) _statements.get(stmtText);
        if (result == null) {
            result = _connection.prepareStatement(stmtText);
            _statements.put(stmtText, result);
        } else {
            /* This would be a nice check to ensure that everything is set correctly.
             * Unfortunately the stupid jdbc:odbc bridge throws a NullPointerException
             * when this method is called if the statement has no parameters (JDK 1.3)
             */
            // result.clearParameters();
        }
        return result;
    }
}
