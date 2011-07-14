package edu.stanford.smi.protege.ui;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import edu.stanford.smi.protege.action.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.plugin.*;
import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;
import edu.stanford.smi.protege.widget.WidgetUtilities;

/**
 * The main menu bar for the application.
 *
 * @author Ray Fergerson
 */
public class ProjectMenuBar extends JMenuBar {

    private static final long serialVersionUID = 4169911089158397522L;
    private ButtonGroup _group = new ButtonGroup();
    private Map uris = new HashMap();
    private static final int MAX_LENGTH = 50;

    public ProjectMenuBar() {
        createFileMenu();
        createEditMenu();
        createProjectMenu();
        createWindowMenu();
        createHelpMenu();
    }

    private void createEditMenu() {
        JMenu menu = ComponentFactory.createMenu(ResourceKey.MENUBAR_EDIT);
        createItem(menu, new UndoAction(false));
        createItem(menu, new RedoAction(false));
        createItem(menu, new ShowCommandHistoryAction());
        menu.addSeparator();
        createItem(menu, new Cut(false));
        createItem(menu, new Copy(false));
        createItem(menu, new Paste(false));
        createItem(menu, new Clear(false));
        menu.addSeparator();
        createItem(menu, new InsertUnicodeCharacterAction());
        add(menu);
    }

    private JMenu helpMenu;

    public JMenu add(JMenu menu) {
        if (helpMenu == null) {
            super.add(menu);
        } else {
            int last = getComponentCount() - 1;
            add(menu, last);
        }
        return menu;
    }

    private void createHelpMenu() {
        JMenu menu = ComponentFactory.createMenu(ResourceKey.MENUBAR_HELP);
        add(menu);
        helpMenu = menu;
        ComponentFactory.addMenuItemNoIcon(menu, new DisplayHtml(ResourceKey.HELP_MENU_GETTING_STARTED,
                ApplicationProperties.getGettingStartedURLString()));
        ComponentFactory.addMenuItemNoIcon(menu, new DisplayHtml(ResourceKey.HELP_MENU_FAQ, ApplicationProperties
                .getFAQURLString()));
        ComponentFactory.addMenuItemNoIcon(menu, new DisplayHtml(ResourceKey.HELP_MENU_USERS_GUIDE,
                ApplicationProperties.getUsersGuideURLString()));
        ComponentFactory.addMenuItemNoIcon(menu, new DisplayHtml(ResourceKey.HELP_MENU_ONTOLOGIES_101,
                ApplicationProperties.getOntology101URLString()));

        addPluginDocSubmenu();

        menu.addSeparator();
        ComponentFactory.addMenuItemNoIcon(menu, new ShowIconDialog());
        // ComponentFactory.addMenuItemNoIcon(menu, new CollectGarbageAction());

        menu.addSeparator();
        ComponentFactory.addMenuItemNoIcon(menu, new ShowAboutBox());
        ComponentFactory.addMenuItemNoIcon(menu, new ShowAboutPluginsBox());
        ComponentFactory.addMenuItemNoIcon(menu, new DisplayHtml(ResourceKey.HELP_MENU_CITE_PROTEGE, 
        		ApplicationProperties.getHowToCiteProtegeURLString()));

    }

