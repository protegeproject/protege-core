package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.*;

/**
 * Action to bring a particular frame to the front and give it the focus.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class JFrameToFront extends AbstractAction {
    private static final long serialVersionUID = -8458643859431204852L;
    private static final int MAX_LENGTH = 25;
    private JFrame frame;

    public JFrameToFront(JFrame frame) {
        super(getMenuText(frame));
        this.frame = frame;
    }

    public void actionPerformed(ActionEvent event) {
        frame.toFront();
        frame.requestFocus();
    }

    private static String getMenuText(JFrame frame) {
        String menuText;
        String text = frame.getTitle();
        if (text.length() < MAX_LENGTH) {
            menuText = text;
        } else {
            menuText = text.substring(0, MAX_LENGTH - 3) + "...";
        }
        return menuText;
    }
}
