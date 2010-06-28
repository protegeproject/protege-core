package edu.stanford.smi.protege.server.admin;

import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.metaproject.MetaProject;
import edu.stanford.smi.protege.server.metaproject.impl.MetaProjectImpl;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.widget.ClsesAndInstancesTab;

public class MetaprojectPanel extends AbstractRefreshableServerPanel {
    private static final long serialVersionUID = 6214203367190208198L;

    public MetaprojectPanel(RemoteServer server, RemoteSession session) {
        super(server, session);
    }

    @Override
    protected JComponent createCenterComponent() {
        MetaProject mp = null;
        try {
            mp = RemoteProjectManager.getInstance().connectToMetaProject(getServer(), getSession());
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at getting the metaproject", e);
        }

        if (mp == null) {
            JPanel panel = new JPanel();
            panel.add(new JLabel("<html><i><b>You do not have enough privileges to change the metaproject.</b></i></html>"));
            panel.setBorder(BorderFactory.createRaisedBevelBorder());
            return panel;
        }

        KnowledgeBase mp_kb = ((MetaProjectImpl) mp).getKnowledgeBase();
        Project mp_prj = mp_kb.getProject();
        mp_prj.setIsReadonly(false);

        ClsesAndInstancesTab instTab = new ClsesAndInstancesTab();
        instTab.setup(mp_prj.createWidgetDescriptor(), mp_prj);
        instTab.initialize();

        return instTab;
    }

    @Override
    protected void addRefreshButton() {
        return;
    }
}
