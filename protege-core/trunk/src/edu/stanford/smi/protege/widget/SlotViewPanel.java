package edu.stanford.smi.protege.widget;

import java.awt.*;

import javax.swing.*;

/**
 * Message panel allowing the user to select either the "top-level" slot or the slot at a class for viewing.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
class SlotViewPanel extends JComponent {
    private static final long serialVersionUID = -2376860357479232073L;
    private static boolean _viewTopLevelSlotValue = true;
    private JRadioButton _topLevelRadioButton;
    private JRadioButton _slotAtClassRadioButton;

    public SlotViewPanel() {
        setLayout(new GridLayout(2, 1));
        _topLevelRadioButton = new JRadioButton("View top-level slot");
        _slotAtClassRadioButton = new JRadioButton("View slot at class");
        add(_topLevelRadioButton);
        add(_slotAtClassRadioButton);
        if (_viewTopLevelSlotValue) {
            _topLevelRadioButton.setSelected(true);
        } else {
            _slotAtClassRadioButton.setSelected(true);
        }
        ButtonGroup group = new ButtonGroup();
        group.add(_topLevelRadioButton);
        group.add(_slotAtClassRadioButton);
    }

    public boolean viewTopLevelSlot() {
        _viewTopLevelSlotValue = _topLevelRadioButton.isSelected();
        return _viewTopLevelSlotValue;
    }
}
