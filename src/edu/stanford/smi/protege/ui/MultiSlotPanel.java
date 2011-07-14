package edu.stanford.smi.protege.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.stanford.smi.protege.model.BrowserSlotPattern;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.ComponentFactory;

public class MultiSlotPanel extends JPanel {
    private static final long serialVersionUID = -2423974591715847895L;
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
    	this.setLayout(new GridBagLayout());
    	GridBagConstraints c = new GridBagConstraints();
        List<Slot> slots = new ArrayList<Slot>(cls.getVisibleTemplateSlots());
        //add :NAME slot if not already there
        Slot nameSlot = cls.getKnowledgeBase().getNameSlot();
        if (!slots.contains(nameSlot)) {
        	slots.add(nameSlot);
        }

        //sort
        Collections.sort(slots, new FrameComparator<Slot>());

    	// label
        c.gridx = 0;
    	c.gridy = 0;
    	c.gridwidth = 5;
    	c.insets = new Insets(2, 2, 8, 2);
    	c.anchor = GridBagConstraints.FIRST_LINE_START;
    	add(ComponentFactory.createLabel("Set display slots and optional text:"), c);

    	// first row
    	c.gridx = 0;
    	c.gridy = 1;
    	c.gridwidth = 1;
    	c.fill = GridBagConstraints.HORIZONTAL;
    	c.insets = new Insets(2, 2, 2, 2);
    	c.weightx = 0.5;
    	add(createTextPanel(), c);

    	// all other rows
    	for (int i=1; i<=4; i++) {
    		c.gridy = i;
    		addComponentRow(c, slots);
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

    private void addComponentRow(GridBagConstraints c, Collection slots) {
    	c.gridx = 1;
    	c.weightx = 1.0;
    	add(createSlotPanel(slots), c);

    	c.gridx = 2;
    	c.weightx = 0.5;
    	add(createTextPanel(), c);

    	c.gridx = 3;
    	c.weightx = 1.0;
    	add(createSlotPanel(slots), c);

    	c.gridx = 4;
    	c.weightx = 0.5;
    	add(createTextPanel(), c);
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
