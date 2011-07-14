package edu.stanford.smi.protege.ui;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class ConfigureAction extends StandardAction {

    private static final long serialVersionUID = 5652284823215641602L;

    protected ConfigureAction() {
        super(ResourceKey.COMPONENT_MENU);
    }

    public void actionPerformed(ActionEvent event) {
        JPopupMenu menu = new JPopupMenu();
        loadPopupMenu(menu);
        JComponent c = (JComponent) event.getSource();
        menu.show(c, 0, c.getHeight());
    }

    public abstract void loadPopupMenu(JPopupMenu menu);
}
