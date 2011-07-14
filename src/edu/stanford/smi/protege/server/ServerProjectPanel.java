package edu.stanford.smi.protege.server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;

/**
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ServerProjectPanel extends JPanel {
    private static final long serialVersionUID = 1275501290404489474L;
    private RemoteServer _server;
    private RemoteSession _session;
    private JTable _projectTable;
    private String _lastOpenedPrj = "";

    public final static String SERVER_LAST_OPENED_PROJECT_PROP = "server.last.opened.project";

    public ServerProjectPanel(RemoteServer server, RemoteSession session) {
        _server = server;
        _session = session;
        _projectTable = ComponentFactory.createTable(null);
        _lastOpenedPrj = ApplicationProperties.getString(SERVER_LAST_OPENED_PROJECT_PROP, "");
        setLayout(new BorderLayout());
        add(ComponentFactory.createScrollPane(_projectTable));
        loadTable();
        setMinimumSize(new Dimension(400, 300));
    }

    private void loadTable() {
        try {
            Collection names = _server.getAvailableProjectNames(_session);
            Iterator iterator = names.iterator();
            Object[][] data = new Object[names.size()][2];
            int lastOpenPrjIndex = 0;
            for (int i = 0; i < names.size(); ++i) {
                //ESCA-JAVA0282
                String name = (String) iterator.next();
                if (name.equals(_lastOpenedPrj)) {
                	lastOpenPrjIndex = i;
                }
                data[i][0] = name;
                data[i][1] = sessionsToString(_server.getCurrentSessions(name, _session));
            }
            Object[] columns = new Object[] { "Project", "Current Users" };
            _projectTable.setModel(new DefaultTableModel(data, columns));
            _projectTable.createDefaultColumnsFromModel();
            if (names.size() > 0) {
            	_projectTable.setRowSelectionInterval(lastOpenPrjIndex, lastOpenPrjIndex);
            }
        } catch (RemoteException e) {
            Log.getLogger().severe(Log.toString(e));
        }
    }

    private static String sessionsToString(Collection userNames) {
        StringBuffer s = new StringBuffer();
        Iterator i = userNames.iterator();
        while (i.hasNext()) {
            Session session = (Session) i.next();
            if (s.length() > 0) {
                s.append(", ");
            }
            s.append(session.getUserName() + " (" + session.getUserIpAddress() + ")");
        }
        return s.toString();
    }

    public String getProjectName() {
        String name;
        int row = _projectTable.getSelectedRow();
        if (row < 0) {
            name = null;
        } else {
            name = (String) _projectTable.getValueAt(row, 0);
            ApplicationProperties.setString(SERVER_LAST_OPENED_PROJECT_PROP, name);
        }
        return name;
    }
}