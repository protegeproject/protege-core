package edu.stanford.smi.protege.ui;

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.*;

/**
 * The global configuration panel.  This panel consists of a set of tabs that each handles a specific part of the 
 * configuration.
 * Class has been expanded to allow user defined configuration tabs in the project configuration panel. 
 * To add a new tab to the configuration panel, call: ConfigureProjectPanel.registerConfigureTab(String tabTitle, String clsName)
 * To remove a tab from the configuration panel, call: ConfigureProjectPanel.unregisterConfigureTab(String configTabName)
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 * @author    Tania Tudorache <tudorache@stanford.edu>
 */

public class ConfigureProjectPanel extends ValidatableTabComponent {
	private static final long serialVersionUID = 7204453275303618585L;
    private static ArrayList userDefinedConfigTabs = new ArrayList();
	private Project _project;
	
    public ConfigureProjectPanel(Project project) {
    	_project = project;
    	
        addTab("Tab Widgets", new ConfigureTabsPanel(project));
        // addTab("Slot Widgets", new ConfigureWidgetsPanel(project));
        addTab("Options", new ConfigureOptionsPanel(project));

        addUserDefinedConfigTabs();
    }
    
    private void addUserDefinedConfigTabs() {
		for (Iterator iter = userDefinedConfigTabs.iterator(); iter.hasNext();) {
			TabNameClsPair tncp = (TabNameClsPair) iter.next();			
			addConfigTab(tncp.getTabName(), tncp.getTabClsName());
		}
	}

	private void addConfigTab(String tabName, String tabClassName) {
        
        Class prjClass = SystemUtilities.forName("edu.stanford.smi.protege.model.Project");
        
        if (prjClass == null){
        	Log.getLogger().warning("Cannot find class Project");
        	return;
        }
                 	
        Class tabClass = SystemUtilities.forName(tabClassName, true);
        
        if (tabClass == null) {
        	Log.getLogger().warning("Cannot find class: " + tabClassName);
        	return;
        }
        
        Component configTab = null;
        
        try {	
        	Constructor constructor = tabClass.getConstructor(new Class[] {prjClass});            
        	configTab = (Component) constructor.newInstance(new Object[] {_project});
		} catch (Exception e) {
			//Log.getLogger().warning("Cannot initialize constructor with argument of type Project for class " + tabClassName);			
		} finally {
			if (configTab == null) {
				try {
					configTab = (Component) tabClass.newInstance();					
				} catch (Exception e) {
					//Log.getLogger().warning("Cannot initialize default constructor for class " + tabClassName);
				} finally {
					if (configTab == null) 
						Log.getLogger().warning("Cannot initialize constructor for class " + tabClassName);
				}
			}			
		}
		
		if (configTab != null)
			addTab(tabName, configTab); 		
	}

	public  static void registerConfigureTab(String tabTitle, String clsName){	    	
		userDefinedConfigTabs.add(new TabNameClsPair(tabTitle, clsName));
    }
    
    public static void unregisterConfigureTab(String configTabName){
    	boolean found = false;
    	for (Iterator iter = userDefinedConfigTabs.iterator(); iter.hasNext() && !found;) {
			TabNameClsPair tncp = (TabNameClsPair) iter.next();
			if (tncp.getTabName().equals(configTabName)) {
				userDefinedConfigTabs.remove(tncp);
				found = true;
			}			
		}    	
    }   
    
   static class TabNameClsPair {
    	private String _tabName;
    	private String _tabClsName;
    	
    	public TabNameClsPair(String tabName, String tabClsName) {
    		_tabName = tabName;
    		_tabClsName  = tabClsName;   		
    	}
    	
    	public String getTabName() {
    		return _tabName;
    	}
    	
    	public String getTabClsName() {
    		return _tabClsName;    		
    	}
   }
    
}
