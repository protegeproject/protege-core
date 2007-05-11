package edu.stanford.smi.protege.util;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;

import com.jgoodies.looks.plastic.theme.*;

import edu.stanford.smi.protege.resource.*;

/**
 *  @author Ray Fergerson
 *  
 */
public class ProtegePlasticTheme extends ExperienceBlue {

    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);
        Object[] uiDefaults = { 
        		"Tree.expandedIcon", Icons.getHierarchyExpandedIcon(), 
        		"Tree.collapsedIcon", Icons.getHierarchyCollapsedIcon(), 
        		"Table.selectionForeground", getMenuItemSelectedForeground(),
                "Table.selectionBackground", getMenuItemSelectedBackground(), 
                "List.selectionForeground", getMenuItemSelectedForeground(), 
                "List.selectionBackground", getMenuItemSelectedBackground(),
                "Tree.selectionForeground", getMenuItemSelectedForeground(), 
                "Tree.selectionBackground", getMenuItemSelectedBackground(), 
        };
        table.putDefaults(uiDefaults);
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