package edu.stanford.smi.protege.plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProjectPluginManager {
    private static final String IS_SUITABLE_METHOD_NAME = "isSuitable";
    private static final Class[] IS_SUITABLE_METHOD_ARGS = new Class[] {Project.class, Collection.class}; 
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
            if (isSuitable(project, plugin)) {
                plugin.afterCreate(project);
            }
        }
    }

    public void afterLoad(Project project) {
		Iterator i = projectPlugins.iterator();
		while (i.hasNext()) {
			ProjectPlugin plugin = (ProjectPlugin) i.next();
			if (isSuitable(project, plugin)) {
				try {
					plugin.afterLoad(project);
				} catch (Exception e) {
					Log.getLogger().log(Level.WARNING,
							"There were errors at loading project plugin " + plugin, e);
				}
			}
		}
	}

    private static boolean isSuitable(Project project, ProjectPlugin projectPlugin) {
        boolean isSuitable;
        try {
            Collection errors = new ArrayList();
            Method method = projectPlugin.getClass().getMethod(IS_SUITABLE_METHOD_NAME, IS_SUITABLE_METHOD_ARGS);
            Boolean returnValue = (Boolean) method.invoke(projectPlugin, new Object[] { project, errors });
            isSuitable = returnValue.booleanValue();
        } catch (NoSuchMethodException e) {
            isSuitable = true;
        } catch (Exception e) {
            isSuitable = false;
            //Log.getLogger().warning(Log.toString(e));
            Log.getLogger().warning(e.getMessage());
        }
        // Log.getLogger().info("is suitable=" + isSuitable + " " + projectPlugin);
        return isSuitable;
    }

    private static boolean isSuitable(ProjectView projectView, ProjectPlugin projectPlugin) {
        return isSuitable(projectView.getProject(), projectPlugin);
    }
    


    public void afterSave(Project project) {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            if (isSuitable(project, plugin)) {
              try {
                plugin.afterSave(project);
              } catch (AbstractMethodError ame) {
                Log.getLogger().warning("Plugin " + plugin + " does not implement the afterSave method");
              }
            }
        }
    }


    public void afterShow(ProjectView projectView, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            if (isSuitable(projectView, plugin)) {
                try {
                    plugin.afterShow(projectView, toolBar, menuBar);
                } catch (Exception e) {
                    Log.getLogger().warning(e.toString());
                    Log.getLogger().log(Level.FINE, "Exception caught", e);
                }
            }
        }
    }

    public void beforeSave(Project project) {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            if (isSuitable(project, plugin)) {
                plugin.beforeSave(project);
            }
        }
    }

    public void beforeClose(Project project) {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            if (isSuitable(project, plugin)) {
                plugin.beforeClose(project);
            }
        }
    }

    public void beforeHide(ProjectView projectView, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
        Iterator i = projectPlugins.iterator();
        while (i.hasNext()) {
            ProjectPlugin plugin = (ProjectPlugin) i.next();
            if (isSuitable(projectView, plugin)) {
                plugin.beforeHide(projectView, toolBar, menuBar);
            }
        }
    }

}
