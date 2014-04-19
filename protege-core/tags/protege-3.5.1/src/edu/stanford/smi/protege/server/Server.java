package edu.stanford.smi.protege.server;

//ESCA*JAVA0100
import java.io.BufferedReader;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.event.ServerProjectNotificationEvent;
import edu.stanford.smi.protege.event.ServerProjectSessionClosedEvent;
import edu.stanford.smi.protege.event.ServerProjectStatusChangeEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.EventGeneratorFrameStore;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.plugin.ProjectPluginManager;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;
import edu.stanford.smi.protege.server.framestore.LocalizeFrameStoreHandler;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerSessionLost;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.Policy;
import edu.stanford.smi.protege.server.metaproject.PolicyControlledObject;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.metaproject.ServerInstance;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl.SlotEnum;
import edu.stanford.smi.protege.server.socket.RmiSocketFactory;
import edu.stanford.smi.protege.server.socket.SSLFactory;
import edu.stanford.smi.protege.server.util.ProjectInfo;
import edu.stanford.smi.protege.server.util.ServerUtil;
import edu.stanford.smi.protege.storage.clips.ClipsKnowledgeBaseFactory;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ServerJob;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.URIUtilities;

public class Server extends UnicastRemoteObject implements RemoteServer {
    private static final long serialVersionUID = 1675054259604532947L;

    private static final transient Logger log = Log.getLogger(Server.class);

    public static final int NO_SAVE = -1;
    private static final String SAVE_INTERVAL_OPTION = "-saveIntervalSec=";
    private static final String NOPRELOAD_OPTION = "-nopreload";
    private static final String OPTION_CHAR = "-";

    private ThreadFactory daemonizingThreadFactory = new ThreadFactory() {

        public Thread newThread(Runnable r) {
            Thread th = new Thread(r);
            th.setDaemon(true);
            return th;
        }
    };

    /* Synchronization - Lock sequence and deadlocks:
     * The serverInstance monitor is taken before any other locks.
     * The Server.class monitor is no longer used - this protection is
     * ensured by not exposing the Server instance to RMI until the
     * server is initialized. 
     */
      
    private final ExecutorService serverExecutor = Executors.newCachedThreadPool(daemonizingThreadFactory);

    /* Synchronization:
     * There may be a delay before a change to the meta project is
     * seen by  the server variables.  But the server variables need
     * to be synchronized when they are updated and they need to be
     * updated in a sequence that matches the updates to the meta
     * project.  The synchronization of the server variables cannot be
     * done within the metaproject listener threads  because those
     * threads will hold the metaproject knowledge base lock.
     */
    private  final ExecutorService singleJobAtATimeExecutor = Executors.newSingleThreadExecutor(daemonizingThreadFactory);


    /* Synchronization:
     * These guys are essentially constant.  They are set during
     * initialization and then used elsewhere.
     */
    private URI metaprojectURI;
    private URI _baseURI;
    private boolean preload = true;

    /* Synchronization:
     * This variable is set atomically and there are no relevant invariants.
     */
    private volatile int _saveIntervalMsec = NO_SAVE;


    /* Synchronization:
     * This variable is assigned atomically during initialization and
     * reinitialization.  This assignment should not be exposed
     * because the name should not be bound (and accessible through
     * RMI) until initialization is complete.  In particular look at
     * the usage of bind/unBind name in reinitialize.
     */
    private static volatile Server serverInstance;


    /* Synchronization:
     * This variable is changed from null to a value in
     * openMetaProject which is synchronized by the serverInstance
     * monitor.
     */
    private ServerProject serverMetaProject;

    /* Synchronization:
     * This variable is protected by the serverInstance monitor.
     */
    private volatile Thread _updateThread;

    /* Synchronization:
     * These variables are only set during initialization and
     * shutdown.  Race conditions should not occur because the server
     * name is unbound before reinitialization or shutdown are bound
     * only at the end of the initialization sequence.  Internally
     * these classes (metaproject, projectCls, nameSlot) do their own
     * synchronization. 
     */

    private MetaProject metaproject;
    //listener used for setting the status of the newly create projects to closed for maintenance.
    private FrameListener metaprojectFrameListener;
    private FrameListener metaprojectProjectClsListener;
    //cache for convenience and maybe performance
    private Cls projectCls;
    private Slot nameSlot;


    /* Synchronization:
     * I think that after initialization the contents of this variable
     * are essentially constant (?).
     */
    private final ProjectPluginManager _projectPluginManager = new ProjectPluginManager();

    /* Synchronization:
     * These variables are final but their contents needs to be
     * protected.  They are protected by the Server instance monitor. I
     * am considering making these synchronized maps but there are
     * invariants relating these variables that need to be protected
     * as well.  For instance a project with sessions should be an
     * open project.
     */
    private final Map<String, Project> _nameToOpenProjectMap = new HashMap<String, Project>();
    private final Map<String, ProjectStatus> _nameToProjectStatusMap = new  TreeMap<String, ProjectStatus>(); //sorted map by keys
    private final Map<Project, ServerProject> _projectToServerProjectMap = new HashMap<Project, ServerProject>();
    private final Map<RemoteSession, Collection<ServerProject>> _sessionToProjectsMap
        = new HashMap<RemoteSession, Collection<ServerProject>>();

    /* Synchronization:
     * By and large this list is synchronized by the the server
     * instance monitor.  But it is ok for the isActive method to  
     * return slightly stale data and synchronizing this method will
     * cause a  deadlock.  For this reason this is a synchronized list.
     */
    private List<RemoteSession> _sessions = Collections.synchronizedList(new ArrayList<RemoteSession>());


