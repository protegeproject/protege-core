package edu.stanford.smi.protege.server;


import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.ServerProjectAdapter;
import edu.stanford.smi.protege.event.ServerProjectNotificationEvent;
import edu.stanford.smi.protege.event.ServerProjectStatusChangeEvent;
import edu.stanford.smi.protege.event.TransactionAdapter;
import edu.stanford.smi.protege.event.TransactionEvent;
import edu.stanford.smi.protege.exception.TransactionException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.EventDispatchFrameStore;
import edu.stanford.smi.protege.model.framestore.EventGeneratorFrameStore;
import edu.stanford.smi.protege.model.framestore.EventSinkFrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStoreManager;
import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.test.APITestCase;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.LockStepper;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;

public class DBServer_Test extends APITestCase {
  
  private static final String HOST = "localhost";
  private static final String NAME = "//" + HOST + "/" + Server.getBoundName();
  public final static String DBTYPE_PROPERTY = "junit.server.db.dbtype";
  public final static String DBPROJECT_PROPERTY = "junit.server.db.file.dbproject";
  public final static String DB_SERVER_METAPROJECT_PROPERTY = "junit.server.db.file.metaproject";
  public final static String DB_CLIENT_PROJECT_PROPERTY = "junit.server.db.client_project";
  
  private static final String USER = "Ray Fergerson";
  private static final String PASSWORD = "claudia";
  
  private static String clientProject;
  static {
    Properties props = getJunitProperties();
    if (props != null) {
      clientProject = props.getProperty(DB_CLIENT_PROJECT_PROPERTY);
    }
  }
  
  private static boolean configured = true;
  
  private static int counter = 0;

  private boolean informedNotConfigured = false;
  
  @Override
public void setUp() throws Exception {
    super.setUp();
    if (clientProject == null) {
      if (!informedNotConfigured) {
        System.out.println("Server/Database tests not configured");
        informedNotConfigured = true;
      }
      configured = false;
    }
    Properties props = getJunitProperties();
    String serverProjectFile = props.getProperty(DB_SERVER_METAPROJECT_PROPERTY);
    try {
      Server_Test.setMetaProject(serverProjectFile);
      Server_Test.startServer();
      Naming.lookup(NAME);
    } catch (NotBoundException e) {
      fail("Could not bind to server (is rmiregistry running?)");
    }
    cleanProject();
  }

