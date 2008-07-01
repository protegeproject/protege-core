package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.BrowserSlotPattern;
import edu.stanford.smi.protege.util.ComponentFactory;

import javax.swing.*;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.awt.*;

public class MultiSlotPanel extends JPanel {
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
