package edu.stanford.smi.protege.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 * Renderer to display a check box to acquire a boolean value.
 * 
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */

public class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {

    private static final long serialVersionUID = -4049122419048829462L;

    public CheckBoxRenderer() {
        setHorizontalAlignment(CENTER);
        setOpaque(false);
    }

    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean b,
        int row,
        int col) {
        setSelected(Boolean.TRUE.equals(value));
        return this;
    }
}
