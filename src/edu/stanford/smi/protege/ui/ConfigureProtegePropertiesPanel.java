package edu.stanford.smi.protege.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JTabbedPane;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.AbstractValidatableComponent;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.widget.ProtegePropertiesComponent;

/**
 * Configuration panel for setting the proteg.properties and protege LAX properties
 * @author ttania
 *
 */
public class ConfigureProtegePropertiesPanel extends AbstractValidatableComponent {
	
	private static final long serialVersionUID = 1829006876667647966L;

    private final String PROTEGE_LAX_FILE = "Protege.lax";
	
	private Project _project;
	private ProtegePropertiesComponent _protegeProp;
	private ProtegePropertiesComponent _protegeLax;
	private Properties _copyProtegeProperties;
	
	public ConfigureProtegePropertiesPanel(Project project) {
		_project = project;
		buildGUI();
	}
	
	private void buildGUI() {
		setLayout(new BorderLayout());
		
		JTabbedPane _tabbedPane = ComponentFactory.createTabbedPane(true);
		
		_copyProtegeProperties = new Properties();
		
		copyProperties(ApplicationProperties.getApplicationProperties(), _copyProtegeProperties);
		
		_protegeProp = new ProtegePropertiesComponent(_copyProtegeProperties);
		_protegeProp.setVisibleHeaderButton(_protegeProp.getLoadAction(), false);
		
		_tabbedPane.addTab(ApplicationProperties.FILE_NAME,_protegeProp);
				
		File laxFile = getLaxFile();
		
		if (laxFile != null && laxFile.exists()) {
			_protegeLax = new ProtegePropertiesComponent(laxFile);
			_protegeLax.setVisibleHeaderButton(_protegeLax.getLoadAction(), false);
			
			_tabbedPane.addTab(PROTEGE_LAX_FILE, _protegeLax);
		}
		
		add(_tabbedPane);		
	}

	private void copyProperties(Properties source, Properties target) {
		target.clear();
		
		for (Iterator iter = source.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();			
			target.setProperty(key, source.getProperty(key));			
		}		
	}

	private File getLaxFile() {
		File appDir = ApplicationProperties.getApplicationDirectory();
		
		return new File(appDir,PROTEGE_LAX_FILE);	
	}

	public void saveContents() {
		_protegeProp.stopCellEditing();
		if (_protegeLax != null)
			_protegeLax.stopCellEditing();
				
		copyProperties(_copyProtegeProperties, ApplicationProperties.getApplicationProperties());		
		ApplicationProperties.flush();
		
		File laxFile = getLaxFile();
		if (_protegeLax!= null && laxFile != null && laxFile.exists()) {
			_protegeLax.savePropertyFile(laxFile,true);
		}
	}

	public boolean validateContents() {
		return true;
	}

}
