package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * Configure the tab in the application.  This allows the tabs to be enabled and ordered.  It also allows for tab 
 * specific configuration.
 * 
 * @author  Ray Fergerson <fergerson@smi.stanford.edu>
 */

class ConfigureTabsPanel extends AbstractValidatableComponent {

    private static final long serialVersionUID = 230283792426972639L;
    private JTable _table;
    private Project _project;
    private boolean _dirty;

    private class MoveTabUp extends AbstractAction {
        private static final long serialVersionUID = 5052392705264419616L;

        MoveTabUp() {
            super("Move selected tab up", Icons.getUpIcon());
        }

        public void actionPerformed(ActionEvent event) {
            int index = _table.getSelectedRow();
            if (canMoveUp(index)) {
                getTabModel().moveRow(index, index, index - 1);
                int n = index - 1;
                _table.getSelectionModel().setSelectionInterval(n, n);
                _dirty = true;
            }
        }
    }

    private class MoveTabDown extends AbstractAction {
        private static final long serialVersionUID = -7034589703352346373L;

        MoveTabDown() {
            super("Move selected tab down", Icons.getDownIcon());
        }

        public void actionPerformed(ActionEvent event) {
            int index = _table.getSelectedRow();
            if (canMoveDown(index)) {
                getTabModel().moveRow(index, index, index + 1);
                int n = index + 1;
                _table.getSelectionModel().setSelectionInterval(n, n);
                _dirty = true;
            }
        }
    }

    private boolean canMoveUp(int index) {
        return index > 0 && isEnabled(index);
    }

    private boolean canMoveDown(int index) {
        boolean canMoveDown = 0 <= index && index < _table.getRowCount() - 1;
        if (canMoveDown) {
            canMoveDown = isEnabled(index) && canEnable(index + 1);
        }
        return canMoveDown;
    }

    private boolean isEnabled(int row) {
        Boolean b = (Boolean) getTabModel().getValueAt(row, 0);
        return b.booleanValue();
    }

    private void setEnabled(int row, boolean enabled) {
        getTabModel().setValueAt(Boolean.valueOf(enabled), row, 0);
    }

