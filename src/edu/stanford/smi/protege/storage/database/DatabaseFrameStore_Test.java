package edu.stanford.smi.protege.storage.database;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

public class DatabaseFrameStore_Test extends FrameStore_Test {

    // MySQL
    /*
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://fergerson-li-smi/test";
    private static final String USER = "myuser";
    private static final String PASSWORD = null;
    private static final String TABLE = "test_table";
    */

    // Oracle
    ///*
    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String URL = "jdbc:oracle:thin:@biostorm.stanford.edu:1521:PROTEGE";
    private static final String USER = "rwf";
    private static final String PASSWORD = "ray";
    private static final String TABLE = "test_table";
    //*/

    // Access
    /*
    private static final String DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";
    private static final String URL = "jdbc:odbc:protege-access";
    private static final String USER = "rwf";
    private static final String PASSWORD = null;
    private static final String TABLE = "test_table";
    */

    private static int count = 0;
    protected FrameStore createFrameStore(DefaultKnowledgeBase kb) {
        DatabaseFrameStore fs = new DatabaseFrameStore(kb, DRIVER, URL, USER, PASSWORD, TABLE + count++, true);
        fs.reinitializeTable();
        return fs;
    }
}
