package edu.stanford.smi.protege.plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;


public class ProjectFixupsPluginManager {

	private final static String PROJECT_FIXUP_PLUGIN = "ProjectFixupPlugin";

	private static final String IS_SUITABLE_METHOD_NAME = "isSuitable";
	private static final Class<?>[] IS_SUITABLE_METHOD_ARGS = new Class[] {KnowledgeBase.class, Collection.class}; 

	public static Collection<Class> getAvailableProjectFixupPluginClasses() {
		return PluginUtilities.getClassesWithAttribute(PROJECT_FIXUP_PLUGIN, "True");
	}
	
	public static Collection<ProjectFixupPlugin> getAvailableProjectFixupPlugins() {
		Collection<ProjectFixupPlugin> plugins = new ArrayList<ProjectFixupPlugin>();  
		Iterator i = getAvailableProjectFixupPluginClasses().iterator();
		while (i.hasNext()) {
			Class pluginclass = (Class) i.next();
			ProjectFixupPlugin plugin = (ProjectFixupPlugin) SystemUtilities.newInstance(pluginclass);
			if (plugin != null) {
				plugins.add(plugin);
			}
		}
		return plugins;
	}
	
	private static boolean isSuitable(KnowledgeBase internalKB, Class projectFixupPluginClass) {
		boolean isSuitable;

		try {
			Collection errors = new ArrayList();
			Method method = projectFixupPluginClass.getMethod(IS_SUITABLE_METHOD_NAME, IS_SUITABLE_METHOD_ARGS);
			Boolean returnValue = (Boolean) method.invoke(projectFixupPluginClass, new Object[] { internalKB, errors });
			isSuitable = returnValue.booleanValue();
		} catch (NoSuchMethodException e) {
			isSuitable = true;
		} catch (Exception e) {
			isSuitable = false;
			Log.getLogger().warning(e.getMessage());
		}

		return isSuitable;
	}
	
	public static void fixProject(KnowledgeBase internalKB) {
		for (Iterator iterator = getAvailableProjectFixupPlugins().iterator(); iterator.hasNext();) {
			ProjectFixupPlugin plugin = (ProjectFixupPlugin) iterator.next();
			if (plugin != null) {
				try {
					if (isSuitable(internalKB, plugin.getClass())) {
					plugin.fixProject(internalKB);
					}
				} catch (Throwable t) {
					Log.getLogger().log(Level.WARNING, "Error at applying project fix up " + plugin.getName(), t);
				}
			}
		}
	}
	
	
}