  public void createDatabaseProject() {
    Properties props = getJunitProperties();
    String dbType = props.getProperty(DBTYPE_PROPERTY);
    String dbProjectFile = props.getProperty(DBPROJECT_PROPERTY);
    if (dbType == null) {
      System.out.println("Server+Database Test not configured");
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
  
  public DefaultKnowledgeBase getKb() {
    RemoteProjectManager rpm = RemoteProjectManager.getInstance();
    Project p = rpm.getProject(HOST, USER, PASSWORD, clientProject, true);
    return (DefaultKnowledgeBase) p.getKnowledgeBase();
  }
  
  // moderately hacky way to clean out all the frames...
  public void cleanProject() {
    boolean progress = true;
    KnowledgeBase kb = getKb();
    while (progress) {
      progress = false;
      for (Frame frame : kb.getFrames()) {
        if (frame.isSystem()) {
          continue;
        } else if (frame instanceof Cls) {
          Cls cls = (Cls) frame;
          if (cls.getInstanceCount() == 0) {
            cls.delete();
            progress = true;
          }
        } else {
          frame.delete();
          progress = true;
        }
      }
    }
    kb.getProject().dispose();
  }
  
  private String newName() {
    return "A" + (counter++);
  }
  
  public TransactionMonitor getTransactionMonitor() {
    Server server = Server.getInstance();
    ServerProject project = server.getServerProject(clientProject);
    ServerFrameStore fs = (ServerFrameStore) project.getDomainKbFrameStore(null);
    return fs.getTransactionStatusMonitor();
  }
  
  public void getUpdatesFromOtherThread(KnowledgeBase kb) {
    kb.createCls(newName(), Collections.singleton(kb.getRootCls()));
  }
  
  /**
   * A main method that builds the database project based on junit
   * configuration parameters.
   *   
   * @param args The arguments are ignored.
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
    testStarted, mainThreadStarted, transactionOpenWithWrite, readComplete, testComplete
  }
  
  public void testTransaction01() throws Exception {
    if (!configured) {
      return;
    }
    try {
      final LockStepper<Test01Stages> ls = new LockStepper<Test01Stages>(Test01Stages.testStarted);
      KnowledgeBase kb = getKb();
      RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.READ_COMMITTED);
      new Thread("Second Transaction With Writes Thread") {
        @Override
		public void run() {
          try {
            String transactionName = "My transaction";
            KnowledgeBase kb = getKb();
            RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.READ_COMMITTED);
            
            Cls top = (Cls) ls.waitForStage(Test01Stages.mainThreadStarted);
            kb.beginTransaction(transactionName);
            Cls bottom = kb.createCls(newName(), Collections.singleton(top));
            ls.stageAchieved(Test01Stages.transactionOpenWithWrite, bottom);
            
            ls.waitForStage(Test01Stages.readComplete);
            kb.commitTransaction();
            kb.getProject().dispose();
            ls.stageAchieved(Test01Stages.testComplete, null);
          } catch (Throwable e) {
            ls.exceptionOffMainThread(Test01Stages.testComplete, e);
          }
        }
      }.start();
      final Cls top = kb.createCls(newName(), 
          Collections.singleton(kb.getSystemFrames().getRootCls()));
      ls.stageAchieved(Test01Stages.mainThreadStarted, top);
      
      ls.waitForStage(Test01Stages.transactionOpenWithWrite);
      getUpdatesFromOtherThread(kb);
      Collection subClasses = kb.getSubclasses(top);
      if (subClasses != null && !subClasses.isEmpty()) {
        fail("Should not see subclasses being created by other thread.");
      }
      ls.stageAchieved(Test01Stages.readComplete, null);
      
      ls.waitForStage(Test01Stages.testComplete);
      kb.getProject().dispose();
    } catch (Exception e) {
      Log.getLogger().log(Level.WARNING, "Test failed, e");
      throw e;
    }
  }
  
 
  /* ******************************************************************************
   * Testing repeatable read
   */
  
  public void testTransaction02_1() throws Exception {
    doTest02(true, true);
  }
  
  public void testTransaction02_2() throws Exception {
    doTest02(true, false);
  }
 
  public void testTransaction02_3() throws Exception {
    doTest02(false, true);
  }
  
  public void testTransaction02_4() throws Exception {
    doTest02(false, false);
  }
  
  public enum Test02Stages {
    testStarted, firstReadComplete, writeComplete, secondReadComplete, otherTransactionClosed, 
    thirdReadComplete, testComplete
  }
  
  public void doTest02(boolean commit, final boolean commitOther) throws Exception {
    if (!configured) {
      return;
    }
    try{
      KnowledgeBase kb = getKb();
      RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.REPEATABLE_READ);
      final LockStepper<Test02Stages> ls = new LockStepper<Test02Stages>(Test02Stages.testStarted);
      final Cls testCls = kb.createCls(newName(), Collections.singleton(kb.getSystemFrames().getRootCls()));
      new Thread("Second Transaction with Writes Thread doTest02(" + commit + "," + commitOther + ")") {
        @Override
		public void run() {
          try {
            KnowledgeBase kb = getKb();
            try {
              kb.beginTransaction("transaction will modify testCls after other transaction reads testCls");
              
              ls.waitForStage(Test02Stages.firstReadComplete);
              kb.createCls(newName(), Collections.singleton(testCls));
              assertTrue(kb.getSubclasses(testCls).size() == 1);
              ls.stageAchieved(Test02Stages.writeComplete, null);
              
              ls.waitForStage(Test02Stages.secondReadComplete);
            } finally {
              if (commitOther) {
                kb.commitTransaction();
              } else {
                kb.rollbackTransaction();
              }
            }
            if (commitOther) {
              assertTrue(kb.getSubclasses(testCls).size() == 1);
            } else {
              assertTrue(kb.getSubclasses(testCls).isEmpty());
            }
            ls.stageAchieved(Test02Stages.otherTransactionClosed, null);
            
            ls.waitForStage(Test02Stages.thirdReadComplete);
            kb.getProject().dispose();
            ls.stageAchieved(Test02Stages.testComplete, null);
          } catch (Throwable e) {
            ls.exceptionOffMainThread(Test02Stages.testComplete, e);
          }
        }
      }.start();
      try {
        kb.beginTransaction("Repeatable Read");
        assertTrue(kb.getSubclasses(testCls).isEmpty());
        ls.stageAchieved(Test02Stages.firstReadComplete, null);
        
        ls.waitForStage(Test02Stages.writeComplete);
        getUpdatesFromOtherThread(kb);
        assertTrue(kb.getSubclasses(testCls).isEmpty());
        ls.stageAchieved(Test02Stages.secondReadComplete, null);
        
        ls.waitForStage(Test02Stages.otherTransactionClosed);
        getUpdatesFromOtherThread(kb);
        assertTrue(kb.getSubclasses(testCls).isEmpty());
      } finally {
        if (commit) {
          kb.commitTransaction();
        } else {
          kb.rollbackTransaction();
        }
      }
      if (commitOther) {
        assertTrue(kb.getSubclasses(testCls).size() == 1);
      } else {
        assertTrue(kb.getSubclasses(testCls).isEmpty());
      }
      ls.stageAchieved(Test02Stages.thirdReadComplete, null);
      
      ls.waitForStage(Test02Stages.testComplete);
      kb.getProject().dispose();
    } catch (Exception e) {
      Log.getLogger().log(Level.WARNING, "Test faiiled", e);
      throw e;
    }
  }
  
  /* ******************************************************************
   * Checking the transaction nesting...
   *
   */
  
  public void testTransaction03() {
    if (!configured) {
      return;
    }
    KnowledgeBase kb = getKb();
    TransactionMonitor tm = getTransactionMonitor();
    assertTrue(tm.getSessions().isEmpty());
    kb.beginTransaction("Outer");
    assertTrue(tm.getSessions().size() == 1);
    RemoteSession mySession = tm.getSessions().iterator().next();
    
    kb.beginTransaction("First Inner");
    assertTrue(tm.getNesting(mySession) == 2);
    kb.rollbackTransaction();
    assertTrue(tm.getNesting(mySession) == 1);
    
    kb.beginTransaction("Second Inner");
    assertTrue(tm.getNesting(mySession) == 2);
    kb.commitTransaction();
    assertTrue(tm.getNesting(mySession) == 1);
    
    kb.commitTransaction();
    assertTrue(tm.getNesting(mySession) == 0);
    kb.getProject().dispose();
  }

  

  public enum Test04Stages {
    testStarted, firstRead, write, secondRead, firstCommit, preComplete, testCompleted
  }

  public void testTransaction04() throws TransactionException {
    if (!configured) {
      return;
    }
    KnowledgeBase kb = getKb();
    RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.REPEATABLE_READ);
    final LockStepper<Test04Stages> ls = new LockStepper<Test04Stages>(Test04Stages.testStarted);
    final Cls top = kb.createCls(newName(), 
                                 Collections.singleton(kb.getSystemFrames().getRootCls()));
    final Cls middle = kb.createCls(newName(), Collections.singleton(top));
    final Cls firstBottom = kb.createCls(newName(), Collections.singleton(middle));
    new Thread("Second knowledge base which writes a sub-subclass") {
      @Override
	public void run() {
        try {
          KnowledgeBase kb = getKb();
          RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.REPEATABLE_READ);
          kb.beginTransaction("Transaction in other thread");
          
          ls.waitForStage(Test04Stages.firstRead);
          Cls secondBottom = kb.createCls(newName(), Collections.singleton(middle));
          ls.stageAchieved(Test04Stages.write, secondBottom);
          
          ls.waitForStage(Test04Stages.secondRead);
          kb.commitTransaction();
          ls.stageAchieved(Test04Stages.firstCommit, null);
          
          ls.waitForStage(Test04Stages.preComplete);
          kb.getProject().dispose();
          ls.stageAchieved(Test04Stages.testCompleted, null);
        } catch (Exception e) {
          ls.exceptionOffMainThread(Test04Stages.testCompleted, e);
        }
      }
    }.start();
    kb.beginTransaction("First knowledge base which does some reading");
    Collection subclasses = kb.getSubclasses(top);
    assertTrue(subclasses.contains(firstBottom));
    ls.stageAchieved(Test04Stages.firstRead, null);
    
