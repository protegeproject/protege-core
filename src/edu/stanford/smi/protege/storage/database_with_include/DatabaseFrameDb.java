package edu.stanford.smi.protege.storage.database_with_include;

import java.sql.*;
import java.util.*;
import java.util.Date;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.model.query.*;
import edu.stanford.smi.protege.util.*;

public class DatabaseFrameDb implements NarrowFrameStore {
    private static final String FRAME = "frame";
    private static final String FRAME_PROJECT = "frame_project";
    private static final String FRAME_TYPE = "frame_type";
    private static final String SLOT = "slot";
    private static final String SLOT_PROJECT = "slot_project";
    private static final String FACET = "facet";
    private static final String FACET_PROJECT = "facet_project";
    private static final String IS_TEMPLATE = "is_template";
    private static final String VALUE_INDEX = "value_index";
    private static final String VALUE_TYPE = "value_type";
    private static final String SHORT_VALUE = "short_value";
    private static final String LONG_VALUE = "long_value";

    private final RobustConnection _connection;
    private final String _table;
    private FrameFactory _frameFactory;
    private FrameIDHelper _frameIDHelper;
    private static boolean _isTracingUpdate;
    private static boolean _isTracingQuery;
    private static boolean _isModifiable = true;

    private String frameDbName;

    public String getName() {
        return frameDbName;
    }

    public void setName(String name) {
        frameDbName = name;
    }