    private void addPluginDocSubmenu() {
        JMenu menu = ComponentFactory.createMenu(ResourceKey.HELP_MENU_PLUGINS);
        ComponentFactory.addSubmenu(helpMenu, menu);
        Iterator i = PluginUtilities.getPluginComponentNames().iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            URL url = PluginUtilities.getPluginComponentDocURL(name);
            if (url != null) {
                ComponentFactory.addMenuItemNoIcon(menu, new DisplayHtml(name, url.toString()));
            }
        }
        if (menu.getItemCount() == 0) {
            menu.setEnabled(false);
        }
    }

    private static void createItem(JMenu menu, Action action) {
        ComponentFactory.addMenuItem(menu, action);
    }

    private static void createCheckBoxItem(JMenu menu, Action action, boolean selected) {
        ComponentFactory.addCheckBoxMenuItem(menu, action, selected);
    }

    private void addLookAndFeel(JMenu menu, LookAndFeelAction action) {
        JRadioButtonMenuItem item = ComponentFactory.createRadioButtonMenuItem(action);
        _group.add(item);
        if (action.isCurrent()) {
            _group.setSelected(item.getModel(), true);
        }
        menu.add(item);
    }

    private JMenu createLAFMenu() {
        JMenu menu = ComponentFactory.createMenu(ResourceKey.LOOK_AND_FEEL);

        addLookAndFeel(menu, new PlasticLookAndFeelAction());
        UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
        for (int i = 0; i < lookAndFeels.length; ++i) {
            String name = lookAndFeels[i].getName();
            String className = lookAndFeels[i].getClassName();
            addLookAndFeel(menu, new LookAndFeelAction(name, className));
        }
        return menu;
    }

    private void createProjectMenu() {
        JMenu menu = ComponentFactory.createMenu(ResourceKey.MENUBAR_PROJECT);
        loadProjectMenu(menu);
        add(menu);
    }

    private void createFileMenu() {
        JMenu menu = ComponentFactory.createMenu(ResourceKey.MENUBAR_FILE);
        loadFileMenu(menu);
        add(menu);
    }

    private void loadFileMenu(JMenu menu) {
        createItem(menu, new CreateProject(false));
        createItem(menu, new OpenProject(false));
        loadOpenRecent(menu);
        createItem(menu, new CloseProject());
        menu.addSeparator();
        createItem(menu, new SaveProject(false));
        createItem(menu, new SaveProjectAs());
        if (SystemUtilities.showAlphaFeatures()) {
            createItem(menu, new SaveProjectToFormat());
        } else {
            menu.addSeparator();
            ComponentFactory.addSubmenu(menu, createExportSubmenu());
            createItem(menu, new ChangeProjectStorageFormat());
        }

        menu.addSeparator();
        createItem(menu, new SetPreferences());

        menu.addSeparator();
        createItem(menu, new ExitApplication());
    }


    private static void loadProjectMenu(JMenu menu) {
        createItem(menu, new ArchiveProject(false));
        createItem(menu, new RevertProject(false));
        menu.addSeparator();
        createItem(menu, new ManageIncludedProjectsAction());
        createItem(menu, new MergeIncludedProjects());
        menu.addSeparator();
        createItem(menu, new ConfigureProject());
        createItem(menu, new ShowMetrics());
        menu.addSeparator();
        ComponentFactory.addMenuItemNoIcon(menu, new ShowEncodingAndLocales());
    }

    /*
     private JMenu createImportSubmenu() {
     JMenu menu = ComponentFactory.createMenu(ResourceKey.PROJECT_IMPORT_TO_STANDARD);
     Collection classNames = PluginUtilities.getAvailableImportPluginClassNames();
     Iterator i = getSortedPlugins(classNames).iterator();
     while (i.hasNext()) {
     ImportPlugin plugin = (ImportPlugin) i.next();
     createItem(menu, new ImportPluginAction(plugin));
     }
     if (menu.getItemCount() == 0) {
     menu.setEnabled(false);
     }
     return menu;
     }
     */

    /*
     * private JMenu createImport2Submenu() { JMenu menu = ComponentFactory.createMenu(ResourceKey.PROJECT_IMPORT);
     * Collection classNames = PluginUtilities.getAvailableImportPlugin2ClassNames(); Iterator i =
     * getPlugins(classNames).iterator(); while (i.hasNext()) { ImportPlugin2 plugin = (ImportPlugin2) i.next();
     * createItem(menu, new ImportAction(plugin)); } if (menu.getItemCount() == 0) { menu.setEnabled(false); } return
     * menu; } private JMenu createExport2Submenu() { JMenu menu =
     * ComponentFactory.createMenu(ResourceKey.PROJECT_EXPORT); Collection classNames =
     * PluginUtilities.getAvailableExportPlugin2ClassNames(); Iterator i = getPlugins(classNames).iterator(); while
     * (i.hasNext()) { ExportPlugin2 plugin = (ExportPlugin2) i.next(); createItem(menu, new ExportAction(plugin)); } if
     * (menu.getItemCount() == 0) { menu.setEnabled(false); } return menu; }
     */

    private static JMenu createExportSubmenu() {
        JMenu menu = ComponentFactory.createMenu(ResourceKey.PROJECT_EXPORT_TO_FORMAT);
        Project prj = ProjectManager.getProjectManager().getCurrentProject();
        menu.setEnabled(prj != null);
        Collection classNames = PluginUtilities.getAvailableExportPluginClassNames();
        Iterator i = getSortedPlugins(classNames).iterator();
        while (i.hasNext()) {
            ExportPlugin plugin = (ExportPlugin) i.next();
            ExportPluginAction exportAction = new ExportPluginAction(plugin);
            exportAction.setEnabled(WidgetUtilities.isSuitableExport(prj, plugin));
            createItem(menu, exportAction);            
        }
        if (menu.getItemCount() == 0) {
            menu.setEnabled(false);
        }
        return menu;
    }

    private static List getSortedPlugins(Collection classNames) {
        List plugins = getPlugins(classNames);
        Collections.sort(plugins, new PluginComparator());
        return plugins;
    }

    private static List getPlugins(Collection classNames) {
        List plugins = new ArrayList();
        Iterator i = classNames.iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            Plugin plugin = (Plugin) SystemUtilities.newInstance(name);
            if (plugin != null) {
                plugins.add(plugin);
            }
        }
        return plugins;
    }

    private static boolean exists(URI uri) {
        return true;
    }

    private void loadOpenRecent(JMenu menu) {
        JMenu subMenu = ComponentFactory.createMenu(ResourceKey.PROJECT_OPEN_RECENT);
        uris.clear();

        List projectURIList = ApplicationProperties.getMRUProjectList();
        subMenu.setEnabled(!projectURIList.isEmpty());
        for (int i = 0; i < projectURIList.size(); i++) {
            URI uri = (URI) projectURIList.get(i);
            boolean exists = exists(uri);
            if (exists) {
                String text = uri.toString();

                // If the file path is too long to display, reduce it.
                if (text.length() > MAX_LENGTH) {
                    ArrayList list = buildList(text);
                    list = reduce(list);
                    text = rebuildPath(list);
                }

                // Map display text to URI
                uris.put(text, uri);

                JMenuItem menuItem = ComponentFactory.createMenuItem(text);
                menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JMenuItem source = (JMenuItem) (e.getSource());
                        String sourcetext = source.getText();
                        boolean closed = ProjectManager.getProjectManager().closeProjectRequest();
                        if (closed) {
                            URI projecturi = (URI) uris.get(sourcetext);
                            ProjectManager.getProjectManager().loadProject(projecturi);
                            ApplicationProperties.addProjectToMRUList(projecturi);
                        }
                    }
                });
                subMenu.add(menuItem);
            }
        }

        ComponentFactory.addSubmenu(menu, subMenu);
    }

    private static ArrayList buildList(String path) {
        ArrayList list = new ArrayList();
        StringTokenizer st = new StringTokenizer(path, "/");
        for (int i = 0; st.hasMoreElements(); i++) {
            list.add(i, st.nextElement());
        }
        return list;
    }

    private ArrayList reduce(ArrayList list) {
        // sanity checks
        if (list == null) {
            return null;
        }
        if (list.size() == 0) {
            return list;
        }
        if (getLength(list) <= MAX_LENGTH) {
            return list;
        }

        // size is 1
        if (list.size() == 1) {
            String fname = (String) list.get(0);
            fname = fname.substring(0, (MAX_LENGTH - 3)) + "...";
            list.set(0, fname);
        }
        // size is 2
        else if (list.size() == 2) {
            if (list.get(0).equals("...")) {
                list.remove(0);
            } else {
                list.set(0, "...");
            }
        }
        // size is 3 or more
        else {
            if (!list.contains("...")) {
                int mid = list.size() / 2;
                list.set(mid, "...");
            } else {
                int idxDot = list.indexOf("...");
                int before = idxDot;
                int after = (list.size() - 2) - idxDot;

                if (before > after) {
                    list.remove(idxDot - 1);
                } else {
                    // since we always remove "after" when before == after,
                    // we are sure that before is always equal to or one more
                    // than before.
                    list.remove(idxDot + 1);
                }
            }
        }

        if (getLength(list) <= MAX_LENGTH) {
            // list is short enough
            return list;
        }
        // list is too long - recurse
        return reduce(list);
    }

    private static int getLength(ArrayList list) {
        String path = "";
        String sep = "/";
        for (int i = 0; i < list.size(); i++) {
            path += list.get(i) + sep;
        }
        return path.length() - 1;
    }

    private static String rebuildPath(ArrayList list) {
        String path = "";
        String sep = "/";
        for (int i = 0; i < list.size(); i++) {
            path += list.get(i) + sep;
        }
        path = path.substring(0, (path.length() - 1));
        return path;
    }

    private void createWindowMenu() {
        final JMenu menu = ComponentFactory.createMenu(ResourceKey.MENUBAR_WINDOW);
        menu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent event) {
                loadWindowMenu(menu);
            }

            public void menuCanceled(MenuEvent event) {
            }

            public void menuDeselected(MenuEvent event) {
            }
        });
        add(menu);
    }

    private void loadWindowMenu(JMenu windowMenu) {
        windowMenu.removeAll();
        createItem(windowMenu, new IncreaseFontSize());
        createItem(windowMenu, new DecreaseFontSize());
        windowMenu.addSeparator();
        createItem(windowMenu, new CascadeWindows(false));
        createItem(windowMenu, new CloseAllWindows(false));
        windowMenu.addSeparator();
        createItem(windowMenu, new SynchronizeTrees());
        boolean autosync = ApplicationProperties.isAutosynchronizingClsTrees();
        createCheckBoxItem(windowMenu, new AutosynchronizeTrees(), autosync);
        windowMenu.addSeparator();
        createItem(windowMenu, new DetachCurrentView());
        createItem(windowMenu, new CloseCurrentView());
        windowMenu.addSeparator();
        ComponentFactory.addSubmenu(windowMenu, createLAFMenu());
        Project project = ProjectManager.getProjectManager().getCurrentProject();
        if (project != null) {
            Collection openWindows = project.getOpenWindows();
            if (!openWindows.isEmpty()) {
                windowMenu.addSeparator();
                Iterator i = openWindows.iterator();
                while (i.hasNext()) {
                    JFrame jframe = (JFrame) i.next();
                    createItem(windowMenu, new JFrameToFront(jframe));
                }
            }
        }
    }

    public String toString() {
        return StringUtilities.getClassName(this);
    }

    public void paint(Graphics g) {
        ComponentUtilities.enableTextAntialiasing(g);
        super.paint(g);
    }
}

class PluginComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        Plugin p1 = (Plugin) o1;
        Plugin p2 = (Plugin) o2;
        return p1.getName().compareTo(p2.getName());
    }
}
