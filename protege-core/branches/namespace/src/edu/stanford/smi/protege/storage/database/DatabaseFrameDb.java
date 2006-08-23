package edu.stanford.smi.protege.storage.database;

//ESCA*JAVA0100

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.ReferenceImpl;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.model.query.Query;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.Session;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.CacheMap;
import edu.stanford.smi.protege.util.Log;

public class DatabaseFrameDb implements NarrowFrameStore {
  private static Logger log = Log.getLogger(DatabaseFrameDb.class);
  
  public enum Column {
    frame, frame_type, slot, facet, is_template, value_index, value_type, short_value, long_value
  }
  
  int projectId = FrameID.allocateMemoryProjectPart();
	
    private static final String FRAME_COLUMN = "frame";
    private static final String FRAME_TYPE_COLUMN = "frame_type";
    private static final String SLOT_COLUMN = "slot";
    private static final String FACET_COLUMN = "facet";
    private static final String IS_TEMPLATE_COLUMN = "is_template";
    private static final String VALUE_INDEX_COLUMN = "value_index";
    private static final String VALUE_TYPE_COLUMN = "value_type";
    private static final String SHORT_VALUE_COLUMN = "short_value";
    private static final String LONG_VALUE_COLUMN = "long_value";

    private final Map _connections = new HashMap();
    private String _table;
    private String _driver;
    private String _url;
    private String _user;
    private String _password;
    private boolean _isInclude;
    private FrameFactory _frameFactory;
    private static boolean _isModifiable = true;

    private static int lastReturnedFrameID = FrameID.INITIAL_USER_FRAME_ID - 1;

    private String frameDbName;

    public String getName() {
        return frameDbName;
    }

    public void setName(String name) {
        frameDbName = name;
    }

    public Set getClosure(Frame frame, Slot slot, Facet facet, boolean isTemplate) {
        throw new UnsupportedOperationException();
    }

    // Returns the current remote session.  In standalone mode it returns null.
    private Object getCurrentSession() {
        return ServerFrameStore.getCurrentSession();
    }

    protected RobustConnection getCurrentConnection() throws SQLException {
        Object currentSession = getCurrentSession();
        RobustConnection connection = (RobustConnection) _connections.get(currentSession);
        if (connection == null) {
            connection = createConnection();
            _connections.put(currentSession, connection);
        }
        return connection;
    }

