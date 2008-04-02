package edu.stanford.smi.protege.util;

import java.awt.*;
import java.awt.dnd.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;

/**
 * Default implementation of the various renderer interfaces.  This renderer is used for all Protege widget rendering.
 * This ensures a uniform look when a frame is displayed in a tree, a list, and a table.  This renderer also handles
 * the drag over feedback needed to supplement Java's pitiful drag and drop support.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class OldDefaultRenderer extends JComponent implements TreeCellRenderer, ListCellRenderer, TableCellRenderer {
    public final static Object DROP_TARGET = new Object();
    public final static Object DROP_TARGET_AREA = new Object();
    public final static Object DROP_TARGET_AREA_ON = new Object();
    public final static Object DROP_TARGET_AREA_BELOW = new Object();
    public final static Object DROP_TARGET_AREA_ABOVE = new Object();

    protected Color _backgroundNormalColor;
    protected Color _foregroundNormalColor;
    protected Color _backgroundSelectionColor;
    protected Color _foregroundSelectionColor;
    protected final int _iconTextGap = 3;
    protected FontMetrics _fontMetrics;
    protected LookAndFeel _cachedLookAndFeel;
    protected boolean _hasFocus;
    protected boolean _isSelected;
    protected boolean _grayedText;

    protected Icon _mainIcon;
    protected String _mainText;
    protected String _trailingText;
    protected Collection _trailingIcons = new ArrayList();

    protected Object _dropTargetArea;
    protected Color _dropTargetLineColor = Color.black;

    // there should be a better way to cache these
    protected Color _treeForegroundSelectionColor;
    protected Color _treeBackgroundSelectionColor;
    protected Color _treeForegroundNormalColor;
    protected Color _treeBackgroundNormalColor;

    public OldDefaultRenderer() {
        loadTreeColors();
    }

    public void appendIcon(Icon icon) {
        _trailingIcons.add(icon);
    }

    public void appendText(String text) {
        _trailingText = text;
    }

    protected void checkDropTarget(Component component, Object value) {
        DropTarget d = component.getDropTarget();
        if (d != null && d.isActive()) {
            Object o = ((JComponent) component).getClientProperty(DROP_TARGET);
            if (equals(o, value)) {
                _dropTargetArea = ((JComponent) component).getClientProperty(DROP_TARGET_AREA);
            } else {
                _dropTargetArea = null;
            }
        }
    }

    protected Color getBackgroundColor() {
        return (_isSelected) ? _backgroundSelectionColor : _backgroundNormalColor;
    }

    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int row,
        boolean selected,
        boolean hasFocus) {
        _foregroundSelectionColor = list.getSelectionForeground();
        _backgroundSelectionColor = list.getSelectionBackground();
        _foregroundNormalColor = list.getForeground();
        _backgroundNormalColor = list.getBackground();
        return setup(list, value, hasFocus, selected);
    }

    public Dimension getPreferredSize() {
        Dimension d = new Dimension(0, _fontMetrics.getHeight());
        if (_mainIcon != null) {
            d.width += _mainIcon.getIconWidth() + _iconTextGap;
            d.height = Math.max(d.height, _mainIcon.getIconHeight());
        }
        if (_mainText != null) {
            d.width += _fontMetrics.stringWidth(_mainText);
        }
        if (!_trailingIcons.isEmpty()) {
            Iterator i = _trailingIcons.iterator();
            while (i.hasNext()) {
                Icon icon = (Icon) i.next();
                d.width += icon.getIconWidth();
            }
        }
        if (_trailingText != null) {
            d.width += _iconTextGap + _fontMetrics.stringWidth(_trailingText);
        }
        return d;
    }

    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean selected,
        boolean hasFocus,
        int row,
        int col) {
        _foregroundSelectionColor = table.getSelectionForeground();
        _backgroundSelectionColor = table.getSelectionBackground();
        _foregroundNormalColor = table.getForeground();
        _backgroundNormalColor = table.getBackground();
        return setup(table, value, hasFocus, selected);
    }

    protected Color getTextColor() {
        Color textColor;
        if (_isSelected) {
            textColor = _foregroundSelectionColor;
        } else {
            textColor = (_grayedText) ? Color.darkGray : _foregroundNormalColor;
        }
        return textColor;
    }

    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus) {
        _foregroundSelectionColor = _treeForegroundSelectionColor;
        _backgroundSelectionColor = _treeBackgroundSelectionColor;
        _foregroundNormalColor = _treeForegroundNormalColor;
        _backgroundNormalColor = _treeBackgroundNormalColor;
        return setup(tree, value, hasFocus, selected);
    }

    public void load(Object o) {
        setMainText(o.toString());
    }

    public void loadNull() {
        setMainText("<null>");
    }

    protected void loadTreeColors() {
        _treeForegroundSelectionColor = UIManager.getColor("Tree.selectionForeground");
        _treeBackgroundSelectionColor = UIManager.getColor("Tree.selectionBackground");
        _treeForegroundNormalColor = UIManager.getColor("Tree.textForeground");
        _treeBackgroundNormalColor = UIManager.getColor("Tree.textBackground");
    }

    public void paint(Graphics g) {
        Dimension preferredSize = getPreferredSize();
        int height = getHeight();
        int width = getWidth();
        Point p = new Point();
        FontMetrics fm = _fontMetrics; // g.getFontMetrics();
        int ascent = fm.getAscent();

        g.setColor(getBackgroundColor());
        g.fillRect(p.x, p.y, width, height);

        p.y += (height - preferredSize.height) / 2;
        g.setColor(getTextColor());
        if (_mainIcon != null) {
            int iconY = p.y + (height - _mainIcon.getIconHeight()) / 2;
            _mainIcon.paintIcon(this, g, p.x, iconY);
            p.x += _mainIcon.getIconWidth() + _iconTextGap;
        }
        if (_mainText != null) {
            g.drawString(_mainText, p.x, p.y + ascent);
            p.x += fm.stringWidth(_mainText);
        }
        if (!_trailingIcons.isEmpty()) {
            Iterator i = _trailingIcons.iterator();
            while (i.hasNext()) {
                Icon icon = (Icon) i.next();
                icon.paintIcon(this, g, p.x, p.y);
                p.x += icon.getIconWidth();
            }
        }
        if (_trailingText != null) {
            p.x += _iconTextGap;
            g.drawString(_trailingText, p.x, p.y + ascent);
            p.x += fm.stringWidth(_trailingText);
        }

        if (p.x > width) {
            String dots = "...";
            int dotsWidth = fm.stringWidth(dots);
            g.setColor(getBackgroundColor());
            g.fillRect(width - dotsWidth, p.y, dotsWidth, height);
            g.setColor(getTextColor());
            g.drawString(dots, width - dotsWidth, p.y + ascent);
        }

        if (_dropTargetArea == DROP_TARGET_AREA_ON) {
            g.setColor(_dropTargetLineColor);
            // g.drawRect(0, 0, getWidth()-1, getHeight()-1);
            g.drawRect(0, 0, p.x - 1, getHeight() - 1);
        } else if (_dropTargetArea == DROP_TARGET_AREA_BELOW) {
            g.setColor(_dropTargetLineColor);
            int y = getHeight() - 1;
            g.drawLine(0, y, getWidth() - 1, y);
        } else if (_dropTargetArea == DROP_TARGET_AREA_ABOVE) {
            g.setColor(_dropTargetLineColor);
            g.drawLine(0, 0, getWidth() - 1, 0);
        } else {
            // do nothing
        }
    }

    protected void setForegroundColorOverride(Color color) {
        _foregroundNormalColor = color;
        _foregroundSelectionColor = color;
    }

    public void setGrayedText(boolean b) {
        _grayedText = b;
    }

    public void setInvalid() {
    }

    public void setMainIcon(Icon icon) {
        _mainIcon = icon;
    }

    public void setMainText(String text) {
        _mainText = text;
    }

    protected void setNormalForegroundColor(Color color) {
        _foregroundNormalColor = color;
    }

    protected Component setup(Component c, Object value, boolean hasFocus, boolean isSelected) {
        _grayedText = false;
        setFont(c.getFont());
        _hasFocus = hasFocus;
        _isSelected = isSelected;

        _mainText = "";
        _mainIcon = null;
        _trailingText = null;
        _trailingIcons.clear();
        if (value == null) {
            loadNull();
        } else if (value instanceof LazyTreeNode) {
            load(((LazyTreeNode) value).getUserObject());
        } else {
            load(value);
        }

        _fontMetrics = getFontMetrics(getFont());
        LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();
        if (currentLookAndFeel != _cachedLookAndFeel) {
            loadTreeColors();
            _cachedLookAndFeel = currentLookAndFeel;
        }
        checkDropTarget(c, value);
        return this;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }
}
