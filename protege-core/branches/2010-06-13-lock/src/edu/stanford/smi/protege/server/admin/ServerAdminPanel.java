package edu.stanford.smi.protege.server.admin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.Log;

public class ServerAdminPanel extends JPanel implements Disposable {

	private static final long serialVersionUID = -8915505640982212662L;

	private RemoteServer server;
	private RemoteSession session;
	private JTabbedPane tabbedPane;
	private Thread shutdownHook;

	public ServerAdminPanel(RemoteServer server, RemoteSession session) {
		this.server = server;
		this.session = session;
		initializeUI();
		installShutdownHook();
	}

	public void initializeUI() {
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(800, 600));
		tabbedPane = ComponentFactory.createTabbedPane(true);
		if (server != null) {
			tabbedPane.addTab("Projects", createProjectsTab());
			tabbedPane.addTab("Sessions", createSessionsTab());
			tabbedPane.addTab("Metaproject", createMetaProjectTab());
			tabbedPane.addTab("Server Control", createControlTab());
		}
		add(tabbedPane);
	}

	private JPanel createSessionsTab() {
		return new SessionServerPanel(server, session);
	}

	private JPanel createProjectsTab() {
		return new ProjectsServerPanel(server, session);
	}

	private JPanel createControlTab() {
		return new ControlServerPanel(server, session);
	}

	private JPanel createMetaProjectTab() {
	    return new MetaprojectPanel(server, session);
	}

	private void installShutdownHook() {
		shutdownHook = new Thread("Remote Project ShutdownHook") {
			@Override
			public void run() {
				try {
					server.closeSession(session);
				} catch (Throwable t) {
					Log.getLogger().log(Level.INFO, "Exception caught", t);
				}
			}
		};
		try {
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		} catch (Exception e) { // happens in applets
			Log.getLogger().log(Level.WARNING, "Unable to install shutdown hook", e);
		}
	}

	private void uninstallShutdownHook() {
		try {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		} catch (Exception e) { // happens in applets
			Log.getLogger().log(Level.WARNING, "Unable to remove shutdown hook", e);
		}
	}


	public void dispose() {
		try {
			server.closeSession(session);
		} catch (Throwable t) {
			Log.getLogger().log(Level.INFO, "Exception caught", t);
		}
		uninstallShutdownHook();
	}


}
