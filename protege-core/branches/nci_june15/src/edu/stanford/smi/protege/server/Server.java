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
import java.util.Map.Entry;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.plugin.ProjectPluginManager;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.server.framestore.LocalizeFrameStoreHandler;
import edu.stanford.smi.protege.server.framestore.ServerSessionLost;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.MetaProjectInstance;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.UserInstance;
import edu.stanford.smi.protege.server.metaproject.MetaProject.ClsEnum;
import edu.stanford.smi.protege.server.metaproject.MetaProject.SlotEnum;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.storage.clips.ClipsKnowledgeBaseFactory;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.URIUtilities;

public class Server extends UnicastRemoteObject implements RemoteServer {
    private static final long serialVersionUID = 1675054259604532947L;
    
    private static Server serverInstance;
    private Map<String, Project> _nameToOpenProjectMap = new HashMap<String, Project>();
    private Map<Project, ServerProject> _projectToServerProjectMap = new HashMap<Project, ServerProject>();
 
    private URI metaprojectURI;
    private MetaProject metaproject;
    
    private List _sessions = new ArrayList();
    private URI _baseURI;
    private Map<RemoteSession, Collection<ServerProject>> _sessionToProjectsMap 
        = new HashMap<RemoteSession, Collection<ServerProject>>();
    private Thread _updateThread;
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
        for (Entry<String, Project> entry : serverInstance._nameToOpenProjectMap.entrySet()) {
            String name = entry.getKey();
            Project p = entry.getValue();
            Log.getLogger().info("Loading project plugins for project " + name);
            serverInstance._projectPluginManager.afterLoad(p);
        }
        Log.getLogger().info("Protege server ready to accept connections...");
    }
    
    public static Server getInstance() {
        return serverInstance;
    }
    
    public static Policy getPolicy() {
      return serverInstance.metaproject.getPolicy();
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

    private Server(String[] args) throws RemoteException, IOException {
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
        metaproject = new MetaProjectImpl(metaprojectURI);
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
                try {
                	createProject(name);
                } catch (Exception e) {
                	Log.getLogger().warning("Error at loading project: " + name + "Error message: "+ e.getMessage());					
				}
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
    
    public RemoteSession cloneSession(RemoteSession session) {
        if (!_sessions.contains(session)) {
            return null;
        }
        session =  new Session(session.getUserName(), session.getUserIpAddress(), session.getSessionGroup());
        _sessions.add(session);
        return session;
    }

    public void closeSession(RemoteSession session) {
        _sessions.remove(session);
    }
    
    public boolean isActive(RemoteSession session) {
        return _sessions.contains(session);
    }

    public RemoteServerProject openProject(String projectName, RemoteSession session) 
    throws ServerSessionLost {
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

    private void recordConnection(RemoteSession session, ServerProject project) 
    throws ServerSessionLost {
        // Log.enter(this, "recordConnection", session, project);
        Collection<ServerProject> projects = _sessionToProjectsMap.get(session);
        if (projects == null) {
            projects = new ArrayList<ServerProject>();
            _sessionToProjectsMap.put(session, projects);
        }
        projects.add(project);
        project.register(session);
    }

    private void recordDisconnection(RemoteSession session, RemoteServerProject project) 
    throws ServerSessionLost {
        // Log.enter(this, "recordDisconnection", session, project);
        Collection<ServerProject> projects =  _sessionToProjectsMap.get(session);
        projects.remove(project);
        _sessions.remove(session);
        if (project instanceof ServerProject) {
          ((ServerProject) project).deregister(session);
        }
        Log.getLogger().info("removing session: " + session);
    }

    public ServerProject getServerProject(String projectName) {
        Project p = getProject(projectName);
        return (p == null) ? null : getServerProject(p);
    }

    private ServerProject createServerProject(String name, Project p) {
        ServerProject impl = null;
        try {
            impl = new ServerProject(this, getURI(name), metaproject.getProjectInstance(name), p);
        } catch (RemoteException e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return impl;
    }

    private URI getURI(String projectName) {
        String name = FileUtilities.urlEncode(projectName);
        return _baseURI.resolve(name);
    }

    public void disconnectFromProject(RemoteServerProject serverProject, RemoteSession session) 
    throws ServerSessionLost {
        recordDisconnection(session, serverProject);
    }

    public ServerProject getServerProject(Project p) {
        return _projectToServerProjectMap.get(p);
    }

    private void addServerProject(Project p, ServerProject sp) {
        _projectToServerProjectMap.put(p, sp);
    }

    public Project getProject(String name) {
        return _nameToOpenProjectMap.get(name);
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
        
        for (MetaProjectInstance instance : metaproject.getProjectInstances()) {
          String projectName = instance.getName();
          if (projectName.equals(name)) {
            String projectLocation = instance.getLocation();
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

	public RemoteServerProject createProject(String newProjectName, RemoteSession session, KnowledgeBaseFactory kbfactory, boolean saveToMetaProject) throws RemoteException {
        Project project = null;
        
        for (MetaProjectInstance instance : metaproject.getProjectInstances()) {
            String projectName = instance.getName();
            if (projectName.equals(newProjectName)) {              
              	Log.getLogger().warning("Server: Attempting to create server project with existing project name. No server project created.");
              	return null;
              }
        }
                
        String newProjectsDir = ServerProperties.getDefaultNewProjectSaveDirectory();

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
        
        //TT: How to treat other knowledge base factories?
        if (kbfactory instanceof ClipsKnowledgeBaseFactory) {
        	ClipsKnowledgeBaseFactory.setSourceFiles(project.getSources(), newProjectName + ".pont", newProjectName + ".pins");
        }
              
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
        	//TT: Tim, can you please implement this method? Thanks!
        	//this
        	MetaProjectInstance newProjectInstance = metaproject.createMetaProjectInstance(newProjectName);
        	//and this
        	newProjectInstance.setLocation(newProjectsDir + File.separator + newProjectName + ".pprj");
        	        	
        	metaproject.save(errors);
        }
        
        return getServerProject(project);		
	}

    
    
    private static void localizeProject(Project project) {
        localizeKB(project.getKnowledgeBase());
        localizeKB(project.getInternalProjectKnowledgeBase());
    }

    private static void localizeKB(KnowledgeBase kb) {
        FrameStore fs = new LocalizeFrameStoreHandler(kb).newFrameStore();
        kb.insertFrameStore(fs);
    }

    public Collection<String> getAvailableProjectNames(RemoteSession session) {
        List<String> names = new ArrayList<String>();
        for (MetaProjectInstance instance : metaproject.getProjectInstances()) {
          String fileName = instance.getLocation();
          File file = new File(fileName);
          if (file.exists() && file.isFile()) {
              names.add(instance.getName());
          } else {
              Log.getLogger().warning("Missing project at " + fileName);
          }
        }
            
        Collections.sort(names);
        return names;
    }
    
    public Collection<ServerProject> getCurrentProjects(RemoteSession session) {
        return _sessionToProjectsMap.get(session);
    }
    
    public  Collection<RemoteSession> getCurrentSessions() {
        return _sessions;
    }

    @SuppressWarnings("unchecked")
    public Collection<RemoteSession> getCurrentSessions(String projectName, RemoteSession session) {
        Collection<RemoteSession> currentSessions;
        RemoteServerProject project = getServerProject(projectName);
        if (project == null) {
            currentSessions = Collections.EMPTY_LIST;
        } else {
            currentSessions = getCurrentSessions(project);
        }
        return currentSessions;
    }

    public Collection<RemoteSession> getCurrentSessions(RemoteServerProject project) {
        Collection<RemoteSession> sessions = new ArrayList<RemoteSession>();
        Iterator<Map.Entry<RemoteSession, Collection<ServerProject>>> i = _sessionToProjectsMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<RemoteSession, Collection<ServerProject>> entry = i.next();
            Collection<ServerProject> projects = entry.getValue();
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

    public boolean allowsCreateUsers() throws RemoteException {    	
    	return ServerProperties.getAllowsCreateUsers();
    }
    
	public boolean createUser(String userName, String password) {
		List<String> names = new ArrayList<String>();
		for (UserInstance instance : metaproject.getUserInstances()) {
			String existingUserName = instance.getName();
			if (existingUserName.equals(userName)) {
				Log.getLogger().warning(
						"Server: Could not create user with name " + userName
								+ ". User name already exists.");
				return false;
			}
		}
		UserInstance newUserInstance = metaproject.createUserInstance(userName,
				password);

		ArrayList errors = new ArrayList();
		boolean success = metaproject.save(errors);
				
		return (success && errors.size() == 0);
	}
    
    private boolean isValid(String name, String password) {
      boolean isValid = false;
      for (UserInstance ui : metaproject.getUserInstances()) {
        String username = ui.getName();
        if (username.equals(name)) {
          String userpassword = ui.getPassword();
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
        Iterator<Map.Entry<Project, ServerProject>> i = _projectToServerProjectMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<Project, ServerProject> entry =  i.next();
            Project project = entry.getKey();
            ServerProject serverProject = entry.getValue();
            // Log.trace("checking " + project, this, "update");
            if (serverProject.isDirty()) {
                save(serverProject, project);
            }
        }
    }

    private static void save(ServerProject serverProject, Project project) {
        Log.getLogger().info("saving " + project);
        Collection errors = new ArrayList();
        /*
         * The order of these synchronize statements is critical.  There is some 
         * OWLFrameStore code (which holds the knowledgebase lock) that makes calls
         * to the internal project knowledge base to get configuration parameters.
         */
        synchronized (project.getKnowledgeBase()) {
            synchronized (project.getInternalProjectKnowledgeBase()) {
                project.save(errors);
                serverInstance._projectPluginManager.afterSave(project);
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
                for (Project p : _projectToServerProjectMap.keySet()) { 
                    /*
                     * The order of these synchronize statements is critical.  There is some 
                     * OWLFrameStore code (which holds the knowledgebase lock) that makes calls
                     * to the internal project knowledge base to get configuration parameters.
                     */
                    synchronized(p.getKnowledgeBase()) {
                        synchronized (p.getInternalProjectKnowledgeBase()) {
                            try {
                                _projectPluginManager.beforeClose(p);
                            }
                            catch (Exception e) {
                                Log.getLogger().log(Level.INFO, "Exception caught cleaning up", e);
                            }
                        }
                    }
                }
              } catch (Exception e) {
                Log.getLogger().log(Level.INFO, "Exception caught", e);
              }
              finally {
                  System.exit(0);
              }
            }
        };
        thread.start();
    }

    private void stopProjectUpdateThread() {
        _updateThread = null;
    }
    
    public void setFrameCalculatorDisabled(boolean disabled) {
      for (ServerProject sp : _projectToServerProjectMap.values()) {
        sp.setFrameCalculatorDisabled(disabled);
      }
    }
    
    /* -----------------------------------------------------------------------
     * MetaProject utilities
     */
    public KnowledgeBase getMetaProject() {
        return metaproject.getKnowledgeBase();
    }
    
    public Cls getProjectCls()  {
        return metaproject.getCls(ClsEnum.Project);
    }
    
    public Slot getNameSlot() {
        return metaproject.getSlot(SlotEnum.name);
    }
    
    

}