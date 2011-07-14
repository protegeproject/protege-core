package edu.stanford.smi.protege.action;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ConfigureArchivePanel extends JPanel {
    private static final long serialVersionUID = 4467651841510373106L;
    private Project _currentProject;

    public ConfigureArchivePanel(Project project) {
        super(new BorderLayout());
        _currentProject = project;
        add(createExistingArchiveVersionsPanel(), BorderLayout.CENTER);
        add(createOptionsPanel(), BorderLayout.SOUTH);
    }

    private static JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(createAutoArchivePanel());
        panel.add(createNumberOfArchivedVersionsPanel());
        return panel;
    }

    private static JComponent createAutoArchivePanel() {
        return new JCheckBox("Automatically archive the current version on Save");
    }

    private static JComponent createNumberOfArchivedVersionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(ComponentFactory.createLabel("Maximum number of archived versions"));
        panel.add(new JTextField(5));
        return panel;
    }

    private JComponent createExistingArchiveVersionsPanel() {
        JTable table = new JTable(); // ComponentFactory.createTable(null);
        table.setModel(createTableModel());
        LabeledComponent c = new LabeledComponent("Archived Versions", ComponentFactory.createScrollPane(table));
        c.addHeaderButton(createDeleteVersionButton());
        return c;
    }

    private static Action createDeleteVersionButton() {
        return new AbstractAction("Delete Selected Version", Icons.getDeleteIcon()) {
            private static final long serialVersionUID = 7721643833354014740L;

            public void actionPerformed(ActionEvent event) {
                Log.getLogger().info("delete selected version");
            }
        };
    }

    private TableModel createTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Date and Time");
        model.addColumn("Comment");
        Iterator i = ArchiveManager.getArchiveManager().getArchiveRecords(_currentProject).iterator();
        while (i.hasNext()) {
            ArchiveRecord record = (ArchiveRecord) i.next();
            Date timestamp = record.getTimestamp();
            String comment = record.getComment();
            model.addRow(new Object[] { timestamp, comment });
        }
        return model;
    }
}
