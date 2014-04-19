package edu.stanford.smi.protege.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.stanford.smi.protege.action.ExportToCsvUtil;
import edu.stanford.smi.protege.model.Cls;
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
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.SelectableList;

public class ExportConfigurationPanel {

	private KnowledgeBase kb;

	private JPanel configPanel;
	private JCheckBox exportTypesCheckBox;
	private JCheckBox exportHeaderCheckBox;
	private JCheckBox exportBrowserTextCheckBox;
	private JCheckBox exportMetadataCheckBox;
	private JCheckBox exportSuperclassesCheckBox;
	private JCheckBox showSystemSlots;
	private FileField fileField;
	private JTextField slotDelimiterTextField;
	private JTextField slotValuesDelimiterTextField;

	private Collection<Slot> slots = new LinkedHashSet<Slot>();
	private Collection<Slot> possibleSlots = new LinkedHashSet<Slot>();
	private Collection<Cls> clses = new LinkedHashSet<Cls>();

	private String metadataString = "";
	private boolean showExportSuperclasses = false;
	private boolean showExportClasses = false;


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

		fileField = new FileField("Exported file", getExportedFileName(), ExportToCsvUtil.EXPORT_FILE_EXTENSION, "Exported result files" );
		configPanel.add(fileField);

		if (showExportClasses) {
			LabeledComponent clsesListComp = getClsesListComponent();
			configPanel.add(clsesListComp);
		}

		LabeledComponent slotsListComp = getSlotsListComponent();
		configPanel.add(slotsListComp);

		configPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel p0 = new JPanel(new GridLayout(1,1));
		showSystemSlots = ComponentFactory.createCheckBox("Show system slots");
		showSystemSlots.setAlignmentX(Component.LEFT_ALIGNMENT);
		p0.add(showSystemSlots);
		configPanel.add(p0);

		JPanel p1 = new JPanel(new GridLayout(2,2));
		p1.add(new JLabel("Slot delimiter"));
		slotDelimiterTextField = new JTextField(ExportToCsvUtil.getSlotsDelimiter(), 10);
		p1.add(slotDelimiterTextField);

		p1.add(new JLabel("Slot values delimiter"));
		slotValuesDelimiterTextField = new JTextField(ExportToCsvUtil.getSlotValuesDelimiter(), 10);
		p1.add(slotValuesDelimiterTextField);
		configPanel.add(p1);

		configPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel p2 = new JPanel(new GridLayout(showExportSuperclasses ? 5 : 4,1));

		exportHeaderCheckBox = ComponentFactory.createCheckBox("Export slots name as first line in the file");
		exportHeaderCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		exportHeaderCheckBox.setSelected(true);
		p2.add(exportHeaderCheckBox);

		exportTypesCheckBox = ComponentFactory.createCheckBox("Export instance type(s)");
		exportTypesCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		exportTypesCheckBox.setSelected(false);
		p2.add(exportTypesCheckBox);

		exportBrowserTextCheckBox = ComponentFactory.createCheckBox("Export browser text (instead of name)");
		exportBrowserTextCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		exportBrowserTextCheckBox.setSelected(ExportToCsvUtil.isExportBrowserTextEnabled());
		p2.add(exportBrowserTextCheckBox);

		if (showExportSuperclasses) {
			exportSuperclassesCheckBox = ComponentFactory.createCheckBox("Export superclasses");
			exportSuperclassesCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
			exportSuperclassesCheckBox.setSelected(ExportToCsvUtil.isExportSuperclass());
			p2.add(exportSuperclassesCheckBox);
		}

		p2.add(getExportMetadataComponent());
		configPanel.add(p2);

