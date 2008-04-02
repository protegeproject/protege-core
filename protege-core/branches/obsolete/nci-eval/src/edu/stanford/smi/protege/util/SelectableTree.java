package edu.stanford.smi.protege.util;

import java.awt.*;
import java.awt.dnd.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.jgoodies.plaf.*;

/**
 * A JTree component that implements the {@link Selectable} interface.  It also implements Autoscroll so the 
 * drag scrolling works.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectableTree extends JTree implements Selectable, Disposable, Autoscroll {
    private ListenerCollection _selectionListeners = new ListenerList(new SelectionEventDispatcher());
    private static final Insets SCROLL_INSETS = new Insets(8, 8, 8, 8);

    private TreeModelListener listener = new TreeModelListener() {
        public void treeNodesInserted(TreeModelEvent event) {
            expandRow(0);
        }
        public void treeNodesChanged(TreeModelEvent event) {
        }
        public void treeNodesRemoved(TreeModelEvent event) {
        }
        public void treeStructureChanged(TreeModelEvent event) {
        }
    };

    public SelectableTree(Action doubleClickAction) {
        this(doubleClickAction, null);
    }

    public SelectableTree(Action doubleClickAction, LazyTreeRoot root) {
        super(new LazyTreeModel(root));
        ComponentFactory.configureTree(this, doubleClickAction);
        getModel().addTreeModelListener(listener);
        expandRow(0);
        addMouseListener(new TreePopupMenuMouseListener(this) {
            public JPopupMenu getPopupMenu() {
                return SelectableTree.this.getPopupMenu();
            }
        });
        addTreeSelectionListener(new TreeSelectionListenerAdapter(this));
        putClientProperty(Options.TREE_LINE_STYLE_KEY, Options.TREE_LINE_STYLE_NONE_VALUE);
        // setScrollsOnExpand(false);
    }
    
    public void addSelectionListener(SelectionListener selectionListener) {
        _selectionListeners.add(this, selectionListener);
    }

    public void dispose() {
        TreeModel model = getModel();
        if (model instanceof LazyTreeModel) {
            LazyTreeModel lazyModel = (LazyTreeModel) model;
            lazyModel.dispose();
        }
    }

    public JPopupMenu getPopupMenu() {
        return null;
    }

    public Collection getSelection() {
        return ComponentUtilities.getSelection(this);
    }

    public void notifySelectionListeners() {
        _selectionListeners.postEvent(this, SelectionEvent.SELECTION_CHANGED);
    }

    public void removeSelectionListener(SelectionListener selectionListener) {
        _selectionListeners.remove(this, selectionListener);
    }

    public void setRoot(LazyTreeRoot root) {
        LazyTreeModel model = (LazyTreeModel) getModel();
        model.setRoot(root);
        expandRow(0);
    }

    public String toString() {
        return "SelectableTree";
    }

    // these two baffling methods comes from the book "Core Swing: Advanced Programming" by Topley
    public Insets getAutoscrollInsets() {
        Rectangle r = getVisibleRect();
        Dimension size = getSize();
        Insets insets =
            new Insets(
                r.y + SCROLL_INSETS.top,
                r.x + SCROLL_INSETS.left,
                size.height - r.y - r.height + SCROLL_INSETS.bottom,
                size.width - r.x - r.width + SCROLL_INSETS.right);
        return insets;
    }

    public void autoscroll(Point location) {
        JScrollPane scroller = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (scroller != null) {
            JScrollBar hBar = scroller.getHorizontalScrollBar();
            JScrollBar vBar = scroller.getVerticalScrollBar();
            Rectangle r = getVisibleRect();
            if (location.x <= r.x + SCROLL_INSETS.left) {
                scroll(hBar, -1);
            }
            if (location.y <= r.y + SCROLL_INSETS.top) {
                scroll(vBar, -1);
            }
            if (location.x >= r.x + r.width - SCROLL_INSETS.right) {
                scroll(hBar, +1);
            }
            if (location.y >= r.y + r.height - SCROLL_INSETS.bottom) {
                scroll(vBar, +1);
            }
        }
    }

    private static void scroll(JScrollBar bar, int increment) {
        int sign = (increment < 0) ? -1 : +1;
        bar.setValue(bar.getValue() + (sign * bar.getUnitIncrement(increment)));
    }
}
