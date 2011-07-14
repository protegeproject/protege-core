package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.*;

/**
 *  Action to change the applications "look and feel" as defined in the swing documentation.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class LookAndFeelAction extends ProjectAction {
    private static final long serialVersionUID = 5133274859688696709L;
    private String lookAndFeelClassName;
    
    public LookAndFeelAction(String lookAndFeelName, String lookAndFeelClassName) {
        super(lookAndFeelName);
        this.lookAndFeelClassName = lookAndFeelClassName;
        setEnabled(true);
    }
    
    public boolean isCurrent() {
        return UIManager.getLookAndFeel().getClass().getName().equals(lookAndFeelClassName);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().setLookAndFeel(lookAndFeelClassName);
    }
}