		return configPanel;
	}


	private JComponent getExportMetadataComponent() {
		boolean exportMetadata = ExportToCsvUtil.isExportMetadata();

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

		exportMetadataCheckBox = ComponentFactory.createCheckBox("Export additional text as last line in the file");
		exportMetadataCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		exportMetadataCheckBox.setSelected(exportMetadata);

		p.add(exportMetadataCheckBox);
		p.add(Box.createRigidArea(new Dimension(10, 0)));

		final JButton editTextButton = new JButton("Edit text");
		editTextButton.setEnabled(exportMetadata);
		editTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onEditText();
			}
		});

		exportMetadataCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean selected = exportMetadataCheckBox.isSelected();
				editTextButton.setEnabled(selected);
				ExportToCsvUtil.setExportMetadata(selected);
			}
		});

		p.add(editTextButton);

		return p;
	}


	protected void onEditText() {
		 JTextArea _comment = ComponentFactory.createTextArea();
		 _comment.setPreferredSize(new Dimension(300, 150));
		 _comment.setText(getExportMetadataText());
		 LabeledComponent lc = new LabeledComponent("Information to be exported in the last line", new JScrollPane(_comment));
		 int rval = ModalDialog.showDialog(configPanel, lc, "Export information", ModalDialog.MODE_OK_CANCEL);
         if (rval == ModalDialog.OPTION_OK) {
        	 setExportMetadata(_comment.getText());
         }
	}


	private LabeledComponent getSlotsListComponent() {
		final SelectableList slotsList = ComponentFactory.createSelectableList(null);
		slotsList.setCellRenderer(FrameRenderer.createInstance());
		ComponentUtilities.addListValues(slotsList, slots);
		LabeledComponent labeledComp = new LabeledComponent("Slots to export", new JScrollPane(slotsList), true );

		labeledComp.addHeaderButton(new AllowableAction("Add slots", Icons.getAddSlotIcon(), null) {
			private static final long serialVersionUID = -8082099266752807423L;

            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
				HashSet<Slot> allSlots = new HashSet<Slot>();
				Iterator<Slot> j = kb.getSlots().iterator();
				while (j.hasNext()) {
					Slot s = j.next();
					if (showSystemSlots.isSelected() || !s.isSystem()) {
						allSlots.add(s);
					}
				}

				allSlots.add(kb.getNameSlot());
				allSlots.addAll(possibleSlots);

				List<Slot> allSlotsList = new ArrayList<Slot>(allSlots);
				Collections.sort(allSlotsList, new FrameComparator<Slot>());

				// Show util window for multiple slot selection
				Collection<Slot> newSlots = DisplayUtilities.pickSlots(configPanel, allSlotsList, "Select slots to export (multiple selection)");
				slots.addAll(newSlots);

				ComponentUtilities.clearListValues(slotsList);
				ComponentUtilities.addListValues(slotsList, slots);
			}
		});

		labeledComp.addHeaderButton(new AllowableAction("Remove slot", Icons.getRemoveSlotIcon(), slotsList) {
			private static final long serialVersionUID = -1292919958871934527L;

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

	private LabeledComponent getClsesListComponent() {
		final SelectableList clsesList = ComponentFactory.createSelectableList(null);
		clsesList.setCellRenderer(FrameRenderer.createInstance());
		ComponentUtilities.setListValues(clsesList, clses);
		LabeledComponent labeledComp = new LabeledComponent("Classes to export", new JScrollPane(clsesList), true );

		labeledComp.addHeaderButton(new AllowableAction("Add classes", Icons.getAddClsIcon(), null) {
			private static final long serialVersionUID = -8306176540928522503L;

            public void actionPerformed(ActionEvent e) {
				HashSet<Cls> allClses = new HashSet<Cls>();

				List<Cls> allClsesList = new ArrayList<Cls>(allClses);
				Collections.sort(allClsesList, new FrameComparator<Cls>());

				// Show util window for multiple slot selection
				Collection<Cls> newClses = DisplayUtilities.pickClses(configPanel, kb, kb.getRootClses());
				clses.addAll(newClses);

				ComponentUtilities.clearListValues(clsesList);
				ComponentUtilities.addListValues(clsesList, clses);
			}
		});

		labeledComp.addHeaderButton(new AllowableAction("Remove cls", Icons.getRemoveClsIcon(), clsesList) {
			private static final long serialVersionUID = 6906830668815822195L;

            public void actionPerformed(ActionEvent arg0) {
				Collection selection = getSelection();
				if (selection != null) {
					clses.removeAll(selection);
				}
				ComponentUtilities.clearListValues(clsesList);
				ComponentUtilities.addListValues(clsesList, clses);
			}
		});

		return labeledComp;
	}

	@SuppressWarnings("deprecation")
	private String getExportedFileName(){
		String projPath = kb.getProject().getProjectFilePath();;
		String projName = kb.getProject().getProjectName();

		if (kb.getProject().isMultiUserClient()) {
			//use the application directory
			projPath = ApplicationProperties.getApplicationDirectory().getAbsolutePath();
			projName = "query";
		}

		String filename = FileUtilities.replaceFileName(projPath, projName + ExportToCsvUtil.EXPORT_FILE_PREFIX);
		filename = filename + "." + ExportToCsvUtil.EXPORT_FILE_EXTENSION;

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

	public boolean isExportBrowserTextEnabled(){
		return exportBrowserTextCheckBox.isSelected();
	}

	public boolean isExportSuperclassEnabled(){
		return exportSuperclassesCheckBox != null && exportSuperclassesCheckBox.isSelected();
	}

	public boolean isExportMetadataEnabled() {
		return exportMetadataCheckBox.isSelected();
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

		for (Object element : replaceChars.keySet()) {
			String ch = (String) element;
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

	/**
	 * Give a chance to other parts of the code to set the intial classes
	 * that will show up in the exported classes list
	 * @param possibleSlots
	 */
	public void setInitialExportClasses(Collection<Cls> initialExportClses) {
		//we clear previous selection - if this is not desired, just remove the line
		clses.clear();
		clses.addAll(initialExportClses);
	}

	public Collection<Cls> getExportedClassesInPanel() {
		return clses;
	}

	public String getExportMetadataText() {
		return metadataString;
	}

	public void setExportMetadata(String text) {
		metadataString = text;
	}

	public void setShowExportSuperclasses(boolean showExportSuperclasses) {
		this.showExportSuperclasses = showExportSuperclasses;
	}

	public void setShowExportedClasses(boolean showExportClasses) {
		this.showExportClasses = showExportClasses;
	}

}
