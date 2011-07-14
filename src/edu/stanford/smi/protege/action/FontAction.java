package edu.stanford.smi.protege.action;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 *  Base class for any action to change the application font sizes.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class FontAction extends StandardAction {

    private static final long serialVersionUID = -1975048690489932340L;

    protected FontAction(ResourceKey text) {
        super(text);
    }

    protected static void changeSize(int delta) {
        changeSize("Tree.font", delta);
        changeSize("Label.font", delta);
        changeSize("Table.font", delta);
        changeSize("Button.font", delta);
        changeSize("MenuBar.font", delta);
        changeSize("Menu.font", delta);
        changeSize("MenuItem.font", delta);
        changeSize("PopupMenu.font", delta);
        changeSize("RadioButtonMenuItem.font", delta);
        changeSize("CheckBoxMenuItem.font", delta);
        changeSize("List.font", delta);
        changeSize("TextField.font", delta);
        changeSize("TextArea.font", delta);
        changeSize("CheckBox.font", delta);
        changeSize("TableHeader.font", delta);
        changeSize("TabbedPane.font", delta);
        changeSize("RadioButton.font", delta);
        changeSize("ToolTip.font", delta);
        changeSize("OptionPane.font", delta);

        ProjectManager.getProjectManager().reloadUI(true);
    }

    private static void changeSize(String key, int delta) {
        Font oldFont = UIManager.getFont(key);
        Font newFont = new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() + delta);
        UIManager.put(key, newFont);
    }
}
