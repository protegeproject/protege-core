package edu.stanford.smi.protege.plugin;

import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.ui.*;
import edu.stanford.smi.protege.util.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProjectPluginManager {
    private Collection projectPlugins = new ArrayList();

    public ProjectPluginManager() {
        Iterator i = PluginUtilities.getAvailableProjectPluginClassNames().iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            ProjectPlugin plugin = (ProjectPlugin) SystemUtilities.newInstance(name);
            if (plugin != null) {
                projectPlugins.add(plugin);
            }
        }
    }

    public void dispose() {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            plugin.dispose();
        }
        projectPlugins.clear();
        projectPlugins = null;
    }

    public void afterCreate(Project project) {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            plugin.afterCreate(project);
        }
    }

    public void afterLoad(Project project) {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            plugin.afterLoad(project);
        }
    }

    public void afterShow(ProjectView projectView, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            try {
                plugin.afterShow(projectView, toolBar, menuBar);
            } catch (Exception e) {
                Log.getLogger().warning(e.toString());
            }
        }
    }

    public void beforeSave(Project project) {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            plugin.beforeSave(project);
        }
    }

    public void beforeClose(Project project) {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            plugin.beforeClose(project);
        }
    }

    public void beforeHide(ProjectView projectView, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            plugin.beforeHide(projectView, toolBar, menuBar);
        }
    }

}
