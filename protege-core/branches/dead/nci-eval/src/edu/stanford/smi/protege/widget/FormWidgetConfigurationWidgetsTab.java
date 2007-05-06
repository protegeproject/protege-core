package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Tab for configuring which slots appear on a form widget.  This operation can also be done directly on the form itself
 * one slot at a time.  The unique thing that can be done here is to hide or "unhide" a widget associated with a particular
 * slot.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class FormWidgetConfigurationWidgetsTab extends AbstractValidatableComponent {
    private FormWidget _formWidget;
    private JTable _table;

    public FormWidgetConfigurationWidgetsTab(FormWidget widget) {
        _formWidget = widget;
        setLayout(new BorderLayout());
        add(createTableComponent());
    }

    private JComponent createTableComponent() {
        _table = ComponentFactory.createTable(null);
        _table.setModel(createTableModel());
        ComponentUtilities.addColumn(_table, FrameRenderer.createInstance());
        ComponentUtilities.addColumn(_table, new WidgetClassNameRenderer(), new WidgetDescriptorEditor(_formWidget));
        return ComponentFactory.createScrollPane(_table);
    }

    private TableModel createTableModel() {
        PropertyList propertyList = _formWidget.getPropertyList();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Slot");
        model.addColumn("Widget");
        List slots = new ArrayList(_formWidget.getCls().getVisibleTemplateSlots());
        Collections.sort(slots);
        Iterator i = slots.iterator();
        while (i.hasNext()) {
            Slot slot = (Slot) i.next();
            Rectangle bounds = null;
            WidgetDescriptor d = propertyList.getWidgetDescriptor(slot.getName());
            Assert.assertNotNull("widget descriptor for " + slot, d);
            String widgetClassName = (d == null) ? (String) null : d.getWidgetClassName();
            if (widgetClassName == null) {
                widgetClassName = WidgetClassNameRenderer.NONE;
            } else {
                bounds = d.getBounds();
            }
            model.addRow(new Object[] { slot, widgetClassName, bounds });
        }
        return model;
    }

    public void saveContents() {
        int nRows = _table.getRowCount();
        TableModel model = _table.getModel();
        for (int row = 0; row < nRows; ++row) {
            Slot slot = (Slot) model.getValueAt(row, 0);
            String widgetClassName = (String) model.getValueAt(row, 1);
            if (widgetClassName.equals(WidgetClassNameRenderer.NONE)) {
                widgetClassName = null;
            }
            _formWidget.replaceWidget(slot, widgetClassName);
        }
    }

    public boolean validateContents() {
        return true;
    }
}
