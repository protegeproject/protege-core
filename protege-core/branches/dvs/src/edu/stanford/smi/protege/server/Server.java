package edu.stanford.smi.protege.server;

//ESCA*JAVA0100

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.security.auth.login.LoginContext;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.plugin.ProjectPluginManager;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.server.auth.ProtegeCallbackHandler;
import edu.stanford.smi.protege.server.framestore.LocalizeFrameStoreHandler;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.URIUtilities;

public class Server extends UnicastRemoteObject implements RemoteServer {
    private static Server serverInstance;
    private Map _nameToOpenProjectMap = new HashMap();
    private Map _projectToServerProjectMap = new HashMap();
    private KnowledgeBase _systemKb;
    private Slot _nameSlot;
    private Slot _passwordSlot;
    private Slot _locationSlot;
    private Cls _userCls;
    private Cls _projectCls;
    private List _sessions = new ArrayList();
    private URI _baseURI;
    private Map _sessionToProjectsMap = new HashMap();
    private Thread _updateThread;
    private URI metaprojectURI;
    private static final int NO_SAVE = -1;
    private int _saveIntervalMsec = NO_SAVE;
    private static final String SAVE_INTERVAL_OPTION = "-saveIntervalSec=";
    private static final String NOPRELOAD_OPTION = "-nopreload";
    private static final String OPTION_CHAR = "-";
    private boolean preload = true;
    private ProjectPluginManager _projectPluginManager = new ProjectPluginManager();

    public static void main(String[] args) {
        try {
            startServer(args);
        } catch (Exception e) {
            Log.getLogger().log(Level.SEVERE, "server startup failed", e);
        }
    }

    /**
     * Start up the server.
     * 
     * @param args
     *            the arguments to the server
     *
     * @throws IOException
     *             if the socket factory has already been set
     * @see RMISocketFactory#setSocketFactory(RMISocketFactory)
     */
    public static void startServer(String[] args) throws IOException {
        System.setProperty("java.rmi.server.RMIClassLoaderSpi", ProtegeRmiClassLoaderSpi.class.getName());
        SystemUtilities.initialize();
        serverInstance = new Server(args);
        Log.getLogger().info("Protege server ready to accept connections...");
    }
    
    public static Server getInstance() {
        return serverInstance;
    }

    public static String getBoundName() {
        return Text.getProgramTextName();
    }

    protected static String getLocalBoundName() {
        return getBoundName();
    }

