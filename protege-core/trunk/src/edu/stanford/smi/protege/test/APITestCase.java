package edu.stanford.smi.protege.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

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
    private Collection _firedEvents = new ArrayList();
    private static Properties _dbProperties = null;
    private static final String DB_PREFIX = "junit.db.";

    public enum DBType {
      Oracle, MySQL, MsAccess, PostGres, SQLServer
    }

    private static DBType _dbType = DBType.Oracle;

    private static ProjectFactory _factory = new ClipsProjectFactory();
    private static Project _scratchProject = _factory.createProject();
    private static boolean _isFileProject = true;

    protected void assertEventFired(int type) {
        assertTrue(getEventFired(type) != null);
    }

    protected AbstractEvent getEventFired(int type) {
        AbstractEvent matchEvent = null;
        Iterator i = _firedEvents.iterator();
        while (i.hasNext() && matchEvent == null) {
            AbstractEvent event = (AbstractEvent) i.next();
            if (event.getEventType() == type) {
                matchEvent = event;
            }
        }
        return matchEvent;
    }

    public void setUp() throws Exception {
        super.setUp();
        _factory = new ClipsProjectFactory();
        _isFileProject = true;
        _dbType = DBType.Oracle;
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
      DatabaseKnowledgeBaseFactory.setDriver(sources, getDBProperty("driver"));
      DatabaseKnowledgeBaseFactory.setTablename(sources, getDBProperty("table"));
      DatabaseKnowledgeBaseFactory.setUsername(sources, getDBProperty("user"));
      DatabaseKnowledgeBaseFactory.setPassword(sources, getDBProperty("password"));
      DatabaseKnowledgeBaseFactory.setURL(sources, getDBProperty("url"));
    }
    
    public static DBType chooseDBType() {
      for (DBType dbt : DBType.values()) {
        _dbType = dbt;
        if (dbConfigured()) {
          return _dbType;
        }
      }
      return null;
    }

    public static boolean dbConfigured() {
      Properties dbp = getDBProperties();
      if (dbp == null) {
        return false;
      }
      String configured = getDBProperty("configured");
      if (configured == null || !configured.toLowerCase().equals("true")) {
        return false;
      }
      return true;
    }
    
    private static String getDBProperty(String prop) {
      Properties dbp = getDBProperties();
      return dbp.getProperty(DB_PREFIX + _dbType + "." + prop);
    }
    
    
    private static Properties getDBProperties() {
      if (_dbProperties != null) {
        return _dbProperties;
      }
      try {
        Properties dbp = new Properties();
        String dbPropertyFile = ApplicationProperties.getApplicationDirectory().getPath()
                                   +  File.separator
                                   + "junit.properties";
        InputStream is = new FileInputStream(dbPropertyFile);
        dbp.load(is);
        _dbProperties = dbp;
        return _dbProperties;
      } catch (Exception e) {
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

    private static void closeProject() {
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

}