package edu.stanford.smi.protege.test;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import junit.framework.Assert;

import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class TestUtilities {

    public static AbstractButton getButton(Component parent, Icon icon) {
        AbstractButton iconButton = null;
        Iterator i = ComponentUtilities.getDescendentsOfClass(AbstractButton.class, parent).iterator();
        while (i.hasNext()) {
            AbstractButton button = (AbstractButton) i.next();
            if (icon.equals(button.getIcon())) {
                iconButton = button;
                break;
            }
        }
        return iconButton;
    }
    public static void pressButton(Component parent, Icon icon) {

        AbstractButton button = getButton(parent, icon);
        if (button == null) {
            Assert.fail("No such button: " + icon);
        } else {
            button.doClick();
            SystemUtilities.sleepMsec(500);
        }
    }

    public static Component selectTab(Component parent, final Icon icon) {
        Component component = null;
        JTabbedPane tabbedPane = (JTabbedPane) ComponentUtilities.getDescendentOfClass(JTabbedPane.class, parent);
        for (int i = 0; i < tabbedPane.getTabCount(); ++i) {
            Icon tabIcon = tabbedPane.getIconAt(i);
            if (icon.equals(tabIcon)) {
                component = tabbedPane.getComponentAt(i);
                tabbedPane.setSelectedIndex(i);
            }
        }
        return component;
    }

    public static Component getLeftComponent(Component parent) {
        JSplitPane splitPane = (JSplitPane) ComponentUtilities.getDescendentOfClass(JSplitPane.class, parent);
        return splitPane.getLeftComponent();
    }

    public static Component getRightComponent(Component parent) {
        JSplitPane splitPane = (JSplitPane) ComponentUtilities.getDescendentOfClass(JSplitPane.class, parent);
        return splitPane.getLeftComponent();
    }

    public static Component getTopComponent(Component parent) {
        JSplitPane splitPane = (JSplitPane) ComponentUtilities.getDescendentOfClass(JSplitPane.class, parent);
        return splitPane.getLeftComponent();
    }

    public static Component getBottomComponent(Component parent) {
        JSplitPane splitPane = (JSplitPane) ComponentUtilities.getDescendentOfClass(JSplitPane.class, parent);
        return splitPane.getLeftComponent();
    }

    public static JTree getTree(Component parent) {
        return (JTree) ComponentUtilities.getDescendentOfClass(JTree.class, parent);
    }

    public static InstanceDisplay getInstanceDisplay(Component parent) {
        return (InstanceDisplay) ComponentUtilities.getDescendentOfClass(InstanceDisplay.class, parent);
    }

    public static JTree getTreeOnTab(Component parent, Icon icon) {
        return getTree(getLeftComponent(selectTab(parent, icon)));
    }

    public static void setSelectionOnTree(Component parent, Icon icon, Object[] userObjects) {
        JTree tree = getTreeOnTab(parent, icon);
        ComponentUtilities.setSelectedObjectPath(tree, Arrays.asList(userObjects));
    }

    public static Object[] getSelectionOnTree(Component parent, Icon icon) {
        JTree tree = getTreeOnTab(parent, icon);
        TreePath path = tree.getSelectionPath();
        return ComponentUtilities.getObjectPath(path).toArray();
    }

    public static InstanceDisplay getInstanceDisplayOnTab(Component parent, Icon icon) {
        return getInstanceDisplay(getRightComponent(selectTab(parent, icon)));
    }

}