    Cls secondBottom = (Cls) ls.waitForStage(Test04Stages.write);
    LocalizeUtils.localize(secondBottom, kb);
    getUpdatesFromOtherThread(kb);
    subclasses = kb.getDirectSubclasses(middle);
    assertTrue(subclasses.size() == 1);
    assertTrue(!subclasses.contains(secondBottom));
    ls.stageAchieved(Test04Stages.secondRead, null);
    
    ls.waitForStage(Test04Stages.firstCommit);
    getUpdatesFromOtherThread(kb);
    subclasses = kb.getDirectSubclasses(middle);
    assertTrue(subclasses.size() == 1);
    assertTrue(!subclasses.contains(secondBottom));
    kb.commitTransaction();
    subclasses = kb.getDirectSubclasses(middle);
    assertTrue(subclasses.size() == 2);
    assertTrue(subclasses.contains(secondBottom));
    ls.stageAchieved(Test04Stages.preComplete, null);
    
    ls.waitForStage(Test04Stages.testCompleted);
    kb.getProject().dispose();
  }
  

  public enum Test05Stages {
    testStarted, firstRead, write, secondRead, firstCommit, preComplete, testCompleted
  }
  public void testTransaction05() throws TransactionException {
    if (!configured) {
      return;
    }
    KnowledgeBase kb = getKb();
    RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.READ_COMMITTED);
    final LockStepper<Test05Stages> ls = new LockStepper<Test05Stages>(Test05Stages.testStarted);
    final Cls top = kb.createCls(newName(), 
                                 Collections.singleton(kb.getSystemFrames().getRootCls()));
    final Cls middle = kb.createCls(newName(), Collections.singleton(top));
    final Cls firstBottom = kb.createCls(newName(), Collections.singleton(middle));
    new Thread("Second knowledge base which writes a sub-subclass") {
      @Override
	public void run() {
        try {
          KnowledgeBase kb = getKb();
          RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.READ_COMMITTED);
          kb.beginTransaction("Transaction in other thread");
          
          ls.waitForStage(Test05Stages.firstRead);
          Cls secondBottom = kb.createCls(newName(), Collections.singleton(middle));
          ls.stageAchieved(Test05Stages.write, secondBottom);
          
          ls.waitForStage(Test05Stages.secondRead);
          kb.commitTransaction();
          ls.stageAchieved(Test05Stages.firstCommit, null);
          
          ls.waitForStage(Test05Stages.preComplete);
          kb.getProject().dispose();
          ls.stageAchieved(Test05Stages.testCompleted, null);
        } catch (Exception e) {
          ls.exceptionOffMainThread(Test05Stages.testCompleted, e);
        }
      }
    }.start();
    kb.beginTransaction("First knowledge base which does some reading");
    Collection subclasses = kb.getSubclasses(top);
    assertTrue(subclasses.contains(firstBottom));
    ls.stageAchieved(Test05Stages.firstRead, null);
    
    Cls secondBottom = (Cls) ls.waitForStage(Test05Stages.write);
    LocalizeUtils.localize(secondBottom, kb);
    getUpdatesFromOtherThread(kb);
    subclasses = kb.getDirectSubclasses(middle);
    assertTrue(subclasses.size() == 1);
    assertTrue(!subclasses.contains(secondBottom));
    ls.stageAchieved(Test05Stages.secondRead, null);
    
    ls.waitForStage(Test05Stages.firstCommit);
    getUpdatesFromOtherThread(kb);
    subclasses = kb.getDirectSubclasses(middle);
    assertTrue(subclasses.size() == 2);
    assertTrue(subclasses.contains(secondBottom));
    kb.commitTransaction();
    getUpdatesFromOtherThread(kb);
    subclasses = kb.getDirectSubclasses(middle);
    assertTrue(subclasses.size() == 2);
    assertTrue(subclasses.contains(secondBottom));
    ls.stageAchieved(Test05Stages.preComplete, null);
    
    ls.waitForStage(Test05Stages.testCompleted);
    kb.getProject().dispose();
  }
  
  
  

  public enum Test06Stages {
    start, inTransaction, write, preComplete, completed;
  }

  public void testTransaction06() throws TransactionException {
    if (!configured) {
      return;
    }
    final LockStepper<Test06Stages> ls = new LockStepper<Test06Stages>(Test06Stages.start);
    KnowledgeBase kb = getKb();
    RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.REPEATABLE_READ);
    final Cls domainCls = kb.createCls(newName(), Collections.singleton(kb.getRootCls()));
    final Cls val1 = kb.createCls(newName(), Collections.singleton(domainCls));
    final Cls val2 = kb.createCls(newName(), Collections.singleton(domainCls));
    final Slot slot = kb.createSlot(newName(), kb.getSystemFrames().getStandardSlotMetaCls());
    kb.setDirectOwnSlotValues(domainCls, slot, Collections.singleton(val1));
    Collection values = kb.getDirectOwnSlotValues(domainCls, slot);
    assertTrue(values.contains(val1));
    assertTrue(!values.contains(val2));
    new Thread("Non-transaction write thread") {
      @Override
	public void run() {
        try {
          KnowledgeBase kb = getKb();
          RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.REPEATABLE_READ);
          
          ls.waitForStage(Test06Stages.inTransaction);
          kb.setDirectOwnSlotValues(domainCls, slot, Collections.singleton(val2));
          Collection values = kb.getDirectOwnSlotValues(domainCls, slot);
          LocalizeUtils.localize(val1, kb);
          LocalizeUtils.localize(val2, kb);
          assertTrue(!values.contains(val1));
          assertTrue(values.contains(val2));
          ls.stageAchieved(Test06Stages.write, null);
          
          ls.waitForStage(Test06Stages.preComplete);
          ls.stageAchieved(Test06Stages.completed, null);
        } catch (Exception e) {
          ls.exceptionOffMainThread(Test06Stages.completed, e);
        }
      }
    }.start();
    kb.beginTransaction("A lone transaction");
    values = kb.getDirectOwnSlotValues(domainCls, slot);
    LocalizeUtils.localize(val1, kb);
    LocalizeUtils.localize(val2, kb);
    assertTrue(values.contains(val1));
    assertTrue(!values.contains(val2));
    ls.stageAchieved(Test06Stages.inTransaction, null);

    ls.waitForStage(Test06Stages.write);
    getUpdatesFromOtherThread(kb);
    values = kb.getDirectOwnSlotValues(domainCls, slot);
    LocalizeUtils.localize(val1, kb);
    LocalizeUtils.localize(val2, kb);
    assertTrue(values.contains(val1));
    assertTrue(!values.contains(val2));
    kb.commitTransaction();
    values = kb.getDirectOwnSlotValues(domainCls, slot);
    assertTrue(!values.contains(val1));
    assertTrue(values.contains(val2));    
    ls.stageAchieved(Test06Stages.preComplete, null);
    
    ls.waitForStage(Test06Stages.completed);
  }    
  
  public enum Test07Stages {
      START, CLIENT_CONNECTED, LISTENERS_INSTALLED_GO, COMPLETED;
  }
  
  public void testTransaction07() {
      if (!configured) {
          return;
      }
      final LockStepper<Test07Stages> ls = new LockStepper<Test07Stages>(Test07Stages.START);


      new Thread("Client Accessing the server") {
          @Override
		public void run() {
              try {
                  KnowledgeBase kb = getKb();
                  RemoteClientFrameStore.setTransactionIsolationLevel(kb, TransactionIsolationLevel.REPEATABLE_READ);

                  Cls thing = kb.getSystemFrames().getRootCls();

                  ls.stageAchieved(Test07Stages.CLIENT_CONNECTED, null);
                  ls.waitForStage(Test07Stages.LISTENERS_INSTALLED_GO);
                  kb.beginTransaction("Starting outer transaction");
                  kb.createCls("A", Collections.singleton(thing));

                  assertEventFired(TransactionEvent.TRANSACTION_BEGIN);  // a little weird but 
                  assert(getEventFired(KnowledgeBaseEvent.CLS_CREATED) == null); // this is detecting events on the server

                  kb.beginTransaction("Starting inner transaction");
                  kb.createSlot("f");
                  assert(getEventFired(KnowledgeBaseEvent.SLOT_CREATED) == null);
                  kb.commitTransaction();

                  assert(getEventFired(KnowledgeBaseEvent.SLOT_CREATED) == null);
                  assert(getEventFired(KnowledgeBaseEvent.CLS_CREATED) == null);

                  kb.commitTransaction();
                  assertEventFired(KnowledgeBaseEvent.CLS_CREATED);
                  assertEventFired(KnowledgeBaseEvent.SLOT_CREATED);
                  ls.stageAchieved(Test07Stages.COMPLETED, null);
              } catch (Exception e) {
                  ls.exceptionOffMainThread(Test07Stages.COMPLETED, e);
              }
          }
      }.start();
      // Server work.
      ls.waitForStage(Test07Stages.CLIENT_CONNECTED);
      KnowledgeBase serverKb = Server.getInstance().getProject(clientProject).getKnowledgeBase();
      serverKb.setDispatchEventsEnabled(true);
      serverKb.getFrameStoreManager().getFrameStoreFromClass(EventDispatchFrameStore.class).setPassThrough(true);
      clearEvents();
      serverKb.addTransactionListener(new TransactionAdapter() {
          @Override
          public void transactionBegin(TransactionEvent event) {
              recordEventFired(event);
          }
      });
      serverKb.addKnowledgeBaseListener(new KnowledgeBaseAdapter() {
          @Override
          public void clsCreated(KnowledgeBaseEvent event) {
              recordEventFired(event);
          }

          @Override
          public void slotCreated(KnowledgeBaseEvent event) {
              recordEventFired(event);
          }
      });
      ls.stageAchieved(Test07Stages.LISTENERS_INSTALLED_GO, null);
      ls.waitForStage(Test07Stages.COMPLETED);
  }
  
  public enum EventGenerationStages {
      START, LISTENERS_INSTALLED, 
      FIRST_CLASS_ADDED, FIRST_CLASS_CHECKED,
      SECOND_CLASS_ADDED, SECOND_CLASS_CHECKED,
      THIRD_CLASS_ADDED, THIRD_CLASS_CHECKED,
      COMPLETED;
  }
  
  public void testEnableEventGeneration() {
      if (!configured) {
          return;
      }
      
      final String aName = "A";
      final String bName = "B";
      final String cName = "C";
      
      final LockStepper<EventGenerationStages> ls 
              = new LockStepper<EventGenerationStages>(EventGenerationStages.START);
      Server server = Server.getInstance();
      final KnowledgeBase serverSideKb = server.getProject(clientProject).getKnowledgeBase();
      assertTrue(serverSideKb.getProject().isMultiUserServer());
      assertFalse(serverSideKb.getProject().isMultiUserClient());
      checkEventFrameStores(serverSideKb, true, false);

      new Thread("First Client") {
          @Override
		public void run() {   
              try {
                  KnowledgeBase kb1 = getKb();
                  assertTrue(kb1.getProject().isMultiUserClient());
                  assertFalse(kb1.getProject().isMultiUserServer());
                  checkEventFrameStores(kb1, false, false);
                  
                  TestListener listener1 = new TestListener();
                  kb1.addKnowledgeBaseListener(listener1);
                  
                  ls.waitForStage(EventGenerationStages.LISTENERS_INSTALLED);
                  Cls a = kb1.createCls(aName, new ArrayList());
                  List<Cls> created = listener1.getCreatedClassesAndClear();
                  assertEquals(1, created.size());
                  assertTrue(created.contains(a));
                  ls.stageAchieved(EventGenerationStages.FIRST_CLASS_ADDED, null);
                  
                  ls.waitForStage(EventGenerationStages.FIRST_CLASS_CHECKED);
                  kb1.setGenerateEventsEnabled(false);
                  checkEventFrameStores(kb1, false, true);
                  checkEventFrameStores(serverSideKb, true, false);
                  Cls b = kb1.createCls(bName, new ArrayList());
                  created = listener1.getCreatedClassesAndClear();
                  assertEquals(0, created.size());
                  ls.stageAchieved(EventGenerationStages.SECOND_CLASS_ADDED, null);
                  
                  ls.waitForStage(EventGenerationStages.SECOND_CLASS_CHECKED);
                  kb1.setGenerateEventsEnabled(true);
                  checkEventFrameStores(kb1, false, false);
                  checkEventFrameStores(serverSideKb, true, false);
                  Cls c = kb1.createCls(cName, new ArrayList());
                  created = listener1.getCreatedClassesAndClear();
                  assertEquals(1, created.size());
                  assertTrue(created.contains(c));
                  ls.stageAchieved(EventGenerationStages.THIRD_CLASS_ADDED, null);
                  
                  ls.waitForStage(EventGenerationStages.THIRD_CLASS_CHECKED);
                  kb1.getProject().dispose();
                  ls.stageAchieved(EventGenerationStages.COMPLETED, null);
              } catch (Exception e) {
                  ls.exceptionOffMainThread(EventGenerationStages.COMPLETED, e);
              }
          }
      }.start();

      new Thread("Second Client") {
          @Override
		public void run() {   
              try {
                  KnowledgeBase kb2 = getKb();
                  checkEventFrameStores(kb2, false, false);
                  
                  TestListener listener2 = new TestListener();
                  kb2.addKnowledgeBaseListener(listener2);
                  ls.stageAchieved(EventGenerationStages.LISTENERS_INSTALLED, null);
                  
                  ls.waitForStage(EventGenerationStages.FIRST_CLASS_ADDED);
                  checkEventFrameStores(kb2, false, false);
                  kb2.flushEvents();
                  List<Cls> created = listener2.getCreatedClassesAndClear();
                  assertEquals(1, created.size());
                  assertTrue(created.iterator().next().getName().equals(aName));
                  ls.stageAchieved(EventGenerationStages.FIRST_CLASS_CHECKED, null);
                  
                  ls.waitForStage(EventGenerationStages.SECOND_CLASS_ADDED);
                  checkEventFrameStores(kb2, false, false);
                  kb2.flushEvents();
                  created = listener2.getCreatedClassesAndClear();
                  assertEquals(1, created.size());
                  assertTrue(created.iterator().next().getName().equals(bName));
                  ls.stageAchieved(EventGenerationStages.SECOND_CLASS_CHECKED, null);
                  
                  ls.waitForStage(EventGenerationStages.THIRD_CLASS_ADDED);
                  checkEventFrameStores(kb2, false, false);
                  kb2.flushEvents();
                  created = listener2.getCreatedClassesAndClear();
                  assertEquals(1, created.size());
                  assertTrue(created.iterator().next().getName().equals(cName));
                  kb2.getProject().dispose();
                  ls.stageAchieved(EventGenerationStages.THIRD_CLASS_CHECKED, null);
              } catch (Exception e) {
                  ls.exceptionOffMainThread(EventGenerationStages.COMPLETED, e);
              }
          }
      }.start();
      ls.waitForStage(EventGenerationStages.COMPLETED);
  }
  
  private class TestListener extends KnowledgeBaseAdapter {
      private List<Cls> createdClasses = new ArrayList();

      @Override
      public void clsCreated(KnowledgeBaseEvent event) {
          createdClasses.add(event.getCls());
      }
      
      public List<Cls> getCreatedClassesAndClear() {
          List<Cls> result = createdClasses;
          createdClasses = new ArrayList<Cls>();
          return result;
      }
  }
  
  private void checkEventFrameStores(KnowledgeBase kb, boolean generation, boolean sink) {
      FrameStoreManager fsm = kb.getFrameStoreManager();
      EventGeneratorFrameStore eventGeneration = fsm.getFrameStoreFromClass(EventGeneratorFrameStore.class);
      EventSinkFrameStore eventSink = fsm.getFrameStoreFromClass(EventSinkFrameStore.class);
      if (generation) {
          assertNotNull(eventGeneration);
      }
      else {
          assertNull(eventGeneration);
      }
      if (sink) {
          assertNotNull(eventSink);
      }
      else {
          assertNull(eventSink);
      }
  }
  
  public class ProjectShutdownListener extends ServerProjectAdapter {
      private String lastMessage;
      private ProjectStatus oldStatus;
      private ProjectStatus newStatus;

      @Override
      public void projectNotificationReceived(ServerProjectNotificationEvent event) {
          lastMessage = event.getMessage();
      }

      @Override
      public void projectStatusChanged(ServerProjectStatusChangeEvent event) {
          oldStatus = event.getOldStatus();
          newStatus = event.getNewStatus();
      }

      public String getLastMessage() {
          return lastMessage;
      }

      public ProjectStatus getOldStatus() {
          return oldStatus;
      }

      public ProjectStatus getNewStatus() {
          return newStatus;
      }

  }

}