    /* Synchronization:
     * This variable is protected by the serverInstance monitor.
     */
    private final Map<String, FutureTask<Object>> projectToShutdownTaskNotificationMap = new HashMap<String, FutureTask<Object>>();

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
     * @param sf
     *            the socket factory to be used
     * @throws IOException
     *             if the socket factory has already been set
     * @see RMISocketFactory#setSocketFactory(RMISocketFactory)
     */
    public static void startServer(String[] args) throws IOException {
    	Log.getLogger().info("Protege server is starting...");
    	SystemUtilities.logSystemInfo();
        System.setProperty("java.rmi.server.RMIClassLoaderSpi", ProtegeRmiClassLoaderSpi.class.getName());
        // WARNING!!! the checkRegistry must go after the setProperty above or there will be class loader problems
        // not really understood...
        checkRegistry();
        SystemUtilities.initialize();
        serverInstance = new Server(args);
        afterLoad();
        serverInstance.bindName();
        Log.getLogger().info("Protege server ready to accept connections...");
    }
    
    private static void checkRegistry() throws IOException {
        try {
            getRegistry().list();
        }
        catch (RemoteException re) {
            log.log(Level.WARNING, "Is the registry running?", re);
            throw new IOException("Could not connect to the rmi registry task");
        }
    }


