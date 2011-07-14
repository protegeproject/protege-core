package edu.stanford.smi.protege.widget;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Base class for all widgets which are predominately a JTable.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractTableWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = -5973931887065705739L;
    private JTable _table;
    private LabeledComponent _labeledComponent;

    protected AbstractTableWidget() {
        setPreferredColumns(2);
        setPreferredRows(4);
    }

    public void addButton(Action action) {
        addButton(action, true);
    }

    public void addButton(Action action, boolean defaultState) {
        addButtonConfiguration(action, defaultState);
        if (displayButton(action)) {
            _labeledComponent.addHeaderButton(action);
        }
    }

    protected boolean hasButton(Icon icon) {
        return _labeledComponent.hasHeaderButton(icon);
    }

    public void addColumn(int width, ResourceKey key, TableCellRenderer renderer) {
        TableColumn column = new TableColumn(0, width, renderer, null);
        String name = LocalizedText.getText(key);
        column.setHeaderValue(name);
        _table.addColumn(column);
    }

    protected void configureTable(JTable table) {
        table.setModel(createTableModel());
        table.getSelectionModel().addListSelectionListener(new ListSelectionListenerAdapter(this));
        table.addMouseListener(new TablePopupMenuMouseListener(table) {
            public JPopupMenu getPopupMenu() {
                return AbstractTableWidget.this.getPopupMenu();
            }
        });
    }

    public JComponent createMainComponent(Action action) {
        JScrollPane pane = ComponentFactory.createScrollPane(createTable(action));
        JViewport viewPort = pane.getViewport();
        viewPort.addMouseListener(new PopupMenuMouseListener(viewPort) {
            public JPopupMenu getPopupMenu() {
                return AbstractTableWidget.this.getPopupMenu();
            }

            public void setSelection(JComponent c, int x, int y) {
            }
        });
        _labeledComponent = new LabeledComponent(getLabel(), pane);
        return _labeledComponent;
    }

    protected JTable createTable(Action action) {
        _table = ComponentFactory.createSelectableTable(action);
        configureTable(_table);
        return _table;
    }

    public abstract TableModel createTableModel();

    //ESCA-JAVA0130 
    public JPopupMenu getPopupMenu() {
        return null;
    }

    private static int getRow(TableModel model, Object o) {
        int row = -1;
        int nRows = model.getRowCount();
        for (int i = 0; i < nRows; ++i) {
            if (model.getValueAt(i, 0).equals(o)) {
                row = i;
                break;
            }
        }
        return row;
    }

    public Collection getSelection() {
        return ComponentUtilities.getSelection(_table);
    }

    private Collection getSelections() {
        TableModel model = _table.getModel();
        Collection selections = new ArrayList();
        int[] rows = _table.getSelectedRows();
        if (rows != null) {
            for (int i = 0; i < rows.length; ++i) {
                selections.add(model.getValueAt(rows[i], 0));
            }
        }
        return selections;
    }

    public JTable getTable() {
        return _table;
    }

    public void initialize() {
        initialize(null);
    }

    public void initialize(Action action) {
        add(createMainComponent(action));
        reload();
    }

    public void reload() {
        Collection selections = getSelections();
        _table.setModel(createTableModel());
        setSelections(selections);
        _table.revalidate();
        _table.repaint();
        _table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    }

    public void setInstance(Instance instance) {
        super.setInstance(instance);
        reload();
    }

    private void setSelections(Collection c) {
        TableModel model = _table.getModel();
        _table.clearSelection();
        Iterator i = c.iterator();
        while (i.hasNext()) {
            int row = getRow(model, i.next());
            if (row != -1) {
                _table.addRowSelectionInterval(row, row);
            }
        }
    }
}
