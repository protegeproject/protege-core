package edu.stanford.smi.protege.server;

import java.awt.*;
import java.rmi.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ServerProjectPanel extends JPanel {
    private RemoteServer _server;
    private RemoteSession _session;
    private JTable _projectTable;

    public ServerProjectPanel(RemoteServer server, RemoteSession session) {
        _server = server;
        _session = session;
        _projectTable = ComponentFactory.createTable(null);
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
            for (int i = 0; i < names.size(); ++i) {
                String name = (String) iterator.next();
                data[i][0] = name;
                data[i][1] = sessionsToString(_server.getCurrentSessions(name, _session));
            }
            Object[] columns = new Object[] { "Project", "Current Users" };
            _projectTable.setModel(new DefaultTableModel(data, columns));
            _projectTable.createDefaultColumnsFromModel();
            _projectTable.setRowSelectionInterval(0, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
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
            s.append(session.getUserName() + " (" + session.getUserMachine() + ")");
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
        }
        return name;
    }
}