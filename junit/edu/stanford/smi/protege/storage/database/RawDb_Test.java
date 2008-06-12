package edu.stanford.smi.protege.storage.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.logging.Level;

import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.util.Log;

public class RawDb_Test extends APITestCase {
    private Random r = new Random();
    
    private RobustConnection createRobustConnection() throws SQLException {
        return new RobustConnection(getDBProperty(JUNIT_DB_DRIVER_PROPERTY),
                                    getDBProperty(JUNIT_DB_URL_PROPERTY),
                                    getDBProperty(JUNIT_DB_USER_PROPERTY),
                                    getDBProperty(JUNIT_DB_PASSWORD_PROPERTY),
                                    null, null);
    }
    
    public void testCaseSensitivity() {
        try {
            for (DBType dbt : DBType.values()) {
                setDBType(dbt);
                if (!dbConfigured()) {
                    continue;
                }
                RobustConnection connection = createRobustConnection();
                Statement stmt = connection.getStatement();
                try {
                    connection.getStatement().executeUpdate("DROP TABLE test");
                }
                catch (SQLException sqle) {
                    ;
                }

                createTestTable(connection, stmt);
                stmt.executeUpdate("INSERT INTO test VALUES ('aa')");
                stmt.executeUpdate("INSERT INTO test VALUES ('aA')");
                stmt.executeUpdate("INSERT INTO test VALUES ('Aa')");
                stmt.executeUpdate("INSERT INTO test VALUES ('AA')");
                ResultSet  rs = stmt.executeQuery("SELECT str FROM test WHERE  str = 'aa'");
                boolean  found = false;
                while (rs.next()) {
                    if (rs.getString(1).equals("aa")) {
                        found = true;
                    }
                    else {
                        fail("database column " + connection.getVarcharTypeName() + " is not case sensitive.");
                    }
                }
                if (!found) {
                    fail("database query failed!");
                }
                stmt.close();
            }
        }
        catch (Throwable t) {
            Log.getLogger().log(Level.WARNING, "Exception caught", t);
            fail(t.getMessage());
        }
    }
    
    public void testMaxCharSize() {
        try {
            for (DBType dbt : DBType.values()) {
                setDBType(dbt);
                if (!dbConfigured()) {
                    continue;
                }
                RobustConnection connection = createRobustConnection();
                Statement stmt = connection.getStatement();
                try {
                    stmt.executeUpdate("DROP TABLE test");
                }
                catch (SQLException sqle) {
                    ;
                }
                createTestTable(connection, stmt);
                int maxSize = connection.getMaxVarcharSize();
                String s1 = "hello";
                stmt.executeUpdate("INSERT INTO test VALUES ('" + s1 + "')");
                String s2 = createRandomString(r.nextInt(maxSize - 1) + 1);
                stmt.executeUpdate("INSERT INTO test VALUES ('" + s2 + "')");
                String s3 = createRandomString(maxSize);
                stmt.executeUpdate("INSERT INTO test VALUES ('" + s3 + "')");
                boolean found1 = false, found2 = false, found3 = false;
                ResultSet rs = stmt.executeQuery("select * from test");
                while (rs.next()) {
                    String entry = rs.getString("str");
                    if (entry.equals(s1)) {
                        found1 = true;
                    }
                    else if (entry.equals(s2)) {
                        found2 = true;
                    }
                    else if (entry.equals(s3)) {
                        found3 = true;
                    }
                }
                assertTrue(found1 && found2 && found3);
            }
        }
        catch (Throwable t) {
            Log.getLogger().log(Level.WARNING, "Exception caught", t);
            fail(t.getMessage());
        }
    }
    
    private void createTestTable(RobustConnection connection, Statement stmt) throws SQLException {
        String cmd = "CREATE TABLE test ( str " + connection.getVarcharTypeName() + ")";
        if (connection.isMySql() && (connection.getDatabaseMajorVersion() == 5)) {
            cmd = cmd + " ENGINE = INNODB";
        }
        stmt.executeUpdate(cmd);
    }
    
    private String createRandomString(int size) {
        char[] a = new char[size];
        for (int i = 0 ; i < size; i++) {
            a[i] = 'a';
        }
        return new String(a);
    }
}