    public void close() {
        _frameFactory = null;
        try {
            Iterator i = _connections.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry) i.next();
                Object session = entry.getKey();
                RobustConnection connection = (RobustConnection) entry.getValue();
                connection.close();
                Log.getLogger().info("Closed connection for session: " + session);
            }
            _connections.clear();
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }
    
    public DatabaseFrameDb() {
      
    }
    
    protected void setMemoryProjectId(int memoryProjectId) {
      projectId = memoryProjectId;
    }

    public void initialize(FrameFactory factory, 
                           String driver, 
                           String url, String user, String pass, String table,
                           boolean isInclude) {
    	if (log.isLoggable(Level.FINE)) {
    		log.fine("Constructing database frame narrow frame store for " + driver + " " + url + " " + table);
    		log.fine("No delegates");
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
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }
    
    public FrameFactory getFrameFactory() {
      return _frameFactory;
    }
    
    public String getTable() {
      return _table;
    }

    private void clearDeadConnections() throws SQLException {
        Iterator i = _connections.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            Session session = (Session) entry.getKey();
            if (isDead(session)) {
                RobustConnection connection = (RobustConnection) entry.getValue();
                // Log.getLogger().info("Clearing dead connection: " + session);
                connection.close();
                i.remove();
            }
        }
    }

    private boolean isDead(Session session) {
        return session != null && !Server.getInstance().isActive(session);
    }

    private RobustConnection createConnection() throws SQLException {
        clearDeadConnections();
        Object currentSession = getCurrentSession();
        RobustConnection connection = new RobustConnection(_driver, _url, _user, _password);
        _connections.put(currentSession, connection);
        if (log.isLoggable(Level.FINE)) {
          log.fine("Created connection for " + currentSession);  
        }
        return connection;
    }

    public static void setModifiable(boolean modifiable) {
        _isModifiable = modifiable;
    }

    public String getTableName() {
        return _table;
    }

    private RuntimeException createRuntimeException(SQLException e) {
        try {
            if (getCurrentConnection() != null) {
                getCurrentConnection().checkConnection();
            }
        } catch (SQLException ex) {
            // do nothing
        }
        Log.getLogger().info(Log.toString(e));
        return new RuntimeException(e.getMessage());
    }

    /*
     * Using the database metadata to decide if a table exists turns out to be unreliable. Instead we just do a select
     * of something fast and make sure that the table isn't completely empty. If this succeeds the table is assumed to
     * be ok.
     */

    public boolean tableExists() {
        boolean exists = false;
        String command = "SELECT " + FRAME_COLUMN + ", " + FRAME_TYPE_COLUMN + " FROM " + _table;
        command += " WHERE " + SLOT_COLUMN + " = " + getValue(Model.SlotID.NAME);
        command += " AND " + SHORT_VALUE_COLUMN + " = '" + Model.Cls.THING + "'";
        try {
            ResultSet rs = executeQuery(command);
            while (rs.next()) {
                exists = true;
                break;
            }
            rs.close();
        } catch (SQLException e) {
            // do nothing
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
            getCurrentConnection().closeStatements();
            String command = "DROP TABLE " + _table;
            executeUpdate(command);
        } catch (Exception e) {
            // do nothing
        }
    }

    private void createTable() throws SQLException {
        String createTableString = "CREATE TABLE " + _table + " (";
        createTableString += FRAME_COLUMN + " " + getFrameDataType() + " NOT NULL, ";
        createTableString += FRAME_TYPE_COLUMN + " " + getFrameTypeDataType() + " NOT NULL, ";
        createTableString += SLOT_COLUMN + " " + getFrameDataType() + " NOT NULL, ";
        createTableString += FACET_COLUMN + " " + getFrameDataType() + " NOT NULL, ";
        createTableString += IS_TEMPLATE_COLUMN + " " + getIsTemplateDataType() + " NOT NULL, ";
        createTableString += VALUE_INDEX_COLUMN + " " + getValueIndexDataType() + " NOT NULL, ";
        createTableString += VALUE_TYPE_COLUMN + " " + getFrameTypeDataType() + " NOT NULL, ";
        createTableString += SHORT_VALUE_COLUMN + " " + getShortValueDataType() + ", ";
        createTableString += LONG_VALUE_COLUMN + " " + getLongValueDataType();
        createTableString += ")";
        if (checkMySQLBug()) {
          createTableString += " ENGINE = INNODB";
        }
        try {
            executeUpdate(createTableString);
            if (log.isLoggable(Level.FINE)) {
                log.fine("Created table with command '" + createTableString + "'");
            }
        } catch (SQLException e) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Failed to create table on database ");
            buffer.append(getCurrentConnection().getDatabaseProductName());
            buffer.append(" with command '");
            buffer.append(createTableString);
            buffer.append("' :");
            buffer.append(e.getMessage());
            Log.getLogger().warning(buffer.toString());
            throw e;
        }
    }

    private String getFrameDataType() throws SQLException {
        return getCurrentConnection().getIntegerTypeName();
    }

    private String getFrameTypeDataType() throws SQLException {
        return getCurrentConnection().getSmallIntTypeName();
    }

    private String getIsTemplateDataType() throws SQLException {
      return getCurrentConnection().getBitTypeName();
      // return getCurrentConnection().getSmallIntTypeName();
    }

    private String getValueIndexDataType() throws SQLException {
        return getCurrentConnection().getIntegerTypeName();
    }

    private String getShortValueDataType() throws SQLException {
        return getCurrentConnection().getVarcharTypeName() + "(" + getCurrentConnection().getMaxVarcharSize() + ")";
    }

    private String getLongValueDataType() throws SQLException {
        return getCurrentConnection().getLongvarcharTypeName();
    }
   
    /**
     * This routine checks for a bug in mysql.
     * 
     * MySQL hack - there is a bug in mysql where SELECT statements will fail to 
     *              produce the correct result because of these index statements.  There is 
     *              a bug report out to mysql for this problem on the page
     *                  http://bugs.mysql.com/bug.php?id=16121
     *              Right now we are working around this problem by disabling indexing.
     *              Sometime I need to investigate exactly which mysql's are bad.
     */
    public boolean checkMySQLBug() {
      try {
        if (getCurrentConnection().isMySql() 
            && getCurrentConnection().getDatabaseMajorVersion() == 5) {
          if (log.isLoggable(Level.FINE)) {
            log.fine("Found mysql 5.0 - correcting for mysql bug 16121.");
          }
          return true;
        }
      } catch (Exception e) {
        Log.getLogger().log(Level.WARNING, "Exception caught checking database version", e);
        Log.getLogger().warning("Unable to check database version with this jdbc driver");
        Log.getLogger().warning("If this database is mysql 5 then protege will perform incorrectly");
      }
      return false;    
    }

    private void createIndices() throws SQLException {
        String indexString;

        /*
         * VALUE_INDEX is included in this index solely for its value as a side effect. It keeps the values ordered by
         * the VALUE_INDEX so that the ORDER_BY clause in the getValues SELECT statement does not cost anything.
         * Otherwise this clause has a huge performance impact. We are essentially trading disk space and INSERT time
         * for a SELECT optimization.
         */
        // used for slot and facet value lookup
        indexString = "CREATE INDEX " + _table + "_I1 ON " + _table;
        indexString += " (" + FRAME_COLUMN + ", " + SLOT_COLUMN + ", " + FACET_COLUMN + ", " + IS_TEMPLATE_COLUMN
                + ", " + VALUE_INDEX_COLUMN + ")";
        executeUpdate(indexString);

        // used for searching for values
        indexString = "CREATE INDEX " + _table + "_I2 ON " + _table;
        indexString += " (" + SHORT_VALUE_COLUMN + ")";
        executeUpdate(indexString);

        // used for getting slots with any value and for counting frames
        indexString = "CREATE INDEX " + _table + "_I3 ON " + _table;
        indexString += " (" + SLOT_COLUMN + ", " + FRAME_TYPE_COLUMN + ")";
        executeUpdate(indexString);

        if (needsIndexOnLowerValue()) {
            createIndexOnLowerValue();
        }
    }

    private boolean needsIndexOnLowerValue() throws SQLException {
        return !getCurrentConnection().supportsCaseInsensitiveMatches()
                && getCurrentConnection().supportsIndexOnFunction();
    }

    /*
     * Some databases (e.g. Oracle) do case sensitive string compares on a LIKE but we want case insensitive matches.
     * Thus for these databases we lower case the string before matching and match on a LOWER function on the column. To
     * work efficiently this requires an index on the LOWER of the column. For some bizarre reason on Oracle doing an
     * index on the function of a column requires that a user have an obscure addition privelege on the database.
     * Unfortunately this privelege is disabled by default.
     */
    private void createIndexOnLowerValue() throws SQLException {
        String indexString;
        indexString = "CREATE INDEX " + _table + "_IV ON " + _table + "(LOWER(" + SHORT_VALUE_COLUMN + "))";
        executeUpdate(indexString);
    }

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
    
    private static int traceCount = 0;
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

    private static ResultSet executeQuery(PreparedStatement stmt) throws SQLException {
        long startTime = 0;
        traceQuery(stmt);
        if (log.isLoggable(Level.FINER)) {
          startTime = System.nanoTime();
        }       
        ResultSet ret = stmt.executeQuery();
        if (log.isLoggable(Level.FINER)) {
          log.finer("Query took " + ((float) (System.nanoTime() - startTime))/1000000.0 
                      + " milliseconds (more or less)");
        }       
        return ret;
    }

    private ResultSet executeQuery(String text) throws SQLException {
        return executeQuery(text, 0);
    }

    private ResultSet executeQuery(String text, int maxRows) throws SQLException {
        long startTime = 0;
        traceQuery(text);
        Statement statement = getCurrentConnection().getStatement();
        // statement.setMaxRows(maxRows);
        if (log.isLoggable(Level.FINER)) {
          startTime = System.nanoTime();
        }
        ResultSet ret = statement.executeQuery(text);
        if (log.isLoggable(Level.FINER)) {
          log.finer("Query took " + ((float) (System.nanoTime() - startTime))/1000000.0
                      + " milliseconds (more or less)");
        }
        return ret;
    }

    private static void executeUpdate(PreparedStatement stmt) throws SQLException {
        traceUpdate(stmt);
        stmt.executeUpdate();
    }

    private void executeUpdate(String text) throws SQLException {
        traceUpdate(text);
        getCurrentConnection().getStatement().executeUpdate(text);
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
     * This call is implemented as multiple SQL calls rather than a single one. This is because surprisingly, it turns
     * out to be much faster to execute them separately. The MySQL optimizer (and perhaps others) has trouble using the
     * indices in the presence of an OR.
     */
    private void deleteFrameSQL(Frame frame) throws SQLException {
        String deleteFrameText = "DELETE FROM " + _table + " WHERE " + FRAME_COLUMN + " = ?";
        PreparedStatement deleteFrameStmt = getCurrentConnection().getPreparedStatement(deleteFrameText);
        setFrame(deleteFrameStmt, 1, frame);
        executeUpdate(deleteFrameStmt);

        String deleteValueText = "DELETE FROM " + _table;
        deleteValueText += " WHERE " + SHORT_VALUE_COLUMN + " = ? AND " + VALUE_TYPE_COLUMN + " = ?";
        PreparedStatement deleteValueStmt = getCurrentConnection().getPreparedStatement(deleteValueText);
        setShortValue(deleteValueStmt, 1, 2, frame);
        executeUpdate(deleteValueStmt);

        if (frame instanceof Slot) {
            String text = "DELETE FROM " + _table + " WHERE " + SLOT_COLUMN + " = ?";
            PreparedStatement deleteSlotStmt = getCurrentConnection().getPreparedStatement(text);
            setFrame(deleteSlotStmt, 1, frame);
            executeUpdate(deleteSlotStmt);
        } else if (frame instanceof Facet) {
            String text = "DELETE FROM " + _table + " WHERE " + FACET_COLUMN + " = ?";
            PreparedStatement deleteFacetStmt = getCurrentConnection().getPreparedStatement(text);
            setFrame(deleteFacetStmt, 1, frame);
            executeUpdate(deleteFacetStmt);
        }

    }

    public Set<Reference> getReferences(Object value) {
        try {
            return getReferencesSQL(value);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    /*
     * We need to return the short value and test for a real match because MySQL does a case insensitve lookup and this
     * call is supposed to be case sensitive.
     */
    private String _referencesText;

    private Set<Reference> getReferencesSQL(Object value) throws SQLException {
        if (_referencesText == null) {
            _referencesText = "SELECT " + SHORT_VALUE_COLUMN + ", " + FRAME_COLUMN + ", " + FRAME_TYPE_COLUMN + ", "
                    + SLOT_COLUMN + ", " + FACET_COLUMN + ", " + IS_TEMPLATE_COLUMN;
            _referencesText += " FROM " + _table;
            _referencesText += " WHERE " + SHORT_VALUE_COLUMN + " = ?";
            _referencesText += " AND " + VALUE_TYPE_COLUMN + " = ?";
        }

        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_referencesText);
        setShortValue(stmt, 1, 2, value);

        Set<Reference> references = new HashSet<Reference>();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            boolean realMatch = true;
            if (value instanceof String) {
                String returnedValue = rs.getString(1);
                realMatch = value.toString().equals(returnedValue);
            }
            if (realMatch) {
                Frame frame = getFrame(rs, 2, 3);
                Slot slot = getSlot(rs, 4);
                Facet facet = getFacet(rs, 5);
                boolean isTemplate = getIsTemplate(rs, 6);
                references.add(new ReferenceImpl(frame, slot, facet, isTemplate));
            }
        }
        rs.close();
        return references;
    }

    public Set<Reference> getMatchingReferences(String value, int maxMatches) {
        try {
            return getMatchingReferencesSQL(value, maxMatches);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String _matchingReferencesText;

    private Set<Reference> getMatchingReferencesSQL(String value, int maxMatches) throws SQLException {
        if (_matchingReferencesText == null) {
            _matchingReferencesText = "SELECT " + FRAME_COLUMN + ", " + FRAME_TYPE_COLUMN + ", " + SLOT_COLUMN + ", "
                    + FACET_COLUMN;
            _matchingReferencesText += ", " + IS_TEMPLATE_COLUMN;
            _matchingReferencesText += " FROM " + _table;
            _matchingReferencesText += " WHERE " + VALUE_TYPE_COLUMN + " = " + DatabaseUtils.getStringValueType();
            _matchingReferencesText += " AND " + getShortValueMatchColumn();
            _matchingReferencesText += " LIKE '";
        }
        String text = _matchingReferencesText + getMatchString(value) + "' " + getEscapeClause();

        Set<Reference> references = new HashSet<Reference>();
        ResultSet rs = executeQuery(text, maxMatches);
        while (rs.next()) {
            Frame frame = getFrame(rs, 1, 2);
            Slot slot = getSlot(rs, 3);
            Facet facet = getFacet(rs, 4);
            boolean isTemplate = getIsTemplate(rs, 5);
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

    private void removeValueSQL(Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value)
            throws SQLException {
        if (_removeValueText == null) {
            _removeValueText = "DELETE FROM " + _table;
            _removeValueText += " WHERE " + FRAME_COLUMN + " = ?";
            _removeValueText += " AND " + SLOT_COLUMN + " = ?";
            _removeValueText += " AND " + FACET_COLUMN + " = ?";
            _removeValueText += " AND " + IS_TEMPLATE_COLUMN + " = ?";
            _removeValueText += " AND " + SHORT_VALUE_COLUMN + " = ?";
            _removeValueText += " AND " + VALUE_TYPE_COLUMN + " = ?";
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_removeValueText);

        setFrame(stmt, 1, frame);
        setSlot(stmt, 2, slot);
        setFacet(stmt, 3, facet);
        setIsTemplate(stmt, 4, isTemplate);
        setShortValue(stmt, 5, 6, value);

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

    private void removeValuesSQL(Frame frame, Slot slot, Facet facet, boolean isTemplate) throws SQLException {
        if (_removeValuesText == null) {
            _removeValuesText = "DELETE FROM " + _table;
            _removeValuesText += " WHERE " + FRAME_COLUMN + " = ?";
            _removeValuesText += " AND " + SLOT_COLUMN + " = ?";
            _removeValuesText += " AND " + FACET_COLUMN + " = ?";
            _removeValuesText += " AND " + IS_TEMPLATE_COLUMN + " = ?";
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_removeValuesText);

        setFrame(stmt, 1, frame);
        setSlot(stmt, 2, slot);
        setFacet(stmt, 3, facet);
        setIsTemplate(stmt, 4, isTemplate);

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
            _addValuesText += " (" + FRAME_COLUMN + ", " + FRAME_TYPE_COLUMN + ", " + SLOT_COLUMN + ", " + FACET_COLUMN
                    + ", " + IS_TEMPLATE_COLUMN + ", ";
            _addValuesText += VALUE_INDEX_COLUMN + ", " + SHORT_VALUE_COLUMN + ", " + LONG_VALUE_COLUMN + ", "
                    + VALUE_TYPE_COLUMN + ")";
            _addValuesText += " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
        return getCurrentConnection().getPreparedStatement(_addValuesText);
    }

    private void addValuesSQL(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values, int index)
            throws SQLException {
        PreparedStatement stmt = getAddValuesStatement();
        boolean useBatch = values.size() > 1 && getCurrentConnection().supportsBatch();

        setFrame(stmt, 1, 2, frame, _frameFactory);
        setSlot(stmt, 3, slot);
        setFacet(stmt, 4, facet);
        setIsTemplate(stmt, 5, isTemplate);

        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            if (isNullValue(value)) {
                Log.getLogger().warning("Skiping null value");
            } else {
                setValueIndex(stmt, 6, index);
                setValue(stmt, 7, 8, 9, value);
                if (useBatch) {
                    stmt.addBatch();
                } else {
                    executeUpdate(stmt);
                }
                ++index;
            }
        }
        if (useBatch) {
            stmt.executeBatch();
        }
    }

    private static boolean isNullValue(Object o) {
        boolean isNull = o == null;
        if (o instanceof String) {
            String s = (String) o;
            isNull = s.trim().length() == 0;
        }
        return isNull;
    }

    private String _maxIndexText;

    private int getMaxIndex(Frame frame, Slot slot, Facet facet, boolean isTemplate) throws SQLException {
        if (_maxIndexText == null) {
            _maxIndexText = "SELECT MAX(" + VALUE_INDEX_COLUMN + ")";
            _maxIndexText += " FROM " + _table;
            _maxIndexText += " WHERE " + FRAME_COLUMN + " = ?";
            _maxIndexText += " AND " + SLOT_COLUMN + " = ? ";
            _maxIndexText += " AND " + FACET_COLUMN + " = ? ";
            _maxIndexText += " AND " + IS_TEMPLATE_COLUMN + " = ? ";
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_maxIndexText);

        setFrame(stmt, 1, frame);
        setSlot(stmt, 2, slot);
        setFacet(stmt, 3, facet);
        setIsTemplate(stmt, 4, isTemplate);

        ResultSet rs = executeQuery(stmt);
        int index = -1;
        while (rs.next()) {
            index = rs.getInt(1);
            break;
        }
        rs.close();
        return index;
    }

    public void moveValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int from, int index) {
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

    public Set getMatchingFrames(Slot slot, Facet facet, boolean isTemplate, String value, int maxMatches) {
        try {
            return getMatchingFramesSQL(slot, facet, isTemplate, value);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String getShortValueMatchColumn() throws SQLException {
        String matchColumn;
        if (getCurrentConnection().supportsCaseInsensitiveMatches()) {
            matchColumn = SHORT_VALUE_COLUMN;
        } else {
            matchColumn = "LOWER(" + SHORT_VALUE_COLUMN + ")";
        }
        return matchColumn;
    }

    private Set getMatchingFramesSQL(Slot slot, Facet facet, boolean isTemplate, String value) throws SQLException {
        String text = "SELECT " + FRAME_COLUMN + ", " + FRAME_TYPE_COLUMN + ", " + SLOT_COLUMN + ", " + FACET_COLUMN
                + ", " + IS_TEMPLATE_COLUMN;
        text += " FROM " + _table;
        text += " WHERE " + VALUE_TYPE_COLUMN + " = " + DatabaseUtils.getStringValueType();
        text += " AND " + getShortValueMatchColumn() + " LIKE '" + getMatchString(value) + "' " + getEscapeClause();

        Set results = new HashSet();
        ResultSet rs = executeQuery(text);
        while (rs.next()) {
            Frame frame = getFrame(rs, 1, 2);
            int returnedSlot = rs.getInt(3);
            int returnedFacet = rs.getInt(4);
            boolean returnedIsTemplate = rs.getBoolean(5);
            if (equals(returnedSlot, slot) && equals(returnedFacet, facet) && returnedIsTemplate == isTemplate) {
                results.add(frame);
            }
        }
        rs.close();
        return results;
    }

    private static boolean equals(int frameIDValue, Frame frame) {
        int value = (frame == null) ? FrameID.NULL_FRAME_ID_VALUE : getValue(frame.getFrameID());
        return frameIDValue == value;
    }

    private String getMatchString(String value) throws SQLException {
        return DatabaseUtils.getMatchString(value, getCurrentConnection().getEscapeCharacter());
    }

    private String getEscapeClause() throws SQLException {
        return getCurrentConnection().getEscapeClause();
    }

    public Set getFrames(Slot slot, Facet facet, boolean isTemplate, Object value) {
        try {
            return getFramesSQL(slot, facet, isTemplate, value);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    /*
     * We need to return the value and test for a "real match" because MySQL will match in a case insensitive way. This
     * really messes up our frame lookup by name because different types of frames often only differ by the case of
     * their names.
     */
    private String _framesText;

    private Set getFramesSQL(Slot slot, Facet facet, boolean isTemplate, Object value) throws SQLException {
        if (_framesText == null) {
            _framesText = "SELECT " + SHORT_VALUE_COLUMN + ", " + FRAME_COLUMN + ", " + FRAME_TYPE_COLUMN + " FROM "
                    + _table;
            _framesText += " WHERE " + SLOT_COLUMN + " = ?";
            _framesText += " AND " + FACET_COLUMN + " = ?";
            _framesText += " AND " + IS_TEMPLATE_COLUMN + " = ?";
            _framesText += " AND " + SHORT_VALUE_COLUMN + " = ?";
            _framesText += " AND " + VALUE_TYPE_COLUMN + " = ?";
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_framesText);

        setSlot(stmt, 1, slot);
        setFacet(stmt, 2, facet);
        setIsTemplate(stmt, 3, isTemplate);
        setShortValue(stmt, 4, 5, value);

        Set results = new HashSet();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            boolean isRealMatch = true;
            if (value instanceof String) {
                String returnedValue = rs.getString(1);
                isRealMatch = value.equals(returnedValue);
            }
            if (isRealMatch) {
                Frame frame = getFrame(rs, 2, 3);
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

    private Set getFramesWithAnyValueSQL(Slot slot, Facet facet, boolean isTemplate) throws SQLException {
        if (_framesWithAnyValueText == null) {
            _framesWithAnyValueText = "SELECT " + FRAME_COLUMN + ", " + FRAME_TYPE_COLUMN + " FROM " + _table;
            _framesWithAnyValueText += " WHERE " + SLOT_COLUMN + " = ?";
            _framesWithAnyValueText += " AND " + FACET_COLUMN + " = ?";
            _framesWithAnyValueText += " AND " + IS_TEMPLATE_COLUMN + " = ?";
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_framesWithAnyValueText);

        setSlot(stmt, 1, slot);
        setFacet(stmt, 2, facet);
        setIsTemplate(stmt, 3, isTemplate);

        Set results = new HashSet();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            Frame frame = getFrame(rs, 1, 2);
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

    private List getValuesSQL(Frame frame, Slot slot, Facet facet, boolean isTemplate) throws SQLException {
        if (_valuesText == null) {
            _valuesText = "SELECT " + SHORT_VALUE_COLUMN + ", " + VALUE_TYPE_COLUMN + ", " + VALUE_INDEX_COLUMN;
            _valuesText += " FROM " + _table;
            _valuesText += " WHERE " + FRAME_COLUMN + " = ?";
            _valuesText += " AND " + SLOT_COLUMN + " = ?";
            _valuesText += " AND " + FACET_COLUMN + " = ?";
            _valuesText += " AND " + IS_TEMPLATE_COLUMN + " = ?";
            _valuesText += " ORDER BY " + VALUE_INDEX_COLUMN;
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_valuesText);

        setFrame(stmt, 1, frame);
        setSlot(stmt, 2, slot);
        setFacet(stmt, 3, facet);
        setIsTemplate(stmt, 4, isTemplate);

        List values = new ArrayList();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            Object value = getShortValue(rs, 1, 2);
            if (value == null) {
                int index = getIndex(rs, 3);
                value = getLongValue(frame, slot, facet, isTemplate, index);
                if (value == null) {
                    value = "";
                }
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

    private int getValuesCountSQL(Frame frame, Slot slot, Facet facet, boolean isTemplate) throws SQLException {
        if (_valuesCountText == null) {
            _valuesCountText = "SELECT COUNT(*) ";
            _valuesCountText += " FROM " + _table;
            _valuesCountText += " WHERE " + FRAME_COLUMN + " = ?";
            _valuesCountText += " AND " + SLOT_COLUMN + " = ?";
            _valuesCountText += " AND " + FACET_COLUMN + " = ?";
            _valuesCountText += " AND " + IS_TEMPLATE_COLUMN + " = ?";
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_valuesCountText);

        setFrame(stmt, 1, frame);
        setSlot(stmt, 2, slot);
        setFacet(stmt, 3, facet);
        setIsTemplate(stmt, 4, isTemplate);

        ResultSet rs = executeQuery(stmt);
        int count = 0;
        while (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        return count;
    }

    public CacheMap getFrameValues() {
        try {
            return getFrameValuesSQL();
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    public Map getFrameValues(Frame frame) {
        try {
            // Log.trace("call=" + ++call, this, "getFrameValues",
            // frame.getFrameID());
            return getFrameValuesSQL(frame);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private static int getValue(FrameID id) {
        return DatabaseUtils.getValue(id);
    }

    private String _frameValuesText;

    private Map getFrameValuesSQL(Frame frame) throws SQLException {
        if (_frameValuesText == null) {
            /*
             * We only select the frame for performance reasons. The order by clause can use an index if the frame is
             * selected.
             */
            _frameValuesText = "SELECT " + FRAME_COLUMN + ", " + SLOT_COLUMN + ", ";
            _frameValuesText += FACET_COLUMN + ", " + IS_TEMPLATE_COLUMN + ", ";
            _frameValuesText += SHORT_VALUE_COLUMN + ", " + VALUE_TYPE_COLUMN + ", " + VALUE_INDEX_COLUMN;
            _frameValuesText += " FROM " + _table;
            _frameValuesText += " WHERE " + FRAME_COLUMN + " = ?";
            _frameValuesText += " AND " + SLOT_COLUMN + " <> " + getValue(Model.SlotID.DIRECT_INSTANCES);
            _frameValuesText += " ORDER BY " + FRAME_COLUMN + ", " + SLOT_COLUMN + ", " + FACET_COLUMN + ", "
                    + IS_TEMPLATE_COLUMN + ", " + VALUE_INDEX_COLUMN;
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_frameValuesText);

        setFrame(stmt, 1, frame);

        Map sftToValueMap = new HashMap();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            // Ignore the returned frame
            Slot slot = getSlot(rs, 2);
            Facet facet = getFacet(rs, 3);
            boolean isTemplate = getIsTemplate(rs, 4);
            Object value = getShortValue(rs, 5, 6);
            if (value == null) {
                int index = getIndex(rs, 7);
                value = getLongValue(frame, slot, facet, isTemplate, index);
                if (value == null) {
                    value = "";
                }
            }
            addToMap(sftToValueMap, slot, facet, isTemplate, value);
        }
        rs.close();
        return sftToValueMap;
    }

    private String _allFrameValuesText;

    private CacheMap getFrameValuesSQL() throws SQLException {
        if (_allFrameValuesText == null) {
            _allFrameValuesText = "SELECT " + FRAME_COLUMN + ", " + FRAME_TYPE_COLUMN;
            _allFrameValuesText += ", " + SLOT_COLUMN + ", " + FACET_COLUMN + ", " + IS_TEMPLATE_COLUMN;
            _allFrameValuesText += ", " + SHORT_VALUE_COLUMN + ", " + VALUE_TYPE_COLUMN + ", " + VALUE_INDEX_COLUMN;
            _allFrameValuesText += ", " + LONG_VALUE_COLUMN;
            _allFrameValuesText += " FROM " + _table;
            _allFrameValuesText += " ORDER BY " + SLOT_COLUMN + ", " + FACET_COLUMN + ", " + IS_TEMPLATE_COLUMN + ", "
                    + VALUE_INDEX_COLUMN;
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_allFrameValuesText);

        CacheMap frameToSftToValueMap = new CacheMap();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            Frame frame = getFrame(rs, 1, 2);
            Slot slot = getSlot(rs, 3);
            Facet facet = getFacet(rs, 4);
            boolean isTemplate = getIsTemplate(rs, 5);
            Object value = getShortValue(rs, 6, 7);
            if (value == null) {
                // int index = getIndex(rs, 8);
                // value = getLongValue(frame, slot, facet, isTemplate, index);
                value = getLongValue(rs, 9);
                if (value == null) {
                    value = "";
                }
            }
            addToMap(frameToSftToValueMap, frame, slot, facet, isTemplate, value);
        }
        rs.close();
        return frameToSftToValueMap;
    }

    private static void addToMap(Map map, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Sft sft = new Sft(slot, facet, isTemplate);
        Collection values = (Collection) map.get(sft);
        if (values == null) {
            values = new ArrayList();
            map.put(sft, values);
        }
        values.add(value);
    }

    private static void addToMap(CacheMap map, Frame frame, Slot slot, Facet facet, boolean isTemplate, Object value) {
        Map sftToValueMap = (Map) map.get(frame);
        if (sftToValueMap == null) {
            sftToValueMap = createInitialMap(frame);
            map.put(frame, sftToValueMap);
        }
        addToMap(sftToValueMap, slot, facet, isTemplate, value);
    }

    private static Map createInitialMap(Frame frame) {
        Map map = new HashMap();
        if (frame instanceof Cls) {
            Slot directInstancesSlot = frame.getKnowledgeBase().getSystemFrames().getDirectInstancesSlot();
            map.put(new Sft(directInstancesSlot, null, false), new ArrayList());
            // map.put(new Sft(directSubclassesSlot, null, false), new
            // ArrayList());
        }
        return map;
    }

    private String _longValueText;

    private Object getLongValue(Frame frame, Slot slot, Facet facet, boolean isTemplate, int index) throws SQLException {
        if (_longValueText == null) {
            _longValueText = "SELECT " + LONG_VALUE_COLUMN;
            _longValueText += " FROM " + _table;
            _longValueText += " WHERE " + FRAME_COLUMN + " = ?";
            _longValueText += " AND " + SLOT_COLUMN + " = ?";
            _longValueText += " AND " + FACET_COLUMN + " = ?";
            _longValueText += " AND " + IS_TEMPLATE_COLUMN + " = ?";
            _longValueText += " AND " + VALUE_INDEX_COLUMN + " = ?";
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_longValueText);

        setFrame(stmt, 1, frame);
        setSlot(stmt, 2, slot);
        setFacet(stmt, 3, facet);
        setIsTemplate(stmt, 4, isTemplate);
        setValueIndex(stmt, 5, index);

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
    private static void setFrame(PreparedStatement stmt, int index, Frame frame) throws SQLException {
        DatabaseUtils.setFrame(stmt, index, frame);
    }

    private static void setFrame(PreparedStatement stmt, int frameIndex, int valueTypeIndex, Frame frame,
            FrameFactory factory) throws SQLException {
        DatabaseUtils.setFrame(stmt, frameIndex, valueTypeIndex, frame, factory);
    }

    private static void setSlot(PreparedStatement stmt, int index, Slot slot) throws SQLException {
        DatabaseUtils.setSlot(stmt, index, slot);
    }

    private static void setFacet(PreparedStatement stmt, int index, Facet facet) throws SQLException {
        DatabaseUtils.setFacet(stmt, index, facet);
    }

    private static void setIsTemplate(PreparedStatement stmt, int index, boolean isTemplate) throws SQLException {
        DatabaseUtils.setIsTemplate(stmt, index, isTemplate);
    }

    private void setShortValue(PreparedStatement stmt, int valueIndex, int valueTypeIndex, Object value)
            throws SQLException {
        DatabaseUtils.setShortValue(stmt, valueIndex, valueTypeIndex, value, _frameFactory);
    }

    private void setValue(PreparedStatement stmt, int shortIndex, int longIndex, int valueTypeIndex, Object value)
            throws SQLException {
        DatabaseUtils.setValue(stmt, shortIndex, longIndex, valueTypeIndex, value, getCurrentConnection()
                .getMaxVarcharSize(), _frameFactory);
    }

    private static void setValueIndex(PreparedStatement stmt, int index, int valueIndex) throws SQLException {
        DatabaseUtils.setValueIndex(stmt, index, valueIndex);
    }

    private Frame getFrame(ResultSet rs, int frameIndex, int typeIndex) throws SQLException {
        return DatabaseUtils.getFrame(rs, 
                                      frameIndex, typeIndex,
                                      _frameFactory, projectId,
                                      _isInclude);
    }

    private Slot getSlot(ResultSet rs, int index) throws SQLException {
        return DatabaseUtils.getSlot(rs, index, _frameFactory, projectId, _isInclude);
    }

    private Facet getFacet(ResultSet rs, int index) throws SQLException {
        return DatabaseUtils.getFacet(rs, index, _frameFactory, projectId, _isInclude);
    }

    private static int getIndex(ResultSet rs, int index) throws SQLException {
        return DatabaseUtils.getIndex(rs, index);
    }

    private static boolean getIsTemplate(ResultSet rs, int index) throws SQLException {
        return DatabaseUtils.getIsTemplate(rs, index);
    }

    private Object getShortValue(ResultSet rs, int index, int valueTypeIndex) throws SQLException {
      return DatabaseUtils.getShortValue(rs, 
                                         index, valueTypeIndex, 
                                         _frameFactory, projectId, 
                                         _isInclude);
    }

    private static Object getLongValue(ResultSet rs, int index) throws SQLException {
        return DatabaseUtils.getLongValue(rs, index);
    }

    private PreparedStatement _addValuesBatchStmt;
    private int _addValuesBatchCounter;
    private static final int MAX_BATCH = 1000;

    protected void beginBatch() throws SQLException {
        _addValuesBatchStmt = getAddValuesStatement();
        getCurrentConnection().setAutoCommit(false);
        _addValuesBatchCounter = 0;
    }

    protected void endBatch() throws SQLException {
        if (_addValuesBatchCounter > 0) {
            executeBatch();
        }
        _addValuesBatchStmt = null;
        getCurrentConnection().setAutoCommit(true);
    }

    public void overwriteKB(KnowledgeBase kb, 
                                boolean saveFrames) throws SQLException {
        slotToFacetsCacheMap.clear();
        ensureEmptyTableExists();
        boolean wasCaching = kb.setCallCachingEnabled(false);
        if (saveFrames) {
          beginBatch();
          saveFrames(kb);
          endBatch();
        }
        createIndices();
        kb.setCallCachingEnabled(wasCaching);
    }

    private static final Map slotToFacetsCacheMap = new HashMap();

    protected void saveFrames(KnowledgeBase kb) throws SQLException {
        nFrames = kb.getFrameCount();
        loopcount = 0;
        previousTime = System.currentTimeMillis();

        if (nFrames > LOOP_SIZE) {
            Log.getLogger().info("Getting " + nFrames + " frames, please be patient, " + new Date());
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
            //ESCA-JAVA0284 
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

    protected void saveValues(Frame frame, Slot slot, Facet facet, boolean isTemplate, Collection values)
            throws SQLException {
        setFrame(_addValuesBatchStmt, 1, 2, frame, _frameFactory);
        setSlot(_addValuesBatchStmt, 3, slot);
        setFacet(_addValuesBatchStmt, 4, facet);
        setIsTemplate(_addValuesBatchStmt, 5, isTemplate);
        boolean locationIsSystem = locationIsSystem(frame, slot, facet);
        int index = 0;
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object value = i.next();
            if (!(locationIsSystem && valueIsSystem(value))) {
                setValueIndex(_addValuesBatchStmt, 6, index);
                setValue(_addValuesBatchStmt, 7, 8, 9, value);
                addBatch();
                ++index;
            }
        }
    }

    private static boolean locationIsSystem(Frame frame, Slot slot, Facet facet) {
        boolean isSystem = frame.isSystem() && slot.isSystem();
        if (isSystem && facet != null) {
            isSystem = facet.isSystem();
        }
        return isSystem;
    }

    private static boolean valueIsSystem(Object value) {
        boolean valueIsSystem = true;
        if (value instanceof Frame) {
            Frame frame = (Frame) value;
            valueIsSystem = frame.isSystem();
        }
        return valueIsSystem;
    }

    private void addBatch() throws SQLException {
        _addValuesBatchStmt.addBatch();
        ++_addValuesBatchCounter;
        if (_addValuesBatchCounter == MAX_BATCH) {
            executeBatch();
        }
    }

    private void executeBatch() throws SQLException {
        _addValuesBatchStmt.executeBatch();
        _addValuesBatchCounter = 0;
        getCurrentConnection().commit();
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

    private static Collection getTemplateFacets(Cls cls, Slot slot) {
        Collection facets = (Collection) slotToFacetsCacheMap.get(slot);
        if (facets == null) {
            facets = cls.getTemplateFacets(slot);
            slotToFacetsCacheMap.put(slot, facets);
        }
        return facets;
    }

    public boolean beginTransaction(String name) {
        checkModifiability();
        try {
            return getCurrentConnection().beginTransaction();
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }

    }

    public boolean commitTransaction() {
        checkModifiability();
        try {
            return getCurrentConnection().commitTransaction();
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }

    }

    public boolean rollbackTransaction() {
        checkModifiability();
        try {
            return getCurrentConnection().rollbackTransaction();
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }

    }

    private static void checkModifiability() {
        if (!_isModifiable) {
            throw new IllegalStateException("Database is not modifiable");
        }
    }

    public void replaceFrame(Frame frame) {
        try {
            int newTypeId = DatabaseUtils.valueType(frame, _frameFactory);
            replaceFrameTypeSQL(frame, newTypeId);
            replaceValueTypeSQL(frame, newTypeId);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String queryFrameTypeIdCommand;

    private int getFrameTypeIdSQL(Frame frame) {
        try {
            if (queryFrameTypeIdCommand == null) {
                queryFrameTypeIdCommand = "SELECT " + FRAME_TYPE_COLUMN;
                queryFrameTypeIdCommand += " FROM " + _table;
                queryFrameTypeIdCommand += " WHERE " + FRAME_COLUMN + " = ?"; // Fixed by Holger
            }
            int id = -1;
            PreparedStatement stmt = getCurrentConnection().getPreparedStatement(queryFrameTypeIdCommand);
            setFrame(stmt, 1, frame);
            stmt.setMaxRows(1);
            ResultSet rs = executeQuery(stmt);
            while (rs.next()) {
                id = rs.getInt(1);
                break;
            }
            rs.close();
            return id;
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
    }

    private String replaceFrameTypeCommand;

    private void replaceFrameTypeSQL(Frame frame, int newTypeId) throws SQLException {
        if (replaceFrameTypeCommand == null) {
            replaceFrameTypeCommand = "UPDATE " + _table + " SET " + FRAME_TYPE_COLUMN + " = ?";
            replaceFrameTypeCommand += " WHERE " + FRAME_COLUMN + " = ?";
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(replaceFrameTypeCommand);

        DatabaseUtils.setValueType(stmt, 1, newTypeId);
        DatabaseUtils.setFrame(stmt, 2, frame);
        executeUpdate(stmt);
    }

    private String replaceValueTypeCommand;

    private void replaceValueTypeSQL(Frame frame, int newTypeId) throws SQLException {
        if (replaceValueTypeCommand == null) {
            replaceValueTypeCommand = "UPDATE " + _table + " SET " + VALUE_TYPE_COLUMN + " = ?";
            replaceValueTypeCommand += " WHERE " + SHORT_VALUE_COLUMN + " = ?";
            replaceValueTypeCommand += " AND " + VALUE_TYPE_COLUMN + " > 4";
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(replaceValueTypeCommand);

        DatabaseUtils.setValueType(stmt, 1, newTypeId);
        DatabaseUtils.setFrame(stmt, 2, frame);
        // DatabaseUtils.setValueType(stmt, 3, currentTypeId);
        executeUpdate(stmt);
    }

    public String toString() {
        return "DatabaseFrameDb";
    }

    public int getClsCount() {
        return countUniqueFrames(_frameFactory.getClsJavaClassIds());
    }

    public int getSlotCount() {
        return countUniqueFrames(_frameFactory.getSlotJavaClassIds());
    }

    public int getFacetCount() {
        return countUniqueFrames(_frameFactory.getFacetJavaClassIds());
    }

    public int getFrameCount() {
        return countUniqueFrames();
    }

    public Set getFrames() {
        if (log.isLoggable(Level.FINE)) {
          log.fine("Calling get frames...");
        }
        Set frames = null;
        try {
            frames = getFramesSQL();
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
        return frames;
    }

    public int getSimpleInstanceCount() {
        return countUniqueFrames(_frameFactory.getSimpleInstanceJavaClassIds());
    }

    private int countUniqueFrames(Collection types) {
        int count = -1;
        try {
            count = countFramesSQL(types);
        } catch (SQLException e) {
            throw createRuntimeException(e);
        }
        return count;
    }

    private int countFramesSQL(Collection types) throws SQLException {
        StringBuffer command = new StringBuffer();
        command.append("SELECT COUNT(*) FROM " + _table);
        command.append(" WHERE " + SLOT_COLUMN + " = " + getValue(Model.SlotID.NAME));
        command.append(" AND " + FACET_COLUMN + " = " + FrameID.NULL_FRAME_ID_VALUE);
        command.append(" AND " + IS_TEMPLATE_COLUMN + " = ?");
        command.append(" AND (");
        boolean isFirst = true;
        Iterator i = types.iterator();
        while (i.hasNext()) {
            Integer intValue = (Integer) i.next();
            if (isFirst) {
                isFirst = false;
            } else {
                command.append(" OR ");
            }
            command.append(FRAME_TYPE_COLUMN + " = ");
            command.append(intValue.intValue());
        }
        command.append(")");
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(command.toString());
        setIsTemplate(stmt, 1, false);
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
            _countFramesText += " WHERE " + SLOT_COLUMN + " = " + getValue(Model.SlotID.NAME);
            _countFramesText += " AND " + FACET_COLUMN + " = " + FrameID.NULL_FRAME_ID_VALUE;
            _countFramesText += " AND " + IS_TEMPLATE_COLUMN + " = ?";
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_countFramesText);
        setIsTemplate(stmt, 1, false);

        int count = -1;
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        return count;
    }

    private String _getFramesText;

    private Set getFramesSQL() throws SQLException {
        if (_getFramesText == null) {
            _getFramesText = "SELECT DISTINCT " + FRAME_COLUMN + ", " + FRAME_TYPE_COLUMN;
            _getFramesText += " FROM " + _table;
        }
        PreparedStatement stmt = getCurrentConnection().getPreparedStatement(_getFramesText);

        Set frames = new HashSet();
        ResultSet rs = executeQuery(stmt);
        while (rs.next()) {
            Frame frame = getFrame(rs, 1, 2);
            frames.add(frame);
        }
        rs.close();
        return frames;
    }

    private String _getFrameFromIdText;

    public Frame getFrame(FrameID id) {
        // return object
        Frame returnFrame = null;

        // define prepared statement text if not already defined
        if (_getFrameFromIdText == null) {
            _getFrameFromIdText = "SELECT DISTINCT " + FRAME_COLUMN + ", " + FRAME_TYPE_COLUMN + " FROM " + _table
                    + " WHERE " + FRAME_COLUMN + " = ?";
        }
        try {
            // if the id is null, we cannot continue (method will return null)
            if (id != null) {
                // get the prepared statement and set the id value
                PreparedStatement getFrameStmt = getCurrentConnection().getPreparedStatement(_getFrameFromIdText);
                getFrameStmt.setInt(1, id.getLocalPart());

                // execute the query and retrieve the result frame
                ResultSet rs = executeQuery(getFrameStmt);
                while (rs.next()) {
                    returnFrame = getFrame(rs, 1, 2);
                }
                rs.close();
            }
        } catch (SQLException e) {
            createRuntimeException(e);
        }

        return returnFrame;
    }


    private int getNextFrameID() {
        try {
            int maxID = 0;
            String text = "SELECT MAX(" + FRAME_COLUMN + ") FROM " + _table;
            ResultSet rs = executeQuery(text);
            while (rs.next()) {
                maxID = rs.getInt(1);
            }
            lastReturnedFrameID = Math.max(maxID + 1, lastReturnedFrameID + 1);
        } catch (SQLException e) {
            createRuntimeException(e);
        }
        return lastReturnedFrameID;
    }

    public NarrowFrameStore getDelegate() {
        throw new UnsupportedOperationException();
    }
}