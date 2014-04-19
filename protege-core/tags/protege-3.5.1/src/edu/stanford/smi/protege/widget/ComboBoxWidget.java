package edu.stanford.smi.protege.widget;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * Slot widget for displaying the "allowed values" of a value-type=Symbol slot.  The values are displayed in a standard
 * drop-down list.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ComboBoxWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = -1649506562178607049L;
    protected final static String NONE = "";
    private JComboBox _comboBox;
    private boolean _displayNullEntry = true;

    private ActionListener _listener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            comboBoxValueChanged();
        }
    };

    protected void comboBoxValueChanged() {
        valueChanged();
    }

    public JComboBox createComboBox() {
        return ComponentFactory.createComboBox();
    }

    public ComboBoxModel createModel() {
        ComboBoxModel model;
        Slot slot = getSlot();
        if (slot == null) {
            Log.getLogger().warning("No slot");
            model = new DefaultComboBoxModel();
        } else {
            List values = new ArrayList();
            ValueType type = getCls().getTemplateSlotValueType(slot);
            if (type == ValueType.BOOLEAN) {
                values.add(Boolean.TRUE);
                values.add(Boolean.FALSE);
            } else if (type == ValueType.SYMBOL) {
                values.addAll(getCls().getTemplateSlotAllowedValues(slot));
            } else {
                Assert.fail("bad type");
            }
            if (_displayNullEntry) {
                values.add(0, NONE);
            }
            model = new DefaultComboBoxModel(values.toArray());
        }
        return model;
    }

    public JComboBox getComboBox() {
        return _comboBox;
    }

    public Object getComboBoxSelection() {
        return _comboBox.getSelectedItem();
    }

    public boolean getDisplayNullEntry() {
        return _displayNullEntry;
    }

    public Collection getValues() {
        Object value = _comboBox.getSelectedItem();
        if (value == NONE) {
            value = null;
        }
        return CollectionUtilities.createList(value);
    }

    public void initialize() {
        _comboBox = createComboBox();
        setComboBoxModel(createModel());
        add(new LabeledComponent(getLabel(), _comboBox));
        setPreferredColumns(1);
        setPreferredRows(1);
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean isSuitable;
        if (cls == null || slot == null) {
            isSuitable = false;
        } else {
            ValueType type = cls.getTemplateSlotValueType(slot);
            boolean typeOK = type == ValueType.SYMBOL || type == ValueType.BOOLEAN;
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            isSuitable = typeOK && !isMultiple;
        }
        return isSuitable;
    }

    public void setComboBoxModel(ComboBoxModel model) {
        _comboBox.removeActionListener(_listener);
        _comboBox.setModel(model);
        _comboBox.addActionListener(_listener);
    }

    protected void setComboBoxValue(String value) {
        _comboBox.removeActionListener(_listener);
        _comboBox.setSelectedItem(value);
        _comboBox.addActionListener(_listener);
    }

    public void setDisplayNullEntry(boolean b) {
        _displayNullEntry = b;
    }

    public void setEditable(boolean b) {
    	b = b && !isReadOnlyConfiguredWidget();
    	
        _comboBox.setEnabled(b);
    }

    public void setLabel(String text) {
        super.setLabel(text);
        LabeledComponent c = (LabeledComponent) getComponent(0);
        c.setHeaderLabel(text);
    }

    public void setRenderer(ListCellRenderer renderer) {
        _comboBox.setRenderer(renderer);
    }

    public void setValues(Collection values) {
        Object value = CollectionUtilities.getFirstItem(values);
        _comboBox.setSelectedItem(value);
    }
    
    @Override
    public WidgetConfigurationPanel createWidgetConfigurationPanel() {
    	WidgetConfigurationPanel confPanel = super.createWidgetConfigurationPanel();
    	
    	confPanel.addTab("Options", new ReadOnlyWidgetConfigurationPanel(this));
    	
    	return confPanel;
    }
}
