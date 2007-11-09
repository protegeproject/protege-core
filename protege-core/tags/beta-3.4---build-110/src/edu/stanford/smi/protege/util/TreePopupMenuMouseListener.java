package edu.stanford.smi.protege.util;

import javax.swing.JComponent;
import javax.swing.JTree;

/**
 * A "right mouse" popup listener for a JTree.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class TreePopupMenuMouseListener extends PopupMenuMouseListener {

    protected TreePopupMenuMouseListener(JTree tree) {
        super(tree);
    }

    /*
     * Ensure that the row at (x, y) is selected.  If the row is already selected
     * then we don't want to select it again because this would result in other 
     * selected rows being deselected.  We want to all for the possibility of
     * actions that work on multiple selections
     */
    public void setSelection(JComponent c, int x, int y) {
        JTree tree = (JTree) c;
        int row = tree.getRowForLocation(x, y);
        if (row != -1) {
	        boolean selectRow = true;
	        int[] selectedRows = tree.getSelectionRows();
	        if (selectedRows != null) {
	            for (int i = 0; i < selectedRows.length; ++i) {
	                if (row == selectedRows[i]) {
	                    selectRow = false;
	                    break;
	                }
	            }
	        }
	        if (selectRow) {
	            tree.setSelectionRow(row);
	        }
        }
    }
}