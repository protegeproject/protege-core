package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Descrease the font used by most of the widgets by one increment.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DecreaseFontSize extends FontAction {

    public DecreaseFontSize() {
        super(ResourceKey.DECREASE_FONT_SIZE);
    }

    public void actionPerformed(ActionEvent event) {
        changeSize(-2);
    }
}
