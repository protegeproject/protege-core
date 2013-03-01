package edu.stanford.smi.protege.widget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.table.AbstractTableModel;

import edu.stanford.smi.protege.util.ApplicationProperties;

/**
 * @author ttania
 *
 */
public class PropertiesTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -1781140859643294527L;
    private static String COL_PROP = "Property";
	private static String COL_VAL = "Value";
	private final static int COL_PROP_COLNO = 0;
	private final static int COL_VAL_COLNO = 1;
	
	private Properties properties;

	private List<String> propertyList;

	private String[] columnNames = {COL_PROP, COL_VAL};
	
	private boolean changed = false;

	public PropertiesTableModel() {
		this(null);
	}
	
	public PropertiesTableModel(Properties properties) {
		setProperties(properties);
	}

	private ArrayList<String> fillPropertyList() {
		ArrayList<String> list = new ArrayList<String>();

		for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
			list.add((String) e.nextElement());
		}
		
		Collections.sort(list);
		
		return list;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public int getRowCount() {
		return properties.size();
	}

	public Class getColumnClass(int colIndex) {
		switch (colIndex) {
		case COL_PROP_COLNO:
			return String.class;
		case COL_VAL_COLNO:
			return String.class;
		default:
			return String.class;
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	};
	
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case COL_PROP_COLNO:
			return propertyList.get(rowIndex);
		case COL_VAL_COLNO:
			String text = properties.getProperty(propertyList.get(rowIndex));
			return text;
		default:
			return new String("??");
		}
	}
	
	public void addRow(String prop, String value) {
		properties.setProperty(prop, value);
		propertyList = fillPropertyList();
		
		changed = true;
		fireTableDataChanged();		
	}
	
	public void deleteRow(int rowIndex){
		String propName = propertyList.get(rowIndex);
		properties.remove(propName);
		propertyList = fillPropertyList();
		
		changed = true;
		fireTableRowsDeleted(rowIndex, rowIndex);
	}
	
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case COL_PROP_COLNO:		
			String oldPropName = propertyList.get(rowIndex);
			String value = properties.getProperty(oldPropName,"");
			String newPropName = (String)aValue;
			
			if (oldPropName.equals(newPropName))
				return;
			
			properties.setProperty(newPropName, value);
			properties.remove(oldPropName);
			
			propertyList = fillPropertyList();			
		
			break;
		case COL_VAL_COLNO:
			String propName = propertyList.get(rowIndex);
			
			String oldValue = properties.getProperty(propName, "");			
			String newValue = (String)aValue;
			
			if (oldValue.equals(newValue))
				return;
			
			newValue = newValue.trim();
			properties.setProperty(propName, newValue);
						
			break;
		default:
			break;
		}
		
		changed = true;
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	public Properties getProperties() {
		return properties;
	};
	
	public int getRowOfProperty(String propName){	
		return propertyList.indexOf(propName);
	}

	public int getRowOfPropertyValue(String value){
		int row = -1;
		
		for (int i = 0; i < propertyList.size(); i++) {
			if (((String)properties.getProperty(propertyList.get(i))).equals(value))
					return i;					
		}
		
		return -1;			
	}
	
	public static int getValueColumnIndex() {
		return COL_VAL_COLNO;
	}

	public static int getPropertyColumnIndex() {
		return COL_PROP_COLNO;
	}

	public void setProperties(Properties properties) {
		if (properties == null)
			properties = ApplicationProperties.getApplicationProperties();
		
		this.properties = properties;
		propertyList = fillPropertyList();	
				
		changed = false;
		fireTableDataChanged();
	}

	public boolean isChanged() {
		return changed;
	}

	protected void setChanged(boolean changed){
		this.changed = changed;
	}
}
