package edu.stanford.smi.protege.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.Icon;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.ExportConfigurationPanel;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.StandardAction;


/**
 * This action class allows the export of an instance list to a CSV file. <br><br>
 * Delimiters between different slots and slots values can be configured
 * (see {@link #setSlotDelimiter(String)} and {@link #setSlotValueDelimiter(String)}).
 * Slots to be exported for each instance can be configured (see {@link #setSlotsToExport(Collection)}. 
 * <br><br>
 *  It works both with an UI and without. To use it witout a UI, call the setter methods to configure the
 *  export action and then call {@link #export()}.
 *  
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ExportToCsvAction extends StandardAction {

	private KnowledgeBase kb;	
	private Collection<Instance> instancesToExport = new ArrayList<Instance>();
	private File exportFile;
	private Collection<Slot> slotsToExport = new ArrayList<Slot>();
	private boolean exportHeader;
	private boolean exportTypes;
	private String slotValueDelimiter = ExportConfigurationPanel.DEFAULT_SLOT_VALUES_DELIMITER;
	private String slotDelimiter = ExportConfigurationPanel.DEFAULT_SLOTS_DELIMITER;

	private ExportConfigurationPanel exportConfigurationPanel;
	

	public ExportToCsvAction(KnowledgeBase kb) {
		this(kb, "Export Slot Values to file", Icons.getQueryExportIcon());	
	}

	protected ExportToCsvAction(KnowledgeBase kb, String name, Icon icon) {
		super(name, icon);
		this.kb = kb;
	}

	public void actionPerformed(ActionEvent arg0) {
		exportConfigurationPanel = getExportConfigurationPanel();

		int sel = ModalDialog.showDialog(ProjectManager.getProjectManager().getCurrentProjectView(), exportConfigurationPanel.getConfigPanel(), "Export configuration", ModalDialog.MODE_OK_CANCEL);

		if (sel == ModalDialog.OPTION_CANCEL) {
			return;
		}

		exportFile = exportConfigurationPanel.getExportedFile();
		slotsToExport = exportConfigurationPanel.getExportedSlots();
		exportHeader = exportConfigurationPanel.isExportHeaderEnabled();
		exportTypes = exportConfigurationPanel.isExportTypesEnabled();
		slotDelimiter = exportConfigurationPanel.getSlotDelimiter();
		slotValueDelimiter = exportConfigurationPanel.getSlotValuesDelimiter();		

		boolean success = export();

		String messageText = success ? "Query results exported successfully to:\n" + exportFile.getAbsolutePath() :
			"There were errors at saving query results.\n" +
			"Please consult the console for more details.";

		ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getCurrentProjectView(), 
				messageText, success ? "Export successful" : "Errors at export");
	}
	
	
	public boolean export() {
		boolean success = false;

		try {
			Writer outputStream = FileUtilities.createBufferedWriter(exportFile);

			if (outputStream == null) {
				Log.getLogger().log(Level.WARNING, "Unable to open output file " + exportFile + ".");
			} else {
				printResults(outputStream, instancesToExport, slotsToExport);
				success = true;
			}

		} catch (Exception ex) {
			Log.getLogger().log(Level.WARNING, "Errors at writing out query results file " + exportFile + ".", ex);					
		}
		
		return success;
	}

	private void printResults(Writer writer, Collection<Instance> instances, Collection slots) {
		PrintWriter output = new PrintWriter(writer);

		if (exportHeader) {
			printHeader(output, slots);
		}

		Iterator i = instances.iterator();
		while (i.hasNext()) {
			Instance instance = (Instance) i.next();
			printInstance(output, instance);
		}
		output.close();
	}

	private void printHeader(PrintWriter writer, Collection slots) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("Instance");
		buffer.append(slotDelimiter);

		if (exportTypes) {
			buffer.append("Type(s)");
			buffer.append(slotDelimiter);        	
		}

		for (Iterator iter = slots.iterator(); iter.hasNext();) {
			Slot slot = (Slot) iter.next();
			buffer.append(slot.getBrowserText());
			buffer.append(slotDelimiter);
		}

		writer.println(buffer.toString());
	}

	private void printInstance(PrintWriter writer, Instance instance) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(instance.getBrowserText());

		if (exportTypes) {
			buffer.append(slotDelimiter);
			Collection directTypes = instance.getDirectTypes();
			Iterator i = directTypes.iterator();
			while (i.hasNext()) {
				Cls directType = (Cls) i.next();
				buffer.append(directType.getBrowserText());
				if (i.hasNext()) {
					buffer.append(slotValueDelimiter);
				}
			}
		}

		// Export the own slot values for each slot attached to the
		// current instance.
		if (!slotsToExport.isEmpty()) { 
			// Loop through slots attached to instance.
			Iterator<Slot> j = slotsToExport.iterator();
			while (j.hasNext()) {
				Slot slot = (Slot) j.next();

				Collection values = instance.getOwnSlotValues(slot);            	
				buffer.append(slotDelimiter);            	

				// Loop through values for particular slot.
				Iterator k = values.iterator();
				while (k.hasNext()) {
					Object value = k.next();
					if (value instanceof Frame) {
						Frame frame = (Frame) value;
						value = frame.getBrowserText();            		
					}
					buffer.append(value);

					if (k.hasNext()) {
						buffer.append(slotValueDelimiter);
					}
				}
			}
		}

		writer.println(buffer.toString());
	}

	private ExportConfigurationPanel getExportConfigurationPanel() {
		if (exportConfigurationPanel != null) {
			return exportConfigurationPanel;
		}

		exportConfigurationPanel = new ExportConfigurationPanel(kb);
		return exportConfigurationPanel;
	}

	
	/*
	 * Public setter and getter methods
	 */
	
	public Collection<Instance> getInstancesToExport() {
		return instancesToExport;
	}

	public void setInstancesToExport(Collection<Instance> instancesToExport) {
		this.instancesToExport = instancesToExport;
	}

	public File getExportFile() {
		return exportFile;
	}

	public void setExportFile(File exportFile) {
		this.exportFile = exportFile;
	}

	public Collection<Slot> getSlotsToExport() {
		return slotsToExport;
	}

	public void setSlotsToExport(Collection<Slot> slotsToExport) {
		this.slotsToExport = slotsToExport;
	}

	public boolean isExportHeader() {
		return exportHeader;
	}

	public void setExportHeader(boolean exportHeader) {
		this.exportHeader = exportHeader;
	}

	public boolean isExportTypes() {
		return exportTypes;
	}

	public void setExportTypes(boolean exportTypes) {
		this.exportTypes = exportTypes;
	}

	public String getSlotValueDelimiter() {
		return slotValueDelimiter;
	}

	public void setSlotValueDelimiter(String slotValueDelimiter) {
		this.slotValueDelimiter = slotValueDelimiter;
	}

	public String getSlotDelimiter() {
		return slotDelimiter;
	}

	public void setSlotDelimiter(String slotDelimiter) {
		this.slotDelimiter = slotDelimiter;
	}

}
