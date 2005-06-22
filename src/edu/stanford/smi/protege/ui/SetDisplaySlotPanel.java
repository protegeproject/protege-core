package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SetDisplaySlotPanel extends JComponent implements Disposable {
    private static final String MULTISLOT = "Multiple Slots";
    private JComboBox _displaySlotComboBox;
    private Cls _cls;
    private BrowserSlotPattern _currentPattern;

    private ActionListener _actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            if (_cls != null) {
                Object o = _displaySlotComboBox.getSelectedItem();
                if (MULTISLOT.equals(o)) {
                    setMultiSlotPattern();
                } else {
                    BrowserSlotPattern pattern = (BrowserSlotPattern) _displaySlotComboBox.getSelectedItem();
                    _cls.setDirectBrowserSlotPattern(pattern);
                }
            }
        }
    };

    private ClsListener _clsListener = new ClsAdapter() {
        public void directSuperclassAdded(ClsEvent event) {
            reload();
        }

        public void directSuperclassRemoved(ClsEvent event) {
            reload();
        }

        public void templateSlotAdded(ClsEvent event) {
            reload();
        }

        public void templateSlotRemoved(ClsEvent event) {
            reload();
        }
    };

    public SetDisplaySlotPanel() {
        createComponents();
        layoutComponents();
    }

    public void setCls(Cls cls) {
        if (_cls != null) {
            _cls.removeClsListener(_clsListener);
        }
        _cls = cls;
        if (_cls != null) {
            _cls.addClsListener(_clsListener);
        }
        _displaySlotComboBox.setRenderer(new DisplaySlotRenderer(cls));
        reload();
    }

    public void dispose() {
        if (_cls != null) {
            _cls.removeClsListener(_clsListener);
        }
    }

    private void layoutComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
        String displaySlotLabel = LocalizedText.getText(ResourceKey.FORM_EDITOR_DISPLAY_SLOT_LABEL);
        add(ComponentFactory.createSmallFontLabel(displaySlotLabel));
        add(_displaySlotComboBox);
        Dimension d = _displaySlotComboBox.getPreferredSize();
        d.width = 250;
        _displaySlotComboBox.setPreferredSize(d);
    }

    private void createComponents() {
        createDisplaySlotComboBox();
    }

    private void createDisplaySlotComboBox() {
        _displaySlotComboBox = ComponentFactory.createComboBox();
        _displaySlotComboBox.addActionListener(_actionListener);
    }

    private void reload() {
        _currentPattern = null;
        boolean shouldEnable = _cls != null;
        Object selection = (_cls == null) ? null : _cls.getBrowserSlotPattern();
        Collection values = new ArrayList();
        if (shouldEnable) {
            Collection slots = _cls.getVisibleTemplateSlots();
            Iterator i = slots.iterator();
            while (i.hasNext()) {
                Slot slot = (Slot) i.next();
                BrowserSlotPattern pattern = new BrowserSlotPattern(slot);
                values.add(pattern);
            }
            if (selection != null && !values.contains(selection)) {
                values.add(selection);
            }
            if (slots.size() > 0) {
                values.add(MULTISLOT);
            }
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(values.toArray());
        _displaySlotComboBox.removeActionListener(_actionListener);
        _displaySlotComboBox.setModel(model);
        _displaySlotComboBox.setSelectedItem(selection);
        _displaySlotComboBox.addActionListener(_actionListener);
        setEnabled(shouldEnable);
    }

    public void setEnabled(boolean b) {
        _displaySlotComboBox.setEnabled(b);
    }

    private void setMultiSlotPattern() {
        // we need to display the dialog later to allow the combo box to close.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                acquireMultiSlotPattern();
            }
        });
    }

    private void acquireMultiSlotPattern() {
        MultiSlotPanel panel = new MultiSlotPanel(_currentPattern, _cls);
        int rval = ModalDialog.showDialog(this, panel, "Multislot Display Pattern", ModalDialog.MODE_OK_CANCEL);
        if (rval == ModalDialog.OPTION_OK) {
            BrowserSlotPattern pattern = panel.getBrowserTextPattern();
            if (pattern != null) {
                _cls.setDirectBrowserSlotPattern(pattern);
                reload();
                _currentPattern = pattern;
                _displaySlotComboBox.setSelectedItem(pattern);
            }
        }
    }
}

class MultiSlotPanel extends JPanel {
    private Cls cls;
    private BrowserSlotPattern pattern;
    private List panels = new ArrayList();

    MultiSlotPanel(BrowserSlotPattern pattern, Cls cls) {
        this.cls = cls;
        this.pattern = pattern;
        createUI();
        loadUI();
    }

    private void createUI() {
        setLayout(new GridLayout(2, 10, 0, 4));
        add(ComponentFactory.createLabel("Set display slots and optional text:"));
        Collection slots = cls.getVisibleTemplateSlots();
        JPanel panel = new JPanel(new FlowLayout());
        for (int i = 0; i < 5; ++i) {
            panel.add(createTextPanel());
            panel.add(createSlotPanel(slots));
        }
        panel.add(createTextPanel());
        add(panel);
    }

    private void loadUI() {
        if (pattern != null) {
            Iterator j = panels.iterator();
            Iterator i = pattern.getElements().iterator();
            while (i.hasNext() && j.hasNext()) {
                Object o = i.next();
                Object panel = j.next();
                if (o instanceof String) {
                    if (!(panel instanceof JTextField)) {
                        panel = j.next();
                    }
                    JTextField field = (JTextField) panel;
                    field.setText((String) o);
                } else {
                    if (!(panel instanceof JComboBox)) {
                        panel = j.next();
                    }
                    JComboBox box = (JComboBox) panel;
                    box.setSelectedItem(o);
                }
            }
        }
    }

    private JComponent createTextPanel() {
        JTextField textField = ComponentFactory.createTextField();
        textField.setColumns(2);
        panels.add(textField);
        return textField;
    }

    private JComponent createSlotPanel(Collection slots) {
        JComboBox slotBox = ComponentFactory.createComboBox();
        slotBox.setRenderer(new FrameRenderer());
        List values = new ArrayList(slots);
        values.add(0, null);
        ComboBoxModel model = new DefaultComboBoxModel(values.toArray());
        slotBox.setModel(model);
        slotBox.setSelectedItem(null);
        panels.add(slotBox);
        return slotBox;
    }

    public BrowserSlotPattern getBrowserTextPattern() {
        List elements = new ArrayList();
        Iterator i = panels.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof JTextField) {
                JTextField textField = (JTextField) o;
                String text = textField.getText();
                if (text != null && text.length() > 0) {
                    elements.add(text);
                }
            } else {
                JComboBox box = (JComboBox) o;
                Object slot = box.getSelectedItem();
                if (slot != null) {
                    elements.add(slot);
                }
            }
        }
        return new BrowserSlotPattern(elements);
    }
}
