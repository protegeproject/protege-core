package edu.stanford.smi.protege.server;

//ESCA*JAVA0130

import java.rmi.Naming;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.server.admin.ServerAdminPanel;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SystemUtilities;

public class RemoteProjectManager {
    private static RemoteProjectManager _theInstance;

    public static RemoteProjectManager getInstance() {
        if (_theInstance == null) {
            _theInstance = new RemoteProjectManager();
        }
        return _theInstance;
    }

    public Project getRemoteProject() {
        JComponent mainPanel = ProjectManager.getProjectManager().getMainPanel();
        Project project = null;
        RemoteServer server = null;
        RemoteSession session = null;
        ServerPanel panel = new ServerPanel();
        String text = LocalizedText.getText(ResourceKey.REMOTE_HOST_CONNECT_DIALOG_TITLE);
        int rval = ModalDialog.showDialog(mainPanel, panel, text, ModalDialog.MODE_OK_CANCEL);
        if (rval == ModalDialog.OPTION_OK) {
            server = panel.getServer();
            session = panel.getSession();
            if (server != null && session != null) {
            	if (panel.isAdminsterServerActivated()) {
            		showServerAdminWindow(server, session);
            	} else {  //show projects only if administer server is not activated
            		project = getServerProject(mainPanel, server, session);
            	}
            }
        }
        return project;
    }

    public Project getServerProject(JComponent parent, RemoteServer server, RemoteSession session) {
        Project project = null;
        ServerProjectPanel panel = new ServerProjectPanel(server, session);
        String title = LocalizedText.getText(ResourceKey.REMOTE_PROJECT_SELECT_DIALOG_TITLE);
        int rval = ModalDialog.showDialog(parent, panel, title, ModalDialog.MODE_OK_CANCEL);
        if (rval == ModalDialog.OPTION_OK) {
            String projectName = panel.getProjectName();
            if (projectName != null) {
                project = connectToProject(server, session, projectName);
            }
        }
        return project;
    }

    public Project getProject(String serverName,
                              String username,
                              String password,
                              String projectName,
                              boolean pollForEvents) {
        Project p = null;
        try {
            RemoteServer server = (RemoteServer) Naming.lookup("//" + serverName + "/" + Server.getBoundName());
            if (server != null) {
                RemoteSession session = server.openSession(username, SystemUtilities.getMachineIpAddress(), password);
                if (session != null) {
                    RemoteServerProject serverProject = server.openProject(projectName, session);
                    if (serverProject != null) {
                        p = RemoteClientProject.createProject(server, serverProject, session, pollForEvents);
                    }
                }
            }
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return p;
    }

    public Project connectToProject(RemoteServer server, RemoteSession session, String name) {
        Project p = null;
        try {
            RemoteServerProject serverProject = server.openProject(name, session);
            if (serverProject == null) {
            	Log.getLogger().warning("Could not open project " + name + " on server.");
            	return null;
            }
            p = RemoteClientProject.createProject(server, serverProject, session, true);
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Could not connect to remote project " + name, e);
        }
        return p;
    }

    public MetaProject connectToMetaProject(RemoteServer server, RemoteSession session) {
        Project p = null;
        try {
            RemoteServerProject serverProject = server.openMetaProject(session);
            if (serverProject == null) {
                Log.getLogger().warning("Could not open meta project on server.");
                return null;
            }
            p = RemoteClientProject.createProject(server, serverProject, session, true);
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Could not connect to remote meta project ", e);
        }
        return p == null ? null : new MetaProjectImpl(p);
    }

    public void showServerAdminWindow(RemoteServer server, RemoteSession session) {
    	ProjectManager.getProjectManager().setExitVMOnApplicationExit(false);
    	ProjectManager.getProjectManager().exitApplicationRequest();
    	JFrame frame = ComponentFactory.showInFrame(new ServerAdminPanel(server, session),
    			"Administer Protege Server (Logged in as " + session.getUserName() + ")");
    	frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    	frame.requestFocus();
    	ProjectManager.getProjectManager().setExitVMOnApplicationExit(true);
    }
}