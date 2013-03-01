package edu.stanford.smi.protege.util;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class RowTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 4015658826028434227L;
    private JTable table;
    private List rows = new ArrayList();
    
    public RowTableModel(JTable table) {
        this.table = table;
    }
    
    public String getColumnName(int index) {
        return (String) table.getColumnModel().getColumn(index).getHeaderValue();
    }

    public int getColumnCount() {
        return table.getColumnCount();
    }

    public int getRowCount() {
        return rows.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex);
    }
    
    public void addRow(Object row) {
        rows.add(row);
    }
    
    public void clear() {
        rows.clear();
    }
}
