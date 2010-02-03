package edu.stanford.smi.protege.action;

import edu.stanford.smi.protege.util.ApplicationProperties;

/**
 * Utility methods for getting/setting the configuration parameters for the export to CSV action (See {@link ExportToCsvAction}). <br>
 * The export configuration is saved in the protege.properties file, so that it is preserved
 * across different Protege sessions.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ExportToCsvUtil {

	public static final String EXPORT_FILE_PREFIX = "_exported";
	public static final String EXPORT_FILE_EXTENSION = "csv";

	public static final String DEFAULT_SLOT_VALUES_DELIMITER = ",";
	public static final String DEFAULT_SLOTS_DELIMITER = "\\t";

	public static final String SLOTS_DELIMITER_PROPERTY = "query.export.slots.delimiter";
	public static final String SLOT_VALUES_DELIMITER_PROPERTY = "query.export.slot.values.delimiter";
	public static final String EXPORT_FILE_PREFIX_PROPERTY = "query.export.file.prefix";
	public static final String EXPORT_FILE_EXTENSION_PROPERTY = "query.export.file.extension";
	public static final String EXPORT_BROWSER_TEXT_PROPERTY = "query.export.browser.text";
	public static final String EXPORT_METADATA_PROPERTY = "query.export.metadata";
	public static final String EXPORT_SUPERCLASS_PROPERTY = "query.export.superclass";

	public static String getSlotValuesDelimiter() {
		return ApplicationProperties.getString(SLOT_VALUES_DELIMITER_PROPERTY, DEFAULT_SLOT_VALUES_DELIMITER);
	}

	public static void setSlotValuesDelimiter(String slotValuesDelimiter) {
		ApplicationProperties.setString(SLOT_VALUES_DELIMITER_PROPERTY, slotValuesDelimiter);
	}


	public static String getSlotsDelimiter() {
		return ApplicationProperties.getString(SLOTS_DELIMITER_PROPERTY, DEFAULT_SLOTS_DELIMITER);
	}

	public static void setSlotsDelimiter(String slotsDelimiter) {
		ApplicationProperties.setString(SLOTS_DELIMITER_PROPERTY, slotsDelimiter);
	}


	public static boolean isExportBrowserTextEnabled() {
		return ApplicationProperties.getBooleanProperty(EXPORT_BROWSER_TEXT_PROPERTY, true);
	}

	public static void setExportBrowserText(boolean exportBrowserText) {
		ApplicationProperties.setBoolean(EXPORT_BROWSER_TEXT_PROPERTY, exportBrowserText);
	}


	public static boolean isExportMetadata() {
		return ApplicationProperties.getBooleanProperty(EXPORT_METADATA_PROPERTY, false);
	}

	public static void setExportMetadata(boolean exportQuery) {
		ApplicationProperties.setBoolean(EXPORT_METADATA_PROPERTY, exportQuery);
	}


	public static boolean isExportSuperclass() {
		return ApplicationProperties.getBooleanProperty(EXPORT_SUPERCLASS_PROPERTY, false);
	}

	public static void setExportSuperclass(boolean exportSuperclass) {
		ApplicationProperties.setBoolean(EXPORT_SUPERCLASS_PROPERTY, exportSuperclass);
	}


	
	/*
	 * Other getter/setter methods can be added if necessary
	 */

}
