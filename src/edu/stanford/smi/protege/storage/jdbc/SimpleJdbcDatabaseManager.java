package edu.stanford.smi.protege.storage.jdbc;

import java.io.*;
import java.sql.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * An implementation of the database schema interface that implements a very simple database schema. This schema is
 * described in some detail, along with its rationale at http://protege.stanford.edu/doc/design/jdbc_backend.html
 * 
 * This schema is quite convenient for Protege itself to use because it maps directly to the way Protege works.
 * Everything is a slot value or a facet value. The schema is totally useless for someone trying to write reasonable SQL
 * to get information out of their knowledge base. Such a backend could certainly be developed. This is not it though!
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SimpleJdbcDatabaseManager implements DatabaseManager {
    private final static String FRAME = "frame";
    private final static String SLOT = "slot";
    private final static String FACET = "facet";
    private final static String IS_TEMPLATE = "is_template";
    private final static String INDEX = "value_index";
    private final static String VALUE_TYPE = "value_type";
    private final static String SHORT_VALUE = "slot_or_facet_value";
    private final static String LONG_VALUE = "long_slot_or_facet_value";
    private final static int MAX_STRING_LENGTH = 254;

    private boolean _supportsEscape;
    private String _escapeClause;
    private static final char ESCAPE_CHAR = '\\';

    private final static int NULL_FRAME_ID = 0;
    private final static String PARAM_LONGVARCHARNAME = "SimpleJdbcDatabaseManager.longvarcharname";

    private RobustConnection _connection;
    private String _tableName;
    private String _driverName;
    private boolean _isTracing;
    private boolean _needsUpperForLike;

    private boolean _doLongStringsWork;
    private boolean _canDisableAutoCommit;

    /*
     * private int _nFrames; private final static int TRACE_COUNT = 100; private Slot _nameSlot; private Slot
     * _directTypeSlot; private Slot _directSubclassesSlot; private Slot _directInstancesSlot;
     */

    public static class ValueDBIndexPair {
        public String value;
        public int dbIndex;

        public ValueDBIndexPair(String value, int dbIndex) {
            this.value = value;
            this.dbIndex = dbIndex;
        }
    }

    public SimpleJdbcDatabaseManager(String driver, String url, String tablename, String username,
            String password, Collection errors) {
        // Log.enter(this, "JdbcManager", driver, url, tablename, username, password);
        try {
            initializeTracing();
            Assert.assertNotNull("tableName", tablename);
            _tableName = tablename;
            _driverName = driver;

            _connection = new RobustConnection(driver, url, username, password);
            initializeCanDisableAutoCommit();
            initializeLongStringWorkingFlag();
            initializeNeedsUpper();
            initializeEscapeHandling();
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
            errors.add(e);
        }
    }

    /*
     * public void addValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) throws SQLException {
     * if (value == null) { Log.warning("null value ignored", this, "addValue", frame, slot, facet); String text = "null
     * value in SimpleJdbcDatabaseManager.addValue(" + frame + ", " + slot + ", " + facet + ", " + isTemplate + ")";
     * throw new RuntimeException(text); } else { addValue(frame, slot, facet, isTemplate, value, _nextIndex);
     * ++_nextIndex; } }
     */

    public void addValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value)
            throws SQLException {
        throw new UnsupportedOperationException();
    }

    private void addValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value,
            int dbIndex) throws SQLException {
        trace("addValue", frame, slot, facet, value);
        String command = "INSERT INTO " + _tableName;
        command += " (" + FRAME + ", " + SLOT + ", " + FACET + ", " + IS_TEMPLATE + ", ";
        command += INDEX + ", " + VALUE_TYPE + ", " + SHORT_VALUE + ", " + LONG_VALUE + ")";
        command += " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = _connection.prepareStatement(command);
        setFrameParam(stmt, 1, frame);
        setSlotParam(stmt, 2, slot);
        setFacetParam(stmt, 3, facet);
        setIsTemplateParam(stmt, 4, isTemplate);
        setIndexParam(stmt, 5, dbIndex);
        setValueTypeParam(stmt, 6, valueType(value));
        setShortValueParam(stmt, 7, toShortString(value));
        setLongValueParam(stmt, 8, toLongString(value));
        stmt.executeUpdate();
    }

    private void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate,
            Collection values) throws SQLException {
        int index = 0;
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            if (value == null) {
                Log.getLogger().warning("null value ignored");
            } else {
                addValue(frame, slot, facet, isTemplate, value, index);
                ++index;
            }
        }
    }

    /** returns true if transaction started successfully */
    public boolean beginTransaction() {
        boolean result = false;
        if (_canDisableAutoCommit) {
            try {
                _connection.setAutoCommit(false);
                result = true;
            } catch (SQLException e) {
                Log.getLogger().warning("Transactions not supported");
                _canDisableAutoCommit = false;
            }
        }
        return result;
    }

    private Object convertValue(String s, int type) {
        Object value;
        switch (type) {
        case TYPE_BOOLEAN:
            value = new Boolean(s);
            break;
        case TYPE_INTEGER:
            value = new Integer(s);
            break;
        case TYPE_FLOAT:
            value = new Float(s);
            break;
        case TYPE_STRING:
            value = s;
            break;
        case TYPE_CLASS:
        case TYPE_SIMPLE_INSTANCE:
        case TYPE_FACET:
        case TYPE_SLOT:
            int val = Integer.valueOf(s).intValue();
            value = (val < FrameID.INITIAL_USER_FRAME_ID) ? FrameID.createSystem(val) : FrameID
                    .createLocal(val);
            break;
        default:
            Log.getLogger().severe("bad type: " + type);
            value = null;
            break;
        }
        return value;
    }

    public void createIndexes() throws SQLException {
        trace("createIndexes");
        Statement stmt = _connection.createStatement();

        String command;
        command = "CREATE index " + _tableName + "_FSFTIndex ON " + _tableName;
        command += "(" + FRAME + ", " + SLOT + ", " + FACET + ", " + IS_TEMPLATE + ")";
        stmt.executeUpdate(command);

        command = "CREATE index " + _tableName + "_SFTVIndex ON " + _tableName;
        command += "(" + SLOT + ", " + FACET + ", " + IS_TEMPLATE + ", " + SHORT_VALUE + ")";
        stmt.executeUpdate(command);

        command = "CREATE index " + _tableName + "_VIndex ON " + _tableName + "(" + SHORT_VALUE
                + ")";
        stmt.executeUpdate(command);

        if (_needsUpperForLike) {
            ensureUpperIndexExists();
        }

        stmt.close();
    }

    private void ensureUpperIndexExists() {
        Statement stmt = null;
        try {
            stmt = _connection.createStatement();
            String command = "CREATE INDEX " + _tableName + "_I4 ON " + _tableName + "(UPPER("
                    + SHORT_VALUE + "))";
            stmt.executeUpdate(command);
            // Log.trace("created upper index", this, "ensureUpperIndexExists");
        } catch (SQLException e) {
            // Log.trace("create index failed: " + e.getMessage(), this, "ensureUpperIndexExists");
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
    }

    /*
     * private void createNewTable() throws SQLException { if (isPharmGKB() && tableExistsAndIsOK()) { // do
     * non-portable stuff here truncateTable(); } else { dropTable(); createTable(); } }
     */

    public void createTable() throws SQLException {
        trace("createTable");
        String command = "CREATE TABLE " + _tableName + "(" + FRAME + " INTEGER NOT NULL, " + SLOT
                + " INTEGER NOT NULL, " + FACET + " INTEGER NOT NULL, " + IS_TEMPLATE
                + " SMALLINT NOT NULL, " + INDEX + " INTEGER NOT NULL, " + VALUE_TYPE
                + " SMALLINT NOT NULL, " + SHORT_VALUE + " VARCHAR(" + MAX_STRING_LENGTH + "), "
                + LONG_VALUE + " " + getLongvarcharName() + ")";
        // Log.trace(command, this, "createTable");
        Statement stmt = _connection.createStatement();
        try {
            stmt.executeUpdate(command);
        } catch (SQLException e) {
            Log.getLogger().severe(Log.toString(e));
        } finally {
            stmt.close();
        }
    }

    /*
     * private void deleteFrame(Frame frame) throws SQLException { trace("deleteFrame", frame); String command = "DELETE
     * FROM " + _tableName + " WHERE " + FRAME + "= ? "; PreparedStatement stmt = _connection.prepareStatement(command);
     * setFrameParam(stmt, 1, frame); stmt.executeUpdate(); }
     * 
     * private void deleteSlot(Slot slot) throws SQLException { trace("deleteSlot", slot); String command = "DELETE FROM " +
     * _tableName + " WHERE " + SLOT + "= ? "; PreparedStatement stmt = _connection.prepareStatement(command);
     * setSlotParam(stmt, 1, slot); stmt.executeUpdate(); }
     * 
     * private void deleteValue(Frame frame) throws SQLException { trace("deleteValue", frame); String command = "DELETE
     * FROM " + _tableName + " WHERE " + SHORT_VALUE + "= ? AND " + VALUE_TYPE + " = ?"; PreparedStatement stmt =
     * _connection.prepareStatement(command); setShortValueParam(stmt, 1, toShortString(frame)); setValueTypeParam(stmt,
     * 2, valueType(frame)); stmt.executeUpdate(); }
     */

    public void dispose() {
        try {
            _connection.close();
        } catch (Exception e) {
        }
        _connection = null;
    }

    /*
     * private void dropTable() throws SQLException { trace("dropTable"); String text = "DROP TABLE " + _tableName;
     * Statement stmt = _connection.createStatement(); try { stmt.executeUpdate(text); } catch (SQLException e) {
     * Log.trace("failed: " + e.getMessage(), this, "dropTable"); } stmt.close(); }
     */

    /** return true if transation commited successfull */
    public boolean endTransaction(boolean doCommit) {
        boolean result = doCommit;
        if (_canDisableAutoCommit) {
            try {
                if (doCommit) {
                    _connection.commit();
                } else {
                    _connection.rollback();
                }
                _connection.setAutoCommit(true);
                result = doCommit;
            } catch (SQLException e) {
                _canDisableAutoCommit = false;
                Log.getLogger().warning("transaction failed, disabling transactions");
            }
        }
        return result;
    }

    public int getFrameCount(int type) throws SQLException {
        int count = 0;
        // String command = "SELECT COUNT(DISTINCT " + FRAME + ") FROM " + _tableName;
        String command = "SELECT COUNT(" + SHORT_VALUE + ")";
        command += " FROM " + _tableName;
        command += " WHERE " + SLOT + " = " + Model.SlotID.DIRECT_INSTANCES.getLocalPart();
        command += " AND " + FACET + " = 0";
        command += " AND " + IS_TEMPLATE + " = 0";
        if (type != TYPE_INVALID) {
            command += " AND " + VALUE_TYPE + " = " + type;
        }
        PreparedStatement stmt = _connection.prepareStatement(command);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        return count;
    }

    public Collection getAllFrameIDs() throws SQLException {
        trace("getAllFrameIDs");
        Collection frameIDs = new ArrayList();
        String command = "SELECT DISTINCT " + FRAME + " FROM " + _tableName;
        PreparedStatement stmt = _connection.prepareStatement(command);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            FrameID id = getFrameID(rs, 1);
            frameIDs.add(id);
        }
        rs.close();
        return frameIDs;
    }

    private FrameID getFrameID(ResultSet rs, int index) throws SQLException {
        return createFrameID(getInt(rs, index));
    }

    private static FrameID createFrameID(int value) {
        FrameID id;
        if (value == 0) {
            id = null;
        } else if (value < FrameID.INITIAL_USER_FRAME_ID) {
            id = FrameID.createSystem(value);
        } else {
            id = FrameID.createLocal(value);
        }
        return id;
    }

    public Collection getDBReferences(Object o, int maxMatches) throws SQLException {
        trace("getDBReferences", o);
        boolean isMatch = false;
        if (o instanceof String) {
            String a = (String) o;
            a = getStringForMatching(a);
            o = a;
            isMatch = true;
        }

        Collection references = new ArrayList();
        String command = "SELECT " + FRAME + ", " + SLOT + ", " + FACET + ", " + IS_TEMPLATE;
        command += " FROM " + _tableName + " WHERE ";
        command += VALUE_TYPE + " = " + valueType(o) + " AND ";
        if (isMatch && _needsUpperForLike) {
            command += "UPPER(" + SHORT_VALUE + ")";
        } else {
            command += SHORT_VALUE;
        }
        command += " LIKE '" + toShortString(o) + "' " + _escapeClause;
        Statement stmt = _connection.createStatement();
        if (maxMatches > 0) {
            stmt.setMaxRows(maxMatches);
        }
        ResultSet rs = stmt.executeQuery(command);
        while (rs.next()) {
            FrameID frameID = getFrameID(rs, 1);
            FrameID slotID = getFrameID(rs, 2);
            FrameID facetID = getFrameID(rs, 3);
            boolean isTemplate = rs.getBoolean(4);
            references.add(new DBReference(frameID, slotID, facetID, isTemplate));
        }
        rs.close();
        stmt.close();
        return references;
    }

    private static int getInt(ResultSet rs, int index) throws SQLException {
        return rs.getInt(index);
    }

    public String getDriverLongvarcharName() throws SQLException {
        String longvarcharName = null;
        String longvarbinaryName = null;
        String blobName = null;
        DatabaseMetaData md = _connection.getMetaData();
        ResultSet rs = md.getTypeInfo();
        while (rs.next() && longvarcharName == null) {
            String name = rs.getString("TYPE_NAME");
            int type = rs.getInt("DATA_TYPE");
            switch (type) {
            case Types.LONGVARCHAR:
                longvarcharName = name;
                break;
            case Types.LONGVARBINARY:
                if (longvarbinaryName == null) {
                    longvarbinaryName = name;
                }
                break;
            case Types.BLOB:
                if (blobName == null) {
                    blobName = name;
                }
                break;
            default:
            // do nothing
            }
        }
        rs.close();
        if (longvarcharName == null) {
            if (longvarbinaryName == null) {
                longvarcharName = blobName;
            } else {
                longvarcharName = longvarbinaryName;
            }
        }
        // Log.trace("result=" + longvarcharName, this, "getLongvarcharName");
        return longvarcharName;
    }

    public String getDriverName() {
        return _driverName;
    }

    public Collection getFrameIDs(FrameID slot, FrameID facet, boolean isTemplate, String value)
            throws SQLException {
        trace("getFrameIDs", slot, facet, new Boolean(isTemplate), value);

        String command = "SELECT " + FRAME + ", " + SHORT_VALUE + " FROM " + _tableName;
        command += " WHERE " + SLOT + "= ? AND " + FACET + "= ? AND " + IS_TEMPLATE + "= ?";
        command += " AND " + SHORT_VALUE + " = ? AND " + VALUE_TYPE + " = ?";
        PreparedStatement stmt = _connection.prepareStatement(command);
        setFrameIDParam(stmt, 1, slot);
        setFrameIDParam(stmt, 2, facet);
        setIsTemplateParam(stmt, 3, isTemplate);
        setShortValueParam(stmt, 4, toShortString(value));
        setValueTypeParam(stmt, 5, valueType(value));
        Collection frameIDs = new ArrayList();
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String dbValue = getShortString(rs, 2);
            if (value.equals(dbValue)) {
                frameIDs.add(getFrameID(rs, 1));
            }
        }
        rs.close();
        return frameIDs;
    }

    public int getFrameType(FrameID id) throws SQLException {
        trace("getFrameType", id);
        int frameType = TYPE_INVALID;
        String command = "SELECT " + VALUE_TYPE + " FROM " + _tableName;
        command += " WHERE " + SHORT_VALUE + " = ? " + " AND " + VALUE_TYPE + ">="
                + TYPE_SIMPLE_INSTANCE;
        PreparedStatement stmt = _connection.prepareStatement(command);
        setShortValueParam(stmt, 1, toShortString(id));
        stmt.setMaxRows(1);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            frameType = rs.getInt(1);
            break;
        }
        rs.close();
        return frameType;
    }

    /*
     * This method uses a lot of memory for buffering so we take some care to null out the references to large objects
     * on the stack when they are no longer needed.
     */
    private String getLongString(ResultSet rs, int index) throws SQLException {
        StringBuffer buffer = new StringBuffer();
        Reader reader = null;
        try {
            reader = getReader(rs, index);
            if (reader == null) {
                Log.getLogger().warning("null long value");
            } else {
                char[] array = new char[1000000];
                int nRead = 0;
                while (nRead != -1) {
                    nRead = reader.read(array);
                    if (nRead > 0) {
                        buffer.append(array, 0, nRead);
                    }
                }
                array = null;
            }
        } catch (IOException e) {
            Log.getLogger().warning(e.toString());
        } finally {
            FileUtilities.close(reader);
        }
        String s = buffer.toString();
        buffer = null;
        return s.trim();
    }

    public String getLongvarcharName() throws SQLException {
        String name = getParameterLongvarcharName();
        if (name == null || name.length() == 0) {
            name = getDriverLongvarcharName();
        }
        return name;
    }

    private static final char SINGLE_QUOTE = '\'';

    private String getStringForMatching(String input) {
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            c = Character.toUpperCase(c);
            if (_supportsEscape && (c == '%' || c == '_' || c == ESCAPE_CHAR)) {
                output.append(ESCAPE_CHAR);
                output.append(c);
            } else if (c == SINGLE_QUOTE) {
                output.append(SINGLE_QUOTE);
                output.append(c);
            } else if (c == '*') {
                output.append('%');
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }

    public Collection getFramesWithValue(Slot slot, Facet facet, boolean isTemplate, Object value)
            throws SQLException {
        trace("getFramesWithValue", slot, facet);
        Collection ids = new ArrayList();
        String command = "SELECT " + FRAME + " FROM " + _tableName + " WHERE " + SLOT + "= ? "
                + " AND " + FACET + " = ? " + " AND " + IS_TEMPLATE + "= ?" + " AND " + VALUE_TYPE
                + "= ?" + " AND " + SHORT_VALUE + "= ?";
        PreparedStatement stmt = _connection.prepareStatement(command);
        setSlotParam(stmt, 1, slot);
        setFacetParam(stmt, 2, facet);
        setIsTemplateParam(stmt, 3, isTemplate);
        setValueTypeParam(stmt, 4, valueType(value));
        setShortValueParam(stmt, 5, toShortString(value));
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            FrameID id = getFrameID(rs, 1);
            ids.add(id);
        }
        rs.close();
        return ids;
    }

    public Collection getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate)
            throws SQLException {
        trace("getFramesWithValue", slot, facet);
        Collection ids = new ArrayList();
        String command = "SELECT " + FRAME + " FROM " + _tableName + " WHERE " + SLOT + "= ? "
                + " AND " + FACET + " = ? " + " AND " + IS_TEMPLATE + "= ?";
        PreparedStatement stmt = _connection.prepareStatement(command);
        setSlotParam(stmt, 1, slot);
        setFacetParam(stmt, 2, facet);
        setIsTemplateParam(stmt, 3, isTemplate);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            FrameID id = getFrameID(rs, 1);
            ids.add(id);
        }
        rs.close();
        return ids;
    }

    public Collection getMatchingFrameIDs(FrameID slot, FrameID facet, boolean isTemplate,
            String value, int maxMatches) throws SQLException {

        Assert.assertNotNull("slot", slot);
        trace("getMatchingFrameIDs", slot, facet, new Boolean(isTemplate), value);

        value = getStringForMatching(value);

        List frameIDs = new ArrayList();
        String command = "SELECT " + FRAME + " FROM " + _tableName;
        command += " WHERE " + SLOT + "= " + getFrameIdValue(slot);
        command += " AND " + FACET + " = " + getFrameIdValue(facet);
        command += " AND " + IS_TEMPLATE + " = " + getIsTemplateValue(isTemplate);
        command += " AND " + VALUE_TYPE + " = " + valueType(value);
        command += " AND ";
        if (_needsUpperForLike) {
            command += "UPPER(" + SHORT_VALUE + ")";
        } else {
            command += SHORT_VALUE;
        }
        command += " LIKE '" + value + "' " + _escapeClause;

        Statement stmt = _connection.createStatement();
        if (maxMatches > 0) {
            stmt.setMaxRows(maxMatches);
        }
        // Log.trace("command=" + command, this, "getMatchingFrameIDs");
        ResultSet rs = stmt.executeQuery(command);
        while (rs.next()) {
            FrameID id = getFrameID(rs, 1);
            frameIDs.add(id);
        }
        rs.close();
        stmt.close();
        // make sure we return any exact matches
        if (frameIDs.size() == maxMatches) {
            frameIDs.addAll(getFrameIDs(slot, facet, isTemplate, value));
        }
        // Log.trace("found " + frameIDs.size() + " matches", this, "getMatchingFrameIDs");
        return frameIDs;
    }

    public String getParameterLongvarcharName() {
        return SystemUtilities.getSystemProperty(PARAM_LONGVARCHARNAME);
    }

    private Reader getReader(ResultSet rs, int index) throws SQLException {
        Reader reader = null;
        try {
            // try the JDBC 2.0 character call
            reader = rs.getCharacterStream(index);
        } catch (Exception e) {
            // try the JDBC 1.0 ascii call
            InputStream is = rs.getAsciiStream(index);
            reader = new InputStreamReader(is);
        }
        return reader;
    }

    private String getShortString(ResultSet rs, int index) throws SQLException {
        String s = rs.getString(index);
        return (s == null) ? (String) null : s.trim();
    }

    public String getTableName() {
        return _tableName;
    }

    /*
     * private Collection getUserInstances(Collection instances) { Collection userInstances; if (instances.isEmpty()) {
     * userInstances = instances; } else { userInstances = new ArrayList(); Iterator i = instances.iterator(); while
     * (i.hasNext()) { Instance instance = (Instance) i.next(); if (!instance.isSystem()) { userInstances.add(instance); } } }
     * return userInstances; }
     */

    public int getValueCount(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate)
            throws SQLException {
        trace("getValueCount", frame, slot, facet);
        String command = "SELECT COUNT(*) FROM " + _tableName;
        command += " WHERE " + FRAME + "= ? AND " + SLOT + "= ? AND " + IS_TEMPLATE + "=? AND "
                + FACET + "= ?";
        PreparedStatement stmt = _connection.prepareStatement(command);
        setFrameIDParam(stmt, 1, frame);
        setFrameIDParam(stmt, 2, slot);
        setIsTemplateParam(stmt, 3, isTemplate);
        setFrameIDParam(stmt, 4, facet);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        return count;
    }

    private static final int MAX_CACHE_SIZE = 1000;
    private Map _frameToDBReferenceToValuesMap = new LinkedHashMap() {
        protected boolean removeEldestEntry(Map.Entry entry) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    public List getValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate)
            throws SQLException {
        trace("getValues", frame, slot, facet);
        Map dbReferenceToValuesMap = (Map) _frameToDBReferenceToValuesMap.get(frame);
        if (dbReferenceToValuesMap == null) {
            dbReferenceToValuesMap = loadCache(frame);
            _frameToDBReferenceToValuesMap.put(frame, dbReferenceToValuesMap);
        } else {
            // Log.trace("Cache hit!", this, "getValues");
        }
        List values = (List) dbReferenceToValuesMap.get(new DBReference(frame, slot, facet,
                isTemplate));
        if (values == null) {
            values = new ArrayList();
        }
        return values;
    }

    private Map loadCache(FrameID frame) throws SQLException {
        Map map = new HashMap();

        String command = "SELECT " + SLOT + ", " + FACET + ", " + IS_TEMPLATE + ", ";
        command += SHORT_VALUE + ", " + LONG_VALUE + ", " + INDEX + ", " + VALUE_TYPE;
        command += " FROM " + _tableName;
        command += " WHERE " + FRAME + "= ?";
        command += " ORDER BY " + INDEX;
        PreparedStatement stmt = _connection.prepareStatement(command);
        setFrameIDParam(stmt, 1, frame);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            FrameID slot = getFrameID(rs, 1);
            if (slot != null) {
                FrameID facet = getFrameID(rs, 2);
                boolean isTemplate = rs.getBoolean(3);
                String s = getShortString(rs, 4);
                if (s == null) {
                    s = getLongString(rs, 5);
                }
                int type = rs.getInt(7);
                Object value = convertValue(s, type);
                if (value == null) {
                    Log.getLogger().severe("encountered null value: " + frame);
                } else {
                    cacheInsert(map, frame, slot, facet, isTemplate, value);
                }
            }
        }
        rs.close();
        return map;
    }

    private void cacheInsert(Map map, FrameID frame, FrameID slot, FrameID facet,
            boolean isTemplate, Object value) {
        DBReference ref = new DBReference(frame, slot, facet, isTemplate);
        List list = (List) map.get(ref);
        if (list == null) {
            list = new ArrayList();
            map.put(ref, list);
        }
        list.add(value);
    }

    /*
     * public List getValues(FrameID frame, FrameID slot, FrameID facet, boolean isTemplate) throws SQLException {
     * trace("getValues", frame, slot, facet); List values = new ArrayList(); String command = "SELECT " + SHORT_VALUE + ", " +
     * LONG_VALUE + ", " + INDEX + ", " + VALUE_TYPE; command += " FROM " + _tableName; command += " WHERE " + FRAME + "= ?
     * AND " + SLOT + "= ? AND " + IS_TEMPLATE + "= ? AND " + FACET + "= ? "; command += "ORDER BY " + INDEX;
     * PreparedStatement stmt = _connection.prepareStatement(command); setFrameIDParam(stmt, 1, frame);
     * setFrameIDParam(stmt, 2, slot); setIsTemplateParam(stmt, 3, isTemplate); setFrameIDParam(stmt, 4, facet);
     * ResultSet rs = stmt.executeQuery(); while (rs.next()) { String s = getShortString(rs, 1); if (s == null) { s =
     * getLongString(rs, 2); } int type = rs.getInt(4); Object value = convertValue(s, type); if (value == null) {
     * Log.error("encountered null value", this, "getValues", frame, slot, facet); } else { values.add(value); } }
     * rs.close(); return values; }
     */

    public boolean hasValueAtSomeFrame(Slot slot, Facet facet, boolean isTemplate)
            throws SQLException {
        trace("hasValueAtSomeFrame", slot, facet);
        boolean result = false;
        String command = "SELECT " + FRAME + " FROM " + _tableName;
        command += " WHERE " + SLOT + " = ? AND " + FACET + " = ? AND " + IS_TEMPLATE + " = ? ";
        PreparedStatement stmt = _connection.prepareStatement(command);
        setSlotParam(stmt, 1, slot);
        setFacetParam(stmt, 2, facet);
        setIsTemplateParam(stmt, 3, isTemplate);
        stmt.setMaxRows(1);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            result = true;
            break;
        }
        rs.close();
        return result;
    }

    private void initializeLongStringWorkingFlag() throws SQLException {
        _doLongStringsWork = !isMsAccess();
    }

    private boolean isMySql() throws SQLException {
        return _connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("mysql");
    }

    private boolean isMsAccess() throws SQLException {
        return _connection.getMetaData().getDatabaseProductName().equalsIgnoreCase("access");
    }

    private void initializeCanDisableAutoCommit() throws SQLException {
        _canDisableAutoCommit = !(isMsAccess());
    }

    private void initializeEscapeHandling() throws SQLException {
        _supportsEscape = _connection.getMetaData().supportsLikeEscapeClause();

        // The MySQL driver (version 2.x) claims to support the "like" escape clause but it doesn't. It does
        // support escaping with the \ character though.
        if (_supportsEscape && !isMySql()) {
            _escapeClause = "{escape '" + ESCAPE_CHAR + "'}";
        } else {
            _escapeClause = "";
        }
    }

    private void initializeNeedsUpper() throws SQLException {
        DatabaseMetaData md = _connection.getMetaData();
        String databaseProductName = md.getDatabaseProductName();
        if (databaseProductName.equalsIgnoreCase("oracle")) {
            _needsUpperForLike = true;
            ensureUpperIndexExists();
        } else {
            _needsUpperForLike = false;
        }
    }

    private void initializeTracing() {
        _isTracing = Boolean.getBoolean("SimpleJdbcDatabaseManager.trace");
    }

    /*
     * private boolean isPharmGKB() { return _url.indexOf("PHARM") != -1; }
     */

    private boolean longStringsWork() {
        return _doLongStringsWork;
    }

    /*
     * public void removeAllReferences(Frame frame) throws SQLException { // The MySQL sql driver (at least) doesn't
     * optimize to use indexes in the presence of OR so we issue // separate delete calls to speed things up.
     * deleteFrame(frame); deleteValue(frame); if (frame instanceof Slot) { deleteSlot((Slot) frame); } else if (frame
     * instanceof Facet) { // deleteFacet((Facet) frame); } }
     * 
     * public void removeAllValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) throws SQLException {
     * trace("removeAllValues", frame, slot, facet); String command = "DELETE FROM " + _tableName; command += " WHERE " +
     * FRAME + "= ? AND " + SLOT + "= ? AND " + FACET + "= ?" + " AND " + IS_TEMPLATE + "= ?"; PreparedStatement stmt =
     * _connection.prepareStatement(command); setFrameParam(stmt, 1, frame); setSlotParam(stmt, 2, slot);
     * setFacetParam(stmt, 3, facet); setIsTemplateParam(stmt, 4, isTemplate); stmt.executeUpdate(); }
     * 
     * public void removeFramesWithValue(Slot slot, Facet facet, boolean isTemplate) throws SQLException { //
     * Log.enter(this, "removeFramesWithValue", slot, facet, new Boolean(isTemplate)); trace("removeFramesWithValue",
     * slot, facet); String command = "DELETE FROM " + _tableName + " WHERE " + SLOT + "= ? AND " + FACET + "= ? AND " +
     * IS_TEMPLATE + "= ?"; PreparedStatement stmt = _connection.prepareStatement(command); setSlotParam(stmt, 1, slot);
     * setFacetParam(stmt, 2, facet); setIsTemplateParam(stmt, 3, isTemplate); stmt.executeUpdate(); }
     * 
     * public void removeSingleValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) throws
     * SQLException { trace("removeSingleValue", frame, slot, facet, value); String command = "DELETE FROM " +
     * _tableName; command += " WHERE " + FRAME + "= ? AND " + SLOT + "= ? AND " + FACET + "= ?"; command += " AND " +
     * IS_TEMPLATE + "= ? AND " + SHORT_VALUE + "= ?"; PreparedStatement stmt = _connection.prepareStatement(command);
     * setFrameParam(stmt, 1, frame); setSlotParam(stmt, 2, slot); setFacetParam(stmt, 3, facet);
     * setIsTemplateParam(stmt, 4, isTemplate); setShortValueParam(stmt, 5, toShortString(value)); stmt.executeUpdate(); }
     */
    public void removeAllReferences(Frame frame) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void removeAllValues(Frame frame, Slot slot, Facet facet, boolean isTemplate)
            throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void removeFramesWithValue(Slot slot, Facet facet, boolean isTemplate)
            throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void removeSingleValue(Frame frame, Slot slot, Facet facet, boolean isTemplate,
            Object value) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /*
     * public void removeValues(FrameID slot) throws SQLException { trace("removeValues", slot); String command =
     * "DELETE FROM " + _tableName + " WHERE " + SLOT + "= ? AND " + FACET + " = ?"; PreparedStatement stmt =
     * _connection.prepareStatement(command); setFrameIDParam(stmt, 1, slot); stmt.executeUpdate(); }
     */

    public void saveKnowledgeBase(KnowledgeBase kb) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /*
     * public void saveKnowledgeBase(KnowledgeBase kb) throws SQLException { long t1 = System.currentTimeMillis(); //
     * Log.enter(this, "saveKnowledgeBase", kb); _nameSlot = kb.getSlot(Model.Slot.NAME); _directTypeSlot =
     * kb.getSlot(Model.Slot.DIRECT_TYPES); _directSubclassesSlot = kb.getSlot(Model.Slot.DIRECT_SUBCLASSES);
     * _directInstancesSlot = kb.getSlot(Model.Slot.DIRECT_INSTANCES);
     * 
     * createNewTable(); Iterator i = kb.getFrames().iterator(); while (i.hasNext()) { Frame frame = (Frame) i.next();
     * if (++_nFrames % TRACE_COUNT == 0) { Log.trace("n=" + _nFrames + ", frame=" + frame, this, "saveKnowledgeBase"); }
     * if (frame.isSystem()) { saveSystemFrame(frame); } else { saveStandardFrame(frame); } } try { createIndexes(); }
     * catch (SQLException e) { // fails if table already exists if (!isPharmGKB()) { throw e; } } long t2 =
     * System.currentTimeMillis(); Log.trace("save complete, time=" + (t2 - t1) / 1000, this, "saveKnowledgeBase"); }
     * 
     * private void saveOwnSlot(Frame frame, Slot slot) throws SQLException { Collection values =
     * frame.getOwnSlotValues(slot); addValues(frame, slot, null, false, values); }
     * 
     * private void saveOwnSlots(Frame frame) throws SQLException { Iterator i = frame.getOwnSlots().iterator(); while
     * (i.hasNext()) { Slot slot = (Slot) i.next(); saveOwnSlot(frame, slot); } }
     * 
     * private void saveStandardFrame(Frame frame) throws SQLException { saveOwnSlots(frame); if (frame instanceof Cls) {
     * saveTemplateSlots((Cls) frame); } }
     * 
     * private void saveSystemFrame(Frame frame) throws SQLException { if (frame instanceof Cls) { Cls cls = (Cls)
     * frame; Collection userSubclasses = getUserInstances(cls.getDirectSubclasses()); addValues(frame,
     * _directSubclassesSlot, null, false, userSubclasses);
     * 
     * Collection userInstances = getUserInstances(cls.getDirectInstances()); addValues(frame, _directInstancesSlot,
     * null, false, userInstances); } else if (frame instanceof Slot) { /* Slot slot = (Slot) frame; Collection subSlots =
     * slot.getDirectSubslots(); addValues(frame, _directSubslotsSlot, null, false, subslots);
     *//*
        * } }
        * 
        * private void saveTemplateSlotFacets(Cls cls, Slot slot) throws SQLException { Iterator i =
        * cls.getTemplateFacets(slot).iterator(); while (i.hasNext()) { Facet facet = (Facet) i.next(); Collection
        * values = cls.getDirectTemplateFacetValues(slot, facet); if (values != null) { addValues(cls, slot, facet,
        * true, values); } } }
        * 
        * private void saveTemplateSlots(Cls cls) throws SQLException { Iterator i = cls.getTemplateSlots().iterator();
        * while (i.hasNext()) { Slot slot = (Slot) i.next(); Collection values = cls.getDirectTemplateSlotValues(slot);
        * if (values != null) { addValues(cls, slot, null, true, values); } saveTemplateSlotFacets(cls, slot); } }
        */

    private static void setFacetParam(PreparedStatement stmt, int index, Facet facet)
            throws SQLException {
        setFrameIDParam(stmt, index, (facet == null) ? (FrameID) null : facet.getFrameID());
    }

    private static int getFrameIdValue(FrameID id) {
        return (id == null) ? NULL_FRAME_ID : toInt(id);
    }

    private static int toInt(FrameID id) {
        return id.getLocalPart();
    }

    private static void setFrameIDParam(PreparedStatement stmt, int index, FrameID id)
            throws SQLException {
        stmt.setInt(index, getFrameIdValue(id));
    }

    private static void setFrameParam(PreparedStatement stmt, int index, Frame frame)
            throws SQLException {
        setFrameIDParam(stmt, index, frame.getFrameID());
    }

    private static void setIndexParam(PreparedStatement stmt, int index, int value)
            throws SQLException {
        stmt.setInt(index, value);
    }

    private static void setIsTemplateParam(PreparedStatement stmt, int index, boolean value)
            throws SQLException {
        stmt.setInt(index, getIsTemplateValue(value));
    }

    private static int getIsTemplateValue(boolean value) {
        return value ? 1 : 0;
    }

    private static boolean setLongValueParam(PreparedStatement stmt, int index, String s)
            throws SQLException {
        boolean isNull = true;
        if (s == null) {
            stmt.setNull(index, Types.LONGVARCHAR);
        } else if (s.length() < MAX_STRING_LENGTH) {
            Log.getLogger().warning("Short string in long position!");
        } else {
            stmt.setCharacterStream(index, new java.io.StringReader(s), s.length());
            isNull = false;
        }
        return isNull;
    }

    private static boolean setShortValueParam(PreparedStatement stmt, int index, String s)
            throws SQLException {
        boolean isNull;
        if (s == null) {
            stmt.setNull(index, Types.VARCHAR);
            isNull = true;
        } else if (s.length() == 0) {
            stmt.setString(index, " ");
            isNull = false;
        } else {
            stmt.setString(index, s);
            isNull = false;
        }
        return isNull;
    }

    private static void setSlotParam(PreparedStatement stmt, int index, Slot slot)
            throws SQLException {
        setFrameIDParam(stmt, index, slot.getFrameID());
    }

    public void setTracing(boolean b) {
        Log.getLogger().info("Set tracing: " + b);
        _isTracing = b;
    }

    public void setValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value)
            throws SQLException {
        setValues(frame, slot, facet, isTemplate, CollectionUtilities.createCollection(value));
    }

    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values)
            throws SQLException {
        removeAllValues(frame, slot, facet, isTemplate);
        addValues(frame, slot, facet, isTemplate, values);
    }

    private static void setValueTypeParam(PreparedStatement stmt, int index, int type)
            throws SQLException {
        stmt.setInt(index, type);
    }

    public boolean supportsTransactions() {
        return _canDisableAutoCommit;
    }

    /*
     * This method should check all of the columns, types, etc Unfortunately the metadata access calls don't always work
     * so we to a quick and dirty test. If the table exists and has one of our more bizarre column names then we will
     * assume that it is ok.
     */
    /*
     * private boolean tableExistsAndIsOK() throws SQLException { trace("tableExistsAndIsOK"); boolean result; Statement
     * stmt = _connection.createStatement(); try { stmt.setMaxRows(1); String command = "SELECT " + LONG_VALUE + " FROM " +
     * _tableName; ResultSet rs = stmt.executeQuery(command); rs.close(); result = true; } catch (SQLException e) {
     * result = false; } stmt.close(); return result; }
     */

    private String toLongString(Object o) {
        String longString;
        if (o instanceof String) {
            String s = (String) o;
            if (s.length() < MAX_STRING_LENGTH || !longStringsWork()) {
                longString = null;
            } else {
                longString = s;
            }
        } else {
            longString = null;
        }
        return longString;
    }

    private String toShortString(Object o) {
        String shortString;
        if (o == null) {
            shortString = null;
        } else if (o instanceof Frame) {
            shortString = String.valueOf(((Frame) o).getFrameID().getLocalPart());
        } else if (o instanceof String) {
            String s = (String) o;
            if (s.length() < MAX_STRING_LENGTH) {
                shortString = s;
            } else if (longStringsWork()) {
                shortString = null;
            } else {
                shortString = s.substring(0, MAX_STRING_LENGTH - 1);
                String stringStart = s.substring(0, 10) + "...";
                String message = "truncated from " + s.length() + " to " + MAX_STRING_LENGTH
                        + " characters: ";
                Log.getLogger().warning(message + stringStart);
            }
        } else if (o instanceof FrameID) {
            shortString = String.valueOf(((FrameID) o).getLocalPart());
        } else {
            shortString = o.toString();
        }
        return shortString;
    }

    public String toString() {
        return "SimpleJdbcDatabaseManager";
    }

    private void trace(String method) {
        trace(method, 0, null, null, null, null, null);
    }

    private void trace(String method, int nGoodArgs, Object arg1, Object arg2, Object arg3,
            Object arg4, Object arg5) {
        if (_isTracing) {
            StringBuffer buffer = new StringBuffer("dbtrace: ");
            buffer.append(method);
            buffer.append('(');
            Object[] args = new Object[] { arg1, arg2, arg3, arg4, arg5 };
            for (int i = 0; i < nGoodArgs; ++i) {
                if (i != 0) {
                    buffer.append(", ");
                }
                buffer.append(args[i]);
            }
            buffer.append(')');
            Log.getLogger().finest(buffer.toString());
        }
    }

    private void trace(String method, Object arg1) {
        trace(method, 1, arg1, null, null, null, null);
    }

    private void trace(String method, Object arg1, Object arg2) {
        trace(method, 2, arg1, arg2, null, null, null);
    }

    private void trace(String method, Object arg1, Object arg2, Object arg3) {
        trace(method, 3, arg1, arg2, arg3, null, null);
    }

    private void trace(String method, Object arg1, Object arg2, Object arg3, Object arg4) {
        trace(method, 4, arg1, arg2, arg3, arg4, null);
    }

    private static int valueType(Object o) {
        int type;
        if (o instanceof String) {
            type = TYPE_STRING;
        } else if (o instanceof Cls) {
            type = TYPE_CLASS;
        } else if (o instanceof Slot) {
            type = TYPE_SLOT;
        } else if (o instanceof Facet) {
            type = TYPE_FACET;
        } else if (o instanceof Instance) {
            type = TYPE_SIMPLE_INSTANCE;
        } else if (o instanceof Integer) {
            type = TYPE_INTEGER;
        } else if (o instanceof Boolean) {
            type = TYPE_BOOLEAN;
        } else if (o instanceof Float) {
            type = TYPE_FLOAT;
        } else {
            Assert.fail("unknown object: " + o);
            type = -1;
        }
        return type;
    }
}