    public void close() {
        _frameFactory = null;
        try {
            _connection.close();
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    public DatabaseFrameDb(FrameFactory factory, FrameIDHelper helper, String driver, String url,
            String user, String pass, String table) {
        _table = table;
        _frameFactory = factory;
        _frameIDHelper = helper;
        initializeTracing();
        try {
            _connection = new RobustConnection(driver, url, user, pass);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private void initializeTracing() {
        _isTracingUpdate = ApplicationProperties.getBooleanProperty("database.tracing.update",
                false);
        _isTracingQuery = ApplicationProperties.getBooleanProperty("database.tracing.query", false);
    }

    public void setModifiable(boolean modifiable) {
        _isModifiable = modifiable;
    }

    public String getTableName() {
        return _table;
    }

    private RuntimeException createRuntimeException(SQLException e) {
        try {
            if (_connection != null) {
                _connection.checkConnection();
            }
        } catch (SQLException ex) {
            // do nothing
        }
        return new RuntimeException(e.getMessage());
    }

    /*
     * Using the database metadata to decide if a table exists turns out to be
     * unreliable. Instead we just do a select of something fast and make sure
     * that the table isn't completely empty. If this succeeds the table is
     * assumed to be ok.
     */

    public boolean tableExists() {
        boolean exists = false;
        String command = "SELECT " + FRAME + ", " + FRAME_TYPE + " FROM " + _table;
        command += " WHERE " + SLOT + " = " + Model.SlotID.NAME.getLocalPart();
        command += " AND " + SHORT_VALUE + " = '" + Model.Cls.THING + "'";
        try {
            ResultSet rs = executeQuery(command);
            while (rs.next()) {
                exists = true;
                break;
            }
            rs.close();
        } catch (SQLException e) {
        }
        return exists;
    }

    public void createNewTableAndIndices() {
        try {
            ensureEmptyTableExists();
            createIndices();
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private void ensureEmptyTableExists() throws SQLException {
        dropTableIfItExists();
        createTable();
    }

    private void dropTableIfItExists() {
        try {
            _connection.closeStatements();
            String command = "DROP TABLE " + _table;
            executeUpdate(command);
        } catch (Exception e) {
            // do nothing
        }
    }

    private void createTable() throws SQLException {
        String createTableString = "CREATE TABLE " + _table + " (";
        createTableString += FRAME_PROJECT + " " + getProjectDataType() + " NOT NULL, ";
        createTableString += FRAME + " " + getFrameDataType() + " NOT NULL, ";
        createTableString += FRAME_TYPE + " " + getFrameTypeDataType() + " NOT NULL, ";
        createTableString += SLOT_PROJECT + " " + getProjectDataType() + " NOT NULL, ";
        createTableString += SLOT + " " + getFrameDataType() + " NOT NULL, ";
        createTableString += FACET_PROJECT + " " + getProjectDataType() + " NOT NULL, ";
        createTableString += FACET + " " + getFrameDataType() + " NOT NULL, ";
        createTableString += IS_TEMPLATE + " " + getIsTemplateDataType() + " NOT NULL, ";
        createTableString += VALUE_INDEX + " " + getValueIndexDataType() + " NOT NULL, ";
        createTableString += VALUE_TYPE + " " + getFrameTypeDataType() + " NOT NULL, ";
        createTableString += SHORT_VALUE + " " + getShortValueDataType() + ", ";
        createTableString += LONG_VALUE + " " + getLongValueDataType();
        createTableString += ")";
        try {
            executeUpdate(createTableString);
        } catch (SQLException e) {
            Log.getLogger().warning("Failed to create table: " + createTableString);
            throw e;
        }
    }

    private String getFrameDataType() {
        return _connection.getIntegerTypeName();
    }

    private String getProjectDataType() {
        return _connection.getIntegerTypeName();
    }

    private String getFrameTypeDataType() {
        return _connection.getTinyIntTypeName();
    }

    private String getIsTemplateDataType() {
        return _connection.getBitTypeName();
    }

    private String getValueIndexDataType() {
        return _connection.getIntegerTypeName();
    }

    private String getShortValueDataType() {
        return _connection.getVarcharTypeName() + "(" + _connection.getMaxVarcharSize() + ")";
    }

    private String getLongValueDataType() {
        return _connection.getLongvarcharTypeName();
    }

    private void createIndices() throws SQLException {
        String indexString;

        /*
         * VALUE_INDEX is included in this index solely for its value as a side
         * effect. It keeps the values ordered by the VALUE_INDEX so that the
         * ORDER_BY clause in the getValues SELECT statement does not cost
         * anything. Otherwise this clause has a huge performance impact. We are
         * essentially trading disk space and INSERT time for a SELECT
         * optimization.
         */
        // used for slot and facet value lookup
        indexString = "CREATE INDEX " + _table + "_I1 ON " + _table;
        indexString += " (" + FRAME + ", " + SLOT + ", " + FACET + ", " + IS_TEMPLATE + ", "
                + VALUE_INDEX + ")";
        executeUpdate(indexString);

        // used for searching for values
        indexString = "CREATE INDEX " + _table + "_I2 ON " + _table;
        indexString += " (" + SHORT_VALUE + ")";
        executeUpdate(indexString);

        // used for getting slots with any value and for counting frames
        indexString = "CREATE INDEX " + _table + "_I3 ON " + _table;
        indexString += " (" + SLOT + ", " + FRAME_TYPE + ")";
        executeUpdate(indexString);

        if (needsIndexOnLowerValue()) {
            createIndexOnLowerValue();
        }
    }

    private boolean needsIndexOnLowerValue() throws SQLException {
        return !_connection.supportsCaseInsensitiveMatches()
                && _connection.supportsIndexOnFunction();
    }

    /*
     * Some databases (e.g. Oracle) do case sensitive string compares on a LIKE
     * but we want case insensitive matches. Thus for these databases we lower
     * case the string before matching and match on a LOWER function on the
     * column. To work efficiently this requires an index on the LOWER of the
     * column. For some bizarre reason on Oracle doing an index on the function
     * of a column requires that a user have an obscure addition privelege on
     * the database. Unfortunately this privelege is disabled by default.
     */
    private void createIndexOnLowerValue() throws SQLException {
        String indexString;
        indexString = "CREATE INDEX " + _table + "_IV ON " + _table + "(LOWER(" + SHORT_VALUE
                + "))";
        executeUpdate(indexString);
    }

    private boolean supportsEscapeClause() {
        return _connection.supportsEscapeClause();
    }

    private boolean supportsEscapeCharacter() throws SQLException {
        return supportsEscapeClause() || _connection.isMySql();
    }

    private static void traceUpdate(PreparedStatement stmt) {
        traceUpdate(stmt, "");
    }

    private static void traceUpdate(PreparedStatement stmt, String append) {
        trace(stmt, append, _isTracingUpdate);
    }

    private static void traceQuery(PreparedStatement stmt) {
        trace(stmt, "", _isTracingQuery);
    }

    private static void trace(PreparedStatement stmt, String append, boolean tracing) {
        if (tracing) {
            String text = stmt.toString();
            if (text.indexOf("PreparedStatement") != -1) {
                int index = text.indexOf(' ');
                if (index != -1) {
                    text = text.substring(index);
                }
            }
            trace(text + append, true);
        }
    }

    private static void trace(String text, boolean tracing) {
        if (tracing) {
            System.out.println("SQL: " + text);
        }
    }

    private static void traceQuery(String text) {
        trace(text, _isTracingQuery);
    }

    private static void traceUpdate(String text) {
        trace(text, _isTracingUpdate);
    }

    private ResultSet executeQuery(PreparedStatement stmt) throws SQLException {
        traceQuery(stmt);
        return stmt.executeQuery();
    }

    private ResultSet executeQuery(String text) throws SQLException {
        traceQuery(text);
        return _connection.getStatement().executeQuery(text);
    }

    private void executeUpdate(PreparedStatement stmt) throws SQLException {
        traceUpdate(stmt);
        stmt.executeUpdate();
    }

    private void executeUpdate(String text) throws SQLException {
        traceUpdate(text);
        _connection.getStatement().executeUpdate(text);
    }

    public void deleteFrame(Frame frame) {
        checkModifiability();
        try {
            deleteFrameSQL(frame);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    /*
     * This call is implemented as multiple SQL calls rather than a single one.
     * This is because surprisingly, it turns out to be much faster to execute
     * them separately. The MySQL optimizer (and perhaps others) has trouble
     * using the indices in the presence of an OR.
     */
    private void deleteFrameSQL(Frame frame) throws SQLException {
        String deleteFrameText = "DELETE FROM " + _table;
        deleteFrameText += " WHERE " + FRAME + " = ? AND " + FRAME_PROJECT + " = ?";
        PreparedStatement deleteFrameStmt = _connection.getPreparedStatement(deleteFrameText);
        setFrame(deleteFrameStmt, 1, 2, frame);
        executeUpdate(deleteFrameStmt);

        String deleteValueText = "DELETE FROM " + _table;
        deleteValueText += " WHERE " + SHORT_VALUE + " = ? AND " + VALUE_TYPE + " = ?";
        PreparedStatement deleteValueStmt = _connection.getPreparedStatement(deleteValueText);
        setShortValue(deleteValueStmt, 1, 2, frame);
        executeUpdate(deleteValueStmt);

        if (frame instanceof Slot) {
            String text = "DELETE FROM " + _table;
            text += " WHERE " + SLOT + " = ? AND " + SLOT_PROJECT + " = ?";
            PreparedStatement deleteSlotStmt = _connection.getPreparedStatement(text);
            setFrame(deleteSlotStmt, 1, 2, frame);
            executeUpdate(deleteSlotStmt);
        } else if (frame instanceof Facet) {
            String text = "DELETE FROM " + _table;
            text += " WHERE " + FACET + " = ? AND " + FACET_PROJECT + " = ?";
            PreparedStatement deleteFacetStmt = _connection.getPreparedStatement(text);
            setFrame(deleteFacetStmt, 1, 2, frame);
            executeUpdate(deleteFacetStmt);
        }

    }

    public Set getReferences(Object value) {
        try {
            return getReferencesSQL(value);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    /*
     * We need to return the short value and test for a real match because MySQL
     * does a case insensitve lookup and this call is supposed to be case
     * sensitive.
     */
    private String _referencesText;

    private Set getReferencesSQL(Object value) throws SQLException {
        if (_referencesText == null) {
            _referencesText = "SELECT " + SHORT_VALUE;
            _referencesText += ", " + FRAME + ", " + FRAME_PROJECT + ", " + FRAME_TYPE;
            _referencesText += ", " + SLOT + ", " + SLOT_PROJECT;
            _referencesText += ", " + FACET + ", " + FACET_PROJECT;
            _referencesText += ", " + IS_TEMPLATE;
            _referencesText += " FROM " + _table;
            _referencesText += " WHERE " + SHORT_VALUE + " = ?";
            _referencesText += " AND " + VALUE_TYPE + " = ?";
        }

        PreparedStatement stmt = _connection.getPreparedStatement(_referencesText);
        setShortValue(stmt, 1, 2, value);

        Set references = new HashSet();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            boolean realMatch = true;
            if (value instanceof String) {
                String returnedValue = rs.getString(1);
                realMatch = value.toString().equals(returnedValue);
            }
            if (realMatch) {
                Frame frame = getFrame(rs, 2, 3, 4);
                Slot slot = getSlot(rs, 5, 6);
                Facet facet = getFacet(rs, 7, 8);
                boolean isTemplate = getIsTemplate(rs, 9);
                references.add(new ReferenceImpl(frame, slot, facet, isTemplate));
            }
        }
        rs.close();
        return references;
    }

    public Set getMatchingReferences(String value, int maxMatches) {
        try {
            return getMatchingReferencesSQL(value);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String _matchingReferencesText;

    private Set getMatchingReferencesSQL(String value) throws SQLException {
        if (_matchingReferencesText == null) {
            _matchingReferencesText = "SELECT " + FRAME + ", " + FRAME_PROJECT + ", " + FRAME_TYPE;
            _matchingReferencesText += ", " + SLOT + ", " + SLOT_PROJECT;
            _matchingReferencesText += ", " + FACET + ", " + FACET_PROJECT;
            _matchingReferencesText += ", " + IS_TEMPLATE;
            _matchingReferencesText += " FROM " + _table;
            _matchingReferencesText += " WHERE " + VALUE_TYPE + " = "
                    + DatabaseUtils.getStringValueType();
            _matchingReferencesText += " AND " + getShortValueMatchColumn();
            _matchingReferencesText += " LIKE '";
        }
        String text = _matchingReferencesText + getMatchString(value) + "' " + getEscapeClause();

        Set references = new HashSet();
        ResultSet rs = executeQuery(text);
        while (rs.next()) {
            Frame frame = getFrame(rs, 1, 2, 3);
            Slot slot = getSlot(rs, 4, 5);
            Facet facet = getFacet(rs, 6, 7);
            boolean isTemplate = getIsTemplate(rs, 8);
            references.add(new ReferenceImpl(frame, slot, facet, isTemplate));
        }
        rs.close();
        return references;
    }

    public void removeValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        checkModifiability();
        try {
            removeValueSQL(frame, slot, facet, isTemplate, value);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String _removeValueText;

    private void removeValueSQL(Frame frame, Slot slot, Facet facet, boolean isTemplate,
            Object value) throws SQLException {
        if (_removeValueText == null) {
            _removeValueText = "DELETE FROM " + _table;
            _removeValueText += " WHERE " + FRAME + " = ? AND " + FRAME_PROJECT + " = ?";
            _removeValueText += " AND " + SLOT + " = ? AND " + SLOT_PROJECT + " = ?";
            _removeValueText += " AND " + FACET + " = ? AND " + FACET_PROJECT + " = ?";
            _removeValueText += " AND " + IS_TEMPLATE + " = ?";
            _removeValueText += " AND " + SHORT_VALUE + " = ?";
            _removeValueText += " AND " + VALUE_TYPE + " = ?";
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_removeValueText);

        setFrame(stmt, 1, 2, frame);
        setSlot(stmt, 3, 4, slot);
        setFacet(stmt, 5, 6, facet);
        setIsTemplate(stmt, 7, isTemplate);
        setShortValue(stmt, 8, 9, value);

        executeUpdate(stmt);
    }

    public void removeValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        checkModifiability();
        try {
            removeValuesSQL(frame, slot, facet, isTemplate);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String _removeValuesText;

    private void removeValuesSQL(Frame frame, Slot slot, Facet facet, boolean isTemplate)
            throws SQLException {
        if (_removeValuesText == null) {
            _removeValuesText = "DELETE FROM " + _table;
            _removeValuesText += " WHERE " + FRAME + " = ? AND " + FRAME_PROJECT + " = ?";
            _removeValuesText += " AND " + SLOT + " = ? AND " + SLOT_PROJECT + " = ?";
            _removeValuesText += " AND " + FACET + " = ? AND " + FACET_PROJECT + " = ?";
            _removeValuesText += " AND " + IS_TEMPLATE + " = ?";
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_removeValuesText);

        setFrame(stmt, 1, 2, frame);
        setSlot(stmt, 3, 4, slot);
        setFacet(stmt, 5, 6, facet);
        setIsTemplate(stmt, 7, isTemplate);

        executeUpdate(stmt);
    }

    public void addValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        try {
            int index = getMaxIndex(frame, slot, facet, isTemplate);
            addValuesSQL(frame, slot, facet, isTemplate, values, index + 1);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String _addValuesText;

    private PreparedStatement getAddValuesStatement() throws SQLException {
        if (_addValuesText == null) {
            _addValuesText = "INSERT INTO " + _table;
            _addValuesText += " (" + FRAME + ", " + FRAME_PROJECT + ", " + FRAME_TYPE;
            _addValuesText += ", " + SLOT + ", " + SLOT_PROJECT;
            _addValuesText += ", " + FACET + ", " + FACET_PROJECT;
            _addValuesText += ", " + IS_TEMPLATE;
            _addValuesText += ", " + VALUE_INDEX + ", " + SHORT_VALUE + ", " + LONG_VALUE + ", "
                    + VALUE_TYPE + ")";
            _addValuesText += " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
        return _connection.getPreparedStatement(_addValuesText);
    }

    private void addValuesSQL(Frame frame, Slot slot, Facet facet, boolean isTemplate,
            Collection values, int index) throws SQLException {
        PreparedStatement stmt = getAddValuesStatement();
        boolean useBatch = values.size() > 1 && _connection.supportsBatch();

        setFrame(stmt, 1, 2, 3, frame);
        setSlot(stmt, 4, 5, slot);
        setFacet(stmt, 6, 7, facet);
        setIsTemplate(stmt, 8, isTemplate);

        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            setValueIndex(stmt, 9, index);
            setValue(stmt, 10, 11, 12, value);
            if (useBatch) {
                stmt.addBatch();
            } else {
                executeUpdate(stmt);
            }
            ++index;
        }
        if (useBatch) {
            stmt.executeBatch();
        }
    }

    private String _maxIndexText;

    private int getMaxIndex(Frame frame, Slot slot, Facet facet, boolean isTemplate)
            throws SQLException {
        if (_maxIndexText == null) {
            _maxIndexText = "SELECT MAX(" + VALUE_INDEX + ")";
            _maxIndexText += " FROM " + _table;
            _maxIndexText += " WHERE " + FRAME + " = ? AND " + FRAME_PROJECT + " = ?";
            _maxIndexText += " AND " + SLOT + " = ? AND " + SLOT_PROJECT + " = ?";
            _maxIndexText += " AND " + FACET + " = ? AND " + FACET_PROJECT + " = ?";
            _maxIndexText += " AND " + IS_TEMPLATE + " = ? ";
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_maxIndexText);

        setFrame(stmt, 1, 2, frame);
        setSlot(stmt, 3, 4, slot);
        setFacet(stmt, 5, 6, facet);
        setIsTemplate(stmt, 7, isTemplate);

        ResultSet rs = executeQuery(stmt);
        int index = -1;
        while (rs.next()) {
            index = rs.getInt(1);
            break;
        }
        rs.close();
        return index;
    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from,
            int index) {
        checkModifiability();
        // this implementation could probably be optimized but it probably isn't
        // worth it
        List values = getValues(frame, slot, facet, isTemplate);
        Object value = values.remove(from);
        values.add(index, value);
        setValues(frame, slot, facet, isTemplate, values);
    }

    public Set executeQuery(Query query) {
        return null;
    }

    public Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value,
            int maxMatches) {
        try {
            return getMatchingFramesSQL(slot, facet, isTemplate, value);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String getShortValueMatchColumn() throws SQLException {
        String matchColumn;
        if (_connection.supportsCaseInsensitiveMatches()) {
            matchColumn = SHORT_VALUE;
        } else {
            matchColumn = "LOWER(" + SHORT_VALUE + ")";
        }
        return matchColumn;
    }

    private Set getMatchingFramesSQL(Slot slot, Facet facet, boolean isTemplate, String value)
            throws SQLException {
        String text = "SELECT " + FRAME + ", " + FRAME_PROJECT + ", " + FRAME_TYPE;
        text += ", " + SLOT + ", " + SLOT_PROJECT;
        text += ", " + FACET + ", " + FACET_PROJECT;
        text += ", " + IS_TEMPLATE;
        text += " FROM " + _table;
        text += " WHERE " + VALUE_TYPE + " = " + DatabaseUtils.getStringValueType();
        text += " AND " + getShortValueMatchColumn() + " LIKE '" + getMatchString(value) + "' "
                + getEscapeClause();

        Set results = new HashSet();
        ResultSet rs = executeQuery(text);
        while (rs.next()) {
            Frame frame = getFrame(rs, 1, 2, 3);
            int returnedSlot = rs.getInt(4);
            int returnedSlotProject = rs.getInt(5);
            int returnedFacet = rs.getInt(6);
            int returnedFacetProject = rs.getInt(7);
            boolean returnedIsTemplate = rs.getBoolean(8);
            if (equals(returnedSlotProject, returnedSlot, slot)
                    && equals(returnedFacetProject, returnedFacet, facet)
                    && returnedIsTemplate == isTemplate) {
                results.add(frame);
            }
        }
        rs.close();
        return results;
    }

    private boolean equals(int projectId, int localId, Frame frame) {
        boolean equal;
        if (frame == null) {
            equal = localId == FrameID.NULL_FRAME_ID_VALUE;
        } else {
            FrameID id = frame.getFrameID();
            equal = FrameID.create(projectId, localId).equals(id);
        }
        return equal;
    }

    private String getMatchString(String value) throws SQLException {
        return DatabaseUtils.getMatchString(value, supportsEscapeCharacter());
    }

    private String getEscapeClause() {
        return supportsEscapeClause() ? DatabaseUtils.getEscapeClause() : "";
    }

    public Set getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        try {
            return getFramesSQL(slot, facet, isTemplate, value);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    /*
     * We need to return the value and test for a "real match" because MySQL
     * will match in a case insensitive way. This really messes up our frame
     * lookup by name because different types of frames often only differ by the
     * case of their names.
     */
    private String _framesText;

    private Set getFramesSQL(Slot slot, Facet facet, boolean isTemplate, Object value)
            throws SQLException {
        if (_framesText == null) {
            _framesText = "SELECT " + SHORT_VALUE;
            _framesText += ", " + FRAME + ", " + FRAME_PROJECT + ", " + FRAME_TYPE;
            _framesText += " FROM " + _table;
            _framesText += " WHERE " + SLOT + " = ? AND " + SLOT_PROJECT + " = ?";
            _framesText += " AND " + FACET + " = ? AND " + FACET_PROJECT + " = ?";
            _framesText += " AND " + IS_TEMPLATE + " = ?";
            _framesText += " AND " + SHORT_VALUE + " = ?";
            _framesText += " AND " + VALUE_TYPE + " = ?";
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_framesText);

        setSlot(stmt, 1, 2, slot);
        setFacet(stmt, 3, 4, facet);
        setIsTemplate(stmt, 5, isTemplate);
        setShortValue(stmt, 6, 7, value);

        Set results = new HashSet();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            boolean isRealMatch = true;
            if (value instanceof String) {
                String returnedValue = rs.getString(1);
                isRealMatch = value.equals(returnedValue);
            }
            if (isRealMatch) {
                Frame frame = getFrame(rs, 2, 3, 4);
                results.add(frame);
            }
        }
        rs.close();
        return results;
    }

    public Set getFramesWithAnyValue(Slot slot, Facet facet, boolean isTemplate) {
        try {
            return getFramesWithAnyValueSQL(slot, facet, isTemplate);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String _framesWithAnyValueText;

    private Set getFramesWithAnyValueSQL(Slot slot, Facet facet, boolean isTemplate)
            throws SQLException {
        if (_framesWithAnyValueText == null) {
            _framesWithAnyValueText = "SELECT " + FRAME + ", " + FRAME_PROJECT + ", " + FRAME_TYPE;
            _framesWithAnyValueText += " FROM " + _table;
            _framesWithAnyValueText += " WHERE " + SLOT + " = ? AND " + SLOT_PROJECT + " = ?";
            _framesWithAnyValueText += " AND " + FACET + " = ? AND " + FACET_PROJECT + " = ?";
            _framesWithAnyValueText += " AND " + IS_TEMPLATE + " = ?";
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_framesWithAnyValueText);

        setSlot(stmt, 1, 2, slot);
        setFacet(stmt, 3, 4, facet);
        setIsTemplate(stmt, 5, isTemplate);

        Set results = new HashSet();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            Frame frame = getFrame(rs, 1, 2, 3);
            results.add(frame);
        }
        rs.close();
        return results;
    }

    public void setValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values) {
        // Log.enter(this, "setValues", frame, slot, facet,
        // Boolean.valueOf(isTemplate), values);
        checkModifiability();
        try {
            removeValuesSQL(frame, slot, facet, isTemplate);
            addValuesSQL(frame, slot, facet, isTemplate, values, 0);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    public List getValues(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        List values;
        try {
            values = getValuesSQL(frame, slot, facet, isTemplate);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
        return values;
    }

    private String _valuesText;

    private List getValuesSQL(Frame frame, Slot slot, Facet facet, boolean isTemplate)
            throws SQLException {
        if (_valuesText == null) {
            _valuesText = "SELECT " + SHORT_VALUE + ", " + VALUE_TYPE + ", " + VALUE_INDEX;
            _valuesText += " FROM " + _table;
            _valuesText += " WHERE " + FRAME + " = ? AND " + FRAME_PROJECT + " = ?";
            _valuesText += " AND " + SLOT + " = ? AND " + SLOT_PROJECT + " = ?";
            _valuesText += " AND " + FACET + " = ? AND " + FACET_PROJECT + " = ?";
            _valuesText += " AND " + IS_TEMPLATE + " = ?";
            _valuesText += " ORDER BY " + VALUE_INDEX;
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_valuesText);

        setFrame(stmt, 1, 2, frame);
        setSlot(stmt, 3, 4, slot);
        setFacet(stmt, 5, 6, facet);
        setIsTemplate(stmt, 7, isTemplate);

        List values = new ArrayList();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            Object value = getShortValue(rs, 1, 2);
            if (value == null) {
                int index = getIndex(rs, 3);
                value = getLongValue(frame, slot, facet, isTemplate, index);
            }
            values.add(value);
        }
        rs.close();
        return values;
    }

    public int getValuesCount(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        int count = 0;
        try {
            count = getValuesCountSQL(frame, slot, facet, isTemplate);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
        return count;
    }

    private String _valuesCountText;

    private int getValuesCountSQL(Frame frame, Slot slot, Facet facet, boolean isTemplate)
            throws SQLException {
        if (_valuesCountText == null) {
            _valuesCountText = "SELECT COUNT(*) ";
            _valuesCountText += " FROM " + _table;
            _valuesCountText += " WHERE " + FRAME + " = ? AND " + FRAME_PROJECT + " = ?";
            _valuesCountText += " AND " + SLOT + " = ? AND " + SLOT_PROJECT + " = ?";
            _valuesCountText += " AND " + FACET + " = ? AND " + FACET_PROJECT + " = ?";
            _valuesCountText += " AND " + IS_TEMPLATE + " = ?";
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_valuesText);

        setFrame(stmt, 1, 2, frame);
        setSlot(stmt, 3, 4, slot);
        setFacet(stmt, 5, 6, facet);
        setIsTemplate(stmt, 7, isTemplate);

        ResultSet rs = executeQuery(stmt);
        int count = 0;
        while (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        return count;
    }

    public Map getFrameValues(Frame frame) {
        try {
            return getFrameValuesSQL(frame);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String _frameValuesText;

    private Map getFrameValuesSQL(Frame frame) throws SQLException {
        if (_frameValuesText == null) {
            _frameValuesText = "SELECT " + SLOT + ", " + SLOT_PROJECT;
            _frameValuesText += ", " + FACET + ", " + FACET_PROJECT;
            _frameValuesText += ", " + IS_TEMPLATE;
            _frameValuesText += ", " + SHORT_VALUE + ", " + VALUE_TYPE + ", " + VALUE_INDEX;
            _frameValuesText += " FROM " + _table;
            _frameValuesText += " WHERE " + FRAME + " = ? AND " + FRAME_PROJECT + " = ?";
            if (true) {
                _frameValuesText += " AND " + SLOT + " <> "
                        + Model.SlotID.DIRECT_INSTANCES.getLocalPart();
                _frameValuesText += " AND " + SLOT + " <> "
                        + Model.SlotID.DIRECT_SUBCLASSES.getLocalPart();
            }
            _frameValuesText += " ORDER BY " + SLOT + ", " + FACET + ", " + IS_TEMPLATE + ", "
                    + VALUE_INDEX;
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_frameValuesText);
        // System.out.println("fetch size = " + stmt.getFetchSize());

        setFrame(stmt, 1, 2, frame);

        Map sftToValueMap = new HashMap();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            Slot slot = getSlot(rs, 1, 2);
            Facet facet = getFacet(rs, 3, 4);
            boolean isTemplate = getIsTemplate(rs, 5);
            Object value = getShortValue(rs, 6, 7);
            if (value == null) {
                int index = getIndex(rs, 8);
                value = getLongValue(frame, slot, facet, isTemplate, index);
            }
            addToMap(sftToValueMap, slot, facet, isTemplate, value);
        }
        rs.close();
        return sftToValueMap;
    }

    private void addToMap(Map map, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Sft sft = new Sft(slot, facet, isTemplate);
        Collection values = (Collection) map.get(sft);
        if (values == null) {
            values = new ArrayList();
            map.put(sft, values);
        }
        values.add(value);
    }

    private String _longValueText;

    private Object getLongValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int index)
            throws SQLException {
        if (_longValueText == null) {
            _longValueText = "SELECT " + LONG_VALUE;
            _longValueText += " FROM " + _table;
            _longValueText += " WHERE " + FRAME + " = ? AND " + FRAME_PROJECT + " = ?";
            _longValueText += " AND " + SLOT + " = ? AND " + SLOT_PROJECT + " = ?";
            _longValueText += " AND " + FACET + " = ? AND " + FACET_PROJECT + " = ?";
            _longValueText += " AND " + IS_TEMPLATE + " = ?";
            _longValueText += " AND " + VALUE_INDEX + " = ?";
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_longValueText);

        setFrame(stmt, 1, 2, frame);
        setSlot(stmt, 3, 4, slot);
        setFacet(stmt, 5, 6, facet);
        setIsTemplate(stmt, 7, isTemplate);
        setValueIndex(stmt, 8, index);

        Object value = null;
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            value = getLongValue(rs, 1);
            break;
        }
        rs.close();
        return value;
    }

    // forwarding methods
    private void setFrame(PreparedStatement stmt, int index, int projectIndex, Frame frame)
            throws SQLException {
        DatabaseUtils.setFrame(stmt, index, projectIndex, frame, _frameIDHelper);
    }

    private void setFrame(PreparedStatement stmt, int frameIndex, int projectIndex,
            int valueTypeIndex, Frame frame) throws SQLException {
        DatabaseUtils.setFrame(stmt, frameIndex, projectIndex, valueTypeIndex, frame,
                _frameIDHelper);
    }

    private void setSlot(PreparedStatement stmt, int index, int projectIndex, Slot slot)
            throws SQLException {
        DatabaseUtils.setSlot(stmt, index, projectIndex, slot, _frameIDHelper);
    }

    private void setFacet(PreparedStatement stmt, int index, int projectIndex, Facet facet)
            throws SQLException {
        DatabaseUtils.setFacet(stmt, index, projectIndex, facet, _frameIDHelper);
    }

    private static void setIsTemplate(PreparedStatement stmt, int index, boolean isTemplate)
            throws SQLException {
        DatabaseUtils.setIsTemplate(stmt, index, isTemplate);
    }

    private void setShortValue(PreparedStatement stmt, int valueIndex, int valueTypeIndex,
            Object value) throws SQLException {
        DatabaseUtils.setShortValue(stmt, valueIndex, valueTypeIndex, value, _frameIDHelper);
    }

    private void setValue(PreparedStatement stmt, int shortIndex, int longIndex,
            int valueTypeIndex, Object value) throws SQLException {
        DatabaseUtils.setValue(stmt, shortIndex, longIndex, valueTypeIndex, value, _connection
                .getMaxVarcharSize(), _frameIDHelper);
    }

    private static void setValueIndex(PreparedStatement stmt, int index, int valueIndex)
            throws SQLException {
        DatabaseUtils.setValueIndex(stmt, index, valueIndex);
    }

    private Frame getFrame(ResultSet rs, int frameIndex, int projectIndex, int typeIndex)
            throws SQLException {
        return DatabaseUtils.getFrame(rs, frameIndex, projectIndex, typeIndex, _frameFactory,
                _frameIDHelper);
    }

    private Slot getSlot(ResultSet rs, int index, int projectIndex) throws SQLException {
        return DatabaseUtils.getSlot(rs, index, projectIndex, _frameFactory, _frameIDHelper);
    }

    private Facet getFacet(ResultSet rs, int index, int projectIndex) throws SQLException {
        return DatabaseUtils.getFacet(rs, index, projectIndex, _frameFactory, _frameIDHelper);
    }

    private int getIndex(ResultSet rs, int index) throws SQLException {
        return DatabaseUtils.getIndex(rs, index);
    }

    private boolean getIsTemplate(ResultSet rs, int index) throws SQLException {
        return DatabaseUtils.getIsTemplate(rs, index);
    }

    private Object getShortValue(ResultSet rs, int index, int valueTypeIndex) throws SQLException {
        return DatabaseUtils
                .getShortValue(rs, index, valueTypeIndex, _frameFactory, _frameIDHelper);
    }

    private Object getLongValue(ResultSet rs, int index) throws SQLException {
        return DatabaseUtils.getLongValue(rs, index);
    }

    private PreparedStatement _addValuesBatchStmt;
    private int _addValuesBatchCounter;
    private static final int MAX_BATCH = 1000;

    private void beginBatch() throws SQLException {
        _addValuesBatchStmt = getAddValuesStatement();
        _connection.setAutoCommit(false);
        _addValuesBatchCounter = 0;
    }

    private void endBatch() throws SQLException {
        if (_addValuesBatchCounter > 0) {
            _addValuesBatchStmt.executeBatch();
            _connection.commit();
        }
        _addValuesBatchStmt = null;
        _connection.setAutoCommit(true);
    }

    public void saveKnowledgeBase(KnowledgeBase kb) throws SQLException {
        _slotToFacetsCacheMap.clear();
        ensureEmptyTableExists();
        boolean wasCaching = kb.setCallCachingEnabled(false);
        beginBatch();
        saveFrames(kb);
        endBatch();
        createIndices();
        kb.setCallCachingEnabled(wasCaching);
    }

    private static final Map _slotToFacetsCacheMap = new HashMap();

    private void saveFrames(KnowledgeBase kb) throws SQLException {
        nFrames = kb.getFrameCount();
        loopcount = 0;
        previousTime = System.currentTimeMillis();

        if (nFrames > LOOP_SIZE) {
            Log.getLogger()
                    .info("Getting " + nFrames + " frames, please be patient, " + new Date());
        }
        Iterator i = kb.getFrames().iterator();
        while (i.hasNext()) {
            Frame frame = (Frame) i.next();
            printTraceMessage();
            saveDirectOwnSlotValues(frame);
            if (frame instanceof Cls) {
                Cls cls = (Cls) frame;
                saveDirectTemplateSlotInformation(cls);
            }
            // do this just in case it helps with caching
            i.remove();
        }
    }

    private static final int LOOP_SIZE = 1000;
    private int loopcount;
    private int nFrames;
    private long previousTime;

    private void printTraceMessage() {
        ++loopcount;
        if (loopcount % LOOP_SIZE == 0) {
            long currentTime = System.currentTimeMillis();
            long delta = (currentTime - previousTime) / 1000;
            previousTime = currentTime;
            System.gc();
            Runtime runtime = Runtime.getRuntime();
            String text = loopcount + "/" + nFrames;
            text += ", " + new Date();
            text += ", delta=" + delta;
            text += ", mem(f/t/m)=" + runtime.freeMemory() / 1000;
            text += "/" + runtime.totalMemory() / 1000;
            text += "/" + runtime.maxMemory() / 1000;
            Log.getLogger().info(text);
        }
    }

    private void saveValues(Frame frame, Slot slot, Facet facet, boolean isTemplate,
            Collection values) throws SQLException {
        int index = 0;
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            setFrame(_addValuesBatchStmt, 1, 2, 3, frame);
            setSlot(_addValuesBatchStmt, 4, 5, slot);
            setFacet(_addValuesBatchStmt, 6, 7, facet);
            setIsTemplate(_addValuesBatchStmt, 8, isTemplate);
            setValueIndex(_addValuesBatchStmt, 9, index);
            setValue(_addValuesBatchStmt, 10, 11, 12, value);
            addBatch();
            ++index;
        }
    }

    private void addBatch() throws SQLException {
        _addValuesBatchStmt.addBatch();
        ++_addValuesBatchCounter;
        if (_addValuesBatchCounter > MAX_BATCH) {
            _addValuesBatchStmt.executeBatch();
            _addValuesBatchCounter = 0;
            _connection.commit();
        }
    }

    private void saveDirectOwnSlotValues(Frame frame) throws SQLException {
        Iterator i = frame.getOwnSlots().iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            Collection values = frame.getDirectOwnSlotValues(slot);
            saveValues(frame, slot, null, false, values);
        }
    }

    private void saveDirectTemplateSlotInformation(Cls cls) throws SQLException {
        Iterator i = cls.getTemplateSlots().iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            Collection values = cls.getDirectTemplateSlotValues(slot);
            saveValues(cls, slot, null, true, values);
            saveDirectTemplateFacetValues(cls, slot);
        }
    }

    private void saveDirectTemplateFacetValues(Cls cls, Slot slot) throws SQLException {
        Iterator i = getTemplateFacets(cls, slot).iterator();
        while (i.hasNext()) {
            Facet facet = (Facet) i.next();
            Collection values = cls.getDirectTemplateFacetValues(slot, facet);
            saveValues(cls, slot, facet, true, values);
        }
    }

    private Collection getTemplateFacets(Cls cls, Slot slot) {
        Collection facets = (Collection) _slotToFacetsCacheMap.get(slot);
        if (facets == null) {
            facets = cls.getTemplateFacets(slot);
            _slotToFacetsCacheMap.put(slot, facets);
        }
        return facets;
    }

    public boolean beginTransaction(String name) {
        checkModifiability();
        return _connection.beginTransaction();
    }

    public boolean commitTransaction() {
        checkModifiability();
        return _connection.commitTransaction();
    }

    public boolean rollbackTransaction() {
        checkModifiability();
        return _connection.rollbackTransaction();
    }

    private void checkModifiability() {
        if (!_isModifiable) {
            throw new IllegalStateException("Database is not modifiable");
        }
    }

    public void replaceFrame(Frame frame) {
        // do nothing
    }

    public String toString() {
        return "DatabaseFrameDb";
    }

    public int getClsCount() {
        return countUniqueFrames(Cls.class);
    }

    public int getSlotCount() {
        return countUniqueFrames(Slot.class);
    }

    public int getFacetCount() {
        return countUniqueFrames(Facet.class);
    }

    public int getFrameCount() {
        return countUniqueFrames();
    }

    public int getSimpleInstanceCount() {
        return countUniqueFrames(SimpleInstance.class);
    }

    private int countUniqueFrames(Class frameClass) {
        int count = -1;
        try {
            count = countFramesSQL(frameClass);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
        return count;
    }

    private String _countFramesOfTypeText;

    private int countFramesSQL(Class frameClass) throws SQLException {
        if (_countFramesOfTypeText == null) {
            _countFramesOfTypeText = "SELECT COUNT(*) FROM " + _table;
            _countFramesOfTypeText += " WHERE " + SLOT + " = " + Model.SlotID.NAME.getLocalPart();
            _countFramesOfTypeText += " AND " + FACET + " = " + FrameID.NULL_FRAME_ID_VALUE;
            _countFramesOfTypeText += " AND " + IS_TEMPLATE + " = 0";
            _countFramesOfTypeText += " AND " + FRAME_TYPE + " = ?";
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_countFramesOfTypeText);

        setFrameType(stmt, 1, frameClass);
        int count = -1;
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        return count;
    }

    private int countUniqueFrames() {
        int count = -1;
        try {
            count = countFramesSQL();
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
        return count;
    }

    private String _countFramesText;

    private int countFramesSQL() throws SQLException {
        if (_countFramesText == null) {
            _countFramesText = "SELECT COUNT(*) FROM " + _table;
            _countFramesText += " WHERE " + SLOT + " = " + Model.SlotID.NAME.getLocalPart();
            _countFramesText += " AND " + FACET + " = " + FrameID.NULL_FRAME_ID_VALUE;
            _countFramesText += " AND " + IS_TEMPLATE + " = 0";
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_countFramesText);

        int count = -1;
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        return count;
    }

    private void setFrameType(PreparedStatement stmt, int index, Class frameClass)
            throws SQLException {
        DatabaseUtils.setFrameType(stmt, index, frameClass);
    }

    // We arbitrarily decide that a frame is defined where its name is set.
    public Frame getFrame(FrameID id) {
        throw new UnsupportedOperationException();
    }

    public FrameID generateFrameID() {
        return FrameID.createLocal(getNextFrameID());
    }

    private int getNextFrameID() {
        int nextID = 0;
        try {
            int maxID = 0;
            String text = "SELECT MAX(" + FRAME + ")";
            text += " FROM " + _table;
            text += " WHERE " + FRAME_PROJECT + " = " + FrameID.LOCAL_PROJECT_ID;
            ResultSet rs = executeQuery(text);
            while (rs.next()) {
                maxID = rs.getInt(1);
            }
            nextID = Math.max(maxID + 1, FrameID.INITIAL_USER_FRAME_ID);
        } catch (SQLException e) {
            createRuntimeException(e);
        }
        return nextID;
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        return ClosureUtils.calculateClosure(this, frame, slot, facet, isTemplate);
    }

    public NarrowFrameStore getDelegate() {
        return null;
    }

    public Set getFrames() {
        Set frames = null;
        try {
            frames = getFramesSQL();
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
        return frames;
    }

    private String _getFramesText;

    private Set getFramesSQL() throws SQLException {
        if (_getFramesText == null) {
            _getFramesText = "SELECT DISTINCT " + FRAME + ", " + FRAME_PROJECT + ", " + FRAME_TYPE;
            _getFramesText += " FROM " + _table;
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_getFramesText);

        Set frames = new HashSet();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            Frame frame = getFrame(rs, 1, 2, 3);
            frames.add(frame);
        }
        rs.close();
        return frames;
    }

    public CacheMap getFrameValues() {
        try {
            return getFrameValuesSQL();
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String _allFrameValuesText;

    private CacheMap getFrameValuesSQL() throws SQLException {
        if (_allFrameValuesText == null) {
            _allFrameValuesText = "SELECT " + FRAME + ", " + FRAME_PROJECT + ", " + FRAME_TYPE;
            _allFrameValuesText += ", " + SLOT + ", " + SLOT_PROJECT;
            _allFrameValuesText += ", " + FACET + ", " + FACET_PROJECT;
            _allFrameValuesText += ", " + IS_TEMPLATE;
            _allFrameValuesText += ", " + SHORT_VALUE + ", " + VALUE_TYPE + ", " + VALUE_INDEX;
            _allFrameValuesText += ", " + LONG_VALUE;
            _allFrameValuesText += " FROM " + _table;
            _allFrameValuesText += " ORDER BY " + SLOT + ", " + FACET + ", " + IS_TEMPLATE + ", "
                    + VALUE_INDEX;
        }
        PreparedStatement stmt = _connection.getPreparedStatement(_allFrameValuesText);

        CacheMap frameToSftToValueMap = new CacheMap();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            Frame frame = getFrame(rs, 1, 2, 3);
            Slot slot = getSlot(rs, 4, 5);
            Facet facet = getFacet(rs, 6, 7);
            boolean isTemplate = getIsTemplate(rs, 8);
            Object value = getShortValue(rs, 9, 10);
            if (value == null) {
                // int index = getIndex(rs, 8);
                // value = getLongValue(frame, slot, facet, isTemplate, index);
                value = getLongValue(rs, 9);
            }
            if (value == null) {
                nullValueError(frame, slot, facet, isTemplate);
            } else {
                addToMap(frameToSftToValueMap, frame, slot, facet, isTemplate, value);
            }
        }
        rs.close();
        return frameToSftToValueMap;
    }

    private void nullValueError(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        Log.getLogger().warning(
                "null value: " + id(frame) + " " + id(slot) + " " + id(facet) + isTemplate);
    }

    private static FrameID id(Frame frame) {
        return (frame == null) ? null : frame.getFrameID();
    }

    private void addToMap(CacheMap map, Frame frame, Slot slot, Facet facet, boolean isTemplate,
            Object value) {
        Map sftToValueMap = (Map) map.get(frame);
        if (sftToValueMap == null) {
            sftToValueMap = createInitialMap(frame);
            map.put(frame, sftToValueMap);
        }
        addToMap(sftToValueMap, slot, facet, isTemplate, value);
    }

    private Map createInitialMap(Frame frame) {
        Map map = new HashMap();
        if (frame instanceof Cls) {
            Slot directInstancesSlot = frame.getKnowledgeBase().getSystemFrames()
                    .getDirectInstancesSlot();
            map.put(new Sft(directInstancesSlot, null, false), new ArrayList());
            // map.put(new Sft(directSubclassesSlot, null, false), new
            // ArrayList());
        }
        return map;
    }

}