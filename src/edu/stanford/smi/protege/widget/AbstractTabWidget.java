package edu.stanford.smi.protege.widget;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * Base class for all TabWidgets. The interesting methods are all in
 * _AbstractWidget for the moment.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class AbstractTabWidget extends AbstractWidget implements TabWidget {
    private Icon _icon;
    private String _label;
    private Collection _splitPanes;
    private String _shortDescription;
    private JTree _clsTree;

    public JButton addMainWindowToolBarButton(Action action) {
        JButton button;
        JToolBar toolBar = getMainWindowToolBar();
        if (toolBar == null) {
            Log.warning("Cannot find window tool bar", this, "addMainWindowToolBarButton", action);
            button = null;
        } else {
            button = ComponentFactory.addToolBarButton(toolBar, action);
        }
        return button;
    }

    // Tabs don't store their labels in the property list: they are always set
    public void setLabel(String label) {
        _label = label;
    }

    public String getLabel() {
        return (_label == null) ? super.getLabel() : _label;
    }

    public static boolean isSuitable(Project p, Collection errors) {
        return true;
    }

    public boolean canClose() {
        return true;
    }

    public boolean canSave() {
        return true;
    }

    protected JSplitPane createLeftRightSplitPane(String locationPropertyName, int defaultLocation) {
        JSplitPane pane = ComponentFactory.createLeftRightSplitPane();
        setSplitPane(pane, locationPropertyName, defaultLocation);
        return pane;
    }

    protected JSplitPane createTopBottomSplitPane(String locationPropertyName, int defaultLocation) {
        JSplitPane pane = ComponentFactory.createTopBottomSplitPane();
        setSplitPane(pane, locationPropertyName, defaultLocation);
        return pane;
    }

    public Icon getIcon() {
        return _icon;
    }

    public JMenuBar getMainWindowMenuBar() {
        return ProjectManager.getProjectManager().getCurrentProjectMenuBar();
    }

    protected JToolBar getMainWindowToolBar() {
        return ProjectManager.getProjectManager().getCurrentProjectMainToolBar();
    }

    protected JToolBar getUserToolBar(String name) {
        return ProjectManager.getProjectManager().getUserToolBar(name);
    }

    protected void addUserToolBar(JToolBar toolbar) {
        ProjectManager.getProjectManager().addUserToolBar(toolbar);
    }

    protected void removeUserToolBar(JToolBar toolbar) {
        ProjectManager.getProjectManager().removeUserToolBar(toolbar);
    }

    public void removeMainWindowToolBarButton(JButton button) {
        JToolBar toolBar = getMainWindowToolBar();
        if (toolBar != null) {
            toolBar.remove(button);
        }
    }

    public void save() {
        saveSplitterLocations();
    }

    public void close() {
        // do nothing
    }

    public String getShortDescription() {
        return _shortDescription;
    }

    private void saveSplitterLocations() {
        if (_splitPanes != null) {
            Iterator i = _splitPanes.iterator();
            while (i.hasNext()) {
                JSplitPane pane = (JSplitPane) i.next();
                int location = pane.getDividerLocation();
                getPropertyList().setInteger(pane.getName(), location);
            }
        }
    }

    public void setIcon(Icon icon) {
        _icon = icon;
    }

    private void setSplitPane(JSplitPane pane, String name, int defaultLocation) {
        //        if (_splitPanes == null) {
        //            _splitPanes = new ArrayList();
        //        }
        //        pane.setName(name);
        //        _splitPanes.add(pane);
        //        Integer locationInteger = getPropertyList().getInteger(name);
        //        int location;
        //        if (locationInteger == null) {
        //            location = defaultLocation;
        //        } else {
        //            location = locationInteger.intValue();
        //        }
        //        pane.setDividerLocation(location);
    }

    public boolean configure() {
        ModalDialog.showMessageDialog(this, "No configuration options are available for this tab.");
        return true;
    }

    public void setup(WidgetDescriptor descriptor, Project project) {
        super.setup(descriptor, false, project);
    }

    public void setShortDescription(String description) {
        _shortDescription = description;
    }

    protected void setClsTree(JTree tree) {
        _clsTree = tree;
        _clsTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                notifySelectionListeners();
            }
        });
        if (getProject().isMultiUserClient()) {
            _clsTree.addTreeExpansionListener(new TreeExpansionListener() {
                public void treeCollapsed(TreeExpansionEvent event) {
                    // do nothing
                }

                public void treeExpanded(TreeExpansionEvent event) {
                    final WaitCursor cursor = new WaitCursor((JTree) event.getSource());
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            Toolkit.getDefaultToolkit().sync();
                            cursor.hide();
                        }
                    });
                }

            });
        }
    }

    public JTree getClsTree() {
        return _clsTree;
    }

    public Collection getSelection() {
        TreePath path = _clsTree.getSelectionPath();
        return (path == null) ? Collections.EMPTY_LIST : ComponentUtilities.getObjectPath(path);
    }

    public void synchronizeClsTree(Collection clses) {
        if (_clsTree != null) {
            ComponentUtilities.setSelectedObjectPath(_clsTree, clses);
        }
    }
}