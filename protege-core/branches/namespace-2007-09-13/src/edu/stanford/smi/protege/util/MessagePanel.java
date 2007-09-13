package edu.stanford.smi.protege.util;

import javax.swing.*;

/**
 * Displays a possibly multiline message.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class MessagePanel extends JPanel {

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
