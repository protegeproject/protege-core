package edu.stanford.smi.protege.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.ListenerCollection;
import edu.stanford.smi.protege.util.ListenerList;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProjectViewDispatcher;
import edu.stanford.smi.protege.util.ProjectViewEvent;
import edu.stanford.smi.protege.util.ProjectViewListener;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protege.widget.WidgetUtilities;

/**
 * The GUI view of a project.  This is basically the outer tabbed pane for the application.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */

interface TabbedPaneInterface {
    void addChangeListener(ChangeListener listener);

    Component[] getComponents();

    int getComponentCount();

    void setSelectedComponent(Component component);

    Component getSelectedComponent();

    void remove(Component component);

    void removeTabAt(int index);

    int indexOfComponent(Component component);

    int getSelectedIndex();

    void setSelectedIndex(int index);

    Component getComponent(int i);

    void addTab(String title, Icon icon, Component c, String helpText);

    void insertTab(String title, Icon icon, Component c, String helpText, int index);
}

class MyCardPanel extends JPanel implements TabbedPaneInterface {
    private CardLayout layout = new CardLayout();
    private ChangeListener changeListener;

    MyCardPanel() {
        setLayout(layout);
    }

    public void addChangeListener(ChangeListener listener) {
        this.changeListener = listener;
    }

    public void setSelectedComponent(Component component) {
        layout.show(this, component.getName());
    }

    public Component getSelectedComponent() {
        Component component = null;
        for (int i = 0; i < getComponentCount(); ++i) {
            Component child = getComponent(i);
            if (child.isVisible()) {
                component = child;
                break;
            }
        }
        return component;
    }

    public void removeTabAt(int index) {
        remove(index);
    }

    public int indexOfComponent(Component component) {
        int index = -1;
        for (int i = 0; i < getComponentCount(); ++i) {
            Component child = getComponent(i);
            if (child == component) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getSelectedIndex() {
        return indexOfComponent(getSelectedComponent());
    }

    public void setSelectedIndex(int index) {
        setSelectedComponent(getComponent(index));
        changeListener.stateChanged(new ChangeEvent(this));
    }

    public void addTab(String title, Icon icon, Component c, String helpText) {
        c.setName(title);
        add(c, title);
    }

    public void insertTab(String title, Icon icon, Component c, String helpText, int index) {
        c.setName(title);
        add(c, title, index);
    }
}

class MyJTabbedPane extends JTabbedPane implements TabbedPaneInterface {
}

public class ProjectView extends JComponent {
    static private Logger log = Log.getLogger(ProjectView.class);
    
    private ListenerCollection projectViewListeners = new ListenerList(new ProjectViewDispatcher());
	
    private Project _project;
    private TabbedPaneInterface _viewHolder;
    private Collection _currentClsPath;
    private Collection _currentInstances;
    private Collection _detachedTabs = new HashSet();
    private Thread busyFlagThread = null;

    public ProjectView(Project project) {
        if (log.isLoggable(Level.FINE)) {
          // Add a listener for debug purposes only... if debugging is turned on.
          addProjectViewListener(new ProjectViewListener() {
            {
              if (log.isLoggable(Level.FINE)) {
                log.fine("Constructing ProjectViewListener");
              }
            }

            public void tabAdded(ProjectViewEvent event) {
              if (log.isLoggable(Level.FINE)) {
                log.fine("Tab added event found " + event + " with widget " + event.getWidget());
              }
            }

            public void saved(ProjectViewEvent event) {
            }

            public void closed(ProjectViewEvent event) {
            }
            
          });
        }
        _project = project;
        setLayout(new BorderLayout());
        // add(createTabbedPane(), BorderLayout.CENTER); what does this change do? (bug fix?)
        add(BorderLayout.CENTER, createTabbedPane());
        project.getKnowledgeBase().setUndoEnabled(project.isUndoOptionEnabled());
        if (project.isMultiUserClient()) {
          startBusyFlagThread();
        }
    }
    
    public void startBusyFlagThread() {
      if (busyFlagThread == null) {
        busyFlagThread = 
          new Thread("Thread for checking how busy the client is") {
          public void run() {
            while (true) {
              if (RemoteClientFrameStore.isBusy()) {
                ProjectManager.getProjectManager().getServerActivityMonitorButton().setBackground(Color.RED);                   
              }
              else {
                ProjectManager.getProjectManager().getServerActivityMonitorButton().setBackground(Color.WHITE);                 
              }
              try {
                Thread.sleep(300);
              } catch (InterruptedException e) {
                Log.emptyCatchBlock(e);
              }
            }
          }
        };
        busyFlagThread.start();
      }
    }

    public TabWidget addTab(WidgetDescriptor widgetDescriptor) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Adding tab " + widgetDescriptor);
        }
        TabWidget widget = WidgetUtilities.createTabWidget(widgetDescriptor, _project);
        addTab(widget);
        projectViewListeners.postEvent(this, 
        		                       ProjectViewEvent.Type.addTab.ordinal(), 
        		                       widget);
        return widget;
    }

