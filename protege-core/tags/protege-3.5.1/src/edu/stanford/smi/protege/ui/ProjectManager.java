package edu.stanford.smi.protege.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Window;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import edu.stanford.smi.protege.Application;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory2;
import edu.stanford.smi.protege.model.KnowledgeBaseSourcesEditor;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.CreateProjectWizard;
import edu.stanford.smi.protege.plugin.ExportPlugin;
import edu.stanford.smi.protege.plugin.ExportWizard;
import edu.stanford.smi.protege.plugin.ImportPlugin;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.plugin.ProjectPluginManager;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.server.util.RemoteProjectUtil;
import edu.stanford.smi.protege.storage.clips.ParseErrorPanel;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ArchiveManager;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MessageError;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.ModalDialogCloseDoubleClickAdapter;
import edu.stanford.smi.protege.util.ProjectChooser;
import edu.stanford.smi.protege.util.PropertyList;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protege.util.WaitCursor;
import edu.stanford.smi.protege.util.Wizard;
import edu.stanford.smi.protege.widget.TextComponentWidget;

/**
 * Manager for the open project. The original model was that more than one project could be open at a time. This is not
 * however the case now so this object just manages a single Project. It has a handle to the view that is displaying
 * this project.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProjectManager {
    private static ProjectManager _projectManager = new ProjectManager();
    private ProjectPluginManager _projectPluginManager = new ProjectPluginManager();
    private Project _currentProject;
    private JRootPane _rootPane;
    private ProjectMenuBar _menuBar;
    private ProjectToolBar _mainToolBar;
    private ProjectView _projectView;
    private ViewSelector _viewSelector;
    private JPanel _headerPanel;
    private boolean _doExitVM = true;
    private JFrame _errorFrame;
    private JComponent _toolBarHolder;

    private static class FactoryPanel extends JPanel {
        private static final long serialVersionUID = 4971171312817011752L;
        private JList _list;

        FactoryPanel() {
            _list = ComponentFactory.createList(null);
            _list.addMouseListener(new ModalDialogCloseDoubleClickAdapter());
            _list.setCellRenderer(new FactoryRenderer());
            ComponentUtilities.setListValues(_list, PluginUtilities.getAvailableFactories());
            _list.setSelectedIndex(0);
            setLayout(new BorderLayout());
            add(ComponentFactory.createScrollPane(_list));
            setPreferredSize(new Dimension(300, 100));
        }

        public KnowledgeBaseFactory getSelectedFactory() {
            return (KnowledgeBaseFactory) _list.getSelectedValue();
        }
    }

    private ProjectManager() {
    }

    private static void advance(Point p) {
        p.x += 25;
        p.y += 25;
    }

    public void buildProjectRequest() {
        if (closeProjectRequest()) {
            KnowledgeBaseFactory factory = promptForFactory();
            if (factory != null) {
                importSources(factory);
            }
        }
    }

    public boolean buildProject(KnowledgeBaseFactory factory) {
        boolean succeeded = true;
        if (factory != null) {
            succeeded = importSources(factory);
        }
        return succeeded;
    }

    public void importProjectRequest(ImportPlugin plugin) {
        if (closeProjectRequest()) {
            _currentProject = plugin.handleImportRequest();
            if (_currentProject != null) {
                displayCurrentProject();
            }
        }
    }

    public void exportProjectRequest(ExportPlugin plugin) {
        plugin.handleExportRequest(_currentProject);
    }

    private boolean importSources(KnowledgeBaseFactory factory) {
        boolean succeeded = false;
        Collection errors = new ArrayList();

        if (factory != null) {
            Project p = Project.createBuildProject(factory, errors);
            succeeded = loadNewSources(p, factory, false);
            if (succeeded) {
                WaitCursor waitCursor = new WaitCursor(_rootPane);
                try {
                    p.createDomainKnowledgeBase(factory, errors, true);
                    _currentProject = p;
                    _projectPluginManager.afterLoad(p);
                    displayCurrentProject();
                } finally {
                    waitCursor.hide();
                }
                displayErrors("Build Project Errors", errors);
            }
        }

        return succeeded;
    }

    //ESCA-JAVA0130
    public void cascadeWindows(Point p, Collection w) {
        ArrayList windows = new ArrayList(w);
        Collections.sort(windows, new WindowComparator());
        Iterator i = windows.iterator();
        while (i.hasNext()) {
            Window window = (Window) i.next();
            window.setLocation(p);
            window.toFront();
            window.requestFocus();
            advance(p);
        }
    }

    public void cascadeWindowsRequest() {
        Project project = getCurrentProject();
        if (project != null) {
            Point startPoint = SwingUtilities.windowForComponent(_rootPane).getLocation();
            startPoint.x += 25;
            startPoint.y += 110;
            cascadeWindows(startPoint, project.getOpenWindows());
        }
    }

    public void changeProjectStorageFormatRequest() {
        if (hasLoadedProject()) {
            KnowledgeBaseFactory oldFactory = _currentProject.getKnowledgeBase().getKnowledgeBaseFactory();
            KnowledgeBaseFactory factory = promptForFactory();
            boolean succeeded = factory != null && factory != oldFactory;
            if (succeeded) {
                succeeded = loadNewSources(_currentProject, factory, true);
            }
            if (succeeded) {
                if (_currentProject.hasCompleteSources()) {
                    if (succeeded) {
                        succeeded = prepareToSave(oldFactory, factory);
                    }
                    boolean oldIsReadonly = _currentProject.isReadonly();
                    if (succeeded) {
                        _currentProject.setIsReadonly(false);
                        succeeded = save();
                    }
                    if (succeeded) {
                        URI uri = _currentProject.getProjectURI();
                        closeCurrentProject();
                        loadProject(uri);
                    } else {
                        _currentProject.setIsReadonly(oldIsReadonly);
                    }
                } else {
                    Log.getLogger().warning("Sources are not complete");
                }
            }
        }
    }

    public boolean closeProjectRequest() {
        boolean succeeded = true;
        if (hasLoadedProject()) {
            commitChanges();
            if (_currentProject.isDirty()) {
                succeeded = confirmSave();
            }
            if (succeeded) {
                succeeded = closeCurrentProject();
            }
        }
        return succeeded;
    }

    public boolean closeCurrentProject() {
    	ProjectView prjView = getCurrentProjectView();

    	//this should not be the case
    	if (prjView == null) {
			return true;
		}

        boolean succeeded = prjView.canClose();
        if (succeeded) {
            _projectPluginManager.beforeHide(_projectView, _mainToolBar, _menuBar);
            _projectView.setVisible(false);
            _projectView.close();
            ComponentUtilities.closeAllWindows();
            _rootPane.getContentPane().remove(_projectView);
            ComponentUtilities.dispose(_projectView);
            _projectView = null;

            _viewSelector = null;
            _projectPluginManager.beforeClose(_currentProject);
            _currentProject.dispose();
            _currentProject = null;
            updateFrameTitle();
            createMenuAndToolBar();
            _rootPane.revalidate();
            _rootPane.repaint();
        }
        return succeeded;
    }

    public void configureProjectRequest() {
        Project p = getCurrentProject();
        boolean displayHidden = p.getDisplayHiddenClasses();
        boolean displayTabbedInstanceForm = p.getTabbedInstanceFormLayout();
        boolean addNameOnInstanceForm = p.getAddNameOnInstanceForm();
        boolean supressInstancesCountDisplay = p.getSuppressInstanceCounting();
        if (p != null) {
           ConfigureProjectPanel panel = new ConfigureProjectPanel(p);
           String title = "Configure " + p.getProjectURI();
           int result = ModalDialog.showDialog(_rootPane, panel, title, ModalDialog.MODE_OK_CANCEL);
           if (result == ModalDialog.OPTION_OK) {
                boolean needToRegenerate = displayHidden != p.getDisplayHiddenClasses() ||
                    displayTabbedInstanceForm != p.getTabbedInstanceFormLayout() ||
                    addNameOnInstanceForm != p.getAddNameOnInstanceForm() ||
                    supressInstancesCountDisplay != p.getSuppressInstanceCounting();
                reloadUI(needToRegenerate);
            }
        }
    }

    private boolean confirmSave() {
        boolean succeeded;
        JComponent c = ComponentFactory.createLabel("Do you want to save changes to the current project?");
        int result = ModalDialog.showDialog(_rootPane, c, "Save?", ModalDialog.MODE_YES_NO_CANCEL);
        switch (result) {
            case ModalDialog.OPTION_YES:
                succeeded = saveProjectRequest();
                break;
            case ModalDialog.OPTION_NO:
                succeeded = true;
                break;
            case ModalDialog.OPTION_CANCEL:
            case ModalDialog.OPTION_CLOSE:
                succeeded = false;
                break;
            default:
                Assert.fail("bad result: " + result);
                succeeded = false;
                break;
        }
        return succeeded;
    }

    private void displayCurrentProject() {
        displayCurrentProject(false);
    }

    private void displayCurrentProject(boolean remote) {
        // Log.enter(this, "displayCurrentProject");
        WaitCursor waitCursor = new WaitCursor(_rootPane);
        try {
            createMenuAndToolBar();
            _projectView = new ProjectView(_currentProject);
            if (remote) {
                RemoteProjectUtil.configure(_projectView);
            }
            addViewSelector(_projectView);
            _rootPane.getContentPane().add(_projectView, BorderLayout.CENTER);
            _projectView.revalidate();
            _projectView.repaint();
            _projectPluginManager.afterShow(_projectView, _mainToolBar, _menuBar);
            _rootPane.revalidate();
        } finally {
            waitCursor.hide();
        }
        updateFrameTitle();
        // ComponentUtilities.pack(itsRootPane);
        // Log.exit(this, "displayCurrentProject");
    }

    public JMenuBar getCurrentProjectMenuBar() {
        return _menuBar;
    }

    /**
     * @deprecated Use #getCurrentProjectSystemToolBar()
     */
    @Deprecated
	public JToolBar getCurrentProjectToolBar() {
        return getCurrentProjectMainToolBar();
    }

    public JToolBar getCurrentProjectMainToolBar() {
        return _mainToolBar;
    }

    public void displayErrors(String label, Collection errors) {
        if (!errors.isEmpty() && ProjectManager.getProjectManager().getMainPanel() != null) {
            JComponent panel = new ParseErrorPanel(errors);
            _errorFrame = ComponentFactory.showInFrame(panel, label);
            bringErrorFrameToFront();
        } else {
            Log.handleErrors(Log.getLogger(), Level.WARNING, errors);
        }
    }

    public void bringErrorFrameToFront() {
        if (_errorFrame != null && _errorFrame.isVisible()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    _errorFrame.toFront();
                }
            });
        }
    }

    public void exitApplicationRequest() {
        boolean succeeded = closeProjectRequest();
        if (succeeded) {
        	try {
        		java.awt.Frame mainFrame = ComponentUtilities.getFrame(_rootPane);
        		ApplicationProperties.recordMainFrameProperties(mainFrame);
        		ApplicationProperties.flush();
        		ComponentUtilities.dispose(mainFrame);
        	} catch (Throwable e) {
        		Log.getLogger().warning("Errors at saving protege.properties");
        	}
            if (_doExitVM) {
                SystemUtilities.exit();
            }
        }
    }

    public Project getCurrentProject() {
        return _currentProject;
    }

    public ProjectView getCurrentProjectView() {
        return _projectView;
    }

    public JComponent getMainPanel() {
        return _rootPane;
    }

    public static ProjectManager getProjectManager() {
        return _projectManager;
    }

    /*
     private URI getRequestedProject() {
     URI projectURI = null;
     JFileChooser chooser = ComponentFactory.createFileChooser("Project", ".pprj");
     int rval = chooser.showOpenDialog(_rootPane);
     if (rval == JFileChooser.APPROVE_OPTION) {
     File file = chooser.getSelectedFile();
     if (file.exists()) {
     projectURI = file.toURI();
     }
     }
     return projectURI;
     }
     */

    private static Project getRequestedProject(Component parent) {
        Project project = null;
        ProjectChooser chooser = new ProjectChooser();
        int rval = chooser.showOpenDialog(parent);
        if (rval == JFileChooser.APPROVE_OPTION) {
            project = chooser.getProject();
        }
        return project;
    }

    //TODO: check if condition is correct
    private boolean hasLoadedProject() {
        return _currentProject != null && _currentProject.getProjectInstance() != null && _currentProject.getKnowledgeBase() != null;
    }

    public void changeIncludedProjectURIsRequest(Collection includedProjectURIs) {
        if (canChangeIncludedProjectURIs(includedProjectURIs)) {
            _currentProject.setDirectIncludedProjectURIs(includedProjectURIs);
            boolean succeeded = saveProjectRequest();
            if (succeeded) {
                reload();
            }
        }
    }

    public void setActiveProjectURI(URI projectURI) {
        _currentProject.setActiveRootURI(projectURI);
        reloadUI(true);
    }

    private void reload() {
        URI uri = getCurrentProject().getProjectURI();
        WaitCursor waitCursor = new WaitCursor(_rootPane);
        try {
            closeProjectRequest();
            loadProject(uri);
        } finally {
            waitCursor.hide();
        }
    }

    private boolean canChangeIncludedProjectURIs(Collection newIncludedProjectURIs) {
        boolean canChange;
        Project p = getCurrentProject();
        boolean needsProjectName = p.getProjectURI() == null;
        Set currentIncludedProjectURISet = new HashSet(p.getDirectIncludedProjectURIs());
        Set newIncludedProjectURISet = new HashSet(newIncludedProjectURIs);
        if (CollectionUtilities.equalsSet(currentIncludedProjectURISet, newIncludedProjectURISet)) {
            canChange = false;
        } else {
            String text = "Changing the included projects will cause the current project to\n"
                    + "be saved and reloaded. ";
            if (needsProjectName) {
                text += "You will need to first specify a name for your project.";
            }
            int rval = ModalDialog.showMessageDialog(_rootPane, text, ModalDialog.MODE_OK_CANCEL);
            canChange = rval == ModalDialog.OPTION_OK;
        }
        return canChange;
    }

    // return true if sources are complete and 'ok' was pressed
    private boolean loadNewSources(Project project, KnowledgeBaseFactory factory, boolean showProject) {
        if (factory == null) {
            factory = project.getKnowledgeBaseFactory();
        }
        PropertyList sources = project.getSources();
        URI projectURI = project.getProjectURI();
        String s = projectURI == null ? null : projectURI.toString();
        KnowledgeBaseSourcesEditor editor = factory.createKnowledgeBaseSourcesEditor(s, sources);
        editor.setShowProject(showProject);
        String title = factory.getDescription();
        int result = ModalDialog.showDialog(_rootPane, editor, title, ModalDialog.MODE_OK_CANCEL);
        if (result == ModalDialog.OPTION_OK) {
            sources.setString(KnowledgeBaseFactory.FACTORY_CLASS_NAME, factory.getClass().getName());
            project.setProjectURI(URIUtilities.createURI(editor.getProjectPath()));
            Iterator i = editor.getIncludedProjects().iterator();
            while (i.hasNext()) {
                URI uri = (URI) i.next();
                project.includeProject(uri, false, null);
            }
        }
        return result == ModalDialog.OPTION_OK;
    }

    public void loadProject(URI uri) {
        long t1 = System.currentTimeMillis();
        Collection errors = new ArrayList();
        WaitCursor waitCursor = new WaitCursor(_rootPane);
        try {
            if (uri == null) {
                KnowledgeBaseFactory factory = promptForFactory();
                t1 = System.currentTimeMillis(); // reinitialize after prompt to
                // user!
                if (factory != null) {
                    _currentProject = createNewProject(factory, errors);
                }
            } else {
                _currentProject = loadProjectFromURI(uri, errors);
            }
        } finally {
            waitCursor.hide();
        }
        long t2 = System.currentTimeMillis();

        //TODO: reimplement this when exception handling is improved. Handle here invalid project files
        if (_currentProject != null && _currentProject.getProjectInstance() == null) {
        	String errorMsg = "Unable to load file: " + uri
        			+ "\nPossible reasons:\n- The file has an unsupported file format\n- The file is not well-formed\n- The project file is corrupt";
        	Log.getLogger().severe(errorMsg);
        	errors.add(new MessageError(null, errorMsg));
        	//JOptionPane.showMessageDialog(getMainPanel(), errorMsg, "Invalid file", JOptionPane.WARNING_MESSAGE);
        }

        displayErrors("Load Project Errors", errors);

        if (_currentProject != null && _currentProject.getProjectInstance() != null  && _currentProject.getKnowledgeBase() != null) {
            displayCurrentProject();
            printLoadTimes(t1, t2);
        }

    }

    private Project createNewProject(KnowledgeBaseFactory factory, Collection errors) {
        Project project = Project.createNewProject(factory, errors);
        _projectPluginManager.afterCreate(project);
        return project;
    }

    public Project loadProjectFromURI(URI uri, Collection errors) {
        Project project = null;
        try {
            project = Project.loadProjectFromURI(uri, errors);
            _projectPluginManager.afterLoad(project);
        } catch (Exception e) {
        	errors.add(new MessageError(e));
            Log.getLogger().log(Level.FINE, "Error loading project", e);
        }
        return project;
    }

    public void loadProject(URI uri, KnowledgeBaseFactory factory) {
        Collection errors = new ArrayList();
        long t0 = System.currentTimeMillis();
        long t1 = 0;

        if (factory != null) {
            if (uri == null) {
                _currentProject = createNewProject(factory, errors);
            } else {
                _currentProject = loadProjectFromURI(uri, errors);
            }
            t1 = System.currentTimeMillis();
        }

        if (_currentProject != null) {
            displayErrors("Load Project Errors", errors);
            displayCurrentProject();
            printLoadTimes(t0, t1);
        }
    }

    public void mergeIncludedProjectsRequest() {
        if (hasLoadedProject() && _currentProject.hasIncludedProjects()) {
            String text = "This action will make all included frames in the knowledge base direct members of the current project.";
            String title = " " + Text.getProgramName() + ": " + LocalizedText.getText(ResourceKey.PROJECT_MERGE_INCLUDED);
            JComponent parent = getProjectManager().getMainPanel();
            int rval = ModalDialog.showMessageDialog(parent, text, title, ModalDialog.MODE_OK_CANCEL);
            if (rval == ModalDialog.OPTION_OK) {
                _currentProject.mergeIncludedProjects();
            }
        }
    }

    public boolean createNewProjectRequest() {
        boolean succeeded = false;
        if (closeProjectRequest()) {
            CreateProjectWizard wizard = new CreateProjectWizard(getMainPanel());
            int result = wizard.execute();
            if (result == Wizard.RESULT_FINISH) {
                _currentProject = wizard.getProject();
                _projectPluginManager.afterCreate(_currentProject);
                getProjectManager().displayCurrentProject();
                succeeded = true;
            }
        }
        return succeeded;
    }

    public void saveToFormatRequest() {
        if (hasLoadedProject()) {
            ExportWizard wizard = new ExportWizard(getMainPanel(), _currentProject);
            int result = wizard.execute();
            if (result == Wizard.RESULT_FINISH) {
                Project newProject = wizard.getNewProject();
                if (newProject != null) {
                    closeCurrentProject();
                    _currentProject = newProject;
                    _projectPluginManager.afterCreate(newProject);
                    getProjectManager().displayCurrentProject();
                }
            }
        }
    }

    public void newProjectRequest() {
        if (closeProjectRequest()) {
            loadProject(null);
        }
    }

    public void openProjectRequest() {
        openProjectRequest(_rootPane);
    }

    public boolean openProjectRequest(Component parent) {
        if (closeProjectRequest()) {
            _currentProject = getRequestedProject(parent);
            //check condition
            if (_currentProject != null && _currentProject.getProjectInstance() != null && _currentProject.getKnowledgeBase() != null) {
                ApplicationProperties.addProjectToMRUList(_currentProject.getProjectURI());
                long t1 = System.currentTimeMillis();
                _projectPluginManager.afterLoad(_currentProject);
                displayCurrentProject();
                printDisplayTime(t1);
            }
            bringErrorFrameToFront();
        }
        return _currentProject != null;
    }

    public void setCurrentProject(Project project) {
        setCurrentProject(project, true);
    }

    public void setCurrentProject(Project project, boolean remote) {
    	setCurrentProject(project, remote, false);
    }

    public void  setCurrentProject(Project project, boolean remote, boolean suppressDisplay) {
        if (closeProjectRequest()) {
            _currentProject = project;
            if ( _currentProject != null ) {
                _projectPluginManager .afterLoad( _currentProject);
                if (!suppressDisplay) {
					displayCurrentProject(remote);
				}
            }
        }
    }

    private static void printDisplayTime(final long t1) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                long t2 = System.currentTimeMillis();
                Log.getLogger().info("UI display time = " + (t2 - t1) / 1000 + " sec");
            }
        });
    }

    private static Project getRequestedRemoteProject() {
        return RemoteProjectManager.getInstance().getRemoteProject();
    }

    public void openRemoteProjectRequest() {
        if (closeProjectRequest()) {
            long t1 = System.currentTimeMillis();
            _currentProject = getRequestedRemoteProject();
            long t2 = System.currentTimeMillis();
            if (_currentProject != null) {
                _projectPluginManager.afterLoad(_currentProject);
                displayCurrentProject(true);
                printLoadTimes(t1, t2);
            }
        }
    }

    public Project openRemoteProjectRequest (String serverName, String username, String password, String projectName) {
    	if (closeProjectRequest()) {
    		long t1 = System.currentTimeMillis();
    		_currentProject = RemoteProjectManager.getInstance().getProject(serverName, username, password, projectName, true);
    		long t2 = System.currentTimeMillis();
    		if (_currentProject != null) {
    			_projectPluginManager.afterLoad(_currentProject);
    			displayCurrentProject(true);
    			printLoadTimes(t1, t2);
    		} else {
    		}
    	}
    	return _currentProject;
    }

    private void printLoadTimes(final long start, final long stopProject) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                long stopUI = System.currentTimeMillis();
                StringBuffer buffer = new StringBuffer();
                buffer.append("Load time for ");
                buffer.append(URIUtilities.getDisplayText(_currentProject.getProjectURI()));
                buffer.append(" = ");
                buffer.append((stopProject - start) / 1000);
                buffer.append(" sec (project) + ");
                buffer.append((stopUI - stopProject) / 1000);
                buffer.append(" sec (ui)");
                Log.getLogger().info(buffer.toString());
            }
        });
    }

    private KnowledgeBaseFactory promptForFactory() {
        FactoryPanel panel = new FactoryPanel();
        int rval = ModalDialog.showDialog(_rootPane, panel, "Select Format", ModalDialog.MODE_OK_CANCEL);
        return rval == ModalDialog.OPTION_OK ? panel.getSelectedFactory() : null;
    }

    public void reloadUI(boolean regenerate) {
        ProjectView view = getCurrentProjectView();
        if (view != null) {
            view.reload(regenerate);
        }
        if (_viewSelector != null) {
            _viewSelector.reload();
        }
        _menuBar.updateUI();
        Application.repaint();
    }

    private static void commitChanges() {
        Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        TextComponentWidget widget = (TextComponentWidget) SwingUtilities.getAncestorOfClass(TextComponentWidget.class,
                c);
        if (widget != null) {
            widget.commitChanges();
        }
    }

    private boolean save() {
        Collection errors = new ArrayList();
        commitChanges();
        boolean save = getCurrentProjectView().attemptSave();
        if (save) {
            _projectPluginManager.beforeSave(_currentProject);
            WaitCursor waitCursor = new WaitCursor(_rootPane);
            try {
                _currentProject.save(errors);
            } catch (Exception e) {
                errors.add(new MessageError(e));
                Log.getLogger().log(Level.WARNING, "Errors at save", e);
            } finally {
                _projectPluginManager.afterSave(_currentProject);
                waitCursor.hide();
            }
            displayErrors("Save Project Errors", errors);
        }
        return save && errors.isEmpty();
    }

    public boolean saveProjectAsRequest() {
        return saveProjectAsRequest(null);
    }

    public boolean saveProjectAsRequest(KnowledgeBaseFactory factory) {
        boolean succeeded = true;
        if (hasLoadedProject()) {
            succeeded = loadNewSources(_currentProject, factory, true);
            if (succeeded) {
                if (_currentProject.hasCompleteSources()) {
                    succeeded = save();
                    if (succeeded) {
                        updateFrameTitle();
                        ApplicationProperties.addProjectToMRUList(_currentProject.getProjectURI());
                    }
                } else {
                    Log.getLogger().warning("Sources are not complete");
                }
            }
        }
        return succeeded;
    }

    private boolean prepareToSave(KnowledgeBaseFactory oldFactory, KnowledgeBaseFactory newFactory) {
        // Log.enter(this, "prepareToSave", oldFactory, newFactory);
        boolean succeeded = true;
        if (newFactory != null && newFactory != oldFactory && oldFactory instanceof KnowledgeBaseFactory2) {
            KnowledgeBaseFactory2 oldFactory2 = (KnowledgeBaseFactory2) oldFactory;
            Collection errors = new ArrayList();
            KnowledgeBase kb = _currentProject.getKnowledgeBase();
            oldFactory2.prepareToSaveInFormat(kb, newFactory, errors);
            succeeded = errors.isEmpty();
        }
        return succeeded;
    }

    public boolean saveProjectRequest() {
        boolean succeeded = true;
        if (hasLoadedProject()) {
            if (_currentProject.hasCompleteSources()) {
                succeeded = save();
            } else {
                succeeded = saveProjectAsRequest(_currentProject.getKnowledgeBaseFactory());
            }
        }
        return succeeded;
    }

    public void setLookAndFeel(String lookAndFeelName) {
        try {
            SystemUtilities.setLookAndFeel(lookAndFeelName);
            ApplicationProperties.setLookAndFeel(lookAndFeelName);
            updateUI();
        } catch (Exception e) {
            Log.getLogger().warning(e.toString());
        }
    }

    public void setRootPane(JRootPane rootPane) {
        _rootPane = rootPane;
        setupRootPane();
    }

    private void setupRootPane() {
        createMenuAndToolBar();
        updateFrameTitle();
    }

    private void createMenuAndToolBar() {
        Container contentPane = _rootPane.getContentPane();
        contentPane.setLayout(new BorderLayout());
        if (_headerPanel != null) {
            contentPane.remove(_headerPanel);
        }
        _headerPanel = new JPanel(new BorderLayout());
        _headerPanel.setBackground(Color.WHITE);
        _mainToolBar = new ProjectToolBar();

        JComponent panel = Box.createHorizontalBox();
        panel.setOpaque(false);
        panel.add(_mainToolBar);
        _headerPanel.add(panel, BorderLayout.WEST);

        _menuBar = new ProjectMenuBar();
        JComponent panel2 = Box.createHorizontalBox();
        panel2.setOpaque(false);

        JLabel icon = ComponentFactory.createLabel(Icons.getLogo());
        icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        panel2.add(icon);
        _headerPanel.add(panel2, BorderLayout.EAST);

        _toolBarHolder = Box.createHorizontalBox();
        _toolBarHolder.setOpaque(false);
        _headerPanel.add(_toolBarHolder, BorderLayout.SOUTH);

        _rootPane.setJMenuBar(_menuBar);
        contentPane.add(_headerPanel, BorderLayout.NORTH);
    }

    private void addViewSelector(ProjectView view) {
        if (SystemUtilities.showAlphaFeatures()) {
            _viewSelector = new ViewSelector(view);
            _headerPanel.add(_viewSelector, BorderLayout.CENTER);
        }
    }

    public void addUserToolBar(JToolBar toolBar) {
        _toolBarHolder.add(toolBar);
        _toolBarHolder.revalidate();
    }

    public void removeUserToolBar(JToolBar toolBar) {
        _toolBarHolder.remove(toolBar);
        _toolBarHolder.revalidate();
    }

    public JToolBar getUserToolBar(String name) {
        JToolBar userToolBar = null;
        Component[] components = _toolBarHolder.getComponents();
        for (int i = 0; i < components.length; ++i) {
            Component c = components[i];
            if (c.getName().equals(name)) {
                userToolBar = (JToolBar) c;
                break;
            }
        }
        return userToolBar;
    }

    @Override
	public String toString() {
        return "ProjectManager";
    }

    private void updateFrameTitle() {
        String text;
        String programName = Text.getProgramNameAndVersion();
        if (_currentProject == null) {
            text = programName;
        } else {
            URI uri = _currentProject.getProjectURI();
            if (uri == null) {
                text = "<new>  " + programName;
            } else {
                String shortname = URIUtilities.getBaseName(uri);
                String longname = URIUtilities.getDisplayText(uri);
                KnowledgeBaseFactory factory = _currentProject.getKnowledgeBaseFactory();
                String backend = "";
                if (factory != null) {
                    backend = ", " + factory.getDescription();
                }
                text = shortname + "  " + programName + "    (" + longname + backend +
                		(_currentProject.isReadonly() ? " - Read-Only)" : ")");
            }
        }
        ComponentUtilities.setFrameTitle(_rootPane, text);
    }

    //ESCA-JAVA0130
    public void updateLookAndFeel(Collection windows) {
        Iterator i = windows.iterator();
        while (i.hasNext()) {
            Window window = (Window) i.next();
            SwingUtilities.updateComponentTreeUI(window);
        }
    }

    public void updateUI() {
        Window window = SwingUtilities.getWindowAncestor(_rootPane);
        SwingUtilities.updateComponentTreeUI(window);
        reloadUI(true);
    }

    public void setExitVMOnApplicationExit(boolean exit) {
        _doExitVM = exit;
    }

    public void requestRevertProject() {
        if (_currentProject != null) {
            RevertProjectPanel panel = new RevertProjectPanel(_currentProject);
            int rval = ModalDialog.showDialog(getMainPanel(), panel, "Revert to Archived Version",
                    ModalDialog.MODE_OK_CANCEL);
            Date timestamp = panel.getSelectedTimestamp();
            if (rval == ModalDialog.OPTION_OK && timestamp != null) {
                ArchiveManager manager = ArchiveManager.getArchiveManager();
                boolean archive = panel.getArchiveCurrentVersion();
                if (archive) {
                    manager.archive(_currentProject, "Automatic archive before revert");
                }
                Project newProject = manager.revertToVersion(_currentProject, timestamp);
                if (newProject != null) {
                    closeCurrentProject();
                    _currentProject = newProject;
                    displayCurrentProject();
                } else {
                	ModalDialog.showMessageDialog(getMainPanel(), "Could not revert to archived project version.\nSee console for details.", "Revert to version", ModalDialog.MODE_CLOSE);
                }
            }
        }
    }

    public void archiveProjectRequest() {
        if (_currentProject != null) {
            if (ArchivePanel.displayPanel()) {
                ArchivePanel panel = new ArchivePanel();
                int rval = ModalDialog.showDialog(_rootPane, panel, "Archive Project", ModalDialog.MODE_OK_CANCEL);
                if (rval == ModalDialog.OPTION_OK) {
                    String comment = panel.getComment();
                    archive(comment);
                }
            } else {
                archive(null);
            }
        }
    }

    private void archive(String comment) {
        ArchiveManager manager = ArchiveManager.getArchiveManager();
        manager.archive(_currentProject, comment);
    }
}
