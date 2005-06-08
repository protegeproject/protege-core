package edu.stanford.smi.protege.util;

import java.awt.*;

// import javax.swing.*;

/**
 * A wait cursor that can to enabled and disabled on command.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class WaitCursor {
    private boolean enabled = true;
    private Component rootComponent;
    private Cursor standardCursor;
    private static final Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

    public WaitCursor(Component c) {
        rootComponent = null; // SwingUtilities.getRoot(c);
        if (rootComponent != null) {
	        standardCursor = rootComponent.getCursor();
	        if (standardCursor == waitCursor) {
	            enabled = false;
	        }
	        show();
        }
    }

    public void hide() {
        if (rootComponent != null && enabled) {
            rootComponent.setCursor(standardCursor);
        }
    }

    public void show() {
        if (rootComponent != null && enabled) {
            rootComponent.setCursor(waitCursor);
        }
    }
}