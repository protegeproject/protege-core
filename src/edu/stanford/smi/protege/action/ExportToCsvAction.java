package edu.stanford.smi.protege.action;

import java.awt.Component;
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
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.metaproject.impl.UnbackedOperationImpl;
import edu.stanford.smi.protege.ui.ExportConfigurationPanel;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.FileUtilities;
import edu.stanford.smi.protege.util.FrameWithBrowserText;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.StandardAction;


/**
 * This action class allows the export of an instance list to a CSV file. <br><br>
 * Delimiters between different slots and slots values can be configured
 * (see {@link #setSlotsDelimiter(String)} and {@link #setSlotValuesDelimiter(String)}).
 * Slots to be exported for each instance can be configured (see {@link #setSlotsToExport(Collection)}.
 * <br><br>
 *  It works both with an UI and without. To use it without a UI, call the setter methods to configure the
 *  export action and then call {@link #export()}.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ExportToCsvAction extends StandardAction {

	private static final long serialVersionUID = 8536785619825508769L;

	public static final Operation EXPORT_TO_CSV_OPERATION = new UnbackedOperationImpl("ExportToCSV", null);

	private static final char NEW_LINE = '\n';
	private static final char QUOTE_CHAR = '"';

	private KnowledgeBase kb;
	private Collection<Instance> instancesToExport = new ArrayList<Instance>();
	private File exportFile;
	private Collection<Slot> slotsToExport = new ArrayList<Slot>();
	private String exportMetadataText;
	private boolean exportHeader;
	private boolean exportTypes;
	private boolean exportMetadata;
	private boolean exportSuperclass;
	private boolean exportCompletedSuccessul = false;
	
	private ExportConfigurationPanel exportConfigurationPanel;

	public ExportToCsvAction(KnowledgeBase kb) {
		this(kb, "Export Slot Values to file", Icons.getQueryExportIcon());
	}

	protected ExportToCsvAction(KnowledgeBase kb, String name, Icon icon) {
		super(name, icon);
		this.kb = kb;
	}

	public void actionPerformed(ActionEvent arg0) {
		exportCompletedSuccessul = false;
		exportConfigurationPanel = getExportConfigurationPanel();
		exportConfigurationPanel.setPossibleSlots(getSlotsToExport());
		exportConfigurationPanel.setExportMetadata(exportMetadataText);
		exportConfigurationPanel.setInitialExportClasses(getInitialExportClses());

		int sel = ModalDialog.showDialog(getParentComponent(), exportConfigurationPanel.getConfigPanel(), "Export configuration", ModalDialog.MODE_OK_CANCEL);
		if (sel == ModalDialog.OPTION_CANCEL) {
			return;
		}
		
		exportFile = exportConfigurationPanel.getExportedFile();
		slotsToExport = exportConfigurationPanel.getExportedSlots();
		exportHeader = exportConfigurationPanel.isExportHeaderEnabled();
		exportTypes = exportConfigurationPanel.isExportTypesEnabled();
		exportSuperclass = exportConfigurationPanel.isExportSuperclassEnabled();
		exportMetadata = exportConfigurationPanel.isExportMetadataEnabled();

		//write configuration to protege.properties
		setSlotsDelimiter(exportConfigurationPanel.getSlotDelimiter());
		setSlotValuesDelimiter(exportConfigurationPanel.getSlotValuesDelimiter());
		setExportBrowserText(exportConfigurationPanel.isExportBrowserTextEnabled());
		setExportMetadata(exportConfigurationPanel.isExportMetadataEnabled());
		setExportSuperclass(exportConfigurationPanel.isExportSuperclassEnabled());
		setExportMetadata(exportConfigurationPanel.getExportMetadataText());		

		exportCompletedSuccessul = export();

		String messageText = exportCompletedSuccessul ? "Query results exported successfully to:\n" + exportFile.getAbsolutePath() :
			"There were errors at saving query results.\n" +
			"Please consult the console for more details.";

		ModalDialog.showMessageDialog(getParentComponent(), messageText, exportCompletedSuccessul ? "Export successful" : "Errors at export");
	}


	public boolean export() {
		boolean success = false;

		try {
			Writer outputStream = FileUtilities.createBufferedWriter(exportFile);

			if (outputStream == null) {
				Log.getLogger().log(Level.WARNING, "Unable to open output file " + exportFile + ".");
			} else {
				printResults(outputStream, getInstancesToExport(), slotsToExport);
				success = true;
			}

		} catch (Exception ex) {
			Log.getLogger().log(Level.WARNING, "Errors at writing out query results file " + exportFile + ".", ex);
		}

		return success;
	}

	protected void printResults(Writer writer, Collection<Instance> instances, Collection<Slot> slots) {
		PrintWriter output = new PrintWriter(writer);

		if (exportHeader) {
			printHeader(output, slots);
		}

		Iterator<Instance> i = instances.iterator();
		while (i.hasNext()) {
			Instance instance = i.next();
			printInstance(output, instance);
		}

		if (exportMetadata) {
			printMetadata(output);
		}

		output.close();
	}

	private void printMetadata(PrintWriter writer) {
		if (exportMetadataText == null || exportMetadataText.length() == 0) {
			return;
		}
		writer.println();
		writer.println();
		writer.println(getQuotedValule(exportMetadataText));
	}

	protected void printHeader(PrintWriter writer, Collection<Slot> slots) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("Instance");
		buffer.append(getSlotsDelimiter());

		if (exportTypes) {
			buffer.append("Type(s)");
			buffer.append(getSlotsDelimiter());
		}
		
		if (exportSuperclass) {
			buffer.append("Superclass(es)");
			buffer.append(getSlotsDelimiter());
		}

		for (Object element : slots) {
			Slot slot = (Slot) element;
			buffer.append(getExportName(slot));
			buffer.append(getSlotsDelimiter());
		}

		writer.println(buffer.toString());
	}

	protected void printInstance(PrintWriter writer, Instance instance) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getExportName(instance));

		if (exportTypes) {
			buffer.append(ExportToCsvUtil.getSlotsDelimiter());
			Collection directTypes = instance.getDirectTypes();
			Iterator i = directTypes.iterator();
			while (i.hasNext()) {
				Cls directType = (Cls) i.next();
				buffer.append(getExportName(directType));
				if (i.hasNext()) {
					buffer.append(getSlotValuesDelimiter());
				}
			}
		}
		
		if (exportSuperclass) {
			buffer.append(ExportToCsvUtil.getSlotsDelimiter());			
			Iterator<Cls> i = getSuperclassesToExport(instance).iterator();
			while (i.hasNext()) {
				Cls supercls = (Cls) i.next();
				buffer.append(getExportName(supercls));
				if (i.hasNext()) {
					buffer.append(getSlotValuesDelimiter());
				}
			}
		}

		// Export the own slot values for each slot attached to the current instance
		if (!slotsToExport.isEmpty()) {
			Iterator<Slot> j = slotsToExport.iterator();
			while (j.hasNext()) {
				Slot slot = j.next();
				buffer.append(getSlotsDelimiter());
				buffer.append(getSlotValuesExportString(instance, slot));
			}
		}

		writer.println(buffer.toString());
	}
	
	protected String getSlotValuesExportString(Instance instance, Slot slot) {
		StringBuffer buffer = new StringBuffer();

		Collection values = instance.getOwnSlotValues(slot);

		// Loop through the values for a particular slot
		Iterator i = values.iterator();
		while (i.hasNext()) {
			Object value = i.next();
			if (value instanceof Frame) {
				Frame frame = (Frame) value;
				buffer.append(getExportName(frame));
			} else {
				buffer.append(getExportDataValueName(value));
			}

			if (i.hasNext()) {
				buffer.append(getSlotValuesDelimiter());
			}
		}

		return getQuotedValule(buffer.toString());
	}



	protected String getQuotedValule(String value) {
		if (value == null || value.length() == 0) {
			return value;
		}

		StringBuffer buffer = new StringBuffer(value);

		for (int i = 0; i < buffer.length(); i++) {
			if (buffer.charAt(i) == QUOTE_CHAR) {
				buffer.insert(i, QUOTE_CHAR);
				i = i + 1;
			}
		}

		/* 
		 * at svn 18655 there was a conditional here.  It did not 
		 * work as it did not take into account that the slot/slot-value
		 * delimiter was a string and not just a character.
		 * 
		 * Why not just always quote?
		 */
		buffer.insert(0, QUOTE_CHAR);
		buffer.insert(buffer.length(), QUOTE_CHAR);
		
		return buffer.toString();
	}


	protected String getExportName(Frame frame) {
		return getQuotedValule(isExportBrowserTextEnabled() ? frame.getBrowserText() : frame.getName());
	}
	
	protected String getExportDataValueName(Object data) {
	    return  data.toString();
	}


	protected ExportConfigurationPanel getExportConfigurationPanel() {
		if (exportConfigurationPanel != null) {
			return exportConfigurationPanel;
		}

		exportConfigurationPanel = new ExportConfigurationPanel(kb);
		return exportConfigurationPanel;
	}

	protected Collection<Cls> getSuperclassesToExport(Instance inst) {
		Collection<Cls> superclses = new ArrayList<Cls>();
		if (!(inst instanceof Cls)) { return superclses; }
		Cls cls = (Cls) inst;
		return cls.getDirectSuperclasses();
	}	
	
	protected Component getParentComponent() {
		return ProjectManager.getProjectManager().getCurrentProjectView();		
	}
	
	protected Collection<Cls> getInitialExportClses() {
		return new ArrayList<Cls>();
	}

	/*
	 * Public setter and getter methods
	 */
	
	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}

	public Collection<Instance> getInstancesToExport() {
		return instancesToExport;
	}

	public void setInstancesToExport(Collection<Instance> instancesToExport) {
		this.instancesToExport = instancesToExport;
	}
	
	public void setFramesWithBrowserTextToExport(Collection<FrameWithBrowserText> frames) {
	    instancesToExport = new ArrayList<Instance>();
	    for (FrameWithBrowserText frameWithBrowserText : frames) {
            Frame frame = frameWithBrowserText.getFrame();
            if (frame instanceof Instance) {
                instancesToExport.add((Instance) frame);
            }
        }
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

	public void setExportMetadata(String metadata) {
		this.exportMetadataText = metadata;
	}

	public String getExportMetadata() {
		return exportMetadataText;
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

	public boolean isExportMetadata() {
		return exportMetadata;
	}

	public void setExportMetadata(boolean exportMetadata) {
		this.exportMetadata = exportMetadata;
		ExportToCsvUtil.setExportMetadata(exportMetadata);
	}
		
	public void setExportSuperclass(boolean exportSuperclass) {
		this.exportSuperclass = exportSuperclass;
		ExportToCsvUtil.setExportSuperclass(exportSuperclass);
	}

	public boolean isExportSuperclass() {
		return exportSuperclass;
	}	

	public String getSlotValuesDelimiter() {
		return ExportToCsvUtil.getSlotValuesDelimiter();
	}

	public void setSlotValuesDelimiter(String slotValuesDelimiter) {
		ExportToCsvUtil.setSlotValuesDelimiter(slotValuesDelimiter);
	}

	public String getSlotsDelimiter() {
		return ExportToCsvUtil.getSlotsDelimiter();
	}

	public void setSlotsDelimiter(String slotsDelimiter) {
		ExportToCsvUtil.setSlotsDelimiter(slotsDelimiter);
	}

	public boolean isExportBrowserTextEnabled() {
		return ExportToCsvUtil.isExportBrowserTextEnabled();
	}

	public void setExportBrowserText(boolean exportBrowserText) {
		ExportToCsvUtil.setExportBrowserText(exportBrowserText);
	}
	
	public boolean exportCompletedSuccessful() {
		return exportCompletedSuccessul;
	}

}
