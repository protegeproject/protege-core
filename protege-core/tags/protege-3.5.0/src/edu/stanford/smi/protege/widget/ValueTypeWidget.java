package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Widget for setting the value type of a slot or a cls-slot pair.  This widget morphs when the user selects a new
 * type.  It allows the user to select either allowed-classes, allowed-parents, or allowed values (for type symbol).
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ValueTypeWidget extends AbstractSlotWidget {
    private static final long serialVersionUID = 7319731533086348647L;
    private ValuesComponent _valuesComponent;
    private JComboBox _typeComboBox;
    private ValueType _oldValue;
    private boolean _isEditable;

    private ActionListener _typeListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            ValueType newValue = (ValueType) _typeComboBox.getSelectedItem();
            if (newValue != _oldValue) {
                if (confirmChange()) {
                    updateWidget();
                    valueChanged();
                    _oldValue = newValue;
                } else {
                    setComboBoxValue(_oldValue);
                }
            }
        }

        private boolean confirmChange() {
            boolean result = true;
            if (hasInstanceWithValue()) {
                // Since we are in _typeComboBoxndler the popup window is still down.
                // It is ugly to leave it down when the dialog pops up.
                _typeComboBox.hidePopup();

                String text = "There may be instances which have values for this slot.\n"
                        + "Changing the type will cause these values to be removed.\n" + "\n"
                        + "Do you really want to make this change?";
                int response = ModalDialog.showMessageDialog(ValueTypeWidget.this, text, ModalDialog.MODE_YES_NO);
                result = response == ModalDialog.OPTION_YES;
            }
            return result;
        }

        private boolean hasInstanceWithValue() {
            Slot slot = (Slot) getInstance();
            return slot.hasValueAtSomeFrame();
        }
    };

    protected ComboBoxModel createModel() {
        List c = new ArrayList(ValueType.getValues());
        Collections.sort(c);
        return new DefaultComboBoxModel(c.toArray());
    }

    protected JComboBox getTypeComboBox() {
        return _typeComboBox;
    }

    public Collection getSelection() {
        return _valuesComponent.getSelection();
    }

    public Collection getValues() {
        Collection values = _valuesComponent.getValues();
        if (values == null) {
            ValueType type = (ValueType) _typeComboBox.getSelectedItem();
            values = ValueTypeConstraint.getValues(type);
        }
        return values;
    }

    public void initialize() {
        _typeComboBox = ComponentFactory.createComboBox();
        _typeComboBox.setModel(createModel());
        setLayout(new BorderLayout(10, 10));
        add(new LabeledComponent(getLabel(), _typeComboBox), BorderLayout.NORTH);
        _typeComboBox.addActionListener(_typeListener);
        setValuesComponent(new NullValuesComponent());
        _typeComboBox.setRenderer(new ValueTypeRenderer());
    }

    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return slot.getName().equals(Model.Slot.VALUE_TYPE);
    }

    private void setComboBoxValue(ValueType type) {
        _typeComboBox.removeActionListener(_typeListener);
        _typeComboBox.setSelectedItem(type);
        updateWidget();
        _typeComboBox.addActionListener(_typeListener);
        _oldValue = type;
    }

    public void setEditable(boolean b) {
        _isEditable = b;
        _typeComboBox.setEnabled(b && canChangeType());
        _valuesComponent.setEditable(b);
    }

    private boolean canChangeType() {
        return !isSlotAtCls() || _typeComboBox.getSelectedItem().equals(ValueType.ANY);
    }

    public void setValues(Collection values) {
        // Log.enter(this, "setValues", values);
        if (values.isEmpty()) {
            Log.getLogger().warning("empty values: " + getInstance() + " " + getSlot());
            setComboBoxValue(ValueType.ANY);
        } else {
            ValueType type = ValueTypeConstraint.getType(values);
            setComboBoxValue(type);
            _valuesComponent.setValues(values);
        }
    }

    private void setValuesComponent(ValuesComponent valueComponent) {
        if (_valuesComponent != null) {
            remove(_valuesComponent.getComponent());
            ComponentUtilities.dispose(_valuesComponent.getComponent());
        }
        _valuesComponent = valueComponent;
        _valuesComponent.setEditable(_isEditable);
        add(_valuesComponent.getComponent());
        revalidate();
    }

    private void updateWidget() {
        ValuesComponent valuesComponent;
        ValueType type = (ValueType) _typeComboBox.getSelectedItem();
        valuesComponent = createValuesComponent(type);
        setValuesComponent(valuesComponent);
    }

    protected ValuesComponent createValuesComponent(ValueType type) {
        if (equals(type, ValueType.ANY)) {
            return new NullValuesComponent();
        } else if (equals(type, ValueType.BOOLEAN)) {
            return new NullValuesComponent();
        } else if (equals(type, ValueType.CLS)) {
            return new ClsValuesComponent(getProject());
        } else if (equals(type, ValueType.FLOAT)) {
            return new NullValuesComponent();
        } else if (equals(type, ValueType.INSTANCE)) {
            return new InstanceValuesComponent(getProject());
        } else if (equals(type, ValueType.INTEGER)) {
            return new NullValuesComponent();
        } else if (equals(type, ValueType.STRING)) {
            return new NullValuesComponent();
        } else if (equals(type, ValueType.SYMBOL)) {
            return new SymbolValuesComponent();
        }
        Assert.fail("bad type: " + type);
        return null;
    }

    public String getLabel() {
        return localizeStandardLabel(super.getLabel(), "Value Type", ResourceKey.VALUE_TYPE_SLOT_WIDGET_LABEL);
    }
}
