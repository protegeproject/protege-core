package edu.stanford.smi.protege.widget;

/**
 * Tab plugin that allows the graphical handling of property files.
 * @author ttania
 *
 */
public class ProtegePropertiesTab extends AbstractTabWidget {
	private ProtegePropertiesComponent propComp;
	
	public void initialize() {
		setLabel("ProtegePropertiesTab");
			
		propComp = new ProtegePropertiesComponent();

		add(propComp);
	};

	
	public static void main(String[] args) {
		edu.stanford.smi.protege.Application.main(args);
	}

}
