package edu.stanford.smi.protege.test;

import java.net.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.storage.jdbc.*;
import edu.stanford.smi.protege.util.*;

/**
 * Base class for unit tests.  This class provides some helper methods to project and kb access.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class APITestCase extends AbstractTestCase {
    private Collection _firedEvents = new ArrayList();

    public static final int ORACLE = 1;
    public static final int MYSQL = 2;
    public static final int MS_ACCESS = 3;

    private static final int INITIAL_DB_TYPE = ORACLE;

    private static int _dbType = INITIAL_DB_TYPE;

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

    public void setUp() {
        _factory = new ClipsProjectFactory();
        _isFileProject = true;
        _dbType = INITIAL_DB_TYPE;
    }

    public final void tearDown() {
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
        switch (_dbType) {
            case MS_ACCESS :
                configureForAccess(sources);
                break;
            case ORACLE :
                configureForOracle(sources);
                break;
            case MYSQL :
                configureForMySQL(sources);
                break;
        }
    }

    private static int callNumber = 0;
    private static void configureForAccess(PropertyList sources) {
        JdbcKnowledgeBaseFactory.setDriver(sources, "sun.jdbc.odbc.JdbcOdbcDriver");
        JdbcKnowledgeBaseFactory.setTablename(sources, "scratch" + callNumber++);
        JdbcKnowledgeBaseFactory.setUsername(sources, "rwf");
        JdbcKnowledgeBaseFactory.setURL(sources, "jdbc:odbc:protege-access");
    }

    private static void configureForOracle(PropertyList sources) {
        JdbcKnowledgeBaseFactory.setDriver(sources, "oracle.jdbc.driver.OracleDriver");
        JdbcKnowledgeBaseFactory.setTablename(sources, "scratch");
        JdbcKnowledgeBaseFactory.setUsername(sources, "rwf");
        JdbcKnowledgeBaseFactory.setPassword(sources, "ray");
        JdbcKnowledgeBaseFactory.setURL(sources, "jdbc:oracle:thin:@biostorm.stanford.edu:1521:PROTEGE");
    }

    private static void configureForMySQL(PropertyList sources) {
        JdbcKnowledgeBaseFactory.setDriver(sources, "com.mysql.jdbc.Driver");
        JdbcKnowledgeBaseFactory.setTablename(sources, "scratch");
        JdbcKnowledgeBaseFactory.setUsername(sources, "myuser");
        JdbcKnowledgeBaseFactory.setPassword(sources, "");
        JdbcKnowledgeBaseFactory.setURL(sources, "jdbc:mysql://fergerson-li-smi/test");
    }

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
        sources.setString(
            KnowledgeBaseFactory.FACTORY_CLASS_NAME,
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

    public static void _init() {
        _scratchProject = null;
    }

    public void recordEventFired(AbstractEvent event) {
        _firedEvents.add(event);
    }

    protected void saveAndReload() {
        _scratchProject = _factory.saveAndReloadProject(_scratchProject);
    }

    protected static void setDatabaseProject() {
        _isFileProject = false;
        closeProject();
        _scratchProject = createScratchDatabaseProject();
    }

    protected void setDatabaseProject(int database) {
        _dbType = database;
        setDatabaseProject();
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