    private static Registry getRegistry() throws RemoteException {
        int port = Integer.getInteger(ClientRmiSocketFactory.REGISTRY_PORT, 
                                      Registry.REGISTRY_PORT).intValue();
        return LocateRegistry.getRegistry(null, port);
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            parseArg(args[i]);
        }
    }

    protected void parseArg(String arg) {
        if (arg.startsWith(SAVE_INTERVAL_OPTION)) {
            extractSaveInterval(arg);
        } else if (arg.startsWith(NOPRELOAD_OPTION)) {
            preload = false;
        } else if (arg.startsWith(OPTION_CHAR)) {
            printUsage();
        } else {
            extractMetaProjectLocation(arg);
        }
    }

    private void extractSaveInterval(String s) {
        if (s.startsWith(SAVE_INTERVAL_OPTION)) {
            String min = s.substring(SAVE_INTERVAL_OPTION.length());
            int seconds = Integer.parseInt(min);
            Log.getLogger().config("Save interval sec=" + seconds);
            if (seconds > 0) {
                _saveIntervalMsec = seconds * 1000;
            }
        } else {
            printUsage();
        }
    }

    protected void extractMetaProjectLocation(String s) {
        metaprojectURI = URIUtilities.createURI(s);
    }

    //ESCA-JAVA0130 
    protected void printUsage() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("usage: java -cp protege.jar edu.stanford.smi.protege.server.Server [options] <metaproject>");
        buffer.append("\n\tOptions:");
        buffer.append("\n\t\t" + SAVE_INTERVAL_OPTION + "<nseconds>");
        buffer.append("\n\t\t\tSave any dirty projects every n minutes (only needed for file based projects)");
        buffer.append("\n\t\t" + NOPRELOAD_OPTION);
        buffer.append("\n\t\t\tDon't preload projects.");
        //ESCA-JAVA0267 
        System.err.println(buffer.toString());
        System.exit(-1);
    }

    public Server(String[] args) throws RemoteException, IOException {
        super(ServerRmiSocketFactory.getServerPort(),
              ClientRmiSocketFactory.getInstance(), ServerRmiSocketFactory.getInstance());
        parseArgs(args);
        initialize();
    }

    public void reinitialize() throws RemoteException {
        Log.getLogger().info("Server reinitializing");
        clear();
        initialize();
    }

    private void clear() {
        _nameToOpenProjectMap.clear();
        _projectToServerProjectMap.clear();
        _sessions.clear();
        _sessionToProjectsMap.clear();
        stopProjectUpdateThread();
    }

    private void initialize() throws RemoteException {
        Collection errors = new ArrayList();
        Project project = Project.loadProjectFromURI(metaprojectURI, errors);
        if (!errors.isEmpty()) {
            throw new RuntimeException(errors.iterator().next().toString());
        }
        _systemKb = project.getKnowledgeBase();
        _projectCls = _systemKb.getCls("Project");
        _userCls = _systemKb.getCls("User");
        _nameSlot = _systemKb.getSlot("name");
        _passwordSlot = _systemKb.getSlot("password");
        _locationSlot = _systemKb.getSlot("location");
        bindName();
        dumpProjects();
        startProjectUpdateThread();
    }

    private void dumpProjects() {
        Iterator i = getAvailableProjectNames(null).iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            if (preload) {
                Log.getLogger().info("Loading project " + name);
                createProject(name);
            } else {
                Log.getLogger().info("Found project " + name);
            }
        }
    }

    protected void bindName() throws RemoteException {
        try {
            String boundName = getLocalBoundName();
            getRegistry().rebind(boundName, this);
            _baseURI = new URI("rmi://" + getMachineName() + "/" + boundName);
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
            if (e instanceof RemoteException) {
                throw (RemoteException) e;
            }
            throw new RemoteException(e.getMessage());
        }
    }

    private static String getMachineName() {
        String name;
        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            Log.getLogger().severe(Log.toString(e));
            name = "localhost";
        }
        return name;
    }

    public RemoteSession openSession(String username, String userIpAddress, String password) {
        RemoteSession session = null;
        if (isValid(username, password)) {
            session = new Session(username, userIpAddress);
            _sessions.add(session);
        }
        return session;
    }

    public void closeSession(RemoteSession session) {
        _sessions.remove(session);
    }
    
    public boolean isActive(RemoteSession session) {
        return _sessions.contains(session);
    }

    public RemoteServerProject openProject(String projectName, RemoteSession session) {
        ServerProject serverProject = null;
        Project p = getOrCreateProject(projectName);
        if (p != null) {
            serverProject = getServerProject(p);
            if (serverProject == null) {
                serverProject = createServerProject(projectName, p);
                addServerProject(p, serverProject);
            }
            recordConnection(session, serverProject);
        }
        return serverProject;
    }

    private void recordConnection(RemoteSession session, ServerProject project) {
        // Log.enter(this, "recordConnection", session, project);
        Collection projects = (Collection) _sessionToProjectsMap.get(session);
        if (projects == null) {
            projects = new ArrayList();
            _sessionToProjectsMap.put(session, projects);
        }
        projects.add(project);
        project.register(session);
    }

    private void recordDisconnection(RemoteSession session, RemoteServerProject project) {
        // Log.enter(this, "recordDisconnection", session, project);
        Collection projects = (Collection) _sessionToProjectsMap.get(session);
        projects.remove(project);
        _sessions.remove(session);
        Log.getLogger().info("removing session: " + session);
    }

    private ServerProject getServerProject(String projectName) {
        Project p = getProject(projectName);
        return (p == null) ? null : getServerProject(p);
    }

    private ServerProject createServerProject(String name, Project p) {
        ServerProject impl = null;
        try {
            impl = new ServerProject(this, getURI(name), p);
        } catch (RemoteException e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return impl;
    }

    private URI getURI(String projectName) {
        String name = FileUtilities.urlEncode(projectName);
        return _baseURI.resolve(name);
    }

    public void disconnectFromProject(RemoteServerProject serverProject, RemoteSession session) {
        recordDisconnection(session, serverProject);
    }

    private ServerProject getServerProject(Project p) {
        return (ServerProject) _projectToServerProjectMap.get(p);
    }

    private void addServerProject(Project p, RemoteServerProject sp) {
        _projectToServerProjectMap.put(p, sp);
    }

    private Project getProject(String name) {
        return (Project) _nameToOpenProjectMap.get(name);
    }

    private Project getOrCreateProject(String name) {
        Project project = getProject(name);
        if (project == null) {
            project = createProject(name);
        }
        return project;
    }

    private Project createProject(String name) {
        Project project = null;
        Iterator i = _systemKb.getInstances(_projectCls).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            String projectName = (String) instance.getOwnSlotValue(_nameSlot);
            if (projectName.equals(name)) {
                String projectLocation = (String) instance.getOwnSlotValue(_locationSlot);
                projectLocation = localizeLocation(projectLocation);
                URI uri = URIUtilities.createURI(projectLocation);
                project = Project.loadProjectFromURI(uri, new ArrayList(), true);
                _projectPluginManager.afterLoad(project);
                localizeProject(project);
                _nameToOpenProjectMap.put(name, project);
                break;
            }
        }
        return project;
    }

    private static String localizeLocation(String location) {
        if (File.separatorChar != '\\') {
            location = location.replace('\\', File.separatorChar);
        }
        return location;
    }

    private static void localizeProject(Project project) {
        localizeKB(project.getKnowledgeBase());
        localizeKB(project.getInternalProjectKnowledgeBase());
    }

    private static void localizeKB(KnowledgeBase kb) {
        FrameStore fs = new LocalizeFrameStoreHandler(kb).newFrameStore();
        kb.insertFrameStore(fs);
    }

    public Collection getAvailableProjectNames(RemoteSession session) {
        List names = new ArrayList();
        Iterator i = _systemKb.getInstances(_projectCls).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            String fileName = (String) instance.getOwnSlotValue(_locationSlot);
            File file = new File(fileName);
            if (file.exists() && file.isFile()) {
                String name = (String) instance.getOwnSlotValue(_nameSlot);
                names.add(name);
            } else {
                Log.getLogger().warning("Missing project at " + fileName);
            }
        }
        Collections.sort(names);
        return names;
    }

    public Collection getCurrentSessions(String projectName, RemoteSession session) {
        Collection currentSessions;
        RemoteServerProject project = getServerProject(projectName);
        if (project == null) {
            currentSessions = Collections.EMPTY_LIST;
        } else {
            currentSessions = getCurrentSessions(project);
        }
        return currentSessions;
    }

    public Collection getCurrentSessions(RemoteServerProject project) {
        Collection sessions = new ArrayList();
        Iterator i = _sessionToProjectsMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            Collection projects = (Collection) entry.getValue();
            if (projects.contains(project)) {
                Session session = (Session) entry.getKey();
                if (isCurrent(session)) {
                    sessions.add(session);
                }
            }
        }
        return sessions;
    }

    private static boolean isCurrent(Session session) {
        return true;
        // return System.currentTimeMillis() - session.getLastAccessTime() > 10000;
    }
    


    private boolean isValid(String name, String password) {
        LoginContext lc;
        try {
            lc = new LoginContext("Protege",
                                  new ProtegeCallbackHandler(name, password));
            lc.login();
        }
        catch (Exception e) {
            Log.getLogger().warning("Failed login " + e);
            return false;
        }
        return true;
    }
    
    public boolean metaprojectAuthCheck(String name, String password) {
        boolean isValid = false;
        Iterator i = _systemKb.getInstances(_userCls).iterator();
        while (i.hasNext()) {
            Instance user = (Instance) i.next();
            String username = (String) user.getOwnSlotValue(_nameSlot);
            if (username.equals(name)) {
                String userpassword = (String) user.getOwnSlotValue(_passwordSlot);
                if (userpassword.equals(password)) {
                    isValid = true;
                    break;
                }
            }
        }
        return isValid;
    }

    public String toString() {
        return "Server";
    }

    private void startProjectUpdateThread() {
        if (_saveIntervalMsec != NO_SAVE) {
            _updateThread = new Thread("Save Projects") {
                public void run() {
                    try {
                        while (_updateThread == this) {
                            sleep(_saveIntervalMsec);
                            saveAllProjects();
                        }
                    } catch (Throwable e) {
                      Log.getLogger().log(Level.INFO, "Exception caught", e);
                    }
                }
            };
            _updateThread.setDaemon(true);
            _updateThread.start();
        }
    }

    private void saveAllProjects() {
        // Log.enter(this, "update");
        ///CLOVER:FLUSH
        Iterator i = _projectToServerProjectMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            Project project = (Project) entry.getKey();
            ServerProject serverProject = (ServerProject) entry.getValue();
            // Log.trace("checking " + project, this, "update");
            if (serverProject.isDirty()) {
                save(serverProject, project);
            }
        }
    }

    private static void save(ServerProject serverProject, Project project) {
        Log.getLogger().info("saving " + project);
        Collection errors = new ArrayList();
        synchronized (serverProject.getDomainKbFrameStore(null)) {
            synchronized (serverProject.getProjectKbFrameStore(null)) {
                project.save(errors);
            }
        }
        serverProject.setClean();
        dumpErrors(project, errors);
    }

    private static void dumpErrors(Project p, Collection errors) {
        if (!errors.isEmpty()) {
            Log.getLogger().warning("Unable to save project " + p);
            Iterator i = errors.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                Log.getLogger().warning("\t" + o.toString());
            }
        }
    }

    public void shutdown() {
        Log.getLogger().info("Received shutdown request.");
        saveAllProjects();
        Thread thread = new Thread() {
            public void run() {
              try {
                SystemUtilities.sleepMsec(100);
                Log.getLogger().info("Server exiting.");
                System.exit(0);
              } catch (Exception e) {
                Log.getLogger().log(Level.INFO, "Exception caught", e);
              }
            }
        };
        thread.start();
    }

    private void stopProjectUpdateThread() {
        _updateThread = null;
    }
}