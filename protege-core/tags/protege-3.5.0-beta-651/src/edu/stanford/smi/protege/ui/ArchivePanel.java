package edu.stanford.smi.protege.ui;

import java.awt.*;
import javax.swing.*;

import edu.stanford.smi.protege.util.*;
/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ArchivePanel extends JPanel {
    private static final long serialVersionUID = -3503256676413359906L;
    private static boolean _displayPanel = true;
    private JCheckBox _displayPanelCheckBox;
    private JTextArea _comment;
    
    public ArchivePanel() {
        super(new BorderLayout());
        add(createCommentPanel(), BorderLayout.CENTER);
        add(createOptionPanel(), BorderLayout.SOUTH);
    }
    
    public static boolean displayPanel() {
        return _displayPanel;
    }
    public String getComment() {
        _displayPanel = _displayPanelCheckBox.isSelected();
        return _comment.getText();
    }
    private JComponent createCommentPanel() {
        _comment = ComponentFactory.createTextArea();
        _comment.setPreferredSize(new Dimension(300, 150));
        return new LabeledComponent("Comment", new JScrollPane(_comment));
    }
    private JComponent createOptionPanel() {
        _displayPanelCheckBox = new JCheckBox("Display this dialog");
        _displayPanelCheckBox.setSelected(_displayPanel);
        return _displayPanelCheckBox;
    }
}
