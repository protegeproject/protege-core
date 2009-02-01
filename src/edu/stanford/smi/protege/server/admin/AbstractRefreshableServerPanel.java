package edu.stanford.smi.protege.server.admin;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;

public abstract class AbstractRefreshableServerPanel extends JPanel {
	
	private static final long serialVersionUID = 1545066175980799819L;
	
	private RemoteServer server;
	private RemoteSession session;
	private JComponent centerComponent;
	private JComponent footerComponent;

	public AbstractRefreshableServerPanel(RemoteServer server, RemoteSession session) {
		this.server = server;
		this.session = session;
		init();
	}

	protected void init() {
		setLayout(new BorderLayout());

		add(centerComponent = createCenterComponent(), BorderLayout.CENTER);
		add(footerComponent = createFooterComponent(), BorderLayout.SOUTH);

		addRefreshButton();
	}


	protected JComponent createFooterComponent() {
		return new JPanel();
	}

	protected JComponent createCenterComponent() {
		return new JPanel();
	}

	protected void addRefreshButton() {
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				refresh();
			}
		});
		footerComponent.add(refreshButton);
	}

	public void refresh() {}

	public RemoteServer getServer() {
		return server;
	}
	
	public RemoteSession getSession() {
		return session;
	}

	public JComponent getCenterComponent() {
		return centerComponent;
	}

	public JComponent getFooterComponent() {
		return footerComponent;
	}
	
}
