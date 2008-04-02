package edu.stanford.smi.protege.action;

import java.awt.event.*;

import com.jgoodies.plaf.plastic.*;

import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class PlasticLookAndFeelAction extends LookAndFeelAction {
    private static final String className = "com.jgoodies.plaf.plastic.PlasticLookAndFeel";
    private static final String name = "Protege Default";
        
    public PlasticLookAndFeelAction() {
        super(name, className);
    }

    public void actionPerformed(ActionEvent event) {
        PlasticLookAndFeel.setMyCurrentTheme(new ProtegePlasticTheme());
        super.actionPerformed(event);
    }
}
