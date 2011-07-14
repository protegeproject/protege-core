package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class RevertProjectPanel extends JPanel {
    private static final long serialVersionUID = 8202074349453422106L;
    private static boolean _lastArchiveCurrentValue;
    private Project _currentProject;
    private JCheckBox _archiveCurrentCheckBox;
    private JTable _table;

    RevertProjectPanel(Project p) {
        super(new BorderLayout());
        _currentProject = p;
        add(createVersionsTable(), BorderLayout.CENTER);
        add(createOptionsPanel(), BorderLayout.SOUTH);
    }

    private JComponent createVersionsTable() {
        _table = ComponentFactory.createTable(null);
        _table.setAutoCreateColumnsFromModel(true);
        _table.setModel(createTableModel());
        _table.addMouseListener(new ModalDialogCloseDoubleClickAdapter());
        int selectedRow = _table.getRowCount() - 1;
        if (selectedRow >= 0) {
            _table.setRowSelectionInterval(selectedRow, selectedRow);
        }
        return new LabeledComponent("Archived Versions", ComponentFactory.createScrollPane(_table));
    }

    private JComponent createOptionsPanel() {
        _archiveCurrentCheckBox = new JCheckBox("Archive current version before revert");
        _archiveCurrentCheckBox.setSelected(_lastArchiveCurrentValue);
        return _archiveCurrentCheckBox;
    }

    public Date getSelectedTimestamp() {
        Date date = null;
        int row = _table.getSelectedRow();
        if (row != -1) {
            date = (Date) _table.getValueAt(row, 0);
        }
        return date;
    }

    public boolean getArchiveCurrentVersion() {
        _lastArchiveCurrentValue = _archiveCurrentCheckBox.isSelected();
        return _lastArchiveCurrentValue;
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
