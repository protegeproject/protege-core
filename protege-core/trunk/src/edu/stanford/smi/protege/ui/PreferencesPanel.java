package edu.stanford.smi.protege.ui;

import edu.stanford.smi.protege.util.ValidatableTabComponent;


public class PreferencesPanel extends ValidatableTabComponent {
	
	public PreferencesPanel() {
		addTab("General", new GeneralPreferencesPanel());
		addTab("Property Files", new ConfigureProtegePropertiesPanel(null));
	}
	
	
}
