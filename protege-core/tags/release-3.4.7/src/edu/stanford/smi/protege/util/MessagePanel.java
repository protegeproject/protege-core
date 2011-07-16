package edu.stanford.smi.protege.util;

import javax.swing.*;

/**
 * Displays a possibly multiline message.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MessagePanel extends JPanel {

    private static final long serialVersionUID = -1418928771375394463L;

    public MessagePanel(String text) {
        setLayout(new java.awt.BorderLayout());
        JTextArea area = new JTextArea(text);

        // configure the text area to look like a label except that it can handle carriage returns
        JLabel label = ComponentFactory.createLabel();
        area.setEditable(false);
        area.setBackground(label.getBackground());
        area.setForeground(label.getForeground());
        area.setFont(label.getFont());

        add(area);
    }
}
