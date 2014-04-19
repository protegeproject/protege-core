package edu.stanford.smi.protege.action;

import java.awt.event.*;

import com.jgoodies.looks.plastic.*;

import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class PlasticLookAndFeelAction extends LookAndFeelAction {
    private static final long serialVersionUID = -871244397545901767L;
    private static final String className = "com.jgoodies.looks.plastic.PlasticLookAndFeel";
    private static final String name = "Protege Default";

    public PlasticLookAndFeelAction() {
        super(name, className);
    }

    public void actionPerformed(ActionEvent event) {
        PlasticLookAndFeel.setPlasticTheme(new ProtegePlasticTheme());
        super.actionPerformed(event);
    }
}
