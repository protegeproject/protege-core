package edu.stanford.smi.protege.util;

import java.awt.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class StandardAction extends AbstractAction {
    private static final long serialVersionUID = 3832046476795272130L;
    public static final String DISABLED_ICON = "DisabledSmallIcon";

    protected StandardAction(String name) {
        super(name);
    }

    protected StandardAction(String name, Icon icon) {
        super(name, icon);
    }

    protected StandardAction(ResourceKey key) {
        this(key, false);
    }

    protected StandardAction(ResourceKey key, boolean useLargeIcons) {
        initialize(this, key, useLargeIcons);
    }

    public void substituteIntoName(String text) {
        String name = getName();
        String newName = StringUtilities.replace(name, "{0}", text);
        setName(newName);
    }

    public static void initialize(Action action, ResourceKey key, boolean useLargeIcons) {
        setName(action, LocalizedText.getText(key));
        // setIcon(action, Icons.lookupActionIcon(key, useLargeIcons, false));
        setIcon(action, Icons.lookupActionIcon(key, false, false));
        // setDisabledIcon(action, Icons.lookupActionIcon(key, useLargeIcons, true));
        setShortcut(action, LocalizedText.getShortcut(key));
        setMnemonic(action, LocalizedText.getMnemonic(key));
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public String getName() {
        return getName(this);
    }

    public static String getName(Action action) {
        return (String) action.getValue(NAME);
    }

    public void setName(String text) {
        setName(this, text);
    }

    public void setDescription(String text) {
        setDescription(this, text);
    }

    public static void setName(Action action, String text) {
        action.putValue(NAME, text);
    }

    public static void setDescription(Action action, String text) {
        action.putValue(SHORT_DESCRIPTION, text);
    }

    public Icon getIcon() {
        return (Icon) getValue(SMALL_ICON);
    }

    public void setIcon(Icon icon) {
        setIcon(this, icon);
    }

    public static void setIcon(Action action, Icon icon) {
        action.putValue(SMALL_ICON, icon);
    }

    public void setDisabledIcon(Icon icon) {
        setDisabledIcon(this, icon);
    }

    public static void setDisabledIcon(Action action, Icon icon) {
        action.putValue(DISABLED_ICON, icon);
    }

    public void setShortcut(int c) {
        setShortcut(this, c);
    }

    public static void setShortcut(Action action, int c) {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        KeyStroke stroke = (c == 0) ? null : KeyStroke.getKeyStroke(c, mask);
        action.putValue(ACCELERATOR_KEY, stroke);
    }

    public void setMnemonic(int c) {
        setMnemonic(this, c);
    }

    public static void setMnemonic(Action action, int c) {
        Integer value = (c == 0) ? null : new Integer(c);
        action.putValue(MNEMONIC_KEY, value);
    }
}