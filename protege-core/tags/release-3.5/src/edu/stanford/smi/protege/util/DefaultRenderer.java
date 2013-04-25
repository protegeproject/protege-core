package edu.stanford.smi.protege.util;

//ESCA*JAVA0025

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 * Default implementation of the various renderer interfaces. This renderer is used for all Protege widget rendering.
 * This ensures a uniform look when a frame is displayed in a tree, a list, and a table. This renderer also handles the
 * drag over feedback needed to supplement Java's pitiful drag and drop support.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DefaultRenderer extends JComponent implements TreeCellRenderer, ListCellRenderer, TableCellRenderer {
    private static final long serialVersionUID = -5077332037549517658L;
    public static final Object DROP_TARGET = new Object();
    public static final Object DROP_TARGET_AREA = new Object();
    public static final Object DROP_TARGET_AREA_ON = new Object();
    public static final Object DROP_TARGET_AREA_BELOW = new Object();
    public static final Object DROP_TARGET_AREA_ABOVE = new Object();

    protected Color _backgroundNormalColor;
    protected Color _foregroundNormalColor;
    protected Color _backgroundSelectionColor;
    protected Color _foregroundSelectionColor;
    protected Color _backgroundSelectionColorOverride;
    private static final int ICON_TEXT_GAP = 3;
    private static final int TEXT_ICON_GAP = 0;
    private static final int ICON_ICON_GAP = 0;
    private static final int TEXT_TEXT_GAP = 0;
    protected FontMetrics _fontMetrics;
    protected LookAndFeel _cachedLookAndFeel;
    protected boolean _hasFocus;
    protected boolean _isSelected;
    protected boolean _grayedText;
    protected boolean _grayedSecondaryText = true;
    protected Color _focusRectColor = Color.GRAY;

    protected List _elements = new ArrayList();

    protected Object _dropTargetArea;
    protected Color _dropTargetLineColor = Color.black;

    // there should be a better way to cache these
    protected Color _treeForegroundSelectionColor;
    protected Color _treeBackgroundSelectionColor;
    protected Color _treeForegroundNormalColor;
    protected Color _treeBackgroundNormalColor;

    protected JTree _tree;
    protected int _row;
    protected Object _value;

    public DefaultRenderer() {
        loadTreeColors();
    }

    public void setBackgroundSelectionColor(Color color) {
        _backgroundSelectionColorOverride = color;
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

    protected Object getValue() {
        return _value;
    }

    protected void setFontStyle(int style) {
        setFont(getFont().deriveFont(style));
    }

    protected Color getBackgroundColor() {
        return (_isSelected) ? getBackgroundSelectionColor() : _backgroundNormalColor;
    }

    protected Color getBackgroundSelectionColor() {
        return _backgroundSelectionColorOverride == null ? _backgroundSelectionColor
                : _backgroundSelectionColorOverride;
    }

    public Component getListCellRendererComponent(JList list, Object value, int row, boolean selected, boolean hasFocus) {
        _foregroundSelectionColor = list.getSelectionForeground();
        _backgroundSelectionColor = list.getSelectionBackground();
        _foregroundNormalColor = list.getForeground();
        _backgroundNormalColor = list.getBackground();
        return setup(list, value, hasFocus, selected);
    }

    public Dimension getPreferredSize() {
        Dimension d = new Dimension(0, _fontMetrics.getHeight());
        Object previousElement = null;
        Iterator i = _elements.iterator();
        while (i.hasNext()) {
            Object element = i.next();
            d.width += getGap(previousElement, element);
            updateSize(d, element);
            previousElement = element;
        }

        if (true) {
            if (_tree != null && !gettingRowBounds) {
                gettingRowBounds = true;
                Rectangle bounds = _tree.getRowBounds(_row);
                if (bounds != null) {
                    d.width = Math.max(d.width, getNiceWidth(_tree, bounds.x));
                }
                gettingRowBounds = false;
            }
        }
        return d;
    }

    private static int getNiceWidth(JComponent component, int x) {
        int width;
        JViewport port = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, component);
        if (port != null) {
            Rectangle r = port.getViewRect();
            width = r.x + r.width - x;
        } else {
            width = component.getWidth() - x;
        }
        return width;
    }

    // hack to avoid infinite recursion
    private boolean gettingRowBounds;

    private static int getGap(Object previousElement, Object currentElement) {
        int gap;
        if (previousElement == null) {
            gap = currentElement instanceof String ? 3 : 0;
        } else if (previousElement instanceof Icon) {
            if (currentElement instanceof Icon) {
                gap = ICON_ICON_GAP;
            } else {
                gap = ICON_TEXT_GAP;
            }
        } else {
            if (currentElement instanceof Icon) {
                gap = TEXT_ICON_GAP;
            } else {
                gap = TEXT_TEXT_GAP;
            }
        }
        return gap;
    }

    private void updateSize(Dimension d, Object element) {
        if (element instanceof Icon) {
            updatePreferredSize(d, (Icon) element);
        } else if (element instanceof String) {
            updatePreferredSize(d, (String) element);
        } else {
            Log.getLogger().warning("Strange thing in element list: " + element);
        }
    }

    private static void updatePreferredSize(Dimension d, Icon icon) {
        d.width += icon.getIconHeight();
        d.height = Math.max(d.height, icon.getIconHeight());
    }

    private void updatePreferredSize(Dimension d, String text) {
        d.width += _fontMetrics.stringWidth(text);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus,
            int row, int col) {
        _foregroundSelectionColor = table.getSelectionForeground();
        _backgroundSelectionColor = table.getSelectionBackground();
        _foregroundNormalColor = table.getForeground();
        _backgroundNormalColor = table.getBackground();
        return setup(table, value, hasFocus, selected);
    }

    protected Color getTextColor() {
        Color textColor;
        if (_isSelected) {
            textColor = (_backgroundSelectionColorOverride == null) ? _foregroundSelectionColor
                    : _foregroundNormalColor;
        } else {
            textColor = (_grayedText) ? Color.darkGray : _foregroundNormalColor;
        }
        return textColor;
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        _foregroundSelectionColor = _treeForegroundSelectionColor;
        _backgroundSelectionColor = _treeBackgroundSelectionColor;
        _foregroundNormalColor = _treeForegroundNormalColor;
        _backgroundNormalColor = _treeBackgroundNormalColor;
        _tree = tree;
        _row = row;
        return setup(tree, value, hasFocus, selected);
    }

    public void load(Object o) {
        addText(o.toString());
    }

    public void loadNull() {
        addText("<null>");
    }

    protected void loadTreeColors() {
        _treeForegroundSelectionColor = UIManager.getColor("Tree.selectionForeground");
        _treeBackgroundSelectionColor = UIManager.getColor("Tree.selectionBackground");
        _treeForegroundNormalColor = UIManager.getColor("Tree.textForeground");
        _treeBackgroundNormalColor = UIManager.getColor("Tree.textBackground");
    }

    public void paint(Graphics graphics) {
        ComponentUtilities.enableTextAntialiasing(graphics);

        graphics.setFont(getFont());
        _fontMetrics = graphics.getFontMetrics();
        Dimension currentSize = getSize();

        graphics.setColor(getBackgroundColor());
        graphics.fillRect(0, 0, currentSize.width, currentSize.height);

        graphics.setColor(getTextColor());

        Point position = new Point();
        Object previousElement = null;
        boolean paintedString = false;
        Iterator i = _elements.iterator();
        while (i.hasNext()) {
            Object currentElement = i.next();
            position.x += getGap(previousElement, currentElement);
            if (currentElement instanceof Icon) {
                paintIcon(graphics, (Icon) currentElement, position, currentSize);
            } else if (currentElement instanceof String) {
                Color color = null;
                if (paintedString && _grayedSecondaryText) {
                    color = Color.gray;
                }
                paintString(graphics, (String) currentElement, position, color, currentSize);
                paintedString = true;
            }
            previousElement = currentElement;
        }

        if (position.x > currentSize.width + 1) {
            int y = 0;
            String dots = "...";
            int dotsWidth = _fontMetrics.stringWidth(dots);
            graphics.setColor(getBackgroundColor());
            graphics.fillRect(currentSize.width - dotsWidth, y, dotsWidth, currentSize.height);
            graphics.setColor(getTextColor());
            graphics.drawString(dots, currentSize.width - dotsWidth, y + _fontMetrics.getAscent());
        }

        if (_hasFocus) {
            graphics.setColor(_focusRectColor);
            graphics.drawRect(0, 0, currentSize.width - 1, currentSize.height - 1);
        }
        if (_dropTargetArea == DROP_TARGET_AREA_ON) {
            graphics.setColor(_dropTargetLineColor);
            graphics.drawRect(0, 0, position.x - 1, currentSize.height - 1);
        } else if (_dropTargetArea == DROP_TARGET_AREA_BELOW) {
            graphics.setColor(_dropTargetLineColor);
            int y = currentSize.height - 1;
            graphics.drawLine(0, y, currentSize.width - 1, y);
        } else if (_dropTargetArea == DROP_TARGET_AREA_ABOVE) {
            graphics.setColor(_dropTargetLineColor);
            graphics.drawLine(0, 0, currentSize.width - 1, 0);
        } else {
            // do nothing
        }
    }

    protected void paintIcon(Graphics graphics, Icon icon, Point position, Dimension size) {
        int y = (size.height - icon.getIconHeight()) / 2;
        icon.paintIcon(this, graphics, position.x, y);
        position.x += icon.getIconWidth();
    }

    protected void paintString(Graphics graphics, String text, Point position, Dimension size) {
        paintString(graphics, text, position, null, size);
    }

    protected void paintString(Graphics graphics, String text, Point position, Color color, Dimension size) {
        if (color != null) {
            graphics.setColor(color);
        }
        int y = (size.height + _fontMetrics.getAscent()) / 2 - 2; // -2 is a bizarre fudge factor that makes it look
        // better!
        graphics.drawString(text, position.x, y);
        position.x += _fontMetrics.stringWidth(text);
    }

    public void setForegroundColorOverride(Color color) {
        _foregroundNormalColor = color;
        _foregroundSelectionColor = color;
    }

    public void setGrayedText(boolean b) {
        _grayedText = b;
    }

    public void setGrayedSecondaryText(boolean b) {
        _grayedSecondaryText = b;
    }

    public void setInvalid() {
    }

    public String getMainText() {
        return (String) getFirst(String.class);
    }

    public Icon getMainIcon() {
        return (Icon) getFirst(Icon.class);
    }

    //ESCA-JAVA0130 
    public int getIconTextGap() {
        return ICON_TEXT_GAP;
    }

    //ESCA-JAVA0130 
    public int getTextIconGap() {
        return TEXT_ICON_GAP;
    }

    //ESCA-JAVA0130 
    public int getTextTextGap() {
        return TEXT_TEXT_GAP;
    }

    //ESCA-JAVA0130 
    public int getIconIconGap() {
        return ICON_ICON_GAP;
    }

    public Object getFirst(Class clas) {
        Object first = null;
        Iterator i = _elements.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (clas.isInstance(o)) {
                first = o;
                break;
            }
        }
        return first;
    }

    private static int indexOfClass(List elements, Class clas) {
        int index = -1;
        for (int i = 0; i < elements.size(); ++i) {
            Object o = elements.get(0);
            if (clas.isInstance(o)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void setMainIcon(Icon icon) {
        int index = indexOfClass(_elements, Icon.class);
        if (index == 0) {
            _elements.remove(index);
        }
        if (icon != null) {
            _elements.add(0, icon);
        }
    }

    public void setMainText(String text) {
        int index = indexOfClass(_elements, String.class);
        if (index == 0 || index == 1) {
            _elements.remove(index);
        }
        addText(text);
    }

    public void appendIcon(Icon icon) {
        addIcon(icon);
    }

    public void appendText(String text) {
        addText(text);
    }

    public void addIcon(Icon icon) {
        if (icon != null) {
            _elements.add(icon);
        }
    }

    public void addText(String text) {
        if (text != null) {
            _elements.add(text);
        }
    }

    public void setNormalForegroundColor(Color color) {
        _foregroundNormalColor = color;
    }

    protected void loadDuplicate(Object o) {
        load(o);
    }

    protected Component setup(Component c, Object value, boolean hasFocus, boolean isSelected) {
        _grayedText = false;
        _value = value;
        Font font = c.getFont();
        if (font.isBold()) {
            font = font.deriveFont(Font.PLAIN);
        }
        setFont(font);
        _hasFocus = hasFocus;
        _isSelected = isSelected;

        _elements.clear();
        if (value == null) {
            loadNull();
        } else if (value instanceof LazyTreeNode) {
            LazyTreeNode node = (LazyTreeNode) value;
            Object object = node.getUserObject();
            if (node.isDuplicate()) {
                loadDuplicate(object);
            } else {
                load(object);
            }
        } else {
            load(value);
        }

        _fontMetrics = getFontMetrics(getFont());
        LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();
        if (currentLookAndFeel != _cachedLookAndFeel) {
            loadTreeColors();
            _cachedLookAndFeel = currentLookAndFeel;
            if (_cachedLookAndFeel instanceof MetalLookAndFeel) {
                _focusRectColor = MetalLookAndFeel.getFocusColor();
            } else {
                _focusRectColor = Color.GRAY;
            }
        }
        checkDropTarget(c, value);
        return this;
    }

    public void clear() {
        _elements.clear();
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    // overridden for performance reasons to do nothing
    public boolean isOpaque() {
        return true;
    }

    public void validate() {
    }

    public void revalidate() {
    }

    public void invalidate() {
    }

    public void repaint() {
    }

    public void repaint(int x, int y, int w, int h) {
    }

    public void repaint(Rectangle r) {
    }

    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
    }

    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
    }

    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
    }

    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
    }

    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    }

    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
    }

    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    }
}