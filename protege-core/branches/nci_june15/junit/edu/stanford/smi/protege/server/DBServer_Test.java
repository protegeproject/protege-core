package edu.stanford.smi.protege.server;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.util.LockStepper;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.exceptions.TransactionException;

public class DBServer_Test extends APITestCase {
  
  private static final String HOST = "localhost";
  private static final String NAME = "//" + HOST + "/" + Server.getBoundName();
  public final static String DBTYPE_PROPERTY = "junit.server.db.dbtype";
  public final static String DBPROJECT_PROPERTY = "junit.server.db.file.dbproject";
  public final static String DB_SERVER_METAPROJECT_PROPERTY = "junit.server.db.file.metaproject";
  public final static String DB_CLIENT_PROJECT_PROPERTY = "junit.server.db.client_project";
  
  private static final String USER = "Ray Fergerson";
  private static final String PASSWORD = "claudia";
  
  private static boolean projectCleaned = false;
  
  private static String clientProject;
  static {
    Properties props = getJunitProperties();
    if (props != null) {
      clientProject = props.getProperty(DB_CLIENT_PROJECT_PROPERTY);
    }
  }
  
  private static boolean configured = true;
  
  private static int counter = 0;

  public void setUp() throws Exception {
    super.setUp();
    Properties props = getJunitProperties();
    String serverProjectFile = props.getProperty(DB_SERVER_METAPROJECT_PROPERTY);
    try {
      Server_Test.startServer(serverProjectFile);
      Naming.lookup(NAME);
    } catch (NotBoundException e) {
      fail("Could not bind to server (is rmiregistry running?)");
    }
    if (!projectCleaned) {
      cleanProject();
      projectCleaned = true;
    }
  }
  
  private static boolean informNotConfigured = false;
  
  public void createDatabaseProject() {
    Properties props = getJunitProperties();
    String dbType = props.getProperty(DBTYPE_PROPERTY);
    String dbProjectFile = props.getProperty(DBPROJECT_PROPERTY);
    if (dbType == null) {
      configured = false;
      if (!informNotConfigured) {
        System.out.println("Server+Database Test not configured");
        informNotConfigured = true;
      }
      return;
    }
    for (DBType dbt : DBType.values()) {
      if (dbt.toString().equals(dbType)) {
        setDBType(dbt);
        break;
      }
    }
    setDatabaseProject();
    Collection errors = new ArrayList();
    getProject().setProjectFilePath(dbProjectFile);
    getProject().save(errors);
    closeProject();
  }
  
  public KnowledgeBase getKb() {
    RemoteProjectManager rpm = RemoteProjectManager.getInstance();
    Project p = rpm.getProject(HOST, USER, PASSWORD, clientProject, true);
    return p.getKnowledgeBase();
  }
  
  public void cleanProject() {
    KnowledgeBase kb = getKb();
    for (Frame frame : kb.getFrames()) {
      if (!frame.isSystem()) {
        frame.delete();
      }
    }
  }
  
  private String newClassName() {
    return "A" + (counter++);
  }
  

  /**
   * A main method that configures  
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    DBServer_Test dbst = new DBServer_Test();
    
    dbst.createDatabaseProject();
  }

  /*
   *************************************** Tests ***************************************
   */
  
  public enum Test01Stages {
    testStarted, mainThreadStarted, transactionOpenWithWrite, testComplete
  }
  
  public void testTransaction01() {
    if (!configured) {
      return;
    }
    try {
      final LockStepper<Test01Stages> ls = new LockStepper<Test01Stages>(Test01Stages.testStarted);
      new Thread() {
        public void run() {
          try {
            String transactionName = "My transaction";
            KnowledgeBase kb = getKb();
            RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.READ_COMMITTED);
            Cls top = (Cls) ls.waitForStage(Test01Stages.mainThreadStarted);
            kb.beginTransaction(transactionName);
            Cls bottom = kb.createCls(newClassName(), Collections.singleton(top));
            ls.stageAchieved(Test01Stages.transactionOpenWithWrite, bottom);
            ls.waitForStage(Test01Stages.testComplete);
            kb.endTransaction(true);
          } catch (Exception e) {
            Log.getLogger().log(Level.SEVERE, "Exception caught in second thread", e);
            fail("Excception caught in alternative thread");
          }
        }
      }.start();
      KnowledgeBase kb = getKb();
      final Cls top = kb.createCls(newClassName(), 
                                   Collections.singleton(kb.getSystemFrames().getRootCls()));
      ls.stageAchieved(Test01Stages.mainThreadStarted, top);
      ls.waitForStage(Test01Stages.transactionOpenWithWrite);
      Collection subClasses = top.getSubclasses();
      if (subClasses != null && !subClasses.isEmpty()) {
        fail("Should not see subclasses being created by other thread.");
      }
      ls.stageAchieved(Test01Stages.testComplete, null);
    } catch (Throwable t) {
      Log.getLogger().log(Level.SEVERE, "Exception caught in main thread", t);
    }
  }
  
  public enum Test02Stages {
    testStarted, firstReadComplete, writeComplete, testComplete
  }
  
  /*
   * Testing repeatable read
   */
  public void testTransaction02() throws TransactionException {
    KnowledgeBase kb = getKb();
    RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.REPEATABLE_READ);
    final LockStepper<Test02Stages> ls = new LockStepper(Test02Stages.testStarted);
    final Cls testCls = kb.createCls(newClassName(), 
                                     Collections.singleton(kb.getSystemFrames().getRootCls()));
    new Thread() {
      public void run() {
        try {
          KnowledgeBase kb = getKb();
          kb.beginTransaction("transaction will modify testCls after other transaction reads testCls");
          ls.waitForStage(Test02Stages.firstReadComplete);
          kb.createCls(newClassName(), Collections.singleton(testCls));
          ls.stageAchieved(Test02Stages.writeComplete, null);
          ls.waitForStage(Test02Stages.testComplete);
          kb.endTransaction(true);
        } catch (Exception e) {
          Log.getLogger().log(Level.SEVERE, "Exception caught", e);
          fail("Exception in second thread - see logs.");
        }
      }
    }.start();
    kb.beginTransaction("Repeatable Read");
    assertTrue(kb.getSubclasses(testCls).isEmpty());
    ls.stageAchieved(Test02Stages.firstReadComplete, null);
    ls.waitForStage(Test02Stages.writeComplete);
    assertTrue(kb.getSubclasses(testCls).isEmpty());
    ls.stageAchieved(Test02Stages.testComplete, null);
    kb.endTransaction(true);
  }
}
