package edu.stanford.smi.protege.server.admin;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculatorStats;
import edu.stanford.smi.protege.server.job.GetFrameCalculatorStatisticsJob;
import edu.stanford.smi.protege.server.job.GetProjectsStatusMapJob;
import edu.stanford.smi.protege.server.job.GetTransactionIsolationLevelJob;
import edu.stanford.smi.protege.server.job.GetUserInfoJob;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.server.util.RemoteProjectUtil;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SelectableTable;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protege.util.ViewAction;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;


public class ProjectsServerPanel extends AbstractRefreshableServerPanel {

	private static final long serialVersionUID = -3092833104709366831L;

	private DefaultTableModel prjsTableModel;
	private UserInfoTableModel userInfoTableModel;

	private JTextField serverRoundTripField;
	private JTextField transactionIsolationLevelField;
	private JTextField timeToFrameCacheField;

	private LabeledComponent userInfoLC;

	private AllowableAction stopProjectAction;
	private AllowableAction startProjectAction;

    private SelectableTable projectsTable;


	public ProjectsServerPanel(RemoteServer server, RemoteSession session) {
		super(server, session);
	}

	@Override
	protected JComponent createCenterComponent() {
		projectsTable = createProjectsTable();
		projectsTable.addSelectionListener(new SelectionListener() {
			public void selectionChanged(SelectionEvent event) {
				int row = projectsTable.getSelectedRow();
				updateUserInfoPanel(row);
			}
		});
		LabeledComponent projectsLC = new LabeledComponent("Remote projects", new JScrollPane(projectsTable), true);
		projectsLC.addHeaderButton(getViewAction(projectsTable));
		projectsLC.addHeaderButton(getStopProjectAction(projectsTable));
		projectsLC.addHeaderButton(getStartProjectAction(projectsTable));
		projectsLC.setPreferredSize(new Dimension(800, 300));

		JComponent userInfoPanel = createUserInfoPanel();
		userInfoLC = new LabeledComponent("Details (no project selected)", new JScrollPane(userInfoPanel), true);
		userInfoLC.setPreferredSize(new Dimension(800, 300));

		JSplitPane sp = ComponentFactory.createTopBottomSplitPane(projectsLC, userInfoLC, false);
		sp.setOneTouchExpandable(true);
		return sp;
	}