    private static void afterLoad() {
        for (Entry<String, Project> entry : serverInstance._nameToOpenProjectMap.entrySet()) {
            String name = entry.getKey();
            Project p = entry.getValue();
            Log.getLogger().info("Loading project plugins for project " + name);
            serverInstance._projectPluginManager.afterLoad(p);
        }
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

    public int getSaveIntervalMsec() {
        return _saveIntervalMsec;
    }

    public void setSaveIntervalMsec(int saveIntervalMsec) {
        this._saveIntervalMsec = saveIntervalMsec;
    }

    public static Registry getRegistry() throws RemoteException {
        int port = Integer.getInteger(RmiSocketFactory.REGISTRY_PORT, Registry.REGISTRY_PORT).intValue();
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

    private Server(String[] args) throws RemoteException, IOException {
        super(SSLFactory.getServerPort(SSLFactory.Context.LOGIN),
              new RmiSocketFactory(SSLFactory.Context.LOGIN),
              new RmiSocketFactory(SSLFactory.Context.LOGIN));
        parseArgs(args);
        initialize();
    }

    private void clear() {
        _nameToOpenProjectMap.clear();
        _nameToProjectStatusMap.clear();
        _projectToServerProjectMap.clear();
        _sessions.clear();
        _sessionToProjectsMap.clear();
        removeMetaProjectListeners();
        stopProjectUpdateThread();
    }

    private void initialize() throws RemoteException {
    	Log.getLogger().info("Using metaproject from: " + metaprojectURI);
        metaproject = new MetaProjectImpl(metaprojectURI, true);
        serverMetaProject = null;
        ServerUtil.fixMetaProject(metaproject);
        initializeProjects();
        addMetaProjectListeners();
        startProjectUpdateThread();
    }


	private void initializeProjects() {
        Iterator i = getAvailableProjectNames(null).iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            _nameToProjectStatusMap.put(name, ProjectStatus.READY);
            if (preload) {
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
    
    protected void unBindName() throws RemoteException {
        try {
            String boundName = getLocalBoundName();
            getRegistry().unbind(boundName);
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


    private boolean readAllowed(String projectName, RemoteSession session) {
        Policy policy = metaproject.getPolicy();
        User user = policy.getUserByName(session.getUserName());
        ProjectInstance projectInstance = metaproject.getProject(projectName);
        /*
         * Temporary projects (e.g. projects that do not appear in the metaproject like the chat project)
         * cannot have an associated policy.  So for things to work we must allow access.
         */
        if (projectInstance == null) {
            return true;
        }
        return policy.isOperationAuthorized(user, MetaProjectConstants.OPERATION_READ, projectInstance);
    }

    private void recordConnection(RemoteSession session, ServerProject project)
    throws ServerSessionLost {
        Collection<ServerProject> projects = _sessionToProjectsMap.get(session);
        if (projects == null) {
            projects = new ArrayList<ServerProject>();
            _sessionToProjectsMap.put(session, projects);
        }
        projects.add(project);
        project.register(session);
        Log.getLogger().info("Server: Adding " + session + " on " + new Date());
    }

    private void recordDisconnection(RemoteSession session, RemoteServerProject project)
    throws ServerSessionLost {
        Collection<ServerProject> projects =  _sessionToProjectsMap.get(session);
        if (projects != null) {
        	projects.remove(project);
        }
        if (projects == null ||projects.isEmpty()) {
        	_sessionToProjectsMap.remove(session);
        	_sessions.remove(session);
        }
        if (project instanceof ServerProject) {
          ((ServerProject) project).deregister(session);
        }
        Log.getLogger().info("Server: Removing " + session + " on " + new Date());
    }

    public synchronized ServerProject getServerProject(String projectName) {
        Project p = getProject(projectName);
        return p == null ? null : getServerProject(p);
    }

    private ServerProject createServerProject(String name, Project p) {
        ServerProject impl = null;
        try {
            impl = new ServerProject(this, getURI(name), metaproject.getProject(name), p);
        } catch (RemoteException e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return impl;
    }

    private URI getURI(String projectName) {
        String name = FileUtilities.urlEncode(projectName);
        return _baseURI.resolve(name);
    }


    private void addServerProject(Project p, ServerProject sp) {
        _projectToServerProjectMap.put(p, sp);
    }

    public synchronized Project getOrCreateProject(String name) {
        Project project = getProject(name);
        if (project == null) {
            project = createProject(name);
        }
        return project;
    }



    private Project createProject(String name) {
        if (_nameToProjectStatusMap.get(name) != ProjectStatus.READY) {
            return null;
        }

        Project project = null;

        for (ProjectInstance instance : metaproject.getProjects()) {
          String projectName = instance.getName();
          if (projectName.equals(name)) {
            String projectLocation = instance.getLocation();
            URI uri = URIUtilities.createURI(projectLocation);
            Log.getLogger().info("Loading project " + name + " from " + uri);
            List errors = new ArrayList();
            project = Project.loadProjectFromURI(uri, errors, true);
            Log.handleErrors(log, Level.WARNING, errors);
            if (serverInstance != null) {
				_projectPluginManager.afterLoad(project);
			}
            localizeProject(project);
            _nameToOpenProjectMap.put(name, project);
            break;
          }
        }
        return project;
    }


    private static void localizeProject(Project project) {
        localizeKB(project.getKnowledgeBase());
        localizeKB(project.getInternalProjectKnowledgeBase());
    }

    private static void localizeKB(KnowledgeBase kb) {
        FrameStore fs = new LocalizeFrameStoreHandler(kb).newFrameStore();
        kb.insertFrameStore(fs);
    }





    private static boolean isCurrent(Session session) {
        return true;
        // return System.currentTimeMillis() - session.getLastAccessTime() > 10000;
    }


    private boolean isValid(String name, String password) {
        User user = metaproject.getUser(name);
        if (user == null) {
            return false;
        }
        return user.verifyPassword(password);
    }

	public String toString() {
        return "Server";
    }

    private synchronized void startProjectUpdateThread() {
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


    private synchronized void stopProjectUpdateThread() {
        _updateThread = null;
    }

    public synchronized boolean saveMetaProject(RemoteSession session) throws RemoteException {
        Collection errors = new ArrayList();
        log.info("Saving metaproject...");
        metaproject.save(errors);
        if (!errors.isEmpty()) {
            log.warning("Errors found saving metaproject");
            dumpErrors(((MetaProjectImpl)metaproject).getKnowledgeBase().getProject(), errors);
        }
        return errors.size() == 0;
    }


    @SuppressWarnings("unchecked")
    public synchronized void saveAllProjects() {
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
        Collection errors  = new ArrayList();
        metaproject.save(errors);
        if (!errors.isEmpty()) {
            log.warning("Errors found saving metaproject");
            dumpErrors(((MetaProjectImpl)metaproject).getKnowledgeBase().getProject(), errors);
        }
    }

    private static void save(ServerProject serverProject, Project project) {
        Log.getLogger().info("Saving " + project);
        Collection errors = new ArrayList();
        /*
         * The order of these synchronize statements is critical.  There is some
         * OWLFrameStore code (which holds the knowledgebase lock) that makes calls
         * to the internal project knowledge base to get configuration parameters.
         */
        synchronized (project.getKnowledgeBase()) {
            synchronized (project.getInternalProjectKnowledgeBase()) {
            	/* TT: Save only the domain kb, not the prj kb.
            	 * Saving the prj kb while a client opens a
            	 * remote project can corrupt the client prj kb.
            	 */
            	KnowledgeBase kb = project.getKnowledgeBase();
            	KnowledgeBaseFactory factory = kb.getKnowledgeBaseFactory();
            	factory.saveKnowledgeBase(kb, project.getSources(), errors);
                serverInstance._projectPluginManager.afterSave(project);
            }
        }
        //TODO: ask Tim
        if (serverProject != null) {
            serverProject.setClean();
        }
        dumpErrors(project, errors);
    }

    private static void dumpErrors(Project p, Collection errors) {
        if (!errors.isEmpty()) {
            Log.getLogger().warning("Unable to save project " + p);
            Log.handleErrors(log, Level.WARNING, errors);
        }
    }


    private void closeProject(String name) {
        Project p = _nameToOpenProjectMap.get(name);
        ServerProject sp = _projectToServerProjectMap.get(p);
        Set<Session> sessionsToRemove = new HashSet<Session>();
        for (Entry<RemoteSession, Collection<ServerProject>> entry  : _sessionToProjectsMap.entrySet()) {
            RemoteSession session = entry.getKey();
            Collection<ServerProject> serverProjects  = entry.getValue();
            if (serverProjects.remove(sp)) {
                try {
                    sp.deregister(session);
                }
                catch (ServerSessionLost ssl) {
                    log.log(Level.WARNING, "Unexpected exception deregistering client", ssl);
                }
            }
            if (serverProjects.isEmpty() && session instanceof Session) {
                sessionsToRemove.add((Session) session);
            }
        }
        for (Session session : sessionsToRemove) {
            _sessions.remove(session);
            _sessionToProjectsMap.remove(session);
        }

        /*
         * Save project
         */
        save(sp, p);

        p.dispose();
        _projectToServerProjectMap.remove(p);
        _nameToOpenProjectMap.remove(name);
    }


    /* -----------------------------------------------------------------
     * Services available on the server
     */


    /* Synchronization:
     * This method can't be synchronized because it is used locally
     * by the database backend.  So it may return stale data and the
     * _sessions list must be a synchronized list.
     */
    public boolean isActive(RemoteSession session) {
        return _sessions.contains(session);
    }

    public synchronized void disconnectFromProject(RemoteServerProject serverProject, RemoteSession session)
    throws ServerSessionLost {
        recordDisconnection(session, serverProject);
    }

    public synchronized Project getProject(String name) {
        return _nameToOpenProjectMap.get(name);
    }


    public synchronized ServerProject getServerProject(Project p) {
        return _projectToServerProjectMap.get(p);
    }


    public synchronized ProjectStatus getProjectStatus(String name) {
        return _nameToProjectStatusMap.get(name);
    }


    public synchronized void setProjectStatus(String name, ProjectStatus status) {
        ProjectStatus oldStatus  = _nameToProjectStatusMap.put(name, status);
        Project  p = _nameToOpenProjectMap.get(name);
        if (p != null) {
            KnowledgeBase kb = p.getKnowledgeBase();
            EventGeneratorFrameStore fs = kb.getFrameStoreManager().getFrameStoreFromClass(EventGeneratorFrameStore.class);
            fs.addCustomEvent(new ServerProjectStatusChangeEvent(name, oldStatus, status));
        }
    }


    public synchronized Collection<ServerProject> getCurrentProjects(RemoteSession session) {
        return _sessionToProjectsMap.get(session);
    }


    public synchronized  Collection<RemoteSession> getCurrentSessions() {
        return new ArrayList<RemoteSession>(_sessions);
    }

    public synchronized Collection<RemoteSession> getCurrentSessions(RemoteServerProject project) {
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



    public synchronized void setFrameCalculatorDisabled(boolean disabled) {
      for (ServerProject sp : _projectToServerProjectMap.values()) {
        sp.setFrameCalculatorDisabled(disabled);
      }
    }


    /* -----------------------------------------------------------------------
     * MetaProject utilities
     */

    public synchronized MetaProject getMetaProjectNew() {
        return metaproject;
    }

    /**
     * @deprecated In ProtegeJobs use getMetaProjectInstance
     */
    @Deprecated
    public synchronized KnowledgeBase getMetaProject() {
        return ((MetaProjectImpl) metaproject).getKnowledgeBase();
    }

    /**
     * @deprecated In ProtegeJobs use getMetaProjectInstance
     */
    @Deprecated
    public synchronized Cls getProjectCls()  {
        if (projectCls == null) {
            projectCls = ((MetaProjectImpl) metaproject).getCls(MetaProjectImpl.ClsEnum.Project);
        }
        return projectCls;
    }

    /**
     * @deprecated In ProtegeJobs use getMetaProjectInstance
     */
    @Deprecated
    public synchronized Slot getNameSlot() {
        if (nameSlot == null) {
            nameSlot = ((MetaProjectImpl) metaproject).getSlot(SlotEnum.name);
        }
        return nameSlot;
    }


    private FrameListener getMetaProjectFrameListener() {
        if (metaprojectFrameListener == null) {
            metaprojectFrameListener = new FrameAdapter() {
                /* Synchronization:
                 * don't do this  inside the listener method
                 * it takes the Server lock and can deadlock.
                 * note that the listener is invoked by the last access time updater
                 */
                private Cls projectClass = getProjectCls();

                public void ownSlotValueChanged(FrameEvent event) {
                    Instance frame = (Instance)event.getFrame();
                    if (frame.hasType(projectClass) && event.getSlot().equals(nameSlot)) {
                        List oldValues = event.getOldValues();
                        final String oldName = 
                            (oldValues != null && oldValues.size() > 0) ? CollectionUtilities.getFirstItem(oldValues).toString() : new String();

                            final String name = (String) frame.getOwnSlotValue(nameSlot);
                            singleJobAtATimeExecutor.submit(new Runnable() {
                                public void run() {
                                    synchronized (Server.this) {
                                        if (name != null && name.length() > 0) {
                                            if (oldName.length() == 0) {  //new project
                                                _nameToProjectStatusMap.put(name, ProjectStatus.CLOSED_FOR_MAINTENANCE);
                                                log.info("Project instance " + name + " was added to the metaproject.");
                                            } else { //rename of a project
                                                _nameToOpenProjectMap.put(name, _nameToOpenProjectMap.get(oldName));
                                                _nameToProjectStatusMap.put(name, _nameToProjectStatusMap.get(oldName));

                                                _nameToOpenProjectMap.remove(oldName);
                                                _nameToProjectStatusMap.remove(oldName);
                                                log.info("Project instance name was changed. Old name: " + oldName + "; New name: " + name);
                                            }
                                        }
                                    }
                                }
                            });
                    }
                }
            };
        }
        return metaprojectFrameListener;
    }

    private FrameListener getMetaProjectProjectClsListener() {
        if (metaprojectProjectClsListener == null) {

            metaprojectProjectClsListener = new FrameAdapter() {
                public void ownSlotValueChanged(FrameEvent event) {
                    singleJobAtATimeExecutor.submit(new Runnable() {
                        public void run() {
                            synchronized (Server.this) {
                                //need to find out which project instance was deleted..
                                Set<String> keySet = new HashSet<String>(_nameToProjectStatusMap.keySet());
                                for (String name : keySet) {
                                    ProjectInstance prj = metaproject.getProject(name);
                                    if (prj == null) {
                                        Project p = _nameToOpenProjectMap.remove(name);
                                        _nameToProjectStatusMap.remove(name);
                                        if (p != null) {
                                            _projectToServerProjectMap.remove(p);
                                            p.dispose();
                                        }
                                        log.info("Project instance " + name + " was removed from the metaproject");
                                    }
                                }
                            }
                        }
                    });
                }
            };
        }
        return metaprojectProjectClsListener;
    }

    private void addMetaProjectListeners() {
        if (metaproject != null) {
            getProjectCls();
            getNameSlot();

            KnowledgeBase kb = ((MetaProjectImpl)metaproject).getKnowledgeBase();
            ServerFrameStore.requestEventDispatch(kb);
            getMetaProjectFrameListener();
            kb.addFrameListener(metaprojectFrameListener);

            getMetaProjectProjectClsListener();
            projectCls.addFrameListener(metaprojectProjectClsListener);
        }
    }

    private void removeMetaProjectListeners() {
        if (metaprojectFrameListener != null) {
            try {
                ((MetaProjectImpl)metaproject).getKnowledgeBase().removeFrameListener(metaprojectFrameListener);
            } catch (Exception e) {
                log.log(Level.WARNING, "Could not remove metaproject frame listener", e);
            }
        }

        if (metaprojectProjectClsListener != null) {
            try {
                projectCls.removeFrameListener(metaprojectProjectClsListener);
            } catch (Exception e) {
                log.log(Level.WARNING, "Could not remove metaproject cls listener for cls Project", e);
            }
        }
    }

    /* -----------------------------------------------------------------
     * RemoteServer Interfaces
     */

    /* Synchronization:
     * This routine is currently only called by Junit code.  If it is
     * called elsewhere we need to be  sure that there is no residual
     * use of serverInstance when it is set to null and that the
     * server interfaces are not exposed before serverInstance is
     * reassigned.
     */
    public synchronized void reinitialize() throws RemoteException {
        Log.getLogger().info("Server reinitializing");
        unBindName();
        clear();
        serverInstance = null;
        initialize();
        serverInstance = this;
        afterLoad();
        bindName();
    }

    /* -----------------------------------------------------------------
     * Session Management
     */

   public synchronized RemoteSession openSession(String username, String userIpAddress, String password) {
        RemoteSession session = null;
        if (isValid(username, password)) {
            boolean allowsDelegation = isServerOperationAllowed(new Session(username, userIpAddress, false), MetaProjectConstants.OPERATION_DELEGATE);
            session = new Session(username, userIpAddress, allowsDelegation);
            _sessions.add(session);
            metaproject.getUser(username).setLastLogin(new Date());
        } else {
        	Log.getLogger().warning("Failed login for user " + username + " IP: " + userIpAddress);
        }

        return session;
    }

    public synchronized RemoteSession cloneSession(RemoteSession session) {
        if (!_sessions.contains(session)) {
            return null;
        }
        session =  new Session(session.getUserName(), session.getUserIpAddress(), session.allowDelegation());
        _sessions.add(session);
        return session;
    }

    public synchronized void closeSession(RemoteSession session) {
        _sessions.remove(session);
    }


    @SuppressWarnings("unchecked")
    public synchronized Collection<RemoteSession> getCurrentSessions(String projectName, RemoteSession session) {
        Collection<RemoteSession> currentSessions;
        RemoteServerProject project = getServerProject(projectName);
        if (project == null) {
            currentSessions = Collections.EMPTY_LIST;
        } else {
            currentSessions = getCurrentSessions(project);
        }
        return currentSessions;
    }


    /* -----------------------------------------------------------------
     * Project Access
     */

    public synchronized Collection<String> getAvailableProjectNames(RemoteSession session) {
        Policy policy = metaproject.getPolicy();
        User user = session != null ? policy.getUserByName(session.getUserName()) : null;
        List<String> names = new ArrayList<String>();
        for (ProjectInstance instance : metaproject.getProjects()) {
            if (user != null && (
                    !policy.isOperationAuthorized(user,
                            MetaProjectConstants.OPERATION_DISPLAY_IN_PROJECT_LIST,
                            instance) ||
                            !policy.isOperationAuthorized(user,
                                    MetaProjectConstants.OPERATION_READ,
                                    instance))) {
                continue;
            }
            String name = instance.getName();
            ProjectStatus status = _nameToProjectStatusMap.get(name);
            if (status != null && status != ProjectStatus.READY) {
                continue;
            }
            String fileName = instance.getLocation();
            if (fileName == null || fileName.length() == 0) {
                log.warning("Project with empty location will be ignored: " + name);
            } else {
                URI uri = URIUtilities.createURI(fileName);
                String scheme = uri.getScheme();
                if (scheme != null && scheme.contains("http")) {
                    BufferedReader reader = URIUtilities.createBufferedReader(uri);
                    if (reader != null) {
                        names.add(instance.getName());
                        FileUtilities.close(reader);
                    } else {
                        Log.getLogger().warning("Missing project at " + fileName);
                    }
                } else {
                    File file = new File(fileName);
                    if (file.exists() && file.isFile()) {
                        names.add(instance.getName());
                    } else {
                        Log.getLogger().warning("Missing project at " + fileName);
                    }
                }
            }
        }
        Collections.sort(names);
        return names;
    }


    public synchronized Collection<ProjectInfo> getAvailableProjectInfo(RemoteSession session) {
    	List<ProjectInfo> projectInfos = new ArrayList<ProjectInfo>();
    	Collection<String> projectNames = getAvailableProjectNames(session);
    	for (String projectName : projectNames) {
    		//should work, but there is a problem with empty descriptions, therefore the try catch
    		try {
    			ProjectInstance prj = metaproject.getProject(projectName);
    			User owner = prj.getOwner();
    			projectInfos.add(new ProjectInfo(prj.getName(), prj.getDescription(), owner == null ? null : owner.getName()));
			} catch (Exception e) {
				Log.getLogger().log(Level.WARNING, "Errors at retrieving project instance from metaproject. Project name" + projectName, e);
			}
		}
    	return projectInfos;
    }

    public synchronized Collection<String> getAllProjectNames() {
       ArrayList<String> prjs = new ArrayList<String>(_nameToProjectStatusMap.keySet());
       Collections.sort(prjs);
       return prjs;
    }

    public synchronized Map<String, ProjectStatus> getProjectsStatusMap() {
    	return _nameToProjectStatusMap;
    }


    public synchronized RemoteServerProject openProject(String projectName, RemoteSession session)
    throws ServerSessionLost {
        if (!_sessions.contains(session)) {
        	Log.getLogger().warning("Failed to open project: Invalid " + session + " tried to open project " +
        			projectName + ". Most likely user is not logged in.");
            return null;  // user didn't really log in
        }
        if (_nameToProjectStatusMap.get(projectName) == ProjectStatus.CLOSED_FOR_MAINTENANCE) {
        	Log.getLogger().warning("Failed to open project: " + session + " tried to open project " +
        			projectName + ", but project is closed for maintenance");
            return null;
        }
        if (!readAllowed(projectName, session)) {
        	Log.getLogger().warning("Failed to open project: " + session + " tried to open project " +
        			projectName + ", but user does not have read permission on this project.");
            return null;
        }
        ServerProject serverProject = null;
        Project p = getOrCreateProject(projectName);
        if (p != null) {
            serverProject = getServerProject(p);
            if (serverProject == null) {
                serverProject = createServerProject(projectName, p);
                addServerProject(p, serverProject);
            }
            if (_sessionToProjectsMap.get(session) != null &&
                    _sessionToProjectsMap.get(session).contains(serverProject)) {
                return null;
            }
            recordConnection(session, serverProject);
            Log.getLogger().info("Server: Opened project " + projectName + " for " + session + " on " + new Date());
        } else {
        	Log.getLogger().warning("Failed to open project:  " + session + " tried to open project " +
        			projectName + ", but operation failed." +
        					"Possible causes: project with this name is not defined in the metaproject;" +
        					"project is not in the ready status, or project could not be opened by the server" +
        					"(check previous server logs)");
        }

        return serverProject;
    }

    public synchronized RemoteServerProject openMetaProject(RemoteSession session) throws RemoteException {
    	if (!isServerOperationAllowed(session, MetaProjectConstants.OPERATION_ADMINISTER_SERVER)) {
    		log.warning("Failed attempt to open metaproject by " + session + ". Not enough privileges.");
    		throw new SecurityException("Not enough privileges to open metaproject.");
    	}
        String metaProjectName = "Meta-Project";
        if (serverMetaProject == null) {
            KnowledgeBase metaProjectKb = ((MetaProjectImpl) metaproject).getKnowledgeBase();
            localizeKB(metaProjectKb);
            serverMetaProject =  new ServerProject(this,
                                                 getURI(metaProjectName), metaproject.getProject(metaProjectName),
                                                 metaProjectKb.getProject());
        }
        serverMetaProject.register(session);
        return serverMetaProject;
    }


    /* -----------------------------------------------------------------
     * Server Admin
     */

    public synchronized RemoteServerProject createProject(String newProjectName,
                                             RemoteSession session,
                                             KnowledgeBaseFactory kbfactory,
                                             boolean saveToMetaProject) throws RemoteException {
        Project project = null;

        for (ProjectInstance instance : metaproject.getProjects()) {
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
            Log.handleErrors(log, Level.SEVERE, errors);
        	return null;
        }

        project.setProjectURI(uri);

        //TODO TT: How to treat other knowledge base factories?
        if (kbfactory instanceof ClipsKnowledgeBaseFactory) {
        	ClipsKnowledgeBaseFactory.setSourceFiles(project.getSources(), newProjectName + ".pont", newProjectName + ".pins");
        }

        project.save(errors);
        if (errors.size() > 0) {
            Log.handleErrors(log, Level.SEVERE, errors);
        	return null;
        }

        project = Project.loadProjectFromURI(uri, new ArrayList(), true);

        if (serverInstance != null) {
        	_projectPluginManager.afterLoad(project);
        }

        localizeProject(project);
        _nameToOpenProjectMap.put(newProjectName, project);

        if (saveToMetaProject) {
        	ProjectInstance newProjectInstance = metaproject.createProject(newProjectName);
        	newProjectInstance.setLocation(newProjectsDir + File.separator + newProjectName + ".pprj");
        	metaproject.save(errors);
            Log.handleErrors(log, Level.SEVERE, errors);

        }

        return getServerProject(project);
	}

    public synchronized void setProjectStatus(String projectName,
    		ProjectStatus status,
    		RemoteSession session) {
    	//TODO: what is the best policy to use?
    	ProjectStatus oldStatus = _nameToProjectStatusMap.put(projectName, status);
    	Project p = _nameToOpenProjectMap.get(projectName);
    	if (p != null) {
    		KnowledgeBase kb = p.getKnowledgeBase();
    		EventGeneratorFrameStore fs = kb.getFrameStoreManager().getFrameStoreFromClass(EventGeneratorFrameStore.class);
    		fs.addCustomEvent(new ServerProjectStatusChangeEvent(projectName, oldStatus, status));
    	}

    }

    //TODO: should this method check the policy?
    public synchronized void notifyProject(String projectName,
    		String message,
    		RemoteSession session) {
    	Project p = _nameToOpenProjectMap.get(projectName);
    	if (p != null) {
    		KnowledgeBase kb = p.getKnowledgeBase();
    		EventGeneratorFrameStore fs = kb.getFrameStoreManager().getFrameStoreFromClass(EventGeneratorFrameStore.class);
    		fs.addCustomEvent(new ServerProjectNotificationEvent(projectName, message));
    	}
    }

	public synchronized boolean createUser(String userName, String password) {
		List<String> names = new ArrayList<String>();
		for (User instance : metaproject.getUsers()) {
			String existingUserName = instance.getName();
			if (existingUserName.equals(userName)) {
				Log.getLogger().warning(
						"Server: Could not create user with name " + userName
								+ ". User name already exists.");
				return false;
			}
		}
		User newUserInstance = metaproject.createUser(userName, password);

		ArrayList errors = new ArrayList();
		boolean success = metaproject.save(errors);
        Log.handleErrors(log, Level.SEVERE, errors);

		return success && errors.size() == 0;
	}

	public synchronized boolean hasValidCredentials(String userName, String password) {
		return isValid(userName, password);
	}


    public synchronized void shutdown() {
        log.info("Received shutdown request.");
        try {
            unBindName();
        }
        catch (RemoteException re) {
            log.log(Level.WARNING, "Exception caught unbinding server name", re);
        }
        removeMetaProjectListeners();
        saveAllProjects();
        Thread thread = new Thread() {
			public void run() {
              try {
                  SystemUtilities.sleepMsec(100);  // what  is this for?
                  Log.getLogger().info("Server exiting.");
                  synchronized (Server.this) {
                      for (Project p : _projectToServerProjectMap.keySet()) {
                          /* Synchronization
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

    public synchronized void shutdown(String projectName, RemoteSession session) {
        if (!isServerOperationAllowed(session, MetaProjectConstants.OPERATION_ADMINISTER_SERVER) &&
        	!isOperationAllowed(session, MetaProjectConstants.OPERATION_STOP_REMOTE_PROJECT, projectName)) {
            log.warning("Unauthorized attempt to shutdown project " + projectName + " by " + session.getUserName() + " @ " + session.getUserIpAddress());
            return;
        }
        setProjectStatus(projectName, ProjectStatus.CLOSED_FOR_MAINTENANCE, session);
        closeProject(projectName);
    }


    public synchronized void killOtherUserSession(RemoteSession sessionToKill, RemoteSession session) {
    	killOtherUserSession(sessionToKill, session, 0);
    }

    public void killOtherUserSession(final RemoteSession sessionToKill, final RemoteSession session, final int finalGracePeriod) {
    	//TODO: check permission for session kill
    	Thread killSessionThread = new Thread("Kill other session thread") {
    		public void run() {
				Collection<ServerProject> projectsWithSessionRemoved = new HashSet<ServerProject>();
				Collection<ServerProject> candidateProjectsToKill = new HashSet<ServerProject>();
				boolean partial = false;
				Collection<ServerProject> projects;

    			synchronized (Server.this) {
    				projects = _sessionToProjectsMap.get(sessionToKill);
    				if (projects == null) {
    					_sessionToProjectsMap.remove(sessionToKill);
    					_sessions.remove(sessionToKill);
    					return;
    				}

    				for (ServerProject serverProject : projects) {
    					String projectName = serverProject.getMetaProjectInstance().getName();
    					if (isServerOperationAllowed(session, MetaProjectConstants.OPERATION_ADMINISTER_SERVER) ||
    							isOperationAllowed(session,	MetaProjectConstants.OPERATION_KILL_OTHER_USER_SESSION,	projectName)) {
    						candidateProjectsToKill.add(serverProject);
    						//send notification that the session will be killed
    						Project p = _nameToOpenProjectMap.get(projectName);
    						if (p != null) {
    							KnowledgeBase kb = p.getKnowledgeBase();
    							EventGeneratorFrameStore fs = kb.getFrameStoreManager().getFrameStoreFromClass(EventGeneratorFrameStore.class);
    							fs.addCustomEvent(new ServerProjectSessionClosedEvent(projectName, sessionToKill));
    						} else {
    							partial = true;
    						}
    					}
    				}
    			}

    			//wait for the grace period to pass - this will allow the clients to get the notification
    			try {
    				sleep(finalGracePeriod * 1000);
    			} catch (InterruptedException e) {
    				log.severe("Unexpected interrupt - you trying to kill me or what?");
    			}

    			synchronized (Server.this) {
    				//kill one by one the projects in the session to kill
    				for (ServerProject serverProject : candidateProjectsToKill) {
    					try {
    						serverProject.deregister(sessionToKill);
    						projectsWithSessionRemoved.add(serverProject);
    					}
    					catch (Throwable  t) {
    						String projectName = "(unknown)";
    						try {
    							projectName = serverProject.getMetaProjectInstance().getName();
    						} catch (Exception e) {
    							Log.emptyCatchBlock(e);
    						}
    						log.log(Level.WARNING, "Could not deregister session " + sessionToKill + " from project " +
    								projectName, t);
    					}
    				} //end for
    				if (partial) {
    					projects.removeAll(projectsWithSessionRemoved);
    				} else {
    					_sessionToProjectsMap.remove(sessionToKill);
    					_sessions.remove(sessionToKill);
    				}
    			}
    		}
    	};

    	killSessionThread.start();
    }


    /**
     * Shuts down the remote project with the name projectName and sends shutdown notifications
	 * at certain time periods: it starts with the warningTimeInSeconds argument and divides it by two.
	 * A {@link SecurityException} is thrown if the user of the session does not
	 * have the right to shutdown the project.
	 * @param session - the session that tries to shutdown the project
	 * @param projectName - the remote project name
     * @param warningTimeInSeconds - the time of the first notification in seconds
     */
    public void shutdownProject(RemoteSession session, String projectName, float warningTimeInSeconds)
    	throws RemoteException {
    	if (session == null) {
    		log.warning("Can only shutdown the remote project " + projectName + " in multi-user mode.");
    		return;
    	}
		int lastWarning = 5;
		int finalGracePeriodInSeconds = 7;

		int t = (int) warningTimeInSeconds;
		ArrayList<Integer> ints = new ArrayList<Integer>();

		while (t > lastWarning) {
			ints.add(Integer.valueOf(t));
			if (t >= 120) {
				t = t/120 * 60;
			} else if (t >= 20) {
				t = t/20 * 10;
			} else if (t >= 10) {
				t = t/10 * 5;
			} else {
				t = lastWarning;
			}
		}
		ints.add(Integer.valueOf(t));
		shutdownProject(session, projectName, ints.toArray(new Integer[ints.size()]), finalGracePeriodInSeconds);
    }


	/**
	 * Shuts down the remote project with the name projectName and sends shutdown notifications
	 * at time periods (in seconds) from the warningTimesInSeconds argument.
	 * A {@link SecurityException} is thrown if the user of the session does not
	 * have the right to shutdown the project.
	 * @param session - the session that tries to shutdown the project
	 * @param projectName - the remote project name
	 * @param warningTimesInSeconds - a list of {@link Integer} with the notify periods of time in seconds
	 * @param finalGracePeriodInSeconds - the time the server will wait until shutting down the project after the last notification message
	 */
	public synchronized void shutdownProject(final RemoteSession session, final String projectName,
								final Integer[] warningTimesInSeconds, final int finalGracePeriodInSeconds)
									throws RemoteException {
    	if (session == null) {
    		log.warning("Can only shutdown the remote project " + projectName + " in multi-user mode.");
    		return;
    	}
    	if (!isServerOperationAllowed(session, MetaProjectConstants.OPERATION_ADMINISTER_SERVER) &&
    		!isOperationAllowed(session, MetaProjectConstants.OPERATION_STOP_REMOTE_PROJECT, projectName)) {
    		throw new SecurityException("Operation not permitted: Shutdown remote project " + projectName +
    				" for user: " + session.getUserName() + " (" + session.getUserIpAddress() + ")");
    	}
	    Arrays.sort(warningTimesInSeconds, new Comparator<Integer>() {
	        public int compare(Integer o1, Integer o2) {
	            return o2.compareTo(o1);
	        }
	    });

		Runnable projShutdownNotificaitonThread = getProjectShutdownRunnable(session, projectName,
				warningTimesInSeconds, finalGracePeriodInSeconds);
		FutureTask<Object> task = new FutureTask<Object>(projShutdownNotificaitonThread, null);
		serverExecutor.submit(task);
		projectToShutdownTaskNotificationMap.put(projectName, task);
	}

	public synchronized boolean cancelShutdownProject(RemoteSession session, String projectName) {
		boolean success = false;
		FutureTask<Object> task = null;
		task = projectToShutdownTaskNotificationMap.get(projectName);
		if (task == null || task.isDone()) { return false; }
		success = task.cancel(true);

		if (!success) {
			log.info("Could not cancel task: " + task);
		} else {
			log.info("Task canceled: " + task);
			projectToShutdownTaskNotificationMap.remove(projectName);
			setProjectStatus(projectName, ProjectStatus.READY);
			notifyProject(projectName, "Shut down of project " + projectName + " has been canceled by the administrator.", session);
		}

		return success;
	}


	private Runnable getProjectShutdownRunnable(final RemoteSession session, final String projectName,
								final Integer[] warningTimesInSeconds, final int finalGracePeriodInSeconds) {
		Runnable projShutdownNotificaitonThread = new Runnable() {
			public void run() {
				setProjectStatus(projectName, ProjectStatus.SHUTTING_DOWN, session);

			    for (int i = 0; i < warningTimesInSeconds.length; i++) {
			        int timeLeft = warningTimesInSeconds[i];
			        int timeToNextWarning = ((i == warningTimesInSeconds.length - 1) ? timeLeft : timeLeft - warningTimesInSeconds[i + 1]);
			        String message = "Remote project " + projectName + " will be shut down in " +
			        	 (timeLeft <= 60 ? ""  + timeLeft + " seconds." :  "" + timeLeft / 60 + " minutes.");

					notifyProject(projectName, message, session);

			        try {
			            Thread.sleep(timeToNextWarning * 1000);
			        }
			        catch (InterruptedException e) {
			        	FutureTask<Object> task;
			        	synchronized (Server.this) {
			        		task = projectToShutdownTaskNotificationMap.get(projectName);
						}
			        	if (task != null && task.isCancelled()) {
				            log.warning("Thread for shutting down project " + projectName + " has been interrupted.");
			        	} else {
			        		log.severe("Thread for shutting down project " + projectName + " has been interrupted for no good reason.");
			        	}
			        	return;
			        }
			    }

				setProjectStatus(projectName, ProjectStatus.CLOSED_FOR_MAINTENANCE, session);

			    if (finalGracePeriodInSeconds != 0) {
			        try {
			            Thread.sleep(finalGracePeriodInSeconds * 1000);
			        } catch (InterruptedException e) {
			        	FutureTask<Object> task;
			        	synchronized (Server.this) {
			        		task = projectToShutdownTaskNotificationMap.get(projectName);
						}
			        	if (task != null && task.isCancelled()) {
				            log.warning("Thread for shutting down project " + projectName + " has been interrupted.");
			        	} else {
			        		log.severe("Thread for shutting down project " + projectName + " has been interrupted for no good reason.");
			        	}
			        	return;
			        }
			    }

			    synchronized (Server.this) {
			    	shutdown(projectName, session);
			    	projectToShutdownTaskNotificationMap.remove(projectName);
				}
			}
		};
		return projShutdownNotificaitonThread;
	}


    /* -----------------------------------------------------------------
     * Policy
     */

    public boolean allowsCreateUsers() throws RemoteException {
    	return ServerProperties.getAllowsCreateUsers();
    }

    public synchronized boolean isOperationAllowed(RemoteSession session, Operation op,  String projectName) {
    	return isServerOperationAllowed(session, op, metaproject.getPolicy().getProjectInstanceByName(projectName));
    }

    public synchronized boolean isServerOperationAllowed(RemoteSession session, Operation op) {
    	ServerInstance firstServerInstance = metaproject.getPolicy().getFirstServerInstance();
		return firstServerInstance == null ? true : isServerOperationAllowed(session, op, firstServerInstance);
    }

    public synchronized boolean isServerOperationAllowed(RemoteSession session, Operation op,  String serverName) {
    	return isServerOperationAllowed(session, op, metaproject.getPolicy().getServerInstanceByName(serverName));
    }

    public synchronized boolean isGroupOperationAllowed(RemoteSession session, Operation op,  String groupName) {
        return isServerOperationAllowed(session, op, metaproject.getGroup(groupName));
    }

    private boolean isServerOperationAllowed(RemoteSession session, Operation op, PolicyControlledObject policyControlledObject) {
    	Policy policy = metaproject.getPolicy();
        User user = policy.getUserByName(session.getUserName());
        if (user == null || policyControlledObject == null) {
            return false;
        }
        return policy.isOperationAuthorized(user, op, policyControlledObject);
    }

    public synchronized Collection<Operation> getAllowedOperations(RemoteSession session, String projectName, String userName) {
    	Collection<Operation> allowedOps = new ArrayList<Operation>();
    	Policy policy = metaproject.getPolicy();
    	User user = policy.getUserByName(userName);
    	ProjectInstance project = metaproject.getPolicy().getProjectInstanceByName(projectName);
    	if (user == null || project == null) {  return allowedOps;  }
    	for (Operation op : policy.getKnownOperations()) {
			if (policy.isOperationAuthorized(user, op, project)) {
				allowedOps.add(op);
			}
		}
    	return allowedOps;
    }

     /* -----------------------------------------------------------------
      * Misc
      */
    public Object executeServerJob(ServerJob job, RemoteSession session) {
        job.fixLoader();
        return job.run();
    }



}