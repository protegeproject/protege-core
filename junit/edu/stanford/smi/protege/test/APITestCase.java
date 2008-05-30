package edu.stanford.smi.protege.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseFactory;
import edu.stanford.smi.protege.util.AbstractEvent;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.PropertyList;

/**
 * Base class for unit tests. This class provides some helper methods to project and kb access.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class APITestCase extends AbstractTestCase {
    private Collection<AbstractEvent> _firedEvents = new ArrayList<AbstractEvent>();
    private static Properties _junitProperties = null;
    private static final String DB_PREFIX = "junit.db.";

    public enum DBType {
      Oracle, MySQL, MsAccess, PostGres, SQLServer
    }

    private static DBType _dbType = DBType.Oracle;

    public static final String JUNIT_DB_DRIVER_PROPERTY   = "driver";
    public static final String JUNIT_DB_TABLE_PROPERTY    = "table";
    public static final String JUNIT_DB_USER_PROPERTY     = "user";
    public static final String JUNIT_DB_PASSWORD_PROPERTY = "password";
    public static final String JUNIT_DB_URL_PROPERTY      = "url";

    private static ProjectFactory _factory = new ClipsProjectFactory();
    private static Project _scratchProject = _factory.createProject();
    private static boolean _isFileProject = true;

    protected void assertEventFired(int type) {
        assertTrue(getEventFired(type) != null);
    }

    protected AbstractEvent getEventFired(int type) {
        AbstractEvent matchEvent = null;
        Iterator<AbstractEvent> i = _firedEvents.iterator();
        while (i.hasNext() && matchEvent == null) {
            AbstractEvent event = i.next();
            if (event.getEventType() == type) {
                matchEvent = event;
            }
        }
        return matchEvent;
    }
    
    protected Collection<AbstractEvent> getFiredEvents() {
        return _firedEvents;
    }

    public void setUp() throws Exception {
        super.setUp();
        _factory = new ClipsProjectFactory();
        _isFileProject = true;
    }

    public final void tearDown() throws Exception {
        super.tearDown();
        if (_scratchProject != null) {
            // careful here in case there is an error
            Project p = _scratchProject;
            _scratchProject = null;
            p.dispose();
        }
    }

    protected void clearEvents() {
        _firedEvents.clear();
    }

    protected int getEventCount() {
        return _firedEvents.size();
    }

    private static void configureDBSources(PropertyList sources) {
      if (!dbConfigured()) {
        return;
      }
      DatabaseKnowledgeBaseFactory.setDriver(sources, getDBProperty(JUNIT_DB_DRIVER_PROPERTY));
      DatabaseKnowledgeBaseFactory.setTablename(sources, getDBProperty(JUNIT_DB_TABLE_PROPERTY));
      DatabaseKnowledgeBaseFactory.setUsername(sources, getDBProperty(JUNIT_DB_USER_PROPERTY));
      DatabaseKnowledgeBaseFactory.setPassword(sources, getDBProperty(JUNIT_DB_PASSWORD_PROPERTY));
      DatabaseKnowledgeBaseFactory.setURL(sources, getDBProperty(JUNIT_DB_URL_PROPERTY));
    }
    
    public static DBType chooseDBType() {
      for (DBType dbt : DBType.values()) {
        _dbType = dbt;
        if (dbConfigured()) {
          return _dbType;
        }
      }
      _dbType = null;
      return null;
    }

    private static Set<DBType> informedDBConfigured = EnumSet.noneOf(DBType.class);
 
    public static boolean dbConfigured() {
      return dbConfigured(true);
    }
    
    public static boolean dbConfigured(boolean report) {
      Properties dbp = getJunitProperties();
      if (dbp == null) {
        return false;
      }
      String configured = getDBProperty("configured");
      if (configured == null || !configured.toLowerCase().equals("true")) {
        if (report && !informedDBConfigured.contains(_dbType)) {
          System.out.println("Database Tests for " + _dbType + " not configured");
          informedDBConfigured.add(_dbType);
        }
        return false;
      }
      if (report && !informedDBConfigured.contains(_dbType)) {
        System.out.println("Database Tests for " + _dbType + " configured");
        informedDBConfigured.add(_dbType);
      }
      return true;
    }
    
    public static String getDBProperty(String prop) {
      Properties dbp = getJunitProperties();
      return dbp.getProperty(DB_PREFIX + _dbType + "." + prop);
    }
    
    private static boolean informedNoConfigurationFile = false;
    
    public static Properties getJunitProperties() {
      if (_junitProperties != null) {
        return _junitProperties;
      }
      try {
        Properties dbp = new Properties();
        String dbPropertyFile = ApplicationProperties.getApplicationDirectory().getPath()
                                   +  File.separator
                                   + "junit.properties";
        InputStream is = new FileInputStream(dbPropertyFile);
        dbp.load(is);
        _junitProperties = dbp;
        return _junitProperties;
      } catch (Exception e) {
        if (!informedNoConfigurationFile) {
          System.out.println("No configuration file for tests");
          informedNoConfigurationFile = true;
        }
        return null;
      }
    }

    private static int callNumber = 0;

    public Project getProject() {
        if (_scratchProject == null) {
            _scratchProject = _factory.createProject();
        }
        return _scratchProject;
    }

    public static boolean isDatabaseProject() {
        return !_isFileProject && _scratchProject != null;
    }

    public static boolean isFileProject() {
        return _isFileProject && _scratchProject != null;
    }

    public static void closeProject() {
        if (_scratchProject != null) {
            _scratchProject.dispose();
            _scratchProject = null;
        }
    }

    private static Project createScratchDatabaseProject() {
        ArrayList errors = new ArrayList();
        Project project = new Project(null, errors);
        URI uri = AbstractProjectFactory.getProjectURI();
        project.setProjectURI(uri);
        PropertyList sources = project.getSources();
        sources.setString(KnowledgeBaseFactory.FACTORY_CLASS_NAME,
                edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseFactory.class.getName());
        configureDBSources(sources);
        project.save(errors);
        checkErrors(errors);
        _scratchProject = Project.loadProjectFromURI(uri, errors);
        checkErrors(errors);
        return _scratchProject;
    }

    private static Project createScratchFileProject() {
        return _factory.createProject();
    }

    protected Slot getSlot(String name) {
        return getDomainKB().getSlot(name);
    }

    public static void init() {
        _scratchProject = null;
    }

    public void recordEventFired(AbstractEvent event) {
        _firedEvents.add(event);
    }

    protected static void saveAndReload() {
        _scratchProject = _factory.saveAndReloadProject(_scratchProject);
    }

    protected static void setDatabaseProject() {
        _isFileProject = false;
        closeProject();
        _scratchProject = createScratchDatabaseProject();
    }

    protected static void setFileProject() {
        _isFileProject = true;
        closeProject();
        _scratchProject = createScratchFileProject();
    }

    protected Object getClientInformation(String name) {
        return getProject().getClientInformation(name);
    }

    protected void setClientInformation(String name, Object value) {
        getProject().setClientInformation(name, value);
    }

    public static void setProjectFactory(ProjectFactory factory) {
        _factory = factory;
    }
    
    public static void setDBType(DBType dbt) {
      _dbType = dbt;
    }
    
    public static DBType getDBType() {
      return _dbType;
    }

}