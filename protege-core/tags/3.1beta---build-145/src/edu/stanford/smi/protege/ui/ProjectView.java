package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.*;

/**
 * The GUI view of a project.  This is basically the outer tabbed pane for the application.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProjectView extends JComponent {
    private Project _project;
    private JTabbedPane _tabbedPane;
    private Collection _currentClsPath;
    private Collection _detachedTabs = new HashSet();

    public ProjectView(Project project) {
        _project = project;
        setLayout(new BorderLayout());
        add(createTabbedPane(), BorderLayout.CENTER);
        project.getKnowledgeBase().setUndoEnabled(true);
    }

    public TabWidget addTab(WidgetDescriptor widgetDescriptor) {
        TabWidget widget = WidgetUtilities.createTabWidget(widgetDescriptor, _project);
        addTab(widget);
        return widget;
    }

    private void addTab(TabWidget widget) {
        addTab(widget, -1);
    }
    private void addTab(final TabWidget widget, int index) {
        JComponent component = (JComponent) widget;
        Icon icon = widget.getIcon();
        String title = widget.getLabel();
        String help = widget.getShortDescription();
        if (index < 0) {
            _tabbedPane.addTab(title, icon, component, help);
        } else {
            _tabbedPane.insertTab(title, icon, component, help, index);
        }
        widget.addSelectionListener(new SelectionListener() {
            public void selectionChanged(SelectionEvent event) {
                java.util.List list = new ArrayList(widget.getSelection());
                if (!list.isEmpty() && list.get(0) instanceof Collection) {
                    list.remove(0);
                }
                setCurrentClsPath(list);
            }
        });
    }

    public boolean attemptClose() {
        boolean canClose = true;
        Iterator i = getTabs().iterator();
        while (canClose && i.hasNext()) {
            TabWidget tab = (TabWidget) i.next();
            canClose = tab.canClose();
        }
        if (canClose) {
            close();
        }
        return canClose;
    }

    public boolean attemptSave() {
        boolean canSave = true;
        Iterator i = getTabs().iterator();
        while (canSave && i.hasNext()) {
            TabWidget tab = (TabWidget) i.next();
            canSave = tab.canSave();
        }
        if (canSave) {
            save();
        }
        return canSave;
    }

    public void close() {
        Iterator i = getTabs().iterator();
        while (i.hasNext()) {
            TabWidget tab = (TabWidget) i.next();
            tab.close();
        }
    }

    private JComponent createTabbedPane() {
        _tabbedPane = ComponentFactory.createTabbedPane(true);
        _tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (isAutosynchronizingClsTrees()) {
                    synchronizeClsTree();
                }
            }
            
        });
        Iterator i = _project.getTabWidgetDescriptors().iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = (WidgetDescriptor) i.next();
            if (d.isVisible()) {
                addTab(d);
            }
        }
        if (_tabbedPane.getTabCount() > 0) {
            _tabbedPane.setSelectedIndex(0);
        }
        setBorder(BorderFactory.createLoweredBevelBorder());
        return _tabbedPane;
    }

    public void dispose() {
    }

    public Project getProject() {
        return _project;
    }
    
    /**
     * @deprecated Use #getTabByClassName(String)
     */
    public TabWidget getTab(String className) {
        return getTabByClassName(className);
    }

    public TabWidget getTabByClassName(String className) {
        TabWidget result = null;
        Component[] components = _tabbedPane.getComponents();
        for (int i = 0; i < components.length; ++i) {
            Component c = components[i];
            if (c.getClass().getName().equals(className)) {
                result = (TabWidget) c;
                break;
            }
        }
        return result;
    }
    
    public Collection getTabs() {
        return Arrays.asList(_tabbedPane.getComponents());
    }
    
    public void setSelectedTab(TabWidget tab) {
        _tabbedPane.setSelectedComponent((Component) tab);
    }
    
    public TabWidget getSelectedTab() {
        return (TabWidget) _tabbedPane.getSelectedComponent();
    }
    
    public JTabbedPane getTabbedPane() {
        return _tabbedPane;
    }

    public void reload(boolean regenerate) {
        if (regenerate) {
            reloadAll();
        } else {
            reloadPartial();
        }
    }

    private void reloadPartial() {
        closeDetachedDisabledTabs();
        synchronizeTabbedPane();
    }

    private void synchronizeTabbedPane() {
        removeDisabledTabs();
        addEnabledTabs();
        reorderTabs();
    }

    private void removeDisabledTabs() {
        Iterator i = getTabs().iterator();
        while (i.hasNext()) {
            TabWidget tab = (TabWidget) i.next();
            if (!isEnabled(tab)) {
                _tabbedPane.remove((Component) tab);
                tab.dispose();
            }
        }
    }

    private void addEnabledTabs() {
        Iterator i = _project.getTabWidgetDescriptors().iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = (WidgetDescriptor) i.next();
            if (d.isVisible()) {
                String className = d.getWidgetClassName();
                if (getTab(className) == null) {
                    addTab(d);
                }
            }
        }
    }

    private void reorderTabs() {
        int index = 0;
        Iterator i = _project.getTabWidgetDescriptors().iterator();
        while (i.hasNext()) {
            WidgetDescriptor d = (WidgetDescriptor) i.next();
            if (d.isVisible()) {
                int currentIndex = getTabIndex(d);
                if (currentIndex != index) {
                    TabWidget tab = (TabWidget) _tabbedPane.getComponent(currentIndex);
                    addTab(tab, index);
                }
                ++index;
            }
        }
    }

    private int getTabIndex(WidgetDescriptor d) {
        return _tabbedPane.indexOfComponent((Component) getTab(d.getWidgetClassName()));
    }

    public void reloadAll() {
        closeDetachedTabs();
        if (_tabbedPane != null) {
            ComponentUtilities.dispose(_tabbedPane);
        }
        removeAll();
        _project.clearCachedWidgets();
        add(createTabbedPane());
        revalidate();
        repaint();
    }

    private void closeDetachedTabs() {
        Iterator i = _detachedTabs.iterator();
        while (i.hasNext()) {
            TabWidget tab = (TabWidget) i.next();
            i.remove();
            closeDetachedTab(tab);
        }
    }

    private void closeDetachedTab(TabWidget tab) {
        Component c = (Component) tab;
        JFrame frame = (JFrame) SwingUtilities.getRoot(c);
        frame.dispose();
    }

    private void closeDetachedDisabledTabs() {
        Iterator i = _detachedTabs.iterator();
        while (i.hasNext()) {
            TabWidget tab = (TabWidget) i.next();
            if (!isEnabled(tab)) {
                i.remove();
                closeDetachedTab(tab);
            }
        }
    }

    private boolean isEnabled(TabWidget tab) {
        return tab.getDescriptor().isVisible();
    }

    private void save() {
        Iterator i = getTabs().iterator();
        while (i.hasNext()) {
            TabWidget tab = (TabWidget) i.next();
            tab.save();
        }
    }

    public String toString() {
        return "ProjectView";
    }
    
    public void closeCurrentView() {
        int index = _tabbedPane.getSelectedIndex();
        Component c = _tabbedPane.getSelectedComponent();
        _tabbedPane.removeTabAt(index);
        TabWidget widget = (TabWidget) c;
        widget.getDescriptor().setVisible(false);
        ComponentUtilities.dispose(c);
    }

    public void detachCurrentView() {
        JComponent c = (JComponent) _tabbedPane.getSelectedComponent();
        int index = _tabbedPane.getSelectedIndex();
        _tabbedPane.removeTabAt(index);
        final JFrame frame = ComponentFactory.createFrame();
        c.setPreferredSize(c.getSize());
        frame.getContentPane().add(c);
        frame.pack();
        String title = ((TabWidget) c).getLabel() + " Tab - " + Text.getProgramName();
        frame.setTitle(title);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                Component tab = frame.getContentPane().getComponent(0);
                reattachTab(tab);
                frame.setVisible(false);
                frame.dispose();
            }
        });
        frame.setVisible(true);
        _detachedTabs.add(c);
    }

    private void reattachTab(Component c) {
        int index = getInsertionPoint(c);
        addTab((TabWidget) c, index);
        _tabbedPane.setSelectedIndex(index);
        _detachedTabs.remove(c);
    }

    private int getInsertionPoint(Component c) {
        String classNameToMatch = c.getClass().getName();
        int insertionPoint = 0;
        Iterator i = _project.getTabWidgetDescriptors().iterator();
        while (i.hasNext() && insertionPoint < _tabbedPane.getComponentCount()) {
            WidgetDescriptor desc = (WidgetDescriptor) i.next();
            String className = desc.getWidgetClassName();
            if (className.equals(classNameToMatch)) {
                break;
            }
            if (className.equals(_tabbedPane.getComponentAt(insertionPoint).getClass().getName())) {
                ++insertionPoint;
            }
        }
        return insertionPoint;
    }

    public void setAutosynchronizeClsTrees(boolean b) {
        ApplicationProperties.setAutosynchronizingClsTrees(b);
        synchronizeClsTree();
    }
    
    public boolean isAutosynchronizingClsTrees() {
        return ApplicationProperties.isAutosynchronizingClsTrees();
    }

    private void setCurrentClsPath(Collection c) {
        _currentClsPath = c;
    }
    
    public void synchronizeClsTree() {
        if (_currentClsPath != null) {
            TabWidget currentWidget = (TabWidget) _tabbedPane.getSelectedComponent();
            if (currentWidget != null) {
                currentWidget.synchronizeClsTree(_currentClsPath);
            }
        }
    }
}
