package edu.stanford.smi.protege.server.admin;

import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Session;
import edu.stanford.smi.protege.server.job.GetProjectsForSessionJob;
import edu.stanford.smi.protege.server.job.GetSessionsJob;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.server.util.RemoteProjectUtil;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SelectableTable;
import edu.stanford.smi.protege.util.ViewAction;


public class SessionServerPanel extends AbstractRefreshableServerPanel {

	private static final long serialVersionUID = 8567269732489128636L;
	
	private static final String UNKNOWN_ENTRY = "(unknown)";
	private static final String NO_PROJECT = "(none)";
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss, yyyy.MM.dd");

	private DefaultTableModel tableModel;
	private List<RemoteSession> remoteSessions;
	private AllowableAction killSessionAction;

	public SessionServerPanel(RemoteServer server, RemoteSession session) {
		super(server, session);		
	}

	@Override
	protected JComponent createCenterComponent() {
		remoteSessions = new ArrayList<RemoteSession>();
		SelectableTable sessionsTable = ComponentFactory.createSelectableTable(null);
		sessionsTable.setAutoCreateColumnsFromModel(true);
		sessionsTable.setModel(createTableModel());

		LabeledComponent lc = new LabeledComponent("Live sessions", new JScrollPane(sessionsTable), true);
		lc.addHeaderButton(getViewAction(sessionsTable));
		lc.addHeaderButton(getKillSessionAction(sessionsTable));
		
		return lc;
	}
	
	private TableModel createTableModel() {
		tableModel = new DefaultTableModel();
		tableModel.addColumn("Id");		
		tableModel.addColumn("User name");
		tableModel.addColumn("User IP");
		tableModel.addColumn("Project");
		tableModel.addColumn("Login time");

		fillTableModel();

		return tableModel;
	}

	private void fillTableModel() {
		tableModel.setRowCount(0);
		remoteSessions.clear();

		RemoteSession mySession = getSession();

		try {
			Collection<RemoteSession> sessions = (Collection<RemoteSession>) new GetSessionsJob(getServer(), mySession).execute();
			for (RemoteSession session : sessions) {
				remoteSessions.add(session);
				Collection<String> prjs = (Collection<String>) new GetProjectsForSessionJob(getServer(), mySession, session).execute();
				if (prjs.size() == 0) { //it might be an administrative session
					prjs = new ArrayList<String>();
					prjs.add(NO_PROJECT);
				}

				String loginTime = UNKNOWN_ENTRY;
				String sessionId = UNKNOWN_ENTRY;

				if (session instanceof Session) {
					Date date = new Date(((Session) session).getStartTime());
					loginTime = dateFormat.format(date);
					sessionId = Integer.toString(((Session)session).getId());

					if (session.equals(mySession)) {
						sessionId = "*" + sessionId;
					}
				}
				tableModel.addRow(new Object[] {sessionId, session.getUserName(), session.getUserIpAddress(), prjs, loginTime});
			}
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Could not get remote sessions from server.", e);
			treatPossibleConnectionLostException(e);
		}
	}


	private AllowableAction getKillSessionAction(final SelectableTable table) {
		killSessionAction = new AllowableAction("Kill session(s)", Icons.getCancelIcon(), table) {

			private static final long serialVersionUID = -845012769836420622L;

            public void actionPerformed(ActionEvent arg0) {				
				int kill =
					JOptionPane.showConfirmDialog(SessionServerPanel.this, "Kill selected session(s)?" +
							"\nUsers may lose work as a result of this action.",
							"Confirm Kill Sessions",
							JOptionPane.OK_CANCEL_OPTION);
				if (kill == JOptionPane.OK_OPTION ) {
					int[] rows = table.getSelectedRows();
					
					for (int i = 0; i < rows.length; i++) {
						RemoteSession session = remoteSessions.get(rows[i]);
						try {
							getServer().killOtherUserSession(session, getSession(), 10);
						} catch (RemoteException e) {
							Log.getLogger().log(Level.WARNING, "Could not kill session " + session, e);
							ModalDialog.showMessageDialog(SessionServerPanel.this, "Could not kill session " + session + ".\n" +
									"See console and logs for more information.");
							treatPossibleConnectionLostException(e);
						}
					}										
					refresh();
				}
			}

			@Override
			public void onSelectionChange() {
				int[] rows = table.getSelectedRows();
				if (rows == null || rows.length <= 0) {
					this.setAllowed(false);
					return;
				}
				for (int i = 0; i < rows.length; i++) {
					RemoteSession session = remoteSessions.get(rows[i]);
					if (!isKillAllowed(session)) {
						this.setAllowed(false);
						return;
					}
				}
				this.setAllowed(true);				
			}
		};

		return killSessionAction;
	}

	private AllowableAction getViewAction(final SelectableTable table) {
		return new ViewAction(table) {
			private static final long serialVersionUID = -2389054693945285483L;

            @Override
			public void onView() {
				int row = table.getSelectedRow();
				ComponentFactory.showTableRowInDialog(table.getModel(), row, SessionServerPanel.this);
			}						
		};
	}
	

	@Override
	public void refresh() {
		try {
			fillTableModel();
			tableModel.fireTableDataChanged();
		} catch (Throwable t) {
			treatPossibleConnectionLostException(t);
		}
	}

	private boolean isKillAllowed(RemoteSession session) {
		if (session.equals(getSession())) {
			return false; //can't kill this session
		}
		int row = remoteSessions.indexOf(session);
		if (row < 0) { return false; }
		if (RemoteProjectUtil.isServerOperationAllowed(getServer(), getSession(), MetaProjectConstants.OPERATION_ADMINISTER_SERVER)) {
			return true;
		}
		if (!RemoteProjectUtil.isServerOperationAllowed(getServer(), getSession(), MetaProjectConstants.OPERATION_KILL_OTHER_USER_SESSION)) {
			return false;
		}
		//for the session to kill check if each project in the session allows the session kill
		Collection<String> prjsInSession = (Collection<String>) tableModel.getValueAt(row, 3);
		for (Iterator<String> iterator = prjsInSession.iterator(); iterator.hasNext();) {
			String project = iterator.next();			
			if (	!project.equals(NO_PROJECT) &&					
					!RemoteProjectUtil.isOperationAllowed(getServer(), getSession(), project, MetaProjectConstants.OPERATION_KILL_OTHER_USER_SESSION)) {
						return false;
			}			
		}
		return true;
	}	

}
