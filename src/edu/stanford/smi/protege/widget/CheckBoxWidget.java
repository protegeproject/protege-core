package edu.stanford.smi.protege.widget;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * A check box slot widget for use with boolean slots.  This is one of the few widgets to not use a LabeledComponent
 * for layout (it just looks too weird).  One problem with using a check box for booleans is that there is no convenient
 * way to indicate that the value is not set.  Thus we adopt the convention that "not set" is displayed the
 * same as false (unchecked). 
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class CheckBoxWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = -2605391749596657296L;
    private JCheckBox _checkBox;

    public JCheckBox createCheckBox() {
        JCheckBox checkBox = ComponentFactory.createCheckBox();
        checkBox.setForeground(UIManager.getColor("Label.foreground"));
        return checkBox;
    }

    protected JCheckBox getCheckBox() {
        return _checkBox;
    }

    public Collection getValues() {
        boolean checked = _checkBox.isSelected();
        return CollectionUtilities.createCollection(checked ? Boolean.TRUE : Boolean.FALSE);
    }

    public void initialize() {
        _checkBox = createCheckBox();
        _checkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                valueChanged();
            }
        });
        _checkBox.setText(getLabel());
        add(_checkBox);
        setPreferredColumns(2);
        setPreferredRows(1);
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        boolean isSuitable;
        if (cls == null || slot == null) {
            isSuitable = false;
        } else {
            ValueType type = cls.getTemplateSlotValueType(slot);
            boolean typeOK = type == ValueType.BOOLEAN;
            boolean isMultiple = cls.getTemplateSlotAllowsMultipleValues(slot);
            isSuitable = typeOK && !isMultiple;
        }
        return isSuitable;
    }

    public void setEditable(boolean b) {
    	b = b && !isReadOnlyConfiguredWidget();
        _checkBox.setEnabled(b);
    }

    public void setLabel(String text) {
        super.setLabel(text);
        _checkBox.setText(text);
    }

    public void setValues(Collection values) {
        Boolean b = (Boolean) CollectionUtilities.getFirstItem(values);
        boolean checked = (b == null) ? false : b.booleanValue();
        _checkBox.setSelected(checked);
    }
    
    @Override
    public WidgetConfigurationPanel createWidgetConfigurationPanel() {
    	WidgetConfigurationPanel confPanel = super.createWidgetConfigurationPanel();
    	
    	confPanel.addTab("Options", new ReadOnlyWidgetConfigurationPanel(this));
    	
    	return confPanel;
    }
}
