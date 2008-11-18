package edu.stanford.smi.protege.util;

import java.awt.*;

import javax.swing.*;

/**
 * A "right mouse" listener for JTables.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class TablePopupMenuMouseListener extends PopupMenuMouseListener {

    protected TablePopupMenuMouseListener(JTable table) {
        super(table);
    }

    public void setSelection(JComponent c, int x, int y) {
        boolean selectRow = true;
        JTable table = (JTable) c;
        int row = table.rowAtPoint(new Point(x, y));
        int[] selectedRows = table.getSelectedRows();
        for (int i = 0; i < selectedRows.length; ++i) {
            if (row == selectedRows[i]) {
                selectRow = false;
                break;
            }
        }
        if (selectRow) {
            table.setRowSelectionInterval(row, row);
        }
    }
}