    private class ClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent event) {
            Point p = event.getPoint();
            int col = _table.columnAtPoint(p);
            if (col == 0) {
                int row = _table.rowAtPoint(p);
                if (isEditable(row)) {
                    boolean b = isEnabled(row);
                    setEnabled(row, !b);
                    _dirty = true;
                }
            }
        }
    }

    private boolean isEditable(int row) {
        WidgetDescriptor d = getDescriptor(row);
        Collection strings = new ArrayList();
        return WidgetUtilities.isSuitableTab(d.getWidgetClassName(), _project, strings);
    }

    protected ConfigureTabsPanel(Project project) {
        setLayout(new BorderLayout());
        _project = project;
        _table = ComponentFactory.createTable(getConfigureAction());
        _table.setModel(createTableModel());
        ComponentUtilities.addColumn(_table, new WidgetDescriptorEnableRenderer());
        _table.getColumnModel().getColumn(0).setMaxWidth(50);
        ComponentUtilities.addColumn(_table, new WidgetDescriptorRenderer(project));
        _table.addMouseListener(new ClickListener());
        JScrollPane pane = ComponentFactory.createScrollPane(_table);
        pane.setColumnHeaderView(_table.getTableHeader());
        pane.setBackground(_table.getBackground());
        LabeledComponent c = new LabeledComponent("Tabs", pane);
        c.addHeaderButton(new MoveTabUp());
        c.addHeaderButton(new MoveTabDown());
        add(c);
    }

    private TableModel createTableModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Visible");
        model.addColumn("Tab Widget");
        boolean all = true; // _filterComboBox.getSelectedItem().equals(ALL);

        Collection tabDescriptors = new ArrayList(_project.getTabWidgetDescriptors());
        tabDescriptors = sort(tabDescriptors);
        Iterator i = tabDescriptors.iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = (WidgetDescriptor) i.next();
            if (all || canEnable(d)) {
                model.addRow(new Object[] { Boolean.valueOf(d.isVisible()), d });
            }
        }
        return model;
    }

    private Collection sort(Collection descriptors) {
        List sortedDescriptors = new ArrayList(descriptors);
        int i;
        for (i = 0; i < sortedDescriptors.size(); ++i) {
            WidgetDescriptor d = (WidgetDescriptor) sortedDescriptors.get(i);
            if (!d.isVisible()) {
                break;
            }
        }
        Collections.sort(sortedDescriptors, new WidgetDescriptorComparator());
        return sortedDescriptors;
    }

    private Action getConfigureAction() {
        return new AbstractAction("Configure") {
            private static final long serialVersionUID = 900646780807779030L;

            public void actionPerformed(ActionEvent event) {
                int row = _table.getSelectedRow();
                WidgetDescriptor d = getDescriptor(row);
                if (d.isVisible()) {
                    TabWidget widget = ProjectManager.getProjectManager().getCurrentProjectView().getTabByClassName(
                            d.getWidgetClassName());
                    widget.configure();
                }
            }
        };
    }

    private DefaultTableModel getTabModel() {
        return (DefaultTableModel) _table.getModel();
    }

    public void saveContents() {
        if (_dirty) {
            Collection tabWidgetDescriptors = new ArrayList();
            for (int row = 0; row < getTabModel().getRowCount(); ++row) {
                boolean isEnabled = isEnabled(row);
                WidgetDescriptor descriptor = getDescriptor(row);
                descriptor.setVisible(isEnabled);
                tabWidgetDescriptors.add(descriptor);
            }
            _project.setTabWidgetDescriptorOrder(tabWidgetDescriptors);
        }
    }

    public boolean validateContents() {
        return true;
    }

    private boolean canEnable(String className) {
        return WidgetUtilities.isSuitableTab(className, _project, new ArrayList());
    }

    private WidgetDescriptor getDescriptor(int row) {
        return (WidgetDescriptor) getTabModel().getValueAt(row, 1);
    }

    private boolean canEnable(int row) {
        WidgetDescriptor d = getDescriptor(row);
        return canEnable(d);
    }

    private boolean canEnable(WidgetDescriptor d) {
        return canEnable(d.getWidgetClassName());
    }

    class WidgetDescriptorComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            WidgetDescriptor wd1 = (WidgetDescriptor) o1;
            WidgetDescriptor wd2 = (WidgetDescriptor) o2;
            boolean isEnabled1 = wd1.isVisible();
            boolean isEnabled2 = wd2.isVisible();
            int compare;
            if (isEnabled1) {
                compare = isEnabled2 ? 0 : -1;
            } else {
                compare = isEnabled2 ? +1 : 0;
            }
            if (!isEnabled1 && !isEnabled2) {
                String n1 = wd1.getWidgetClassName();
                String n2 = wd2.getWidgetClassName();
                boolean canEnable1 = canEnable(n1);
                boolean canEnable2 = canEnable(n2);
                if (canEnable1) {
                    compare = canEnable2 ? 0 : -1;
                } else {
                    compare = canEnable2 ? +1 : 0;
                }
                if (compare == 0) {
                    String sn1 = StringUtilities.getShortClassName(n1);
                    String sn2 = StringUtilities.getShortClassName(n2);
                    compare = sn1.compareToIgnoreCase(sn2);
                }
            }
            return compare;
        }

    }

    class WidgetDescriptorEnableRenderer extends CheckBoxRenderer {
        private static final long serialVersionUID = 6693202760974205518L;
        private final Component EMPTY;

        {
            EMPTY = new JPanel() {
                private static final long serialVersionUID = -2060692706529621224L;

                public boolean isOpaque() {
                    return false;
                }
            };
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean b,
                int row, int col) {
            Component c;
            if (canEnable(row)) {
                c = super.getTableCellRendererComponent(table, value, isSelected, b, row, col);
            } else {
                c = EMPTY;
            }
            return c;
        }
    }
}
