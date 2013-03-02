package edu.stanford.smi.protege.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import edu.stanford.smi.protege.event.ClsAdapter;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.model.BrowserSlotPattern;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.ModalDialog;

/**
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SetDisplaySlotPanel extends JComponent implements Disposable {
    private static final long serialVersionUID = -5802235885558570599L;
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
        @Override
		public void directSuperclassAdded(ClsEvent event) {
            reload();
        }

        @Override
		public void directSuperclassRemoved(ClsEvent event) {
            reload();
        }

        @Override
		public void templateSlotAdded(ClsEvent event) {
            reload();
        }

        @Override
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
        Object selection = _cls == null ? null : _cls.getBrowserSlotPattern();
        Collection values = new ArrayList();
        if (shouldEnable) {
            Collection slots = _cls.getVisibleTemplateSlots();

            //add :NAME slot, if not already there
            Slot nameSlot = _cls.getKnowledgeBase().getNameSlot();
            if (!slots.contains(nameSlot)) {
            	slots.add(nameSlot);
            }

            Collections.sort((List) slots, new FrameComparator());

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

    @Override
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
