package edu.stanford.smi.protege.widget;

import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.ui.SubslotPane;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;

/**
 * The standard slot tab.  This tab displays slots and subslots in a tree and allows
 * the user to edit top-level slots.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SlotsTab extends AbstractTabWidget {
    private static final long serialVersionUID = 1274808722828634747L;
    private SubslotPane _slotsPanel;
    private SuperslotsPanel _superslotsPanel;
    private InstanceDisplay _slotDisplay;
    private JSplitPane _mainSplitter;
    private JSplitPane _clsesSplitter;

    private JComponent createMainSplitter() {
        _mainSplitter = createLeftRightSplitPane("SlotsTab.left_right", 250);
        _mainSplitter.setLeftComponent(createSlotsSplitter());
        _slotDisplay = (InstanceDisplay) createSlotDisplay();
        _mainSplitter.setRightComponent(_slotDisplay);
        return _mainSplitter;
    }

    private JComponent createSuperslotsPanel() {
        _superslotsPanel = new SuperslotsPanel(getProject());
        _superslotsPanel.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                Collection selection = _superslotsPanel.getSelection();
                if (selection.size() == 1) {
                    Slot slot = (Slot) CollectionUtilities.getFirstItem(selection);
                    _slotsPanel.setDisplayParent(slot);
                }
            }
        });
        return _superslotsPanel;
    }

    protected JComponent createSlotDisplay() {
        return new InstanceDisplay(getProject());
    }

    private JComponent createSlotsPanel() {
        _slotsPanel = new SubslotPane(getProject());
        _slotsPanel.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                transmitSelection();
            }
        });
        return _slotsPanel;
    }

    private JComponent createSlotsSplitter() {
        _clsesSplitter = createTopBottomSplitPane("SlotTab.left.top_bottom", 400);
        _clsesSplitter.setTopComponent(createSlotsPanel());
        _clsesSplitter.setBottomComponent(createSuperslotsPanel());
        return _clsesSplitter;
    }
    
    public LabeledComponent getLabeledComponent() {
        return _slotsPanel.getLabeledComponent();
    }

    public void initialize() {
        setIcon(Icons.getSlotIcon());
        setLabel(LocalizedText.getText(ResourceKey.SLOTS_VIEW_TITLE));
        add(createMainSplitter());
        transmitSelection();
        setClsTree(_slotsPanel.getDropComponent());
    }

    private void transmitSelection() {
        Slot selection = (Slot) CollectionUtilities.getFirstItem(_slotsPanel.getSelection());
        _superslotsPanel.setSlot(selection, _slotsPanel.getDisplayParent());
        _slotDisplay.setInstance(selection);
    }

}
