package edu.stanford.smi.protege.util;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.FocusEvent;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;


/**
 * A JTree component that implements the {@link Selectable} interface.  It also implements Autoscroll so the
 * drag scrolling works.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SelectableTree extends JTree implements Selectable, Disposable, Autoscroll {
    private static final long serialVersionUID = -97980195042663048L;
    private ListenerCollection _selectionListeners = new ListenerList(new SelectionEventDispatcher());
    // private static final Insets SCROLL_INSETS = new Insets(8, 8, 8, 8);
    private static final int SCROLL_BORDER = 8;

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
            @Override
			public JPopupMenu getPopupMenu() {
                return SelectableTree.this.getPopupMenu();
            }
        });
        addTreeSelectionListener(new TreeSelectionListenerAdapter(this));

        try {
        	LookAndFeelUtil.setTreeLineStyle(this);
		} catch (Throwable e) {
			//fail quietly
			Log.emptyCatchBlock(e);
		}

        // setScrollsOnExpand(false);
    }

    public void addSelectionListener(SelectionListener selectionListener) {
        _selectionListeners.add(this, selectionListener);
    }

    public void dispose() {
    	TreeModel model = getModel();
    	if (model != null){
    		model.removeTreeModelListener(listener);
    		if (model instanceof LazyTreeModel) {
    			LazyTreeModel lazyModel = (LazyTreeModel) model;
    			lazyModel.dispose();
    		}
    	}
    	setCellRenderer(null);
    }

    //ESCA-JAVA0130
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

    @Override
	public String toString() {
        return "SelectableTree";
    }

    @Override
	public void processFocusEvent(FocusEvent event) {
        // prevent dispatch of focus event if we have been removed from the screen!
        if (getParent() != null) {
            super.processFocusEvent(event);
        }
    }

    // these two baffling methods comes from the book "Core Swing: Advanced Programming" by Topley
    public Insets getAutoscrollInsets() {
        Rectangle r = getVisibleRect();
        Dimension size = getSize();
        Insets insets = new Insets(r.y + SCROLL_BORDER, r.x + SCROLL_BORDER, size.height - r.y - r.height
                + SCROLL_BORDER, size.width - r.x - r.width + SCROLL_BORDER);
        return insets;
    }

    public void autoscroll(Point location) {
        JScrollPane scroller = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (scroller != null) {
            JScrollBar hBar = scroller.getHorizontalScrollBar();
            JScrollBar vBar = scroller.getVerticalScrollBar();
            Rectangle r = getVisibleRect();
            if (location.x <= r.x + SCROLL_BORDER) {
                scroll(hBar, -1);
            }
            if (location.y <= r.y + SCROLL_BORDER) {
                scroll(vBar, -1);
            }
            if (location.x >= r.x + r.width - SCROLL_BORDER) {
                scroll(hBar, +1);
            }
            if (location.y >= r.y + r.height - SCROLL_BORDER) {
                scroll(vBar, +1);
            }
        }
    }

    private static void scroll(JScrollBar bar, int increment) {
        int sign = increment < 0 ? -1 : +1;
        bar.setValue(bar.getValue() + sign * bar.getUnitIncrement(increment));
    }
}
