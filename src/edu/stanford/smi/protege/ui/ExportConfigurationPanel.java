package edu.stanford.smi.protege.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.FileField;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.SelectableList;

public class ExportConfigurationPanel {

	public static final String EXPORT_FILE_PREFIX = "_exported";
	
	public static final String EXPORT_FILE_EXTENSION = "csv";

	public static final String DEFAULT_SLOT_VALUES_DELIMITER = ",";

	public static final String DEFAULT_SLOTS_DELIMITER = "\\t";
		
	private KnowledgeBase kb;

	private JPanel configPanel;
	
	private JCheckBox exportTypesCheckBox;
	
	private JCheckBox exportHeaderCheckBox;
	
	private FileField fileField;
	
	private JTextField slotDelimiterTextField;
	
	private JTextField slotValuesDelimiterTextField;
	
	private List<Slot> slots = new ArrayList<Slot>();
	
	private Collection<Slot> possibleSlots = new ArrayList<Slot>();
	

	//Ugly, but did not find a better solution
	private static HashMap<String, String> replaceChars = new HashMap<String, String>();
	
	static {
		replaceChars.put("\\t", "\t");
		replaceChars.put("\\b", "\b");
		replaceChars.put("\\n", "\n");
		replaceChars.put("\\f", "\f");
		replaceChars.put("\\r", "\r");		
	}
	
	
	public ExportConfigurationPanel(KnowledgeBase kb) {
		this.kb = kb;		
	}


	public JPanel getConfigPanel() {
		configPanel = new JPanel();
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
			
		fileField = new FileField("Exported file", getExportedFileName(), EXPORT_FILE_EXTENSION, "Exported result files" );
		configPanel.add(fileField);
		
		LabeledComponent slotsListComp = getSlotsListComponent();
		configPanel.add(slotsListComp);
	
		configPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		JPanel p1 = new JPanel(new GridLayout(2,2));
		p1.add(new JLabel("Slot delimiter"));
		slotDelimiterTextField = new JTextField(DEFAULT_SLOTS_DELIMITER, 10);
		p1.add(slotDelimiterTextField);
		configPanel.add(p1);
		
		p1.add(new JLabel("Slot values delimiter"));
		slotValuesDelimiterTextField = new JTextField(DEFAULT_SLOT_VALUES_DELIMITER, 10);
		p1.add(slotValuesDelimiterTextField);		

		configPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		exportHeaderCheckBox = ComponentFactory.createCheckBox("Export slots name as first line in the file");
		exportHeaderCheckBox.setSelected(true);	
		configPanel.add(exportHeaderCheckBox);
		
		configPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		exportTypesCheckBox = ComponentFactory.createCheckBox("Export instances type(s)");
		exportTypesCheckBox.setSelected(false);	
		configPanel.add(exportTypesCheckBox);
		
		return configPanel;
	}
	
	
	private LabeledComponent getSlotsListComponent() {
		final SelectableList slotsList = ComponentFactory.createSelectableList(null);
		slotsList.setCellRenderer(FrameRenderer.createInstance());
		
		ComponentUtilities.addListValues(slotsList, slots);
		
		LabeledComponent labeledComp = new LabeledComponent("Slots to export", new JScrollPane(slotsList), true );
		
		labeledComp.addHeaderButton(new AllowableAction("Add slots", Icons.getAddSlotIcon(), null) {

			public void actionPerformed(ActionEvent e) {
				HashSet<Slot> allSlots = new HashSet<Slot>();
				Iterator j = kb.getSlots().iterator();
				while (j.hasNext()) {
					Slot s = (Slot) j.next();
					if (!s.isSystem()) {
						allSlots.add(s);
					}
				}

				allSlots.add(kb.getNameSlot());
				allSlots.addAll(possibleSlots);
				
				List allSlotsList = new ArrayList<Slot>(allSlots);				
				Collections.sort(allSlotsList, new FrameComparator());
				
				// Show util window for multiple slot selection
				Collection<Slot> newSlots = DisplayUtilities.pickSlots(configPanel, allSlotsList, "Select slots to export (multiple selection)");
				addSlotsIfNotExists(newSlots);
				
				ComponentUtilities.clearListValues(slotsList);
				ComponentUtilities.addListValues(slotsList, slots);							
			}
			
		});
		
		labeledComp.addHeaderButton(new AllowableAction("Remove slot", Icons.getRemoveSlotIcon(), slotsList) {

			public void actionPerformed(ActionEvent arg0) {
				Collection selection = getSelection();
				
				if (selection != null) {
					slots.removeAll(selection);
				}

				ComponentUtilities.clearListValues(slotsList);
				ComponentUtilities.addListValues(slotsList, slots);				
			}
			
		});
		
		return labeledComp;
	}
	
	private void addSlotsIfNotExists(Collection<Slot> newSlots) {
		for (Slot slot : newSlots) {
			if (!slots.contains(slot)) {
				slots.add(slot);
			}
		}
		
		//Collections.sort(slots, new FrameComparator());
	}
	
	private String getExportedFileName(){
		String projPath = kb.getProject().getProjectFilePath();;
		String projName = kb.getProject().getProjectName();
		
		if (kb.getProject().isMultiUserClient()) {
			//use the application directory
			projPath = ApplicationProperties.getApplicationDirectory().getAbsolutePath();
			projName = "query";
		}			
			
		String filename = FileUtilities.replaceFileName(projPath, projName + EXPORT_FILE_PREFIX);
			
		filename = filename + "." + EXPORT_FILE_EXTENSION;
		
		return filename;
	}
	
	
	public boolean isExportTypesEnabled() {
		return exportTypesCheckBox.isSelected();
	}
	
	public Collection<Slot> getExportedSlots() {
		return slots;
	}
	
	public File getExportedFile() {
		return fileField.getFilePath();
	}
	
	public boolean isExportHeaderEnabled() {
		return exportHeaderCheckBox.isSelected();
	}
	
	public String getSlotDelimiter() {
		return getDelimiterString(slotDelimiterTextField.getText());
	}
	
	public String getSlotValuesDelimiter() {
		return getDelimiterString(slotValuesDelimiterTextField.getText());
	}
	
	//Ugly, but did not find better solution
	private String getDelimiterString(String string) {
		String newString = new String(string);
		
		for (Iterator iter = replaceChars.keySet().iterator(); iter.hasNext();) {
			String ch = (String) iter.next();
			newString = newString.replace(ch, replaceChars.get(ch));			
		}
		
		return newString;
	}


	public Collection<Slot> getPossibleSlots() {
		return possibleSlots;
	}


	/**
	 * Give a chance to other parts of the code to set the slots that will show up
	 * when clicking on the "Add Slot" button
	 * @param possibleSlots
	 */
	public void setPossibleSlots(Collection<Slot> possibleSlots) {
		this.possibleSlots = possibleSlots;
	}
	
}
