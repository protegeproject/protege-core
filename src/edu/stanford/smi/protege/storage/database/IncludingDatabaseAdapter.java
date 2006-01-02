package edu.stanford.smi.protege.storage.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameFactory;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.IncludingKBAdapter;
import edu.stanford.smi.protege.model.framestore.IncludingKBSupport;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.util.Log;

public class IncludingDatabaseAdapter extends IncludingKBAdapter
                                      implements IncludingKBSupport {
  private Logger log = Log.getLogger(IncludingDatabaseAdapter.class);
  
  public enum Column {
    local_frame_id, frame_name
  };
  
  private NarrowFrameStore delegate;
  private DatabaseFrameDb frameDb;
  
  private String tableName;
  
  public IncludingDatabaseAdapter(DatabaseFrameDb nfs) {
    super(nfs);
    delegate = nfs;
    frameDb  = nfs;
  }
  
  public IncludingDatabaseAdapter(ValueCachingNarrowFrameStore vcnfs) {
    super(vcnfs);
    delegate = vcnfs;
    frameDb  = vcnfs.getDelegate();
  }
  
  public void initialize(FrameFactory factory, 
                         String driver, 
                         String url, String user, String pass, String table) {
    frameDb.initialize(factory, driver, url, user, pass, table);
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
  
  private void initializeInheritanceTable() throws SQLException {
    tableName = frameDb.getTableName() + "_INHERITED";
    String cmd = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                  Column.local_frame_id + " INT, " + Column.frame_name + " TEXT)";
    execute(cmd);
  }

  @Override
  public boolean isLocalFrameInherited(Frame frame) {
    try {
      Statement statement = frameDb.getCurrentConnection().getStatement();
      String cmd = "SELECT * FROM " + tableName + " WHERE " 
      + Column.local_frame_id + " = " + frame.getFrameID().getLocalPart();
      ResultSet rs = executeQuery(cmd);
      while (rs.next()) {
        rs.close();
        return true;
      }
      rs.close();
      return false;
    } catch (SQLException sqle) { // TOOD - should throw IOException
                                  // requires interface change.
      throw new RuntimeException(sqle);
    }
  }
  
  public Frame mapLocalFrame(Frame frame) {
    if (log.isLoggable(Level.FINEST) && frame != null) {
      log.finest("(" + memoryProjectId + ") Mapping local frame with id " + frame.getFrameID());
    }
    try {
      if (frame == null  || frame.getFrameID().isSystem()) {
        return frame;
      }
      String name = null;
      SelectStatement<Column> select = new SelectStatement<Column>(EnumSet.of(Column.frame_name));
      ResultSet rs = executeQuery(select.toString() + " FROM " + tableName + " WHERE " + 
          Column.local_frame_id + " = " + frame.getFrameID().getLocalPart());
      while (rs.next()) {
        name = rs.getString(select.rsIndex(Column.frame_name));
        rs.close();
        Frame globalFrame = getInheritedFrames().getInheritedFrame(name);
        if (log.isLoggable(Level.FINEST)) {
          log.finest("returning global frame with id = " + globalFrame.getFrameID());
        }
        return globalFrame;
      }
      rs.close();
      if (log.isLoggable(Level.FINEST)) {
        log.finest("Global frame = local frame - no mapping found.");
      }
      return frame;
    } catch (SQLException sqle) {
      throw new RuntimeException(sqle);
    }
  }
  
  protected Frame createLocalFrame(Frame global, String name) {
    try {
      Frame localFrame = super.createLocalFrame(global, name);
      execute("INSERT INTO " + tableName + 
              " (" + Column.local_frame_id + ", " + Column.frame_name + ") VALUES (" +
              localFrame.getFrameID().getLocalPart() + ", \"" + name + "\")");
      return localFrame;
    } catch (SQLException sqle) { // TODO - should be IOException - requires interface change.
      throw new RuntimeException(sqle);
    }
  }
  
  public boolean execute(String cmd) throws SQLException {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Executing database command = " + cmd);
    }
    Statement statement = frameDb.getCurrentConnection().getStatement();
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
  
  public void saveKnowledgeBase(KnowledgeBase kb) throws SQLException {
    execute("DROP TABLE IF EXISTS " + tableName);
    initializeInheritanceTable();
    frameDb.saveKnowledgeBase(kb);
  }
  

}
