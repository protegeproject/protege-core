package edu.stanford.smi.protege.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import edu.stanford.smi.protege.Application;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;

/**
 * Utility methods for dealing with various swing components.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ComponentUtilities {
    private static final int BORDER_SIZE = 50;
    private static final int STANDARD_ROW_HEIGHT = 60;
    private static final int STANDARD_COLUMN_WIDTH = 100;

    private static Collection _openWindows = new ArrayList();

    public static TableColumn addColumn(JTable table, TableCellRenderer renderer) {
    	return addColumn(table, null, renderer);
    }

    public static TableColumn addColumn(JTable table, String header, TableCellRenderer renderer) {
        return addColumn(table, renderer, null);
    }

    public static TableColumn addColumn(JTable table, TableCellRenderer renderer, TableCellEditor editor) {
    	return addColumn(table, renderer, null, editor);
    }

    public static TableColumn addColumn(JTable table, TableCellRenderer renderer, String header, TableCellEditor editor) {
        int nColumns = table.getColumnCount();
        TableColumn column = new TableColumn(nColumns);
        column.setCellRenderer(renderer);
        column.setCellEditor(editor);
        column.setHeaderValue(header);
        table.addColumn(column);
        return column;
    }

    public static int addListValue(JList list, Object newValue) {
        return getModel(list).addValue(newValue);
    }

    public static void addListValue(JList list, Object newValue, int index) {
        getModel(list).addValue(newValue, index);
    }

    public static int addListValue(JList list, Object newValue, Comparator comparator) {
        int index = getPositionIndex(list, newValue, comparator);
        addListValue(list, newValue, index);
        return index;
    }

    public static void addListValues(JList list, Collection newValues) {
        if (!newValues.isEmpty()) {
            getModel(list).addValues(newValues);
        }
    }

    public static int addSelectedListValue(JList list, Object newValue) {
        int index = getModel(list).addValue(newValue);
        list.setSelectedIndex(index);
        return index;
    }

    public static void addSelectedListValues(JList list, Collection newValues) {
        if (!newValues.isEmpty()) {
            int index = getModel(list).addValues(newValues);
            list.setSelectionInterval(index, index + newValues.size() - 1);
        }
    }

    public static void addUniqueListValues(JList list, Collection newValues) {
        Collection uniqueValues = new HashSet(newValues);
        uniqueValues.removeAll(getModel(list).getValues());
        addListValues(list, uniqueValues);
    }

    public static void apply(Component component, UnaryFunction f) {
        f.apply(component);
        applyToDescendents(component, f);
    }

    public static void applyToDescendents(Component component, UnaryFunction f) {
        if (component instanceof Container) {
            Container container = (Container) component;
            int count = container.getComponentCount();
            for (int i = 0; i < count; ++i) {
                Component subComponent = container.getComponent(i);
                apply(subComponent, f);
            }
        }
    }

    public static void center(Component c) {
        Dimension screenSize = c.getToolkit().getScreenSize();
        screenSize.width -= BORDER_SIZE;
        screenSize.height -= BORDER_SIZE;
        Dimension componentSize = c.getSize();
        int xPos = (screenSize.width - componentSize.width) / 2;
        xPos = Math.max(xPos, 0);
        int yPos = (screenSize.height - componentSize.height) / 2;
        yPos = Math.max(yPos, 0);
        c.setLocation(new Point(xPos, yPos));
    }

    public static void centerInMainWindow(Component c) {
        Component mainWindow = Application.getMainWindow();
        if (mainWindow == null) {
            center(c);
            return;
        }
        int xPos = mainWindow.getX()  + (mainWindow.getWidth() - c.getWidth()) / 2;
        xPos = Math.max(xPos, 0);
        int yPos = mainWindow.getY() + (mainWindow.getHeight() - c.getHeight()) / 2;
        yPos = Math.max(yPos, 0);
        c.setLocation(new Point(xPos, yPos));
    }

    public static void clearListValues(JList list) {
        getModel(list).clear();
    }

    private static void clearSelectionIfNecessary(JList list, int count) {
        // Workaround for swing bug. Removing all elements from a list does not
        // cause a selection event
        // to get fired. setSelectedIndex(-1) also does not cause an event to
        // fire. Thus we clear the
        // selection manually if the result of the remove is that the list will
        // be empty
        if (list.getModel().getSize() == count) {
            list.clearSelection();
        }
    }

    public static void closeAllWindows() {
        Iterator i = new ArrayList(_openWindows).iterator();
        while (i.hasNext()) {
            Window w = (Window) i.next();
            closeWindow(w);
        }
    }

    public static void closeWindow(Window window) {
        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }

    public static void deregisterWindow(Window window) {
        _openWindows.remove(window);
    }

    private static void disassemble(Component component) {
        if (component instanceof Container) {
            Container container = (Container) component;
            int nSubcomponents = container.getComponentCount();
            for (int i = 0; i < nSubcomponents; ++i) {
                disassemble(container.getComponent(i));
            }
            container.removeAll();
        }
    }

    public static void dispose(Component component) {
        if (component != null) {
            component.setVisible(false);
            UnaryFunction dispose = new UnaryFunction() {
                public Object apply(Object o) {
                    if (o instanceof Disposable) {
                        // Log.getLogger().info("disposing: " + o);
                        ((Disposable) o).dispose();
                    }
                    return Boolean.TRUE;
                }
            };
            apply(component, dispose);
            disassemble(component);
            component.setBackground(Color.pink);
            component.setForeground(Color.green);
        }
    }

    public static void ensureSelectionIsVisible(final JList list) {
    }

    public static void extendSelection(JTree tree, Object userObject) {
        LazyTreeNode selectedNode = (LazyTreeNode) tree.getLastSelectedPathComponent();

        if (selectedNode == null) {
        	return;
        }

        int index = selectedNode.getUserObjectIndex(userObject);
        if (index == -1) {
            Log.getLogger().warning("object not found: " + userObject);
        } else {
            TreeNode node = selectedNode.getChildAt(index);
            setSelectedNode(tree, node);
        }
    }

    private static int fullExpand(JTree tree, TreePath parentPath, int nExpansions) {
        TreeNode parent = (TreeNode) parentPath.getLastPathComponent();
        int count = parent.getChildCount();
        for (int i = 0; i < count && nExpansions > 0; ++i) {
            TreeNode child = parent.getChildAt(i);
            TreePath childPath = parentPath.pathByAddingChild(child);
            //ESCA-JAVA0119
            nExpansions = fullExpand(tree, childPath, nExpansions);
        }
        tree.expandPath(parentPath);
        return --nExpansions;
    }

    public static void fullSelectionCollapse(JTree tree) {
        int startRow = tree.getLeadSelectionRow();
        int stopRow = getStopRow(tree, startRow);
        for (int i = stopRow - 1; i >= startRow; --i) {
            tree.collapseRow(i);
        }
    }

    public static void fullSelectionExpand(JTree tree, int max_expansions) {
        TreePath topPath = tree.getLeadSelectionPath();
        if (topPath != null) {
            fullExpand(tree, topPath, max_expansions);
        }
    }

    public static LazyTreeNode getChildNode(LazyTreeNode node, Object userObject) {
        LazyTreeNode childNode = null;
        int nChildren = node.getChildCount();
        for (int i = 0; i < nChildren; ++i) {
            childNode = (LazyTreeNode) node.getChildAt(i);
            if (equals(childNode.getUserObject(), userObject)) {
                return childNode;
            }
        }
        return null;
    }

    public static boolean equals(Object o1, Object o2) {
        return SystemUtilities.equals(o1, o2);
    }

    public static Component getDescendentOfClass(Class componentClass, Component root) {
        Collection c = getDescendentsOfClass(componentClass, root);
        // Assert.assertTrue("max 1 descendent", c.size() == 0 || c.size() ==
        // 1);
        return (Component) CollectionUtilities.getFirstItem(c);
    }

    public static Collection getDescendentsOfClass(final Class componentClass, Component root) {
        final Collection results = new ArrayList();
        UnaryFunction f = new UnaryFunction() {
            public Object apply(Object o) {
                if (componentClass.isInstance(o)) {
                    results.add(o);
                }
                return null;
            }
        };
        apply(root, f);
        return results;
    }

    public static Dialog getDialog(Component c) {
        return (Dialog) SwingUtilities.windowForComponent(c);
    }

    public static Object getFirstSelectionParent(JTree tree) {
        Object parent;
        LazyTreeNode node = (LazyTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) {
            parent = null;
        } else {
            LazyTreeNode parentNode = node.getLazyTreeNodeParent();
            if (parentNode instanceof LazyTreeRoot) {
                parent = null;
            } else {
                parent = parentNode.getUserObject();
            }
        }
        return parent;
    }

    public static Frame getFrame(Component c) {
        Frame frame;
        if (c instanceof Frame) {
            frame = (Frame) c;
        } else {
            frame = (Frame) SwingUtilities.windowForComponent(c);
        }
        return frame;
    }

    public static Collection getListValues(JList list) {
    	ListModel model = list.getModel();

    	if (model instanceof SimpleListModel) {
    		return ((SimpleListModel)model).getValues();
    	}
        return getGenericListValues(list);
    }


    private static Collection getGenericListValues(JList list) {
    	ArrayList values = new ArrayList();
    	ListModel model = list.getModel();

    	for (int i = 0; i < model.getSize(); i++) {
    		values.add(model.getElementAt(i));
		}
		return values;
	}

    private static SimpleListModel getModel(JList list) {
        ListModel model = list.getModel();
        if (!(model instanceof SimpleListModel)) {
            model = new SimpleListModel();
            list.setModel(model);
        }
        return (SimpleListModel) model;
    }

    private static int getPositionIndex(JList list, Object value, Comparator comparator) {
        int index = Collections.binarySearch(getModel(list).getValues(), value, comparator);
        if (index < 0) {
            index = -(index + 1);
        }
        return index;
    }

    public static Object getSelectedValue(JList list) {
        return CollectionUtilities.getFirstItem(getSelection(list));
    }

    public static Collection getSelection(JList list) {
        return Arrays.asList(list.getSelectedValues());
    }

    public static void setSelectedValue(JList list, Object value) {
        list.setSelectedValue(value, true);
    }

    public static void setSelectedValues(JList list, Collection values) {
        int[] indexes = new int[values.size()];
        int count = 0;
        Iterator i = values.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            indexes[count++] = indexOf(list, o);
        }
        list.setSelectedIndices(indexes);
    }

    private static int indexOf(JList list, Object o) {
        int index = -1;
        ListModel model = list.getModel();
        for (int i = 0; i < model.getSize(); ++i) {
            Object element = model.getElementAt(i);
            if (element.equals(o)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static Collection getSelection(JTable table) {
        TableModel model = table.getModel();
        int[] indices = table.getSelectedRows();
        Collection selection = new ArrayList();
        for (int i = 0; i < indices.length; ++i) {
            selection.add(model.getValueAt(indices[i], 0));
        }
        return selection;
    }

    public static Collection getSelection(JTree tree) {
        return getSelection(tree, Object.class);
    }

    public static Collection getSelection(JTree tree, Class c) {
        Assert.assertNotNull("tree", tree);
        Collection selections = new LinkedHashSet();
        TreePath[] paths = tree.getSelectionModel().getSelectionPaths();
        if (paths != null) {
            for (int i = 0; i < paths.length; ++i) {
                TreePath path = paths[i];
                // if the tree is empty the invisible root node is "selected".
                // We don't want to return this.
                if (path.getPathCount() > 1) {
                    Object o = paths[i].getLastPathComponent();
                    if (o instanceof LazyTreeNode) {
                        o = ((LazyTreeNode) o).getUserObject();
                    }
                    if (c == null || c.isInstance(o)) {
                        selections.add(o);
                    }
                }
            }
        }
        return selections;
    }

    public static int getStandardColumnWidth() {
        return STANDARD_COLUMN_WIDTH;
    }

    public static int getStandardRowHeight() {
        return STANDARD_ROW_HEIGHT;
    }

    private static int getStopRow(JTree tree, int startRow) {
        int startDepth = tree.getPathForRow(startRow).getPathCount();
        int last = tree.getRowCount();
        int stopRow = last;
        for (int i = startRow + 1; i < last; ++i) {
            int depth = tree.getPathForRow(i).getPathCount();
            if (depth <= startDepth) {
                stopRow = i;
                break;
            }
        }
        return stopRow;
    }

    public static TreePath getTreePath(JTree tree, Collection objectPath) {
        Collection nodePath = new LinkedList();
        LazyTreeNode node = (LazyTreeNode) tree.getModel().getRoot();
        nodePath.add(node);
        Iterator i = objectPath.iterator();
        while (i.hasNext()) {
            Object userObject = i.next();
            node = getChildNode(node, userObject);
            if (node == null) {
                //Log.getLogger().warning("Child node not found: " + userObject);
                return null;
            }
            nodePath.add(node);
        }
        return new TreePath(nodePath.toArray());
    }

    public static Collection getObjectPath(TreePath path) {
        Collection objectPath = new ArrayList();
        Object[] nodePath = path.getPath();
        // Skip the root node
        for (int i = 1; i < nodePath.length; ++i) {
            TreeNode node = (TreeNode) nodePath[i];
            if (node instanceof LazyTreeNode) {
                Object o = ((LazyTreeNode) node).getUserObject();
                objectPath.add(o);
            }
        }
        return objectPath;
    }

    public static void hide(final Component c, final int delayInMillisec) {
        Thread t = new Thread() {
            @Override
			public void run() {
                try {
                    sleep(delayInMillisec);
                    Component topComponent = SwingUtilities.getRoot(c);
                    topComponent.setVisible(false);
                } catch (Exception e) {
                    Log.getLogger().severe(Log.toString(e));
                }
            }
        };
        t.start();
    }

    public static boolean isDragAndDropEnabled(JComponent c) {
        Object o = c.getClientProperty(ComponentUtilities.class);
        return o == null ? true : ((Boolean) o).booleanValue();
    }

    public static boolean listValuesContain(JList list, Object value) {
        return getModel(list).contains(value);
    }

    /**
     * Loads a icon from either the file system or from a jar.
     *
     * @param clas A class used to resolve the icon's relative path name
     * @param iconPath the path relative to the clas in which to find the icon.
     * @return an image icon for the icon, or null if not found
     *
     * <pre>
     * for a directory structure like
     *  edu/
     * 		mypackage/
     * 			MyClass.class
     * 			images/
     * 				myicon.gif
     *
     * call loadImageIcon(MyClass.class, "images/myicon.gif");
     *
     * </pre>
     */
    public static ImageIcon loadImageIcon(Class clas, String iconPath) {
        ImageIcon icon = null;
        URL url = clas.getResource(iconPath);
        if (url != null) {
            icon = new ImageIcon(url);
        }
        return icon;
    }

    public static void pack(Component c) {
        Window window = getFrame(c);
        window.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension bounds = new Dimension();
        bounds.width = Math.min(window.getWidth(), screenSize.width * 8 / 10);
        bounds.height = Math.min(window.getHeight(), screenSize.height * 8 / 10);
        window.setSize(bounds);
    }

    public static Dimension getDefaultMainFrameSize() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        size.width = size.width * 8 / 10;
        size.height = size.height * 8 / 10;
        return size;
    }

    public static void paintImmediately(Component component) {
        Graphics g = component.getGraphics();
        component.paint(g);
        g.dispose();
    }

    public static void registerWindow(Window window) {
        _openWindows.add(window);
    }

    public static void removeListValue(JList list, Object oldValue) {
        clearSelectionIfNecessary(list, 1);
        int selectedIndex = list.getSelectedIndex();
        int index = getModel(list).removeValue(oldValue);
        if (selectedIndex == index) {
            setSelectedIndex(list, index);
        }
    }

    public static void removeListValues(JList list, Collection values) {
        clearSelectionIfNecessary(list, values.size());
        int selectedIndex = list.getSelectedIndex();
        int index = getModel(list).removeValues(values);
        if (selectedIndex == index) {
            setSelectedIndex(list, index);
        }
    }

    public static void removeSelection(JTree tree) {
        LazyTreeNode selectedNode = (LazyTreeNode) tree.getLastSelectedPathComponent();
        if (selectedNode != null) {
            LazyTreeNode parentNode = selectedNode.getLazyTreeNodeParent();
            int index = parentNode.getUserObjectIndex(selectedNode.getUserObject());
            int nChildren = parentNode.getChildCount();
            TreeNode newSelection;
            if (index == nChildren - 1) {
                if (nChildren == 1) {
                    newSelection = parentNode;
                } else {
                    newSelection = parentNode.getChildAt(index - 1);
                }
            } else {
                newSelection = parentNode.getChildAt(index + 1);
            }
            setSelectedNode(tree, newSelection);
        }
    }

    public static void replaceListValue(JList list, Object oldValue, Object newValue) {
        SimpleListModel model = getModel(list);
        if (newValue == null) {
            model.removeValue(oldValue);
        } else {
            int index = model.indexOf(oldValue);
            if (index != -1) {
                model.setValue(index, newValue);
            }
        }
    }

    public static void reposition(JList list, Object value, Comparator comparator) {
        int oldSelectionIndex = list.getSelectedIndex();

        SimpleListModel model = getModel(list);
        int fromIndex = model.indexOf(value);
        model.removeValue(value);

        int toIndex = getPositionIndex(list, value, comparator);
        getModel(list).addValue(value, toIndex);

        if (oldSelectionIndex != -1) {
            int newSelectionIndex = oldSelectionIndex;
            if (fromIndex == oldSelectionIndex) {
                newSelectionIndex = toIndex;
            } else if (fromIndex < oldSelectionIndex && toIndex > oldSelectionIndex) {
                --newSelectionIndex;
            } else if (fromIndex > oldSelectionIndex && toIndex < oldSelectionIndex) {
                ++newSelectionIndex;
            }
            list.setSelectedIndex(newSelectionIndex);
            list.ensureIndexIsVisible(newSelectionIndex);
        }
    }

    public static void setDragAndDropEnabled(JComponent c, boolean enable) {
        c.putClientProperty(ComponentUtilities.class, Boolean.valueOf(enable));
    }

    public static void setEnabled(Component component, final boolean enabled) {
        apply(component, new UnaryFunction() {
            public Object apply(Object o) {
                ((Component) o).setEnabled(enabled);
                return null;
            }
        });
    }

    public static void setExpanded(JTree tree, Collection objectPath, boolean expand) {
        TreePath path = getTreePath(tree, objectPath);
        if (path != null) {
            if (expand) {
                tree.scrollPathToVisible(path);
                tree.expandPath(path);
                tree.updateUI();
            } else {
                tree.collapsePath(path);
            }
        }
    }

    public static void setFrameTitle(Component c, String title) {
        getFrame(c).setTitle(title);
    }

    public static void setListValues(JList list, Collection values) {
        getModel(list).setValues(values);
    }

    private static void setSelectedIndex(JList list, int index) {
        int nElements = list.getModel().getSize();
        index = Math.min(index, nElements - 1);
        list.setSelectedIndex(index);
    }

    /*
     * JLV: wrapped calls to tree.updateUI in SwingUtilities.invokeLater to prevent
     * null pointer exception when users click on sub slots on Slots tab. Workaround
     * to prevent exception is documented on Sun's site, bug ID 5089562.
     *
     * Refer to URL for full bug report: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5089562
     */
    public static void setSelectedNode(final JTree tree, TreeNode node) {
        final TreePath path = new TreePath(((LazyTreeModel) tree.getModel()).getPathToRoot(node));
        if (path != null) {
        	tree.scrollPathToVisible(path);
        	tree.setSelectionPath(path);

            SwingUtilities.invokeLater(new Runnable() {
            	public void run() {
            		tree.updateUI();
            	}
            });
        }

        SwingUtilities.invokeLater(new Runnable() {
        	public void run() {
        		tree.updateUI();
        	}
        });
    }

    public static void setSelectedObjectPath(final JTree tree, Collection objectPath) {
        final TreePath path = getTreePath(tree, objectPath);
        if (path != null) {
            final WaitCursor cursor = new WaitCursor(tree);
            tree.scrollPathToVisible(path);
            tree.updateUI();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    tree.setSelectionPath(path);
                    tree.updateUI();
                    cursor.hide();
                }
            });
        }
    }

    public static void setSelectedObjectPaths(JTree tree, Collection objectPaths) {
        Collection treePaths = new ArrayList();
        Iterator i = objectPaths.iterator();
        while (i.hasNext()) {
            Collection objectPath = (Collection) i.next();
            treePaths.add(getTreePath(tree, objectPath));
        }
        TreePath[] paths = (TreePath[]) treePaths.toArray(new TreePath[treePaths.size()]);
        tree.setSelectionPaths(paths);
        if (paths.length > 0) {
            tree.scrollPathToVisible(paths[0]);
            tree.updateUI();
        }
    }

    public static void requestFocus(final JComponent component) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                component.requestFocus();
            }
        });
    }

    public static void setDisplayParent(JTree tree, Object parent, Traverser traverser) {
        Object child = ((LazyTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject();
        setDisplayParent(tree, parent, child, traverser);
    }

    public static void setDisplayParent(JTree tree, Object parent, Object child, Traverser traverser) {
        LinkedList objects = new LinkedList();
        objects.add(0, child);
        while (parent != null) {
            objects.add(0, parent);
            parent = traverser.get(parent);
        }
        TreeNode node = (TreeNode) tree.getModel().getRoot();
        Iterator i = objects.iterator();
        while (i.hasNext() && node != null) {
            Object o = i.next();
            node = getMatchingChildNode(node, o);
        }
        if (node != null) {
            ComponentUtilities.setSelectedNode(tree, node);
        }
    }

    private static TreeNode getMatchingChildNode(TreeNode node, Object child) {
        TreeNode matchingNode = null;
        for (int childIndex = 0; childIndex < node.getChildCount(); ++childIndex) {
            LazyTreeNode childNode = (LazyTreeNode) node.getChildAt(childIndex);
            if (equals(childNode.getUserObject(), child)) {
                matchingNode = childNode;
                break;
            }
        }
        return matchingNode;
    }

    public static JLabel setTitleLabelFont(JLabel label) {
        return modifyLabelFont(label, Font.BOLD, 0);
    }

    public static JLabel setSmallLabelFont(JLabel label) {
        return modifyLabelFont(label, Font.BOLD, 0);
    }

    public static JLabel modifyLabelFont(JLabel label, int style, int delta) {
        Font font = label.getFont();
        label.setFont(font.deriveFont(style, font.getSize() + delta));
        label.setForeground(new Color(140, 140, 140));
        return label;
    }

    public static void enableTextAntialiasing(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;
        if (SystemUtilities.useAntialiasing()) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    public static void enableAllAntialiasing(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;
        if (SystemUtilities.useAntialiasing()) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    /*
     * This border makes up for the 5 pixel border around slot widgets.  It causes the
     * left hand displays to line up with the instance display.
     */
    public static Border getAlignBorder() {
        return BorderFactory.createEmptyBorder(5, 0, 0, 0);
    }


    /**
     * Scrolls a table so that a certain cell becomes visible.
     * Source: http://javaalmanac.com/egs/javax.swing.table/Vis.html
     * @param table
     * @param rowIndex
     * @param vColIndex
     */
    public static void scrollToVisible(JTable table, int rowIndex, int vColIndex) {
        if (!(table.getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport)table.getParent();

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);

        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();

        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x-pt.x, rect.y-pt.y);

        table.scrollRectToVisible(rect);

        // Scroll the area into view
        //viewport.scrollRectToVisible(rect);
    }


    public static JMenu getMenu(JMenuBar menuBar, String menuText) {
    	return getMenu(menuBar, menuText, false);
    }

    public static JMenu getMenu(JMenuBar menuBar, String menuText, boolean create) {
    	return getMenu(menuBar, menuText, create, menuBar.getMenuCount());
    }

	public static JMenu getMenu(JMenuBar menuBar, String menuText, boolean create, int menuIndex) {
		int menuCount = menuBar.getMenuCount();
		for (int i = 0; i < menuCount; i++) {
			JMenu tmpMenu = menuBar.getMenu(i);
			if (tmpMenu.getText().equals(menuText)) {
				return tmpMenu;
			}
		}

		if (create) {
			JMenu menu = new JMenu(menuText);
			menuBar.add(menu, menuIndex);
			menuBar.revalidate();
			menuBar.repaint();
			return menu;
		}
		return null;
	}

	public static void removeMenuItem(JMenu menu, String menuItemText) {
		for (int i = 0; i < menu.getItemCount(); i++) {
			JMenuItem item = menu.getItem(i);
			if (item != null && menuItemText.equals(item.getText())) {
				menu.remove(item);
			}
		}
	}

	public static void setSelectedNode(final KnowledgeBase kb, JTree tree, FrameWithBrowserText value) {
		TreeSelectionHelper<FrameWithBrowserText> selectionHelper = new TreeSelectionHelper<FrameWithBrowserText>(tree) {
			private Slot superclsesSlot = kb.getSystemFrames().getDirectSuperclassesSlot();
			@Override
			protected Collection<FrameWithBrowserText> getParents(FrameWithBrowserText child) {
				edu.stanford.smi.protege.model.Frame frame = child.getFrame();
				return FrameWithBrowserText.getFramesWithBrowserText(frame.getOwnSlotValues(superclsesSlot));
			}

			@Override
			protected boolean isVisible(FrameWithBrowserText element) {
				edu.stanford.smi.protege.model.Frame frame = element.getFrame();
				return frame.isVisible();
			}
		};
		selectionHelper.setSelectedNode(value);
	}
}