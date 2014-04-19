package edu.stanford.smi.protege.server.admin;

import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.server.util.RemoteProjectUtil;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;

public class ControlServerPanel extends AbstractRefreshableServerPanel {
	private static final long serialVersionUID = 6214203367190208198L;

	public ControlServerPanel(RemoteServer server, RemoteSession session) {
		super(server, session);
	}

	@Override
	protected JComponent createCenterComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(Box.createVerticalStrut(10));

		JButton shutdownButton = new JButton(getShutdownAction());
		shutdownButton.setText("Shut down the Protege server");
		boolean hasShutdownRight = hasShutdownRight();
		shutdownButton.setEnabled(hasShutdownRight);
		if (!hasShutdownRight) {
			shutdownButton.setToolTipText("You do not have permission to shut down the server");
		}
		panel.add(shutdownButton);

		panel.add(Box.createVerticalStrut(20));

		boolean hasSaveRight = hasMetaProjectSaveRight();
		JButton saveMetaProjectButton = new JButton(getSaveMetaprojectAction());
		saveMetaProjectButton.setText("Save Metaproject");
		saveMetaProjectButton.setEnabled(hasSaveRight);
		if(!hasSaveRight) {
		    saveMetaProjectButton.setToolTipText("You do not have permission to save the metaproject");
		}
		panel.add(saveMetaProjectButton);

		LabeledComponent lc = new LabeledComponent("Control server operation", panel, true);
		return lc;
	}

	@Override
	protected void addRefreshButton() {
		return;
	}


	private Action getShutdownAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 1616026038020695181L;

            public void actionPerformed(ActionEvent arg0) {
				int ret = ModalDialog.showMessageDialog(ControlServerPanel.this,
						"The Protege server will shut down immediately.\n" +
						"All server projects will be saved.\n" +
						"Connected Protege clients might loose work because of this.\n\n" +
						"Are you really sure you want to shut down the server?",
						"Really shutdown?", ModalDialog.MODE_YES_NO);
				if (ret == ModalDialog.OPTION_YES) {
					shutdownServer();
				}
			}
		};
	}

	private void shutdownServer() {
		try {
			getServer().shutdown();
		} catch (RemoteException e) {
			Log.getLogger().log(Level.WARNING, "Server shutdown has failed.", e);
			ModalDialog.showMessageDialog(this, "Server shutdown has failed.\n" +
					"See console and logs for more information.");
			treatPossibleConnectionLostException(e);
			return;
		}
		ModalDialog.showMessageDialog(this, "Server shutdown successful.", "Success");
	}

	private boolean hasShutdownRight() {
		return RemoteProjectUtil.isServerOperationAllowed(getServer(), getSession(), MetaProjectConstants.OPERATION_ADMINISTER_SERVER) ||
		RemoteProjectUtil.isServerOperationAllowed(getServer(), getSession(), MetaProjectConstants.OPERATION_SHUTDOWN_SERVER);
	}


	private Action getSaveMetaprojectAction()  {
	    return new AbstractAction() {
	        private static final long serialVersionUID = -3866525559837315333L;

            public void actionPerformed(ActionEvent ev) {
	            boolean success = false;
	            try {
	                success = getServer().saveMetaProject(getSession());
	            } catch (RemoteException e) {
	                Log.getLogger().log(Level.WARNING, "Saving of metaproject has failed.", e);
	                ModalDialog.showMessageDialog(ControlServerPanel.this, "Saving of metaproject has failed.\n" +
	                        "See console and logs for more information.");
	                return;
	            }
	            ModalDialog.showMessageDialog(ControlServerPanel.this, success ? "Saving of metaproject was successful." :
	                    "Saving of metaproject has failed.", success ? "Success":"Failure");
	        }
	    };
	}


	private boolean hasMetaProjectSaveRight() {
        return RemoteProjectUtil.isServerOperationAllowed(getServer(), getSession(), MetaProjectConstants.OPERATION_ADMINISTER_SERVER);
	}

}
