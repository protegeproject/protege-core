package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.util.ValidatableTabComponent;


public class PreferencesPanel extends ValidatableTabComponent {
	private static final long serialVersionUID = 102285543910901051L;
    private GeneralPreferencesPanel generalPreferencesPanel;
	private ConfigureProtegePropertiesPanel configureProtegePropertiesPanel;
	
	public PreferencesPanel() {
		generalPreferencesPanel = new GeneralPreferencesPanel();
		configureProtegePropertiesPanel = new ConfigureProtegePropertiesPanel(null);
		addTab("General", generalPreferencesPanel);
		addTab("Property Files", configureProtegePropertiesPanel);
	}
	
	@Override
	public void saveContents() {
		/*
		 * We want the save order to be:
		 * 1.ConfigureProtegePropertiesPanel
		 * 2.GeneralPreferencesPanel
		 * so that no.2 overrides no.1
		 */ 
		configureProtegePropertiesPanel.saveContents();
		generalPreferencesPanel.saveContents();
	}
	
	
}
