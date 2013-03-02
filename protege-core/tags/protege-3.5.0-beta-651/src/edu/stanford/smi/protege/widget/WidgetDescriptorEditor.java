package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Allows the user to pick one of the allowed widgets for a given slot.  Also available is the choice "none" that 
 * removes the slot widget for the slot.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class WidgetDescriptorEditor extends DefaultCellEditor {

    private static final long serialVersionUID = -7611162969958514326L;
    private FormWidget _widget;

    public WidgetDescriptorEditor(FormWidget widget) {
        super(ComponentFactory.createComboBox());
        _widget = widget;
        getComboBox().setRenderer(new WidgetClassNameRenderer());
    }

    private JComboBox getComboBox() {
        return (JComboBox) getComponent();
    }

    private ComboBoxModel getComboBoxModel(Cls cls, Slot slot) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(WidgetClassNameRenderer.NONE);
        Project p = _widget.getProject();
        Iterator i = p.getSuitableWidgetClassNames(cls, slot, null).iterator();
        while (i.hasNext()) {
            model.addElement(i.next());
        }
        return model;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JComboBox box = getComboBox();
        Slot slot = (Slot) table.getModel().getValueAt(row, 0);
        box.setModel(getComboBoxModel(_widget.getCls(), slot));
        box.setSelectedItem(value);
        return getComponent();
    }
}