    private void addTab(TabWidget widget) {
        addTab(widget, -1);
    }

    private void addTab(String title, Icon icon, JComponent component, String helpText, int index) {
        if (index < 0) {
            _viewHolder.addTab(title, icon, component, helpText);
        } else {
            _viewHolder.insertTab(title, icon, component, helpText, index);
        }
    }

    private boolean isSuitable(TabWidget widget) {
        String className = widget.getClass().getName();
        return WidgetUtilities.isSuitableTab(className, _project, new ArrayList());
    }

    private void addTab(final TabWidget widget, int index) {
        if (isSuitable(widget)) {
            JComponent component = (JComponent) widget;
            Icon icon = widget.getIcon();
            String title = widget.getLabel();
            String help = widget.getShortDescription();
            addTab(title, icon, component, help, index);
            widget.addSelectionListener(new SelectionListener() {
                public void selectionChanged(SelectionEvent event) {
                    List list = new ArrayList(widget.getSelection());
                    if (!list.isEmpty() && list.get(0) instanceof Collection) {
                        list.remove(0);
                    }
                    setCurrentClsPath(list);
                    setCurrentInstances(widget.getSelectedInstances());
                }
            });
        }
    }

    public boolean canClose() {
        boolean canClose = true;
        Iterator i = getTabs().iterator();
        while (canClose && i.hasNext()) {
            TabWidget tab = (TabWidget) i.next();
            canClose = tab.canClose();
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
        projectViewListeners.postEvent(this, ProjectViewEvent.Type.save.ordinal());
        return canSave;
    }

    public void close() {
        Iterator i = getTabs().iterator();
        while (i.hasNext()) {
            TabWidget tab = (TabWidget) i.next();
            tab.close();
        }
        _project = null;
        projectViewListeners.postEvent(this, ProjectViewEvent.Type.close.ordinal());
    }

    private JComponent createTabbedPane() {
        if (SystemUtilities.showAlphaFeatures()) {
            _viewHolder = new MyCardPanel();
        } else {
            _viewHolder = new MyJTabbedPane();
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Added view holder " + _viewHolder + " for project " + this);
        }
        _viewHolder.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("project view tabbed pane received change event " + e);
                }
                if (isAutosynchronizingClsTrees()) {
                    synchronizeClsTree();
                    synchronizeInstances();
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
        if (_viewHolder.getComponentCount() > 0)
        	_viewHolder.setSelectedIndex(0);
        setBorder(BorderFactory.createLoweredBevelBorder());
        return (JComponent) _viewHolder;
    }

    public void addChangeListener(ChangeListener listener) {
        _viewHolder.addChangeListener(listener);
    }
    
    public void addProjectViewListener(ProjectViewListener pvl) {
        projectViewListeners.add(this, pvl);
    }
    
    public void removeProjectViewListener(ProjectViewListener pvl) {
        projectViewListeners.remove(this, pvl);
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
        Component[] components = _viewHolder.getComponents();
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
        return Arrays.asList(_viewHolder.getComponents());
    }

    public void setSelectedTab(TabWidget tab) {
        _viewHolder.setSelectedComponent((Component) tab);
    }

    public TabWidget getSelectedTab() {
        return (TabWidget) _viewHolder.getSelectedComponent();
    }

    /**
     * @deprecated returns null.  Use the other methods on this class to manipulate views.
     */
    public JTabbedPane getTabbedPane() {
        return _viewHolder instanceof JTabbedPane ? (JTabbedPane) _viewHolder : null;
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
                _viewHolder.remove((Component) tab);
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
                if (currentIndex != index && currentIndex != -1) {
                    TabWidget tab = (TabWidget) _viewHolder.getComponent(currentIndex);
                    addTab(tab, index);
                }
                ++index;
            }
        }
    }

    private int getTabIndex(WidgetDescriptor d) {
        return _viewHolder.indexOfComponent((Component) getTab(d.getWidgetClassName()));
    }

    public void reloadAll() {
        closeDetachedTabs();
        if (_viewHolder != null) {
            ComponentUtilities.dispose((Component) _viewHolder);
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

    private static void closeDetachedTab(TabWidget tab) {
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

    private static boolean isEnabled(TabWidget tab) {
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
        int index = _viewHolder.getSelectedIndex();
        Component c = _viewHolder.getSelectedComponent();
        _viewHolder.removeTabAt(index);
        TabWidget widget = (TabWidget) c;
        widget.getDescriptor().setVisible(false);
        ComponentUtilities.dispose(c);
    }

    public void detachCurrentView() {
        JComponent c = (JComponent) _viewHolder.getSelectedComponent();
        int index = _viewHolder.getSelectedIndex();
        _viewHolder.removeTabAt(index);
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
        _viewHolder.setSelectedIndex(index);
        _detachedTabs.remove(c);
    }

    private int getInsertionPoint(Component c) {
        String classNameToMatch = c.getClass().getName();
        int insertionPoint = 0;
        Iterator i = _project.getTabWidgetDescriptors().iterator();
        while (i.hasNext() && insertionPoint < _viewHolder.getComponentCount()) {
            WidgetDescriptor desc = (WidgetDescriptor) i.next();
            String className = desc.getWidgetClassName();
            if (className.equals(classNameToMatch)) {
                break;
            }
            if (className.equals(_viewHolder.getComponent(insertionPoint).getClass().getName())) {
                ++insertionPoint;
            }
        }
        return insertionPoint;
    }

    public void setAutosynchronizeClsTrees(boolean b) {
        ApplicationProperties.setAutosynchronizingClsTrees(b);
        synchronizeClsTree();
    }

    //ESCA-JAVA0130 
    public boolean isAutosynchronizingClsTrees() {
        return ApplicationProperties.isAutosynchronizingClsTrees();
    }

    private void setCurrentClsPath(Collection c) {
        _currentClsPath = new ArrayList(c);
    }

    private void setCurrentInstances(Collection instances) {
        _currentInstances = (instances == null) ? null : new ArrayList(instances);
    }

    public void synchronizeClsTree() {
        if (_currentClsPath != null) {
            TabWidget currentWidget = (TabWidget) _viewHolder.getSelectedComponent();
            if (currentWidget != null) {
                currentWidget.synchronizeClsTree(_currentClsPath);
            }
        }
    }

    public void synchronizeInstances() {
        if (_currentInstances != null) {
            TabWidget currentWidget = (TabWidget) _viewHolder.getSelectedComponent();
            if (currentWidget != null) {
                currentWidget.synchronizeToInstances(_currentInstances);
            }
        }
    }
}
