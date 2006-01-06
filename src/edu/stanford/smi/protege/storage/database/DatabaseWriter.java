package edu.stanford.smi.protege.storage.database;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.storage.database.IncludingDatabaseAdapter.Column;
import edu.stanford.smi.protege.util.Log;

public class DatabaseWriter {
  private static Logger log = Log.getLogger(DatabaseWriter.class);
  
  private static final int LOOP_SIZE = 1000;
  private long previousTime;
  
  private String tableName;
  private KnowledgeBase inputKb;
  private DatabaseFrameDb frameDb;
  private Collection<Frame> frames;
  private Collection<FrameID> alreadySeen = new HashSet<FrameID>();
  
  public DatabaseWriter(KnowledgeBase inputKb, 
                       String driver, 
                       String url, 
                       String username, 
                       String password,
                       String tablename) {
    frameDb = new DatabaseFrameDb();
    frameDb.initialize(inputKb.getFrameFactory(), 
                       driver, url, username, password, tablename, 
                       false);
    this.tableName = IncludingDatabaseAdapter.getTableName(tablename);
    this.inputKb = inputKb;
    frames = inputKb.getFrames();
  }
  
  public void save() throws SQLException {
    try {
      execute("DROP TABLE " + tableName);
    } catch (SQLException sqle) {
      Log.getLogger().config("Table " + tableName + " does not exist - initializing...");
    }
    IncludingDatabaseAdapter.initializeInheritanceTable(tableName, frameDb.getCurrentConnection());
    boolean noIncluded = true;
    for (Frame frame : frames) {
      if (frame.isIncluded()) {
        noIncluded = false;
        break;
      }
    }
    frameDb.overwriteKB(inputKb, noIncluded);
    if (!noIncluded) {
      frameDb.beginBatch();
      saveFrames();
      frameDb.endBatch();
    }
  }
  

  private void saveFrames() throws SQLException {
    int nFrames = inputKb.getFrameCount();
    int loopcount = 0;
    previousTime = System.currentTimeMillis();
    if (nFrames > LOOP_SIZE) {
      Log.getLogger().info("Getting " + nFrames + " frames, please be patient, " + new Date());
    }
    for (Frame frame : frames) {
      printTraceMessage(++loopcount, nFrames);
      if (!frame.isSystem() && frame.isIncluded()) {
        continue;
      }
      if (log.isLoggable(Level.FINER)) {
        log.finer("Examining frame " + frame.getName());
      }
      saveDirectOwnSlotValues(frame);
      if (frame instanceof Cls) {
        saveDirectTemplateSlotInformation((Cls) frame);
      }
    }
  }


  private void saveDirectOwnSlotValues(Frame frame) throws SQLException {
    for (Slot slot : frame.getOwnSlots()) {
      if (log.isLoggable(Level.FINER)) {
        log.finer("Examining slot " + slot);
      }
      Collection values = frame.getDirectOwnSlotValues(slot);
      values = reduceValues(values, frame);
      frameDb.saveValues(frame, slot, null, false, values);
      updateIncludedTable(values);
    }
  }
  
  private void saveDirectTemplateSlotInformation(Cls cls) throws SQLException {
    for (Slot slot : cls.getTemplateSlots()) {
      if (log.isLoggable(Level.FINER)) {
        log.finer("Looking at slot" + slot);
      }
      Collection values = cls.getDirectTemplateSlotValues(slot);
      values = reduceValues(values, cls);
      frameDb.saveValues(cls, slot, null, true, values);
      updateIncludedTable(values);
      for (Facet facet : cls.getTemplateFacets(slot)) {
        if (log.isLoggable(Level.FINER)) {
          log.finer("Looking at facet " + facet);
        }
        Collection facetValues = cls.getDirectTemplateFacetValues(slot, facet);
        facetValues = reduceValues(facetValues, cls);
        frameDb.saveValues(cls, slot, facet, true, facetValues);
        updateIncludedTable(facetValues);
      }
    }
  }
  
  private Collection reduceValues(Collection values, Frame frame) {
    if (localFrame(frame)) {
      return values;
    }
    Collection ret = new ArrayList();
    for (Object value : values) {
      if (value instanceof Frame) {
        Frame vframe = (Frame) value;
        if (localFrame(vframe)) {
          ret.add(value);
        }
      }
    }
    return ret;
  }
  
  private void updateIncludedTable(Collection values) throws SQLException {
    for (Object value : values) {
      if (log.isLoggable(Level.FINER)) {
        log.finer(" ... value = " + value);
      }
      if (value instanceof Frame) {
        Frame frame = (Frame) value;
        if (frame.isIncluded() && !frame.isSystem() && !alreadySeen.contains(frame.getFrameID())) {
          execute("INSERT INTO " + tableName + 
              " (" + Column.local_frame_id + ", " + Column.frame_name + ") VALUES (" +
              frame.getFrameID().getLocalPart() + ", '" + frame.getName() + "')");
          alreadySeen.add(frame.getFrameID());
        }
      }
    }
  }
  
  public boolean localFrame(Frame frame) {
    return !frame.isSystem() && !frame.isIncluded();
  }
  
  public boolean execute(String cmd) throws SQLException {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Executing database command = " + cmd);
    }
    Statement statement = frameDb.getCurrentConnection().getStatement();
    return statement.execute(cmd);
  }


  private void printTraceMessage(int loopcount, int nFrames) {
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
  
}
