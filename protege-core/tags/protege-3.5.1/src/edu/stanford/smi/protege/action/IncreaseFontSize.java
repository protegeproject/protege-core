package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Action to increase the application font size by 1 unit.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class IncreaseFontSize extends FontAction {

	private static final long serialVersionUID = -7714875071007897094L;

    public IncreaseFontSize() {
		super(ResourceKey.INCREASE_FONT_SIZE);
	}

	public void actionPerformed(ActionEvent event) {
		changeSize(+2);
	}
}