	private JComponent createUserInfoPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setPreferredSize(new Dimension(750,250));
		panel.add(new LabeledComponent("Statistics", createNumbersPanel(), true));
		panel.add(new LabeledComponent("Users", new JScrollPane(createUserInfoTable()), true));
		return panel;
	}

	private SelectableTable createUserInfoTable() {
		 userInfoTableModel = new UserInfoTableModel();
		 SelectableTable userPrjInfoTable = new SelectableTable();
		 userPrjInfoTable.setModel(userInfoTableModel);
		 return userPrjInfoTable;
	}

	private SelectableTable createProjectsTable() {
		SelectableTable projectsTable = ComponentFactory.createSelectableTable(null);
		projectsTable.setAutoCreateColumnsFromModel(true);
		projectsTable.setModel(createProjectsTableModel());
		return projectsTable;
	}

	private TableModel createProjectsTableModel() {
		prjsTableModel = new DefaultTableModel();
		prjsTableModel.addColumn("Project");
		prjsTableModel.addColumn("Status");
		prjsTableModel.addColumn("Sessions");

		fillProjectsTableModel();
		return prjsTableModel;
	}

	private JComponent createNumbersPanel() {
		int col2size = 8;
	    JPanel panel = new JPanel(new GridLayout(3,2,5,5));

	    panel.add(new JLabel("Estimated round trip time for this admin client (ms):"));
	    serverRoundTripField = createOutputTextField(col2size);
	    panel.add(serverRoundTripField);

	    panel.add(new JLabel("Milliseconds to calculate frame cache:"));
	    timeToFrameCacheField = createOutputTextField(col2size);
	    panel.add(timeToFrameCacheField);

	    panel.add(new JLabel("Transaction Isolation Level:"));
	    transactionIsolationLevelField = createOutputTextField(col2size);
	    panel.add(transactionIsolationLevelField);

	    return panel;
	}

	 private JTextField createOutputTextField(int size) {
		    JTextField field = new JTextField(size);
		    field.setEnabled(false);
		    field.setHorizontalAlignment(SwingConstants.LEFT);
		    return field;
	 }

	@SuppressWarnings("unchecked")
    private void fillProjectsTableModel() {
		prjsTableModel.setRowCount(0);

		try {
			Map<String, ProjectStatus> projectsToStatusMap = (Map<String, ProjectStatus>) new GetProjectsStatusMapJob(getServer(), getSession()).execute();

			for (String project : projectsToStatusMap.keySet()) {
				Collection<RemoteSession> sessions = new ArrayList<RemoteSession>();
				try {
					sessions = getServer().getCurrentSessions(project, getSession());
				} catch (RemoteException e) {
					Log.getLogger().log(Level.WARNING, "Error at getting the sessions for the remote project " + project, e);
					treatPossibleConnectionLostException(e);
				}
				prjsTableModel.addRow(new Object[] {project, projectsToStatusMap.get(project), sessions});
			}
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Error at getting projects status from server.", e);
			treatPossibleConnectionLostException(e);
		}
	}


	@SuppressWarnings("unchecked")
    private void updateUserInfoPanel(int row) {
		if (row < 0) {
			userInfoLC.setHeaderLabel("Details (no project selected)");
			userInfoTableModel.setUserInfo(null, null);
			return;
		}

		serverRoundTripField.setText("(not computed)");
		transactionIsolationLevelField.setText("(not computed)");
		timeToFrameCacheField.setText("(not computed)");

		String project = getProject(row);
		userInfoLC.setHeaderLabel("Details on project: " + project);

		Map<RemoteSession, Boolean> userInfo = null;
		FrameCalculatorStats stats = null;
		try {
			userInfo = (Map<RemoteSession, Boolean>) new GetUserInfoJob(getServer(), getSession(), project).execute();

			//we try to calculate also the server roundtrip time here
			long startTime = System.currentTimeMillis();
			stats = (FrameCalculatorStats) new GetFrameCalculatorStatisticsJob(getServer(), getSession(), project).execute();
			long interval = System.currentTimeMillis() - startTime;

			serverRoundTripField.setText("" + interval);
			if (stats != null) {
				timeToFrameCacheField.setText("" + stats.getPrecalculateTime());
			}

			TransactionIsolationLevel level = (TransactionIsolationLevel) new GetTransactionIsolationLevelJob(getServer(), getSession(), project).execute();
			if (level != null) {
				transactionIsolationLevelField.setText("" + level);
			}

		} catch (Throwable t) {
			Log.getLogger().log(Level.WARNING, "Failed to get user info from server for project " + project, t);
			treatPossibleConnectionLostException(t);
		}
		userInfoTableModel.setUserInfo(userInfo, stats);
	}


	private AllowableAction getStopProjectAction(final SelectableTable table) {
		stopProjectAction = new AllowableAction("Stop project(s)", Icons.getCancelIcon(), table) {
			private static final long serialVersionUID = 4683660482257589346L;

            public void actionPerformed(ActionEvent arg0) {
				int[] rows = table.getSelectedRows();
				Collection<String> projects = new ArrayList<String>();
				for (int i = 0; i < rows.length; i++) {
					projects.add(getProject(rows[i]));
				}
				ShutDownPanel shutDownPanel = new ShutDownPanel(projects);
				int opt = ModalDialog.showDialog(ProjectsServerPanel.this, shutDownPanel,
						"Shut down project(s)", ModalDialog.MODE_OK_CANCEL);
				if (opt == ModalDialog.OPTION_OK) {
					int secs = shutDownPanel.getShutdownInSec();
					for (String project : projects) {
						shutDownProject(project, secs);
					}
					refresh();
				}
			}

			private void shutDownProject(String project, int secs) {
				try {
					getServer().shutdownProject(getSession(), project, secs);
				} catch (RemoteException e) {
					Log.getLogger().log(Level.WARNING, "Could not shut down remote project " + project, e);
					ModalDialog.showMessageDialog(ProjectsServerPanel.this, "Shutting down the remote project " + project +
							" failed. \nSee console and log for more details.");
					treatPossibleConnectionLostException(e);
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
					if (!isStopAllowed(rows[i])) {
						this.setAllowed(false);
						return;
					}
				}
				this.setAllowed(true);
			}
		};

		return stopProjectAction;
	}

	private AllowableAction getStartProjectAction(final SelectableTable table) {
		startProjectAction = new AllowableAction("Start project or cancel shut down", Icons.getOkIcon(), table) {
			private static final long serialVersionUID = 4714705761432268351L;

            public void actionPerformed(ActionEvent arg0) {
				int row = table.getSelectedRow();
				String project = getProject(row);
				ProjectStatus status = getProjectStatus(row);

				if (status.equals(ProjectStatus.CLOSED_FOR_MAINTENANCE)) {
					handleStartProject(project);
					return;
				} else if (status.equals(ProjectStatus.SHUTTING_DOWN)) {
					handleCancelShutdown(project);
					return;
				}
			}

			private void handleStartProject(String project) {
				int start =
					JOptionPane.showConfirmDialog(ProjectsServerPanel.this, "Start selected project(s)?",
							"Confirm start projects",
							JOptionPane.OK_CANCEL_OPTION);
				if (start == JOptionPane.OK_OPTION ) {
					try {
						getServer().setProjectStatus(project, ProjectStatus.READY, getSession());
					} catch (RemoteException e) {
						Log.getLogger().log(Level.WARNING, "Could not start remote project " + project, e);
						ModalDialog.showMessageDialog(ProjectsServerPanel.this, "Could not start remote project "
								+ project + ".\n" +
								"See console and logs for more information.");
						treatPossibleConnectionLostException(e);
					}
					refresh();
				}
			}

			private void handleCancelShutdown(String project) {
				int cancelShutdown =
					JOptionPane.showConfirmDialog(ProjectsServerPanel.this,
							"Remote project " + project + " is in the process of shutting down.\n" +
							"Do you want to cancel the shut down operation?\n" +
							"Click on OK to cancel the operation.", "Cancel shut down",
							JOptionPane.OK_CANCEL_OPTION);
				boolean success = false;
				if (cancelShutdown == JOptionPane.OK_OPTION ) {
					try {
						success = getServer().cancelShutdownProject(getSession(), project);
					} catch (RemoteException e) {
						Log.getLogger().log(Level.WARNING, "Could not cancel shut down for remote project " + project, e);
						treatPossibleConnectionLostException(e);
					}
					if (!success) {
						ModalDialog.showMessageDialog(ProjectsServerPanel.this, "Could not cancel shut down of project " + project);
					} else {
						ModalDialog.showMessageDialog(ProjectsServerPanel.this, "Canceling the shut down of project " + project + " was successful.");
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
					if (!isStartAllowed(rows[i])) {
						this.setAllowed(false);
						return;
					}
				}
				this.setAllowed(true);
			}
		};

		return startProjectAction;
	}

	private AllowableAction getViewAction(final SelectableTable table) {
		return new ViewAction(table) {
			private static final long serialVersionUID = 4851439229648071746L;

            @Override
			public void onView() {
				int row = table.getSelectedRow();
				ComponentFactory.showTableRowInDialog(table.getModel(), row, ProjectsServerPanel.this);
			}
		};
	}


	@Override
	public void refresh() {
	    String selectedProject = getSelectedProject();
		try {
			fillProjectsTableModel();
			prjsTableModel.fireTableDataChanged();
		} catch (Throwable t) {
			treatPossibleConnectionLostException(t);
		}
		setProjectTableSelection(selectedProject);
	}

    private String getSelectedProject() {
	    int row = projectsTable.getSelectedRow();
	    if (row == -1) { return null ;}
	    return (String) prjsTableModel.getValueAt(row, 0);
	}

    private void setProjectTableSelection(String selectedProject) {
        if (selectedProject == null) { return; }
        for (int i = 0; i < prjsTableModel.getRowCount(); i++) {
            String prj = (String) prjsTableModel.getValueAt(i, 0);
            if (prj != null && prj.equals(selectedProject)) {
                projectsTable.getSelectionModel().setSelectionInterval(i, i);
            }
        }
    }

	private ProjectStatus getProjectStatus(int row) {
		return (ProjectStatus) prjsTableModel.getValueAt(row, 1);
	}

	private String getProject(int row) {
		return (String)prjsTableModel.getValueAt(row, 0);
	}

	private boolean isStopAllowed(int row) {
		return getProjectStatus(row).equals(ProjectStatus.READY) && (
			RemoteProjectUtil.isServerOperationAllowed(getServer(), getSession(), MetaProjectConstants.OPERATION_ADMINISTER_SERVER) ||
			RemoteProjectUtil.isOperationAllowed(getServer(), getSession(), getProject(row), MetaProjectConstants.OPERATION_STOP_REMOTE_PROJECT));
	}

	private boolean isStartAllowed(int row) {
		return (getProjectStatus(row).equals(ProjectStatus.CLOSED_FOR_MAINTENANCE) ||
				getProjectStatus(row).equals(ProjectStatus.SHUTTING_DOWN)) &&
				(RemoteProjectUtil.isServerOperationAllowed(getServer(), getSession(), MetaProjectConstants.OPERATION_ADMINISTER_SERVER) ||
				 RemoteProjectUtil.isOperationAllowed(getServer(), getSession(), getProject(row), MetaProjectConstants.OPERATION_START_REMOTE_PROJECT));
	}


	class ShutDownPanel extends JPanel {
		private static final long serialVersionUID = 5895752980483951552L;
        private final JTextField minsTextField = new JTextField(5);

		ShutDownPanel(Collection<String> projects){
			 SpringLayout layout = new SpringLayout();
			 setLayout(layout);
			 JLabel shLabel = new JLabel("Shutdown time (in minutes):");
			 layout.putConstraint(SpringLayout.WEST, shLabel, 5, SpringLayout.WEST, this);
			 layout.putConstraint(SpringLayout.NORTH, shLabel, 5, SpringLayout.NORTH, this);
			 layout.putConstraint(SpringLayout.WEST, minsTextField, 5, SpringLayout.EAST, shLabel);
			 layout.putConstraint(SpringLayout.NORTH, minsTextField, 3,  SpringLayout.NORTH, this);
			 minsTextField.setText("10");
			 StringBuffer projectsString = new StringBuffer();
			 for (String project : projects) {
				 projectsString.append(project);
				 projectsString.append("<br>");
			 }
			 JLabel confLabel = new JLabel("<html><b>Are you sure you want to shut down the following project(s)? <br><br>"
					 + projectsString + "</b><br>" +
			 		"This will affect all the users connected currently to the listed project(s).</html>");
			 layout.putConstraint(SpringLayout.WEST, confLabel, 5, SpringLayout.WEST, this);
			 layout.putConstraint(SpringLayout.NORTH, confLabel, 20, SpringLayout.SOUTH, shLabel);
			 layout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST, confLabel);
			 layout.putConstraint(SpringLayout.SOUTH, this, 5, SpringLayout.SOUTH, confLabel);

			 add(shLabel);
			 add(minsTextField);
			 add(confLabel);
		}

		int getShutdownInSec() {
			String str = minsTextField.getText().trim();
			return (int) (Float.valueOf(str) * 60);
		}

	}

}
