package edu.stanford.smi.protege.storage.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.framestore.IncludingKBAdapter;
import edu.stanford.smi.protege.model.framestore.IncludingKBSupport;
import edu.stanford.smi.protege.util.Log;

public class IncludingDatabaseAdapter extends IncludingKBAdapter
                                      implements IncludingKBSupport {
  private static Logger log = Log.getLogger(IncludingDatabaseAdapter.class);
  
  public enum Column {
    local_frame_id, frame_name
  };
  
  private ValueCachingNarrowFrameStore delegate;
  private DatabaseFrameDb frameDb;
  private Map<Integer, String> includedIdCache = new HashMap<Integer, String>();
  
  private String tableName;
  
  
  /**
   * This is the main constructor for the IncludingDatabaseAdapter.  Currently the Narrow 
   * Frame Stores have the slightly odd configuration:
   * 
   *    IncludingDatabaseAdapter -> ValueCachingNarrowFrameStore -> DatabaseFrameDb
   * 
   * This is a little odd but it works for now.  A more natural arrangement would be
   * 
   *    ValueCachingNarrowFrameStore -> IncludingDatabaseAdapter -> DatabaseFrameDb
   *    
   * But this requires some mods to the ValueCachingNarrowFrameStore which haven't been implemented yet.
   */
  public IncludingDatabaseAdapter(ValueCachingNarrowFrameStore vcnfs) {
      super(vcnfs);
      delegate = vcnfs;
      frameDb  = vcnfs.getDelegate();
  }
  

  public void initialize(FrameFactory factory, 
                         String driver, 
                         String url, String user, String pass, String table,
                         boolean isInclude) {
    frameDb.initialize(factory, driver, url, user, pass, table, isInclude);
    super.initialize(factory);
    try {
      initializeInheritanceTable();
    } catch (SQLException e) {
      Log.getLogger().severe("Exception found " + e + " (use fine logging for more info)");
      if (Log.getLogger().isLoggable(Level.FINE)) {
        log.log(Level.FINE, "Exception found ", e);
      }
    }
  }
  
  
  protected void initializeInheritanceTable() throws SQLException {
    tableName = getTableName(frameDb.getTableName());
    initializeInheritanceTable(tableName, frameDb.getCurrentConnection());
  }
  
  protected static String getTableName(String frameTable) {
    return frameTable + "_INCLUDES";
  }
  
  protected static void initializeInheritanceTable(String tableName,
                                                   RobustConnection connection)  {
    String cmd = "CREATE TABLE " + tableName + " (" +
                  Column.local_frame_id + " INT UNIQUE PRIMARY KEY, " 
                  + Column.frame_name + " TEXT)";
    try {
      execute(cmd, connection);
    } catch (SQLException sqle) {
        Log.getLogger().config("Table " + tableName + "already exists");
        if (log.isLoggable(Level.FINE)) {
          log.log(Level.FINE, "Exception caught - probable cause - table already exists", sqle);
        }
    }
  }

  @Override
  public boolean isLocalFrameIncluded(Frame frame) {
    return getLocalIncludedName(frame) != null;
  }
  
  private String getLocalIncludedName(Frame frame) {
    Integer localId = new Integer(frame.getFrameID().getLocalPart());
    String name = includedIdCache.get(localId);
    if (name != null) {
      return name;
    }
    try {
      Statement statement = frameDb.getCurrentConnection().getStatement();
      SelectStatement<Column> s = new SelectStatement<Column>(EnumSet.of(Column.frame_name));
      String cmd = s.toString() + " FROM " + tableName + " WHERE " 
      + Column.local_frame_id + " = " + localId.intValue();
      ResultSet rs = executeQuery(cmd);
      while (rs.next()) {
        name = rs.getString(s.rsIndex(Column.frame_name));
        includedIdCache.put(localId, name);
        rs.close();
        return name;
      }
      rs.close();
      return null;
    } catch (SQLException sqle) { // TOOD - should throw IOException
                                  // requires interface change.
      throw new RuntimeException(sqle);
    }
  }
  
  public Frame mapLocalFrame(Frame frame) {
    if (log.isLoggable(Level.FINEST) && frame != null) {
      log.finest("(" + memoryProjectId + ") Mapping local frame with id " + frame.getFrameID());
    }
    if (frame == null  || frame.getFrameID().isSystem()) {
      return frame;
    }
    String name = getLocalIncludedName(frame);
    if (name != null) {
      Frame globalFrame = getIncludedFrames().getInheritedFrame(name);
      if (log.isLoggable(Level.FINEST)) {
        log.finest("returning global frame with id = " + globalFrame.getFrameID());
      }
      return globalFrame;
    }
    if (log.isLoggable(Level.FINEST)) {
      log.finest("Global frame = local frame - no mapping found.");
    }
    return frame;
  }
  
  protected Frame createLocalFrame(Frame global, String name) {
    try {
      Frame localFrame = super.createLocalFrame(global, name);
      String oldName = getLocalIncludedName(localFrame);
      int localId = localFrame.getFrameID().getLocalPart();

      if (oldName == null) {
        execute("INSERT INTO " + tableName + 
                " (" + Column.local_frame_id + ", " + Column.frame_name + ") VALUES (" +
                localId + ", \"" + name + "\")");
        includedIdCache.put(new Integer(localId), name);
      } else if (!oldName.equals(name)) {
        Log.getLogger().severe("Name mismatch on included frames");
      }    
      return localFrame;
    } catch (SQLException sqle) { // TODO - should be IOException - requires interface change.
      throw new RuntimeException(sqle);
    }
  }
  
  public boolean execute(String cmd) throws SQLException {
    return execute(cmd, frameDb.getCurrentConnection());
  }
  
  public static boolean execute(String cmd, RobustConnection connection) throws SQLException {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Executing database command = " + cmd);
    }
    Statement statement = connection.getStatement();
    return statement.execute(cmd);
  }
  
  public ResultSet executeQuery(String cmd) throws SQLException {
    if (log.isLoggable(Level.FINER)) {
      log.finer("Executing database query = " + cmd);
    }
    Statement statement = frameDb.getCurrentConnection().getStatement();
    ResultSet rs = statement.executeQuery(cmd);
    return rs;
  }
  
  public int getFrameCount() {
    int delegateCount = frameDb.getFrameCount();
    if (log.isLoggable(Level.FINE)) {
      log.fine("found " + delegateCount + " total frames");
    }
    int included = 0;
    try {
      included = getIncludedFrameCount();
    } catch (SQLException sqle) {
      Log.getLogger().log(Level.WARNING, "Exception caught", sqle);
    }
    return delegateCount - included;
  }
    
    public int getClsCount() {
      int delegateCount = frameDb.getClsCount();
      if (log.isLoggable(Level.FINE)) {
        log.fine("found " + delegateCount + " total frames");
      }
      int included = 0;
      try {
        included = getIncludedFrameCount();
      } catch (SQLException sqle) {
        Log.getLogger().log(Level.WARNING, "Exception caught", sqle);
      }
      return delegateCount - included;  
    }
    
    public int getSlotCount() {
      return frameDb.getSlotCount();
    }
    
    public int getFacetCount() {
      return frameDb.getFacetCount();
    }
    
    public int getIncludedFrameCount() throws SQLException {
      ResultSet rs = executeQuery("SELECT COUNT(" + Column.local_frame_id + ") FROM " + tableName
          + " INNER JOIN " + frameDb.getTable() + " ON " +
          Column.local_frame_id + " = " + DatabaseFrameDb.Column.frame);
      int included = 0;
      if (rs.next()) {
        included = rs.getInt(1);
      }
      rs.close();
      return included;
    }    
}
  
  

