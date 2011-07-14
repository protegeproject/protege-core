package edu.stanford.smi.protege.ui;

import javax.swing.*;

import edu.stanford.smi.protege.action.*;
import edu.stanford.smi.protege.util.*;

/**
 * The main toolbar for the application.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProjectToolBar extends JToolBar {

    private static final long serialVersionUID = -252712597962765189L;

    public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setRollover(true);
        setFloatable(false);
        setBorderPainted(false);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 0));
        setAlignmentX(0.5f);
    }

    public ProjectToolBar() {
        addButton(new CreateProject(true));
        addButton(new OpenProject(true));
        addButton(new SaveProject(true));
        addSeparator();

        addButton(new Cut(true));
        addButton(new Copy(true));
        addButton(new Paste(true));
        addButton(new Clear(true));
        addSeparator();

        addButton(new ArchiveProject(true));
        addButton(new RevertProject(true));
        addSeparator();

        addButton(new UndoAction(true));
        addButton(new RedoAction(true));
        addSeparator();
        /*
         * addButton(new DefaultAction("Find", Icons.getFindIcon())); addButton(new DefaultAction("Find Previous",
         * Icons.getFindPreviousIcon())); addButton(new DefaultAction("Find Next", Icons.getFindNextIcon()));
         * addSeparator();
         */
        // addButton(new CascadeWindows(true));
        // addButton(new CloseAllWindows(true));
    }

    public void addButton(Action action) {
        JButton button = ComponentFactory.addLargeToolBarButton(this, action);
        button.setFocusable(false);
    }

    public void addToggleButton(Action action) {
        ComponentFactory.addLargeToggleToolBarButton(this, action);
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }
}