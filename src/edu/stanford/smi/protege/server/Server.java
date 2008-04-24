package edu.stanford.smi.protege.server;

//ESCA*JAVA0100

import java.io.*;
import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.util.logging.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.plugin.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.server.framestore.LocalizeFrameStoreHandler;
import edu.stanford.smi.protege.storage.clips.ClipsKnowledgeBaseFactory;
import edu.stanford.smi.protege.util.*;

public class Server extends UnicastRemoteObject implements RemoteServer {
	private final static String SERVER_NEW_PROJECTS_SAVE_DIRECTORY_PROTEGE_PROPERTY = "server.newproject.save.directory";
	public final static String SERVER_ALLOW_CREATE_USERS = "server.allow.create.users";
	
    private static Server serverInstance;
    private Map<String, Project> _nameToOpenProjectMap = new HashMap<String, Project>();
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
     * Calls startServer with {@link ServerRmiSocketFactory}as the socket factory.
     */
    public static void startServer(String[] args) throws IOException {
        startServer(args, new ServerRmiSocketFactory());
    }

    /**
     * Start up the server.
     * 
     * @param args
     *            the arguments to the server
     * @param sf
     *            the socket factory to be used
     * @throws IOException
     *             if the socket factory has already been set
     * @see RMISocketFactory#setSocketFactory(RMISocketFactory)
     */
    public static void startServer(String[] args, RMISocketFactory sf) throws IOException {
        System.setProperty("java.rmi.server.RMIClassLoaderSpi", ProtegeRmiClassLoaderSpi.class.getName());
        RMISocketFactory.setSocketFactory(sf);
        SystemUtilities.initialize();
        serverInstance = new Server(args);
        for (Project p : serverInstance._nameToOpenProjectMap.values()) {
            serverInstance._projectPluginManager.afterLoad(p);
        }
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
        int port = Integer.getInteger("protege.rmi.registry.port", Registry.REGISTRY_PORT).intValue();
        return LocateRegistry.getRegistry(null, port, RMISocketFactory.getSocketFactory());
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
            //TT: Use a different logger
            Log.getLogger().info("Server: User " + username + " (IP:" + userIpAddress + ") logged to the server at " + new Date());
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
//      TT: Use a different logger
        Log.getLogger().info("Server: User " + session.getUserName() + " (IP:" + session.getUserIpAddress() + ") opened project " + projectName + " at " + new Date());
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
        Log.getLogger().info("Server: Removing session: " + session);
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

    public Project getProject(String name) {
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
                if (serverInstance != null) _projectPluginManager.afterLoad(project);
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

    
	public RemoteServerProject createProject(String newProjectName, RemoteSession session, KnowledgeBaseFactory kbfactory, boolean saveToMetaProject) throws RemoteException {
        Project project = null;
        Iterator i = _systemKb.getInstances(_projectCls).iterator();
        while (i.hasNext()) {
            Instance instance = (Instance) i.next();
            String existingProjectName = (String) instance.getOwnSlotValue(_nameSlot);
            if (existingProjectName.equals(newProjectName)) {
            	Log.getLogger().warning("Server: Attempting to create server project with existing project name. No server project created.");
            	return null;
            }
        }
    
        String defaultSaveDir = ApplicationProperties.getApplicationDirectory().getAbsolutePath();
        
        String newProjectsDir = ApplicationProperties.getApplicationOrSystemProperty(SERVER_NEW_PROJECTS_SAVE_DIRECTORY_PROTEGE_PROPERTY, defaultSaveDir);

        URI uri = URIUtilities.createURI(newProjectsDir + File.separator + newProjectName + ".pprj");
        
        if (uri == null) {
        	Log.getLogger().warning("Could not create new server project at location " + newProjectsDir + File.separator + newProjectName + ".pprj");
        	return null;
        }

        ArrayList errors = new ArrayList();
        
        project = Project.createNewProject(kbfactory, errors);
        Log.getLogger().info("Server: Created server project at: " + uri);
        
        if (errors.size() > 0) {
        	Log.getLogger().warning("Server: Errors at creating new project " + newProjectName);
        	return null;
        }

        project.setProjectURI(uri);
        
        //how to treat other cases?
        if (kbfactory instanceof ClipsKnowledgeBaseFactory) {
        	ClipsKnowledgeBaseFactory.setSourceFiles(project.getSources(), newProjectName + ".pont", newProjectName + ".pins");
        }
        
        //should we do that?
        project.save(errors);
        if (errors.size() > 0) {
        	Log.getLogger().warning("Server: Errors at saving new project " + newProjectName);
        	return null;
        }
        
        project = Project.loadProjectFromURI(uri, new ArrayList(), true);

        if (serverInstance != null) {
        	_projectPluginManager.afterLoad(project);
        }
        
        localizeProject(project);
        _nameToOpenProjectMap.put(newProjectName, project);
        
        if (saveToMetaProject) {
        	Instance newProjectInstance = _projectCls.createDirectInstance(null);
        	newProjectInstance.setOwnSlotValue(_nameSlot, newProjectName);
        	newProjectInstance.setOwnSlotValue(_locationSlot, newProjectsDir + File.separator + newProjectName + ".pprj");
 
        	saveMetaProject();
         	
        }
        
        return getServerProject(project);		
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

	public boolean createUser(String userName, String password) {
		Collection existingUsers = _systemKb.getFramesWithValue(_nameSlot, null, false, userName);
		
		if (existingUsers.size() > 0) {
			Log.getLogger().warning("Server: Could not create user with name " + userName+ ". User name already exists.");
			return false;
		}
		
		Instance userInstance = _userCls.createDirectInstance(null);
		userInstance.setOwnSlotValue(_nameSlot, userName);
		userInstance.setOwnSlotValue(_passwordSlot, password);
			
		return saveMetaProject();
	}



	private boolean isValid(String name, String password) {
        boolean isValid = false;
        Iterator i = _systemKb.getInstances(_userCls).iterator();
        while (i.hasNext()) {
            Instance user = (Instance) i.next();
            String username = (String) user.getOwnSlotValue(_nameSlot);
            if (username != null && username.equals(name)) {
                String userpassword = (String) user.getOwnSlotValue(_passwordSlot);
                if (userpassword != null && userpassword.equals(password)) {
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
    /*********************************************************
     * Meta-project stuff.
     */
    public KnowledgeBase getMetaProject() {
        return _systemKb;
    }
    
    public Cls getProjectCls() {
        return _projectCls;
    }
    
    public Slot getNameSlot() {
        return _nameSlot;
    }

    
    private boolean saveMetaProject() {
    	ArrayList errors = new ArrayList();
    	
       	getMetaProject().getProject().save(errors);
    	if (errors.size() > 0) {
    		Log.getLogger().warning("Server: Errors at saving metaproject");
    		return false;
    	}
    	
    	Log.getLogger().info("Saved metaproject");
	    	
    	return true;
	}
    
}