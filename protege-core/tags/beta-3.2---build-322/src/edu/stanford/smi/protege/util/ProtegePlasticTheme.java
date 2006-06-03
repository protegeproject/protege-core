package edu.stanford.smi.protege.util;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;

import com.jgoodies.looks.plastic.theme.*;

import edu.stanford.smi.protege.resource.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProtegePlasticTheme extends ExperienceBlue {
    private static final int DEFAULT_FONT_SIZE = 11;

    private static final Font replacementFont = new Font("Dialog", Font.PLAIN, DEFAULT_FONT_SIZE);

    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);
        Object[] uiDefaults = { "Tree.expandedIcon", Icons.getHierarchyExpandedIcon(), "Tree.collapsedIcon",
                Icons.getHierarchyCollapsedIcon(), "Table.selectionForeground", getMenuItemSelectedForeground(),
                "Table.selectionBackground", getMenuItemSelectedBackground(), "List.selectionForeground",
                getMenuItemSelectedForeground(), "List.selectionBackground", getMenuItemSelectedBackground(),
                "Tree.selectionForeground", getMenuItemSelectedForeground(), "Tree.selectionBackground",
                getMenuItemSelectedBackground(), };
        table.putDefaults(uiDefaults);
    }

    protected Font getFont0() {
        return getFont0(DEFAULT_FONT_SIZE);
    }

    protected Font getFont0(int size) {
        Font font = super.getFont0(size);
        if (font.getName().startsWith("Tahoma")) {
            font = replacementFont.deriveFont((float) (size));
        }
        return font;
    }

    public ColorUIResource getMenuItemSelectedBackground() {
        return getPrimary3();
    }

    public ColorUIResource getMenuItemSelectedForeground() {
        return new ColorUIResource(Color.BLACK);
    }

    public ColorUIResource getMenuSelectedBackground() {
        return getMenuItemSelectedBackground();
    }

    public ColorUIResource getMenuSelectedForeground() {
        return getMenuItemSelectedForeground();
    }
}